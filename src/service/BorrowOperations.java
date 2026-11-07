package service;

import model.BorrowRecord;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import exception.EquipmentUnavailableException;
import exception.InvalidBorrowRequestException;
import exception.UnauthorizedBorrowingException;
import exception.DuplicateReturnException;

/**
 * Contract for borrowing and returning equipment.
 * Controllers can depend on this interface instead of a concrete service class.
 */
public interface BorrowOperations {
    BorrowRecord borrowEquipment(int userId, int equipmentId) throws SQLException, InvalidBorrowRequestException, EquipmentUnavailableException, UnauthorizedBorrowingException, DuplicateReturnException;

    BorrowRecord borrowEquipment(int userId, int equipmentId, LocalDate borrowDate)
            throws SQLException, InvalidBorrowRequestException, EquipmentUnavailableException, UnauthorizedBorrowingException, DuplicateReturnException;

    BorrowRecord returnEquipment(int recordId, int requestingUserId)
            throws SQLException, InvalidBorrowRequestException, EquipmentUnavailableException, UnauthorizedBorrowingException, DuplicateReturnException;

    BorrowRecord returnEquipment(int recordId, int requestingUserId, LocalDate returnDate)
            throws SQLException, InvalidBorrowRequestException, EquipmentUnavailableException, UnauthorizedBorrowingException, DuplicateReturnException;

    BigDecimal calculateFine(LocalDate dueDate, LocalDate returnDate);

    List<BorrowRecord> getStudentBorrowingHistory(int userId) throws SQLException;

    List<BorrowRecord> getActiveBorrowRecords(int userId) throws SQLException;

    List<BorrowRecord> getAllBorrowingHistory() throws SQLException;

    BorrowRecord getBorrowRecordById(int recordId) throws SQLException;
}
