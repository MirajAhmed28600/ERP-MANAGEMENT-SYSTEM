# ERP Management System

A desktop application for managing employee records with CRUD operations and search functionality.

## Project Structure
erp-management-system/
├── src/
│ └── erpmanagementsystem/
│ ├── Start.java
│ ├── entity/
│ │ └── Employee.java
│ ├── fileio/
│ │ └── EmployeeFileIO.java
│ └── gui/
│ └── EmployeeGUI.java
└── README.md


## Prerequisites

- **Java JDK 11 or higher** (JDK 25.0.2 works)
- **VS Code** (optional) with Java Extension Pack

## Running the Application

### Method 1: Using VS Code (Recommended)

1. **Install Java Extension Pack** in VS Code:
   - Extensions: `Extension Pack for Java` by Microsoft

2. **Open the project folder** in VS Code:
   
4. **Run the application**:
- Navigate to `src/erpmanagementsystem/Start.java`
- Click the `Run` button (▶) above the `main` method
- Or press `F5` and select `Java`

### Method 2: Command Line (Manual)
# Navigate to source directory
cd ERP-MANAGEMENT-SYSTEM/src

# Compile all Java files
javac erpmanagementsystem/*.java erpmanagementsystem/**/*.java

# Run the application
java erpmanagementsystem.Start
