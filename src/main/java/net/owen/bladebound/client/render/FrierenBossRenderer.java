package net.owen.bladebound.client.render;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Identifier;
import net.owen.bladebound.Bladebound;
import net.owen.bladebound.entity.custom.FrierenBossEntity;

public class FrierenBossRenderer extends BipedEntityRenderer<FrierenBossEntity, BipedEntityRenderState, BipedEntityModel<BipedEntityRenderState>> {

    private static final Identifier TEXTURE = Identifier.of(Bladebound.MOD_ID, "textures/entity/player/frieren_boss.png");

    public FrierenBossRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public BipedEntityRenderState createRenderState() {
        return new BipedEntityRenderState();
    }

    @Override
    public void updateRenderState(FrierenBossEntity entity, BipedEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
    }

    @Override
    public Identifier getTexture(BipedEntityRenderState state) {
        return TEXTURE;
    }
}
