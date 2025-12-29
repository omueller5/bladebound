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
                    .icon(() -> new ItemStack(ModItems.FRIEREN_STAFF)) // must be a 1-count stack (this is)
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
                        entries.add(ModItems.MURASAME);
                        entries.add(ModItems.EXCALIBUR);

                        // Staffs
                        entries.add(ModItems.FRIEREN_STAFF);
                        entries.add(ModItems.FRIEREN_STAFF_CREATIVE);

                        // Blocks (add the *item* form)
                        entries.add(BladeboundBlocks.SWORD_IN_STONE_ITEM);

                        //Spells
                        entries.add(ModItems.LIGHTNING_SPELL);
                        entries.add(ModItems.ZOLTRAAK_SPELL);
                        entries.add(ModItems.PERFECT_HEAL_SPELL);

                        // Materials
                        entries.add(ModItems.CURSED_KITETSU_SHARD);

                        // Foods
                        entries.add(ModItems.MANA_APPLE);
                        entries.add(ModItems.GREATER_MANA_APPLE);
                    })
                    .build()
    );

    public static void register() {
        // triggers static init
    }
}
