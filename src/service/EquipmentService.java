package service;

import dao.EquipmentDAO;
import dao.UserDAO;
import model.Equipment;
import model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for Equipment operations.
 * Manages inventory validation, availability checks, and administrative authorization.
 */
public class EquipmentService {
    private final EquipmentDAO equipmentDAO;
    private final UserDAO userDAO;

    public EquipmentService() {
        this.equipmentDAO = new EquipmentDAO();
        this.userDAO = new UserDAO();
    }

    public EquipmentService(EquipmentDAO equipmentDAO, UserDAO userDAO) {
        this.equipmentDAO = equipmentDAO;
        this.userDAO = userDAO;
    }

    /**
     * Verifies that the specified user possesses ADMIN privileges.
     * @param adminUserId ID of the user to check
     * @throws SecurityException if the user does not exist or is not an ADMIN
     */
    private void verifyAdmin(int adminUserId) throws SQLException, SecurityException {
        User user = userDAO.findById(adminUserId);
        if (user == null) {
            throw new SecurityException("Authorization failed: User with ID " + adminUserId + " does not exist.");
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Unauthorized: User with ID " + adminUserId + " (" + user.getName() + ") is not an administrator.");
        }
    }

    /**
     * Administrative operation: Adds a new laboratory equipment item into the inventory.
     * Enforces administrator authorization.
     * @param adminUserId ID of the administrator performing the action
     * @param name name of equipment (e.g., "Digital Multimeter")
     * @param category category (e.g., "Electronics")
     * @param totalQuantity total number of units purchased
     * @param conditionStatus condition of equipment (e.g., "GOOD", "NEW")
     * @return newly created Equipment object with generated ID
     */
    public Equipment addEquipment(int adminUserId, String name, String category, int totalQuantity, String conditionStatus) 
            throws SQLException, SecurityException, IllegalArgumentException {
        // Enforce admin authorization
        verifyAdmin(adminUserId);

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Equipment name cannot be empty.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Equipment category cannot be empty.");
        }
        if (totalQuantity <= 0) {
            throw new IllegalArgumentException("Total quantity must be greater than zero. Provided: " + totalQuantity);
        }

        String condition = (conditionStatus != null && !conditionStatus.trim().isEmpty()) 
                ? conditionStatus.trim() 
                : "GOOD";

        // When added initially, available quantity equals total quantity
        Equipment equipment = new Equipment(name.trim(), category.trim(), totalQuantity, totalQuantity, condition);
        equipmentDAO.insert(equipment);
        return equipment;
    }


    /**
     * Administrative operation: Updates details of an existing equipment item.
     * Enforces administrator authorization.
     */
    public boolean updateEquipment(int adminUserId, Equipment equipment) 
            throws SQLException, SecurityException, IllegalArgumentException {
        verifyAdmin(adminUserId);
        if (equipment == null) {
            throw new IllegalArgumentException("Equipment cannot be null.");
        }
        return equipmentDAO.update(equipment);
    }

    /**
     * Finds equipment by its primary key ID.
     */
    public Equipment getEquipmentById(int equipmentId) throws SQLException {
        return equipmentDAO.findById(equipmentId);
    }

    /**
     * Retrieves all equipment items in the laboratory.
     */
    public List<Equipment> getAllEquipment() throws SQLException {
        return equipmentDAO.findAll();
    }

    /**
     * Retrieves all equipment items that currently have available stock (available_quantity > 0).
     */
    public List<Equipment> getAvailableEquipment() throws SQLException {
        return equipmentDAO.findAvailable();
    }

    /**
     * Checks if a specific equipment item is available for borrowing.
     * @param equipmentId ID of the equipment
     * @return true if item exists and available_quantity > 0, false otherwise
     */
    public boolean isEquipmentAvailable(int equipmentId) throws SQLException {
        Equipment eq = equipmentDAO.findById(equipmentId);
        return eq != null && eq.isAvailable();
    }

    /**
     * Returns the current number of available units for an equipment item.
     * @param equipmentId ID of the equipment
     * @return available quantity, or 0 if not found
     */
    public int getAvailableQuantity(int equipmentId) throws SQLException {
        Equipment eq = equipmentDAO.findById(equipmentId);
        return (eq != null) ? eq.getAvailableQuantity() : 0;
    }
}
