package net.owen.bladebound.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;
import net.minecraft.datafixer.DataFixTypes;

public class BladeboundWorldState extends PersistentState {

    private static final String KEY_DRAGON_DROPPED = "dragonAncientDropped";

    private boolean dragonAncientDropped;

    public boolean hasDragonDroppedAncient() {
        return dragonAncientDropped;
    }

    public void setDragonDroppedAncient(boolean value) {
        this.dragonAncientDropped = value;
        this.markDirty();
    }

    public static BladeboundWorldState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        BladeboundWorldState s = new BladeboundWorldState();
        s.dragonAncientDropped = nbt.getBoolean(KEY_DRAGON_DROPPED);
        return s;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putBoolean(KEY_DRAGON_DROPPED, dragonAncientDropped);
        return nbt;
    }

    // 1.21.1 way
    public static final PersistentState.Type<BladeboundWorldState> TYPE =
            new PersistentState.Type<>(BladeboundWorldState::new, BladeboundWorldState::fromNbt, DataFixTypes.LEVEL);
}
