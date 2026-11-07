package exception;

/** Indicates that a user is not allowed to perform a borrowing operation. */
public class UnauthorizedBorrowingException extends Exception {
    private static final long serialVersionUID = 1L;
    public UnauthorizedBorrowingException(String message) {
        super(message);
    }
}
