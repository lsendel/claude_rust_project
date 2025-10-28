package com.platform.saas.exception;

import java.util.UUID;

/**
 * Exception thrown when a user cannot be found.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("User not found with ID: " + userId);
    }

    public UserNotFoundException(String identifier, String identifierType) {
        super("User not found with " + identifierType + ": " + identifier);
    }
}
