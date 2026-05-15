package erpmanagementsystem.entity;

/**
 * Employee - Data model representing a single employee record in the ERP system.
 *
 * Each employee holds six fields:
 *   employeeId  - exactly 8-digit numeric string (unique identifier)
 *   name        - full name
 *   department  - department the employee belongs to
 *   position    - job title / position
 *   salary      - monthly salary as a string (validated as a positive number)
 *   email       - work email address
 *
 * Provides CSV serialisation (toLine / fromLine) for flat-file persistence
 * and a toRow() helper for direct JTable loading.
 */
public class Employee {

    // ── Fields ────────────────────────────────────────────────────────────────
    private String employeeId;   // Unique 8-digit ID, e.g. "20240001"
    private String name;         // Full name,   e.g. "Alice Rahman"
    private String department;   // Department,  e.g. "Finance"
    private String position;     // Position,    e.g. "Senior Accountant"
    private String salary;       // Salary,      e.g. "5500.00"
    private String email;        // Email,       e.g. "alice@company.com"

    // ── Constructor ───────────────────────────────────────────────────────────
    public Employee(String employeeId, String name, String department,
                    String position, String salary, String email) {
        this.employeeId  = employeeId;
        this.name        = name;
        this.department  = department;
        this.position    = position;
        this.salary      = salary;
        this.email       = email;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getEmployeeId()  { return employeeId; }
    public String getName()        { return name; }
    public String getDepartment()  { return department; }
    public String getPosition()    { return position; }
    public String getSalary()      { return salary; }
    public String getEmail()       { return email; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setEmployeeId(String employeeId)  { this.employeeId  = employeeId; }
    public void setName(String name)              { this.name        = name; }
    public void setDepartment(String department)  { this.department  = department; }
    public void setPosition(String position)      { this.position    = position; }
    public void setSalary(String salary)          { this.salary      = salary; }
    public void setEmail(String email)            { this.email       = email; }

    // ── CSV serialisation ─────────────────────────────────────────────────────

    /**
     * Converts this Employee to a pipe-delimited line for storage.
     * Pipe (|) is used instead of comma to allow commas inside field values.
     *
     * Example: "20240001|Alice Rahman|Finance|Senior Accountant|5500.00|alice@company.com"
     */
    public String toLine() {
        return employeeId + "|" + name + "|" + department + "|"
             + position  + "|" + salary + "|" + email;
    }

    /**
     * Parses a pipe-delimited line from the database file back into an Employee.
     *
     * @param line a raw line from employees.txt (may be null or malformed)
     * @return a new Employee, or null if the line is invalid
     */
    public static Employee fromLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split("\\|", -1);
        if (parts.length != 6) return null;
        return new Employee(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
    }

    /**
     * Returns a 6-element Object array suitable for a JTable row.
     * Column order: Employee ID | Name | Department | Position | Salary | Email
     */
    public Object[] toRow() {
        return new Object[]{ employeeId, name, department, position, salary, email };
    }
}
