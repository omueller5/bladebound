package net.owen.bladebound.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class CodexItem extends Item {

    public CodexItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack codexStack = user.getStackInHand(hand);

        // Reading a written book is server-initiated; do it only on the server. :contentReference[oaicite:1]{index=1}
        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            ItemStack book = buildBook();
            serverPlayer.useBook(book, hand); // opens the GUI for the player :contentReference[oaicite:2]{index=2}
        }

        return TypedActionResult.success(codexStack, world.isClient());
    }

    private static ItemStack buildBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

        // Title + pages are stored in WrittenBookContentComponent. :contentReference[oaicite:3]{index=3}
        RawFilteredPair<String> title = RawFilteredPair.of("Bladebound Codex");
        String author = "Bladebound";
        int generation = 0;

        List<RawFilteredPair<Text>> pages = List.of(
                RawFilteredPair.of(Text.literal(
                        "§lBladebound Codex§r\n\n" +
                                "This book contains info on the legendary swords:\n" +
                                "• Excalibur\n" +
                                "• Wado Ichimonji\n" +
                                "• Murasame\n\n" +
                                "Tip: Keep sword tooltips short—use this book for details."
                )),
                RawFilteredPair.of(Text.literal(
                        "§lExcalibur§r\n\n" +
                                "Origin:\nA sword bound to stone.\n\n" +
                                "Where to find:\n(put your chosen biomes here)\n\n" +
                                "How to claim:\nInteract with the sword-in-stone site.\n\n" +
                                "Effects:\n(list your actual effects here)"
                )),
                RawFilteredPair.of(Text.literal(
                        "§lWado Ichimonji§r\n\n" +
                                "Where to find:\n(put biome list here)\n\n" +
                                "How to obtain:\n(ritual / structure / mob / shrine)\n\n" +
                                "Effects:\n(list effects)\n\n" +
                                "Notes:\nKeep the sword’s item lore short."
                )),
                RawFilteredPair.of(Text.literal(
                        "§lMurasame§r\n\n" +
                                "Where to find:\n(put biome list here)\n\n" +
                                "How to obtain:\n(ritual / structure / execution site)\n\n" +
                                "Effects:\n(list effects)"
                ))
        );

        // resolved=true is fine for plain Text pages; you can also keep it false if you later add selectors/nbt text.
      //  WrittenBookContentComponent content =
        //        new WrittenBookContentComponent(title, author, generation, pages, true); :contentReference[oaicite:4]{index=4}

        // Attach the component to the book item stack. :contentReference[oaicite:5]{index=5}
        //book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, content);

        return book;
    }
}
