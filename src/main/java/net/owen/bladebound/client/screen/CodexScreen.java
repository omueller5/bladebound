package net.owen.bladebound.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CodexScreen extends Screen {

    private static final Identifier BG = Identifier.of("bladebound", "textures/gui/codex.png");
    private static final int BG_W = 570;
    private static final int BG_H = 420;

    // Your front-page icon: assets/bladebound/textures/icon.png
    private static final Identifier FRONT_ICON = Identifier.of("bladebound", "icon.png");

    // Layout (book-space coords)
    private static final int PAGE_MARGIN_X = 28;
    private static final int PAGE_TOP_Y = 28;
    private static final int PAGE_BOTTOM_PAD = 44; // keep away from nav buttons/bottom edge
    private static final int LINE_H = 11;

    // Left page item layout
    private static final int LEFT_IMAGE_TOP_Y = 90;
    private static final int LEFT_ITEM_BOX = 160;
    private static final int LEFT_NAME_GAP = 10;

    // Right page text area top
    private static final int RIGHT_TEXT_TOP = 60;

    // Center body text too (you wanted everything centered)
    private static final boolean CENTER_BODY_TEXT = true;

    // Front page icon size (book-space)
    private static final int FRONT_ICON_SIZE = 44;
    private static final int FRONT_ICON_GAP = 10;

    // Nav buttons (book-space)
    private static final int BTN_W = 70;
    private static final int BTN_H = 20;
    private static final int BTN_Y = BG_H - 28;
    private static final int BTN_PAD_X = 18;

    private int spreadIndex = 0;
    private final List<Spread> spreads = buildSpreads();

    // Computed each render (screen-space)
    private float bookScale = 1.0f;
    private int bookX = 0;
    private int bookY = 0;

    public CodexScreen() {
        super(Text.literal("Bladebound Codex"));
    }

    // Keep blur disabled for this screen
    @Override
    protected void applyBlur(float delta) { }
    @Override
    public void blur() { }

    @Override
    protected void init() {
        // We draw our own scaled buttons; no widgets needed
    }

    private void goSpread(int delta) {
        int n = spreadIndex + delta;
        if (n < 0) n = 0;
        if (n > spreads.size() - 1) n = spreads.size() - 1;
        spreadIndex = n;
    }

    private boolean canPrev() { return spreadIndex > 0; }
    private boolean canNext() { return spreadIndex < spreads.size() - 1; }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 263) { if (canPrev()) goSpread(-1); return true; } // left arrow
        if (keyCode == 262) { if (canNext()) goSpread(1);  return true; } // right arrow
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0) { if (canNext()) goSpread(1); }
        else if (verticalAmount > 0) { if (canPrev()) goSpread(-1); }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double bx = toBookX(mouseX);
        double by = toBookY(mouseY);

        if (bx < 0 || by < 0 || bx > BG_W || by > BG_H) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        // Prev
        int prevX = BTN_PAD_X;
        int prevY = BTN_Y;
        if (bx >= prevX && bx <= prevX + BTN_W && by >= prevY && by <= prevY + BTN_H) {
            if (canPrev()) goSpread(-1);
            return true;
        }

        // Next
        int nextX = BG_W - BTN_PAD_X - BTN_W;
        int nextY = BTN_Y;
        if (bx >= nextX && bx <= nextX + BTN_W && by >= nextY && by <= nextY + BTN_H) {
            if (canNext()) goSpread(1);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        computeBookTransform();

        double bx = toBookX(mouseX);
        double by = toBookY(mouseY);

        context.getMatrices().push();
        context.getMatrices().translate(bookX, bookY, 0);
        context.getMatrices().scale(bookScale, bookScale, 1.0f);

        context.drawTexture(BG, 0, 0, 0, 0, BG_W, BG_H, BG_W, BG_H);

        Spread spread = spreads.get(spreadIndex);

        Text pageNum = Text.literal((spreadIndex + 1) + " / " + spreads.size());
        context.drawText(this.textRenderer, pageNum,
                BG_W - 10 - this.textRenderer.getWidth(pageNum), 10,
                0xFFE6D5B8, false);

        drawLeftPage(context, spread);
        drawRightPageText(context, spread);
        drawNavButtons(context, bx, by);

        context.getMatrices().pop();

        super.render(context, mouseX, mouseY, delta);
    }

    private void computeBookTransform() {
        int sw = this.width;
        int sh = this.height;

        float maxW = sw - 20.0f;
        float maxH = sh - 20.0f;

        float sW = maxW / BG_W;
        float sH = maxH / BG_H;

        bookScale = Math.min(1.0f, Math.min(sW, sH));

        int drawW = Math.round(BG_W * bookScale);
        int drawH = Math.round(BG_H * bookScale);

        bookX = (sw - drawW) / 2;
        bookY = (sh - drawH) / 2;
    }

    private double toBookX(double screenX) {
        return (screenX - bookX) / bookScale;
    }

    private double toBookY(double screenY) {
        return (screenY - bookY) / bookScale;
    }

    private void drawNavButtons(DrawContext ctx, double bx, double by) {
        // Prev
        int prevX = BTN_PAD_X;
        int prevY = BTN_Y;
        boolean prevHover = bx >= prevX && bx <= prevX + BTN_W && by >= prevY && by <= prevY + BTN_H;

        int prevBg = (!canPrev()) ? 0x55222222 : (prevHover ? 0xAA3A2B1F : 0xAA2B1E14);
        ctx.fill(prevX, prevY, prevX + BTN_W, prevY + BTN_H, prevBg);

        Text prevText = Text.literal("< Prev");
        int ptx = prevX + (BTN_W - this.textRenderer.getWidth(prevText)) / 2;
        int pty = prevY + (BTN_H - this.textRenderer.fontHeight) / 2;
        int prevColor = (!canPrev()) ? 0xFF888888 : 0xFFE6D5B8;
        ctx.drawText(this.textRenderer, prevText, ptx, pty, prevColor, false);

        // Next
        int nextX = BG_W - BTN_PAD_X - BTN_W;
        int nextY = BTN_Y;
        boolean nextHover = bx >= nextX && bx <= nextX + BTN_W && by >= nextY && by <= nextY + BTN_H;

        int nextBg = (!canNext()) ? 0x55222222 : (nextHover ? 0xAA3A2B1F : 0xAA2B1E14);
        ctx.fill(nextX, nextY, nextX + BTN_W, nextY + BTN_H, nextBg);

        Text nextText = Text.literal("Next >");
        int ntx = nextX + (BTN_W - this.textRenderer.getWidth(nextText)) / 2;
        int nty = nextY + (BTN_H - this.textRenderer.fontHeight) / 2;
        int nextColor = (!canNext()) ? 0xFF888888 : 0xFFE6D5B8;
        ctx.drawText(this.textRenderer, nextText, ntx, nty, nextColor, false);
    }

    private void drawLeftPage(DrawContext context, Spread spread) {
        int spineX = (BG_W / 2);

        int leftX0 = PAGE_MARGIN_X;
        int leftX1 = spineX - PAGE_MARGIN_X;
        int leftW = leftX1 - leftX0;

        // INTRO: centered + icon.png
        if (spread.isIntro) {
            List<Line> lines = new ArrayList<>();

            if (spread.leftTitle != null && !spread.leftTitle.isBlank()) {
                addWrapped(lines, Text.literal(spread.leftTitle).formatted(Formatting.BOLD), leftW);
                lines.add(Line.blank());
            }

            if (spread.leftSubtitle != null && !spread.leftSubtitle.isBlank()) {
                String[] parts = spread.leftSubtitle.split("\n", -1);
                for (String p : parts) {
                    if (p.isBlank()) lines.add(Line.blank());
                    else addWrapped(lines, Text.literal(p), leftW);
                }
            }

            int pageTop = PAGE_TOP_Y;
            int pageBottom = BG_H - PAGE_BOTTOM_PAD;

            int iconH = FRONT_ICON_SIZE + FRONT_ICON_GAP;
            int contentH = lines.size() * LINE_H;
            int totalH = iconH + contentH;

            int startY = pageTop + Math.max(0, (pageBottom - pageTop - totalH) / 2);

            int iconX = leftX0 + (leftW - FRONT_ICON_SIZE) / 2;
            int iconY = startY;

            context.drawTexture(FRONT_ICON, iconX, iconY, 0, 0,
                    FRONT_ICON_SIZE, FRONT_ICON_SIZE,
                    FRONT_ICON_SIZE, FRONT_ICON_SIZE);

            int y = startY + FRONT_ICON_SIZE + FRONT_ICON_GAP;
            for (Line line : lines) {
                if (line.blank) {
                    y += LINE_H;
                    continue;
                }
                int lineX = leftX0 + (leftW - this.textRenderer.getWidth(line.text)) / 2;
                context.drawText(this.textRenderer, line.text, lineX, y, 0xFF2B1E14, false);
                y += LINE_H;
            }
            return;
        }

        // Normal item spread: original left-side layout (unchanged)
        if (spread.leftItem != null && !spread.leftItem.isEmpty()) {
            int boxX = leftX0 + (leftW - LEFT_ITEM_BOX) / 2;
            int boxY = LEFT_IMAGE_TOP_Y;

            drawItemInBox(context, boxX, boxY, LEFT_ITEM_BOX, spread.leftItem);

            Text name = coloredName(spread.displayName, spread.rarity);
            int nameW = this.textRenderer.getWidth(name);
            int nameX = leftX0 + (leftW - nameW) / 2;
            int nameY = boxY + LEFT_ITEM_BOX + LEFT_NAME_GAP;

            context.drawText(this.textRenderer, name, nameX, nameY, 0xFF2B1E14, false);
        }
    }

    private void drawRightPageText(DrawContext context, Spread spread) {
        int spineX = (BG_W / 2);

        int rightX0 = spineX + PAGE_MARGIN_X;
        int rightX1 = BG_W - PAGE_MARGIN_X;
        int rightW = rightX1 - rightX0;

        List<Line> lines = new ArrayList<>();

        if (spread.rightTitle != null && !spread.rightTitle.isBlank()) {
            Text title = coloredName(spread.rightTitle, spread.rarity).copy().formatted(Formatting.BOLD);
            addWrapped(lines, title, rightW);
            lines.add(Line.blank());
        }

        List<Text> styled = styleBullets(spread.rightBody == null ? "" : spread.rightBody);
        for (Text t : styled) {
            if (t.getString().isBlank()) {
                lines.add(Line.blank());
            } else {
                addWrapped(lines, t, rightW);
            }
        }

        int pageTop = RIGHT_TEXT_TOP;
        int pageBottom = BG_H - PAGE_BOTTOM_PAD;
        int contentH = lines.size() * LINE_H;

        int y = pageTop + Math.max(0, (pageBottom - pageTop - contentH) / 2);

        for (Line line : lines) {
            if (y > pageBottom) return;

            if (line.blank) {
                y += LINE_H;
                continue;
            }

            int x = rightX0;
            if (CENTER_BODY_TEXT) {
                x = rightX0 + (rightW - this.textRenderer.getWidth(line.text)) / 2;
            }

            context.drawText(this.textRenderer, line.text, x, y, 0xFF2B1E14, false);
            y += LINE_H;
        }
    }

    private void addWrapped(List<Line> out, Text text, int maxWidth) {
        for (OrderedText ot : this.textRenderer.wrapLines(text, maxWidth)) {
            out.add(new Line(ot, false));
        }
    }

    private List<Text> styleBullets(String raw) {
        List<Text> out = new ArrayList<>();
        String[] lines = raw.split("\n", -1);

        for (String line : lines) {
            if (line.isEmpty()) {
                out.add(Text.literal(""));
                continue;
            }

            if (line.startsWith("• ") && line.contains(":")) {
                int colon = line.indexOf(':');
                String head = line.substring(2, colon).trim();
                String tail = line.substring(colon + 1).trim();

                MutableText t = Text.literal("• ")
                        .append(Text.literal(head + ": ").formatted(Formatting.BOLD))
                        .append(Text.literal(tail));
                out.add(t);
            } else {
                out.add(Text.literal(line));
            }
        }
        return out;
    }

    private Text coloredName(String name, Rarity rarity) {
        if (name == null) return Text.literal("");
        return switch (rarity) {
            case RARE -> Text.literal(name).formatted(Formatting.DARK_AQUA);
            case LEGENDARY -> Text.literal(name).formatted(Formatting.DARK_PURPLE);
            default -> Text.literal(name);
        };
    }

    private void drawItemInBox(DrawContext context, int x, int y, int boxSize, ItemStack stack) {
        float scale = boxSize / 16.0f;

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 200);
        context.getMatrices().scale(scale, scale, scale);

        context.drawItem(stack, 0, 0);
        context.drawItemInSlot(this.textRenderer, stack, 0, 0);

        context.getMatrices().pop();
    }

    private static ItemStack stack(String id) {
        Identifier ident = Identifier.of(id);
        Item item = Registries.ITEM.get(ident);
        return new ItemStack(item);
    }

    private List<Spread> buildSpreads() {
        List<Spread> s = new ArrayList<>();

        s.add(Spread.intro(
                "Bladebound",
                "A codex of cursed blades and legendary weapons.",
                "What is Bladebound?",
                "Bladebound adds iconic swords and artifacts, each with unique effects and progression.\n" +
                        "Use this codex to learn what each item does, how to obtain it, and the rules that keep it balanced."
        ));

        // Mana Apple
        s.add(Spread.item(
                stack("bladebound:mana_apple"),
                "Mana Apple",
                "A strange apple that expands your mana pool.\n" +
                        "• Effect: Permanently increases max mana by +50\n" +
                        "• How to use: Eat it (always edible)\n" +
                        "• Where to find: Stronghold Library chests or Excalibur Church chests\n" +
                        "• Notes: Glows like a special artifact, but it is not a golden apple",
                "Mana Apple",
                Rarity.RARE
        ));

        // Greater Mana Apple
        s.add(Spread.item(
                stack("bladebound:greater_mana_apple"),
                "Greater Mana Apple",
                "A stronger variant infused with deeper magic.\n" +
                        "• Effect: Permanently increases max mana by +100\n" +
                        "• How to use: Eat it (always edible)\n" +
                        "• How to obtain: Trades from Expert Farmers or Journeyman Clerics\n" +
                        "• Notes: Intended for later progression than the Mana Apple",
                "Greater Mana Apple",
                Rarity.LEGENDARY
        ));

        // Cursed Kitetsu Shard
        s.add(Spread.item(
                stack("bladebound:cursed-kitetsu-shard"),
                "Cursed Kitetsu Shard",
                "A concentrated fragment of malice.\n" +
                        "• What it is: The shard which is used for Sandai Kitetsu\n" +
                        "• How to get it: Kill Wither Skeletons to obtain\n" +
                        "• What it’s used for: Craft Sandai Kitetsu (mix with an iron sword and a string in a crafting table)",
                "Cursed Kitetsu Shard",
                Rarity.RARE
        ));

        // Murasame Gauntlets
        s.add(Spread.item(
                stack("bladebound:murasame-gauntlets"),
                "Gauntlets",
                "A protective trinket that resists corruption.\n" +
                        "• Effect: Grants immunity to Murasame's curse\n" +
                        "• How to obtain: Craft using 2 iron ingots\n" +
                        "• Notes: These must be used along with Murasame",
                "Murasame Gauntlets",
                Rarity.RARE
        ));

        // Cooldown Bracelet
        s.add(Spread.item(
                stack("bladebound:cooldown_bracelet"),
                "Bracelet",
                "A magical band that allows you to cast faster.\n" +
                        "• Effect: Reduces spell cooldowns\n" +
                        "• Where to find: Stronghold chests\n" +
                        "• How to obtain: Trades from Journeyman Librarians\n" +
                        "• Notes: Used alongside magical staves",
                "Cooldown Bracelet",
                Rarity.RARE
        ));

        // Grimoires (single page, future-proof)
        s.add(Spread.item(
                stack("bladebound:zoltraak_spell"),
                "Grimoires",
                "Magic in Bladebound is learned from grimoires and cast with staves.\n" +
                        "• How to switch spells: Press R (default) while holding a staff\n" +
                        "• Casting: Uses mana and respects cooldowns\n" +
                        "\n" +
                        "Legendary spells are found only in:\n" +
                        "• Ancient City chests\n" +
                        "\n" +
                        "More grimoires will be added in future updates.",
                "Grimoires",
                Rarity.LEGENDARY
        ));

        // Sandai Kitetsu
        s.add(Spread.item(
                stack("bladebound:sandai-kitetsu"),
                "Sandai Kitetsu",
                "A cursed blade that demands blood.\n" +
                        "• How to obtain: Can only be obtained through crafting\n" +
                        "• Balance notes: An early game sword on the same level as iron",
                "Sandai Kitetsu",
                Rarity.RARE
        ));

        // Wado Ichimonji
        s.add(Spread.item(
                stack("bladebound:wado-ichimonji"),
                "Wado Ichimonji",
                "A disciplined blade that rewards mastery.\n" +
                        "• Form Mastery: Damage increases per mastery level\n" +
                        "• Additional rules: Mastery decreases when being held but not used; does not decrease when not holding\n" +
                        "• Found at: Wado Dojo\n" +
                        "• Biomes: Cherry Grove, Plains, Bamboo Jungle\n" +
                        "• Notes: A sword on the same level as diamond",
                "Wado Ichimonji",
                Rarity.RARE
        ));

        // Murasame
        s.add(Spread.item(
                stack("bladebound:murasame"),
                "Murasame",
                "A lethal curse — once cut, death is guaranteed.\n" +
                        "• Delayed lethality (varies by target strength)\n" +
                        "• Cannot affect: Wither / Dragon / Warden\n" +
                        "• Found at: Sword shrine\n" +
                        "• Biomes: Dark Forest, Cherry Grove\n" +
                        "• Notes: A sword on the same level as netherite",
                "Murasame",
                Rarity.LEGENDARY
        ));

        // Excalibur
        s.add(Spread.item(
                stack("bladebound:excalibur"),
                "Excalibur",
                "A legendary holy blade of light.\n" +
                        "• Core effect: Deals extra damage to undead mobs\n" +
                        "• Found at: Church\n" +
                        "• Biomes: Plains, Meadow, Birch Forest\n" +
                        "• Notes: A sword on the same level as netherite",
                "Excalibur",
                Rarity.LEGENDARY
        ));

        // Frieren Staff
        s.add(Spread.item(
                stack("bladebound:frieren-staff"),
                "Frieren's Staff",
                "A mage's staff designed for spellcasting.\n" +
                        "• What it is: A staff that enables casting spells from the spell wheel\n" +
                        "• How it works: Uses mana and respects cooldowns\n" +
                        "• Found at: Mage Tower\n" +
                        "• Biomes: Forest, Birch Forest, Flower Forest\n" +
                        "• Notes: Press R (default) to switch spells while holding a staff",
                "Frieren's Staff",
                Rarity.LEGENDARY
        ));

        return s;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private enum Rarity { NORMAL, RARE, LEGENDARY }

    private static final class Line {
        final OrderedText text;
        final boolean blank;

        Line(OrderedText text, boolean blank) {
            this.text = text;
            this.blank = blank;
        }

        static Line blank() {
            return new Line(Text.literal("").asOrderedText(), true);
        }
    }

    private static final class Spread {
        final String leftTitle;
        final String leftSubtitle;
        final ItemStack leftItem;

        final String displayName;
        final Rarity rarity;

        final String rightTitle;
        final String rightBody;

        final boolean isIntro;

        private Spread(String leftTitle, String leftSubtitle, ItemStack leftItem,
                       String displayName, Rarity rarity,
                       String rightTitle, String rightBody,
                       boolean isIntro) {
            this.leftTitle = leftTitle;
            this.leftSubtitle = leftSubtitle;
            this.leftItem = leftItem;
            this.displayName = displayName;
            this.rarity = rarity;
            this.rightTitle = rightTitle;
            this.rightBody = rightBody;
            this.isIntro = isIntro;
        }

        static Spread intro(String leftTitle, String leftSubtitle, String rightTitle, String rightBody) {
            return new Spread(leftTitle, leftSubtitle, ItemStack.EMPTY, null, Rarity.NORMAL, rightTitle, rightBody, true);
        }

        static Spread item(ItemStack leftItem, String rightTitle, String rightBody, String displayName, Rarity rarity) {
            return new Spread(null, null, leftItem, displayName, rarity, rightTitle, rightBody, false);
        }
    }
}
