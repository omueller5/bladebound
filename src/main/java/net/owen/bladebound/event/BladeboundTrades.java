package net.owen.bladebound.event;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;
import net.owen.bladebound.item.ModItems;

import java.util.Optional;

public final class BladeboundTrades {

    // Uses rule: Cleric always has +2 uses compared to Farmer
    private static final int FARMER_GREATER_APPLE_USES = 5;
    private static final int CLERIC_GREATER_APPLE_USES = FARMER_GREATER_APPLE_USES + 2;

    public static void init() {

        // ---------------------------
        // Librarian (Level 5 / Master)
        // ---------------------------
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.LIBRARIAN, 5, factories ->
                factories.add((entity, random) -> new TradeOffer(
                        new TradedItem(Items.EMERALD, 48),
                        Optional.of(new TradedItem(Items.BOOK, 1)),
                        new ItemStack(ModItems.COOLDOWN_BRACELET, 1),
                        3,
                        30,
                        0.2f
                ))
        );

        // ---------------------------
        // Farmer (Level 4)
        // ---------------------------
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 4, factories ->
                factories.add((entity, random) -> new TradeOffer(
                        new TradedItem(Items.EMERALD, 18),
                        Optional.empty(),
                        new ItemStack(ModItems.GREATER_MANA_APPLE, 1),
                        FARMER_GREATER_APPLE_USES,
                        20,
                        0.2f
                ))
        );

        // ---------------------------
        // Cleric (Level 5 / Master)
        // ---------------------------
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.CLERIC, 5, factories ->
                factories.add((entity, random) -> new TradeOffer(
                        new TradedItem(Items.EMERALD, 22),
                        Optional.empty(),
                        new ItemStack(ModItems.GREATER_MANA_APPLE, 1),
                        CLERIC_GREATER_APPLE_USES, // always +2 vs farmer
                        30,
                        0.2f
                ))
        );
    }

    private BladeboundTrades() {}
}
