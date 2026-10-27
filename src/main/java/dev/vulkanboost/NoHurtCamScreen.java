package dev.vulkanboost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class NoHurtCamScreen extends Screen {
    private final Screen parent;
    private final int originalReduction;
    private final boolean originalEnabled;
    private int pendingReduction;
    private boolean pendingEnabled;
    private boolean committed;

    public NoHurtCamScreen(Screen parent) {
        super(Component.translatable("screen.vulkanboost.no_hurt_cam.title"));
        this.parent = parent;
        this.originalReduction = NoHurtCamConfig.getReduction();
        this.originalEnabled = NoHurtCamConfig.isEnabled();
        this.pendingReduction = this.originalReduction;
        this.pendingEnabled = this.originalEnabled;
    }

    @Override
    protected void init() {
        super.init();
        int sliderWidth = Math.min(320, this.width - 40);
        int sliderX = (this.width - sliderWidth) / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(
            enabledLabel(this.pendingEnabled),
            button -> {
                this.pendingEnabled = !this.pendingEnabled;
                NoHurtCamConfig.setEnabledInMemory(this.pendingEnabled);
                button.setMessage(enabledLabel(this.pendingEnabled));
            }
        ).bounds(this.width / 2 - 110, centerY - 15, 220, 20).build());

        this.addRenderableWidget(new StrengthSlider(
            sliderX, centerY + 12, sliderWidth, 20, this.pendingReduction / 100.0, this
        ));

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.done"),
            button -> {
                NoHurtCamConfig.setReductionInMemory(this.pendingReduction);
                NoHurtCamConfig.setEnabledInMemory(this.pendingEnabled);
                NoHurtCamConfig.save();
                this.committed = true;
                Minecraft.getInstance().setScreen(this.parent);
            }
        ).bounds(this.width / 2 - 100, centerY + 43, 200, 20).build());
    }

    private static Component enabledLabel(boolean enabled) {
        return Component.translatable(enabled
            ? "screen.vulkanboost.no_hurt_cam.enabled_on"
            : "screen.vulkanboost.no_hurt_cam.enabled_off");
    }

    private void updatePendingReduction(int value) {
        this.pendingReduction = Math.max(0, Math.min(100, value));
        NoHurtCamConfig.setReductionInMemory(this.pendingReduction);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 26, 0xFFFFFF);
        graphics.drawCenteredString(
            this.font,
            Component.translatable("screen.vulkanboost.no_hurt_cam.description"),
            this.width / 2, this.height / 2 - 43, 0xFFFFFF
        );
    }

    @Override
    public void onClose() {
        if (!this.committed) {
            NoHurtCamConfig.setReductionInMemory(this.originalReduction);
            NoHurtCamConfig.setEnabledInMemory(this.originalEnabled);
        }
        Minecraft.getInstance().setScreen(this.parent);
    }

    private static final class StrengthSlider extends AbstractSliderButton {
        private final NoHurtCamScreen owner;

        private StrengthSlider(int x, int y, int width, int height, double value, NoHurtCamScreen owner) {
            super(x, y, width, height, Component.empty(), value);
            this.owner = owner;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            int reduction = (int) Math.round(this.value * 100.0);
            this.setMessage(Component.translatable("option.vulkanboost.hurt_cam_reduction", reduction));
        }

        @Override
        protected void applyValue() {
            this.owner.updatePendingReduction((int) Math.round(this.value * 100.0));
        }
    }
}
