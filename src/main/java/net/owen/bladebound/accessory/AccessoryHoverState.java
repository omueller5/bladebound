package net.owen.bladebound.accessory;

public final class AccessoryHoverState {
    // PlayerInventory armor slots are typically:
    // boots=36, leggings=37, chest=38, helmet=39 (Yarn)
    // We'll store whichever one is currently hovered.
    public static volatile int hoveredArmorInvIndex = -1;

    private AccessoryHoverState() {}
}
