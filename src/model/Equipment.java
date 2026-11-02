package model;

/**
 * Model class representing Lab Equipment.
 */
public class Equipment {
    private int equipmentId;
    private String name;
    private String category;
    private int totalQuantity;
    private int availableQuantity;
    private String conditionStatus;

    // Default constructor
    public Equipment() {
    }

    // Constructor for creating new equipment (before ID is assigned by the database)
    public Equipment(String name, String category, int totalQuantity, int availableQuantity, String conditionStatus) {
        this.name = name;
        this.category = category;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.conditionStatus = conditionStatus;
    }

    // Full constructor (for equipment fetched from the database)
    public Equipment(int equipmentId, String name, String category, int totalQuantity, int availableQuantity, String conditionStatus) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.category = category;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.conditionStatus = conditionStatus;
    }

    // Helper method to check if at least one unit is available
    public boolean isAvailable() {
        return this.availableQuantity > 0;
    }

    // Getters and Setters
    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(String conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    @Override
    public String toString() {
        return "Equipment{" +
                "equipmentId=" + equipmentId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", totalQuantity=" + totalQuantity +
                ", availableQuantity=" + availableQuantity +
                ", conditionStatus='" + conditionStatus + '\'' +
                '}';
    }
}
