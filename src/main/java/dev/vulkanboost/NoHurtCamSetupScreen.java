package dev.vulkanboost;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class NoHurtCamSetupScreen extends Screen {
    private final int originalReduction = NoHurtCamConfig.getReduction();
    private final boolean originalEnabled = NoHurtCamConfig.isEnabled();
    private final InputConstants.Key originalToggleKey = VulkanBoostClient.getToggleKey();

    private int pendingReduction = this.originalReduction;
    private boolean pendingEnabled = this.originalEnabled;
    private boolean listeningForKey;
    private boolean committed;

    private Button bindButton;
    private Button doneButton;

    public NoHurtCamSetupScreen() {
        super(Component.translatable("screen.vulkanboost.setup.title"));
    }

    @Override
    protected void init() {
        super.init();
        int centerY = this.height / 2;
        int sliderWidth = Math.min(320, this.width - 40);
        int sliderX = (this.width - sliderWidth) / 2;

        this.bindButton = this.addRenderableWidget(Button.builder(bindLabel(), button -> {
            this.listeningForKey = true;
            button.setMessage(Component.translatable("screen.vulkanboost.setup.press_key"));
        }).bounds(this.width / 2 - 140, centerY - 61, 280, 20).build());

        this.addRenderableWidget(Button.builder(enabledLabel(this.pendingEnabled), button -> {
            this.pendingEnabled = !this.pendingEnabled;
            NoHurtCamConfig.setEnabledInMemory(this.pendingEnabled);
            button.setMessage(enabledLabel(this.pendingEnabled));
        }).bounds(this.width / 2 - 110, centerY - 32, 220, 20).build());

        this.addRenderableWidget(new StrengthSlider(
            sliderX, centerY - 3, sliderWidth, 20, this.pendingReduction / 100.0, this
        ));

        this.doneButton = this.addRenderableWidget(Button.builder(
            Component.translatable("gui.done"),
            button -> this.finishSetup()
        ).bounds(this.width / 2 - 100, centerY + 37, 200, 20).build());
        this.doneButton.active = VulkanBoostClient.isToggleKeyAssigned();

        this.addRenderableWidget(Button.builder(
            Component.translatable("screen.vulkanboost.setup.later"),
            button -> this.onClose()
        ).bounds(this.width / 2 - 100, centerY + 64, 200, 20).build());
    }

    private Component bindLabel() {
        return Component.translatable("screen.vulkanboost.setup.bind", VulkanBoostClient.getToggleKeyName());
    }

    private static Component enabledLabel(boolean enabled) {
        return Component.translatable(enabled
            ? "screen.vulkanboost.no_hurt_cam.enabled_on"
            : "screen.vulkanboost.no_hurt_cam.enabled_off");
    }

    private void updateBindLabel() {
        if (this.bindButton != null) {
            this.bindButton.setMessage(bindLabel());
        }
        if (this.doneButton != null) {
            this.doneButton.active = VulkanBoostClient.isToggleKeyAssigned();
        }
    }

    private void assignToggleKey(InputConstants.Key key) {
        if (key.equals(VulkanBoostClient.getOpenSettingsKey())) {
            this.bindButton.setMessage(Component.translatable("screen.vulkanboost.setup.key_conflict"));
            return;
        }

        VulkanBoostClient.setToggleKeyInMemory(key);
        this.listeningForKey = false;
        this.updateBindLabel();
    }

    private void finishSetup() {
        if (!VulkanBoostClient.isToggleKeyAssigned()) {
            return;
        }

        NoHurtCamConfig.setReductionInMemory(this.pendingReduction);
        NoHurtCamConfig.setEnabledInMemory(this.pendingEnabled);
        NoHurtCamConfig.setSetupCompleteInMemory(true);
        NoHurtCamConfig.save();
        VulkanBoostClient.saveKeyMappings();
        this.committed = true;
        Minecraft.getInstance().setScreen(null);
    }

    private void updatePendingReduction(int value) {
        this.pendingReduction = Math.max(0, Math.min(100, value));
        NoHurtCamConfig.setReductionInMemory(this.pendingReduction);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.listeningForKey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.listeningForKey = false;
                this.updateBindLabel();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                VulkanBoostClient.setToggleKeyInMemory(
                    InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_UNKNOWN)
                );
                this.listeningForKey = false;
                this.updateBindLabel();
                return true;
            }

            this.assignToggleKey(InputConstants.getKey(keyCode, scanCode));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.listeningForKey) {
            this.assignToggleKey(InputConstants.Type.MOUSE.getOrCreate(button));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 26, 0xFFFFFF);
        graphics.drawCenteredString(
            this.font,
            Component.translatable("screen.vulkanboost.setup.description"),
            this.width / 2, this.height / 2 - 92, 0xFFFFFF
        );
        graphics.drawCenteredString(
            this.font,
            Component.translatable("screen.vulkanboost.setup.strength_hint"),
            this.width / 2, this.height / 2 + 18, 0xAAAAAA
        );
    }

    @Override
    public void onClose() {
        if (!this.committed) {
            NoHurtCamConfig.setReductionInMemory(this.originalReduction);
            NoHurtCamConfig.setEnabledInMemory(this.originalEnabled);
            VulkanBoostClient.setToggleKeyInMemory(this.originalToggleKey);
            VulkanBoostClient.saveKeyMappings();
        }
        Minecraft.getInstance().setScreen(null);
    }

    private static final class StrengthSlider extends AbstractSliderButton {
        private final NoHurtCamSetupScreen owner;

        private StrengthSlider(int x, int y, int width, int height, double value, NoHurtCamSetupScreen owner) {
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
