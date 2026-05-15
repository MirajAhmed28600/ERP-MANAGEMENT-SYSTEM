package erpmanagementsystem.fileio;

import erpmanagementsystem.entity.Employee;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeFileIO - Handles all flat-file persistence for employee records.
 *
 * Database file: erp-management-system/fileio/employees.txt
 * Format: one employee per line, fields separated by pipe (|)
 *
 * Update / Delete strategy (write-to-temp-then-rename):
 *   1. Read all lines from the original file.
 *   2. Write every line (possibly modified or skipped) to a temp file.
 *   3. Delete the original.
 *   4. Rename the temp file to the original name.
 *
 * Uses ArrayList internally so each file is read only once per operation.
 * All methods are static — no instantiation required.
 */
public class EmployeeFileIO {

    // Resolve path relative to the class-file location so the app works from any
    // working directory (IDE, JAR, command-line).
    private static final String FILE_NAME;
    private static final String TEMP_FILE;

    static {
        // Place the database file beside the compiled class in the fileio package folder.
        // When run from an IDE the working directory is usually the project root,
        // so we use a path that works in both IDE and packaged-JAR scenarios.
        String base = "erp-management-system/fileio/";
        FILE_NAME = base + "employees.txt";
        TEMP_FILE = base + "employees_temp.txt";
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    /**
     * Creates the database file (and any missing parent directories)
     * if they do not already exist.
     */
    public static void createFileIfNotExists() throws IOException {
        File file = new File(FILE_NAME);
        file.getParentFile().mkdirs();   // ensure parent directories exist
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /** Reads all valid Employee records from the file into a List. */
    private static List<Employee> readAll() {
        List<Employee> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                Employee e = Employee.fromLine(line);
                if (e != null) list.add(e);
            }
        } catch (IOException ignored) { }
        return list;
    }

    /** Writes a list of Employee records back to the database file atomically. */
    private static void writeAll(List<Employee> employees) throws IOException {
        File tempFile = new File(TEMP_FILE);
        File mainFile = new File(FILE_NAME);

        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)))) {
            for (Employee e : employees) {
                pw.println(e.toLine());
            }
        }

        if (!mainFile.delete() || !tempFile.renameTo(mainFile)) {
            throw new IOException("Could not finalise write to database.");
        }
    }

    // ── Query helpers ─────────────────────────────────────────────────────────

    /**
     * Returns true if an employee with the given ID already exists in the file.
     */
    public static boolean idExists(String id) {
        for (Employee e : readAll()) {
            if (e.getEmployeeId().equals(id)) return true;
        }
        return false;
    }

    // ── CRUD operations ───────────────────────────────────────────────────────

    /**
     * Appends a new employee record to the database.
     */
    public static void addEmployee(Employee emp) throws IOException {
        try (PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(FILE_NAME, true)))) {
            pw.println(emp.toLine());
        }
    }

    /**
     * Replaces the record whose employeeId matches emp.getEmployeeId().
     *
     * @return true if a record was found and updated; false otherwise.
     */
    public static boolean updateEmployee(Employee emp) throws IOException {
        List<Employee> all = readAll();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getEmployeeId().equals(emp.getEmployeeId())) {
                all.set(i, emp);
                found = true;
                break;
            }
        }
        if (found) writeAll(all);
        return found;
    }

    /**
     * Removes the record with the given employeeId.
     *
     * @return true if a record was found and deleted; false otherwise.
     */
    public static boolean deleteEmployee(String id) throws IOException {
        List<Employee> all = readAll();
        boolean found = all.removeIf(e -> e.getEmployeeId().equals(id));
        if (found) writeAll(all);
        return found;
    }

    // ── Retrieval ─────────────────────────────────────────────────────────────

    /**
     * Returns all employee records as a 2D Object array for JTable.
     * Columns: Employee ID | Name | Department | Position | Salary | Email
     */
    public static Object[][] getAllEmployees() {
        List<Employee> all = readAll();
        Object[][] rows = new Object[all.size()][6];
        for (int i = 0; i < all.size(); i++) {
            Object[] row = all.get(i).toRow();
            System.arraycopy(row, 0, rows[i], 0, 6);
        }
        return rows;
    }

    /**
     * Case-insensitive partial search across Employee ID and Name fields.
     *
     * @param keyword the search term entered by the user
     * @return 2D Object array of matching rows for JTable
     */
    public static Object[][] searchEmployees(String keyword) {
        String kw = keyword.toLowerCase();
        List<Employee> matches = new ArrayList<>();
        for (Employee e : readAll()) {
            if (e.getEmployeeId().toLowerCase().contains(kw)
             || e.getName().toLowerCase().contains(kw)) {
                matches.add(e);
            }
        }
        Object[][] rows = new Object[matches.size()][6];
        for (int i = 0; i < matches.size(); i++) {
            Object[] row = matches.get(i).toRow();
            System.arraycopy(row, 0, rows[i], 0, 6);
        }
        return rows;
    }
}
