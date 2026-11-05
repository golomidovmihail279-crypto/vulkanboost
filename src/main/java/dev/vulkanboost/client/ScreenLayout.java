package dev.vulkanboost.client;

/**
 * Small, dependency-free layout calculations shared by the Control Center screens.
 * Keeping the calculations here makes the small-screen constraints testable in CI.
 */
public final class ScreenLayout {
    private ScreenLayout() {
    }

    public static int centeredWidth(int screenWidth, int preferredWidth, int horizontalMargin) {
        return Math.max(1, Math.min(preferredWidth, screenWidth - 2 * horizontalMargin));
    }

    public static int rowsPerPage(int screenHeight, int contentTop, int footerHeight, int rowPitch) {
        return Math.max(1, (screenHeight - contentTop - footerHeight) / Math.max(1, rowPitch));
    }

    public static int pageCount(int itemCount, int pageSize) {
        int safePageSize = Math.max(1, pageSize);
        return Math.max(1, (Math.max(0, itemCount) + safePageSize - 1) / safePageSize);
    }

    public static int clampPage(int page, int pageCount) {
        return Math.max(0, Math.min(page, Math.max(1, pageCount) - 1));
    }

    public static int clampButtonY(int screenHeight, int desiredY, int buttonHeight, int margin) {
        int safeMargin = Math.max(0, margin);
        int maximumY = Math.max(safeMargin, screenHeight - buttonHeight - safeMargin);
        return Math.max(safeMargin, Math.min(desiredY, maximumY));
    }

    public static int fitLineStep(
            int screenHeight,
            int contentTop,
            int bottomReserve,
            int lineCount,
            int preferredStep,
            int minimumStep
    ) {
        int safeLineCount = Math.max(1, lineCount);
        int available = screenHeight - contentTop - bottomReserve;
        if (available <= 0) {
            return minimumStep;
        }
        return Math.max(minimumStep, Math.min(preferredStep, available / safeLineCount));
    }
}
