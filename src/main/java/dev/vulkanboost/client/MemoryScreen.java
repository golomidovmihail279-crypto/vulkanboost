package dev.vulkanboost.client;

import dev.vulkanboost.config.VulkanBoostConfig;
import dev.vulkanboost.memory.SmartMemoryManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings and status for the opt-in memory-pressure helper.
 */
public final class MemoryScreen extends Screen {
    private final Screen parent;

    public MemoryScreen(Screen parent) {
        super(Component.translatable("screen.vulkanboost.memory.title"));
        this.parent = parent;
    }

    private int controlsTop() {
        return Math.max(100, Math.min(this.height / 2 - 20, this.height - 82));
    }

    @Override
    protected void init() {
        int width = ScreenLayout.centeredWidth(this.width, 300, 12);
        int x = (this.width - width) / 2;
        int y = controlsTop();

        addRenderableWidget(Button.builder(enabledLabel(), button -> {
            VulkanBoostConfig.setMemoryAssistEnabled(!VulkanBoostConfig.memoryAssistEnabled());
            button.setMessage(enabledLabel());
        }).bounds(x, y, width, 20).build());

        addRenderableWidget(Button.builder(thresholdLabel(), button -> {
            int[] thresholds = {70, 80, 85, 90, 95};
            int current = VulkanBoostConfig.memoryThresholdPercent();
            int next = thresholds[0];
            for (int i = 0; i < thresholds.length; i++) {
                if (thresholds[i] == current) {
                    next = thresholds[(i + 1) % thresholds.length];
                    break;
                }
            }
            VulkanBoostConfig.setMemoryThresholdPercent(next);
            button.setMessage(thresholdLabel());
        }).bounds(x, y + 28, width, 20).build());

        int backY = ScreenLayout.clampButtonY(this.height, this.height - 26, 20, 4);
        addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.performance.back"),
                button -> this.onClose()
        ).bounds(x, backY, width, 20).build());
    }

    private static Component enabledLabel() {
        return Component.translatable(VulkanBoostConfig.memoryAssistEnabled()
                ? "screen.vulkanboost.memory.toggle_on"
                : "screen.vulkanboost.memory.toggle_off");
    }

    private static Component thresholdLabel() {
        return Component.translatable("screen.vulkanboost.memory.threshold",
                VulkanBoostConfig.memoryThresholdPercent());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        List<Component> messages = List.of(
                Component.translatable("screen.vulkanboost.memory.subtitle"),
                Component.translatable("screen.vulkanboost.memory.pressure",
                        SmartMemoryManager.pressurePercent()),
                Component.translatable(SmartMemoryManager.statusKey()),
                Component.translatable("screen.vulkanboost.memory.note")
        );

        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component message : messages) {
            lines.addAll(this.font.split(message, Math.max(120, this.width - 24)));
        }

        int top = 32;
        int available = Math.max(10, controlsTop() - top - 6);
        int lineStep = Math.max(10, Math.min(12, available / Math.max(1, lines.size())));
        int y = top;
        for (FormattedCharSequence line : lines) {
            graphics.drawCenteredString(this.font, line, this.width / 2, y, 0xFFFFFF);
            y += lineStep;
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
