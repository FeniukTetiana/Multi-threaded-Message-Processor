package ua.shpp.feniuk.exception;

public class MessageSendingException extends RuntimeException {
    public MessageSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}

