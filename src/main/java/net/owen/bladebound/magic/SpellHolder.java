package net.owen.bladebound.magic;

public interface SpellHolder {
    int bladebound$getLearnedMask();
    void bladebound$setLearnedMask(int mask);

    int bladebound$getSelectedSpell();
    void bladebound$setSelectedSpell(int index);

    default int bladebound$getSpellCount() {
        return StaffSpell.values().length;
    }

    default boolean bladebound$hasLearnedSpell(int idx) {
        if (idx < 0 || idx >= bladebound$getSpellCount()) return false;
        return (bladebound$getLearnedMask() & (1 << idx)) != 0;
    }

    default void bladebound$learnSpell(int idx) {
        if (idx < 0 || idx >= bladebound$getSpellCount()) return;
        bladebound$setLearnedMask(bladebound$getLearnedMask() | (1 << idx));
    }
}
