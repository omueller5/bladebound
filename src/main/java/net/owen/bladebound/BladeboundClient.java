package net.owen.bladebound;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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
import net.owen.bladebound.client.screen.SpellScreen;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.mana.ManaHolder;
import net.owen.bladebound.network.ClientPackets;
import net.owen.bladebound.network.Payloads;
import org.lwjgl.glfw.GLFW;

public class BladeboundClient implements ClientModInitializer {

    private static final String NBT_ROOT = "bladebound";
    private static final String NBT_DISCIPLINE = "discipline";

    // Spell UI keybind
    private static KeyBinding OPEN_SPELLS;

    // -----------------------
    // Staff cooldown countdown (client-side)
    // -----------------------
    private static int staffCooldownEndAge = 0;

    /**
     * Called by the client packet receiver when the server starts a staff cooldown.
     * cooldownTicks is in TICKS (20 ticks = 1 second).
     */
    public static void clientStartStaffCooldown(int cooldownTicks) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        staffCooldownEndAge = mc.player.age + Math.max(0, cooldownTicks);
    }

    private static int getStaffCooldownTicksLeft(MinecraftClient mc) {
        if (mc.player == null) return 0;
        return Math.max(0, staffCooldownEndAge - mc.player.age);
    }

    @Override
    public void onInitializeClient() {
        Payloads.register();
        ClientPackets.register();

        OPEN_SPELLS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bladebound.open_spells",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.bladebound"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_SPELLS.wasPressed()) {
                if (client.player == null) return;
                if (client.currentScreen != null) return;

                client.setScreen(new SpellScreen());
            }
        });

        HudRenderCallback.EVENT.register((DrawContext ctx, RenderTickCounter tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            if (!BladeboundConfig.DATA.hudEnabled) return;

            // Discipline HUD (Wado)
            ItemStack stack = mc.player.getMainHandStack();
            if (!stack.isEmpty() && stack.isOf(ModItems.WADOICHIMONJI)) {
                int discipline = readDiscipline(stack);
                renderDisciplineBar(ctx, mc, discipline);
            }

            // Mana HUD (Frieren Staff) - also show for Creative Staff
            ItemStack main = mc.player.getMainHandStack();
            ItemStack off = mc.player.getOffHandStack();

            boolean holdingStaff =
                    main.isOf(ModItems.FRIEREN_STAFF) || off.isOf(ModItems.FRIEREN_STAFF)
                            // CHANGE THIS NAME if yours differs:
                            || main.isOf(ModItems.FRIEREN_STAFF_CREATIVE) || off.isOf(ModItems.FRIEREN_STAFF_CREATIVE);

            if (holdingStaff) {
                ManaHolder mana = (ManaHolder) mc.player;

                int currentMana = mana.bladebound$getMana();
                int maxMana = mana.bladebound$getMaxMana();

                // Draw bar + optional numbers; returns the next Y position to start lines (cooldown)
                int nextLineY = renderManaHud(ctx, mc, currentMana, maxMana);

                int ticksLeft = getStaffCooldownTicksLeft(mc);

                String cdLine;
                if (ticksLeft <= 0) {
                    cdLine = "Cooldown: READY";
                } else {
                    int secondsLeft = (ticksLeft + 19) / 20;
                    cdLine = "Cooldown: " + secondsLeft + "s";
                }

                drawManaHudLine(ctx, mc, cdLine, nextLineY);
            }
        });
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

        int nextY = y + barHeight + 4; // base spacing for text lines under the bar

        // Optional numeric mana under the bar (mode-based)
        BladeboundConfig.BladeboundConfigData.ManaHudNumbersMode mode = BladeboundConfig.DATA.manaHudNumbersMode;
        if (mode != BladeboundConfig.BladeboundConfigData.ManaHudNumbersMode.OFF) {
            String text;

            if (mode == BladeboundConfig.BladeboundConfigData.ManaHudNumbersMode.PERCENT) {
                int pct = (maxMana <= 0) ? 0 : Math.round((mana / (float) maxMana) * 100.0f);
                text = pct + "%";
            } else { // CURRENT_MAX
                text = mana + " / " + maxMana;
            }

            int textWidth = mc.textRenderer.getWidth(text);
            int textX = x + (barWidth / 2) - (textWidth / 2);

            int textY = y + barHeight + BladeboundConfig.DATA.manaHudNumbersYOffset;
            ctx.drawText(mc.textRenderer, text, textX, textY, 0xFFFFFF, true);

            // After drawing numbers, move the next line below them
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
