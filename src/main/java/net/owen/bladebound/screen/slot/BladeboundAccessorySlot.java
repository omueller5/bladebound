package net.owen.bladebound.screen.slot;

import net.minecraft.inventory.Inventory;

import java.util.function.BooleanSupplier;

public class BladeboundAccessorySlot extends ConditionalSlot {
    private final int linkedArmorIndex;

    public BladeboundAccessorySlot(Inventory inv, int index, int x, int y, int linkedArmorIndex, BooleanSupplier enabled) {
        super(inv, index, x, y, enabled);
        this.linkedArmorIndex = linkedArmorIndex;
    }

    public int bladebound$getLinkedArmorIndex() {
        return linkedArmorIndex;
    }
}
