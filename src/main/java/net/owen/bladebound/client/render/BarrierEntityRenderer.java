package net.owen.bladebound.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.owen.bladebound.Bladebound;
import net.owen.bladebound.entity.BarrierEntity;
import net.owen.bladebound.mana.ManaHolder;
import org.joml.Matrix4f;

public class BarrierEntityRenderer extends EntityRenderer<BarrierEntity> {

    // assets/bladebound/textures/entity/defensive_magic.png
    private static final Identifier TEX =
            Identifier.of(Bladebound.MOD_ID, "textures/entity/defensive_magic.png");

    private static final float HALF_W = 0.85f;
    private static final float HALF_H = 1.10f;

    public BarrierEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    @Override
    public Identifier getTexture(BarrierEntity entity) {
        return TEX;
    }

    @Override
    public boolean shouldRender(
            BarrierEntity entity,
            net.minecraft.client.render.Frustum frustum,
            double x, double y, double z
    ) {
        return true;
    }

    @Override
    public void render(
            BarrierEntity entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light
    ) {
        matrices.push();

        // Billboard toward camera
        matrices.multiply(this.dispatcher.getRotation());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));

        // =========================================================
        // FAST LOW-MANA FADE (starts at 30%)
        // =========================================================
        float manaFactor = 1.0f;

        if (entity.getOwnerUuid() != null) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null
                    && mc.player.getUuid().equals(entity.getOwnerUuid())
                    && mc.player instanceof ManaHolder mh) {

                int mana = mh.bladebound$getMana();
                int max = mh.bladebound$getMaxMana();

                if (max > 0) {
                    float pct = mana / (float) max;

                    // Start fading at 30% mana (fast)
                    if (pct <= 0.30f) {
                        manaFactor = Math.max(0.04f, pct / 0.30f);
                    }
                }
            }
        }

        // =========================================================
        // ORIGINAL BLUE LOOK
        // =========================================================
        float r = 0.55f;
        float g = 0.85f;
        float b = 1.00f;
        float alpha = 0.70f * manaFactor;

        // =========================================================
        // IMPORTANT:
        // Render with DEPTH WRITES OFF so entities behind still render.
        // We flush immediately so the depthMask state applies to THIS quad.
        // =========================================================
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        // Use the immediate entity consumer so we can flush right after drawing
        MinecraftClient mc = MinecraftClient.getInstance();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();

        VertexConsumer vc = immediate.getBuffer(RenderLayer.getEntityTranslucent(getTexture(entity)));
        Matrix4f mat = matrices.peek().getPositionMatrix();
        int fullBright = LightmapTextureManager.MAX_LIGHT_COORDINATE;

        vc.vertex(mat, -HALF_W,  HALF_H, 0.0f)
                .color(r, g, b, alpha)
                .texture(0.0f, 0.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(fullBright)
                .normal(0, 0, 1);

        vc.vertex(mat,  HALF_W,  HALF_H, 0.0f)
                .color(r, g, b, alpha)
                .texture(1.0f, 0.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(fullBright)
                .normal(0, 0, 1);

        vc.vertex(mat,  HALF_W, -HALF_H, 0.0f)
                .color(r, g, b, alpha)
                .texture(1.0f, 1.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(fullBright)
                .normal(0, 0, 1);

        vc.vertex(mat, -HALF_W, -HALF_H, 0.0f)
                .color(r, g, b, alpha)
                .texture(0.0f, 1.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(fullBright)
                .normal(0, 0, 1);

        // Flush just-rendered vertices now (so our depthMask(false) is honored)
        immediate.draw();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
