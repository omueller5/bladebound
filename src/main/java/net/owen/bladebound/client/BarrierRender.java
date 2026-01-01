package net.owen.bladebound.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.owen.bladebound.item.ModItems;
import org.joml.Matrix4f;

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

    // Tuning: “wall in front of you”
    private static final double DIST_FORWARD = 1.25; // a bit farther reduces “zoomed” feel
    private static final float HALF_W = 0.95f;
    private static final float HALF_H = 1.20f;

    // Glyph particles (reduced a lot)
    private static final int SPAWN_EVERY_TICKS = 10; // was 2, now much slower
    private static final int PARTICLES_PER_SPAWN = 1; // was 2 per tick, now 1 per spawn
    private static final double PARTICLE_PLANE_JITTER = 0.015;

    private static final Random RNG = new Random();

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(BarrierRender::render);
    }

    private static void render(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        MatrixStack ms = ctx.matrixStack();
        if (ms == null) return;

        // Only show while holding right click (using item)
        if (!player.isUsingItem()) return;

        // Must be holding a staff (tag). Keep Frieren fallback.
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();

        boolean holdingStaff =
                main.isIn(STAVES_TAG) || off.isIn(STAVES_TAG) ||
                        main.isOf(ModItems.FRIEREN_STAFF) || main.isOf(ModItems.FRIEREN_STAFF_CREATIVE) ||
                        off.isOf(ModItems.FRIEREN_STAFF) || off.isOf(ModItems.FRIEREN_STAFF_CREATIVE);

        if (!holdingStaff) return;

        Vec3d camPos = ctx.camera().getPos();
        Vec3d look = player.getRotationVec(1.0f).normalize();

        // Barrier position in front of camera
        Vec3d shieldCenter = camPos.add(look.multiply(DIST_FORWARD));

        // Spawn glyph particles (client-side)
        spawnGlyphParticles(mc, shieldCenter, look, player.age);

        ms.push();

        ms.translate(shieldCenter.x - camPos.x, shieldCenter.y - camPos.y, shieldCenter.z - camPos.z);

        // Face the camera
        float yaw = ctx.camera().getYaw();
        float pitch = ctx.camera().getPitch();
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
        ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));

        // Render state
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, TEXTURE);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        Tessellator tess = Tessellator.getInstance();
        Matrix4f mat = ms.peek().getPositionMatrix();

        // IMPORTANT: no tiling (your PNG already is the full pattern)
        float u0 = 0.0f, v0 = 0.0f, u1 = 1.0f, v1 = 1.0f;

        // Base pass (light blue tint)
        {
            float r = 0.60f, g = 0.90f, b = 1.00f, a = 0.70f;

            BufferBuilder bb = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            bb.vertex(mat, -HALF_W,  HALF_H, 0.0f).texture(u0, v0).color(r, g, b, a);
            bb.vertex(mat,  HALF_W,  HALF_H, 0.0f).texture(u1, v0).color(r, g, b, a);
            bb.vertex(mat,  HALF_W, -HALF_H, 0.0f).texture(u1, v1).color(r, g, b, a);
            bb.vertex(mat, -HALF_W, -HALF_H, 0.0f).texture(u0, v1).color(r, g, b, a);

            BufferRenderer.drawWithGlobalProgram(bb.end());
        }

        // Glow pass (subtle)
        {
            float glowScale = 1.015f;
            float r = 0.70f, g = 0.95f, b = 1.00f, a = 0.25f;

            BufferBuilder glow = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            glow.vertex(mat, -HALF_W * glowScale,  HALF_H * glowScale, 0.0005f).texture(u0, v0).color(r, g, b, a);
            glow.vertex(mat,  HALF_W * glowScale,  HALF_H * glowScale, 0.0005f).texture(u1, v0).color(r, g, b, a);
            glow.vertex(mat,  HALF_W * glowScale, -HALF_H * glowScale, 0.0005f).texture(u1, v1).color(r, g, b, a);
            glow.vertex(mat, -HALF_W * glowScale, -HALF_H * glowScale, 0.0005f).texture(u0, v1).color(r, g, b, a);

            BufferRenderer.drawWithGlobalProgram(glow.end());
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        ms.pop();
    }

    private static void spawnGlyphParticles(MinecraftClient mc, Vec3d shieldCenter, Vec3d look, int age) {
        if (mc.world == null) return;

        // throttle hard
        if ((age % SPAWN_EVERY_TICKS) != 0) return;

        // camera-facing basis in world space
        Vec3d upWorld = new Vec3d(0.0, 1.0, 0.0);
        Vec3d right = look.crossProduct(upWorld);
        if (right.lengthSquared() < 1.0e-6) {
            upWorld = new Vec3d(1.0, 0.0, 0.0);
            right = look.crossProduct(upWorld);
        }
        right = right.normalize();
        Vec3d up = right.crossProduct(look).normalize();

        for (int i = 0; i < PARTICLES_PER_SPAWN; i++) {
            // random point on the barrier plane
            double u = (RNG.nextDouble() * 2.0 - 1.0) * HALF_W;
            double v = (RNG.nextDouble() * 2.0 - 1.0) * HALF_H;
            double depth = (RNG.nextDouble() * 2.0 - 1.0) * PARTICLE_PLANE_JITTER;

            Vec3d pos = shieldCenter
                    .add(right.multiply(u))
                    .add(up.multiply(v))
                    .add(look.multiply(depth));

            // gentle drift upward
            double vx = (RNG.nextDouble() * 2.0 - 1.0) * 0.006;
            double vy = 0.012 + RNG.nextDouble() * 0.010;
            double vz = (RNG.nextDouble() * 2.0 - 1.0) * 0.006;

            mc.world.addParticle(ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, vx, vy, vz);
        }
    }
}
