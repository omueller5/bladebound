package net.owen.bladebound.client;

import net.minecraft.client.MinecraftClient;
import net.owen.bladebound.client.screen.CodexScreen;

public final class CodexClientOpen {
    private CodexClientOpen() {}

    public static void open() {
        MinecraftClient.getInstance().setScreen(new CodexScreen());
    }
}
