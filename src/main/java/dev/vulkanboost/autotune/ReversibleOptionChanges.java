package dev.vulkanboost.autotune;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Applies integer-option changes as a transaction and restores only values
 * that still match the value written by the transaction.
 *
 * This class is independent of Minecraft so rollback behavior can be tested
 * deterministically with a fake settings provider.
 */
public final class ReversibleOptionChanges {
    public interface Access {
        OptionalInt read(String optionName);
        boolean write(String optionName, int value);
        void save() throws ReflectiveOperationException;
    }

    public record Change(String optionName, int originalValue, int appliedValue) {
    }

    public record Snapshot(List<Change> changes) {
        public Snapshot {
            changes = List.copyOf(changes);
        }
        public boolean isEmpty() {
            return changes.isEmpty();
        }
    }

    public record RestoreResult(boolean complete, int restoredValues, int preservedValues) {
    }

    /** Carries a snapshot when rollback could not finish, allowing a retry. */
    public static final class ApplyException extends RuntimeException {
        private final Snapshot snapshot;
        private final boolean rollbackComplete;

        private ApplyException(String message, Throwable cause, Snapshot snapshot, boolean rollbackComplete) {
            super(message, cause);
            this.snapshot = snapshot;
            this.rollbackComplete = rollbackComplete;
        }

        public Snapshot snapshot() {
            return snapshot;
        }
        public boolean rollbackComplete() {
            return rollbackComplete;
        }
    }

    private ReversibleOptionChanges() {
    }

    public static Snapshot apply(Access access, Map<String, Integer> targets, boolean requireAllOptions) {
        Objects.requireNonNull(access, "access");
        Objects.requireNonNull(targets, "targets");

        List<Change> changes = new ArrayList<>();
        for (Map.Entry<String, Integer> target : targets.entrySet()) {
            String optionName = Objects.requireNonNull(target.getKey(), "optionName");
            int targetValue = Objects.requireNonNull(target.getValue(), "targetValue");
            OptionalInt current = access.read(optionName);

            if (current.isEmpty()) {
                if (requireAllOptions) {
                    throw new IllegalStateException("Option is unavailable: " + optionName);
                }
                continue;
            }
            if (current.getAsInt() != targetValue) {
                changes.add(new Change(optionName, current.getAsInt(), targetValue));
            }
        }

        Snapshot snapshot = new Snapshot(changes);
        try {
            for (Change change : snapshot.changes()) {
                if (!access.write(change.optionName(), change.appliedValue())) {
                    throw new IllegalStateException("Could not change option: " + change.optionName());
                }
            }
            if (!snapshot.isEmpty()) {
                access.save();
            }
            return snapshot;
        } catch (ReflectiveOperationException | RuntimeException failure) {
            boolean rollbackComplete = false;
            try {
                RestoreResult rollback = restoreIfUnchanged(access, snapshot);
                rollbackComplete = rollback.complete();
                if (!rollbackComplete) {
                    failure.addSuppressed(new IllegalStateException(
                            "Automatic rollback is incomplete; the snapshot must be retained for retry."
                    ));
                }
            } catch (ReflectiveOperationException | RuntimeException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            throw new ApplyException(
                    "Could not apply option changes. Rollback complete: " + rollbackComplete,
                    failure, snapshot, rollbackComplete
            );
        }
    }

    /**
     * Reads every value before writing. If a setting cannot be read, restoration
     * remains pending. The caller should retain the snapshot and retry later.
     */
    public static RestoreResult restoreIfUnchanged(Access access, Snapshot snapshot)
            throws ReflectiveOperationException {
        Objects.requireNonNull(access, "access");
        Objects.requireNonNull(snapshot, "snapshot");

        if (snapshot.isEmpty()) {
            return new RestoreResult(true, 0, 0);
        }

        Map<String, Integer> currentValues = new LinkedHashMap<>();
        for (Change change : snapshot.changes()) {
            OptionalInt current = access.read(change.optionName());
            if (current.isEmpty()) {
                return new RestoreResult(false, 0, 0);
            }
            currentValues.put(change.optionName(), current.getAsInt());
        }

        int restored = 0;
        int preserved = 0;
        for (Change change : snapshot.changes()) {
            int current = currentValues.get(change.optionName());
            if (current == change.appliedValue()) {
                if (!access.write(change.optionName(), change.originalValue())) {
                    return new RestoreResult(false, restored, preserved);
                }
                restored++;
            } else if (current != change.originalValue()) {
                preserved++;
            }
        }

        // Save also on retries where an earlier attempt restored values but
        // failed while persisting the options file.
        access.save();
        return new RestoreResult(true, restored, preserved);
    }

    public static boolean stillAtAppliedValues(Access access, Snapshot snapshot) {
        Objects.requireNonNull(access, "access");
        Objects.requireNonNull(snapshot, "snapshot");

        for (Change change : snapshot.changes()) {
            OptionalInt current = access.read(change.optionName());
            if (current.isEmpty() || current.getAsInt() != change.appliedValue()) {
                return false;
            }
        }
        return true;
    }
}
