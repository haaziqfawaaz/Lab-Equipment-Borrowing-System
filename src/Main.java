
import database.DatabaseConnection;
import model.BorrowRecord;
import model.Equipment;
import model.User;
import service.BorrowService;
import service.EquipmentService;
import service.UserService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Main application for Lab Equipment Borrowing and Return System.
 * Provides student registration, login, admin management,
 * equipment borrowing, returning and history.
 */
public class Main {

    private static final UserService userService = new UserService();
    private static final EquipmentService equipmentService = new EquipmentService();
    private static final BorrowService borrowService = new BorrowService();

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        System.out.println("==================================================");
        System.out.println(" LAB EQUIPMENT BORROWING AND RETURN SYSTEM");
        System.out.println("==================================================");

        boolean running = true;

        while (running) {

            printMenu();

            String choice = readInput("Enter your choice: ");

            if (choice == null) {
                break;
            }

            try {
                switch (choice) {

                    case "1" -> registerStudent();

                    case "2" -> login();

                    case "3" -> viewEquipment();

                    case "4" -> borrowEquipment();

                    case "5" -> returnEquipment();

                    case "6" -> viewHistory();

                    case "7" -> addEquipment();

                    case "8" -> registerAdmin();

                    case "9" -> logout();

                    case "10" -> runAutomatedTestSuite();

                    case "0" -> {
                        logout();
                        System.out.println("Thank you for using the system!");
                        running = false;
                    }

                    default -> System.out.println("Invalid choice.");
                }

            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }

        scanner.close();
    }

    /**
     * Displays the main menu.
     */
    private static void printMenu() {

        System.out.println("\n---------------- MAIN MENU ----------------");

        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            System.out.println("Current User: Guest");
        } else {
            System.out.println("Logged in: " + currentUser.getName()
                    + " (" + currentUser.getRole() + ")");
        }

        System.out.println("--------------------------------------------");
        System.out.println("1. Register Student");
        System.out.println("2. Login");
        System.out.println("3. View Equipment");
        System.out.println("4. Borrow Equipment");
        System.out.println("5. Return Equipment");
        System.out.println("6. View Borrowing History");
        System.out.println("7. Add Equipment (Admin Only)");
        System.out.println("8. Register New Admin (Admin Only)");
        System.out.println("9. Logout");
        System.out.println("10. Run Automated Test Suite");
        System.out.println("0. Exit");
        System.out.println("--------------------------------------------");
    }

    /**
     * Safely reads console input.
     */
    private static String readInput(String message) {

        System.out.print(message);

        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }

        return null;
    }

    /**
     * Requires a logged-in user.
     */
    private static User requireLogin() {

        User user = userService.getCurrentUser();

        if (user == null) {
            System.out.println("Please login first.");
            return null;
        }

        return user;
    }

    /**
     * Requires a logged-in administrator.
     */
    private static User requireAdmin() throws SQLException {

        User user = requireLogin();

        if (user == null) {
            return null;
        }

        if (!userService.isAdminLoggedIn()) {
            System.out.println("Access denied. Admin privileges required.");
            return null;
        }

        return user;
    }

    /**
     * 1. Student Registration
     */
    private static void registerStudent() throws SQLException {

        System.out.println("\n--- Student Registration ---");

        String name = readInput("Enter Student Name: ");
        String email = readInput("Enter Student Email: ");
        String contact = readInput("Enter Contact Number: ");
        String password = readInput("Create Password (minimum 8 characters): ");

        if (name == null || email == null ||
                contact == null || password == null) {
            return;
        }

        User student = userService.registerStudent(
                name, email, contact, password);

        System.out.println("Student registered successfully!");
        System.out.println("Student ID: " + student.getUserId());
        System.out.println("Name: " + student.getName());
        System.out.println("Role: " + student.getRole());
    }

    /**
     * 2. User Login
     */
    private static void login() throws SQLException {

        if (userService.isLoggedIn()) {
            System.out.println("Please logout before logging in again.");
            return;
        }

        System.out.println("\n--- User Login ---");

        String email = readInput("Enter Email: ");
        String password = readInput("Enter Password: ");

        if (email == null || password == null) {
            return;
        }

        User user = userService.login(email, password);

        if (user == null) {
            System.out.println("Invalid email or password.");
            return;
        }

        System.out.println("Login successful!");
        System.out.println("Welcome, " + user.getName());
        System.out.println("Role: " + user.getRole());
    }

    /**
     * 3. View Equipment
     */
    private static void viewEquipment() throws SQLException {

        System.out.println("\n--- Equipment Inventory ---");

        List<Equipment> equipmentList =
                equipmentService.getAllEquipment();

        if (equipmentList.isEmpty()) {
            System.out.println("No equipment available.");
            return;
        }

        System.out.printf("%-5s %-30s %-15s %-10s %-10s%n",
                "ID", "Name", "Category", "Total", "Available");

        for (Equipment equipment : equipmentList) {

            System.out.printf("%-5d %-30s %-15s %-10d %-10d%n",
                    equipment.getEquipmentId(),
                    equipment.getName(),
                    equipment.getCategory(),
                    equipment.getTotalQuantity(),
                    equipment.getAvailableQuantity());
        }
    }

    /**
     * 4. Borrow Equipment
     */
    private static void borrowEquipment() throws SQLException {

        User user = requireLogin();

        if (user == null) {
            return;
        }

        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            System.out.println("Only students can borrow equipment.");
            return;
        }

        System.out.println("\n--- Borrow Equipment ---");

        String equipmentInput =
                readInput("Enter Equipment ID: ");

        if (equipmentInput == null) {
            return;
        }

        int equipmentId = Integer.parseInt(equipmentInput);

        BorrowRecord record = borrowService.borrowEquipment(
                user.getUserId(), equipmentId);

        System.out.println("Borrow successful!");
        System.out.println("Record ID: " + record.getRecordId());
        System.out.println("Borrow Date: " + record.getBorrowDate());
        System.out.println("Due Date: " + record.getDueDate());
        System.out.println("Borrowing Period: 7 Days");
    }

    /**
     * 5. Return Equipment
     */
    private static void returnEquipment() throws SQLException {

        User user = requireLogin();

        if (user == null) {
            return;
        }

        System.out.println("\n--- Return Equipment ---");

        String recordInput =
                readInput("Enter Borrow Record ID: ");

        if (recordInput == null) {
            return;
        }

        int recordId = Integer.parseInt(recordInput);

        BorrowRecord record = borrowService.returnEquipment(
                recordId, user.getUserId());

        System.out.println("Equipment returned successfully!");
        System.out.println("Return Date: " + record.getReturnDate());
        System.out.println("Status: " + record.getStatus());
        System.out.println("Fine: Rs. " + record.getFineAmount());
    }

    /**
     * 6. View Borrowing History
     */
    private static void viewHistory() throws SQLException {

        User user = requireLogin();

        if (user == null) {
            return;
        }

        List<BorrowRecord> history;

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            history = borrowService.getAllBorrowingHistory();
        } else {
            history = borrowService.getStudentBorrowingHistory(
                    user.getUserId());
        }

        System.out.println("\n--- Borrowing History ---");

        if (history.isEmpty()) {
            System.out.println("No borrowing records found.");
            return;
        }

        for (BorrowRecord record : history) {

            System.out.println("----------------------------------");
            System.out.println("Record ID: " + record.getRecordId());
            System.out.println("Student ID: " + record.getUserId());
            System.out.println("Equipment ID: " + record.getEquipmentId());
            System.out.println("Borrow Date: " + record.getBorrowDate());
            System.out.println("Due Date: " + record.getDueDate());
            System.out.println("Return Date: " + record.getReturnDate());
            System.out.println("Status: " + record.getStatus());
            System.out.println("Fine: Rs. " + record.getFineAmount());
        }
    }

    /**
     * 7. Add Equipment - Admin Only
     */
    private static void addEquipment() throws SQLException {

        User admin = requireAdmin();

        if (admin == null) {
            return;
        }

        System.out.println("\n--- Add Equipment ---");

        String name = readInput("Enter Equipment Name: ");
        String category = readInput("Enter Category: ");
        String quantityInput = readInput("Enter Quantity: ");
        String condition = readInput("Enter Condition: ");

        if (name == null || category == null ||
                quantityInput == null || condition == null) {
            return;
        }

        int quantity = Integer.parseInt(quantityInput);

        Equipment equipment = equipmentService.addEquipment(
                admin.getUserId(),
                name,
                category,
                quantity,
                condition);

        System.out.println("Equipment added successfully!");
        System.out.println("Equipment ID: " + equipment.getEquipmentId());
    }

    /**
     * 8. Register another administrator.
     */
    private static void registerAdmin() throws SQLException {

        User admin = requireAdmin();

        if (admin == null) {
            return;
        }

        System.out.println("\n--- Register New Administrator ---");

        String name = readInput("Enter Admin Name: ");
        String email = readInput("Enter Admin Email: ");
        String contact = readInput("Enter Contact Number: ");
        String password = readInput("Create Password (minimum 8 characters): ");

        if (name == null || email == null ||
                contact == null || password == null) {
            return;
        }

        User newAdmin = userService.registerAdmin(
                name, email, contact, password);

        System.out.println("New administrator registered successfully!");
        System.out.println("Admin ID: " + newAdmin.getUserId());
        System.out.println("Admin Name: " + newAdmin.getName());
    }

    /**
     * 9. Logout
     */
    private static void logout() {

        if (!userService.isLoggedIn()) {
            System.out.println("No user is currently logged in.");
            return;
        }

        userService.logout();

        System.out.println("Logout successful!");
    }

    /**
     * 10. Automated Backend Test Suite
     */
    public static void runAutomatedTestSuite() {

        System.out.println("\n==========================================");
        System.out.println(" AUTOMATED BACKEND TEST SUITE");
        System.out.println("==========================================");

        int passed = 0;
        int total = 10;

        long timestamp = System.currentTimeMillis();

        try {
            String testEmail = "test_" + timestamp + "@test.edu";

            // Test 1: Student Registration
            System.out.println("\nTEST 1: Student Registration");

            User student = userService.registerStudent(
                    "Test Student",
                    testEmail,
                    "9999999999",
                    "TestPass123!");

            if (student.getUserId() > 0) {
                System.out.println("PASS: Student registered.");
                passed++;
            }

            // Test 2: Duplicate Email
            System.out.println("\nTEST 2: Duplicate Email Validation");

            try {
                userService.registerStudent(
                        "Duplicate Student",
                        testEmail,
                        "9999999998",
                        "TestPass123!");

                System.out.println("FAIL: Duplicate email accepted.");

            } catch (IllegalArgumentException e) {
                System.out.println("PASS: Duplicate email rejected.");
                passed++;
            }

            // Test 3: Password Authentication
            System.out.println("\nTEST 3: Password Authentication");

            User testLogin = userService.login(
                    testEmail, "TestPass123!");

            if (testLogin != null) {
                System.out.println("PASS: Correct password accepted.");
                passed++;
            }

            userService.logout();

            // Test 4: Incorrect Password
            System.out.println("\nTEST 4: Incorrect Password");

            User wrongLogin = userService.login(
                    testEmail, "WrongPassword123!");

            if (wrongLogin == null) {
                System.out.println("PASS: Incorrect password rejected.");
                passed++;
            }

            // Test 5: Admin Authorization
            System.out.println("\nTEST 5: Admin Authorization");

            int adminId = 1;

            if (userService.isAdmin(adminId)) {
                System.out.println("PASS: Admin account verified.");
                passed++;
            }

            // Test 6: Equipment Creation
            System.out.println("\nTEST 6: Equipment Creation");

            String equipmentName = "Test Equipment " + timestamp;

            Equipment equipment = equipmentService.addEquipment(
                    adminId,
                    equipmentName,
                    "Testing",
                    1,
                    "GOOD");

            if (equipment.getEquipmentId() > 0) {
                System.out.println("PASS: Equipment added.");
                passed++;
            }

            // Test 7: Equipment Availability
            System.out.println("\nTEST 7: Equipment Availability");

            if (equipmentService.isEquipmentAvailable(
                    equipment.getEquipmentId())
                    && equipmentService.getAvailableQuantity(
                    equipment.getEquipmentId()) == 1) {

                System.out.println("PASS: Equipment availability verified.");
                passed++;
            }

            // Test 8: Borrowing and Out-of-Stock Prevention
            System.out.println("\nTEST 8: Borrowing and Stock Validation");

            LocalDate borrowDate = LocalDate.now();

            BorrowRecord record = borrowService.borrowEquipment(
                    student.getUserId(),
                    equipment.getEquipmentId(),
                    borrowDate);

            if (record.getDueDate().equals(borrowDate.plusDays(7))
                    && equipmentService.getAvailableQuantity(
                    equipment.getEquipmentId()) == 0) {

                System.out.println("PASS: Seven-day due date and stock update verified.");
                passed++;
            }

            try {
                borrowService.borrowEquipment(
                        student.getUserId(),
                        equipment.getEquipmentId());

                System.out.println("FAIL: Out-of-stock borrowing accepted.");

            } catch (IllegalStateException e) {
                System.out.println("Out-of-stock borrowing correctly blocked.");
            }

            // Test 9: Return and Fine Calculation
            System.out.println("\nTEST 9: Return and Fine Calculation");

            BorrowRecord returned = borrowService.returnEquipment(
                    record.getRecordId(),
                    adminId);

            if ("RETURNED".equals(returned.getStatus())
                    && equipmentService.getAvailableQuantity(
                    equipment.getEquipmentId()) == 1
                    && BigDecimal.ZERO.compareTo(
                    returned.getFineAmount()) == 0) {

                System.out.println("PASS: Return, stock restoration and fine verified.");
                passed++;
            }

            // Test 10: Duplicate Return Prevention
            System.out.println("\nTEST 10: Duplicate Return Prevention");

            try {
                borrowService.returnEquipment(
                        record.getRecordId(),
                        adminId);

                System.out.println("FAIL: Duplicate return accepted.");

            } catch (IllegalStateException e) {
                System.out.println("PASS: Duplicate return blocked.");
                passed++;
            }

        } catch (Exception e) {

            System.out.println("TEST SUITE ERROR: " + e.getMessage());
            e.printStackTrace();

        } finally {

            userService.logout();

            System.out.println("\n==========================================");
            System.out.println(" TEST SUMMARY: " + passed + "/" + total
                    + " TESTS PASSED");
            System.out.println("==========================================");
        }
    }
}
