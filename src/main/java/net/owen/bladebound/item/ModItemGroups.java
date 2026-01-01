package net.owen.bladebound.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.owen.bladebound.BladeboundBlocks;

public class ModItemGroups {

    public static final ItemGroup BLADEBOUND_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of("bladebound", "bladebound"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.EXCALIBUR))
                    .displayName(Text.translatable("itemGroup.bladebound"))
                    .entries((displayContext, entries) -> {

                        // Items
                        entries.add(ModItems.CODEX);
                        entries.add(ModItems.MURASAME_GAUNTLETS);
                        entries.add(ModItems.COOLDOWN_BRACELET);
                        entries.add(ModItems.FIXED_COOLDOWN_BRACELET);

                        // Swords
                        entries.add(ModItems.SANDAIKITETSU);
                        entries.add(ModItems.WADOICHIMONJI);
                        entries.add(ModItems.STARKAXE);
                        entries.add(ModItems.ZENITSUSWORD);
                        entries.add(ModItems.MURASAME);
                        entries.add(ModItems.EXCALIBUR);

                        // Staffs
                        entries.add(ModItems.FRIEREN_STAFF);
                        entries.add(ModItems.FRIEREN_STAFF_CREATIVE);

                        // Spells
                        entries.add(ModItems.FIREBOLT_SPELL);
                        entries.add(ModItems.FROST_RAY_SPELL);
                        entries.add(ModItems.HEAL_SPELL);
                        entries.add(ModItems.STONE_DART_SPELL);
                        entries.add(ModItems.LIGHTNING_SPELL);
                        entries.add(ModItems.MANA_BARRIER_SPELL);
                        entries.add(ModItems.ZOLTRAAK_SPELL);
                        entries.add(ModItems.PERFECT_HEAL_SPELL);
                        entries.add(ModItems.WORLD_REWRITE_SPELL);

                        // Materials
                        entries.add(ModItems.CURSED_KITETSU_SHARD);
                        entries.add(ModItems.STEEL_INGOT);

                        // Foods
                        entries.add(ModItems.MANA_APPLE);
                        entries.add(ModItems.GREATER_MANA_APPLE);

                        // Blocks (add the *item* form)
                        entries.add(BladeboundBlocks.SWORD_IN_STONE_ITEM);
                    })
                    .build()
    );

    public static void register() {
        // triggers static init
    }
}
