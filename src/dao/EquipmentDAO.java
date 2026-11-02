package dao;

import database.DatabaseConnection;
import model.Equipment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Equipment operations.
 * Uses PreparedStatement for all queries and supports transactions.
 */
public class EquipmentDAO {

    /**
     * Inserts a new equipment item into the database.
     * @param equipment the equipment item to insert
     * @return generated equipment_id
     */
    public int insert(Equipment equipment) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return insert(conn, equipment);
        }
    }

    /**
     * Inserts a new equipment item using an active connection (for transactions).
     * @param conn active database connection
     * @param equipment the equipment item to insert
     * @return generated equipment_id
     */
    public int insert(Connection conn, Equipment equipment) throws SQLException {
        String sql = "INSERT INTO equipment (name, category, total_quantity, available_quantity, condition_status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, equipment.getName());
            pstmt.setString(2, equipment.getCategory());
            pstmt.setInt(3, equipment.getTotalQuantity());
            pstmt.setInt(4, equipment.getAvailableQuantity());
            pstmt.setString(5, equipment.getConditionStatus());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    equipment.setEquipmentId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1;
    }

    /**
     * Finds equipment by its primary key ID.
     * @param equipmentId ID of the equipment
     * @return Equipment object or null if not found
     */
    public Equipment findById(int equipmentId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return findById(conn, equipmentId);
        }
    }

    /**
     * Finds equipment by ID using an existing connection (supports active transactions).
     * @param conn active database connection
     * @param equipmentId ID of the equipment
     * @return Equipment object or null if not found
     */
    public Equipment findById(Connection conn, int equipmentId) throws SQLException {
        String sql = "SELECT equipment_id, name, category, total_quantity, available_quantity, condition_status " +
                     "FROM equipment WHERE equipment_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, equipmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEquipment(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all equipment in the laboratory.
     * @return list of all equipment
     */
    public List<Equipment> findAll() throws SQLException {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT equipment_id, name, category, total_quantity, available_quantity, condition_status " +
                     "FROM equipment ORDER BY equipment_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToEquipment(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves all equipment currently available for borrowing (available_quantity > 0).
     * @return list of available equipment
     */
    public List<Equipment> findAvailable() throws SQLException {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT equipment_id, name, category, total_quantity, available_quantity, condition_status " +
                     "FROM equipment WHERE available_quantity > 0 ORDER BY equipment_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToEquipment(rs));
            }
        }
        return list;
    }

    /**
     * Decrements the available quantity of an equipment item by 1 atomically.
     * Only succeeds if available_quantity > 0.
     * @param conn active database transaction connection
     * @param equipmentId ID of the equipment
     * @return true if updated (row count > 0), false otherwise
     */
    public boolean decrementAvailableQuantity(Connection conn, int equipmentId) throws SQLException {
        String sql = "UPDATE equipment SET available_quantity = available_quantity - 1 " +
                     "WHERE equipment_id = ? AND available_quantity > 0";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, equipmentId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Increments the available quantity of an equipment item by 1 atomically.
     * Only succeeds if available_quantity < total_quantity.
     * @param conn active database transaction connection
     * @param equipmentId ID of the equipment
     * @return true if updated (row count > 0), false otherwise
     */
    public boolean incrementAvailableQuantity(Connection conn, int equipmentId) throws SQLException {
        String sql = "UPDATE equipment SET available_quantity = available_quantity + 1 " +
                     "WHERE equipment_id = ? AND available_quantity < total_quantity";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, equipmentId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Updates general details of an existing equipment item.
     * @param equipment equipment with updated fields
     * @return true if updated, false otherwise
     */
    public boolean update(Equipment equipment) throws SQLException {
        String sql = "UPDATE equipment SET name = ?, category = ?, total_quantity = ?, " +
                     "available_quantity = ?, condition_status = ? WHERE equipment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, equipment.getName());
            pstmt.setString(2, equipment.getCategory());
            pstmt.setInt(3, equipment.getTotalQuantity());
            pstmt.setInt(4, equipment.getAvailableQuantity());
            pstmt.setString(5, equipment.getConditionStatus());
            pstmt.setInt(6, equipment.getEquipmentId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Helper method to map a ResultSet row to an Equipment object.
     */
    private Equipment mapRowToEquipment(ResultSet rs) throws SQLException {
        int equipmentId = rs.getInt("equipment_id");
        String name = rs.getString("name");
        String category = rs.getString("category");
        int totalQuantity = rs.getInt("total_quantity");
        int availableQuantity = rs.getInt("available_quantity");
        String conditionStatus = rs.getString("condition_status");

        return new Equipment(equipmentId, name, category, totalQuantity, availableQuantity, conditionStatus);
    }
}
