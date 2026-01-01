package net.owen.bladebound;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.owen.bladebound.client.MobHealthHud;
import net.owen.bladebound.client.render.BarrierEntityRenderer;
import net.owen.bladebound.client.screen.SpellScreen;
import net.owen.bladebound.entity.ModEntities;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.mana.ManaHolder;
import net.owen.bladebound.magic.StaffSpell;
import net.owen.bladebound.network.BarrierBreakPayload;
import net.owen.bladebound.network.ClientPackets;
import net.owen.bladebound.network.Payloads;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BladeboundClient implements ClientModInitializer {

    // Discipline NBT keys (for Wado)
    private static final String NBT_ROOT = "bladebound";
    private static final String NBT_DISCIPLINE = "discipline";

    // Spell UI keybind
    private static KeyBinding OPEN_SPELLS;

    // =========================================================
    // Per-spell cooldowns (client-side)
    // spellId -> ticks remaining
    // =========================================================
    private static final Object2IntOpenHashMap<Identifier> SPELL_COOLDOWNS = new Object2IntOpenHashMap<>();

    /**
     * Called by ClientPackets when server starts/updates a spell cooldown.
     * ticks is in TICKS (20 ticks = 1 second).
     */
    public static void clientSetSpellCooldown(Identifier spellId, int ticks) {
        if (spellId == null) return;

        if (ticks <= 0) {
            SPELL_COOLDOWNS.removeInt(spellId);
        } else {
            SPELL_COOLDOWNS.put(spellId, ticks);
        }
    }

    private static void tickSpellCooldownsClientSide(MinecraftClient mc) {
        if (mc.player == null) return;
        if (SPELL_COOLDOWNS.isEmpty()) return;

        var it = SPELL_COOLDOWNS.object2IntEntrySet().fastIterator();
        while (it.hasNext()) {
            Object2IntMap.Entry<Identifier> e = it.next();
            int next = e.getIntValue() - 1;
            if (next <= 0) it.remove();
            else e.setValue(next);
        }
    }

    private static String spellNameFromId(Identifier id) {
        if (id == null) return "Unknown";

        for (StaffSpell s : StaffSpell.values()) {
            if (id.equals(s.id)) {
                return s.displayName;
            }
        }

        return id.getPath();
    }

    @Override
    public void onInitializeClient() {
        // Networking (client receivers)
        Payloads.register();
        ClientPackets.register();
        MobHealthHud.register();

        ClientPlayNetworking.registerGlobalReceiver(BarrierBreakPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().player == null) return;
                context.client().player.playSound(SoundEvents.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
            });
        });

        // Barrier entity renderer
        EntityRendererRegistry.register(ModEntities.BARRIER, BarrierEntityRenderer::new);
        net.owen.bladebound.client.BarrierVisualHud.register();

        // Keybinds
        OPEN_SPELLS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bladebound.open_spells",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.bladebound"
        ));

        // Open spell UI + tick cooldown map
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Tick cooldowns down locally so the UI counts smoothly
            tickSpellCooldownsClientSide(client);

            while (OPEN_SPELLS.wasPressed()) {
                if (client.player == null) return;
                if (client.currentScreen != null) return;
                client.setScreen(new SpellScreen());
            }
        });

        // HUD (ALWAYS render mana + cooldowns when HUD is enabled)
        HudRenderCallback.EVENT.register((DrawContext ctx, RenderTickCounter tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            if (!BladeboundConfig.DATA.hudEnabled) return;

            // Discipline HUD (Wado) stays conditional
            ItemStack stack = mc.player.getMainHandStack();
            if (!stack.isEmpty() && stack.isOf(ModItems.WADOICHIMONJI)) {
                int discipline = readDiscipline(stack);
                renderDisciplineBar(ctx, mc, discipline);
            }

            // Mana HUD ALWAYS (assumes player implements ManaHolder)
            if (mc.player instanceof ManaHolder mana) {
                int currentMana = mana.bladebound$getMana();
                int maxMana = mana.bladebound$getMaxMana();

                int nextLineY = renderManaHud(ctx, mc, currentMana, maxMana);

                // Multi-spell cooldown list ALWAYS (even if empty)
                if (!SPELL_COOLDOWNS.isEmpty()) {
                    List<Object2IntMap.Entry<Identifier>> entries = new ArrayList<>();
                    for (Object2IntMap.Entry<Identifier> e : SPELL_COOLDOWNS.object2IntEntrySet()) {
                        if (e.getIntValue() > 0) entries.add(e);
                    }

                    // Sort by largest time remaining
                    entries.sort(Comparator.comparingInt((Object2IntMap.Entry<Identifier> e) -> e.getIntValue()).reversed());

                    int shown = 0;
                    for (Object2IntMap.Entry<Identifier> e : entries) {
                        Identifier spellId = e.getKey();
                        int ticksLeft = e.getIntValue();
                        if (ticksLeft <= 0) continue;

                        String line = spellNameFromId(spellId) + ": " + formatCooldownMmSs(ticksLeft);

                        drawManaHudLine(ctx, mc, line, nextLineY);
                        nextLineY += mc.textRenderer.fontHeight + 2;

                        shown++;
                        if (shown >= 8) break;
                    }
                } else {
                    drawManaHudLine(ctx, mc, "Cooldowns: none", nextLineY);
                }
            }
        });
    }

    private static String formatCooldownMmSs(int ticksLeft) {
        // ticksLeft is in ticks (20 ticks = 1 second)
        // Round UP so it doesn’t visually skip a second early
        int totalSeconds = (ticksLeft + 19) / 20;
        if (totalSeconds < 0) totalSeconds = 0;

        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        // true mm:ss format
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }


    private static int readDiscipline(ItemStack stack) {
        NbtCompound data = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();

        if (!data.contains(NBT_ROOT, NbtElement.COMPOUND_TYPE)) return 0;

        NbtCompound root = data.getCompound(NBT_ROOT);
        int v = root.getInt(NBT_DISCIPLINE);

        if (v < 0) v = 0;
        if (v > 100) v = 100;
        return v;
    }

    private static void renderDisciplineBar(DrawContext ctx, MinecraftClient mc, int discipline) {
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        int barWidth = 81;
        int barHeight = 8;

        int x = (sw / 2) - (barWidth / 2);
        int y = sh - 58;

        ctx.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xAA000000);
        ctx.fill(x, y, x + barWidth, y + barHeight, 0xFF1A1A1A);

        int fill = (int) Math.round((discipline / 100.0) * barWidth);
        if (fill > 0) {
            ctx.fill(x, y, x + fill, y + barHeight, 0xFFFFC04D);
        }

        String text = "Discipline: " + discipline + "%";
        ctx.drawText(mc.textRenderer, text, x, y - 10, 0xFFFFFF, true);
    }

    /**
     * Renders the mana bar in the top-right corner plus optional text under it.
     * Returns the Y pixel where additional lines (like cooldown text) should start.
     */
    private static int renderManaHud(DrawContext ctx, MinecraftClient mc, int mana, int maxMana) {
        int sw = ctx.getScaledWindowWidth();

        int barWidth = 182;
        int barHeight = 5;

        int padding = 8;
        int x = sw - barWidth - padding;
        int y = padding;

        // Frame + background
        ctx.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xAA000000);
        ctx.fill(x, y, x + barWidth, y + barHeight, 0xFF1A1A1A);

        if (maxMana > 0) {
            int fill = (int) Math.floor((mana / (float) maxMana) * barWidth);
            if (fill > 0) {
                ctx.fill(x, y, x + fill, y + barHeight, 0xFF3B82F6);
            }
        }

        int nextY = y + barHeight + 4;

        BladeboundConfig.BladeboundConfigData.ManaHudNumbersMode mode = BladeboundConfig.DATA.manaHudNumbersMode;
        if (mode != BladeboundConfig.BladeboundConfigData.ManaHudNumbersMode.OFF) {
            String text;

            if (mode == BladeboundConfig.BladeboundConfigData.ManaHudNumbersMode.PERCENT) {
                int pct = (maxMana <= 0) ? 0 : Math.round((mana / (float) maxMana) * 100.0f);
                text = pct + "%";
            } else {
                text = mana + " / " + maxMana;
            }

            int textWidth = mc.textRenderer.getWidth(text);
            int textX = x + (barWidth / 2) - (textWidth / 2);

            int textY = y + barHeight + BladeboundConfig.DATA.manaHudNumbersYOffset;
            ctx.drawText(mc.textRenderer, text, textX, textY, 0xFFFFFF, true);

            nextY = textY + mc.textRenderer.fontHeight + 2;
        }

        return nextY;
    }

    private static void drawManaHudLine(DrawContext ctx, MinecraftClient mc, String line, int textY) {
        int sw = ctx.getScaledWindowWidth();

        int barWidth = 182;
        int padding = 8;

        int x = sw - barWidth - padding;
        ctx.drawText(mc.textRenderer, line, x, textY, 0xFFFFFF, true);
    }
}
