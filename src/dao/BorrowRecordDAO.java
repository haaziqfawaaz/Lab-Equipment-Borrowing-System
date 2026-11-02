package dao;

import database.DatabaseConnection;
import model.BorrowRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Borrow and Return Records.
 * Uses PreparedStatement for all database queries, BigDecimal for fines, and supports transactions.
 */
public class BorrowRecordDAO {

    /**
     * Inserts a new borrow record using an active connection (within a transaction).
     * @param conn active database transaction connection
     * @param record borrow record to insert
     * @return generated record_id
     */
    public int insert(Connection conn, BorrowRecord record) throws SQLException {
        String sql = "INSERT INTO borrow_records (user_id, equipment_id, borrow_date, due_date, return_date, status, fine_amount) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getEquipmentId());
            pstmt.setDate(3, Date.valueOf(record.getBorrowDate()));
            pstmt.setDate(4, Date.valueOf(record.getDueDate()));
            if (record.getReturnDate() != null) {
                pstmt.setDate(5, Date.valueOf(record.getReturnDate()));
            } else {
                pstmt.setNull(5, java.sql.Types.DATE);
            }
            pstmt.setString(6, record.getStatus());
            BigDecimal fine = (record.getFineAmount() != null) ? record.getFineAmount() : BigDecimal.ZERO;
            pstmt.setBigDecimal(7, fine);

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    record.setRecordId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1;
    }

    /**
     * Standalone insert without external transaction.
     * @param record borrow record to insert
     * @return generated record_id
     */
    public int insert(BorrowRecord record) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return insert(conn, record);
        }
    }

    /**
     * Updates an active borrow record upon return.
     * Guarded with WHERE record_id = ? AND status = 'BORROWED' to prevent duplicate returns.
     * Uses BigDecimal for financial fine accuracy.
     * @param conn active database transaction connection
     * @param recordId ID of the borrow record
     * @param returnDate actual date of return
     * @param fineAmount calculated fine amount as BigDecimal
     * @return true if updated (rows affected > 0), false if not found or already returned
     */
    public boolean updateReturn(Connection conn, int recordId, LocalDate returnDate, BigDecimal fineAmount) throws SQLException {
        String sql = "UPDATE borrow_records SET return_date = ?, fine_amount = ?, status = 'RETURNED' " +
                     "WHERE record_id = ? AND status = 'BORROWED'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(returnDate));
            BigDecimal fine = (fineAmount != null) ? fineAmount : BigDecimal.ZERO;
            pstmt.setBigDecimal(2, fine);
            pstmt.setInt(3, recordId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Finds a borrow record by its primary key ID.
     * @param recordId ID of the record
     * @return BorrowRecord or null if not found
     */
    public BorrowRecord findById(int recordId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return findById(conn, recordId);
        }
    }

    /**
     * Finds a borrow record by ID using an active connection (within a transaction).
     * @param conn active database connection
     * @param recordId ID of the record
     * @return BorrowRecord or null if not found
     */
    public BorrowRecord findById(Connection conn, int recordId) throws SQLException {
        String sql = "SELECT record_id, user_id, equipment_id, borrow_date, due_date, return_date, status, fine_amount " +
                     "FROM borrow_records WHERE record_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recordId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBorrowRecord(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all borrow records for a specific user (borrowing history).
     * @param userId user ID
     * @return list of borrow records
     */
    public List<BorrowRecord> findByUserId(int userId) throws SQLException {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT record_id, user_id, equipment_id, borrow_date, due_date, return_date, status, fine_amount " +
                     "FROM borrow_records WHERE user_id = ? ORDER BY record_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBorrowRecord(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all currently active (unreturned) borrow records for a specific user.
     * @param userId user ID
     * @return list of active borrow records
     */
    public List<BorrowRecord> findActiveByUserId(int userId) throws SQLException {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT record_id, user_id, equipment_id, borrow_date, due_date, return_date, status, fine_amount " +
                     "FROM borrow_records WHERE user_id = ? AND status = 'BORROWED' ORDER BY record_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBorrowRecord(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all borrow records across the entire laboratory.
     * @return list of all borrow records
     */
    public List<BorrowRecord> findAll() throws SQLException {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT record_id, user_id, equipment_id, borrow_date, due_date, return_date, status, fine_amount " +
                     "FROM borrow_records ORDER BY record_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToBorrowRecord(rs));
            }
        }
        return list;
    }

    /**
     * Helper method to map a ResultSet row to a BorrowRecord object.
     */
    private BorrowRecord mapRowToBorrowRecord(ResultSet rs) throws SQLException {
        int recordId = rs.getInt("record_id");
        int userId = rs.getInt("user_id");
        int equipmentId = rs.getInt("equipment_id");
        Date bDate = rs.getDate("borrow_date");
        Date dDate = rs.getDate("due_date");
        Date rDate = rs.getDate("return_date");
        String status = rs.getString("status");
        BigDecimal fineAmount = rs.getBigDecimal("fine_amount");
        if (fineAmount == null) {
            fineAmount = BigDecimal.ZERO;
        }

        LocalDate borrowDate = (bDate != null) ? bDate.toLocalDate() : null;
        LocalDate dueDate = (dDate != null) ? dDate.toLocalDate() : null;
        LocalDate returnDate = (rDate != null) ? rDate.toLocalDate() : null;

        return new BorrowRecord(recordId, userId, equipmentId, borrowDate, dueDate, returnDate, status, fineAmount);
    }
}
