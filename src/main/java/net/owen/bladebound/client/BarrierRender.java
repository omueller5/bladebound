package net.owen.bladebound.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.owen.bladebound.item.ModItems;

import java.util.Random;

public final class BarrierRender {
    private BarrierRender() {}

    // data/bladebound/tags/items/staves.json
    private static final TagKey<net.minecraft.item.Item> STAVES_TAG = TagKey.of(
            RegistryKeys.ITEM,
            Identifier.of("bladebound", "staves")
    );

    // Your barrier texture (PNG)
    private static final Identifier TEXTURE = Identifier.of("bladebound", "textures/misc/defensive_magic.png");

    // HUD sizing
    private static final int TEXTURE_SIZE = 256;
    private static final float HUD_SCALE = 0.90f;

    // Particle tuning
    private static final int SPAWN_EVERY_TICKS = 10;
    private static final int PARTICLES_PER_SPAWN = 1;
    private static final double DIST_FORWARD = 1.25;
    private static final float HALF_W = 0.95f;
    private static final float HALF_H = 1.20f;
    private static final double PARTICLE_PLANE_JITTER = 0.015;

    private static final Random RNG = new Random();

    public static void register() {
        HudRenderCallback.EVENT.register(BarrierRender::renderHud);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerEntity player = mc.player;
            if (player == null || mc.world == null) return;
            if (!shouldRenderBarrier(player)) return;

            Vec3d eyePos = player.getCameraPosVec(1.0f);
            Vec3d look = player.getRotationVec(1.0f).normalize();
            Vec3d shieldCenter = eyePos.add(look.multiply(DIST_FORWARD));

            spawnGlyphParticles(mc, shieldCenter, look, player.age);
        });
    }

    private static void renderHud(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return;
        if (mc.options.hudHidden) return;
        if (!shouldRenderBarrier(player)) return;

        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        int width = Math.round(TEXTURE_SIZE * HUD_SCALE);
        int height = Math.round(TEXTURE_SIZE * HUD_SCALE);

        int x = (sw - width) / 2;
        int y = (sh - height) / 2 - 6;

        // Base barrier
        ctx.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x, y,
                0.0f, 0.0f,
                width, height,
                TEXTURE_SIZE, TEXTURE_SIZE,
                0xB0FFFFFF
        );

        // Slight glow pass
        int glowPad = 6;
        ctx.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x - glowPad, y - glowPad,
                0.0f, 0.0f,
                width + (glowPad * 2), height + (glowPad * 2),
                TEXTURE_SIZE, TEXTURE_SIZE,
                0x40BFEFFF
        );
    }

    private static boolean shouldRenderBarrier(ClientPlayerEntity player) {
        if (!player.isUsingItem()) return false;

        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();

        return main.isIn(STAVES_TAG) || off.isIn(STAVES_TAG) ||
                main.isOf(ModItems.FRIEREN_STAFF) || main.isOf(ModItems.FRIEREN_STAFF_CREATIVE) ||
                off.isOf(ModItems.FRIEREN_STAFF) || off.isOf(ModItems.FRIEREN_STAFF_CREATIVE);
    }

    private static void spawnGlyphParticles(MinecraftClient mc, Vec3d shieldCenter, Vec3d look, int age) {
        if (mc.world == null) return;
        if ((age % SPAWN_EVERY_TICKS) != 0) return;

        Vec3d upWorld = new Vec3d(0.0, 1.0, 0.0);
        Vec3d right = look.crossProduct(upWorld);
        if (right.lengthSquared() < 1.0e-6) {
            upWorld = new Vec3d(1.0, 0.0, 0.0);
            right = look.crossProduct(upWorld);
        }
        right = right.normalize();
        Vec3d up = right.crossProduct(look).normalize();

        for (int i = 0; i < PARTICLES_PER_SPAWN; i++) {
            double u = (RNG.nextDouble() * 2.0 - 1.0) * HALF_W;
            double v = (RNG.nextDouble() * 2.0 - 1.0) * HALF_H;
            double depth = (RNG.nextDouble() * 2.0 - 1.0) * PARTICLE_PLANE_JITTER;

            Vec3d pos = shieldCenter
                    .add(right.multiply(u))
                    .add(up.multiply(v))
                    .add(look.multiply(depth));

            double vx = (RNG.nextDouble() * 2.0 - 1.0) * 0.006;
            double vy = 0.012 + RNG.nextDouble() * 0.010;
            double vz = (RNG.nextDouble() * 2.0 - 1.0) * 0.006;

            mc.world.addParticleClient(ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, vx, vy, vz);
        }
    }
}
