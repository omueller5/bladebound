package net.owen.bladebound.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;
import net.owen.bladebound.Bladebound;
import net.owen.bladebound.entity.custom.FrierenBossEntity;

public class FrierenBossRenderer extends LivingEntityRenderer<FrierenBossEntity, PlayerEntityModel<FrierenBossEntity>> {

    private static final Identifier TEXTURE =
            Identifier.of(Bladebound.MOD_ID, "textures/entity/player/frieren_boss.png");


    public FrierenBossRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5f);

        // Render held items (the staff)
        this.addFeature(new HeldItemFeatureRenderer<>(this, ctx.getHeldItemRenderer()));
    }



    @Override
    public Identifier getTexture(FrierenBossEntity entity) {
        return TEXTURE;
    }
}
