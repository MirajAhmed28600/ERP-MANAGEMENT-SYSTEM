package erpmanagementsystem.gui;

import erpmanagementsystem.entity.Employee;
import erpmanagementsystem.fileio.EmployeeFileIO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * EmployeeGUI - Main application window for the ERP Management System.
 *
 * Layout (distinct from the Student Management System demo):
 *
 *  ┌─────────────────────────────────────────────────────────┐
 *  │  HEADER BANNER  (company name + tagline)                │
 *  ├──────────────────────┬──────────────────────────────────┤
 *  │  LEFT PANEL          │  RIGHT PANEL                     │
 *  │  ─ Form fields       │  ─ Search bar                    │
 *  │    (card style)      │  ─ Employee table                │
 *  │  ─ Action buttons    │  ─ Status bar                    │
 *  └──────────────────────┴──────────────────────────────────┘
 *
 * The two-column split (form left, table right) is the key structural
 * difference from the Student demo's single-column top/bottom layout.
 */
public class EmployeeGUI extends JFrame {

    // ── Colour palette ─────────────────────────────────────────────────────
    private static final Color C_HEADER_BG  = new Color(15,  23,  42);   // deep navy
    private static final Color C_HEADER_FG  = new Color(248, 250, 252);  // near-white
    private static final Color C_ACCENT     = new Color(56,  189, 248);  // sky-blue
    private static final Color C_PANEL_BG   = new Color(241, 245, 249);  // slate-50
    private static final Color C_CARD_BG    = Color.WHITE;
    private static final Color C_BORDER     = new Color(203, 213, 225);  // slate-300
    private static final Color C_BTN_ADD    = new Color(34,  197, 94);   // green-500
    private static final Color C_BTN_UPD    = new Color(59,  130, 246);  // blue-500
    private static final Color C_BTN_DEL    = new Color(239, 68,  68);   // red-500
    private static final Color C_BTN_CLR    = new Color(100, 116, 139);  // slate-500
    private static final Color C_TBL_HEADER = new Color(15,  23,  42);   // same as header
    private static final Color C_TBL_SEL    = new Color(186, 230, 253);  // sky-200
    private static final Color C_STATUS_BG  = new Color(226, 232, 240);  // slate-200
    private static final Color C_TEXT_MUTED = new Color(100, 116, 139);

    // ── Input fields ───────────────────────────────────────────────────────
    private JTextField fldId;
    private JTextField fldName;
    private JComboBox<String> cbDepartment;
    private JTextField fldPosition;
    private JTextField fldSalary;
    private JTextField fldEmail;
    private JTextField fldSearch;

    // ── Table ──────────────────────────────────────────────────────────────
    private JTable table;
    private DefaultTableModel tableModel;

    // ── Status bar ─────────────────────────────────────────────────────────
    private JLabel lblStatus;

    // ── Departments list ───────────────────────────────────────────────────
    private static final String[] DEPARTMENTS = {
        "-- Select Department --",
        "Human Resources", "Finance & Accounting", "Operations",
        "Sales & Marketing", "Information Technology", "Research & Development",
        "Supply Chain", "Legal & Compliance", "Customer Service", "Executive"
    };

    // ══════════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ══════════════════════════════════════════════════════════════════════════

    public EmployeeGUI() {
        setTitle("ERP Management System — Employee Records");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 560));
        getContentPane().setBackground(C_PANEL_BG);
        setLayout(new BorderLayout());

        add(buildHeader(),     BorderLayout.NORTH);
        add(buildMainSplit(),  BorderLayout.CENTER);
        add(buildStatusBar(),  BorderLayout.SOUTH);

        // Initialise database and load records
        try {
            EmployeeFileIO.createFileIfNotExists();
        } catch (IOException ex) {
            showError("Could not initialise database:\n" + ex.getMessage());
        }
        refreshTable();
        setStatus("Ready  •  " + tableModel.getRowCount() + " employee(s) loaded.");

        // Centre on screen
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UI BUILDERS
    // ══════════════════════════════════════════════════════════════════════════

    /** Top banner with company branding. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        // Left: logo-style text
        JLabel lblTitle = new JLabel("ERP Management System");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(C_HEADER_FG);

        JLabel lblSub = new JLabel("Employee Records & Administration");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(C_ACCENT);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(lblTitle);
        left.add(lblSub);

        // Right: record count badge
        JLabel lblBadge = new JLabel("EMPLOYEE PORTAL", SwingConstants.RIGHT);
        lblBadge.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblBadge.setForeground(C_ACCENT);

        header.add(left,     BorderLayout.WEST);
        header.add(lblBadge, BorderLayout.EAST);
        return header;
    }

    /** Two-column split: form panel (left) + table panel (right). */
    private JSplitPane buildMainSplit() {
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            buildFormPanel(),
            buildTablePanel()
        );
        split.setDividerLocation(360);
        split.setDividerSize(4);
        split.setBorder(null);
        split.setBackground(C_PANEL_BG);
        return split;
    }

    // ── LEFT PANEL ──────────────────────────────────────────────────────────

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setBackground(C_PANEL_BG);
        outer.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 8));

        outer.add(buildFieldCard(), BorderLayout.CENTER);
        outer.add(buildButtonRow(), BorderLayout.SOUTH);
        return outer;
    }

    /** Card containing all input fields with labelled rows. */
    private JPanel buildFieldCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(C_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        GridBagConstraints lc = new GridBagConstraints(); // label column
        lc.gridx = 0; lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(6, 0, 6, 10);

        GridBagConstraints fc = new GridBagConstraints(); // field column
        fc.gridx = 1; fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0; fc.insets = new Insets(6, 0, 6, 0);

        int row = 0;

        // Section heading
        JLabel secLabel = new JLabel("Employee Details");
        secLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        secLabel.setForeground(C_HEADER_BG);
        GridBagConstraints sc = new GridBagConstraints();
        sc.gridx = 0; sc.gridwidth = 2; sc.anchor = GridBagConstraints.WEST;
        sc.insets = new Insets(0, 0, 12, 0);
        card.add(secLabel, sc); row++;

        // Employee ID
        fldId = makeField("e.g. 20240001");
        addFormRow(card, lc, fc, row++, "Employee ID *", fldId,
                   "<html><font color='#64748b' size='2'>Exactly 8 digits</font></html>");

        // Name
        fldName = makeField("Full name");
        addFormRow(card, lc, fc, row++, "Full Name *", fldName, null);

        // Department (combo box)
        cbDepartment = new JComboBox<>(DEPARTMENTS);
        cbDepartment.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cbDepartment.setBackground(Color.WHITE);
        addFormRow(card, lc, fc, row++, "Department *", cbDepartment, null);

        // Position
        fldPosition = makeField("Job title");
        addFormRow(card, lc, fc, row++, "Position *", fldPosition, null);

        // Salary
        fldSalary = makeField("e.g. 5500.00");
        addFormRow(card, lc, fc, row++, "Salary (USD) *", fldSalary, null);

        // Email
        fldEmail = makeField("work@company.com");
        addFormRow(card, lc, fc, row++, "Email Address *", fldEmail, null);

        return card;
    }

    /** Adds a label + component pair to the GridBagLayout form. */
    private void addFormRow(JPanel card, GridBagConstraints lc, GridBagConstraints fc,
                            int row, String labelText, JComponent field, String hint) {
        lc.gridy = row;
        fc.gridy = row;

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(new Color(51, 65, 85));
        card.add(lbl, lc);

        if (hint != null) {
            JPanel wrap = new JPanel(new BorderLayout(0, 2));
            wrap.setOpaque(false);
            wrap.add(field, BorderLayout.CENTER);
            wrap.add(new JLabel(hint), BorderLayout.SOUTH);
            card.add(wrap, fc);
        } else {
            card.add(field, fc);
        }
    }

    /** Constructs a styled text field with placeholder support. */
    private JTextField makeField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        tf.setToolTipText(placeholder);
        // Placeholder text via FocusListener
        tf.setForeground(C_TEXT_MUTED);
        tf.setText(placeholder);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setForeground(C_TEXT_MUTED);
                    tf.setText(placeholder);
                }
            }
        });
        return tf;
    }

    /** Helper: returns the real text of a placeholder field (empty string if placeholder shown). */
    private String fieldText(JTextField tf) {
        String placeholder = tf.getToolTipText();
        String text = tf.getText().trim();
        return text.equals(placeholder) ? "" : text;
    }

    /** Row of four action buttons below the form card. */
    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new GridLayout(2, 2, 8, 8));
        row.setOpaque(false);

        row.add(makeButton("＋  Add Employee",    C_BTN_ADD, e -> doAdd()));
        row.add(makeButton("✎  Update Record",   C_BTN_UPD, e -> doUpdate()));
        row.add(makeButton("✕  Delete Record",   C_BTN_DEL, e -> doDelete()));
        row.add(makeButton("↺  Clear Fields",    C_BTN_CLR, e -> clearFields()));
        return row;
    }

    private JButton makeButton(String text, Color bg, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.addActionListener(al);
        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            Color original = bg;
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(original.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(original);
            }
        });
        return btn;
    }

    // ── RIGHT PANEL ─────────────────────────────────────────────────────────

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(C_PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 8, 8, 16));

        panel.add(buildSearchBar(), BorderLayout.NORTH);
        panel.add(buildTable(),     BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel lbl = new JLabel("🔍");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 16));

        fldSearch = new JTextField();
        fldSearch.setFont(new Font("SansSerif", Font.PLAIN, 13));
        fldSearch.setToolTipText("Search by Employee ID or Name…");
        fldSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        fldSearch.addActionListener(e -> doSearch());

        JButton btnSearch = makeButton("Search", new Color(79, 70, 229), e -> doSearch());
        JButton btnAll    = makeButton("View All", C_BTN_CLR, e -> {
            fldSearch.setText("");
            refreshTable();
            setStatus("Showing all " + tableModel.getRowCount() + " employee(s).");
        });

        JPanel btns = new JPanel(new GridLayout(1, 2, 6, 0));
        btns.setOpaque(false);
        btns.add(btnSearch);
        btns.add(btnAll);

        bar.add(lbl,       BorderLayout.WEST);
        bar.add(fldSearch, BorderLayout.CENTER);
        bar.add(btns,      BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        String[] cols = { "Employee ID", "Full Name", "Department", "Position", "Salary (USD)", "Email" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setGridColor(new Color(226, 232, 240));
        table.setShowVerticalLines(true);
        table.setSelectionBackground(C_TBL_SEL);
        table.setSelectionForeground(Color.BLACK);
        table.setIntercellSpacing(new Dimension(8, 4));

        // Header styling
        JTableHeader th = table.getTableHeader();
        th.setBackground(C_TBL_HEADER);
        th.setForeground(C_HEADER_FG);
        th.setFont(new Font("SansSerif", Font.BOLD, 12));
        th.setReorderingAllowed(false);
        th.setBorder(BorderFactory.createEmptyBorder());

        // Column widths
        int[] widths = { 110, 150, 150, 140, 110, 180 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Row click → populate form
        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) populateFormFromTable();
        });

        // Alternating row colours
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    setForeground(Color.BLACK);
                }
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(C_STATUS_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER),
            BorderFactory.createEmptyBorder(5, 16, 5, 16)
        ));
        lblStatus = new JLabel("Initialising…");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblStatus.setForeground(C_TEXT_MUTED);
        bar.add(lblStatus, BorderLayout.WEST);

        JLabel version = new JLabel("ERP v1.0");
        version.setFont(new Font("SansSerif", Font.PLAIN, 11));
        version.setForeground(C_TEXT_MUTED);
        bar.add(version, BorderLayout.EAST);
        return bar;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // VALIDATION
    // ══════════════════════════════════════════════════════════════════════════

    /** Validates that id is exactly 8 numeric digits. */
    private boolean isValidId(String id) {
        if (id.isEmpty()) {
            showError("Employee ID is required.");
            return false;
        }
        if (!id.matches("\\d{8}")) {
            showError("Employee ID must be exactly 8 digits (numbers only).\n"
                    + "Example: 20240001");
            return false;
        }
        return true;
    }

    /** Validates all fields for Add / Update. */
    private boolean isValidAll(String id, String name, String dept,
                               String position, String salary, String email) {
        if (!isValidId(id)) return false;

        if (name.isEmpty()) {
            showError("Full Name is required.");
            return false;
        }
        if (dept.equals(DEPARTMENTS[0])) {
            showError("Please select a Department.");
            return false;
        }
        if (position.isEmpty()) {
            showError("Position is required.");
            return false;
        }
        if (salary.isEmpty()) {
            showError("Salary is required.");
            return false;
        }
        try {
            double s = Double.parseDouble(salary);
            if (s <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Salary must be a positive number.\nExample: 5500.00");
            return false;
        }
        if (email.isEmpty()) {
            showError("Email Address is required.");
            return false;
        }
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+$")) {
            showError("Email address format is invalid.\nExample: name@company.com");
            return false;
        }
        // Pipe characters would corrupt the database line format
        for (String val : new String[]{ name, position, salary, email }) {
            if (val.contains("|")) {
                showError("Fields must not contain the '|' character.");
                return false;
            }
        }
        return true;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CRUD ACTIONS
    // ══════════════════════════════════════════════════════════════════════════

    private void doAdd() {
        String id       = fieldText(fldId);
        String name     = fieldText(fldName);
        String dept     = (String) cbDepartment.getSelectedItem();
        String position = fieldText(fldPosition);
        String salary   = fieldText(fldSalary);
        String email    = fieldText(fldEmail);

        if (!isValidAll(id, name, dept, position, salary, email)) return;

        if (EmployeeFileIO.idExists(id)) {
            showError("An employee with ID " + id + " already exists.\n"
                    + "Employee IDs must be unique.");
            return;
        }

        try {
            EmployeeFileIO.addEmployee(new Employee(id, name, dept, position, salary, email));
            showInfo("Employee added successfully!");
            clearFields();
            refreshTable();
            setStatus("Employee " + id + " added  •  Total: " + tableModel.getRowCount());
        } catch (IOException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void doUpdate() {
        String id       = fieldText(fldId);
        String name     = fieldText(fldName);
        String dept     = (String) cbDepartment.getSelectedItem();
        String position = fieldText(fldPosition);
        String salary   = fieldText(fldSalary);
        String email    = fieldText(fldEmail);

        if (!isValidAll(id, name, dept, position, salary, email)) return;

        try {
            boolean updated = EmployeeFileIO.updateEmployee(
                new Employee(id, name, dept, position, salary, email));
            if (updated) {
                showInfo("Employee record updated successfully!");
                clearFields();
                refreshTable();
                setStatus("Employee " + id + " updated.");
            } else {
                showError("No employee found with ID " + id + ".\n"
                        + "Select a record from the table first.");
            }
        } catch (IOException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void doDelete() {
        String id = fieldText(fldId);
        if (!isValidId(id)) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete employee ID: " + id + "?\n"
          + "This action cannot be undone.",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean deleted = EmployeeFileIO.deleteEmployee(id);
            if (deleted) {
                showInfo("Employee record deleted.");
                clearFields();
                refreshTable();
                setStatus("Employee " + id + " deleted  •  Total: " + tableModel.getRowCount());
            } else {
                showError("No employee found with ID " + id + ".");
            }
        } catch (IOException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void doSearch() {
        String keyword = fldSearch.getText().trim();
        if (keyword.isEmpty()) {
            showError("Please enter an Employee ID or Name to search.");
            return;
        }
        Object[][] results = EmployeeFileIO.searchEmployees(keyword);
        tableModel.setRowCount(0);
        for (Object[] row : results) tableModel.addRow(row);

        if (results.length == 0) {
            setStatus("No results found for: \"" + keyword + "\"");
            showInfo("No matching employee found for: \"" + keyword + "\"");
        } else {
            setStatus("Search returned " + results.length + " result(s) for: \"" + keyword + "\"");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /** Reloads all records from the database into the table. */
    private void refreshTable() {
        Object[][] rows = EmployeeFileIO.getAllEmployees();
        tableModel.setRowCount(0);
        for (Object[] row : rows) tableModel.addRow(row);
    }

    /** Fills the form fields from the currently selected table row. */
    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        setField(fldId,       (String) tableModel.getValueAt(row, 0));
        setField(fldName,     (String) tableModel.getValueAt(row, 1));
        cbDepartment.setSelectedItem(tableModel.getValueAt(row, 2));
        setField(fldPosition, (String) tableModel.getValueAt(row, 3));
        setField(fldSalary,   (String) tableModel.getValueAt(row, 4));
        setField(fldEmail,    (String) tableModel.getValueAt(row, 5));
    }

    /** Sets a placeholder-aware field value. */
    private void setField(JTextField tf, String value) {
        tf.setForeground(Color.BLACK);
        tf.setText(value != null ? value : "");
    }

    /** Clears all input fields back to their placeholder state. */
    private void clearFields() {
        String[] placeholders = {
            "e.g. 20240001", "Full name", "Job title", "e.g. 5500.00", "work@company.com"
        };
        JTextField[] fields = { fldId, fldName, fldPosition, fldSalary, fldEmail };
        for (int i = 0; i < fields.length; i++) {
            fields[i].setForeground(C_TEXT_MUTED);
            fields[i].setText(placeholders[i]);
        }
        cbDepartment.setSelectedIndex(0);
        fldSearch.setText("");
        table.clearSelection();
        setStatus("Fields cleared.");
    }

    private void setStatus(String msg) {
        lblStatus.setText(msg);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error",
            JOptionPane.ERROR_MESSAGE);
    }
}
