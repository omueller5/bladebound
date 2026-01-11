package net.owen.bladebound.client.render;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.owen.bladebound.entity.custom.BlackHoleEntity;

public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity> {

    // Any valid texture id works since we render nothing.
    private static final Identifier DUMMY = Identifier.of("minecraft", "textures/particle/particles.png");

    public BlackHoleRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(BlackHoleEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        // Intentionally render nothing.
        // Your debug particles are spawned server-side already, so you’ll still see the radius ring.
    }

    @Override
    public Identifier getTexture(BlackHoleEntity entity) {
        return DUMMY;
    }

    @Override
    public boolean shouldRender(BlackHoleEntity entity, Frustum frustum, double x, double y, double z) {
        // Always "render" (no-op) so it never gets culled weirdly while testing.
        return true;
    }
}
