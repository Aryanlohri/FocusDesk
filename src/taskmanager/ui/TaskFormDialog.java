package taskmanager.ui;

import taskmanager.model.Task;
import taskmanager.model.Task.Priority;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Modal dialog for creating or editing a task.
 * Shows extra "Repeat every N days" field when "Recurring" checkbox is ticked.
 */
public class TaskFormDialog extends JDialog {

    // ── Result holder ────────────────────────────────────────────────────────

    public static class TaskFormResult {
        public final String    title;
        public final String    description;
        public final Priority  priority;
        public final LocalDate deadline;
        public final boolean   isRecurring;
        public final int       repeatDays;

        public TaskFormResult(String title, String description,
                              Priority priority, LocalDate deadline,
                              boolean isRecurring, int repeatDays) {
            this.title       = title;
            this.description = description;
            this.priority    = priority;
            this.deadline    = deadline;
            this.isRecurring = isRecurring;
            this.repeatDays  = repeatDays;
        }
    }

    // ── Fields ───────────────────────────────────────────────────────────────

    private final JTextField     titleField       = new JTextField(25);
    private final JTextArea      descArea         = new JTextArea(3, 25);
    private final JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
    private final JTextField     deadlineField    = new JTextField("dd-MM-yyyy", 12);
    private final JCheckBox      recurringCheck   = new JCheckBox("Recurring task");
    private final JSpinner       repeatSpinner    = new JSpinner(new SpinnerNumberModel(7, 1, 365, 1));

    private TaskFormResult result = null;

    // ── Constructor ──────────────────────────────────────────────────────────

    public TaskFormDialog(Frame owner, String dialogTitle) {
        super(owner, dialogTitle, true);
        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    /** Pre-populate fields when editing an existing task. */
    public void populate(Task t) {
        titleField.setText(t.getTitle());
        descArea.setText(t.getDescription());
        priorityBox.setSelectedItem(t.getPriority());
        deadlineField.setText(t.getDeadline().format(Task.DATE_FORMATTER));
        if (t instanceof taskmanager.model.RecurringTask rt) {
            recurringCheck.setSelected(true);
            repeatSpinner.setValue(rt.getRepeatEveryDays());
            repeatSpinner.setEnabled(true);
        }
    }

    /** Returns the filled-in result, or null if the user cancelled. */
    public TaskFormResult getResult() { return result; }

    // ── UI construction ──────────────────────────────────────────────────────

    private void buildUI() {
        setResizable(false);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(4, 4, 4, 8);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill   = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(4, 0, 4, 4);
        fc.gridx   = 1;

        int row = 0;

        // Title
        lc.gridy = fc.gridy = row++;
        form.add(new JLabel("Title *"), lc);
        form.add(titleField, fc);

        // Description
        lc.gridy = fc.gridy = row++;
        lc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Description"), lc);
        lc.anchor = GridBagConstraints.WEST;
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(250, 70));
        form.add(descScroll, fc);

        // Priority
        lc.gridy = fc.gridy = row++;
        form.add(new JLabel("Priority *"), lc);
        form.add(priorityBox, fc);

        // Deadline
        lc.gridy = fc.gridy = row++;
        form.add(new JLabel("Deadline * (dd-MM-yyyy)"), lc);
        deadlineField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (deadlineField.getText().equals("dd-MM-yyyy")) deadlineField.setText("");
            }
        });
        form.add(deadlineField, fc);

        // Recurring checkbox
        lc.gridy = fc.gridy = row++;
        fc.gridwidth = 2; fc.gridx = 0;
        recurringCheck.addActionListener(e ->
                repeatSpinner.setEnabled(recurringCheck.isSelected()));
        form.add(recurringCheck, fc);
        fc.gridwidth = 1; fc.gridx = 1;

        // Repeat interval
        lc.gridy = fc.gridy = row++;
        form.add(new JLabel("  Repeat every (days)"), lc);
        repeatSpinner.setEnabled(false);
        form.add(repeatSpinner, fc);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save   = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        save.setPreferredSize(new Dimension(80, 28));
        cancel.setPreferredSize(new Dimension(80, 28));

        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());

        buttons.add(cancel);
        buttons.add(save);

        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
    }

    // ── Action handlers ──────────────────────────────────────────────────────

    private void onSave() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Title cannot be empty.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate deadline;
        try {
            deadline = LocalDate.parse(deadlineField.getText().trim(), Task.DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Use dd-MM-yyyy (e.g. 31-12-2025).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        result = new TaskFormResult(
                title,
                descArea.getText().trim(),
                (Priority) priorityBox.getSelectedItem(),
                deadline,
                recurringCheck.isSelected(),
                (int) repeatSpinner.getValue());

        dispose();
    }
}
