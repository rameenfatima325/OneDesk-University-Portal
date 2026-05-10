package business.shared.exceptions;

public class DeadlineExceededException extends Exception {
    public DeadlineExceededException(String message) {
        super(message);
    }
}