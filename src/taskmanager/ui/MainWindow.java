package taskmanager.ui;

import taskmanager.model.Task;
import taskmanager.model.Task.Status;
import taskmanager.service.DeadlineMonitor;
import taskmanager.service.TaskService;
import taskmanager.ui.TaskFormDialog.TaskFormResult;
import taskmanager.util.TaskException;
import taskmanager.util.TaskException.DataAccessException;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Main application window.
 * Holds the toolbar, task table, status bar, and wires up all interactions.
 */
public class MainWindow extends JFrame {

    // ── Constants ─────────────────────────────────────────────────────────────

    private static final String DATA_FILE = "data/tasks.dat";

    // ── Components ─────────────────────────────────────────────────────────────

    private final TaskService       taskService;
    private final DeadlineMonitor   monitor;
    private final TaskTableModel    tableModel = new TaskTableModel();
    private final JTable            table      = new JTable(tableModel);
    private final JLabel            statusBar  = new JLabel(" Ready");
    private final JComboBox<String> filterBox  =
            new JComboBox<>(new String[]{ "All", "Pending", "In Progress", "Completed", "Overdue", "Due Soon" });

    // ── Constructor ───────────────────────────────────────────────────────────

    public MainWindow() throws DataAccessException {
        super("📋 Personal Task Manager");
        taskService = new TaskService(DATA_FILE);

        // Register the UI refresh listener with the service
        taskService.addChangeListener(this::refreshTable);

        // Start the background deadline monitor
        monitor = new DeadlineMonitor(taskService, this::showDeadlineAlert);
        monitor.start();

        buildUI();
        refreshTable();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                monitor.stop();
                dispose();
                System.exit(0);
            }
        });

        setMinimumSize(new Dimension(860, 540));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── UI construction ───────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout());

        // ── Toolbar ──────────────────────────────────────────────────────────
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        JButton addBtn    = makeButton("➕ Add Task",    "Add a new task");
        JButton editBtn   = makeButton("✏️ Edit",        "Edit selected task");
        JButton deleteBtn = makeButton("🗑️ Delete",      "Delete selected task");
        JButton doneBtn   = makeButton("✅ Mark Done",   "Mark selected task as complete");
        JButton refreshBtn= makeButton("🔄 Refresh",    "Refresh task list");

        addBtn.addActionListener(e    -> onAddTask());
        editBtn.addActionListener(e   -> onEditTask());
        deleteBtn.addActionListener(e -> onDeleteTask());
        doneBtn.addActionListener(e   -> onMarkDone());
        refreshBtn.addActionListener(e-> refreshTable());

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.addSeparator();
        toolbar.add(doneBtn);
        toolbar.addSeparator();

        // Sort buttons
        JButton sortPriBtn  = makeButton("Sort: Priority", "Sort by priority");
        JButton sortDateBtn = makeButton("Sort: Deadline", "Sort by deadline");
        sortPriBtn.addActionListener(e  -> showSorted("priority"));
        sortDateBtn.addActionListener(e -> showSorted("deadline"));
        toolbar.add(sortPriBtn);
        toolbar.add(sortDateBtn);
        toolbar.addSeparator();

        // Filter combo
        toolbar.add(new JLabel("  Filter: "));
        filterBox.addActionListener(e -> refreshTable());
        toolbar.add(filterBox);

        toolbar.addSeparator();
        toolbar.add(refreshBtn);
        add(toolbar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        TaskTableRenderer renderer = new TaskTableRenderer(tableModel);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Column widths
        int[] widths = { 40, 240, 80, 100, 100, 80 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowSorter(new TableRowSorter<>(tableModel));

        // Double-click to edit
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) onEditTask();
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Status bar ────────────────────────────────────────────────────────
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        add(statusBar, BorderLayout.SOUTH);
    }

    // ── Action handlers ───────────────────────────────────────────────────────

    private void onAddTask() {
        TaskFormDialog dialog = new TaskFormDialog(this, "Add New Task");
        dialog.setVisible(true);
        TaskFormResult r = dialog.getResult();
        if (r == null) return;

        try {
            if (r.isRecurring) {
                taskService.addRecurringTask(r.title, r.description,
                        r.priority, r.deadline, r.repeatDays);
            } else {
                taskService.addTask(r.title, r.description, r.priority, r.deadline);
            }
            setStatus("Task \"" + r.title + "\" added.");
        } catch (TaskException ex) {
            showError("Could not add task: " + ex.getMessage());
        }
    }

    private void onEditTask() {
        Task selected = getSelectedTask();
        if (selected == null) return;

        TaskFormDialog dialog = new TaskFormDialog(this, "Edit Task");
        dialog.populate(selected);
        dialog.setVisible(true);

        TaskFormResult r = dialog.getResult();
        if (r == null) return;

        try {
            taskService.updateTask(selected.getId(),
                    r.title, r.description, r.priority, r.deadline);
            setStatus("Task updated.");
        } catch (TaskException ex) {
            showError("Could not update task: " + ex.getMessage());
        }
    }

    private void onDeleteTask() {
        Task selected = getSelectedTask();
        if (selected == null) return;

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete task: \"" + selected.getTitle() + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        try {
            taskService.deleteTask(selected.getId());
            setStatus("Task deleted.");
        } catch (TaskException ex) {
            showError("Could not delete task: " + ex.getMessage());
        }
    }

    private void onMarkDone() {
        Task selected = getSelectedTask();
        if (selected == null) return;

        try {
            taskService.markComplete(selected.getId());
            setStatus("Task marked as complete.");
        } catch (TaskException ex) {
            showError("Could not update task: " + ex.getMessage());
        }
    }

    private void showSorted(String mode) {
        List<Task> sorted = mode.equals("priority")
                ? taskService.getTasksSortedByPriority()
                : taskService.getTasksSortedByDeadline();
        tableModel.setTasks(sorted);
        setStatus("Sorted by " + mode + ".");
    }

    // ── Table refresh ─────────────────────────────────────────────────────────

    private void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            String filter = (String) filterBox.getSelectedItem();
            List<Task> toShow = switch (filter) {
                case "Pending"     -> taskService.getAllTasks().stream()
                        .filter(t -> t.getStatus() == Status.PENDING).toList();
                case "In Progress" -> taskService.getAllTasks().stream()
                        .filter(t -> t.getStatus() == Status.IN_PROGRESS).toList();
                case "Completed"   -> taskService.getAllTasks().stream()
                        .filter(t -> t.getStatus() == Status.COMPLETED).toList();
                case "Overdue"     -> taskService.getOverdueTasks();
                case "Due Soon"    -> taskService.getDueSoonTasks();
                default            -> taskService.getAllTasks();
            };
            tableModel.setTasks(toShow);
            updateStatusBar();
        });
    }

    // ── Deadline alert (called from background thread) ───────────────────────

    private void showDeadlineAlert(List<Task> alertTasks) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><b>⚠️ Deadline Alert</b><br><br>");
            for (Task t : alertTasks) {
                String label = t.isOverdue() ? "OVERDUE" : "DUE SOON";
                sb.append(String.format("• [%s] %s — %s<br>",
                        label, t.getTitle(),
                        t.getDeadline().format(Task.DATE_FORMATTER)));
            }
            sb.append("</html>");
            JOptionPane.showMessageDialog(this, sb.toString(),
                    "Deadline Reminder", JOptionPane.WARNING_MESSAGE);
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Task getSelectedTask() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a task first.", "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getTaskAt(modelRow);
    }

    private void updateStatusBar() {
        long total     = taskService.getAllTasks().size();
        long overdue   = taskService.getOverdueTasks().size();
        long dueSoon   = taskService.getDueSoonTasks().size();
        setStatus(String.format("Total: %d tasks  |  Overdue: %d  |  Due Soon: %d",
                total, overdue, dueSoon));
    }

    private void setStatus(String msg) {
        statusBar.setText("  " + msg);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JButton makeButton(String text, String tooltip) {
        JButton b = new JButton(text);
        b.setToolTipText(tooltip);
        b.setFocusPainted(false);
        return b;
    }
}
