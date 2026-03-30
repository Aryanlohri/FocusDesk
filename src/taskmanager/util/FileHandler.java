package taskmanager.util;

import taskmanager.model.RecurringTask;
import taskmanager.model.Task;
import taskmanager.util.TaskException.DataAccessException;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading and writing tasks to a plain-text CSV file.
 * Demonstrates: File I/O and exception handling.
 *
 * File format (one task per line):
 *   Normal task  : id|title|description|priority|status|deadline
 *   Recurring    : R|id|title|description|priority|status|deadline|repeatDays
 */
public class FileHandler {

    private final String filePath;

    public FileHandler(String filePath) {
        this.filePath = filePath;
        ensureFileExists();
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Loads all tasks from the data file.
     *
     * @return list of Task objects (may be empty, never null)
     * @throws DataAccessException if the file cannot be read
     */
    public List<Task> loadTasks() throws DataAccessException {
        List<Task> tasks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    if (line.startsWith("R|")) {
                        tasks.add(RecurringTask.fromCsv(line));
                    } else {
                        tasks.add(Task.fromCsv(line));
                    }
                } catch (Exception e) {
                    // Skip corrupted lines silently — do not crash the whole app
                    System.err.println("Skipping corrupted line: " + line);
                }
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to read tasks from file: " + filePath, e);
        }
        return tasks;
    }

    /**
     * Saves all tasks to the data file, overwriting previous content.
     *
     * @param tasks list of tasks to persist
     * @throws DataAccessException if the file cannot be written
     */
    public void saveTasks(List<Task> tasks) throws DataAccessException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            for (Task t : tasks) {
                writer.write(t.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to save tasks to file: " + filePath, e);
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void ensureFileExists() {
        try {
            Path path = Paths.get(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create data file – " + e.getMessage());
        }
    }
}
