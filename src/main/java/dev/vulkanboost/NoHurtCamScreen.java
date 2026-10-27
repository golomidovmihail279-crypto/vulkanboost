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
    private int pendingReduction;
    private boolean committed;

    public NoHurtCamScreen(Screen parent) {
        super(Component.translatable("screen.vulkanboost.no_hurt_cam.title"));
        this.parent = parent;
        this.originalReduction = NoHurtCamConfig.getReduction();
        this.pendingReduction = this.originalReduction;
    }

    @Override
    protected void init() {
        super.init();
        int sliderWidth = Math.min(320, this.width - 40);
        int sliderX = (this.width - sliderWidth) / 2;

        this.addRenderableWidget(new StrengthSlider(
            sliderX, this.height / 2 - 4, sliderWidth, 20, this.pendingReduction / 100.0, this
        ));
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.done"),
            button -> {
                NoHurtCamConfig.setReductionInMemory(this.pendingReduction);
                NoHurtCamConfig.save();
                this.committed = true;
                Minecraft.getInstance().setScreen(this.parent);
            }
        ).bounds(this.width / 2 - 100, this.height / 2 + 34, 200, 20).build());
    }

    private void updatePendingReduction(int value) {
        this.pendingReduction = Math.max(0, Math.min(100, value));
        NoHurtCamConfig.setReductionInMemory(this.pendingReduction);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(
            this.font,
            Component.translatable("screen.vulkanboost.no_hurt_cam.description"),
            this.width / 2, this.height / 2 - 34, 0xFFFFFF
        );
    }

    @Override
    public void onClose() {
        if (!this.committed) {
            NoHurtCamConfig.setReductionInMemory(this.originalReduction);
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
