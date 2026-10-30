package dev.vulkanboost.client;

import dev.vulkanboost.config.VulkanBoostConfig;
import dev.vulkanboost.memory.SmartMemoryManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Settings and status for the opt-in memory-pressure helper.
 */
public final class MemoryScreen extends Screen {
    private final Screen parent;
    private Button enabledButton;
    private Button thresholdButton;

    public MemoryScreen(Screen parent) {
        super(Component.translatable("screen.vulkanboost.memory.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int width = Math.min(300, this.width - 24);
        int x = (this.width - width) / 2;
        int y = this.height / 2 - 30;

        enabledButton = addRenderableWidget(Button.builder(enabledLabel(), button -> {
            VulkanBoostConfig.setMemoryAssistEnabled(!VulkanBoostConfig.memoryAssistEnabled());
            button.setMessage(enabledLabel());
        }).bounds(x, y, width, 20).build());

        thresholdButton = addRenderableWidget(Button.builder(thresholdLabel(), button -> {
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

        addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.performance.back"),
                button -> this.onClose()
        ).bounds(x, y + 56, width, 20).build());
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
        int center = this.width / 2;

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.memory.subtitle"),
                center, 36, 0xB8C7D9);

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.memory.pressure",
                        SmartMemoryManager.pressurePercent()),
                center, this.height / 2 + 70, 0xFFFFFF);

        graphics.drawCenteredString(this.font,
                Component.translatable(SmartMemoryManager.statusKey()),
                center, this.height / 2 + 86, 0xA0A0A0);

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.memory.note"),
                center, Math.min(this.height - 18, this.height / 2 + 112), 0x707070);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
