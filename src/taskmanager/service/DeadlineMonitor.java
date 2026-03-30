package taskmanager.service;

import taskmanager.model.Task;

import java.util.List;
import java.util.function.Consumer;

/**
 * Background daemon thread that polls the TaskService every minute
 * and fires a callback when tasks are due soon or overdue.
 *
 * Demonstrates: Multithreading with a daemon thread, Runnable,
 * thread sleep, and safe inter-thread communication via callbacks.
 */
public class DeadlineMonitor implements Runnable {

    private static final long POLL_INTERVAL_MS = 60_000; // 1 minute

    private final TaskService          taskService;
    private final Consumer<List<Task>> alertCallback;  // called on the monitor thread
    private volatile boolean           running = true;
    private Thread                     thread;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * @param taskService   the service to poll
     * @param alertCallback invoked with the list of tasks needing attention;
     *                      the UI must use SwingUtilities.invokeLater() internally
     */
    public DeadlineMonitor(TaskService taskService,
                           Consumer<List<Task>> alertCallback) {
        this.taskService   = taskService;
        this.alertCallback = alertCallback;
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    /** Starts the monitor on a new daemon thread. */
    public void start() {
        thread = new Thread(this, "DeadlineMonitorThread");
        thread.setDaemon(true);   // JVM exits even if this thread is still running
        thread.start();
        System.out.println("[DeadlineMonitor] Started — polling every 60 seconds.");
    }

    /** Signals the monitor to stop at the next poll cycle. */
    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
        System.out.println("[DeadlineMonitor] Stopped.");
    }

    // ── Runnable ─────────────────────────────────────────────────────────────

    @Override
    public void run() {
        while (running) {
            try {
                checkDeadlines();
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void checkDeadlines() {
        List<Task> alerts = taskService.getDueSoonTasks();
        alerts.addAll(taskService.getOverdueTasks());

        // Deduplicate (a task can appear in both lists)
        List<Task> unique = alerts.stream()
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        if (!unique.isEmpty()) {
            alertCallback.accept(unique);
        }
    }
}
