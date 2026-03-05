package net.owen.bladebound.loot;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public final class BossLockState extends PersistentState {

    private final LongOpenHashSet locks = new LongOpenHashSet();
    private final LongOpenHashSet claimed = new LongOpenHashSet();

    public static BossLockState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new Type<>(
                        BossLockState::new,
                        BossLockState::fromNbt,
                        null
                ),
                "bladebound_boss_locks"
        );
    }

    private static BossLockState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        BossLockState s = new BossLockState();

        if (nbt.contains("locks", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("locks", NbtElement.LONG_TYPE);
            for (int i = 0; i < list.size(); i++) s.locks.add(((NbtLong) list.get(i)).longValue());
        }

        if (nbt.contains("claimed", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("claimed", NbtElement.LONG_TYPE);
            for (int i = 0; i < list.size(); i++) s.claimed.add(((NbtLong) list.get(i)).longValue());
        }

        return s;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtList locksList = new NbtList();
        for (long v : locks) locksList.add(NbtLong.of(v));
        nbt.put("locks", locksList);

        NbtList claimedList = new NbtList();
        for (long v : claimed) claimedList.add(NbtLong.of(v));
        nbt.put("claimed", claimedList);

        return nbt;
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
