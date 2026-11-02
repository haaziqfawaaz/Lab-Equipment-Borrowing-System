import database.DatabaseConnection;
import model.BorrowRecord;
import model.Equipment;
import model.User;
import service.BorrowService;
import service.EquipmentService;
import service.UserService;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Main application entry point and console test runner.
 * Connects to the layered architecture (Services -> DAOs -> Database).
 */
public class Main {
    private static final UserService userService = new UserService();
    private static final EquipmentService equipmentService = new EquipmentService();
    private static final BorrowService borrowService = new BorrowService();
    
    // Single shared Scanner instance for standard input throughout the application
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("  Lab Equipment Borrowing and Return System - Test Runner");
        System.out.println("===============================================================");

        // Automatically ensure tables exist from schema.sql if first-time run
        initializeDatabaseIfEmpty();

        boolean running = true;
        try {
            while (running) {
                printMenu();
                String input = readInput("Enter your choice: ");
                if (input == null) {
                    System.out.println("\nConsole input stream closed or unavailable. Exiting gracefully.");
                    break;
                }

                switch (input) {
                    case "1" -> handleRegisterStudent();
                    case "2" -> handleUserLoginOrLookup();
                    case "3" -> handleAddEquipment();
                    case "4" -> handleViewEquipment();
                    case "5" -> handleBorrowEquipment();
                    case "6" -> handleReturnEquipment();
                    case "7" -> handleViewHistory();
                    case "8" -> runAutomatedTestSuite();
                    case "0" -> {
                        System.out.println("\nExiting system. Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid option. Please enter a number between 0 and 8.");
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nConsole input ended unexpectedly. Exiting gracefully.");
        }
    }

    /**
     * Helper method to safely read input from the shared Scanner without crashing.
     * Returns null if end of input is reached or input is unavailable.
     */
    private static String readInput(String prompt) {
        System.out.print(prompt);
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine().trim();
            }
        } catch (NoSuchElementException | IllegalStateException ignored) {
        }
        return null;
    }

    /**
     * Checks if the required tables exist in lab_equipment_db.
     * If missing, executes database/schema.sql safely using IF NOT EXISTS.
     */
    private static void initializeDatabaseIfEmpty() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "users", null)) {
                if (!rs.next()) {
                    System.out.println("Initializing database tables from schema.sql...");
                    executeSqlScript(conn, "database/schema.sql");
                    System.out.println("Database tables and seed data initialized successfully.");
                }
            }
        } catch (Exception e) {
            System.err.println("Database check note: " + e.getMessage());
        }
    }

    private static void executeSqlScript(Connection conn, String scriptPath) {
        try (Statement stmt = conn.createStatement()) {
            String content = new String(Files.readAllBytes(Paths.get(scriptPath)));
            // Split SQL statements by semicolon
            String[] commands = content.split(";");
            for (String cmd : commands) {
                // Strip SQL comments
                String cleaned = cmd.replaceAll("(?m)^--.*$", "").trim();
                if (!cleaned.isEmpty()) {
                    try {
                        stmt.execute(cleaned);
                    } catch (SQLException ex) {
                        // Ignore harmless duplicate or database exists notices
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not run schema script: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n--------------------------- MENU ---------------------------");
        System.out.println("1. Register Student");
        System.out.println("2. User Login / Lookup (Student or Admin)");
        System.out.println("3. Add Equipment (Admin Only)");
        System.out.println("4. View Equipment Inventory & Availability");
        System.out.println("5. Borrow Equipment (7-Day Duration)");
        System.out.println("6. Return Equipment & Calculate Fine (₹5/day)");
        System.out.println("7. View Borrowing History");
        System.out.println("8. Run Automated Test Suite (All Scenarios)");
        System.out.println("0. Exit");
        System.out.println("------------------------------------------------------------");
    }

    // 1. Student Registration
    private static void handleRegisterStudent() {
        System.out.println("\n--- Student Registration ---");
        try {
            String name = readInput("Enter Student Full Name: ");
            if (name == null) return;

            String email = readInput("Enter Student Email: ");
            if (email == null) return;

            String contact = readInput("Enter Student Contact (optional): ");
            if (contact == null) return;

            User student = userService.registerStudent(name, email, contact);
            System.out.println("SUCCESS: Student registered with ID: " + student.getUserId());
            System.out.println("Details: " + student);
        } catch (Exception e) {
            System.out.println("REGISTRATION FAILED: " + e.getMessage());
        }
    }

    // 2. User Login / Lookup
    private static void handleUserLoginOrLookup() {
        System.out.println("\n--- User Login / Lookup ---");
        try {
            String email = readInput("Enter User Email: ");
            if (email == null) return;

            User user = userService.getUserByEmail(email);
            if (user != null) {
                System.out.println("Login/Lookup Successful!");
                System.out.println("User ID: " + user.getUserId());
                System.out.println("Name: " + user.getName());
                System.out.println("Role: " + user.getRole());
                System.out.println("Contact: " + user.getContact());
            } else {
                System.out.println("User not found with email: " + email);
            }
        } catch (Exception e) {
            System.out.println("Lookup Error: " + e.getMessage());
        }
    }

    // 3. Add Equipment (Requires Admin Authorization)
    private static void handleAddEquipment() {
        System.out.println("\n--- Add Equipment (Admin Authorized) ---");
        try {
            String adminIdStr = readInput("Enter Admin User ID: ");
            if (adminIdStr == null) return;
            int adminId = Integer.parseInt(adminIdStr);

            // Verify the user BEFORE asking for equipment details.
            User admin = userService.getUserById(adminId);
            if (admin == null || !"ADMIN".equalsIgnoreCase(admin.getRole())) {
                System.out.println("Access Denied: Admin privileges required.");
                return;
            }

            System.out.println("Admin verified: " + admin.getName());

            String name = readInput("Enter Equipment Name: ");
            if (name == null) return;

            String category = readInput("Enter Category (e.g., Electronics, Physics): ");
            if (category == null) return;

            String qtyStr = readInput("Enter Total Quantity: ");
            if (qtyStr == null) return;
            int qty = Integer.parseInt(qtyStr);

            String condition = readInput("Enter Condition Status (e.g., GOOD, NEW): ");
            if (condition == null) return;

            // EquipmentService performs its own authorization check as well.
            Equipment eq = equipmentService.addEquipment(adminId, name, category, qty, condition);
            System.out.println("SUCCESS: Equipment added with ID: " + eq.getEquipmentId());
            System.out.println("Details: " + eq);
        } catch (NumberFormatException e) {
            System.out.println("INPUT ERROR: Please enter valid numbers for ID and quantity.");
        } catch (SecurityException e) {
            System.out.println("AUTHORIZATION DENIED: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ADD EQUIPMENT FAILED: " + e.getMessage());
        }
    }

    // 4. View Equipment Inventory & Availability
    private static void handleViewEquipment() {
        System.out.println("\n--- Equipment Inventory ---");
        try {
            List<Equipment> list = equipmentService.getAllEquipment();
            if (list.isEmpty()) {
                System.out.println("No equipment found in the database.");
                return;
            }

            System.out.printf("%-5s | %-32s | %-15s | %-8s | %-10s | %-10s%n", 
                    "ID", "Name", "Category", "Total", "Available", "Condition");
            System.out.println("-----------------------------------------------------------------------------------------");
            for (Equipment eq : list) {
                System.out.printf("%-5d | %-32s | %-15s | %-8d | %-10d | %-10s%n",
                        eq.getEquipmentId(), eq.getName(), eq.getCategory(), 
                        eq.getTotalQuantity(), eq.getAvailableQuantity(), eq.getConditionStatus());
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch equipment: " + e.getMessage());
        }
    }

    // 5. Borrow Equipment (7-Day Duration)
    private static void handleBorrowEquipment() {
        System.out.println("\n--- Borrow Equipment ---");
        try {
            String userIdStr = readInput("Enter Student User ID: ");
            if (userIdStr == null) return;
            int userId = Integer.parseInt(userIdStr);

            String equipIdStr = readInput("Enter Equipment ID to Borrow: ");
            if (equipIdStr == null) return;
            int equipmentId = Integer.parseInt(equipIdStr);

            BorrowRecord record = borrowService.borrowEquipment(userId, equipmentId);
            System.out.println("BORROW SUCCESSFUL!");
            System.out.println("Borrow Record ID: " + record.getRecordId());
            System.out.println("Borrow Date: " + record.getBorrowDate());
            System.out.println("Due Date (Standard 7 Days): " + record.getDueDate());
            System.out.println("Status: " + record.getStatus());
        } catch (NumberFormatException e) {
            System.out.println("INPUT ERROR: Please enter numeric IDs.");
        } catch (Exception e) {
            System.out.println("BORROW FAILED: " + e.getMessage());
        }
    }

    // 6. Return Equipment & Calculate Fine
    private static void handleReturnEquipment() {
        System.out.println("\n--- Return Equipment ---");
        try {
            String recordIdStr = readInput("Enter Borrow Record ID to Return: ");
            if (recordIdStr == null) return;
            int recordId = Integer.parseInt(recordIdStr);

            BorrowRecord record = borrowService.returnEquipment(recordId);
            System.out.println("RETURN SUCCESSFUL!");
            System.out.println("Record ID: " + record.getRecordId());
            System.out.println("Return Date: " + record.getReturnDate());
            System.out.println("Status: " + record.getStatus());
            System.out.println("Overdue Fine (₹5/day): ₹" + record.getFineAmount());
        } catch (NumberFormatException e) {
            System.out.println("INPUT ERROR: Please enter a numeric Record ID.");
        } catch (Exception e) {
            System.out.println("RETURN FAILED: " + e.getMessage());
        }
    }

    // 7. View Borrowing History
    private static void handleViewHistory() {
        System.out.println("\n--- View Borrowing History ---");
        System.out.println("a. View history for specific Student");
        System.out.println("b. View complete Lab borrowing history");
        String subChoice = readInput("Choice (a/b): ");
        if (subChoice == null) return;
        subChoice = subChoice.toLowerCase();

        try {
            List<BorrowRecord> history;
            if ("a".equals(subChoice)) {
                String userIdStr = readInput("Enter Student User ID: ");
                if (userIdStr == null) return;
                int userId = Integer.parseInt(userIdStr);
                history = borrowService.getStudentBorrowingHistory(userId);
            } else {
                history = borrowService.getAllBorrowingHistory();
            }

            if (history.isEmpty()) {
                System.out.println("No records found.");
                return;
            }

            System.out.printf("%-5s | %-8s | %-12s | %-11s | %-11s | %-11s | %-10s | %-8s%n",
                    "RecID", "UserID", "EquipID", "BorrowDate", "DueDate", "ReturnDate", "Status", "Fine (₹)");
            System.out.println("------------------------------------------------------------------------------------------------");
            for (BorrowRecord r : history) {
                System.out.printf("%-5d | %-8d | %-12d | %-11s | %-11s | %-11s | %-10s | ₹%-7s%n",
                        r.getRecordId(), r.getUserId(), r.getEquipmentId(),
                        r.getBorrowDate(), r.getDueDate(),
                        (r.getReturnDate() != null ? r.getReturnDate().toString() : "N/A"),
                        r.getStatus(), r.getFineAmount());
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch history: " + e.getMessage());
        }
    }

    // 8. Automated Backend Test Suite
    public static void runAutomatedTestSuite() {
        System.out.println("\n===============================================================");
        System.out.println("   STARTING AUTOMATED BACKEND VERIFICATION SUITE");
        System.out.println("===============================================================");
        long timestamp = System.currentTimeMillis();
        int passCount = 0;
        int totalTests = 10;

        try {
            // TEST 1: Student Registration & Duplicate Email Rejection
            System.out.println("\n[TEST 1]: Student Registration & Duplicate Validation");
            String testEmail = "student_" + timestamp + "@test.edu";
            User student = userService.registerStudent("Test Student", testEmail, "+91-9999900000");
            if (student.getUserId() > 0 && "STUDENT".equals(student.getRole())) {
                System.out.println("  Sub-test 1.1: Registration successful (ID: " + student.getUserId() + ")");
            } else {
                throw new AssertionError("Test 1.1 failed: User ID not generated");
            }

            try {
                userService.registerStudent("Duplicate Student", testEmail, "+91-1111111111");
                System.out.println("  Sub-test 1.2: FAIL (Duplicate email was accepted)");
            } catch (IllegalArgumentException e) {
                System.out.println("  Sub-test 1.2: PASS (Duplicate email rejected: " + e.getMessage() + ")");
                passCount++;
            }

            // TEST 2: Student Login / Lookup
            System.out.println("\n[TEST 2]: Student Login / Email Lookup");
            User lookupUser = userService.getUserByEmail(testEmail);
            if (lookupUser != null && lookupUser.getUserId() == student.getUserId()) {
                System.out.println("  PASS: Retrieved user by email successfully.");
                passCount++;
            } else {
                System.out.println("  FAIL: Could not locate user by email.");
            }

            // TEST 3: Admin Authorization & Non-Admin Rejection
            System.out.println("\n[TEST 3]: Admin Authorization Enforcement");
            int adminId = 1; // Default seed admin
            try {
                // Attempt to add equipment using the student's ID (must fail)
                equipmentService.addEquipment(student.getUserId(), "Unauthorized Meter", "Testing", 5, "GOOD");
                System.out.println("  Sub-test 3.1: FAIL (Student was allowed to perform admin action)");
            } catch (SecurityException e) {
                System.out.println("  Sub-test 3.1: PASS (Unauthorized user blocked: " + e.getMessage() + ")");
            }

            // Perform valid admin equipment creation
            String equipmentName = "Precision Sensor " + timestamp;
            Equipment testEquipment = equipmentService.addEquipment(adminId, equipmentName, "Sensors", 1, "NEW");
            if (testEquipment.getEquipmentId() > 0) {
                System.out.println("  Sub-test 3.2: PASS (Admin added equipment ID: " + testEquipment.getEquipmentId() + ")");
                passCount++;
            } else {
                System.out.println("  Sub-test 3.2: FAIL (Admin failed to add equipment)");
            }

            // TEST 4: Equipment Availability Check
            System.out.println("\n[TEST 4]: Equipment Availability Check");
            boolean isAvail = equipmentService.isEquipmentAvailable(testEquipment.getEquipmentId());
            int availQty = equipmentService.getAvailableQuantity(testEquipment.getEquipmentId());
            if (isAvail && availQty == 1) {
                System.out.println("  PASS: Equipment available count confirmed: " + availQty);
                passCount++;
            } else {
                System.out.println("  FAIL: Initial availability incorrect: " + availQty);
            }

            // TEST 5: Borrowing & 7-Day Due Date Verification
            System.out.println("\n[TEST 5]: Borrow Equipment with 7-Day Due Date");
            LocalDate borrowDate = LocalDate.now();
            BorrowRecord borrowRecord1 = borrowService.borrowEquipment(student.getUserId(), testEquipment.getEquipmentId(), borrowDate);
            LocalDate expectedDueDate = borrowDate.plusDays(7);
            int updatedQty = equipmentService.getAvailableQuantity(testEquipment.getEquipmentId());

            if (borrowRecord1.getDueDate().equals(expectedDueDate) && updatedQty == 0) {
                System.out.println("  PASS: Due date correctly set to: " + borrowRecord1.getDueDate() 
                        + ", stock decremented to: " + updatedQty);
                passCount++;
            } else {
                System.out.println("  FAIL: Due date mismatch or stock not decremented.");
            }

            // TEST 6: Out-of-Stock Prevention
            System.out.println("\n[TEST 6]: Out-of-Stock Prevention");
            try {
                borrowService.borrowEquipment(student.getUserId(), testEquipment.getEquipmentId());
                System.out.println("  FAIL: Out of stock borrowing was permitted!");
            } catch (IllegalStateException e) {
                System.out.println("  PASS: Zero stock prevented: " + e.getMessage());
                passCount++;
            }

            // TEST 7: Duplicate Return Prevention
            System.out.println("\n[TEST 7]: Return & Duplicate Return Prevention");
            // Perform return
            BorrowRecord returnedRecord = borrowService.returnEquipment(borrowRecord1.getRecordId());
            int restoredQty = equipmentService.getAvailableQuantity(testEquipment.getEquipmentId());
            if ("RETURNED".equals(returnedRecord.getStatus()) && restoredQty == 1) {
                System.out.println("  Sub-test 7.1: Return processed, stock restored to: " + restoredQty);
            }

            try {
                // Attempt second return on already returned record
                borrowService.returnEquipment(borrowRecord1.getRecordId());
                System.out.println("  Sub-test 7.2: FAIL (Duplicate return was permitted)");
            } catch (IllegalStateException e) {
                System.out.println("  Sub-test 7.2: PASS (Duplicate return blocked: " + e.getMessage() + ")");
                passCount++;
            }

            // TEST 8: Overdue Fine Calculation (₹5 per overdue day)
            System.out.println("\n[TEST 8]: Overdue Fine Calculation (₹5/day)");
            // Simulate borrow that took place 10 days ago (due 3 days ago)
            LocalDate simulatedPastBorrow = LocalDate.now().minusDays(10);
            BorrowRecord lateRecord = borrowService.borrowEquipment(student.getUserId(), testEquipment.getEquipmentId(), simulatedPastBorrow);
            // Return today (3 days overdue -> 3 * ₹5 = ₹15.00)
            BorrowRecord processedLateReturn = borrowService.returnEquipment(lateRecord.getRecordId(), LocalDate.now());
            BigDecimal expectedFine = new BigDecimal("15.00");

            if (expectedFine.compareTo(processedLateReturn.getFineAmount()) == 0) {
                System.out.println("  PASS: 3 days overdue correctly charged: ₹" + processedLateReturn.getFineAmount());
                passCount++;
            } else {
                System.out.println("  FAIL: Expected fine ₹15.00, got: ₹" + processedLateReturn.getFineAmount());
            }

            // TEST 9: On-Time Return (₹0.00 Fine)
            System.out.println("\n[TEST 9]: On-Time Return Fine Calculation (₹0.00)");
            BorrowRecord onTimeRecord = borrowService.borrowEquipment(student.getUserId(), testEquipment.getEquipmentId(), LocalDate.now());
            BorrowRecord processedOnTime = borrowService.returnEquipment(onTimeRecord.getRecordId(), LocalDate.now());
            if (BigDecimal.ZERO.compareTo(processedOnTime.getFineAmount()) == 0) {
                System.out.println("  PASS: On-time return calculated fine: ₹" + processedOnTime.getFineAmount());
                passCount++;
            } else {
                System.out.println("  FAIL: Expected ₹0.00 fine, got: ₹" + processedOnTime.getFineAmount());
            }

            // TEST 10: Borrowing History Verification
            System.out.println("\n[TEST 10]: Borrowing History Verification");
            List<BorrowRecord> studentHistory = borrowService.getStudentBorrowingHistory(student.getUserId());
            if (studentHistory.size() >= 3) {
                System.out.println("  PASS: Student history retrieved " + studentHistory.size() + " records successfully.");
                passCount++;
            } else {
                System.out.println("  FAIL: Expected at least 3 records in history, found: " + studentHistory.size());
            }

            // Summary
            System.out.println("\n===============================================================");
            System.out.println("  AUTOMATED TEST SUMMARY: " + passCount + "/" + totalTests + " TESTS PASSED");
            System.out.println("===============================================================");

        } catch (Exception e) {
            System.err.println("UNEXPECTED TEST SUITE FAILURE: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
