package dev.vulkanboost.client;

/**
 * Dependency-free layout regression tests for the Control Center screen geometry.
 * These tests validate the shared calculations used by the Minecraft screens.
 */
public final class ScreenLayoutSelfTest {
    private static final int[][] SIZES = {
            {320, 180},
            {427, 240},
            {640, 360},
            {854, 480},
            {1920, 1080}
    };

    private static int checks;

    private ScreenLayoutSelfTest() {
    }

    public static void main(String[] args) {
        for (int[] size : SIZES) {
            int width = size[0];
            int height = size[1];
            checkControlCenter(width, height);
            checkOptimizationPagination(width, height);
            checkAutoTune(width, height);
            checkMemory(width, height);
            checkPerformance(height);
            checkResultScreen(width, height);
        }
        System.out.println("ScreenLayout: " + checks + " geometry checks passed.");
    }

    private static void checkControlCenter(int width, int height) {
        int buttonWidth = ScreenLayout.centeredWidth(width, 230, 12);
        int x = (width - buttonWidth) / 2;
        int rows = ScreenLayout.rowsPerPage(height, 56, 50, 22);
        int pages = ScreenLayout.pageCount(6, rows);
        int navigationY = ScreenLayout.clampButtonY(height, height - 48, 20, 4);
        int closeY = ScreenLayout.clampButtonY(height, height - 24, 20, 4);

        check(x >= 0 && x + buttonWidth <= width, "Control Center button width is inside screen");
        check(pages * rows >= 6, "Control Center pages expose every action");
        for (int action = 0; action < 6; action++) {
            int row = action % rows;
            int y = 56 + row * 22;
            check(y >= 0 && y + 20 <= navigationY, "Control Center action does not overlap navigation");
        }
        check(closeY >= navigationY + 20, "Control Center Close does not overlap page navigation");
        check(closeY + 20 <= height - 4, "Control Center Close remains visible");
    }

    private static void checkOptimizationPagination(int width, int height) {
        int buttonWidth = ScreenLayout.centeredWidth(width, 340, 12);
        int x = (width - buttonWidth) / 2;
        int refreshY = ScreenLayout.clampButtonY(height, height - 78, 20, 4);
        int rows = Math.max(1, (refreshY - 54) / 24);
        int pages = ScreenLayout.pageCount(5, rows);
        int navigationY = ScreenLayout.clampButtonY(height, height - 52, 20, 4);
        int backY = ScreenLayout.clampButtonY(height, height - 26, 20, 4);

        check(x >= 0 && x + buttonWidth <= width, "Optimization buttons are inside screen");
        check(pages * rows >= 5, "Optimization pages expose all five registry entries");
        for (int row = 0; row < rows && row < 5; row++) {
            int y = 54 + row * 24;
            check(y + 20 <= refreshY, "Optimization row does not overlap Refresh");
        }
        check(refreshY + 20 <= navigationY, "Refresh does not overlap page navigation");
        check(navigationY + 20 <= backY, "Page navigation does not overlap Back");
        check(backY + 20 <= height - 4, "Optimization Back remains visible");
    }

    private static void checkAutoTune(int width, int height) {
        int buttonWidth = ScreenLayout.centeredWidth(width, 320, 12);
        int actionY = 130;
        int backY = ScreenLayout.clampButtonY(height, height - 26, 20, 4);

        for (int row = 0; row < 3; row++) {
            int y = 64 + row * 22;
            check(y + 20 <= actionY, "Auto-tune profile does not overlap Apply/Undo");
        }
        check(actionY + 20 <= backY, "Auto-tune Apply/Undo do not overlap Back");
        check(backY + 20 <= height - 4, "Auto-tune Back remains visible");
        check(buttonWidth > 0, "Auto-tune buttons have a positive width");
    }

    private static void checkMemory(int width, int height) {
        int y = Math.max(100, Math.min(height / 2 - 20, height - 82));
        int backY = ScreenLayout.clampButtonY(height, height - 26, 20, 4);
        check(y >= 0 && y + 20 <= height, "Memory enable control has valid bounds");
        check(y + 48 <= backY, "Memory threshold does not overlap Back");
        check(88 + 9 <= y, "Memory status text does not overlap controls");
        check(ScreenLayout.centeredWidth(width, 300, 12) > 0, "Memory controls have positive width");
    }

    private static void checkPerformance(int height) {
        int top = 28;
        int lines = 11;
        int lineStep = ScreenLayout.fitLineStep(height, top, 30, lines, 14, 10);
        int lastLineBottom = top + (lines - 1) * lineStep + 9;
        int backY = ScreenLayout.clampButtonY(height, height - 26, 20, 4);

        check(lineStep >= 10 && lineStep <= 14, "Performance metric spacing stays readable");
        check(lastLineBottom <= backY - 2, "Performance metrics remain above Back");
    }

    private static void checkResultScreen(int width, int height) {
        int buttonWidth = ScreenLayout.centeredWidth(width, 180, 12);
        int buttonY = ScreenLayout.clampButtonY(height, height - 26, 20, 4);
        check(buttonWidth > 0 && buttonWidth <= width, "Auto-tune result Back width is valid");
        check(buttonY + 20 <= height - 4, "Auto-tune result Back remains visible");
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }
}
