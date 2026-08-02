package org.shashanka.exception;

public class FraudDetectedException extends RuntimeException{
    public FraudDetectedException(final String message) {
        super(message);
    }
}
