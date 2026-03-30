package taskmanager.ui;

import taskmanager.model.Task;
import taskmanager.model.Task.Status;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom cell renderer that colours table rows:
 *   • RED background  → overdue tasks
 *   • ORANGE          → due within 2 days
 *   • GREEN           → completed tasks
 *   • Priority badge colours in the Priority column
 */
public class TaskTableRenderer extends DefaultTableCellRenderer {

    // Soft palette
    private static final Color RED_BG     = new Color(255, 204, 204);
    private static final Color ORANGE_BG  = new Color(255, 235, 180);
    private static final Color GREEN_BG   = new Color(204, 255, 204);
    private static final Color WHITE_BG   = Color.WHITE;
    private static final Color ALT_BG     = new Color(245, 245, 250);

    private final TaskTableModel model;

    public TaskTableRenderer(TaskTableModel model) {
        this.model = model;
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (!isSelected) {
            Task t = model.getTaskAt(row);
            if (t.getStatus() == Status.COMPLETED) {
                setBackground(GREEN_BG);
            } else if (t.isOverdue()) {
                setBackground(RED_BG);
            } else if (t.isDueSoon()) {
                setBackground(ORANGE_BG);
            } else {
                setBackground(row % 2 == 0 ? WHITE_BG : ALT_BG);
            }
            setForeground(Color.BLACK);
        }

        // Bold for HIGH priority tasks
        Task t = model.getTaskAt(row);
        setFont(t.getPriority() == Task.Priority.HIGH
                ? getFont().deriveFont(Font.BOLD)
                : getFont().deriveFont(Font.PLAIN));

        setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        return this;
    }
}
