package com.platform.saas.repository;

import com.platform.saas.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository.
 *
 * Sprint 3 - Repository Layer Testing
 * Uses @DataJpaTest for in-memory H2 database testing
 * Excludes Flyway and uses JPA schema generation
 *
 * Test Categories:
 * 1. findByCognitoUserId (3 tests)
 * 2. findByEmail (3 tests)
 * 3. existsByEmail (2 tests)
 * 4. existsByCognitoUserId (2 tests)
 * 5. CRUD Operations (3 tests)
 */
@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setCognitoUserId("cognito-test-123");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setCreatedAt(java.time.LocalDateTime.now());
    }

    // ==================== findByCognitoUserId Tests ====================

    @Test
    @DisplayName("Should find user by Cognito user ID when exists")
    void findByCognitoUserId_ExistingUser_Found() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByCognitoUserId("cognito-test-123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCognitoUserId()).isEqualTo("cognito-test-123");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by Cognito user ID")
    void findByCognitoUserId_NonExistingUser_Empty() {
        // When
        Optional<User> found = userRepository.findByCognitoUserId("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find correct user among multiple users by Cognito ID")
    void findByCognitoUserId_MultipleUsers_FindsCorrect() {
        // Given
        User user1 = new User();
        user1.setCognitoUserId("cognito-1");
        user1.setEmail("user1@example.com");
        user1.setName("User 1");
        user1.setCreatedAt(java.time.LocalDateTime.now());

        User user2 = new User();
        user2.setCognitoUserId("cognito-2");
        user2.setEmail("user2@example.com");
        user2.setName("User 2");
        user2.setCreatedAt(java.time.LocalDateTime.now());

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByCognitoUserId("cognito-2");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCognitoUserId()).isEqualTo("cognito-2");
        assertThat(found.get().getEmail()).isEqualTo("user2@example.com");
    }

    // ==================== findByEmail Tests ====================

    @Test
    @DisplayName("Should find user by email when exists")
    void findByEmail_ExistingUser_Found() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getCognitoUserId()).isEqualTo("cognito-test-123");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void findByEmail_NonExistingUser_Empty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find correct user among multiple users by email")
    void findByEmail_MultipleUsers_FindsCorrect() {
        // Given
        User user1 = new User();
        user1.setCognitoUserId("cognito-1");
        user1.setEmail("user1@example.com");
        user1.setName("User 1");
        user1.setCreatedAt(java.time.LocalDateTime.now());

        User user2 = new User();
        user2.setCognitoUserId("cognito-2");
        user2.setEmail("user2@example.com");
        user2.setName("User 2");
        user2.setCreatedAt(java.time.LocalDateTime.now());

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByEmail("user1@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("user1@example.com");
        assertThat(found.get().getCognitoUserId()).isEqualTo("cognito-1");
    }

    // ==================== existsByEmail Tests ====================

    @Test
    @DisplayName("Should return true when email exists")
    void existsByEmail_ExistingEmail_ReturnsTrue() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void existsByEmail_NonExistingEmail_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    // ==================== existsByCognitoUserId Tests ====================

    @Test
    @DisplayName("Should return true when Cognito user ID exists")
    void existsByCognitoUserId_ExistingId_ReturnsTrue() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsByCognitoUserId("cognito-test-123");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when Cognito user ID does not exist")
    void existsByCognitoUserId_NonExistingId_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsByCognitoUserId("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    // ==================== CRUD Operations Tests ====================

    @Test
    @DisplayName("Should save and retrieve user")
    void save_ValidUser_Success() {
        // When
        User saved = userRepository.save(testUser);
        User retrieved = userRepository.findById(saved.getId()).orElse(null);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should delete user")
    void delete_ExistingUser_Success() {
        // Given
        User saved = userRepository.save(testUser);

        // When
        userRepository.delete(saved);
        Optional<User> found = userRepository.findById(saved.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should count all users")
    void count_MultipleUsers_ReturnsCorrectCount() {
        // Given
        User user1 = new User();
        user1.setCognitoUserId("cognito-1");
        user1.setEmail("user1@example.com");
        user1.setName("User 1");
        user1.setCreatedAt(java.time.LocalDateTime.now());

        User user2 = new User();
        user2.setCognitoUserId("cognito-2");
        user2.setEmail("user2@example.com");
        user2.setName("User 2");
        user2.setCreatedAt(java.time.LocalDateTime.now());

        userRepository.save(user1);
        userRepository.save(user2);

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}
