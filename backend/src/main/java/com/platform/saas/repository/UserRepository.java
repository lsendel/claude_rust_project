package com.platform.saas.repository;

import com.platform.saas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by their Cognito user ID.
     * @param cognitoUserId The AWS Cognito user identifier
     * @return Optional containing the user if found
     */
    Optional<User> findByCognitoUserId(String cognitoUserId);

    /**
     * Find a user by their email address.
     * @param email The user's email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email already exists.
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user with the given Cognito ID already exists.
     * @param cognitoUserId The Cognito user ID to check
     * @return true if Cognito user ID exists, false otherwise
     */
    boolean existsByCognitoUserId(String cognitoUserId);
}
