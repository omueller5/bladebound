package net.owen.bladebound.item;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.owen.bladebound.Bladebound;
import net.owen.bladebound.item.custom.*;

import java.util.List;


public class ModItems {

    //---------------
    // Swords
    //---------------
    private static final AttributeModifiersComponent MURASAME_ATTRIBUTES =
            AttributeModifiersComponent.builder()
                    // Netherite sword damage (8.0 total)
                    .add(
                            EntityAttributes.GENERIC_ATTACK_DAMAGE,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "murasame_damage"),
                                    7.0,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    // Netherite sword speed
                    .add(
                            EntityAttributes.GENERIC_ATTACK_SPEED,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "murasame_speed"),
                                    3.4,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    .build();

    private static final AttributeModifiersComponent EXCALIBUR_ATTRIBUTES =
            AttributeModifiersComponent.builder()
                    .add(
                            EntityAttributes.GENERIC_ATTACK_DAMAGE,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "excalibur_damage"),
                                    7.0,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    .add(
                            EntityAttributes.GENERIC_ATTACK_SPEED,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "excalibur_speed"),
                                    2.4,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    .build();

    private static final AttributeModifiersComponent WADO_ATTRIBUTES =
            AttributeModifiersComponent.builder()
                    .add(
                            EntityAttributes.GENERIC_ATTACK_DAMAGE,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "wado_damage"),
                                    5.0,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    .add(
                            EntityAttributes.GENERIC_ATTACK_SPEED,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "wado_speed"),
                                    1.4,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    .build();

    private static final AttributeModifiersComponent SANDAI_ATTRIBUTES =
            AttributeModifiersComponent.builder()
                    .add(
                            EntityAttributes.GENERIC_ATTACK_DAMAGE,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "sandai_damage"),
                                    5.0,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    .add(
                            EntityAttributes.GENERIC_ATTACK_SPEED,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "sandai_speed"),
                                    1.4,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    .build();

    // Weapons
    public static final Item EXCALIBUR = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "excalibur"),
            new ExcaliburItem(
                    new Item.Settings()
                            .maxCount(1)
                            .fireproof()
                            .maxDamage(2031) // Legendary -> Netherite durability
                            .attributeModifiers(EXCALIBUR_ATTRIBUTES)
                            .rarity(Rarity.EPIC)
            )
    );

    public static final Item MURASAME = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "murasame"),
            new MurasameItem(
                    new Item.Settings()
                            .maxCount(1)
                            .fireproof()
                            .maxDamage(2031) // Legendary -> Netherite durability
                            .attributeModifiers(MURASAME_ATTRIBUTES)
                            .rarity(Rarity.EPIC)
            )
    );

    public static final Item WADOICHIMONJI = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "wado-ichimonji"),
            new WadoIchimonjiItem(
                    new Item.Settings()
                            .maxCount(1)
                            .fireproof()
                            .maxDamage(1561) // Non-legendary -> Diamond durability
                            .attributeModifiers(WADO_ATTRIBUTES)
                            .rarity(Rarity.RARE)
            )
    );

    public static final Item SANDAIKITETSU = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "sandai-kitetsu"),
            new SandaiKitetsuItem(
                    ToolMaterials.IRON, // iron damage + durability
                    new Item.Settings()
                            .maxCount(1)
                            .maxDamage(783)
                            .attributeModifiers(SANDAI_ATTRIBUTES)
                            .rarity(Rarity.UNCOMMON)
            )
    );

    public static final Item STARKAXE = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "stark-axe"),
            new StarkAxeItem(
                    ToolMaterials.DIAMOND,
                    4.0f,    // damage modifier → ~8 total damage
                    -2.8f,   // speed modifier → 1.2 attack speed
                    new Item.Settings()
                            .maxCount(1)
                            .rarity(Rarity.RARE)
            )
    );

    public static final Item ZENITSUSWORD = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "zenitsu-nichirin"),
            new ZenitsuSwordItem(
                    ToolMaterials.DIAMOND,
                    3,        // diamond sword level damage
                    -2.3f,    // speed modifier → 1.7 attack speed
                    new Item.Settings()
                            .maxCount(1)
                            .rarity(Rarity.RARE)
            )
    );

    //---------------
    // Items
    //---------------

    public static final Item MURASAME_GAUNTLETS = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "murasame-gauntlets"),
            new MurasameGauntletsItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE))
    );

    public static final Item CURSED_KITETSU_SHARD =
            Registry.register(
                    Registries.ITEM,
                    Identifier.of("bladebound", "cursed-kitetsu-shard"),
                    new Item(new Item.Settings().rarity(Rarity.RARE))
            );

    public static final Item STEEL_INGOT =
            Registry.register(
                    Registries.ITEM,
                    Identifier.of("bladebound", "steel_ingot"),
                    new Item(new Item.Settings().rarity(Rarity.UNCOMMON))
            );

    public static final Item CODEX = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "bladebound-codex"),
            new BladeboundCodexItem(new Item.Settings().maxCount(1))
    );

    //---------------
    // Magical items
    //---------------
    public static final Item FRIEREN_STAFF = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "frieren-staff"),
            new FrierenStaffItem(
                    new Item.Settings()
                            .maxCount(1)
                            .maxDamage(550)
                            .rarity(Rarity.EPIC)
            )
    );

    public static final Item FRIEREN_STAFF_CREATIVE = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "frieren-staff-creative"),
            new FrierenStaffCreativeItem(
                    new Item.Settings()
                            .maxCount(1)
                            .rarity(Rarity.EPIC)
            )
    );

    public static final Item FIREBOLT_SPELL = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "firebolt_spell"),
            new SpellGrantItem(
                    new Item.Settings().maxCount(16),
                    Identifier.of("bladebound", "firebolt"),
                    Formatting.GREEN,
                    List.of(
                            Text.literal("STARTER SPELL").formatted(Formatting.GREEN, Formatting.BOLD),
                            Text.literal("Launches a condensed bolt of flame.").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal("Simple, reliable, and dangerous in practiced hands.").formatted(Formatting.GRAY, Formatting.ITALIC)
                    )
            )
    );

    public static final Item FROST_RAY_SPELL = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "frost_ray_spell"),
            new SpellGrantItem(
                    new Item.Settings().maxCount(16),
                    Identifier.of("bladebound", "frost_ray"),
                    Formatting.GREEN,
                    List.of(
                            Text.literal("STARTER SPELL").formatted(Formatting.GREEN, Formatting.BOLD),
                            Text.literal("Unleashes a chilling beam of frost.").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal("Slows enemies, leaving them vulnerable to follow-up attacks.").formatted(Formatting.GRAY, Formatting.ITALIC)
                    )
            )
    );

    public static final Item HEAL_SPELL = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "heal_spell"),
            new SpellGrantItem(
                    new Item.Settings().maxCount(16),
                    Identifier.of("bladebound", "heal"),
                    Formatting.GREEN,
                    List.of(
                            Text.literal("STARTER SPELL").formatted(Formatting.GREEN, Formatting.BOLD),
                            Text.literal("Mends minor wounds through focused mana.").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal("Restores a small amount of health when invoked.").formatted(Formatting.GRAY, Formatting.ITALIC)
                    )
            )
    );

    public static final Item STONE_DART_SPELL = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "stone_dart_spell"),
            new SpellGrantItem(
                    new Item.Settings().maxCount(16),
                    Identifier.of("bladebound", "stone_dart"),
                    Formatting.GREEN,
                    List.of(
                            Text.literal("STARTER SPELL").formatted(Formatting.GREEN, Formatting.BOLD),
                            Text.literal("Fires a quick magic dart.").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal("Great for early combat and pulling enemies.").formatted(Formatting.GRAY, Formatting.ITALIC)
                    )
            )
    );

    public static final Item LIGHTNING_SPELL = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "lightning_spell"),
            new SpellGrantItem(
                    new Item.Settings().maxCount(16),
                    Identifier.of("bladebound", "lightning_strike"),
                    Formatting.BLUE,
                    List.of(
                            Text.literal("RARE SPELL").formatted(Formatting.BLUE, Formatting.BOLD),
                            Text.literal("Calls down lightning at the targeted location.").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal("Strikes enemies or blocks in your line of sight.").formatted(Formatting.GRAY, Formatting.ITALIC)
                    )
            )
    );

    public static final Item MANA_BARRIER_SPELL = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "mana_barrier_spell"),
            new SpellGrantItem(
                    new Item.Settings().maxCount(16),
                    Identifier.of("bladebound", "mana_barrier"),
                    Formatting.BLUE,
                    List.of(
                            Text.literal("RARE SPELL").formatted(Formatting.BLUE, Formatting.BOLD),
                            Text.literal("A condensed shell of mana woven into a living shield.").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal("It stands between the caster and all harm,").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal("unyielding so long as mana continues to flow.").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal(""),
                            Text.literal("Those lacking discipline will find the barrier unforgiving,").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC),
                            Text.literal("as the strain of maintaining it exacts a heavy toll.").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC)
                    )
            )
    );

    public static final Item ZOLTRAAK_SPELL = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "zoltraak_spell"),
            new SpellGrantItem(
                    new Item.Settings().maxCount(16),
                    Identifier.of("bladebound", "zoltraak"),
                    Formatting.GOLD,
                    List.of(
                            Text.literal("LEGENDARY SPELL").formatted(Formatting.GOLD, Formatting.BOLD),
                            Text.literal("A piercing beam of refined mana.").formatted(Formatting.DARK_AQUA, Formatting.ITALIC),
                            Text.literal("Feared even by demons.").formatted(Formatting.GRAY, Formatting.ITALIC)
                    )
            )
    );

    public static final Item PERFECT_HEAL_SPELL = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "perfect_heal_spell"),
            new SpellGrantItem(
                    new Item.Settings().maxCount(16),
                    Identifier.of("bladebound", "perfect_heal"),
                    Formatting.GOLD,
                    List.of(
                            Text.literal("LEGENDARY SPELL").formatted(Formatting.GOLD, Formatting.BOLD),
                            Text.literal("Restores you to full health instantly.").formatted(Formatting.GREEN, Formatting.ITALIC),
                            Text.literal("Cleanses all negative status effects.").formatted(Formatting.AQUA, Formatting.ITALIC)
                    )
            )
    );

    public static final Item WORLD_REWRITE_SPELL = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "world_rewrite_spell"),
            new SpellGrantItem(
                    new Item.Settings().maxCount(1),
                    Identifier.of("bladebound", "world_rewrite"),
                    Formatting.RED,
                    List.of(
                            Text.literal("ANCIENT SPELL").formatted(Formatting.RED, Formatting.BOLD),
                            Text.literal("This spell cannot be learned by conventional means.").formatted(Formatting.YELLOW, Formatting.ITALIC),
                            Text.literal(""),
                            Text.literal("This grimoire does not channel magic — it overwrites it.").formatted(Formatting.AQUA),
                            Text.literal(""),
                            Text.literal("Reality bends, time falters, and all motion is rendered").formatted(Formatting.AQUA),
                            Text.literal("meaningless within its domain.").formatted(Formatting.AQUA),
                            Text.literal(""),
                            Text.literal("The knowledge sealed within predates modern spellcraft,").formatted(Formatting.LIGHT_PURPLE),
                            Text.literal("feared even by those who once ruled the arcane.").formatted(Formatting.LIGHT_PURPLE),
                            Text.literal(""),
                            Text.literal("To invoke it is not to cast a spell,").formatted(Formatting.AQUA),
                            Text.literal("but to assert dominion over the world itself.").formatted(Formatting.AQUA)
                    )
            )
    );


    // Food Items

    public static final FoodComponent ALWAYS_EDIBLE_APPLE = new FoodComponent.Builder()
            .nutrition(4)
            .saturationModifier(2.4f)
            .alwaysEdible()
            .build();

    public static final Item MANA_APPLE = Registry.register(
            Registries.ITEM,
            Identifier.of(Bladebound.MOD_ID, "mana_apple"),
            new ManaAppleItem(new Item.Settings().rarity(Rarity.RARE)
                    .food(ALWAYS_EDIBLE_APPLE)
                    .maxCount(16),
                    50)
    );

    public static final Item GREATER_MANA_APPLE = Registry.register(
            Registries.ITEM,
            Identifier.of(Bladebound.MOD_ID, "greater_mana_apple"),
            new ManaAppleItem(new Item.Settings().rarity(Rarity.EPIC)
                    .food(ALWAYS_EDIBLE_APPLE)
                    .maxCount(8),
                    100)
    );

    public static final Item COOLDOWN_BRACELET = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "cooldown_bracelet"),
            new CooldownBraceletItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE))
    );

    public static final Item FIXED_COOLDOWN_BRACELET = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "fixed_cooldown_bracelet"),
            new FixedCooldownBraceletItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON))
    );

    /**
     * Called from Bladebound.onInitialize()
     * Forces class loading so static registration runs.
     */
    public static void register() {
        // no-op
    }
}
