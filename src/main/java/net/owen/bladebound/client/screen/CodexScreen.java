package net.owen.bladebound.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
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

    // Layout tuning (spine anchored)
    private static final int PAGE_MARGIN_X = 28;     // padding from page edges and from spine
    private static final int PAGE_TOP_Y = 28;
    private static final int LINE_H = 11;

    // Left page: item image + name under it
    private static final int LEFT_IMAGE_TOP_Y = 90;
    private static final int LEFT_ITEM_BOX = 160;   // bigger image area
    private static final int LEFT_NAME_GAP = 10;

    // Right page
    private static final int RIGHT_TEXT_Y = 60;

    // Center body text too (set false if you only want centered titles)
    private static final boolean CENTER_BODY_TEXT = true;

    private int spreadIndex = 0;

    private ButtonWidget prevBtn;
    private ButtonWidget nextBtn;

    private final List<Spread> spreads = buildSpreads();

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
        int x = (this.width - BG_W) / 2;
        int y = (this.height - BG_H) / 2;

        prevBtn = ButtonWidget.builder(Text.literal("< Prev"), b -> goSpread(-1))
                .dimensions(x + 18, y + BG_H - 28, 70, 20)
                .build();

        nextBtn = ButtonWidget.builder(Text.literal("Next >"), b -> goSpread(1))
                .dimensions(x + BG_W - 18 - 70, y + BG_H - 28, 70, 20)
                .build();

        addDrawableChild(prevBtn);
        addDrawableChild(nextBtn);

        updateButtons();
    }

    private void goSpread(int delta) {
        int n = spreadIndex + delta;
        if (n < 0) n = 0;
        if (n > spreads.size() - 1) n = spreads.size() - 1;
        spreadIndex = n;
        updateButtons();
    }

    private void updateButtons() {
        if (prevBtn != null) prevBtn.active = spreadIndex > 0;
        if (nextBtn != null) nextBtn.active = spreadIndex < spreads.size() - 1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 263) { goSpread(-1); return true; } // left arrow
        if (keyCode == 262) { goSpread(1);  return true; } // right arrow
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0) goSpread(1);
        else if (verticalAmount > 0) goSpread(-1);
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        int x = (this.width - BG_W) / 2;
        int y = (this.height - BG_H) / 2;

        context.drawTexture(BG, x, y, 0, 0, BG_W, BG_H, BG_W, BG_H);

        Spread spread = spreads.get(spreadIndex);

        Text pageNum = Text.literal((spreadIndex + 1) + " / " + spreads.size());
        context.drawText(this.textRenderer, pageNum,
                x + BG_W - 10 - this.textRenderer.getWidth(pageNum), y + 10,
                0xFFE6D5B8, false);

        drawLeftPage(context, x, y, spread);
        drawRightPageText(context, x, y, spread);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawLeftPage(DrawContext context, int x, int y, Spread spread) {
        int spineX = x + (BG_W / 2);

        int leftX0 = x + PAGE_MARGIN_X;
        int leftX1 = spineX - PAGE_MARGIN_X;
        int leftW = leftX1 - leftX0;

        if (spread.leftItem != null && !spread.leftItem.isEmpty()) {
            // Center the item inside a big square box
            int boxX = leftX0 + (leftW - LEFT_ITEM_BOX) / 2;
            int boxY = y + LEFT_IMAGE_TOP_Y;

            drawItemInBox(context, boxX, boxY, LEFT_ITEM_BOX, spread.leftItem);

            // Name under image (already centered)
            Text name = coloredName(spread.displayName, spread.rarity);
            int nameW = this.textRenderer.getWidth(name);
            int nameX = leftX0 + (leftW - nameW) / 2;
            int nameY = boxY + LEFT_ITEM_BOX + LEFT_NAME_GAP;

            context.drawText(this.textRenderer, name, nameX, nameY, 0xFF2B1E14, false);

        } else {
            // Intro left-page title/subtitle (CENTERED)
            int ty = y + PAGE_TOP_Y;

            Text title = Text.literal(spread.leftTitle == null ? "" : spread.leftTitle).formatted(Formatting.BOLD);
            int titleX = leftX0 + (leftW - this.textRenderer.getWidth(title)) / 2;
            context.drawText(this.textRenderer, title, titleX, ty, 0xFF2B1E14, false);

            if (spread.leftSubtitle != null && !spread.leftSubtitle.isBlank()) {
                int maxWidth = leftW;
                List<OrderedText> lines = wrap(Text.literal(spread.leftSubtitle), maxWidth);
                int ly = ty + 18;
                for (OrderedText line : lines) {
                    int lineX = leftX0 + (leftW - this.textRenderer.getWidth(line)) / 2;
                    context.drawText(this.textRenderer, line, lineX, ly, 0xFF2B1E14, false);
                    ly += LINE_H;
                }
            }
        }
    }

    private void drawRightPageText(DrawContext context, int x, int y, Spread spread) {
        int spineX = x + (BG_W / 2);

        int rightX0 = spineX + PAGE_MARGIN_X;
        int rightX1 = x + BG_W - PAGE_MARGIN_X;
        int rightW = rightX1 - rightX0;

        int startY = y + PAGE_TOP_Y;

        // Title (CENTERED, colored rarity for swords)
        if (spread.rightTitle != null && !spread.rightTitle.isBlank()) {
            Text title = coloredName(spread.rightTitle, spread.rarity).copy().formatted(Formatting.BOLD);
            int titleX = rightX0 + (rightW - this.textRenderer.getWidth(title)) / 2;
            context.drawText(this.textRenderer, title, titleX, startY, 0xFF2B1E14, false);
            startY += 20;
        }

        // Body: keep your text, but bold bullet headings
        List<Text> styledLines = styleBullets(spread.rightBody == null ? "" : spread.rightBody);

        int drawY = y + RIGHT_TEXT_Y;
        int maxY = y + BG_H - 44;

        for (Text logical : styledLines) {
            if (logical.getString().isBlank()) {
                drawY += LINE_H;
                continue;
            }

            List<OrderedText> wrapped = wrap(logical, rightW);
            for (OrderedText line : wrapped) {
                if (drawY > maxY) return;

                int lineX = rightX0;
                if (CENTER_BODY_TEXT) {
                    lineX = rightX0 + (rightW - this.textRenderer.getWidth(line)) / 2;
                }

                context.drawText(this.textRenderer, line, lineX, drawY, 0xFF2B1E14, false);
                drawY += LINE_H;
            }
        }
    }

    private List<OrderedText> wrap(Text text, int maxWidth) {
        return this.textRenderer.wrapLines(text, maxWidth);
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
            case RARE -> Text.literal(name).formatted(Formatting.AQUA);
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
                "Bladebound adds 4 iconic swords and artifacts, each with unique effects and progression.\n" +
                        "Use this codex to learn what each item does, how to obtain it, and the rules that keep it balanced."
        ));

        s.add(Spread.item(
                stack("bladebound:cursed-kitetsu-shard"),
                "Cursed Kitetsu Shard",
                "A concentrated fragment of malice. \n" +
                        "• What it is: The shard which is used for Sandai Kitetsu\n" +
                        "• How to get it: Kill Wither Skeletons to obtain\n" +
                        "• What it’s used for: Crafting Sandai Kitetsu, mix with an iron sword and a string in a crafting table to make the sword",
                "Cursed Kitetsu Shard",
                Rarity.RARE
        ));

        s.add(Spread.item(
                stack("bladebound:murasame-gauntlets"),
                "Gauntlets",
                "A protective trinket that resists corruption.\n" +
                        "• Slot: Trinkets (hands)\n" +
                        "• Effect: Grants immunity to Murasames curse\n" +
                        "• How to obtain: Craft using 2 iron ingots\n" +
                        "• Notes: These must be used along with Murasame",
                "Murasame Gauntlets",
                Rarity.RARE
        ));

        s.add(Spread.item(
                stack("bladebound:sandai-kitetsu"),
                "Sandai Kitetsu",
                "A cursed blade that demands blood.\n" +
                        "• How to obtain: Can only be obtained through crafting\n" +
                        "• Balance notes: An early game sword on the same level as iron",
                "Sandai Kitetsu",
                Rarity.RARE
        ));

        s.add(Spread.item(
                stack("bladebound:wado-ichimonji"),
                "Wado Ichimonji",
                "A disciplined blade that rewards mastery.\n" +
                        "• Form Mastery: damage increases per mastery level\n" +
                        "• Additional rules: Mastery decreases when being held but not used, does not decrease when you are not holding\n" +
                        "• How to obtain: Find the shrine spawned randomly throughout the world, spawns in Cherry Groves, Plains and Bamboo Jungle\n" +
                        "• Notes: A sword on the same level as diamond",
                "Wado Ichimonji",
                Rarity.RARE
        ));

        s.add(Spread.item(
                stack("bladebound:murasame"),
                "Murasame",
                "A lethal curse — once cut, death is guaranteed.\n" +
                        "• Delayed lethality (varies by target strength)\n" +
                        "• Cannot Effect: Wither / Dragon / Warden\n" +
                        "• How to obtain: Can be found at the execution site in the biomes, Dark Forest, Windswept Hills and Badlands \n" +
                        "• Notes: A sword on the same level as netherite",
                "Murasame",
                Rarity.LEGENDARY
        ));

        s.add(Spread.item(
                stack("bladebound:excalibur"),
                "Excalibur",
                "A legendary holy blade of light.\n" +
                        "• Core effect: Deals extra damage to undead mobs\n" +
                        "• How to obtain: Can be found in the biomes, Plains, Meadows, Forest and Birch Forest\n" +
                        "• Notes: A sword on the same level as netherite",
                "Excalibur",
                Rarity.LEGENDARY
        ));

        return s;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private enum Rarity { NORMAL, RARE, LEGENDARY }

    private static final class Spread {
        final String leftTitle;
        final String leftSubtitle;
        final ItemStack leftItem;

        final String displayName;
        final Rarity rarity;

        final String rightTitle;
        final String rightBody;

        private Spread(String leftTitle, String leftSubtitle, ItemStack leftItem,
                       String displayName, Rarity rarity,
                       String rightTitle, String rightBody) {
            this.leftTitle = leftTitle;
            this.leftSubtitle = leftSubtitle;
            this.leftItem = leftItem;
            this.displayName = displayName;
            this.rarity = rarity;
            this.rightTitle = rightTitle;
            this.rightBody = rightBody;
        }

        static Spread intro(String leftTitle, String leftSubtitle, String rightTitle, String rightBody) {
            return new Spread(leftTitle, leftSubtitle, ItemStack.EMPTY, null, Rarity.NORMAL, rightTitle, rightBody);
        }

        static Spread item(ItemStack leftItem, String rightTitle, String rightBody, String displayName, Rarity rarity) {
            return new Spread(null, null, leftItem, displayName, rarity, rightTitle, rightBody);
        }
    }
}
