package net.owen.bladebound.client.render;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.owen.bladebound.client.render.state.BlackHoleEntityRenderState;
import net.owen.bladebound.entity.custom.BlackHoleEntity;

public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity, BlackHoleEntityRenderState> {
    private static final Identifier DUMMY = Identifier.of("minecraft", "textures/particle/particles.png");

    public BlackHoleRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public BlackHoleEntityRenderState createRenderState() {
        return new BlackHoleEntityRenderState();
    }

    @Override
    public boolean shouldRender(BlackHoleEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }
}
