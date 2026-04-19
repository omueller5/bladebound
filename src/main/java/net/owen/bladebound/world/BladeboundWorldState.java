package net.owen.bladebound.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class BladeboundWorldState extends PersistentState {

    private static final String KEY_DRAGON_DROPPED = "dragonAncientDropped";

    public static final Codec<BladeboundWorldState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf(KEY_DRAGON_DROPPED, false).forGetter(s -> s.dragonAncientDropped)
    ).apply(instance, BladeboundWorldState::new));

    public static final PersistentStateType<BladeboundWorldState> TYPE =
            new PersistentStateType<>("bladebound_world_state", BladeboundWorldState::new, CODEC, null);

    private boolean dragonAncientDropped;

    public BladeboundWorldState() {
        this(false);
    }

    private BladeboundWorldState(boolean dragonAncientDropped) {
        this.dragonAncientDropped = dragonAncientDropped;
    }

    public boolean hasDragonDroppedAncient() {
        return dragonAncientDropped;
    }

    public void setDragonDroppedAncient(boolean value) {
        this.dragonAncientDropped = value;
        this.markDirty();
    }
}