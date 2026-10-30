package dev.vulkanboost.client;

import dev.vulkanboost.optimization.OptimizationRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays bundled optimization settings only when a verified adapter exists.
 * Unsupported components are listed as informational rows, not fake switches.
 */
public final class OptimizationScreen extends Screen {
    private final Screen parent;
    private final List<Button> refreshableButtons = new ArrayList<>();

    public OptimizationScreen(Screen parent) {
        super(Component.translatable("screen.vulkanboost.optimizations.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        refreshableButtons.clear();
        int width = Math.min(340, this.width - 24);
        int x = (this.width - width) / 2;
        int y = 54;
        int rowHeight = 26;

        for (OptimizationRegistry.Entry entry : OptimizationRegistry.entries()) {
            boolean canToggle = entry.canToggle();
            Component label = rowLabel(entry);

            Button button = Button.builder(label, widget -> {
                try {
                    entry.setEnabled(!entry.isEnabled());
                    widget.setMessage(rowLabel(entry));
                } catch (ReflectiveOperationException | RuntimeException exception) {
                    widget.setMessage(Component.translatable(
                            "screen.vulkanboost.optimizations.status.error",
                            entry.id()
                    ));
                }
            }).bounds(x, y, width, 20).build();

            button.active = canToggle;
            this.addRenderableWidget(button);
            refreshableButtons.add(button);
            y += rowHeight;
        }

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.optimizations.refresh"),
                widget -> refreshLabels()
        ).bounds(x, Math.min(this.height - 58, y + 2), width, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.performance.back"),
                widget -> this.onClose()
        ).bounds(x, this.height - 30, width, 20).build());
    }

    private void refreshLabels() {
        int index = 0;
        for (OptimizationRegistry.Entry entry : OptimizationRegistry.entries()) {
            Button button = refreshableButtons.get(index++);
            button.active = entry.canToggle();
            button.setMessage(rowLabel(entry));
        }
    }

    private static Component rowLabel(OptimizationRegistry.Entry entry) {
        if (entry.canToggle()) {
            return Component.translatable(
                    entry.isEnabled()
                            ? "screen.vulkanboost.optimizations.row_enabled"
                            : "screen.vulkanboost.optimizations.row_disabled",
                    Component.translatable(entry.titleKey())
            );
        }

        String status = entry.isInstalled()
                ? entry.statusKey()
                : "screen.vulkanboost.optimizations.status.not_installed";

        return Component.translatable(
                "screen.vulkanboost.optimizations.row_status",
                Component.translatable(entry.titleKey()),
                Component.translatable(status)
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.optimizations.subtitle"),
                this.width / 2, 34, 0xB8C7D9);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
