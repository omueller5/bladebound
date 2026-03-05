package net.owen.bladebound.mana;

public interface ManaHolder {

    int bladebound$getMana();
    void bladebound$setMana(int mana);

    // EFFECTIVE max mana (what the game/HUD should use)
    int bladebound$getMaxMana();

    // BASE max mana (stored/synced/saved)
    int bladebound$getBaseMaxMana();

    void bladebound$setMaxMana(int baseMaxMana);

    // ----------------------------
    // Infinite mana toggle
    // ----------------------------
    boolean bladebound$hasInfiniteMana();
    void bladebound$setInfiniteMana(boolean value);

    // ----------------------------
    // Mana consumption logic
    // ----------------------------
    default boolean bladebound$tryConsumeMana(int cost) {

        if (bladebound$hasInfiniteMana()) {
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
