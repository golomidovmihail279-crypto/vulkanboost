package dev.vulkanboost.client;

import dev.vulkanboost.autotune.AutoTuneService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Shows a compact preview, profile selection, and reversible apply action.
 * Actions are kept above a fixed Back button on short screens.
 */
public final class AutoTuneScreen extends Screen {
    private final Screen parent;
    private AutoTuneService.Profile selectedProfile = AutoTuneService.recommendedProfile();
    private AutoTuneService.Plan plan;
    private Button applyButton;
    private Button undoButton;

    public AutoTuneScreen(Screen parent) {
        super(Component.translatable("screen.vulkanboost.autotune.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int width = ScreenLayout.centeredWidth(this.width, 320, 12);
        int x = (this.width - width) / 2;
        int profileY = 64;

        addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.autotune.profile.balanced"),
                button -> selectProfile(AutoTuneService.Profile.BALANCED)
        ).bounds(x, profileY, width, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.autotune.profile.fps"),
                button -> selectProfile(AutoTuneService.Profile.FPS_PRIORITY)
        ).bounds(x, profileY + 22, width, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.autotune.profile.memory"),
                button -> selectProfile(AutoTuneService.Profile.LOW_MEMORY)
        ).bounds(x, profileY + 44, width, 20).build());

        int actionY = 130;
        int gap = 6;
        int actionWidth = (width - gap) / 2;
        applyButton = addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.autotune.apply_short"),
                button -> applyPlan()
        ).bounds(x, actionY, actionWidth, 20).build());

        undoButton = addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.autotune.undo_short"),
                button -> undo()
        ).bounds(x + actionWidth + gap, actionY, actionWidth, 20).build());

        int backY = ScreenLayout.clampButtonY(this.height, this.height - 26, 20, 4);
        addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.performance.back"),
                button -> this.onClose()
        ).bounds(x, backY, width, 20).build());

        refreshPlan();
        updateActionButtons();
    }

    private void updateActionButtons() {
        boolean canUndo = AutoTuneService.canUndo();
        if (applyButton != null) {
            applyButton.active = !canUndo;
        }
        if (undoButton != null) {
            undoButton.active = canUndo;
        }
    }

    private void selectProfile(AutoTuneService.Profile profile) {
        selectedProfile = profile;
        refreshPlan();
    }

    private void refreshPlan() {
        Minecraft client = this.minecraft;
        if (client != null) {
            plan = AutoTuneService.preview(client, selectedProfile);
        }
    }

    private void applyPlan() {
        if (this.minecraft == null || plan == null) {
            return;
        }
        try {
            AutoTuneService.apply(this.minecraft, plan);
            refreshPlan();
            updateActionButtons();
            this.minecraft.setScreen(new AutoTuneResultScreen(this, true));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            this.minecraft.setScreen(new AutoTuneResultScreen(this, false));
        }
    }

    private void undo() {
        if (this.minecraft == null || !AutoTuneService.canUndo()) {
            return;
        }
        try {
            AutoTuneService.undo(this.minecraft);
            refreshPlan();
            updateActionButtons();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            this.minecraft.setScreen(new AutoTuneResultScreen(this, false));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (plan == null) {
            return;
        }

        int center = this.width / 2;
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.autotune.detected",
                        plan.logicalProcessors(), formatGiB(plan.maxHeapBytes())),
                center, 27, 0xFFFFFF);

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.autotune.selected",
                        Component.translatable(plan.profile().titleKey())),
                center, 39, 0xFFFFFF);

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.autotune.preview_summary",
                        distancePair(plan.currentRenderDistance(), plan.targetRenderDistance()),
                        distancePair(plan.currentSimulationDistance(), plan.targetSimulationDistance())),
                center, 51, 0xA0A0A0);
    }

    private static String distancePair(int current, int target) {
        if (current < 0 || target < 0) {
            return "—";
        }
        return current + " → " + target;
    }

    private static String formatGiB(long bytes) {
        return String.format(Locale.ROOT, "%.1f", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
