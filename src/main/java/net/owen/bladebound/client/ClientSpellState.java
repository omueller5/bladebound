package net.owen.bladebound.client;

import net.owen.bladebound.magic.StaffSpell;

public final class ClientSpellState {

    // Match server default: starter spells learned (0,1,2)
    private static final int STARTER_MASK = 0b0111;

    private static int learnedMask = STARTER_MASK;
    private static int selected = 0;

    private ClientSpellState() {}

    public static void set(int mask, int sel) {
        // Always ensure starter spells are available on the client UI
        learnedMask = (mask | STARTER_MASK);

        int max = StaffSpell.values().length - 1;
        if (sel < 0) sel = 0;
        if (sel > max) sel = 0;
        selected = sel;

        // If selected isn't learned, fall back to 0
        if (!isLearned(selected)) {
            selected = 0;
        }
    }

    public static boolean isLearned(int idx) {
        if (idx < 0 || idx >= 32) return false;
        return (learnedMask & (1 << idx)) != 0;
    }

    public static int getSelected() {
        return selected;
    }
}
