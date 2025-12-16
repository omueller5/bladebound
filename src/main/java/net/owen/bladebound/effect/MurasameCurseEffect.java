package net.owen.bladebound.effect;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.owen.bladebound.item.ModItems;

public class MurasameCurseEffect extends StatusEffect {

    public MurasameCurseEffect() {
        super(StatusEffectCategory.HARMFUL, 0x5B1B7A); // purple-ish
    }

    /**
     * Called when the effect ticks.
     * We only need to do something when the timer is basically finished.
     */
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration <= 1;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        // If a player has the gauntlets equipped, they're immune (cleanse + do nothing).
        if (entity instanceof PlayerEntity player && hasMurasameGauntletsEquipped(player)) {
            return true;
        }

        // Boss immunity (you can expand this list however you want)
        if (isBossImmune(entity)) {
            return true;
        }

        // Kill when timer ends (server-side)
        if (!entity.getWorld().isClient) {
            entity.kill();
        }
        return true;
    }

    private static boolean hasMurasameGauntletsEquipped(PlayerEntity player) {
        return TrinketsApi.getTrinketComponent(player)
                .map(comp -> comp.isEquipped(ModItems.MURASAME_GAUNTLETS))
                .orElse(false);
    }

    private static boolean isBossImmune(LivingEntity e) {
        // Keep it simple + explicit:
        return e.getType().toString().contains("wither")
                || e.getType().toString().contains("ender_dragon")
                || e.getType().toString().contains("warden");
        // If you want exact checks instead of string contains, tell me your target MC version/mappings
        // and I’ll swap this to EntityType.WITHER / ENDER_DRAGON / WARDEN.
    }

    // --- registration helper ---
    public static final Identifier ID = Identifier.of("bladebound", "murasame_curse");

    public static MurasameCurseEffect register() {
        return Registry.register(Registries.STATUS_EFFECT, ID, new MurasameCurseEffect());
    }
}
