package taskmanager;

import taskmanager.ui.MainWindow;
import taskmanager.util.TaskException.DataAccessException;

import javax.swing.*;

/**
 * Application entry point.
 * Sets the system look-and-feel and launches the main window on the EDT.
 */
public class Main {

    public static void main(String[] args) {

        // Use the OS native look-and-feel (Windows / macOS / GTK)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to Swing default — not critical
            System.err.println("Could not set system look-and-feel: " + e.getMessage());
        }

        // All Swing operations must happen on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                new MainWindow();
            } catch (DataAccessException e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to load task data:\n" + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
