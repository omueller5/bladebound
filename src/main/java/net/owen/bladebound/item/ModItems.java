package net.owen.bladebound.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.owen.bladebound.Bladebound;
import net.owen.bladebound.item.custom.*;

import java.util.List;

public class ModItems {

    private static Identifier id(String path) {
        return Identifier.of(Bladebound.MOD_ID, path);
    }

    private static Item.Settings itemSettings(String path) {
        return new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id(path)));
    }

    private static <T extends Item> T registerItem(String path, T item) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id(path));
        return Registry.register(Registries.ITEM, key, item);
    }

    //---------------
    // Swords
    //---------------
    private static final AttributeModifiersComponent MURASAME_ATTRIBUTES =
            AttributeModifiersComponent.builder()
                    // Netherite sword damage (8.0 total)
                    .add(
                            EntityAttributes.ATTACK_DAMAGE,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "murasame_damage"),
                                    7.0,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    // Netherite sword speed
                    .add(
                            EntityAttributes.ATTACK_SPEED,
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
                            EntityAttributes.ATTACK_DAMAGE,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "excalibur_damage"),
                                    7.0,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    .add(
                            EntityAttributes.ATTACK_SPEED,
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
                            EntityAttributes.ATTACK_DAMAGE,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "wado_damage"),
                                    5.0,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    .add(
                            EntityAttributes.ATTACK_SPEED,
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
                            EntityAttributes.ATTACK_DAMAGE,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "sandai_damage"),
                                    5.0,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    .add(
                            EntityAttributes.ATTACK_SPEED,
                            new EntityAttributeModifier(
                                    Identifier.of("bladebound", "sandai_speed"),
                                    1.4,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.MAINHAND
                    )
                    .build();

    // Weapons
    public static final Item EXCALIBUR = registerItem("excalibur", new ExcaliburItem(
                    itemSettings("excalibur")
                            .maxCount(1)
                            .fireproof()
                            .maxDamage(2031) // Legendary -> Netherite durability
                            .attributeModifiers(EXCALIBUR_ATTRIBUTES)
                            .rarity(Rarity.EPIC)
            )
    );

    public static final Item MURASAME = registerItem("murasame", new MurasameItem(
                    itemSettings("murasame")
                            .maxCount(1)
                            .fireproof()
                            .maxDamage(2031) // Legendary -> Netherite durability
                            .attributeModifiers(MURASAME_ATTRIBUTES)
                            .rarity(Rarity.EPIC)
            )
    );

    public static final Item WADOICHIMONJI = registerItem("wado-ichimonji", new WadoIchimonjiItem(
                    itemSettings("wado-ichimonji")
                            .maxCount(1)
                            .fireproof()
                            .maxDamage(1561) // Non-legendary -> Diamond durability
                            .attributeModifiers(WADO_ATTRIBUTES)
                            .rarity(Rarity.RARE)
            )
    );

    public static final Item SANDAIKITETSU = registerItem("sandai-kitetsu", new SandaiKitetsuItem(
                    ToolMaterial.IRON, // iron damage + durability
                    itemSettings("sandai-kitetsu")
                            .maxCount(1)
                            .maxDamage(783)
                            .attributeModifiers(SANDAI_ATTRIBUTES)
                            .rarity(Rarity.UNCOMMON)
            )
    );

    public static final Item STARKAXE = registerItem("stark-axe", new StarkAxeItem(
                    ToolMaterial.DIAMOND,
                    4.0f,    // damage modifier → ~8 total damage
                    -2.8f,   // speed modifier → 1.2 attack speed
                    itemSettings("stark-axe")
                            .maxCount(1)
                            .rarity(Rarity.RARE)
            )
    );

    public static final Item ZENITSUSWORD = registerItem("zenitsu-nichirin", new ZenitsuSwordItem(
                    ToolMaterial.DIAMOND,
                    3,        // diamond sword level damage
                    -2.3f,    // speed modifier → 1.7 attack speed
                    itemSettings("zenitsu-nichirin")
                            .maxCount(1)
                            .rarity(Rarity.RARE)
            )
    );

    public static final Item SOULKATANA = registerItem("split-soul-katana", new SoulSplitKatanaItem(
                    ToolMaterial.NETHERITE,
                    4,        // Netherite sword bonus damage (+1 over diamond)
                    -2.4f,    // Standard sword speed (1.6 attack speed)
                    itemSettings("split-soul-katana")
                            .maxCount(1)
                            .rarity(Rarity.EPIC) // Legendary tier
            )
    );


    //---------------
    // Items
    //---------------

    public static final Item MURASAME_GAUNTLETS = registerItem("murasame-gauntlets", new MurasameGauntletsItem(itemSettings("murasame-gauntlets").maxCount(1).rarity(Rarity.RARE))
    );

    public static final Item CURSED_KITETSU_SHARD =
            registerItem("cursed-kitetsu-shard", new Item(itemSettings("cursed-kitetsu-shard").rarity(Rarity.RARE))
            );

    public static final Item STEEL_INGOT =
            registerItem("steel_ingot", new Item(itemSettings("steel_ingot").rarity(Rarity.UNCOMMON))
            );

    public static final Item CODEX = registerItem("bladebound-codex", new BladeboundCodexItem(itemSettings("bladebound-codex").maxCount(1))
    );

    //---------------
    // Magical items
    //---------------
    public static final Item FRIEREN_STAFF = registerItem("frieren-staff", new FrierenStaffItem(
                    itemSettings("frieren-staff")
                            .maxCount(1)
                            .maxDamage(550)
                            .rarity(Rarity.EPIC)
            )
    );

    public static final Item FRIEREN_STAFF_CREATIVE = registerItem("frieren-staff-creative", new FrierenStaffCreativeItem(
                    itemSettings("frieren-staff-creative")
                            .maxCount(1)
                            .rarity(Rarity.EPIC)
            )
    );

    public static final Item FIREBOLT_SPELL = registerItem("firebolt_spell", new SpellGrantItem(
                    itemSettings("firebolt_spell").maxCount(16),
                    Identifier.of("bladebound", "firebolt"),
                    Formatting.GREEN,
                    List.of(
                            Text.literal("STARTER SPELL").formatted(Formatting.GREEN, Formatting.BOLD),
                            Text.literal("Launches a condensed bolt of flame.").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal("Simple, reliable, and dangerous in practiced hands.").formatted(Formatting.GRAY, Formatting.ITALIC)
                    )
            )
    );

    public static final Item FROST_RAY_SPELL = registerItem("frost_ray_spell", new SpellGrantItem(
                    itemSettings("frost_ray_spell").maxCount(16),
                    Identifier.of("bladebound", "frost_ray"),
                    Formatting.GREEN,
                    List.of(
                            Text.literal("STARTER SPELL").formatted(Formatting.GREEN, Formatting.BOLD),
                            Text.literal("Unleashes a chilling beam of frost.").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal("Slows enemies, leaving them vulnerable to follow-up attacks.").formatted(Formatting.GRAY, Formatting.ITALIC)
                    )
            )
    );

    public static final Item HEAL_SPELL = registerItem("heal_spell", new SpellGrantItem(
                    itemSettings("heal_spell").maxCount(16),
                    Identifier.of("bladebound", "heal"),
                    Formatting.GREEN,
                    List.of(
                            Text.literal("STARTER SPELL").formatted(Formatting.GREEN, Formatting.BOLD),
                            Text.literal("Mends minor wounds through focused mana.").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal("Restores a small amount of health when invoked.").formatted(Formatting.GRAY, Formatting.ITALIC)
                    )
            )
    );

    public static final Item STONE_DART_SPELL = registerItem("stone_dart_spell", new SpellGrantItem(
                    itemSettings("stone_dart_spell").maxCount(16),
                    Identifier.of("bladebound", "stone_dart"),
                    Formatting.GREEN,
                    List.of(
                            Text.literal("STARTER SPELL").formatted(Formatting.GREEN, Formatting.BOLD),
                            Text.literal("Fires a quick magic dart.").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal("Great for early combat and pulling enemies.").formatted(Formatting.GRAY, Formatting.ITALIC)
                    )
            )
    );

    public static final Item LIGHTNING_SPELL = registerItem("lightning_spell", new SpellGrantItem(
                    itemSettings("lightning_spell").maxCount(16),
                    Identifier.of("bladebound", "lightning_strike"),
                    Formatting.BLUE,
                    List.of(
                            Text.literal("RARE SPELL").formatted(Formatting.BLUE, Formatting.BOLD),
                            Text.literal("Calls down lightning at the targeted location.").formatted(Formatting.AQUA, Formatting.ITALIC),
                            Text.literal("Strikes enemies or blocks in your line of sight.").formatted(Formatting.GRAY, Formatting.ITALIC)
                    )
            )
    );

    public static final Item MANA_BARRIER_SPELL = registerItem("mana_barrier_spell", new SpellGrantItem(
                    itemSettings("mana_barrier_spell").maxCount(16),
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

    public static final Item ZOLTRAAK_SPELL = registerItem("zoltraak_spell", new SpellGrantItem(
                    itemSettings("zoltraak_spell").maxCount(16),
                    Identifier.of("bladebound", "zoltraak"),
                    Formatting.GOLD,
                    List.of(
                            Text.literal("LEGENDARY SPELL").formatted(Formatting.GOLD, Formatting.BOLD),
                            Text.literal("A piercing beam of refined mana.").formatted(Formatting.DARK_AQUA, Formatting.ITALIC),
                            Text.literal("Feared even by demons.").formatted(Formatting.GRAY, Formatting.ITALIC)
                    )
            )
    );

    public static final Item PERFECT_HEAL_SPELL = registerItem("perfect_heal_spell", new SpellGrantItem(
                    itemSettings("perfect_heal_spell").maxCount(16),
                    Identifier.of("bladebound", "perfect_heal"),
                    Formatting.GOLD,
                    List.of(
                            Text.literal("LEGENDARY SPELL").formatted(Formatting.GOLD, Formatting.BOLD),
                            Text.literal("Restores you to full health instantly.").formatted(Formatting.GREEN, Formatting.ITALIC),
                            Text.literal("Cleanses all negative status effects.").formatted(Formatting.AQUA, Formatting.ITALIC)
                    )
            )
    );

    public static final Item WORLD_REWRITE_SPELL = registerItem("world_rewrite_spell", new SpellGrantItem(
                    itemSettings("world_rewrite_spell").maxCount(1),
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

    public static final Item BLACK_HOLE_SPELL = registerItem("black_hole_spell", new SpellGrantItem(
                    itemSettings("black_hole_spell").maxCount(1),
                    Identifier.of("bladebound", "black_hole"),
                    Formatting.RED,
                    List.of(
                            Text.literal("ANCIENT SPELL").formatted(Formatting.RED, Formatting.BOLD),
                            Text.literal("This spell cannot be learned by conventional means.").formatted(Formatting.YELLOW, Formatting.ITALIC),
                            Text.literal(""),
                            Text.literal("A grimoire that describes a hunger with no end.").formatted(Formatting.DARK_PURPLE),
                            Text.literal(""),
                            Text.literal("It does not summon darkness — it creates absence.").formatted(Formatting.AQUA),
                            Text.literal("Matter, magic, and momentum are dragged into silence,").formatted(Formatting.AQUA),
                            Text.literal("spiraling toward a point that refuses to exist.").formatted(Formatting.AQUA),
                            Text.literal(""),
                            Text.literal("Within its event horizon, strength fails and will breaks,").formatted(Formatting.LIGHT_PURPLE),
                            Text.literal("as even the brave are reduced to falling debris.").formatted(Formatting.LIGHT_PURPLE),
                            Text.literal(""),
                            Text.literal("When the singularity collapses,").formatted(Formatting.AQUA),
                            Text.literal("it leaves only echoes and ash behind.").formatted(Formatting.AQUA)
                    )
            )
    );

    // Spell Scrolls
    public static final Item FIREBOLT_SCROLL = registerItem("firebolt_scroll", new net.owen.bladebound.item.custom.SpellScrollItem(
                    itemSettings("firebolt_scroll")
                            .maxCount(64)
                            .component(DataComponentTypes.CUSTOM_NAME,
                                    Text.literal("Firebolt Scroll").formatted(Formatting.GREEN))
                            .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                                    Text.literal("STARTER SPELL").formatted(Formatting.GREEN, Formatting.BOLD),
                                    Text.literal("Launches a condensed bolt of flame.").formatted(Formatting.AQUA),
                                    Text.literal("Simple, reliable, and dangerous in practiced hands.").formatted(Formatting.GRAY),
                                    Text.literal(""),
                                    Text.literal("Mana: 20").formatted(Formatting.BLUE)
                            ))),
                    Identifier.of("bladebound", "firebolt")
            )
    );

    public static final Item FROST_RAY_SCROLL = registerItem("frost_ray_scroll", new net.owen.bladebound.item.custom.SpellScrollItem(
                    itemSettings("frost_ray_scroll")
                            .maxCount(64)
                            .component(DataComponentTypes.CUSTOM_NAME,
                                    Text.literal("Frost Ray Scroll").formatted(Formatting.GREEN))
                            .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                                    Text.literal("STARTER SPELL").formatted(Formatting.GREEN, Formatting.BOLD),
                                    Text.literal("Unleashes a chilling beam of frost.").formatted(Formatting.AQUA),
                                    Text.literal("Slows enemies, leaving them vulnerable to follow-up attacks.").formatted(Formatting.GRAY),
                                    Text.literal(""),
                                    Text.literal("Mana: 25").formatted(Formatting.BLUE)
                            ))),
                    Identifier.of("bladebound", "frost_ray")
            )
    );

    public static final Item HEAL_SCROLL = registerItem("heal_scroll", new net.owen.bladebound.item.custom.SpellScrollItem(
                    itemSettings("heal_scroll")
                            .maxCount(64)
                            .component(DataComponentTypes.CUSTOM_NAME,
                                    Text.literal("Heal Scroll").formatted(Formatting.GREEN))
                            .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                                    Text.literal("STARTER SPELL").formatted(Formatting.GREEN, Formatting.BOLD),
                                    Text.literal("Mends minor wounds through focused mana.").formatted(Formatting.AQUA),
                                    Text.literal("Restores a small smount of health when invoked.").formatted(Formatting.GRAY),
                                    Text.literal(""),
                                    Text.literal("Mana: 30").formatted(Formatting.BLUE)
                            ))),
                    Identifier.of("bladebound", "heal")
            )
    );

    public static final Item STONE_DART_SCROLL = registerItem("stone_dart_scroll", new net.owen.bladebound.item.custom.SpellScrollItem(
                    itemSettings("stone_dart_scroll")
                            .maxCount(64)
                            .component(DataComponentTypes.CUSTOM_NAME,
                                    Text.literal("Stone Dart Scroll").formatted(Formatting.GREEN))
                            .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                                    Text.literal("STARTER SPELL").formatted(Formatting.GREEN, Formatting.BOLD),
                                    Text.literal("Fires a quick magic dart.").formatted(Formatting.AQUA),
                                    Text.literal("Great for early combat and pulling enemies.").formatted(Formatting.GRAY),
                                    Text.literal(""),
                                    Text.literal("Mana: 5").formatted(Formatting.BLUE)
                            ))),
                    Identifier.of("bladebound", "stone_dart")
            )
    );

    public static final Item LIGHTNING_SCROLL = registerItem("lightning_scroll", new net.owen.bladebound.item.custom.SpellScrollItem(
                    itemSettings("lightning_scroll")
                            .maxCount(64)
                            .component(DataComponentTypes.CUSTOM_NAME,
                                    Text.literal("Lightning Strike Scroll").formatted(Formatting.AQUA))
                            .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                                    Text.literal("RARE SPELL").formatted(Formatting.AQUA, Formatting.BOLD),
                                    Text.literal("Calls down lightning at the targeted location.").formatted(Formatting.AQUA),
                                    Text.literal("Strikes enemies or blocks in your line of sight.").formatted(Formatting.GRAY),
                                    Text.literal(""),
                                    Text.literal("Mana: 45").formatted(Formatting.BLUE)
                            ))),
                    Identifier.of("bladebound", "lightning_strike")
            )
    );

    public static final Item ZOLTRAAK_SCROLL = registerItem("zoltraak_scroll", new net.owen.bladebound.item.custom.SpellScrollItem(
                    itemSettings("zoltraak_scroll")
                            .maxCount(64)
                            .component(DataComponentTypes.CUSTOM_NAME,
                                    Text.literal("Zoltraak Scroll").formatted(Formatting.GOLD))
                            .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                                    Text.literal("LEGENDARY SPELL").formatted(Formatting.GOLD, Formatting.BOLD),
                                    Text.literal("A piercing beam of refined mana.").formatted(Formatting.AQUA),
                                    Text.literal("Feared even by demons.").formatted(Formatting.GRAY),
                                    Text.literal(""),
                                    Text.literal("Mana: 50").formatted(Formatting.BLUE)
                            ))),
                    Identifier.of("bladebound", "zoltraak")
            )
    );

    public static final Item PERFECT_HEAL_SCROLL = registerItem("perfect_heal_scroll", new net.owen.bladebound.item.custom.SpellScrollItem(
                    itemSettings("perfect_heal_scroll")
                            .maxCount(64)
                            .component(DataComponentTypes.CUSTOM_NAME,
                                    Text.literal("Perfect Heal Scroll").formatted(Formatting.GOLD))
                            .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                                    Text.literal("LEGENDARY SPELL").formatted(Formatting.GOLD, Formatting.BOLD),
                                    Text.literal("Restores you to full health instantly.").formatted(Formatting.GREEN, Formatting.ITALIC),
                                    Text.literal("Cleanses all negative status effects.").formatted(Formatting.AQUA, Formatting.ITALIC),
                                    Text.literal(""),
                                    Text.literal("Mana: ALL").formatted(Formatting.BLUE)
                            ))),
                    Identifier.of("bladebound", "perfect_heal")
            )
    );



    // Food Items

    public static final FoodComponent ALWAYS_EDIBLE_APPLE = new FoodComponent(
            4,
            2.4f,
            true
    );

    public static final Item MANA_APPLE = registerItem("mana_apple", new ManaAppleItem(itemSettings("mana_apple").rarity(Rarity.RARE)
                    .food(ALWAYS_EDIBLE_APPLE)
                    .maxCount(16),
                    50)
    );

    public static final Item GREATER_MANA_APPLE = registerItem("greater_mana_apple", new ManaAppleItem(itemSettings("greater_mana_apple").rarity(Rarity.EPIC)
                    .food(ALWAYS_EDIBLE_APPLE)
                    .maxCount(8),
                    100)
    );

    public static final Item COOLDOWN_BRACELET = registerItem("cooldown_bracelet", new CooldownBraceletItem(itemSettings("cooldown_bracelet").maxCount(1).rarity(Rarity.RARE))
    );

    public static final Item FIXED_COOLDOWN_BRACELET = registerItem("fixed_cooldown_bracelet", new FixedCooldownBraceletItem(itemSettings("fixed_cooldown_bracelet").maxCount(1).rarity(Rarity.UNCOMMON))
    );

    // Boss Items
    public static final Item BOSS_KEY = registerItem("boss_key", new Item(itemSettings("boss_key"))
    );

    // Armor
    public static final Item ARCHMAGE_HAT = registerItem("archmage_hat", new net.owen.bladebound.item.custom.ArchmageHatItem(
            itemSettings("archmage_hat").maxCount(1).rarity(Rarity.EPIC)
                    .armor(ArmorMaterials.NETHERITE, net.minecraft.item.equipment.EquipmentType.HELMET)
    ));

    public static final Item MAGIC_UPGRADE_SMITHING_TEMPLATE = registerItem("magic_upgrade_smithing_template", new SmithingTemplateItem(
                    Text.translatable("item.bladebound.magic_upgrade_smithing_template.applies_to")
                            .formatted(Formatting.GRAY),
                    Text.translatable("item.bladebound.magic_upgrade_smithing_template.ingredients")
                            .formatted(Formatting.GRAY),
                    Text.translatable("item.bladebound.magic_upgrade_smithing_template.base_slot_description")
                            .formatted(Formatting.BLUE),
                    Text.translatable("item.bladebound.magic_upgrade_smithing_template.additions_slot_description")
                            .formatted(Formatting.BLUE),
                    List.of(
                            Identifier.of("minecraft", "item/empty_armor_slot_helmet")
                    ),
                    List.of(
                            Identifier.of("minecraft", "item/empty_slot_ingot")
                    ),
                    itemSettings("magic_upgrade_smithing_template")
            )
    );

    public static void register() {
        // no-op
    }
}
