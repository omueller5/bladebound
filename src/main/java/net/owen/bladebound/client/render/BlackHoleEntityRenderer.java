package net.owen.bladebound.client.render;

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
import net.owen.bladebound.entity.custom.BlackHoleEntity;
import org.joml.Matrix4f;

public class BlackHoleEntityRenderer extends EntityRenderer<BlackHoleEntity> {

    private static final Identifier DISK_TEX =
            Identifier.of(Bladebound.MOD_ID, "textures/entity/black_hole_disk.png");
    private static final Identifier RIM_TEX =
            Identifier.of(Bladebound.MOD_ID, "textures/entity/black_hole_rim.png");

    // =========================================================
    // VISUAL SCALE (EDIT HERE)
    // 1.0 = current size
    // 1.5 = 50% bigger
    // 2.0 = double size
    // =========================================================
    private static final float VISUAL_SCALE = 2.0f;

    // base sizes (before scaling)
    private static final float DISK_RADIUS = 3.0f;
    private static final float RIM_RADIUS  = 3.25f;

    private static final int DISK_SLICES = 4;

    public BlackHoleEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    @Override
    public Identifier getTexture(BlackHoleEntity entity) {
        return DISK_TEX;
    }

    @Override
    public boolean shouldRender(BlackHoleEntity entity, net.minecraft.client.render.Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(
            BlackHoleEntity entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light
    ) {
        matrices.push();

        // Scale everything (disk + rim) together
        matrices.scale(VISUAL_SCALE, VISUAL_SCALE, VISUAL_SCALE);

        VertexConsumer diskVc = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(DISK_TEX));

        for (int i = 0; i < DISK_SLICES; i++) {
            matrices.push();

            float rot = (180.0f / DISK_SLICES) * i;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot));

            Matrix4f mat = matrices.peek().getPositionMatrix();
            drawQuad(diskVc, mat, DISK_RADIUS, 1f, 1f, 1f, 1.0f, light);

            matrices.pop();
        }

        matrices.push();

        matrices.multiply(this.dispatcher.getRotation());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        matrices.translate(0.0, 0.0, -0.02);

        VertexConsumer rimVc = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(RIM_TEX));
        Matrix4f rimMat = matrices.peek().getPositionMatrix();

        int fullBright = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        drawQuad(rimVc, rimMat, RIM_RADIUS, 1f, 1f, 1f, 0.95f, fullBright);

        matrices.pop();

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private static void drawQuad(VertexConsumer vc, Matrix4f mat, float radius,
                                 float r, float g, float b, float a, int light) {
        vc.vertex(mat, -radius,  radius, 0.0f)
                .color(r, g, b, a)
                .texture(0.0f, 0.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 0, 1);

        vc.vertex(mat,  radius,  radius, 0.0f)
                .color(r, g, b, a)
                .texture(1.0f, 0.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 0, 1);

        vc.vertex(mat,  radius, -radius, 0.0f)
                .color(r, g, b, a)
                .texture(1.0f, 1.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 0, 1);

        vc.vertex(mat, -radius, -radius, 0.0f)
                .color(r, g, b, a)
                .texture(0.0f, 1.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 0, 1);
    }
}
