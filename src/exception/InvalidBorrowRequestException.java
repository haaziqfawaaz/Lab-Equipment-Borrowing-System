package exception;

/** Represents an invalid borrowing or return request. */
public class InvalidBorrowRequestException extends Exception {
    private static final long serialVersionUID = 1L;
    public InvalidBorrowRequestException(String message) {
        super(message);
    }
}
