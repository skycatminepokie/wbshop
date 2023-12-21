package com.skycat.wbshop;

/**
 * {@link IllegalStateException} but doesn't implement {@link RuntimeException}.
 */
public class BadStateException extends Exception {
    public BadStateException() {
        super();
    }

    public BadStateException(String message) {
        super(message);
    }

    public BadStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadStateException(Throwable cause) {
        super(cause);
    }
}
