
package service;

import dao.UserDAO;
import exception.DuplicateEmailException;
import exception.InvalidUserDetailsException;
import model.User;
import security.PasswordUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for User operations.
 * Handles registration, login, logout and authorization.
 */
public class UserService {

    private final UserDAO userDAO;

    // Stores the currently authenticated user for this application session.
    private User currentUser;

    public UserService() {
        this.userDAO = new UserDAO();
        this.currentUser = null;
    }

    // Constructor injection for testing
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.currentUser = null;
    }

    /**
     * Validates common registration details.
     */
    private void validateUserDetails(String name, String email,
                                     String password)
            throws InvalidUserDetailsException {

        if (name == null || name.trim().isEmpty()) {
            throw new InvalidUserDetailsException("Name cannot be empty.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new InvalidUserDetailsException("Email cannot be empty.");
        }

        String cleanEmail = email.trim();

        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            throw new InvalidUserDetailsException("Invalid email format.");
        }

        if (password == null || password.length() < 8) {
            throw new InvalidUserDetailsException(
                    "Password must contain at least 8 characters.");
        }
    }

    /**
     * Registers a new student with a hashed password.
     */
    public User registerStudent(String name, String email,
                                String contact, String password)
            throws SQLException, InvalidUserDetailsException, DuplicateEmailException {

        validateUserDetails(name, email, password);

        String cleanEmail = email.trim();

        if (userDAO.findByEmail(cleanEmail) != null) {
            throw new DuplicateEmailException(
                    "This email is already registered.");
        }

        String passwordHash = PasswordUtil.hashPassword(password);

        String cleanContact =
                (contact != null && !contact.trim().isEmpty())
                        ? contact.trim() : null;

        User student = new User(
                name.trim(),
                cleanEmail,
                "STUDENT",
                cleanContact,
                passwordHash
        );

        userDAO.insert(student);

        return student;
    }

    /**
     * Authenticates a user using email and password.
     * Starts a session after successful authentication.
     */
    public User login(String email, String password)
            throws SQLException {

        // Clear any previous session before attempting a new login.
        currentUser = null;

        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                    "Email and password are required.");
        }

        User user =
                userDAO.findByEmailWithPassword(email.trim());

        if (user == null || user.getPasswordHash() == null) {
            return null;
        }

        boolean valid = PasswordUtil.verifyPassword(
                password,
                user.getPasswordHash()
        );

        if (!valid) {
            return null;
        }

        // Remove password hash before storing the session user.
        user.setPasswordHash(null);
        currentUser = user;

        return currentUser;
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Returns the currently logged-in user.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks whether a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Checks whether the current session belongs to an admin.
     * Rechecks the role from the database.
     */
    public boolean isAdminLoggedIn() throws SQLException {

        if (currentUser == null) {
            return false;
        }

        User verifiedUser =
                userDAO.findById(currentUser.getUserId());

        return verifiedUser != null
                && "ADMIN".equalsIgnoreCase(verifiedUser.getRole());
    }

    /**
     * Registers another administrator.
     * Only an existing authenticated admin can do this.
     */
    public User registerAdmin(String name, String email,
                              String contact, String password)
            throws SQLException, InvalidUserDetailsException, DuplicateEmailException {

        if (currentUser == null) {
            throw new SecurityException(
                    "Please log in as an administrator first.");
        }

        // Verify the current session against the database.
        User verifiedAdmin =
                userDAO.findById(currentUser.getUserId());

        if (verifiedAdmin == null
                || !"ADMIN".equalsIgnoreCase(verifiedAdmin.getRole())) {
            logout();

            throw new SecurityException(
                    "Unauthorized: Admin privileges required.");
        }

        validateUserDetails(name, email, password);

        String cleanEmail = email.trim();

        if (userDAO.findByEmail(cleanEmail) != null) {
            throw new DuplicateEmailException(
                    "This email is already registered.");
        }

        String passwordHash = PasswordUtil.hashPassword(password);

        String cleanContact =
                (contact != null && !contact.trim().isEmpty())
                        ? contact.trim() : null;

        User admin = new User(
                name.trim(),
                cleanEmail,
                "ADMIN",
                cleanContact,
                passwordHash
        );

        userDAO.insert(admin);

        return admin;
    }

    /**
     * Retrieves a user by ID.
     */
    public User getUserById(int userId) throws SQLException {
        return userDAO.findById(userId);
    }

    /**
     * Retrieves a user by email.
     */
    public User getUserByEmail(String email) throws SQLException {
        if (email == null) {
            return null;
        }

        return userDAO.findByEmail(email.trim());
    }

    /**
     * Checks whether a specified user is an admin.
     */
    public boolean isAdmin(int userId) throws SQLException {
        User user = userDAO.findById(userId);

        return user != null
                && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    /**
     * Verifies admin privileges for a specified user ID.
     */
    public void verifyAdmin(int userId)
            throws SQLException, SecurityException {

        User user = userDAO.findById(userId);

        if (user == null) {
            throw new SecurityException(
                    "User with ID " + userId + " does not exist.");
        }

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException(
                    "User does not have ADMIN privileges.");
        }
    }

    /**
     * Retrieves all students.
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
     * Retrieves all users.
     */
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }
}
