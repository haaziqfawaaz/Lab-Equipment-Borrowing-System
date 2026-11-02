package service;

import dao.BorrowRecordDAO;
import dao.EquipmentDAO;
import dao.UserDAO;
import database.DatabaseConnection;
import model.BorrowRecord;
import model.Equipment;
import model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service class for Equipment Borrowing and Returning.
 * Manages atomic transactions, overdue fine calculations using BigDecimal, and borrowing history.
 */
public class BorrowService {
    public static final int STANDARD_BORROW_DAYS = 7;
    public static final BigDecimal FINE_PER_OVERDUE_DAY = new BigDecimal("5.00"); // ₹5 per day late fee

    private final BorrowRecordDAO borrowRecordDAO;
    private final EquipmentDAO equipmentDAO;
    private final UserDAO userDAO;

    public BorrowService() {
        this.borrowRecordDAO = new BorrowRecordDAO();
        this.equipmentDAO = new EquipmentDAO();
        this.userDAO = new UserDAO();
    }

    public BorrowService(BorrowRecordDAO borrowRecordDAO, EquipmentDAO equipmentDAO, UserDAO userDAO) {
        this.borrowRecordDAO = borrowRecordDAO;
        this.equipmentDAO = equipmentDAO;
        this.userDAO = userDAO;
    }

    /**
     * Borrows an equipment item for a student with the standard 7-day due date.
     * Uses an atomic database transaction with commit and rollback.
     * @param userId ID of the borrowing student
     * @param equipmentId ID of the equipment to borrow
     * @return newly created BorrowRecord
     */
    public BorrowRecord borrowEquipment(int userId, int equipmentId) 
            throws SQLException, IllegalArgumentException, IllegalStateException {
        return borrowEquipment(userId, equipmentId, LocalDate.now());
    }

    /**
     * Overload to allow specifying a custom borrow date (useful for simulating/testing past dates).
     */
    public BorrowRecord borrowEquipment(int userId, int equipmentId, LocalDate borrowDate) 
            throws SQLException, IllegalArgumentException, IllegalStateException {
        if (borrowDate == null) {
            borrowDate = LocalDate.now();
        }

        // 1. Verify user exists and has STUDENT role
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException("Only students are authorized to borrow equipment. Current role: " + user.getRole());
        }

        // 2. Verify equipment exists and has available stock
        Equipment equipment = equipmentDAO.findById(equipmentId);
        if (equipment == null) {
            throw new IllegalArgumentException("Equipment with ID " + equipmentId + " does not exist.");
        }
        if (equipment.getAvailableQuantity() <= 0) {
            throw new IllegalStateException("Equipment '" + equipment.getName() + "' is out of stock (available: 0).");
        }

        LocalDate dueDate = borrowDate.plusDays(STANDARD_BORROW_DAYS);
        BorrowRecord record = new BorrowRecord(userId, equipmentId, borrowDate, dueDate);

        // 3. Execute atomic transaction (decrement stock + insert borrow record)
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Atomically decrement stock with SQL constraint (available_quantity > 0)
            boolean decremented = equipmentDAO.decrementAvailableQuantity(conn, equipmentId);
            if (!decremented) {
                throw new IllegalStateException("Stock decrement failed: Equipment is out of stock.");
            }

            int generatedRecordId = borrowRecordDAO.insert(conn, record);
            if (generatedRecordId <= 0) {
                throw new SQLException("Failed to create borrow record in database.");
            }

            conn.commit(); // Transaction successful: both changes committed together
            return record;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback all changes on failure
                } catch (SQLException rollbackEx) {
                    System.err.println("Transaction rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            // Robust cleanup: ensure close() is always called even if setAutoCommit(true) throws
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Ignore auto-commit reset failure so connection closure proceeds unconditionally
                } finally {
                    try {
                        conn.close();
                    } catch (SQLException closeEx) {
                        System.err.println("Failed to close connection: " + closeEx.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Returns an equipment item and calculates overdue fine (₹5 per overdue day using BigDecimal).
     * Enforces atomic transaction and prevents duplicate returns.
     * @param recordId ID of the borrow record
     * @return updated BorrowRecord
     */
    public BorrowRecord returnEquipment(int recordId) 
            throws SQLException, IllegalArgumentException, IllegalStateException {
        return returnEquipment(recordId, LocalDate.now());
    }

    /**
     * Overload to allow specifying an explicit return date (useful for simulating/testing late returns).
     */
    public BorrowRecord returnEquipment(int recordId, LocalDate returnDate) 
            throws SQLException, IllegalArgumentException, IllegalStateException {
        if (returnDate == null) {
            returnDate = LocalDate.now();
        }

        // 1. Fetch borrow record
        BorrowRecord record = borrowRecordDAO.findById(recordId);
        if (record == null) {
            throw new IllegalArgumentException("Borrow record with ID " + recordId + " does not exist.");
        }

        // 2. Prevent duplicate returns (memory check)
        if ("RETURNED".equalsIgnoreCase(record.getStatus())) {
            throw new IllegalStateException("Duplicate return prevented: Record ID " + recordId + " has already been returned.");
        }

        if (returnDate.isBefore(record.getBorrowDate())) {
            throw new IllegalArgumentException("Return date (" + returnDate + ") cannot be before borrow date (" + record.getBorrowDate() + ").");
        }

        // 3. Calculate fine using BigDecimal for financial accuracy
        BigDecimal fineAmount = calculateFine(record.getDueDate(), returnDate);

        // 4. Execute atomic transaction (update return record + increment stock)
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Guarded SQL update: WHERE record_id = ? AND status = 'BORROWED'
            boolean updated = borrowRecordDAO.updateReturn(conn, recordId, returnDate, fineAmount);
            if (!updated) {
                throw new IllegalStateException("Duplicate return prevented: Record not found or already returned.");
            }

            // Restore equipment inventory
            boolean incremented = equipmentDAO.incrementAvailableQuantity(conn, record.getEquipmentId());
            if (!incremented) {
                throw new IllegalStateException("Failed to increment equipment stock.");
            }

            conn.commit(); // Transaction successful: both changes committed together

            // Update in-memory record state
            record.setReturnDate(returnDate);
            record.setFineAmount(fineAmount);
            record.setStatus("RETURNED");

            return record;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback all changes on failure
                } catch (SQLException rollbackEx) {
                    System.err.println("Transaction rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            // Robust cleanup: ensure close() is always called even if setAutoCommit(true) throws
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Ignore auto-commit reset failure so connection closure proceeds unconditionally
                } finally {
                    try {
                        conn.close();
                    } catch (SQLException closeEx) {
                        System.err.println("Failed to close connection: " + closeEx.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Calculates the overdue fine based on due date and return date using BigDecimal.
     * @param dueDate scheduled due date
     * @param returnDate actual date of return
     * @return calculated fine amount in ₹ (₹5.00 per overdue day)
     */
    public BigDecimal calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (dueDate == null || returnDate == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        if (overdueDays > 0) {
            return FINE_PER_OVERDUE_DAY.multiply(BigDecimal.valueOf(overdueDays)).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Retrieves the complete borrowing history for a specific student.
     */
    public List<BorrowRecord> getStudentBorrowingHistory(int userId) throws SQLException {
        return borrowRecordDAO.findByUserId(userId);
    }

    /**
     * Retrieves all currently active (unreturned) borrow records for a student.
     */
    public List<BorrowRecord> getActiveBorrowRecords(int userId) throws SQLException {
        return borrowRecordDAO.findActiveByUserId(userId);
    }

    /**
     * Retrieves all borrow records across the entire laboratory.
     */
    public List<BorrowRecord> getAllBorrowingHistory() throws SQLException {
        return borrowRecordDAO.findAll();
    }

    /**
     * Finds a single borrow record by its primary key ID.
     */
    public BorrowRecord getBorrowRecordById(int recordId) throws SQLException {
        return borrowRecordDAO.findById(recordId);
    }
}
