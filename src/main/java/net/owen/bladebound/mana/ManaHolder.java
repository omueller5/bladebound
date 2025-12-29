package net.owen.bladebound.mana;

public interface ManaHolder {

    int bladebound$getMana();
    void bladebound$setMana(int mana);

    int bladebound$getMaxMana();
    void bladebound$setMaxMana(int maxMana);

    // ----------------------------
    // Infinite mana toggle
    // ----------------------------
    boolean bladebound$hasInfiniteMana();
    void bladebound$setInfiniteMana(boolean value);

    // ----------------------------
    // Mana consumption logic
    // ----------------------------
    default boolean bladebound$tryConsumeMana(int cost) {

        // ✅ infinite mana short-circuit
        if (bladebound$hasInfiniteMana()) {
            // keep HUD full so it looks sane
            if (bladebound$getMana() < bladebound$getMaxMana()) {
                bladebound$setMana(bladebound$getMaxMana());
            }
            return true;
        }

        int mana = bladebound$getMana();
        if (mana < cost) return false;

        bladebound$setMana(mana - cost);
        return true;
    }
}
