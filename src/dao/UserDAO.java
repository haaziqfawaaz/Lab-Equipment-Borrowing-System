
package dao;

import database.DatabaseConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User operations.
 * Handles Students and Admins.
 */
public class UserDAO {

    /**
     * Inserts a new user into the database.
     */
    public int insert(User user) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return insert(conn, user);
        }
    }

    /**
     * Inserts a new user using an existing connection.
     * Supports transaction management.
     */
    public int insert(Connection conn, User user) throws SQLException {

        String sql = "INSERT INTO users "
                + "(name, email, role, contact, password_hash) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getContact());
            pstmt.setString(5, user.getPasswordHash());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    user.setUserId(generatedId);
                    return generatedId;
                }
            }
        }

        return -1;
    }

    /**
     * Finds a user by their user ID.
     * Does not retrieve the password hash.
     */
    public User findById(int userId) throws SQLException {

        String sql = "SELECT user_id, name, email, role, contact, created_at "
                + "FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }

        return null;
    }

    /**
     * Finds a user by email.
     * Does not retrieve the password hash.
     */
    public User findByEmail(String email) throws SQLException {

        String sql = "SELECT user_id, name, email, role, contact, created_at "
                + "FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }

        return null;
    }

    /**
     * Finds a user by email and retrieves the password hash.
     * Used internally for password verification.
     */
    public User findByEmailWithPassword(String email) throws SQLException {

        String sql = "SELECT user_id, name, email, role, contact, "
                + "password_hash, created_at "
                + "FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapRowToUser(rs);
                    user.setPasswordHash(rs.getString("password_hash"));
                    return user;
                }
            }
        }

        return null;
    }

    /**
     * Retrieves all users.
     */
    public List<User> findAll() throws SQLException {

        List<User> users = new ArrayList<>();

        String sql = "SELECT user_id, name, email, role, contact, created_at "
                + "FROM users ORDER BY user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        }

        return users;
    }

    /**
     * Retrieves users matching a specific role.
     */
    public List<User> findByRole(String role) throws SQLException {

        List<User> users = new ArrayList<>();

        String sql = "SELECT user_id, name, email, role, contact, created_at "
                + "FROM users WHERE role = ? ORDER BY user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRowToUser(rs));
                }
            }
        }

        return users;
    }

    /**
     * Maps a ResultSet row to a User object.
     * Password hash is deliberately excluded.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {

        int userId = rs.getInt("user_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String role = rs.getString("role");
        String contact = rs.getString("contact");

        Timestamp ts = rs.getTimestamp("created_at");

        LocalDateTime createdAt =
                (ts != null) ? ts.toLocalDateTime() : null;

        return new User(
                userId,
                name,
                email,
                role,
                contact,
                createdAt
        );
    }
}
