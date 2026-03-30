package taskmanager.ui;

import taskmanager.model.Task;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Swing TableModel that feeds task data into a JTable.
 * Encapsulates column definitions and row-to-Task mapping.
 */
public class TaskTableModel extends AbstractTableModel {

    private static final String[] COLUMNS =
            { "#", "Title", "Priority", "Status", "Deadline", "Type" };

    private List<Task> tasks = new ArrayList<>();

    // ── Public API ───────────────────────────────────────────────────────────

    public void setTasks(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
        fireTableDataChanged();
    }

    /** Returns the Task object for a given view row index. */
    public Task getTaskAt(int row) {
        return tasks.get(row);
    }

    // ── TableModel interface ─────────────────────────────────────────────────

    @Override public int getRowCount()    { return tasks.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Task t = tasks.get(row);
        return switch (col) {
            case 0 -> t.getId();
            case 1 -> t.getTitle();
            case 2 -> t.getPriority().name();
            case 3 -> t.getStatus().name();
            case 4 -> t.getDeadline().format(Task.DATE_FORMATTER);
            case 5 -> (t instanceof taskmanager.model.RecurringTask) ? "Recurring" : "One-time";
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return (col == 0) ? Integer.class : String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;   // editing is done through the form panel
    }
}
