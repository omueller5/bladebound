package net.owen.bladebound.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentStateManager;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.world.BladeboundWorldState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonAncientDropMixin {

    @Unique private static final String STATE_ID = "bladebound_world_state";

    @Inject(method = "updatePostDeath", at = @At("TAIL"))
    private void bladebound$dropAncientOnce(CallbackInfo ci) {
        EnderDragonEntity dragon = (EnderDragonEntity) (Object) this;
        if (!(dragon.getWorld() instanceof ServerWorld sw)) return;

        PersistentStateManager psm = sw.getPersistentStateManager();
        BladeboundWorldState state = psm.getOrCreate(BladeboundWorldState.TYPE, STATE_ID);

        if (state.hasDragonDroppedAncient()) return;

        state.setDragonDroppedAncient(true);

        BlockPos pos = dragon.getBlockPos();
        ItemStack drop = new ItemStack(ModItems.WORLD_REWRITE_SPELL);

        ItemScatterer.spawn(sw, pos.getX(), pos.getY(), pos.getZ(), drop);
    }
}
