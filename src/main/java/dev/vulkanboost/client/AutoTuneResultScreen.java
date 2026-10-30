package dev.vulkanboost.client;

import net.minecraft.client.gui.GuiGraphics;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font,
                Component.translatable(this.success
                        ? "screen.vulkanboost.autotune.result_success"
                        : "screen.vulkanboost.autotune.result_error"),
                this.width / 2, this.height / 2, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
