package net.owen.bladebound;

import net.owen.bladebound.BladeboundConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.owen.bladebound.item.ModItems;

public class BladeboundClient implements ClientModInitializer {

    private static final String NBT_ROOT = "bladebound";
    private static final String NBT_DISCIPLINE = "discipline";

    @Override
    public void onInitializeClient() {
        // NOTE: Some Fabric API versions use (DrawContext ctx),
        // newer 1.21+ builds often use (DrawContext ctx, RenderTickCounter tickCounter).
        // We don't need tickCounter, but we accept it to match the signature.
        HudRenderCallback.EVENT.register((DrawContext ctx, RenderTickCounter tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            if (!BladeboundConfig.DATA.hudEnabled) return;

            ItemStack stack = mc.player.getMainHandStack();
            if (stack.isEmpty()) return;

            // Only show for Wado
            if (!stack.isOf(ModItems.WADOICHIMONJI)) return;

            int discipline = readDiscipline(stack);
            renderDisciplineBar(ctx, mc, discipline);
        });
    }

    private static int readDiscipline(ItemStack stack) {
        // 1.21+ custom data component
        NbtCompound data = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();

        if (!data.contains(NBT_ROOT, NbtElement.COMPOUND_TYPE)) return 0;

        NbtCompound root = data.getCompound(NBT_ROOT);
        int v = root.getInt(NBT_DISCIPLINE);

        if (v < 0) v = 0;
        if (v > 100) v = 100;
        return v;
    }

    private static void renderDisciplineBar(DrawContext ctx, MinecraftClient mc, int discipline) {
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        // Position: above the hotbar, centered
        int barWidth = 81;
        int barHeight = 8;

        int x = (sw / 2) - (barWidth / 2);
        int y = sh - 58;

        // Background frame + inner background
        ctx.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xAA000000);
        ctx.fill(x, y, x + barWidth, y + barHeight, 0xFF1A1A1A);

        // Fill amount
        int fill = (int) Math.round((discipline / 100.0) * barWidth);
        if (fill > 0) {
            ctx.fill(x, y, x + fill, y + barHeight, 0xFFFFC04D);
        }

        // Label
        String text = "Discipline: " + discipline + "%";
        ctx.drawText(mc.textRenderer, text, x, y - 10, 0xFFFFFF, true);
    }
}
