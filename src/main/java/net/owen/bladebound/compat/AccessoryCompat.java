package net.owen.bladebound.compat;

import net.fabricmc.loader.api.FabricLoader;

public final class AccessoryCompat {

    public static final boolean HAS_TRINKETS =
            FabricLoader.getInstance().isModLoaded("trinkets");

    private static AccessoryApi api;

    public static void init() {
        api = HAS_TRINKETS ? loadTrinketsApiOrFallback() : new BuiltInAccessoryApi();
        api.init();
    }

    public static AccessoryApi api() {
        return api;
    }

    private static AccessoryApi loadTrinketsApiOrFallback() {
        try {
            // IMPORTANT: this class name must match your actual package + class
            Class<?> clazz = Class.forName("net.owen.bladebound.compat.trinkets.TrinketsAccessoryApi");
            return (AccessoryApi) clazz.getDeclaredConstructor().newInstance();
        } catch (Throwable t) {
            // Trinkets is present but something went wrong (version mismatch etc.)
            return new BuiltInAccessoryApi();
        }
    }

    private AccessoryCompat() {}
}
