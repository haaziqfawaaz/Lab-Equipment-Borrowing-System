package exception;

/** Indicates that the requested equipment has no available stock. */
public class EquipmentUnavailableException extends Exception {
    private static final long serialVersionUID = 1L;
    public EquipmentUnavailableException(String message) {
        super(message);
    }
}
