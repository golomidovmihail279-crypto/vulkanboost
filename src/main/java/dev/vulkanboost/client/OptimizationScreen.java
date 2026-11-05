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

        // Reserve bottom space for Refresh and Back buttons so they never overlap the rows.
        int bottomReserved = 4 + 20 + 8 + 20; // gap + refresh + gap + back
        int maxRows = (this.height - 54 - bottomReserved) / rowHeight;
        if (maxRows < 1) {
            maxRows = 1;
        }
        int visibleCount = Math.min(OptimizationRegistry.entries().size(), maxRows);

        int index = 0;
        for (OptimizationRegistry.Entry entry : OptimizationRegistry.entries()) {
            if (index >= visibleCount) {
                break;
            }
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
            index++;
        }

        // Refresh button is always placed below the last visible row, never clamped upward.
        int refreshY = y + 4;
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.optimizations.refresh"),
                widget -> refreshLabels()
        ).bounds(x, refreshY, width, 20).build());

        // Back button stays at the bottom edge, but is pushed down if Refresh is near it.
        int backY = Math.max(this.height - 30, refreshY + 28);
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.performance.back"),
                widget -> this.onClose()
        ).bounds(x, backY, width, 20).build());
    }

    private void refreshLabels() {
        int index = 0;
        for (OptimizationRegistry.Entry entry : OptimizationRegistry.entries()) {
            if (index >= refreshableButtons.size()) {
                break;
            }
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
