
package service;

import dao.BorrowRecordDAO;
import dao.EquipmentDAO;
import dao.UserDAO;
import exception.DuplicateReturnException;
import exception.EquipmentUnavailableException;
import exception.InvalidBorrowRequestException;
import exception.UnauthorizedBorrowingException;
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
 * Handles transactions, authorization and fine calculations.
 */
public class BorrowService implements BorrowOperations {

    public static final int STANDARD_BORROW_DAYS = 7;

    public static final BigDecimal FINE_PER_OVERDUE_DAY =
            new BigDecimal("5.00");

    private final BorrowRecordDAO borrowRecordDAO;
    private final EquipmentDAO equipmentDAO;
    private final UserDAO userDAO;

    public BorrowService() {
        this.borrowRecordDAO = new BorrowRecordDAO();
        this.equipmentDAO = new EquipmentDAO();
        this.userDAO = new UserDAO();
    }

    public BorrowService(BorrowRecordDAO borrowRecordDAO,
                         EquipmentDAO equipmentDAO,
                         UserDAO userDAO) {
        this.borrowRecordDAO = borrowRecordDAO;
        this.equipmentDAO = equipmentDAO;
        this.userDAO = userDAO;
    }

    /**
     * Borrows equipment for a student.
     */
    public BorrowRecord borrowEquipment(int userId, int equipmentId)
            throws SQLException, InvalidBorrowRequestException, EquipmentUnavailableException,
                   UnauthorizedBorrowingException, DuplicateReturnException {
        return borrowEquipment(userId, equipmentId, LocalDate.now());
    }

    /**
     * Borrows equipment with a specified date.
     * Custom date is useful for automated testing.
     */
    public BorrowRecord borrowEquipment(int userId, int equipmentId,
                                        LocalDate borrowDate)
            throws SQLException, InvalidBorrowRequestException, EquipmentUnavailableException,
                   UnauthorizedBorrowingException, DuplicateReturnException {

        if (borrowDate == null) {
            borrowDate = LocalDate.now();
        }

        User user = userDAO.findById(userId);

        if (user == null) {
            throw new InvalidBorrowRequestException(
                    "User with ID " + userId + " does not exist.");
        }

        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            throw new InvalidBorrowRequestException(
                    "Only students can borrow equipment.");
        }

        Equipment equipment = equipmentDAO.findById(equipmentId);

        if (equipment == null) {
            throw new InvalidBorrowRequestException(
                    "Equipment with ID " + equipmentId + " does not exist.");
        }

        if (equipment.getAvailableQuantity() <= 0) {
            throw new EquipmentUnavailableException(
                    "Equipment '" + equipment.getName()
                    + "' is out of stock.");
        }

        LocalDate dueDate =
                borrowDate.plusDays(STANDARD_BORROW_DAYS);

        BorrowRecord record =
                new BorrowRecord(userId, equipmentId,
                                 borrowDate, dueDate);

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            boolean decremented =
                    equipmentDAO.decrementAvailableQuantity(
                            conn, equipmentId);

            if (!decremented) {
                throw new EquipmentUnavailableException(
                        "Equipment is out of stock.");
            }

            int generatedId = borrowRecordDAO.insert(conn, record);

            if (generatedId <= 0) {
                throw new SQLException(
                        "Failed to create borrow record.");
            }

            conn.commit();
            return record;

        } catch (Exception e) {
            rollback(conn);
            throw e;

        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Returns equipment after checking the requesting user's identity.
     * Students can return only their own records.
     * Admins can return any record.
     */
    public BorrowRecord returnEquipment(int recordId,
                                        int requestingUserId)
            throws SQLException, InvalidBorrowRequestException, EquipmentUnavailableException,
                   UnauthorizedBorrowingException, DuplicateReturnException {

        return returnEquipment(
                recordId, requestingUserId, LocalDate.now());
    }

    /**
     * Return method with a specified date for automated testing.
     */
    public BorrowRecord returnEquipment(int recordId,
                                        int requestingUserId,
                                        LocalDate returnDate)
            throws SQLException, InvalidBorrowRequestException, EquipmentUnavailableException,
                   UnauthorizedBorrowingException, DuplicateReturnException {

        User requestingUser = userDAO.findById(requestingUserId);

        if (requestingUser == null) {
            throw new UnauthorizedBorrowingException(
                    "User does not exist.");
        }

        if (!"STUDENT".equalsIgnoreCase(requestingUser.getRole())
                && !"ADMIN".equalsIgnoreCase(requestingUser.getRole())) {
            throw new UnauthorizedBorrowingException(
                    "Unauthorized user.");
        }

        BorrowRecord record = borrowRecordDAO.findById(recordId);

        if (record == null) {
            throw new InvalidBorrowRequestException(
                    "Borrow record with ID " + recordId + " does not exist.");
        }

        if ("STUDENT".equalsIgnoreCase(requestingUser.getRole())
                && record.getUserId() != requestingUserId) {
            throw new UnauthorizedBorrowingException(
                    "Students can return only their own equipment.");
        }

        return returnEquipmentInternal(record, returnDate);
    }

    /**
     * Performs the actual return transaction.
     */
    private BorrowRecord returnEquipmentInternal(
            BorrowRecord record, LocalDate returnDate)
            throws SQLException, InvalidBorrowRequestException, EquipmentUnavailableException,
                   UnauthorizedBorrowingException, DuplicateReturnException {

        int recordId = record.getRecordId();

        if (returnDate == null) {
            returnDate = LocalDate.now();
        }

        if ("RETURNED".equalsIgnoreCase(record.getStatus())) {
            throw new DuplicateReturnException(
                    "Record has already been returned.");
        }

        if (returnDate.isBefore(record.getBorrowDate())) {
            throw new InvalidBorrowRequestException(
                    "Return date cannot be before borrow date.");
        }

        BigDecimal fineAmount =
                calculateFine(record.getDueDate(), returnDate);

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            boolean updated = borrowRecordDAO.updateReturn(
                    conn, recordId, returnDate, fineAmount);

            if (!updated) {
                throw new IllegalStateException(
                        "Record not found or already returned.");
            }

            boolean incremented =
                    equipmentDAO.incrementAvailableQuantity(
                            conn, record.getEquipmentId());

            if (!incremented) {
                throw new IllegalStateException(
                        "Failed to restore equipment stock.");
            }

            conn.commit();

            record.setReturnDate(returnDate);
            record.setFineAmount(fineAmount);
            record.setStatus("RETURNED");

            return record;

        } catch (Exception e) {
            rollback(conn);
            throw e;

        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Calculates overdue fine at ₹5 per day.
     */
    public BigDecimal calculateFine(LocalDate dueDate,
                                    LocalDate returnDate) {

        if (dueDate == null || returnDate == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long overdueDays =
                ChronoUnit.DAYS.between(dueDate, returnDate);

        if (overdueDays > 0) {
            return FINE_PER_OVERDUE_DAY
                    .multiply(BigDecimal.valueOf(overdueDays))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Retrieves borrowing history for a student.
     */
    public List<BorrowRecord> getStudentBorrowingHistory(int userId)
            throws SQLException {
        return borrowRecordDAO.findByUserId(userId);
    }

    /**
     * Retrieves active borrowing records for a student.
     */
    public List<BorrowRecord> getActiveBorrowRecords(int userId)
            throws SQLException {
        return borrowRecordDAO.findActiveByUserId(userId);
    }

    /**
     * Retrieves all borrowing history.
     */
    public List<BorrowRecord> getAllBorrowingHistory()
            throws SQLException {
        return borrowRecordDAO.findAll();
    }

    /**
     * Retrieves a borrow record by ID.
     */
    public BorrowRecord getBorrowRecordById(int recordId)
            throws SQLException {
        return borrowRecordDAO.findById(recordId);
    }

    /**
     * Rolls back a failed transaction.
     */
    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                System.err.println(
                        "Transaction rollback failed: " + e.getMessage());
            }
        }
    }

    /**
     * Closes the database connection safely.
     */
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
                // Continue closing the connection.
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println(
                            "Connection close failed: " + e.getMessage());
                }
            }
        }
    }
}
