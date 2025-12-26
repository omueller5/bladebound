package net.owen.bladebound.item;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.owen.bladebound.item.custom.ExcaliburItem;
import net.owen.bladebound.item.custom.MurasameItem;
import net.owen.bladebound.item.custom.SandaiKitetsuItem;
import net.owen.bladebound.item.custom.WadoIchimonjiItem;
import net.owen.bladebound.item.custom.MurasameGauntletsItem;


public class ModItems {

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

    public static final Item MURASAME = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "murasame"),
            new MurasameItem(
                    new Item.Settings()
                            .maxCount(1)
                            .fireproof()
                            .maxDamage(2031) // Legendary -> Netherite durability
                            .attributeModifiers(MURASAME_ATTRIBUTES)
            )
    );

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

    public static final Item EXCALIBUR = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "excalibur"),
            new ExcaliburItem(
                    new Item.Settings()
                            .maxCount(1)
                            .fireproof()
                            .maxDamage(2031) // Legendary -> Netherite durability
                            .attributeModifiers(EXCALIBUR_ATTRIBUTES)
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
            )
    );

    public static final Item SANDAIKITETSU = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "sandai-kitetsu"),
            new SandaiKitetsuItem(
                    ToolMaterials.IRON, // iron damage + durability
                    new Item.Settings()
                            .maxCount(1)
                            .rarity(Rarity.RARE)
            )
    );

    public static final Item MURASAME_GAUNTLETS = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "murasame-gauntlets"),
            new MurasameGauntletsItem(new Item.Settings().maxCount(1))
    );


    public static final Item CODEX = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "bladebound-codex"),
            new BladeboundCodexItem(new Item.Settings().maxCount(1))
    );




    /**
     * Called from Bladebound.onInitialize()
     * Forces class loading so static registration runs.
     */
    public static void register() {
        // no-op
    }
}
