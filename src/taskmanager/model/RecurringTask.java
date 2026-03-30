package taskmanager.model;

import java.time.LocalDate;

/**
 * A Task that repeats on a fixed interval (days).
 * Demonstrates OOP: inheritance and method overriding.
 */
public class RecurringTask extends Task {

    private final int repeatEveryDays;   // e.g. 7 = weekly

    // ── Constructor ──────────────────────────────────────────────────────────

    public RecurringTask(String title, String description,
                         Priority priority, LocalDate deadline,
                         int repeatEveryDays) {
        super(title, description, priority, deadline);
        this.repeatEveryDays = repeatEveryDays;
    }

    /** Constructor used when loading from file. */
    public RecurringTask(int id, String title, String description,
                         Priority priority, Status status,
                         LocalDate deadline, int repeatEveryDays) {
        super(id, title, description, priority, status, deadline);
        this.repeatEveryDays = repeatEveryDays;
    }

    // ── Getter ───────────────────────────────────────────────────────────────

    public int getRepeatEveryDays() { return repeatEveryDays; }

    /**
     * Advances the deadline by repeatEveryDays and resets status to PENDING.
     * Call this when the task is completed to schedule the next occurrence.
     */
    public void scheduleNext() {
        setDeadline(getDeadline().plusDays(repeatEveryDays));
        setStatus(Status.PENDING);
    }

    // ── Serialisation ────────────────────────────────────────────────────────

    @Override
    public String toCsv() {
        // prefix "R" to distinguish from plain Task rows
        return "R|" + super.toCsv() + "|" + repeatEveryDays;
    }

    public static RecurringTask fromCsv(String csv) {
        // format: R|id|title|desc|priority|status|deadline|repeatDays
        String[] p = csv.split("\\|", -1);
        return new RecurringTask(
                Integer.parseInt(p[1]),
                p[2],
                p[3],
                Priority.valueOf(p[4]),
                Status.valueOf(p[5]),
                LocalDate.parse(p[6], DATE_FORMATTER),
                Integer.parseInt(p[7]));
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Repeats every %d day(s)", repeatEveryDays);
    }
}
