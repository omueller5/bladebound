package net.owen.bladebound.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.owen.bladebound.magic.SpellHolder;

@Environment(EnvType.CLIENT)
public final class BarrierVisualHud {

    // src/main/resources/assets/bladebound/textures/entity/defensive_magic.png
    private static final Identifier TEX = Identifier.of("bladebound", "textures/entity/defensive_magic.png");

    // On-screen size (scaled from 128x128 source)
    private static final int ICON_SIZE = 64;

    private BarrierVisualHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register(BarrierVisualHud::render);
    }

    // 1.21.x uses RenderTickCounter here (not float tickDelta)
    private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (!((Object) mc.player instanceof SpellHolder sh)) return;
        if (!sh.bladebound$isBarrierActive()) return;

        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();

        // Centered, slightly below crosshair
        int x = (w / 2) - (ICON_SIZE / 2);
        int y = (h / 2) - (ICON_SIZE / 2) + 18;

        // Draw 128x128 texture scaled to ICON_SIZE
        ctx.drawTexture(TEX, x, y, 0, 0, ICON_SIZE, ICON_SIZE, 128, 128);
    }
}
