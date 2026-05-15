package erpmanagementsystem;

import erpmanagementsystem.gui.EmployeeGUI;
import javax.swing.SwingUtilities;

/**
 * Start - Application entry point for the ERP Management System.
 *
 * Launches the EmployeeGUI on the Swing Event Dispatch Thread (EDT).
 * Using SwingUtilities.invokeLater guarantees that all component
 * creation happens on the EDT, preventing thread-safety issues.
 */
public class Start {

    /**
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Schedule GUI construction on the EDT
        SwingUtilities.invokeLater(EmployeeGUI::new);
    }
}
