package net.owen.bladebound.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.world.BladeboundWorldState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonAncientDropMixin {

    @Inject(method = "updatePostDeath", at = @At("TAIL"))
    private void bladebound$dropAncientOnce(CallbackInfo ci) {
        EnderDragonEntity dragon = (EnderDragonEntity) (Object) this;

        if (dragon.getEntityWorld().isClient()) return;
        ServerWorld sw = (ServerWorld) dragon.getEntityWorld();

        BladeboundWorldState state = sw.getPersistentStateManager().getOrCreate(BladeboundWorldState.TYPE);

        if (state.hasDragonDroppedAncient()) return;

        state.setDragonDroppedAncient(true);

        BlockPos pos = dragon.getBlockPos();
        ItemScatterer.spawn(sw, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ModItems.WORLD_REWRITE_SPELL));
    }
}
