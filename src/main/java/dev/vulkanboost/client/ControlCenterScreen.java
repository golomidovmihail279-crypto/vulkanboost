package dev.vulkanboost.client;

import dev.vulkanboost.config.VulkanBoostConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Main VulkanBoost menu. The action list is paginated when the screen is short.
 */
public final class ControlCenterScreen extends Screen {
    private static final int ACTION_COUNT = 6;
    private static final int CONTENT_TOP = 56;
    private static final int ROW_PITCH = 22;
    private static final int FOOTER_HEIGHT = 50;

    private final Screen parent;
    private final int requestedPage;
    private int currentPage;
    private int pageCount;
    private Button hintsButton;

    public ControlCenterScreen(Screen parent) {
        this(parent, 0);
    }

    private ControlCenterScreen(Screen parent, int page) {
        super(Component.translatable("screen.vulkanboost.control_center.title"));
        this.parent = parent;
        this.requestedPage = page;
    }

    @Override
    protected void init() {
        int buttonWidth = ScreenLayout.centeredWidth(this.width, 230, 12);
        int buttonX = (this.width - buttonWidth) / 2;
        int rowsPerPage = ScreenLayout.rowsPerPage(this.height, CONTENT_TOP, FOOTER_HEIGHT, ROW_PITCH);
        this.pageCount = ScreenLayout.pageCount(ACTION_COUNT, rowsPerPage);
        this.currentPage = ScreenLayout.clampPage(this.requestedPage, this.pageCount);

        int firstAction = this.currentPage * rowsPerPage;
        int endAction = Math.min(ACTION_COUNT, firstAction + rowsPerPage);
        int y = CONTENT_TOP;
        for (int action = firstAction; action < endAction; action++) {
            addActionButton(action, buttonX, y, buttonWidth);
            y += ROW_PITCH;
        }

        int navigationY = ScreenLayout.clampButtonY(this.height, this.height - 48, 20, 4);
        int navigationButtonWidth = Math.max(1, (buttonWidth - 82) / 2);
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.common.previous"),
                button -> showPage(this.currentPage - 1)
        ).bounds(buttonX, navigationY, navigationButtonWidth, 20).build()).active = this.currentPage > 0;

        int nextX = buttonX + buttonWidth - navigationButtonWidth;
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.common.next"),
                button -> showPage(this.currentPage + 1)
        ).bounds(nextX, navigationY, navigationButtonWidth, 20).build()).active = this.currentPage + 1 < this.pageCount;

        int closeY = ScreenLayout.clampButtonY(this.height, this.height - 24, 20, 4);
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.control_center.close"),
                button -> this.onClose()
        ).bounds(buttonX, closeY, buttonWidth, 20).build());
    }

    private void addActionButton(int action, int x, int y, int width) {
        switch (action) {
            case 0 -> this.hintsButton = this.addRenderableWidget(Button.builder(
                    hintsButtonLabel(),
                    button -> {
                        VulkanBoostConfig.setShowHints(!VulkanBoostConfig.showHints());
                        button.setMessage(hintsButtonLabel());
                    }
            ).bounds(x, y, width, 20).build());
            case 1 -> this.addRenderableWidget(Button.builder(
                    Component.translatable("screen.vulkanboost.control_center.monitor"),
                    button -> open(new PerformanceScreen(this))
            ).bounds(x, y, width, 20).build());
            case 2 -> this.addRenderableWidget(Button.builder(
                    Component.translatable("screen.vulkanboost.control_center.optimizations"),
                    button -> open(new OptimizationScreen(this))
            ).bounds(x, y, width, 20).build());
            case 3 -> this.addRenderableWidget(Button.builder(
                    Component.translatable("screen.vulkanboost.control_center.autotune"),
                    button -> open(new AutoTuneScreen(this))
            ).bounds(x, y, width, 20).build());
            case 4 -> this.addRenderableWidget(Button.builder(
                    Component.translatable("screen.vulkanboost.control_center.memory"),
                    button -> open(new MemoryScreen(this))
            ).bounds(x, y, width, 20).build());
            case 5 -> this.addRenderableWidget(Button.builder(
                    Component.translatable("screen.vulkanboost.control_center.reset"),
                    button -> {
                        VulkanBoostConfig.resetToDefaults();
                        if (this.hintsButton != null) {
                            this.hintsButton.setMessage(hintsButtonLabel());
                        }
                    }
            ).bounds(x, y, width, 20).build());
            default -> throw new IllegalArgumentException("Unknown Control Center action: " + action);
        }
    }

    private void open(Screen screen) {
        if (this.minecraft != null) {
            this.minecraft.setScreen(screen);
        }
    }

    private void showPage(int page) {
        if (this.minecraft != null && page >= 0 && page < this.pageCount) {
            this.minecraft.setScreen(new ControlCenterScreen(this.parent, page));
        }
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
                32,
                0xB8C7D9
        );

        if (VulkanBoostConfig.showHints()) {
            graphics.drawCenteredString(
                    this.font,
                    Component.translatable("screen.vulkanboost.control_center.hint"),
                    this.width / 2,
                    43,
                    0xA0A0A0
            );
        }

        int navigationY = ScreenLayout.clampButtonY(this.height, this.height - 48, 20, 4);
        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.vulkanboost.common.page", this.currentPage + 1, this.pageCount),
                this.width / 2,
                navigationY + 6,
                0xFFFFFF
        );
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
