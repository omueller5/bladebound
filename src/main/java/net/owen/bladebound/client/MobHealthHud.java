package net.owen.bladebound.client;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.owen.bladebound.BladeboundConfig;

public final class MobHealthHud {

    private MobHealthHud() {}

    // Tracking last target to keep showing without aiming
    private static int lastEntityId = -1;
    private static int holdTicksLeft = 0;

    // Smooth bar tracking
    private static float shownRatio = 1.0f;

    // Damage tracking (client-side estimate): entityId -> last seen health
    private static final Int2FloatOpenHashMap LAST_HP = new Int2FloatOpenHashMap();

    // Latest damage popup
    private static float lastDamageAmount = 0.0f;
    private static int damagePopupTicksLeft = 0;

    public static void register() {
        HudRenderCallback.EVENT.register(MobHealthHud::render);
    }

    /**
     * Called by a client mixin when the player attacks an entity.
     * This enables true "last hit tracking" even if you're not aiming afterward.
     */
    public static void notifyPlayerAttacked(int entityId) {
        if (entityId <= 0) return;

        lastEntityId = entityId;
        holdTicksLeft = Math.max(0, BladeboundConfig.DATA.mobHealthHoldTicks);
    }

    private static void render(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (mc.options.hudHidden) return;

        var mode = BladeboundConfig.DATA.mobHealthHudMode;
        if (mode == BladeboundConfig.BladeboundConfigData.MobHealthHudMode.OFF) return;

        // Prefer current crosshair target (if enabled)
        LivingEntity target = getCrosshairTarget(mc);
        if (target != null && target.isAlive()) {
            lastEntityId = target.getId();
            holdTicksLeft = Math.max(0, BladeboundConfig.DATA.mobHealthHoldTicks);

            drawForEntity(ctx, mc, target, mode);
            return;
        }

        // Otherwise keep showing last target for holdTicks (from aim OR last hit)
        if (holdTicksLeft > 0 && lastEntityId != -1) {
            holdTicksLeft--;

            LivingEntity last = findLivingById(mc, lastEntityId);
            if (last != null && last.isAlive()) {
                drawForEntity(ctx, mc, last, mode);
            } else {
                lastEntityId = -1;
                holdTicksLeft = 0;
                damagePopupTicksLeft = 0;
                lastDamageAmount = 0.0f;
            }
        } else {
            // No target; decay popup
            if (damagePopupTicksLeft > 0) damagePopupTicksLeft--;
        }
    }

    private static LivingEntity getCrosshairTarget(MinecraftClient mc) {
        HitResult hit = mc.crosshairTarget;
        if (!(hit instanceof EntityHitResult ehr)) return null;
        Entity e = ehr.getEntity();
        return (e instanceof LivingEntity le) ? le : null;
    }

    private static LivingEntity findLivingById(MinecraftClient mc, int id) {
        if (mc.world == null) return null;
        Entity e = mc.world.getEntityById(id);
        return (e instanceof LivingEntity le) ? le : null;
    }

    private static void drawForEntity(
            DrawContext ctx,
            MinecraftClient mc,
            LivingEntity target,
            BladeboundConfig.BladeboundConfigData.MobHealthHudMode mode
    ) {
        float hp = target.getHealth();
        float max = Math.max(1.0f, target.getMaxHealth());
        float ratio = clamp01(hp / max);

        boolean wantBar = (mode == BladeboundConfig.BladeboundConfigData.MobHealthHudMode.BAR_ONLY
                || mode == BladeboundConfig.BladeboundConfigData.MobHealthHudMode.HYBRID);

        boolean wantText = (mode == BladeboundConfig.BladeboundConfigData.MobHealthHudMode.TEXT_ONLY
                || mode == BladeboundConfig.BladeboundConfigData.MobHealthHudMode.HYBRID);

        // Smooth bar if enabled
        if (wantBar && BladeboundConfig.DATA.mobHealthSmooth) {
            if (target.getId() != lastEntityId) {
                shownRatio = ratio;
            }
            shownRatio = lerp(shownRatio, ratio, 0.25f);
        } else {
            shownRatio = ratio;
        }

        // Detect damage by watching HP drop client-side (no packets needed)
        handleDamageDetection(mc, target, hp);

        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        int barW = 150;
        int barH = 8;

        int x = (sw / 2) - (barW / 2);
        int y = sh - 75;

        boolean bossStyle = BladeboundConfig.DATA.mobHealthBossStyle && isBossLike(target);

        int nameColor = bossStyle ? 0xFFD65CFF : 0xFFFFFF; // purple-ish for bosses
        int textColor = 0xFFFFFF;

        String name = target.getDisplayName().getString();
        String hpLine = String.format("%.1f / %.1f", hp, max);

        if (wantBar) {
            drawBar(ctx, x, y, barW, barH, shownRatio, bossStyle);

            int nameW = mc.textRenderer.getWidth(name);
            int nameX = (sw / 2) - (nameW / 2);
            ctx.drawText(mc.textRenderer, name, nameX, y - mc.textRenderer.fontHeight - 2, nameColor, true);

            if (wantText) {
                int hpW = mc.textRenderer.getWidth(hpLine);
                int hpX = (sw / 2) - (hpW / 2);
                ctx.drawText(mc.textRenderer, hpLine, hpX, y + barH + 3, textColor, true);
            }

            // Damage popup (optional)
            if (BladeboundConfig.DATA.mobHealthDamagePopups && damagePopupTicksLeft > 0 && lastDamageAmount > 0.01f) {
                String dmg = "-" + formatDamage(lastDamageAmount);
                int dmgW = mc.textRenderer.getWidth(dmg);
                int dmgX = x + barW + 6;
                int dmgY = y - 2;

                // Pop color: yellow-ish (readable) unless boss styling enabled -> brighter
                int dmgColor = bossStyle ? 0xFFFFE066 : 0xFFFFD54A;
                ctx.drawText(mc.textRenderer, dmg, dmgX, dmgY, dmgColor, true);
            }

        } else if (wantText) {
            int nameW = mc.textRenderer.getWidth(name);
            int hpW = mc.textRenderer.getWidth("HP: " + hpLine);

            int nameX = (sw / 2) - (nameW / 2);
            int hpX = (sw / 2) - (hpW / 2);

            ctx.drawText(mc.textRenderer, name, nameX, y, nameColor, true);
            ctx.drawText(mc.textRenderer, "HP: " + hpLine, hpX, y + mc.textRenderer.fontHeight + 2, textColor, true);

            if (BladeboundConfig.DATA.mobHealthDamagePopups && damagePopupTicksLeft > 0 && lastDamageAmount > 0.01f) {
                String dmg = "-" + formatDamage(lastDamageAmount);
                int dmgW = mc.textRenderer.getWidth(dmg);
                int dmgX = (sw / 2) - (dmgW / 2);
                int dmgY = y + (mc.textRenderer.fontHeight * 2) + 6;
                ctx.drawText(mc.textRenderer, dmg, dmgX, dmgY, 0xFFFFD54A, true);
            }
        }
    }

    private static void handleDamageDetection(MinecraftClient mc, LivingEntity target, float currentHp) {
        int id = target.getId();
        if (id <= 0) return;

        float prev = LAST_HP.getOrDefault(id, currentHp);
        LAST_HP.put(id, currentHp);

        // Tick down popup while we’re focused on something
        if (damagePopupTicksLeft > 0) damagePopupTicksLeft--;

        // Only treat a meaningful decrease as damage
        float delta = prev - currentHp;
        if (delta > 0.01f) {
            lastDamageAmount = delta;
            damagePopupTicksLeft = Math.max(0, BladeboundConfig.DATA.mobHealthDamagePopupTicks);

            if (BladeboundConfig.DATA.mobHealthHitSound && mc.player != null) {
                mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.35f, 1.55f);
            }
        }
    }

    private static boolean isBossLike(LivingEntity e) {
        return (e instanceof WardenEntity)
                || (e instanceof WitherEntity)
                || (e instanceof EnderDragonEntity)
                || (e instanceof ElderGuardianEntity);
    }

    private static void drawBar(DrawContext ctx, int x, int y, int w, int h, float ratio, boolean bossStyle) {
        // frame + background
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, bossStyle ? 0xAA2A0033 : 0xAA000000);
        ctx.fill(x, y, x + w, y + h, 0xFF1A1A1A);

        int fill = (int) Math.floor(w * clamp01(ratio));
        if (fill > 0) {
            // base gradient: green -> yellow -> red
            int r, g;
            if (ratio > 0.5f) {
                r = (int) (255 * (1.0f - (ratio - 0.5f) / 0.5f));
                g = 255;
            } else {
                r = 255;
                g = (int) (255 * (ratio / 0.5f));
            }

            // Boss style: bias toward magenta tint
            int b = bossStyle ? 120 : 40;

            int color = (0xFF << 24) | (r << 16) | (g << 8) | b;
            ctx.fill(x, y, x + fill, y + h, color);
        }
    }

    private static String formatDamage(float dmg) {
        // Keep it readable: show 1 decimal if < 10, else integer
        if (dmg < 10.0f) return String.format("%.1f", dmg);
        return Integer.toString(Math.round(dmg));
    }

    private static float clamp01(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
