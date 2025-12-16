package net.owen.bladebound.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

import java.util.function.BooleanSupplier;

public class ConditionalSlot extends Slot {
    private final BooleanSupplier enabled;

    public ConditionalSlot(Inventory inv, int index, int x, int y, BooleanSupplier enabled) {
        super(inv, index, x, y);
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled.getAsBoolean();
    }
}
