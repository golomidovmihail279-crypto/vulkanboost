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
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.performance.back"),
                button -> this.onClose()
        ).bounds((this.width - buttonWidth) / 2, this.height - 34, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        PerformanceMonitor.Snapshot snapshot = PerformanceMonitor.latest();
        int centerX = this.width / 2;
        int y = 42;
        int lineHeight = 20;

        graphics.drawCenteredString(this.font, Component.translatable("screen.vulkanboost.performance.subtitle"),
                centerX, y, 0xB8C7D9);
        y += 34;

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
        y += lineHeight + 12;

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
                centerX, y + 8, 0x707070);
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
