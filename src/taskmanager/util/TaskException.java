package taskmanager.util;

/**
 * Custom exceptions for the Task Manager application.
 * Demonstrates Exception Handling concepts.
 */
public class TaskException extends Exception {

    // ── Subclasses ───────────────────────────────────────────────────────────

    /** Thrown when task input validation fails (empty title, past deadline, etc.). */
    public static class InvalidTaskException extends TaskException {
        public InvalidTaskException(String message) {
            super(message);
        }
    }

    /** Thrown when a task with a given ID cannot be found. */
    public static class TaskNotFoundException extends TaskException {
        public TaskNotFoundException(int id) {
            super("Task with ID " + id + " was not found.");
        }
    }

    /** Thrown when the data file cannot be read or written. */
    public static class DataAccessException extends TaskException {
        public DataAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    public TaskException(String message) {
        super(message);
    }

    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
