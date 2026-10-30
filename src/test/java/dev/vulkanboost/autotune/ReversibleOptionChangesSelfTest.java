package dev.vulkanboost.autotune;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Dependency-free regression tests for the option transaction helper.
 * Run by GitHub Actions with the Java 21 toolchain.
 */
public final class ReversibleOptionChangesSelfTest {
    private static final class FakeAccess implements ReversibleOptionChanges.Access {
        private final Map<String, Integer> values = new HashMap<>();
        private final Set<String> unavailable = new HashSet<>();
        private String failWriteOption;
        private String hideOptionAfterFailedWrite;
        private int failSaveCount;

        private FakeAccess(int render, int simulation) {
            values.put("renderDistance", render);
            values.put("simulationDistance", simulation);
        }

        @Override
        public OptionalInt read(String optionName) {
            if (unavailable.contains(optionName)) {
                return OptionalInt.empty();
            }
            Integer value = values.get(optionName);
            return value == null ? OptionalInt.empty() : OptionalInt.of(value);
        }

        @Override
        public boolean write(String optionName, int value) {
            if (optionName.equals(failWriteOption)) {
                failWriteOption = null;
                if (hideOptionAfterFailedWrite != null) {
                    unavailable.add(hideOptionAfterFailedWrite);
                }
                return false;
            }
            values.put(optionName, value);
            return true;
        }

        @Override
        public void save() throws ReflectiveOperationException {
            if (failSaveCount > 0) {
                failSaveCount--;
                throw new ReflectiveOperationException("Synthetic save failure");
            }
        }

        private int value(String optionName) {
            return values.get(optionName);
        }
    }

    private static int checks;

    private ReversibleOptionChangesSelfTest() {
    }

    public static void main(String[] args) {
        successfulApplyAndRestore();
        manualChangesArePreserved();
        partialWriteFailureRollsBackEarlierWrites();
        failedSaveRollsBackAppliedValues();
        failedRollbackSaveCanBeRetried();
        incompleteRollbackCanBeRetried();
        alreadyRestoredValuesAreNotOverwritten();
        System.out.println("ReversibleOptionChanges: " + checks + " checks passed.");
    }

    private static void successfulApplyAndRestore() {
        FakeAccess access = new FakeAccess(16, 8);
        ReversibleOptionChanges.Snapshot snapshot = ReversibleOptionChanges.apply(
                access, targets(12, 6), true
        );
        check(access.value("renderDistance") == 12, "render distance applied");
        check(access.value("simulationDistance") == 6, "simulation distance applied");

        ReversibleOptionChanges.RestoreResult result =
                ReversibleOptionChanges.restoreIfUnchanged(access, snapshot);
        check(result.complete(), "normal restore completes");
        check(result.restoredValues() == 2, "both options restored");
        check(access.value("renderDistance") == 16, "render distance returns to original");
        check(access.value("simulationDistance") == 8, "simulation distance returns to original");
    }

    private static void manualChangesArePreserved() {
        FakeAccess access = new FakeAccess(16, 8);
        ReversibleOptionChanges.Snapshot snapshot = ReversibleOptionChanges.apply(
                access, targets(12, 6), true
        );
        access.values.put("renderDistance", 20);

        ReversibleOptionChanges.RestoreResult result =
                ReversibleOptionChanges.restoreIfUnchanged(access, snapshot);
        check(result.complete(), "restore completes after manual override");
        check(result.preservedValues() == 1, "manual override is detected");
        check(access.value("renderDistance") == 20, "manual render distance is preserved");
        check(access.value("simulationDistance") == 8, "unchanged simulation distance is restored");
    }

    private static void partialWriteFailureRollsBackEarlierWrites() {
        FakeAccess access = new FakeAccess(16, 8);
        access.failWriteOption = "simulationDistance";

        ReversibleOptionChanges.ApplyException failure = expectApplyFailure(
                () -> ReversibleOptionChanges.apply(access, targets(12, 6), true)
        );
        check(failure.rollbackComplete(), "partial write rollback completes");
        check(access.value("renderDistance") == 16, "first write is rolled back");
        check(access.value("simulationDistance") == 8, "failed second write remains unchanged");
    }

    private static void failedSaveRollsBackAppliedValues() {
        FakeAccess access = new FakeAccess(16, 8);
        access.failSaveCount = 1;

        ReversibleOptionChanges.ApplyException failure = expectApplyFailure(
                () -> ReversibleOptionChanges.apply(access, targets(12, 6), true)
        );
        check(failure.rollbackComplete(), "save failure rollback completes");
        check(access.value("renderDistance") == 16, "render distance rolled back after save failure");
        check(access.value("simulationDistance") == 8, "simulation distance rolled back after save failure");
    }

    private static void failedRollbackSaveCanBeRetried() {
        FakeAccess access = new FakeAccess(16, 8);
        access.failSaveCount = 2;

        ReversibleOptionChanges.ApplyException failure = expectApplyFailure(
                () -> ReversibleOptionChanges.apply(access, targets(12, 6), true)
        );
        check(!failure.rollbackComplete(), "failed rollback save is reported as pending");
        check(access.value("renderDistance") == 16, "distance values are rolled back before retrying save");
        check(access.value("simulationDistance") == 8, "both values are restored before retrying save");

        ReversibleOptionChanges.RestoreResult retry =
                ReversibleOptionChanges.restoreIfUnchanged(access, failure.snapshot());
        check(retry.complete(), "retry persists values already restored in memory");
        check(access.value("renderDistance") == 16, "retry keeps original render distance");
        check(access.value("simulationDistance") == 8, "retry keeps original simulation distance");
    }

    private static void incompleteRollbackCanBeRetried() {
        FakeAccess access = new FakeAccess(16, 8);
        access.failWriteOption = "simulationDistance";
        access.hideOptionAfterFailedWrite = "renderDistance";

        ReversibleOptionChanges.ApplyException failure = expectApplyFailure(
                () -> ReversibleOptionChanges.apply(access, targets(12, 6), true)
        );
        check(!failure.rollbackComplete(), "unreadable setting keeps rollback pending");
        check(!failure.snapshot().isEmpty(), "pending snapshot remains available");

        access.unavailable.remove("renderDistance");
        ReversibleOptionChanges.RestoreResult retry =
                ReversibleOptionChanges.restoreIfUnchanged(access, failure.snapshot());
        check(retry.complete(), "pending rollback succeeds on retry");
        check(access.value("renderDistance") == 16, "retry restores first option");
        check(access.value("simulationDistance") == 8, "retry leaves untouched second option unchanged");
    }

    private static void alreadyRestoredValuesAreNotOverwritten() {
        FakeAccess access = new FakeAccess(16, 8);
        ReversibleOptionChanges.Snapshot snapshot = ReversibleOptionChanges.apply(
                access, targets(12, 6), true
        );
        access.values.put("renderDistance", 16);

        ReversibleOptionChanges.RestoreResult result =
                ReversibleOptionChanges.restoreIfUnchanged(access, snapshot);
        check(result.complete(), "partly restored snapshot completes");
        check(result.preservedValues() == 0, "original values are not treated as manual overrides");
        check(access.value("renderDistance") == 16, "already restored value stays unchanged");
        check(access.value("simulationDistance") == 8, "remaining value is restored");
    }

    private static Map<String, Integer> targets(int render, int simulation) {
        Map<String, Integer> values = new LinkedHashMap<>();
        values.put("renderDistance", render);
        values.put("simulationDistance", simulation);
        return values;
    }

    private static ReversibleOptionChanges.ApplyException expectApplyFailure(Runnable action) {
        try {
            action.run();
        } catch (ReversibleOptionChanges.ApplyException expected) {
            checks++;
            return expected;
        }
        throw new AssertionError("Expected an apply failure");
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }
}
