package taskmanager.service;

import taskmanager.model.RecurringTask;
import taskmanager.model.Task;
import taskmanager.model.Task.Priority;
import taskmanager.model.Task.Status;
import taskmanager.util.FileHandler;
import taskmanager.util.TaskException;
import taskmanager.util.TaskException.DataAccessException;
import taskmanager.util.TaskException.InvalidTaskException;
import taskmanager.util.TaskException.TaskNotFoundException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business-logic layer for task operations.
 * All UI components interact with the application through this service.
 */
public class TaskService {

    private final List<Task>    tasks       = new ArrayList<>();
    private final FileHandler   fileHandler;

    // Observers (UI panels) notified after every state change
    private final List<Runnable> changeListeners = new ArrayList<>();

    // ── Constructor ──────────────────────────────────────────────────────────

    public TaskService(String dataFilePath) throws DataAccessException {
        this.fileHandler = new FileHandler(dataFilePath);
        tasks.addAll(fileHandler.loadTasks());
    }

    // ── Listener registration ────────────────────────────────────────────────

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable r : changeListeners) r.run();
    }

    // ── CRUD operations ──────────────────────────────────────────────────────

    /**
     * Adds a new plain task.
     *
     * @throws InvalidTaskException if title is blank or deadline is in the past
     */
    public void addTask(String title, String description,
                        Priority priority, LocalDate deadline)
            throws InvalidTaskException, DataAccessException {

        validate(title, deadline);
        tasks.add(new Task(title, description, priority, deadline));
        persist();
        notifyListeners();
    }

    /**
     * Adds a new recurring task.
     *
     * @throws InvalidTaskException if title is blank, deadline is in the past,
     *                              or repeatEveryDays < 1
     */
    public void addRecurringTask(String title, String description,
                                 Priority priority, LocalDate deadline,
                                 int repeatEveryDays)
            throws InvalidTaskException, DataAccessException {

        validate(title, deadline);
        if (repeatEveryDays < 1)
            throw new InvalidTaskException("Repeat interval must be at least 1 day.");
        tasks.add(new RecurringTask(title, description, priority, deadline, repeatEveryDays));
        persist();
        notifyListeners();
    }

    /**
     * Updates an existing task's fields.
     *
     * @throws TaskNotFoundException if no task has the given id
     * @throws InvalidTaskException  if the new values are invalid
     */
    public void updateTask(int id, String title, String description,
                           Priority priority, LocalDate deadline)
            throws TaskException, DataAccessException {

        Task t = findById(id);
        validate(title, deadline);
        t.setTitle(title);
        t.setDescription(description);
        t.setPriority(priority);
        t.setDeadline(deadline);
        persist();
        notifyListeners();
    }

    /**
     * Deletes a task by ID.
     *
     * @throws TaskNotFoundException if no task has the given id
     */
    public void deleteTask(int id) throws TaskNotFoundException, DataAccessException {
        Task t = findById(id);
        tasks.remove(t);
        persist();
        notifyListeners();
    }

    /**
     * Marks a task COMPLETED.
     * For RecurringTask, advances the deadline instead of completing it.
     *
     * @throws TaskNotFoundException if no task has the given id
     */
    public void markComplete(int id) throws TaskNotFoundException, DataAccessException {
        Task t = findById(id);
        if (t instanceof RecurringTask) {
            ((RecurringTask) t).scheduleNext();
        } else {
            t.setStatus(Status.COMPLETED);
        }
        persist();
        notifyListeners();
    }

    /**
     * Sets a task status explicitly.
     *
     * @throws TaskNotFoundException if no task has the given id
     */
    public void setStatus(int id, Status status) throws TaskNotFoundException, DataAccessException {
        Task t = findById(id);
        t.setStatus(status);
        persist();
        notifyListeners();
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    /** Returns a copy of all tasks. */
    public List<Task> getAllTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /** Returns tasks sorted by priority (HIGH first), then deadline. */
    public List<Task> getTasksSortedByPriority() {
        return tasks.stream()
                .sorted(Comparator
                        .comparingInt((Task t) -> t.getPriority().ordinal())
                        .thenComparing(Task::getDeadline))
                .collect(Collectors.toList());
    }

    /** Returns tasks sorted by deadline (earliest first). */
    public List<Task> getTasksSortedByDeadline() {
        return tasks.stream()
                .sorted(Comparator.comparing(Task::getDeadline))
                .collect(Collectors.toList());
    }

    /** Returns tasks whose deadline is within the next {@code days} days and are not completed. */
    public List<Task> getUpcomingTasks(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return tasks.stream()
                .filter(t -> t.getStatus() != Status.COMPLETED)
                .filter(t -> !t.getDeadline().isAfter(cutoff))
                .sorted(Comparator.comparing(Task::getDeadline))
                .collect(Collectors.toList());
    }

    /** Returns all tasks that are overdue and not completed. */
    public List<Task> getOverdueTasks() {
        return tasks.stream()
                .filter(Task::isOverdue)
                .collect(Collectors.toList());
    }

    /** Returns tasks whose deadline is today or within the next 2 days. */
    public List<Task> getDueSoonTasks() {
        return tasks.stream()
                .filter(Task::isDueSoon)
                .collect(Collectors.toList());
    }

    /** Finds a task by ID, throws TaskNotFoundException if absent. */
    public Task findById(int id) throws TaskNotFoundException {
        return tasks.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private void validate(String title, LocalDate deadline) throws InvalidTaskException {
        if (title == null || title.trim().isEmpty())
            throw new InvalidTaskException("Task title cannot be empty.");
        if (deadline == null)
            throw new InvalidTaskException("Deadline must be specified.");
    }

    private void persist() throws DataAccessException {
        fileHandler.saveTasks(tasks);
    }
}
