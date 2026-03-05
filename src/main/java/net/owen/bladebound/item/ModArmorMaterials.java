package net.owen.bladebound.item;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.owen.bladebound.Bladebound;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModArmorMaterials {

    public static final RegistryEntry<ArmorMaterial> ARCHMAGE = register(
            "archmage",
            Map.of(
                    ArmorItem.Type.HELMET, 2,
                    ArmorItem.Type.CHESTPLATE, 6,
                    ArmorItem.Type.LEGGINGS, 5,
                    ArmorItem.Type.BOOTS, 2
            ),
            9,
            () -> Ingredient.ofItems(Items.IRON_INGOT),
            0.0F,
            0.0F,
            false
    );

    private static RegistryEntry<ArmorMaterial> register(
            String id,
            Map<ArmorItem.Type, Integer> defense,
            int enchantability,
            Supplier<Ingredient> repairIngredient,
            float toughness,
            float knockbackResistance,
            boolean dyeable
    ) {
        ArmorMaterial material = new ArmorMaterial(
                defense,
                enchantability,
                SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                repairIngredient,
                List.of(new ArmorMaterial.Layer(Identifier.of(Bladebound.MOD_ID, id), "", dyeable)),
                toughness,
                knockbackResistance
        );

        ArmorMaterial registered = Registry.register(
                Registries.ARMOR_MATERIAL,
                Identifier.of(Bladebound.MOD_ID, id),
                material
        );

        return Registries.ARMOR_MATERIAL.getEntry(registered);
    }

    private ModArmorMaterials() {}
}
