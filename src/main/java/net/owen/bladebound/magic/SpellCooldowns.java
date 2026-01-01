package net.owen.bladebound.magic;

import net.minecraft.util.Identifier;

/**
 * Central place for spell cooldown rules.
 *
 * All values returned are in SECONDS.
 * The staff converts them to ticks.
 */
public final class SpellCooldowns {

    private SpellCooldowns() {}

    /**
     * Base cooldown for a spell.
     * Uses spell Identifier so spells do NOT need items.
     */
    public static int getBaseCooldownSeconds(Identifier spellId) {

        // Starter spells
        if (spellId.equals(StaffSpell.FIREBOLT.id)) return 10;
        if (spellId.equals(StaffSpell.FROST_RAY.id)) return 15;
        if (spellId.equals(StaffSpell.HEAL.id)) return 25;
        if (spellId.equals(StaffSpell.STONE_DART.id)) return 10;

        // Rare spells
        if (spellId.equals(StaffSpell.LIGHTNING_STRIKE.id)) return 35;
        if (spellId.equals(StaffSpell.MANA_BARRIER.id)) return 0;

        // Legendary spells
        if (spellId.equals(StaffSpell.ZOLTRAAK.id)) return 65;
        if (spellId.equals(StaffSpell.PERFECT_HEAL.id)) return 0;

        // Ancient spells
        if (spellId.equals(StaffSpell.WORLD_REWRITE.id)) return 900;

        // Safe fallback
        return 20;
    }
}
