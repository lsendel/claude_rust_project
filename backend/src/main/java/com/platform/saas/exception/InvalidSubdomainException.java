package com.platform.saas.exception;

/**
 * Exception thrown when a subdomain fails validation rules.
 */
public class InvalidSubdomainException extends RuntimeException {

    public InvalidSubdomainException(String message) {
        super(message);
    }

    public InvalidSubdomainException(String subdomain, String reason) {
        super("Subdomain '" + subdomain + "' is invalid: " + reason);
    }
}
