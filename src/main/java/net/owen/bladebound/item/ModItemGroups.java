package net.owen.bladebound.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.owen.bladebound.BladeboundBlocks;
import net.owen.bladebound.BladeboundItems;

public class ModItemGroups {

    public static final ItemGroup BLADEBOUND_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of("bladebound", "bladebound"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.EXCALIBUR))
                    .displayName(Text.translatable("itemGroup.bladebound"))
                    .entries((displayContext, entries) -> {

                        // Codex
                        entries.add(ModItems.CODEX);                 // NEW codex item
                        entries.add(ModItems.MURASAME_GAUNTLETS);

                        // Swords
                        entries.add(ModItems.SANDAIKITETSU);
                        entries.add(ModItems.WADOICHIMONJI);
                        entries.add(ModItems.MURASAME);
                        entries.add(ModItems.EXCALIBUR);

                        // Excalibur shrine blocks
                        entries.add(BladeboundBlocks.SWORD_IN_STONE);

                        // Materials
                        entries.add(BladeboundItems.CURSED_KITETSU_SHARD);
                    })
                    .build()
    );

    public static void register() {
        // static init
    }
}
