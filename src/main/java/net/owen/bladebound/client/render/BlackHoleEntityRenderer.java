package net.owen.bladebound.client.render;

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
import net.owen.bladebound.client.render.state.BlackHoleEntityRenderState;
import net.owen.bladebound.entity.custom.BlackHoleEntity;
import org.joml.Matrix4f;

public class BlackHoleEntityRenderer extends EntityRenderer<BlackHoleEntity, BlackHoleEntityRenderState> {

    private static final Identifier DISK_TEX = Identifier.of(Bladebound.MOD_ID, "textures/entity/black_hole_disk.png");
    private static final Identifier RIM_TEX = Identifier.of(Bladebound.MOD_ID, "textures/entity/black_hole_rim.png");
    private static final float VISUAL_SCALE = 2.0f;
    private static final float DISK_RADIUS = 3.0f;
    private static final float RIM_RADIUS = 3.25f;
    private static final int DISK_SLICES = 4;

    public BlackHoleEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    @Override
    public BlackHoleEntityRenderState createRenderState() {
        return new BlackHoleEntityRenderState();
    }

    @Override
    public boolean shouldRender(BlackHoleEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(
            BlackHoleEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState
    ) {
        matrices.push();
        matrices.scale(VISUAL_SCALE, VISUAL_SCALE, VISUAL_SCALE);

        final int fullBright = LightmapTextureManager.MAX_LIGHT_COORDINATE;

        for (int i = 0; i < DISK_SLICES; i++) {
            matrices.push();
            float rot = (180.0f / DISK_SLICES) * i;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot));

            queue.submitCustom(
                    matrices,
                    RenderLayers.entityNoOutline(DISK_TEX),
                    (entry, vc) -> drawQuad(vc, entry.getPositionMatrix(), DISK_RADIUS, 1.0f, 1.0f, 1.0f, 1.0f, fullBright)
            );

            matrices.pop();
        }

        matrices.push();

        if (cameraState != null && cameraState.orientation != null) {
            matrices.multiply(cameraState.orientation);
        }

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        matrices.translate(0.0, 0.0, -0.02);

        queue.submitCustom(
                matrices,
                RenderLayers.entityTranslucent(RIM_TEX),
                (entry, vc) -> drawQuad(vc, entry.getPositionMatrix(), RIM_RADIUS, 1.0f, 1.0f, 1.0f, 0.95f, fullBright)
        );

        matrices.pop();
        matrices.pop();

        super.render(state, matrices, queue, cameraState);
    }

    private static void drawQuad(VertexConsumer vc, Matrix4f mat, float radius, float r, float g, float b, float a, int light) {
        vc.vertex(mat, -radius, radius, 0.0f)
                .color(r, g, b, a)
                .texture(0.0f, 0.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0.0f, 0.0f, 1.0f);

        vc.vertex(mat, radius, radius, 0.0f)
                .color(r, g, b, a)
                .texture(1.0f, 0.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0.0f, 0.0f, 1.0f);

        vc.vertex(mat, radius, -radius, 0.0f)
                .color(r, g, b, a)
                .texture(1.0f, 1.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0.0f, 0.0f, 1.0f);

        vc.vertex(mat, -radius, -radius, 0.0f)
                .color(r, g, b, a)
                .texture(0.0f, 1.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0.0f, 0.0f, 1.0f);
    }
}