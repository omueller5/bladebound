package net.owen.bladebound.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.owen.bladebound.client.spell.ClientSpellState;
import net.owen.bladebound.magic.StaffSpell;
import net.owen.bladebound.network.ClientPackets;

import java.util.ArrayList;
import java.util.List;

public class SpellScreen extends Screen {

    private static final int UI_W = 230;

    private int left, top;
    private int uiH;

    private Tab currentTab = Tab.STARTER;
    private int scrollOffset = 0;
    private List<SpellDef> filtered = List.of();

    public SpellScreen() {
        super(Text.literal("Spells"));
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearChildren();

        List<SpellDef> all = buildAllSpellDefs();

        this.filtered = all.stream()
                .filter(s -> tabForSpell(s.spell) == currentTab)
                .toList();

        int btnW = UI_W - 24;
        int btnH = 20;
        int gap = 6;

        int topPad = 28;
        int tabsH = 22;
        int listTopPad = 10;
        int bottomPad = 32;
        int selectedLinePad = 28;

        int availableForList = this.height - (topPad + tabsH + listTopPad + selectedLinePad + bottomPad + 40);
        int rowH = btnH + gap;
        int maxVisibleRows = Math.max(3, Math.min(8, availableForList / rowH));

        int maxScroll = Math.max(0, filtered.size() - maxVisibleRows);
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        this.uiH = topPad + tabsH + listTopPad + (maxVisibleRows * rowH) + selectedLinePad + bottomPad;

        this.left = (this.width - UI_W) / 2;
        this.top = (this.height - uiH) / 2;

        int x = left + 12;
        int yTabs = top + 26;
        int yList = yTabs + tabsH + listTopPad;
        int yCast = top + uiH - 28;

        // Tabs
        int tabCount = 4;
        int tabW = (btnW - (gap * (tabCount - 1))) / tabCount;
        addDrawableChild(tabButton(Tab.STARTER, x, yTabs, tabW));
        addDrawableChild(tabButton(Tab.RARE, x + tabW + 4, yTabs, tabW));
        addDrawableChild(tabButton(Tab.LEGENDARY, x + (tabW + 4) * 2, yTabs, tabW));
        addDrawableChild(tabButton(Tab.ANCIENT, x + 3 * (tabW + 4), yTabs, tabW));

        // Scroll buttons if needed
        boolean needsScroll = filtered.size() > maxVisibleRows;
        if (needsScroll) {
            addDrawableChild(ButtonWidget.builder(Text.literal("▲"), b -> {
                        scrollOffset--;
                        rebuild();
                    })
                    .dimensions(left + UI_W - 22, yList, 10, 10)
                    .build());

            addDrawableChild(ButtonWidget.builder(Text.literal("▼"), b -> {
                        scrollOffset++;
                        rebuild();
                    })
                    .dimensions(left + UI_W - 22, yList + (maxVisibleRows * rowH) - 10, 10, 10)
                    .build());
        }

        // Spell buttons
        int start = scrollOffset;
        int end = Math.min(filtered.size(), start + maxVisibleRows);

        for (int row = 0; row < (end - start); row++) {
            SpellDef def = filtered.get(start + row);
            int by = yList + row * rowH;

            boolean learned = ClientSpellState.hasLearned(def.id);

            ButtonWidget btn = ButtonWidget.builder(def.name, b -> ClientPackets.sendSelectedSpellId(def.id))
                    .dimensions(x, by, btnW, btnH)
                    .build();

            btn.active = learned;
            addDrawableChild(btn);
        }

        // Confirm button
        addDrawableChild(ButtonWidget.builder(Text.literal("Confirm"), b -> this.close())
                .dimensions(x, yCast, btnW, 20)
                .build());
    }

    private ButtonWidget tabButton(Tab tab, int x, int y, int w) {
        boolean isActive = (tab == currentTab);
        Text shown = isActive ? tab.label().copy().formatted(Formatting.BOLD) : tab.label();

        return ButtonWidget.builder(shown, b -> {
                    currentTab = tab;
                    scrollOffset = 0;
                    rebuild();
                })
                .dimensions(x, y, w, 20)
                .build();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (filtered == null || filtered.isEmpty()) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        int delta = (verticalAmount < 0) ? 1 : -1;
        if (delta != 0) {
            scrollOffset += delta;
            rebuild();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xAA000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        context.fill(left, top, left + UI_W, top + uiH, 0xCC000000);
        context.drawTextWithShadow(textRenderer, this.title, left + 8, top + 8, 0xFFFFFF);

        Identifier selectedId = ClientSpellState.getSelectedSpellId();
        Text selectedText = Text.literal("Selected: ").append(getNameForId(selectedId));
        context.drawTextWithShadow(textRenderer, selectedText, left + 12, top + uiH - 52, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    private Text getNameForId(Identifier id) {
        if (id == null) return Text.literal("(none)");

        StaffSpell s = StaffSpell.fromId(id);
        if (s == null) return Text.literal(id.toString()).formatted(Formatting.GRAY);

        return Text.literal(s.displayName).formatted(colorForRarity(s.rarity));
    }

    // -------------------------
    // Dynamic spell list (ID-based)
    // -------------------------

    private static List<SpellDef> buildAllSpellDefs() {
        List<SpellDef> out = new ArrayList<>();
        for (StaffSpell s : StaffSpell.values()) {
            Identifier id = s.id; // if your enum uses getter, replace with s.getId()
            if (id == null) continue;
            Text name = Text.literal(s.displayName).formatted(colorForRarity(s.rarity));
            out.add(new SpellDef(id, s, name));
        }
        return out;
    }

    private static Tab tabForSpell(StaffSpell spell) {
        return switch (spell.rarity) {
            case STARTER -> Tab.STARTER;
            case RARE -> Tab.RARE;
            case LEGENDARY -> Tab.LEGENDARY;
            case ANCIENT -> Tab.ANCIENT;
        };
    }

    private static Formatting colorForRarity(StaffSpell.SpellRarity rarity) {
        return switch (rarity) {
            case STARTER -> Formatting.GREEN;
            case RARE -> Formatting.BLUE;
            case LEGENDARY -> Formatting.GOLD;
            case ANCIENT -> Formatting.RED;
        };
    }

    private enum Tab {
        STARTER, RARE, LEGENDARY, ANCIENT;

        Text label() {
            return switch (this) {
                case STARTER -> Text.literal("Start").formatted(Formatting.GREEN);
                case RARE -> Text.literal("Rare").formatted(Formatting.BLUE);
                case LEGENDARY -> Text.literal("Legend").formatted(Formatting.GOLD);
                case ANCIENT -> Text.literal("Ancient").formatted(Formatting.RED);
            };
        }
    }

    private static class SpellDef {
        final Identifier id;
        final StaffSpell spell;
        final Text name;

        SpellDef(Identifier id, StaffSpell spell, Text name) {
            this.id = id;
            this.spell = spell;
            this.name = name;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
