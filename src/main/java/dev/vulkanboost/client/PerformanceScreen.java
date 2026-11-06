package dev.vulkanboost.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Displays sampled Minecraft client and Java heap metrics.
 * Text is wrapped to the available width and packed above a fixed Back button.
 */
public final class PerformanceScreen extends Screen {
    private final Screen parent;

    public PerformanceScreen(Screen parent) {
        super(Component.translatable("screen.vulkanboost.performance.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = ScreenLayout.centeredWidth(this.width, 180, 12);
        int buttonX = (this.width - buttonWidth) / 2;
        int backY = ScreenLayout.clampButtonY(this.height, this.height - 26, 20, 4);
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.performance.back"),
                button -> this.onClose()
        ).bounds(buttonX, backY, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        PerformanceMonitor.Snapshot snapshot = PerformanceMonitor.latest();
        List<Component> metrics = new ArrayList<>();
        metrics.add(Component.translatable("screen.vulkanboost.performance.subtitle"));
        metrics.add(Component.translatable("screen.vulkanboost.performance.fps", snapshot.fps()));

        String frameTime = snapshot.frameTimeNs() > 0
                ? String.format(Locale.ROOT, "%.2f", snapshot.frameTimeMillis())
                : "—";
        metrics.add(Component.translatable("screen.vulkanboost.performance.frame_time", frameTime));
        metrics.add(Component.translatable(
                "screen.vulkanboost.performance.memory",
                formatMiB(snapshot.usedHeapBytes()),
                formatMiB(snapshot.maximumHeapBytes())
        ));
        metrics.add(Component.translatable(
                "screen.vulkanboost.performance.committed",
                formatMiB(snapshot.committedHeapBytes())
        ));
        metrics.add(Component.translatable(
                "screen.vulkanboost.performance.components",
                snapshot.loadedModCount()
        ));
        metrics.add(Component.translatable("screen.vulkanboost.performance.component_usage_unavailable"));
        metrics.add(Component.translatable("screen.vulkanboost.performance.hardware_usage_unavailable"));
        metrics.add(Component.translatable("screen.vulkanboost.performance.refresh_rate"));

        int maxTextWidth = Math.max(120, this.width - 24);
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component metric : metrics) {
            lines.addAll(this.font.split(metric, maxTextWidth));
        }

        int topY = 28;
        int lineStep = ScreenLayout.fitLineStep(this.height, topY, 30, lines.size(), 14, 10);
        int y = topY;
        for (int index = 0; index < lines.size(); index++) {
            int color = index == 0 ? 0xB8C7D9 : 0xFFFFFF;
            graphics.drawCenteredString(this.font, lines.get(index), this.width / 2, y, color);
            y += lineStep;
        }
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
