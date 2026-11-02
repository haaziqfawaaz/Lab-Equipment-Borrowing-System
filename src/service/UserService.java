package service;

import dao.UserDAO;
import model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for User operations (Students and Admins).
 * Contains validation and business rules without any UI coupling.
 */
public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    // Constructor injection for flexibility or testing
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Registers a new student after validating input fields and checking for duplicate email.
     * @param name student name
     * @param email unique email
     * @param contact contact number
     * @return created User object with generated ID
     */
    public User registerStudent(String name, String email, String contact) throws SQLException, IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Student email cannot be empty.");
        }
        String cleanEmail = email.trim();
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            throw new IllegalArgumentException("Invalid email format: " + cleanEmail);
        }

        // Prevent duplicate registration
        User existing = userDAO.findByEmail(cleanEmail);
        if (existing != null) {
            throw new IllegalArgumentException("A user with email '" + cleanEmail + "' is already registered.");
        }

        String cleanContact = (contact != null) ? contact.trim() : null;
        User student = new User(name.trim(), cleanEmail, "STUDENT", cleanContact);
        userDAO.insert(student);
        return student;
    }

    /**
     * Retrieves a user by their ID.
     */
    public User getUserById(int userId) throws SQLException {
        return userDAO.findById(userId);
    }

    /**
     * Retrieves a user by their email.
     */
    public User getUserByEmail(String email) throws SQLException {
        if (email == null) return null;
        return userDAO.findByEmail(email.trim());
    }

    /**
     * Checks if a user exists and possesses the ADMIN role.
     */
    public boolean isAdmin(int userId) throws SQLException {
        User user = userDAO.findById(userId);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    /**
     * Verifies that the specified user exists and possesses the ADMIN role.
     * @throws SecurityException if user is not found or is not an admin
     */
    public void verifyAdmin(int userId) throws SQLException, SecurityException {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new SecurityException("Authorization failed: User with ID " + userId + " does not exist.");
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Unauthorized access: User with ID " + userId + " (" + user.getName() + ") does not have ADMIN privileges.");
        }
    }

    /**
     * Retrieves all registered students.
     */
    public List<User> getAllStudents() throws SQLException {
        return userDAO.findByRole("STUDENT");
    }

    /**
     * Retrieves all administrators.
     */
    public List<User> getAllAdmins() throws SQLException {
        return userDAO.findByRole("ADMIN");
    }

    /**
     * Retrieves all users in the system.
     */
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }
}
