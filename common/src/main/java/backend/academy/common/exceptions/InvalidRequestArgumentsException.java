package backend.academy.common.exceptions;

public class InvalidRequestArgumentsException extends RuntimeException {
    public InvalidRequestArgumentsException(String message) {
        super(message);
    }
}
