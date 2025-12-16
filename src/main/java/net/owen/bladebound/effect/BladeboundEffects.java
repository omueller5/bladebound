package net.owen.bladebound.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class BladeboundEffects {
    private BladeboundEffects() {}

    // Keep both: the actual effect object, AND the registry entry (what LivingEntity APIs want).
    public static StatusEffect MURASAME_CURSE_EFFECT;
    public static RegistryEntry<StatusEffect> MURASAME_CURSE;

    public static void init() {
        MURASAME_CURSE_EFFECT = Registry.register(
                Registries.STATUS_EFFECT,
                Identifier.of("bladebound", "murasame_curse"),
                new MurasameCurseEffect()
        );

        // This is the important part for 1.21.x hasStatusEffect/removeStatusEffect
        MURASAME_CURSE = Registries.STATUS_EFFECT.getEntry(MURASAME_CURSE_EFFECT);
    }
}
