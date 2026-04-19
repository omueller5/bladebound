package net.owen.bladebound.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.List;

public final class BossLockState extends PersistentState {

    private static final Codec<BossLockState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.listOf().optionalFieldOf("locks", List.of()).forGetter(BossLockState::locksAsList),
            Codec.LONG.listOf().optionalFieldOf("claimed", List.of()).forGetter(BossLockState::claimedAsList)
    ).apply(instance, BossLockState::new));

    private static final PersistentStateType<BossLockState> TYPE =
            new PersistentStateType<>("bladebound_boss_locks", BossLockState::new, CODEC, null);

    private final LongOpenHashSet locks = new LongOpenHashSet();
    private final LongOpenHashSet claimed = new LongOpenHashSet();

    public static BossLockState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public BossLockState() {
    }

    private BossLockState(List<Long> locks, List<Long> claimed) {
        for (long v : locks) this.locks.add(v);
        for (long v : claimed) this.claimed.add(v);
    }

    private List<Long> locksAsList() {
        List<Long> out = new ArrayList<>(locks.size());
        for (long v : locks) out.add(v);
        return out;
    }

    private List<Long> claimedAsList() {
        List<Long> out = new ArrayList<>(claimed.size());
        for (long v : claimed) out.add(v);
        return out;
    }

    public void addLock(long pos) {
        if (locks.add(pos)) markDirty();
    }

    public boolean isClaimed(long pos) {
        return claimed.contains(pos);
    }

    public boolean claim(long pos) {
        if (claimed.add(pos)) {
            markDirty();
            return true;
        }
        return false;
    }

    public LongOpenHashSet getLocks() {
        return locks;
    }
}