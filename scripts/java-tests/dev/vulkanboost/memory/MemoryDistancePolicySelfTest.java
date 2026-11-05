package dev.vulkanboost.memory;

/** Dependency-free regression tests for SmartMemoryManager distance reductions. */
public final class MemoryDistancePolicySelfTest {
    private static int checks;

    private MemoryDistancePolicySelfTest() {
    }

    public static void main(String[] args) {
        check(1, 2, 4, 1);
        check(2, 2, 4, 2);
        check(3, 2, 4, 3);
        check(4, 2, 4, 4);
        check(5, 2, 4, 4);
        check(8, 2, 4, 6);
        check(3, 1, 2, 2);
        check(2, 1, 2, 2);
        check(1, 1, 2, 1);
        check(10, 1, 2, 9);
        System.out.println("MemoryDistancePolicy: " + checks + " checks passed.");
    }

    private static void check(int current, int amount, int minimum, int expected) {
        int actual = MemoryDistancePolicy.reduce(current, amount, minimum);
        if (actual != expected) {
            throw new AssertionError("Expected " + expected + " but got " + actual
                    + " for current=" + current + ", amount=" + amount + ", minimum=" + minimum);
        }
        if (actual > current) {
            throw new AssertionError("Memory assistance must never increase a distance.");
        }
        checks++;
    }
}
