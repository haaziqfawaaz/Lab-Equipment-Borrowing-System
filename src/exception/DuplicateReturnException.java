package exception;

/** Indicates that a borrowing record has already been returned. */
public class DuplicateReturnException extends Exception {
    private static final long serialVersionUID = 1L;
    public DuplicateReturnException(String message) {
        super(message);
    }
}
