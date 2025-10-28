package com.platform.saas.exception;

/**
 * Exception thrown when attempting to register a subdomain that already exists.
 */
public class SubdomainAlreadyExistsException extends RuntimeException {

    public SubdomainAlreadyExistsException(String subdomain) {
        super("Subdomain '" + subdomain + "' is already registered");
    }
}
