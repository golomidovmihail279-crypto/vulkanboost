package dev.vulkanboost.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Displays sampled Minecraft client and Java heap metrics.
 */
public final class PerformanceScreen extends Screen {
    private final Screen parent;

    public PerformanceScreen(Screen parent) {
        super(Component.translatable("screen.vulkanboost.performance.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 180;
        int buttonX = (this.width - buttonWidth) / 2;
        int backY = this.height - 30;
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.performance.back"),
                button -> this.onClose()
        ).bounds(buttonX, backY, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        PerformanceMonitor.Snapshot snapshot = PerformanceMonitor.latest();
        int centerX = this.width / 2;
        int lineHeight = 16;
        int topY = 42;

        // Calculate total text height needed
        int textLines = 9; // subtitle + 7 metrics + refresh
        int totalTextHeight = textLines * lineHeight + 16; // extra for subtitle spacing

        // Available space between top and back button
        int backY = this.height - 30;
        int availableHeight = backY - topY - 8;

        if (totalTextHeight > availableHeight) {
            // Not enough space - reduce line height
            lineHeight = Math.max(12, availableHeight / textLines);
        }

        int y = topY;

        graphics.drawCenteredString(this.font, Component.translatable("screen.vulkanboost.performance.subtitle"),
                centerX, y, 0xB8C7D9);
        y += lineHeight + 8;

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.performance.fps", snapshot.fps()),
                centerX, y, 0xFFFFFF);
        y += lineHeight;

        String frameTime = snapshot.frameTimeNs() > 0
                ? String.format(Locale.ROOT, "%.2f", snapshot.frameTimeMillis())
                : "—";
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.performance.frame_time", frameTime),
                centerX, y, 0xFFFFFF);
        y += lineHeight;

        graphics.drawCenteredString(this.font,
                Component.translatable(
                        "screen.vulkanboost.performance.memory",
                        formatMiB(snapshot.usedHeapBytes()),
                        formatMiB(snapshot.maximumHeapBytes())
                ),
                centerX, y, 0xFFFFFF);
        y += lineHeight;

        graphics.drawCenteredString(this.font,
                Component.translatable(
                        "screen.vulkanboost.performance.committed",
                        formatMiB(snapshot.committedHeapBytes())
                ),
                centerX, y, 0xFFFFFF);
        y += lineHeight + 4;

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.performance.components",
                        snapshot.loadedComponentCount()),
                centerX, y, 0xFFFFFF);
        y += lineHeight;

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.performance.component_usage_unavailable"),
                centerX, y, 0xA0A0A0);
        y += lineHeight;

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.performance.hardware_usage_unavailable"),
                centerX, y, 0xA0A0A0);
        y += lineHeight;

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.performance.refresh_rate"),
                centerX, Math.min(y + 4, backY - 12), 0x707070);
    }

    private static String formatMiB(long bytes) {
        return String.format(Locale.ROOT, "%.0f", bytes / (1024.0 * 1024.0));
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
