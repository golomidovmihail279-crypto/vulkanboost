package dev.vulkanboost.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A small non-blocking result notice used when reflective settings cannot be applied. */
public final class AutoTuneResultScreen extends Screen {
    private final Screen parent;
    private final boolean success;

    public AutoTuneResultScreen(Screen parent, boolean success) {
        super(Component.translatable("screen.vulkanboost.autotune.result_title"));
        this.parent = parent;
        this.success = success;
    }

    @Override
    protected void init() {
        int buttonWidth = ScreenLayout.centeredWidth(this.width, 180, 12);
        int buttonX = (this.width - buttonWidth) / 2;
        int buttonY = ScreenLayout.clampButtonY(this.height, this.height - 26, 20, 4);
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.performance.back"),
                button -> this.onClose()
        ).bounds(buttonX, buttonY, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font,
                Component.translatable(this.success
                        ? "screen.vulkanboost.autotune.result_success"
                        : "screen.vulkanboost.autotune.result_error"),
                this.width / 2, this.height / 2 - 10, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
