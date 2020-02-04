package com.limethecoder.accounting.exception;

public class MissingPropertiesException extends RuntimeException {
    public MissingPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
