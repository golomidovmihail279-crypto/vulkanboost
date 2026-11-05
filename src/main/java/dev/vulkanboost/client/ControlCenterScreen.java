package dev.vulkanboost.client;

import dev.vulkanboost.config.VulkanBoostConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * First iteration of the in-game VulkanBoost Control Center.
 * The performance monitor is available as a separate screen.
 */
public final class ControlCenterScreen extends Screen {
    private final Screen parent;
    private Button hintsButton;

    public ControlCenterScreen(Screen parent) {
        super(Component.translatable("screen.vulkanboost.control_center.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 230;
        int buttonX = (this.width - buttonWidth) / 2;

        // Reserve space for subtitle (at y=48) and bottom margin.
        int firstButtonY = Math.max(62, this.height / 2 - 86);
        int buttonStep = 28;

        this.hintsButton = this.addRenderableWidget(Button.builder(
                hintsButtonLabel(),
                button -> {
                    VulkanBoostConfig.setShowHints(!VulkanBoostConfig.showHints());
                    button.setMessage(hintsButtonLabel());
                }
        ).bounds(buttonX, firstButtonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.control_center.monitor"),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new PerformanceScreen(this));
                    }
                }
        ).bounds(buttonX, firstButtonY + buttonStep, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.control_center.optimizations"),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new OptimizationScreen(this));
                    }
                }
        ).bounds(buttonX, firstButtonY + 2 * buttonStep, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.control_center.autotune"),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new AutoTuneScreen(this));
                    }
                }
        ).bounds(buttonX, firstButtonY + 3 * buttonStep, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.control_center.memory"),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new MemoryScreen(this));
                    }
                }
        ).bounds(buttonX, firstButtonY + 4 * buttonStep, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.control_center.reset"),
                button -> {
                    VulkanBoostConfig.resetToDefaults();
                    this.hintsButton.setMessage(hintsButtonLabel());
                }
        ).bounds(buttonX, firstButtonY + 5 * buttonStep, buttonWidth, 20).build());

        int closeY = Math.max(this.height - 30, firstButtonY + 6 * buttonStep + 8);
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.control_center.close"),
                button -> this.onClose()
        ).bounds(buttonX, closeY, buttonWidth, 20).build());
    }

    private static Component hintsButtonLabel() {
        return Component.translatable(VulkanBoostConfig.showHints()
                ? "screen.vulkanboost.control_center.hints_on"
                : "screen.vulkanboost.control_center.hints_off");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.vulkanboost.control_center.subtitle"),
                this.width / 2,
                48,
                0xB8C7D9
        );

        if (VulkanBoostConfig.showHints()) {
            graphics.drawCenteredString(
                    this.font,
                    Component.translatable("screen.vulkanboost.control_center.hint"),
                    this.width / 2,
                    this.height / 2 + 155,
                    0xA0A0A0
            );
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
