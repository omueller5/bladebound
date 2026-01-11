package net.owen.bladebound.config.yacl;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import net.minecraft.text.Text;
import net.owen.bladebound.config.BlackHoleConfig;

public final class BlackHoleYaclCategory {

    private BlackHoleYaclCategory() {}

    public static ConfigCategory build(BlackHoleConfig cfg) {
        return ConfigCategory.createBuilder()
                .name(Text.literal("Black Hole"))
                .tooltip(Text.literal("Ancient spell settings for Black Hole."))
                .group(groupMain(cfg))
                .group(groupCosts(cfg))
                .group(groupRadii(cfg))
                .group(groupForces(cfg))
                .group(groupDamage(cfg))
                .group(groupEventHorizon(cfg))
                .group(groupCollapse(cfg))
                .group(groupTerrain(cfg))
                .group(groupPerformance(cfg))
                .build();
    }

    private static OptionGroup groupMain(BlackHoleConfig cfg) {
        return OptionGroup.createBuilder()
                .name(Text.literal("Main"))
                .option(boolOpt("Enabled", "Enable/disable the Black Hole spell.", () -> cfg.enabled, v -> cfg.enabled = v))
                .option(intOpt("Duration (ticks)", "20 ticks = 1 second. 30s = 600.", 20, 20 * 600,
                        () -> cfg.durationTicks, v -> cfg.durationTicks = v))
                .build();
    }

    private static OptionGroup groupCosts(BlackHoleConfig cfg) {
        return OptionGroup.createBuilder()
                .name(Text.literal("Costs"))
                .option(intOpt("Mana cost", "Mana consumed on cast.", 0, 100000,
                        () -> cfg.manaCost, v -> cfg.manaCost = v))
                .build();
    }

    private static OptionGroup groupRadii(BlackHoleConfig cfg) {
        return OptionGroup.createBuilder()
                .name(Text.literal("Radii"))
                .option(floatOpt("Core radius", "Core: pull + high damage + event horizon.", 0.5f, 128f,
                        () -> cfg.coreRadius, v -> cfg.coreRadius = v))
                .option(floatOpt("Mid radius", "Mid: pull + small damage.", 0.5f, 256f,
                        () -> cfg.midRadius, v -> cfg.midRadius = v))
                .option(floatOpt("Pull radius", "Outer: pull only (terrain ripping happens outside mid).", 0.5f, 512f,
                        () -> cfg.pullRadius, v -> cfg.pullRadius = v))
                .build();
    }

    private static OptionGroup groupForces(BlackHoleConfig cfg) {
        return OptionGroup.createBuilder()
                .name(Text.literal("Forces"))
                .option(doubleOpt("Pull strength", "Base inward pull. Higher = stronger suction.", 0.0, 5.0,
                        () -> cfg.pullStrength, v -> cfg.pullStrength = v))
                .option(doubleOpt("Swirl strength", "Orbit/spiral strength. Higher = tighter spiral.", 0.0, 5.0,
                        () -> cfg.swirlStrength, v -> cfg.swirlStrength = v))
                .option(boolOpt("Swirl clockwise", "Flip spiral direction.", () -> cfg.swirlClockwise, v -> cfg.swirlClockwise = v))
                .build();
    }

    private static OptionGroup groupDamage(BlackHoleConfig cfg) {
        return OptionGroup.createBuilder()
                .name(Text.literal("Damage"))
                .option(floatOpt("Core DPS", "Damage per second inside core radius.", 0.0f, 10000f,
                        () -> cfg.coreDps, v -> cfg.coreDps = v))
                .option(floatOpt("Mid DPS", "Chip damage per second inside mid radius (outside core).", 0.0f, 10000f,
                        () -> cfg.midDps, v -> cfg.midDps = v))
                .build();
    }

    private static OptionGroup groupEventHorizon(BlackHoleConfig cfg) {
        return OptionGroup.createBuilder()
                .name(Text.literal("Event Horizon"))
                .option(boolOpt("Enabled", "Apply Slowness/Weakness to entities in the core.", () -> cfg.eventHorizonEnabled, v -> cfg.eventHorizonEnabled = v))
                .option(intOpt("Refresh (ticks)", "Re-applied while inside core. Keep short (e.g., 25).", 1, 200,
                        () -> cfg.eventHorizonDurationTicks, v -> cfg.eventHorizonDurationTicks = v))
                .option(intOpt("Slowness amplifier", "0=I, 1=II, 2=III, 3=IV...", 0, 10,
                        () -> cfg.slownessAmplifier, v -> cfg.slownessAmplifier = v))
                .option(intOpt("Weakness amplifier", "0=I, 1=II, 2=III...", 0, 10,
                        () -> cfg.weaknessAmplifier, v -> cfg.weaknessAmplifier = v))
                .build();
    }

    private static OptionGroup groupCollapse(BlackHoleConfig cfg) {
        return OptionGroup.createBuilder()
                .name(Text.literal("Singularity Collapse"))
                .option(boolOpt("Enabled", "At the end, deal a burst of damage once.", () -> cfg.collapseEnabled, v -> cfg.collapseEnabled = v))
                .option(floatOpt("Collapse damage", "Flat burst damage. 5 hearts = 10.0.", 0.0f, 10000f,
                        () -> cfg.collapseDamage, v -> cfg.collapseDamage = v))
                .option(floatOpt("Collapse radius", "Usually match core radius.", 0.5f, 128f,
                        () -> cfg.collapseRadius, v -> cfg.collapseRadius = v))
                .option(boolOpt("Hit caster", "If true, caster can be hit by the collapse.", () -> cfg.collapseHitCaster, v -> cfg.collapseHitCaster = v))
                .build();
    }

    private static OptionGroup groupTerrain(BlackHoleConfig cfg) {
        return OptionGroup.createBuilder()
                .name(Text.literal("Terrain Ripping"))
                .option(boolOpt("Rip blocks", "If disabled, no terrain is pulled.", () -> cfg.ripBlocks, v -> cfg.ripBlocks = v))
                .option(intOpt("Blocks per rip", "How many blocks become falling blocks per cycle.", 0, 256,
                        () -> cfg.blocksPerRip, v -> cfg.blocksPerRip = v))
                .option(intOpt("Rip interval (ticks)", "Lower = more intense. Higher = safer performance.", 1, 200,
                        () -> cfg.ripIntervalTicks, v -> cfg.ripIntervalTicks = v))
                .option(doubleOpt("Falling block kick", "Initial shove toward the center.", 0.0, 3.0,
                        () -> cfg.fallingBlockKick, v -> cfg.fallingBlockKick = v))
                .build();
    }

    private static OptionGroup groupPerformance(BlackHoleConfig cfg) {
        return OptionGroup.createBuilder()
                .name(Text.literal("Performance"))
                .option(intOpt("Max targets per tick", "Caps entities processed per tick to prevent spikes.", 10, 2000,
                        () -> cfg.maxTargetsPerTick, v -> cfg.maxTargetsPerTick = v))
                .option(intOpt("Max falling blocks nearby", "Stops ripping if too many falling blocks exist.", 0, 5000,
                        () -> cfg.maxFallingBlocksNearby, v -> cfg.maxFallingBlocksNearby = v))
                .option(boolOpt("Delete debris in core", "Deletes falling blocks near center to prevent pile-up.", () -> cfg.deleteDebrisInCore, v -> cfg.deleteDebrisInCore = v))
                .option(boolOpt("Delete items in core", "Deletes items near center to prevent pile-up.", () -> cfg.deleteItemsInCore, v -> cfg.deleteItemsInCore = v))
                .build();
    }

    // helpers
    private static Option<Boolean> boolOpt(String name, String desc, Getter<Boolean> get, Setter<Boolean> set) {
        return Option.<Boolean>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(get.get(), get::get, set::set)
                .controller(BooleanControllerBuilder::create)
                .build();
    }

    private static Option<Integer> intOpt(String name, String desc, int min, int max, Getter<Integer> get, Setter<Integer> set) {
        return Option.<Integer>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(get.get(), get::get, set::set)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt).min(min).max(max))
                .build();
    }

    private static Option<Float> floatOpt(String name, String desc, float min, float max, Getter<Float> get, Setter<Float> set) {
        return Option.<Float>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(get.get(), get::get, set::set)
                .controller(opt -> FloatFieldControllerBuilder.create(opt).min(min).max(max))
                .build();
    }

    private static Option<Double> doubleOpt(String name, String desc, double min, double max, Getter<Double> get, Setter<Double> set) {
        return Option.<Double>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(get.get(), get::get, set::set)
                .controller(opt -> DoubleFieldControllerBuilder.create(opt).min(min).max(max))
                .build();
    }

    private interface Getter<T> { T get(); }
    private interface Setter<T> { void set(T v); }
}
