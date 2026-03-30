package taskmanager.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Base class representing a Task.
 * Demonstrates OOP: encapsulation, constructors, getters/setters.
 */
public class Task {

    // ── Enums ────────────────────────────────────────────────────────────────

    public enum Priority { HIGH, MEDIUM, LOW }

    public enum Status { PENDING, IN_PROGRESS, COMPLETED }

    // ── Fields ───────────────────────────────────────────────────────────────

    private static int idCounter = 1;

    private final int id;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private LocalDate deadline;

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ── Constructors ─────────────────────────────────────────────────────────

    public Task(String title, String description, Priority priority, LocalDate deadline) {
        this.id          = idCounter++;
        this.title       = title;
        this.description = description;
        this.priority    = priority;
        this.deadline    = deadline;
        this.status      = Status.PENDING;
    }

    /** Constructor used when loading from file (id already known). */
    public Task(int id, String title, String description,
                Priority priority, Status status, LocalDate deadline) {
        this.id          = id;
        this.title       = title;
        this.description = description;
        this.priority    = priority;
        this.status      = status;
        this.deadline    = deadline;
        if (id >= idCounter) idCounter = id + 1;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public int       getId()          { return id; }
    public String    getTitle()       { return title; }
    public String    getDescription() { return description; }
    public Priority  getPriority()    { return priority; }
    public Status    getStatus()      { return status; }
    public LocalDate getDeadline()    { return deadline; }

    public void setTitle(String title)             { this.title       = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(Priority priority)     { this.priority    = priority; }
    public void setStatus(Status status)           { this.status      = status; }
    public void setDeadline(LocalDate deadline)    { this.deadline    = deadline; }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public boolean isOverdue() {
        return status != Status.COMPLETED && deadline.isBefore(LocalDate.now());
    }

    public boolean isDueSoon() {
        LocalDate today = LocalDate.now();
        return status != Status.COMPLETED
                && !deadline.isBefore(today)
                && !deadline.isAfter(today.plusDays(2));
    }

    /** Serialise to CSV line for file persistence. */
    public String toCsv() {
        return String.join("|",
                String.valueOf(id),
                title,
                description,
                priority.name(),
                status.name(),
                deadline.format(DATE_FORMATTER));
    }

    /** Deserialise from CSV line. */
    public static Task fromCsv(String csv) {
        String[] p = csv.split("\\|", -1);
        return new Task(
                Integer.parseInt(p[0]),
                p[1],
                p[2],
                Priority.valueOf(p[3]),
                Status.valueOf(p[4]),
                LocalDate.parse(p[5], DATE_FORMATTER));
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s | Due: %s",
                id, title, priority, status, deadline.format(DATE_FORMATTER));
    }
}
