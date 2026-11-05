package dev.vulkanboost.client;

import dev.vulkanboost.optimization.OptimizationRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Displays bundled optimization settings only when a verified adapter exists.
 * All registry entries remain reachable through pagination on short screens.
 */
public final class OptimizationScreen extends Screen {
    private static final int CONTENT_TOP = 54;
    private static final int ROW_PITCH = 24;
    private static final int REFRESH_FOOTER_HEIGHT = 78;

    private final Screen parent;
    private final int requestedPage;
    private int currentPage;
    private int pageCount;

    public OptimizationScreen(Screen parent) {
        this(parent, 0);
    }

    private OptimizationScreen(Screen parent, int page) {
        super(Component.translatable("screen.vulkanboost.optimizations.title"));
        this.parent = parent;
        this.requestedPage = page;
    }

    @Override
    protected void init() {
        int width = ScreenLayout.centeredWidth(this.width, 340, 12);
        int x = (this.width - width) / 2;
        List<OptimizationRegistry.Entry> entries = OptimizationRegistry.entries();

        int refreshY = ScreenLayout.clampButtonY(this.height, this.height - 78, 20, 4);
        int rowsPerPage = Math.max(1, (refreshY - CONTENT_TOP) / ROW_PITCH);
        this.pageCount = ScreenLayout.pageCount(entries.size(), rowsPerPage);
        this.currentPage = ScreenLayout.clampPage(this.requestedPage, this.pageCount);

        int start = this.currentPage * rowsPerPage;
        int end = Math.min(entries.size(), start + rowsPerPage);
        int y = CONTENT_TOP;
        for (int index = start; index < end; index++) {
            OptimizationRegistry.Entry entry = entries.get(index);
            Button button = Button.builder(rowLabel(entry), widget -> {
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

            button.active = entry.canToggle();
            this.addRenderableWidget(button);
            y += ROW_PITCH;
        }

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.optimizations.refresh"),
                widget -> refreshCurrentPage()
        ).bounds(x, refreshY, width, 20).build());

        int navigationY = ScreenLayout.clampButtonY(this.height, this.height - 52, 20, 4);
        int navigationButtonWidth = Math.max(1, (width - 100) / 2);
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.common.previous"),
                widget -> showPage(this.currentPage - 1)
        ).bounds(x, navigationY, navigationButtonWidth, 20).build()).active = this.currentPage > 0;

        int nextX = x + width - navigationButtonWidth;
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.common.next"),
                widget -> showPage(this.currentPage + 1)
        ).bounds(nextX, navigationY, navigationButtonWidth, 20).build()).active = this.currentPage + 1 < this.pageCount;

        int backY = ScreenLayout.clampButtonY(this.height, this.height - 26, 20, 4);
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.performance.back"),
                widget -> this.onClose()
        ).bounds(x, backY, width, 20).build());
    }

    private void refreshCurrentPage() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new OptimizationScreen(this.parent, this.currentPage));
        }
    }

    private void showPage(int page) {
        if (this.minecraft != null && page >= 0 && page < this.pageCount) {
            this.minecraft.setScreen(new OptimizationScreen(this.parent, page));
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
        int navigationY = ScreenLayout.clampButtonY(this.height, this.height - 52, 20, 4);
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.common.page", this.currentPage + 1, this.pageCount),
                this.width / 2, navigationY + 6, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
