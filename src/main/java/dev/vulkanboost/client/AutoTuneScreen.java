package dev.vulkanboost.client;

import dev.vulkanboost.autotune.AutoTuneService;
import dev.vulkanboost.autotune.MinecraftOptionAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Shows the hardware-based recommendation, a preview, and reversible apply action.
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
        int width = Math.min(320, this.width - 24);
        int x = (this.width - width) / 2;
        int y = 52;

        addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.autotune.profile.balanced"),
                button -> selectProfile(AutoTuneService.Profile.BALANCED)
        ).bounds(x, y, width, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.autotune.profile.fps"),
                button -> selectProfile(AutoTuneService.Profile.FPS_PRIORITY)
        ).bounds(x, y, width, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.autotune.profile.memory"),
                button -> selectProfile(AutoTuneService.Profile.LOW_MEMORY)
        ).bounds(x, y, width, 20).build());
        y += 30;

        applyButton = addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.autotune.apply"),
                button -> applyPlan()
        ).bounds(x, y, width, 20).build());
        y += 24;

        undoButton = addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.autotune.undo"),
                button -> undo()
        ).bounds(x, y, width, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(
                Component.translatable("screen.vulkanboost.performance.back"),
                button -> this.onClose()
        ).bounds(x, y, width, 20).build());

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
        } catch (RuntimeException exception) {
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
        int y = 28;
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.autotune.subtitle"), center, y, 0xB8C7D9);
        y += 14;

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.autotune.detected",
                        plan.logicalProcessors(), formatGiB(plan.maxHeapBytes())),
                center, y, 0xFFFFFF);
        y += 14;

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.autotune.selected",
                        Component.translatable(plan.profile().titleKey())),
                center, y, 0xFFFFFF);
        y += 14;

        graphics.drawCenteredString(this.font,
                distanceLine("render", plan.currentRenderDistance(), plan.targetRenderDistance()),
                center, y, 0xFFFFFF);
        y += 14;
        graphics.drawCenteredString(this.font,
                distanceLine("simulation", plan.currentSimulationDistance(), plan.targetSimulationDistance()),
                center, y, 0xFFFFFF);
        y += 14;

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.autotune.adapters", plan.featureTargets().size()),
                center, y, 0xFFFFFF);
        y += 14;

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.vulkanboost.autotune.preview_note"),
                center, Math.min(this.height - 34, y + 3), 0xA0A0A0);
    }

    private Component distanceLine(String key, int current, int target) {
        if (current < 0 || target < 0) {
            return Component.translatable("screen.vulkanboost.autotune.option_unavailable", key);
        }
        return Component.translatable("screen.vulkanboost.autotune.option_change", key, current, target);
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
