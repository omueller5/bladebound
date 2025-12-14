package net.owen.bladebound.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public final class BladeboundCodexBook {
    private BladeboundCodexBook() {}

    public static ItemStack create() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

        RawFilteredPair<String> title = RawFilteredPair.of("Bladebound Codex");
        String author = "Bladebound";

        // Order request:
        //  - Common/Rare first
        //  - Legendary at the end
        List<RawFilteredPair<Text>> pages = new ArrayList<>();
        pages.add(RawFilteredPair.of(page1()));              // contents + rarity overview
        pages.add(RawFilteredPair.of(sandaiKitetsuPage()));  // most common / rare
        pages.add(RawFilteredPair.of(wadoPage()));           // rare
        pages.add(RawFilteredPair.of(excaliburPage()));      // legendary
        pages.add(RawFilteredPair.of(murasamePage()));       // legendary
        pages.add(RawFilteredPair.of(footerPage()));

        WrittenBookContentComponent content = new WrittenBookContentComponent(title, author, 0, pages, false);
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, content);
        return book;
    }

    // =========================================================
    // Helpers (readable on book texture; NEVER white; NEVER yellow)
    // =========================================================

    private static MutableText t(String s, Formatting color) {
        return Text.literal(s).setStyle(Style.EMPTY.withColor(color));
    }

    private static MutableText nl() {
        return Text.literal("\n");
    }

    private static MutableText section(String name) {
        // Dark + readable on book pages (no yellow)
        return t(name, Formatting.DARK_BLUE);
    }

    private static MutableText bullet(String s, Formatting color) {
        return Text.empty()
                .append(t("• ", Formatting.DARK_GRAY))
                .append(t(s, color));
    }

    // =========================================================
    // Pages
    // =========================================================

    private static Text page1() {
        return Text.empty()
                .append(t("BLADEBOUND CODEX", Formatting.DARK_AQUA)).append(nl())
                .append(t("Sword Effects & Lore", Formatting.DARK_GRAY)).append(nl())
                .append(nl())
                .append(t("A record of blades and what they grant their wielder.", Formatting.GRAY)).append(nl())
                .append(nl())
                .append(section("Contents")).append(nl())
                .append(bullet("Sandai Kitetsu", Formatting.DARK_PURPLE)).append(nl())
                .append(bullet("Wado Ichimonji", Formatting.LIGHT_PURPLE)).append(nl())
                .append(bullet("Excalibur", Formatting.AQUA)).append(nl())
                .append(bullet("Murasame", Formatting.RED)).append(nl())
                .append(nl())
                .append(section("Rarity")).append(nl())
                .append(bullet("Rare (Most Common): Sandai Kitetsu", Formatting.DARK_PURPLE)).append(nl())
                .append(bullet("Rare: Wado Ichimonji", Formatting.DARK_PURPLE)).append(nl())
                .append(bullet("Legendary: Excalibur, Murasame", Formatting.DARK_AQUA)).append(nl());
    }

    private static Text sandaiKitetsuPage() {
        return Text.empty()
                .append(t("SANDAI KITETSU", Formatting.DARK_PURPLE)).append(nl())
                .append(t("Rare Sword", Formatting.DARK_GRAY)).append(nl())
                .append(nl())
                .append(section("Nature")).append(nl())
                .append(bullet("A blade born of a cursed lineage.", Formatting.BLACK)).append(nl())
                .append(bullet("Unforgiving to those who wield it carelessly.", Formatting.BLACK)).append(nl())
                .append(nl())
                .append(section("Reputation")).append(nl())
                .append(t("The Kitetsu blades are said to bring misfortune", Formatting.GRAY)).append(nl())
                .append(t("to those who underestimate their will.", Formatting.GRAY)).append(nl())
                .append(nl())
                .append(section("Notes")).append(nl())
                .append(t("This is the third generation of the Kitetsu line.", Formatting.GRAY));
    }

    private static Text wadoPage() {
        return Text.empty()
                .append(t("WADO ICHIMONJI", Formatting.LIGHT_PURPLE)).append(nl())
                .append(t("Rare Sword", Formatting.DARK_PURPLE)).append(nl())
                .append(nl())
                .append(section("Effects")).append(nl())
                .append(bullet("Damage increases as you level it up.", Formatting.BLACK)).append(nl())
                .append(bullet("Damage increases further as you build Discipline.", Formatting.BLACK)).append(nl())
                .append(nl())
                .append(section("Form Mastery")).append(nl())
                .append(t("Wado gains additional damage boosts", Formatting.GRAY)).append(nl())
                .append(t("for each mastery level of the form.", Formatting.GRAY));
    }

    // Legendary swords moved toward the end (per your request)

    private static Text excaliburPage() {
        return Text.empty()
                .append(t("EXCALIBUR", Formatting.AQUA)).append(nl())
                .append(t("Legendary Sword", Formatting.DARK_AQUA)).append(nl())
                .append(nl())
                .append(section("Effects")).append(nl())
                .append(bullet("Blessed: Deals bonus damage to undead.", Formatting.BLACK)).append(nl())
                .append(nl())
                .append(section("Lore")).append(nl())
                .append(t("A holy blade that answers only the worthy.", Formatting.GRAY)).append(nl())
                .append(t("Its light cuts through shadow and doubt.", Formatting.GRAY));
    }

    private static Text murasamePage() {
        return Text.empty()
                .append(t("MURASAME", Formatting.RED)).append(nl())
                .append(t("Legendary Sword", Formatting.DARK_RED)).append(nl())
                .append(nl())
                .append(section("Effects")).append(nl())
                .append(bullet("On hit: inflicts Poison (4s).", Formatting.BLACK)).append(nl())
                .append(bullet("On hit: inflicts Wither (3s).", Formatting.BLACK)).append(nl())
                .append(nl())
                .append(section("Lore")).append(nl())
                .append(t("A cursed blade that demands a price.", Formatting.GRAY));
    }

    private static Text footerPage() {
        return Text.empty()
                .append(t("HANDLE WITH CARE", Formatting.DARK_AQUA)).append(nl())
                .append(t("Some blades change the wielder.", Formatting.GRAY)).append(nl())
                .append(nl())
                .append(section("Tip")).append(nl())
                .append(bullet("Keep this codex close to remember each sword’s power.", Formatting.BLACK)).append(nl());
    }
}
