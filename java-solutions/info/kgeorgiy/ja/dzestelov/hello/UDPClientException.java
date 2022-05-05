package info.kgeorgiy.ja.dzestelov.hello;

/**
 * Present UDP client exceptions
 */
public class UDPClientException extends RuntimeException {
    /**
     * Constructs exception with message.
     *
     * @param message message of exception
     */
    public UDPClientException(String message) {
        super(message);
    }

    /**
     * Constructs exception with message and cause throwable.
     *
     * @param message message of exception
     * @param cause   throwable then cause this exception
     */
    public UDPClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
