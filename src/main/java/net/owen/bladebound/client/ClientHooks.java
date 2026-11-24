package net.owen.bladebound.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.owen.bladebound.client.screen.CodexScreen; // adjust import to your actual package

@Environment(EnvType.CLIENT)
public class ClientHooks {
    public static void openCodexScreen() {
        MinecraftClient.getInstance().setScreen(new CodexScreen());
    }
}
