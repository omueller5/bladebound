package net.owen.bladebound;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class BladeboundYaclScreen {
    private BladeboundYaclScreen() {}

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("BladeBound Settings"))
                .save(() -> BladeboundConfig.save(FabricLoader.getInstance().getConfigDir()))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("HUD"))

                        // --- Reset button (top) ---
                        .option(ButtonOption.createBuilder()
                                .name(Text.literal("Reset BladeBound to Defaults"))
                                .description(OptionDescription.of(
                                        Text.literal("Resets ALL BladeBound settings back to default values."),
                                        Text.literal("Tip: close and re-open this screen to see every toggle snap back instantly.")
                                ))
                                .action((yaclScreen, thisOption) -> {
                                    BladeboundConfig.DATA = new BladeboundConfig.BladeboundConfigData();
                                    BladeboundConfig.save(FabricLoader.getInstance().getConfigDir());
                                })
                                .build())

                        .option(LabelOption.create(Text.literal(" ")))
                        .option(LabelOption.create(Text.literal("Discipline HUD")))

                        .option(boolOption("Enable Discipline HUD",
                                "Shows the discipline bar when Wado is held.",
                                () -> BladeboundConfig.DATA.hudEnabled,
                                v -> BladeboundConfig.DATA.hudEnabled = v))
                        .option(boolOption("Show HUD Text",
                                "Shows the 'Discipline: xx%' label.",
                                () -> BladeboundConfig.DATA.hudShowText,
                                v -> BladeboundConfig.DATA.hudShowText = v))
                        .option(intOption("HUD Y Offset",
                                "Moves the bar up/down. Negative = higher.",
                                () -> BladeboundConfig.DATA.hudYOffset,
                                v -> BladeboundConfig.DATA.hudYOffset = v))
                        .option(intOption("HUD Width",
                                "Width of the bar in pixels.",
                                () -> BladeboundConfig.DATA.hudWidth,
                                v -> BladeboundConfig.DATA.hudWidth = v))
                        .option(intOption("HUD Height",
                                "Height of the bar in pixels.",
                                () -> BladeboundConfig.DATA.hudHeight,
                                v -> BladeboundConfig.DATA.hudHeight = v))

                        // ----------------------------
                        // Mana HUD (Staff)
                        // ----------------------------
                        .option(LabelOption.create(Text.literal(" ")))
                        .option(LabelOption.create(Text.literal("Mana HUD")))

                        .option(boolOption("Enable Mana HUD",
                                "Shows the mana bar when Frieren's Staff (or the Creative Staff) is held.",
                                () -> BladeboundConfig.DATA.manaHudEnabled,
                                v -> BladeboundConfig.DATA.manaHudEnabled = v))

                        .option(enumOption("Mana Numbers",
                                "Controls the numbers shown under the mana bar: OFF, CURRENT/MAX, or PERCENT.",
                                BladeboundConfig.BladeboundConfigData.ManaHudNumbersMode.class,
                                () -> BladeboundConfig.DATA.manaHudNumbersMode,
                                v -> BladeboundConfig.DATA.manaHudNumbersMode = v))

                        .option(intOption("Mana Numbers Y Offset",
                                "How many pixels below the mana bar to draw the numbers.",
                                () -> BladeboundConfig.DATA.manaHudNumbersYOffset,
                                v -> BladeboundConfig.DATA.manaHudNumbersYOffset = Math.max(0, v)))

                        // ----------------------------
                        // Mob Health Indicator
                        // ----------------------------
                        .option(LabelOption.create(Text.literal(" ")))
                        .option(LabelOption.create(Text.literal("Mob Health Indicator")))

                        .option(enumOption("Mob Health Mode",
                                "OFF disables it. TEXT ONLY shows numbers. BAR ONLY shows a bar. HYBRID shows bar + numbers.",
                                BladeboundConfig.BladeboundConfigData.MobHealthHudMode.class,
                                () -> BladeboundConfig.DATA.mobHealthHudMode,
                                v -> BladeboundConfig.DATA.mobHealthHudMode = v))

                        .option(boolOption("Smooth Mob Health Bar",
                                "Animates the bar smoothly instead of snapping each hit.",
                                () -> BladeboundConfig.DATA.mobHealthSmooth,
                                v -> BladeboundConfig.DATA.mobHealthSmooth = v))

                        .option(intOption("Mob Health Hold Time (ticks)",
                                "How long to keep showing the last target after you stop aiming at it. 20 ticks = 1 second.",
                                () -> BladeboundConfig.DATA.mobHealthHoldTicks,
                                v -> BladeboundConfig.DATA.mobHealthHoldTicks = Math.max(0, v)))

                        // Damage popups
                        .option(boolOption("Damage Popups",
                                "Shows a damage number when the target loses health.",
                                () -> BladeboundConfig.DATA.mobHealthDamagePopups,
                                v -> BladeboundConfig.DATA.mobHealthDamagePopups = v))

                        .option(intOption("Damage Popup Duration (ticks)",
                                "How long the damage number stays visible. 20 ticks = 1 second.",
                                () -> BladeboundConfig.DATA.mobHealthDamagePopupTicks,
                                v -> BladeboundConfig.DATA.mobHealthDamagePopupTicks = Math.max(0, v)))

                        .option(boolOption("Boss Styling",
                                "Special colors for bosses (Warden/Wither/Dragon/Elder Guardian).",
                                () -> BladeboundConfig.DATA.mobHealthBossStyle,
                                v -> BladeboundConfig.DATA.mobHealthBossStyle = v))

                        .option(boolOption("Hit Confirm Sound",
                                "Plays a subtle click when damage is detected (client-side).",
                                () -> BladeboundConfig.DATA.mobHealthHitSound,
                                v -> BladeboundConfig.DATA.mobHealthHitSound = v))

                        .build())

                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Gameplay"))

                        .option(LabelOption.create(Text.literal("Perfect Form")))
                        .option(boolOption("Perfect Form Effects",
                                "Enables special visual effects at high discipline.",
                                () -> BladeboundConfig.DATA.perfectFormEffects,
                                v -> BladeboundConfig.DATA.perfectFormEffects = v))

                        .option(LabelOption.create(Text.literal(" ")))
                        .option(LabelOption.create(Text.literal("Enchant Rules")))
                        .option(boolOption("Restrict Enchantments",
                                "Only allows Unbreaking + Mending on BladeBound swords.",
                                () -> BladeboundConfig.DATA.enforceAllowedEnchantments,
                                v -> BladeboundConfig.DATA.enforceAllowedEnchantments = v))

                        .option(LabelOption.create(Text.literal(" ")))
                        .option(LabelOption.create(Text.literal("Durability")))
                        .option(boolOption("Enable Durability Loss",
                                "If disabled, BladeBound swords won't lose durability on hit.",
                                () -> BladeboundConfig.DATA.durabilityEnabled,
                                v -> BladeboundConfig.DATA.durabilityEnabled = v))
                        .option(intOption("Durability Loss Per Hit",
                                "How much durability is consumed per hit.",
                                () -> BladeboundConfig.DATA.durabilityPerHit,
                                v -> BladeboundConfig.DATA.durabilityPerHit = Math.max(0, v)))
                        .build())

                .build()
                .generateScreen(parent);
    }

    // ---------- helpers ----------

    private static Option<Boolean> boolOption(String name, String desc,
                                              java.util.function.Supplier<Boolean> get,
                                              java.util.function.Consumer<Boolean> set) {
        return Option.<Boolean>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(get.get(), get::get, set::accept)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static Option<Integer> intOption(String name, String desc,
                                             java.util.function.Supplier<Integer> get,
                                             java.util.function.Consumer<Integer> set) {
        return Option.<Integer>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(get.get(), get::get, set::accept)
                .controller(IntegerFieldControllerBuilder::create)
                .build();
    }

    private static <E extends Enum<E>> Option<E> enumOption(
            String name,
            String desc,
            Class<E> enumClass,
            java.util.function.Supplier<E> get,
            java.util.function.Consumer<E> set
    ) {
        return Option.<E>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(get.get(), get::get, set::accept)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(enumClass))
                .build();
    }
}
