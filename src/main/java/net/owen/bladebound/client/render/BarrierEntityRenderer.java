package net.owen.bladebound.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.owen.bladebound.Bladebound;
import net.owen.bladebound.client.render.state.BarrierEntityRenderState;
import net.owen.bladebound.entity.BarrierEntity;
import net.owen.bladebound.mana.ManaHolder;
import org.joml.Matrix4f;

public class BarrierEntityRenderer extends EntityRenderer<BarrierEntity, BarrierEntityRenderState> {

    private static final Identifier TEX = Identifier.of(Bladebound.MOD_ID, "textures/entity/defensive_magic.png");
    private static final float HALF_W = 0.85f;
    private static final float HALF_H = 1.10f;

    public BarrierEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    @Override
    public BarrierEntityRenderState createRenderState() {
        return new BarrierEntityRenderState();
    }

    @Override
    public boolean shouldRender(BarrierEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void updateRenderState(BarrierEntity entity, BarrierEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.ownerUuid = entity.getOwnerUuid();
    }

    @Override
    public void render(
            BarrierEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState
    ) {
        matrices.push();

        // Billboard toward the camera in 1.21.11 render flow
        if (cameraState != null && cameraState.orientation != null) {
            matrices.multiply(cameraState.orientation);
        }
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));

        float manaFactor = 1.0f;
        if (state.ownerUuid != null) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.player.getUuid().equals(state.ownerUuid) && mc.player instanceof ManaHolder mh) {
                int mana = mh.bladebound$getMana();
                int max = mh.bladebound$getMaxMana();
                if (max > 0) {
                    float pct = mana / (float) max;
                    if (pct <= 0.30f) {
                        manaFactor = Math.max(0.04f, pct / 0.30f);
                    }
                }
            }
        }

        final float r = 0.55f;
        final float g = 0.85f;
        final float b = 1.00f;
        final float alpha = 0.45f * manaFactor;
        final int fullBright = LightmapTextureManager.MAX_LIGHT_COORDINATE;

        queue.submitCustom(
                matrices,
                RenderLayers.entityTranslucentEmissive(TEX),
                (entry, vc) -> {
                    Matrix4f mat = entry.getPositionMatrix();

                    vc.vertex(mat, -HALF_W,  HALF_H, 0.0f)
                            .color(r, g, b, alpha)
                            .texture(0.0f, 0.0f)
                            .overlay(OverlayTexture.DEFAULT_UV)
                            .light(fullBright)
                            .normal(0.0f, 0.0f, 1.0f);

                    vc.vertex(mat,  HALF_W,  HALF_H, 0.0f)
                            .color(r, g, b, alpha)
                            .texture(1.0f, 0.0f)
                            .overlay(OverlayTexture.DEFAULT_UV)
                            .light(fullBright)
                            .normal(0.0f, 0.0f, 1.0f);

                    vc.vertex(mat,  HALF_W, -HALF_H, 0.0f)
                            .color(r, g, b, alpha)
                            .texture(1.0f, 1.0f)
                            .overlay(OverlayTexture.DEFAULT_UV)
                            .light(fullBright)
                            .normal(0.0f, 0.0f, 1.0f);

                    vc.vertex(mat, -HALF_W, -HALF_H, 0.0f)
                            .color(r, g, b, alpha)
                            .texture(0.0f, 1.0f)
                            .overlay(OverlayTexture.DEFAULT_UV)
                            .light(fullBright)
                            .normal(0.0f, 0.0f, 1.0f);
                }
        );

        matrices.pop();
        super.render(state, matrices, queue, cameraState);
    }
}