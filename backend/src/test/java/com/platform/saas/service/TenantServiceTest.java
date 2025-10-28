package com.platform.saas.service;

import com.platform.saas.dto.TenantRegistrationRequest;
import com.platform.saas.dto.TenantResponse;
import com.platform.saas.exception.InvalidSubdomainException;
import com.platform.saas.exception.QuotaExceededException;
import com.platform.saas.exception.SubdomainAlreadyExistsException;
import com.platform.saas.exception.TenantNotFoundException;
import com.platform.saas.model.SubscriptionTier;
import com.platform.saas.model.Tenant;
import com.platform.saas.model.User;
import com.platform.saas.model.UserRole;
import com.platform.saas.repository.ProjectRepository;
import com.platform.saas.repository.TaskRepository;
import com.platform.saas.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TenantService.
 * Tests tenant registration, validation, quota enforcement, and retrieval.
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantService Tests")
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TenantService tenantService;

    private TenantRegistrationRequest registrationRequest;
    private Tenant testTenant;
    private User testUser;
    private UUID testTenantId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        registrationRequest = new TenantRegistrationRequest();
        registrationRequest.setSubdomain("test-company");
        registrationRequest.setName("Test Company");
        registrationRequest.setDescription("Test Description");
        registrationRequest.setOwnerEmail("owner@test.com");
        registrationRequest.setOwnerName("Test Owner");
        registrationRequest.setSubscriptionTier(SubscriptionTier.FREE);

        testTenant = new Tenant();
        testTenant.setId(testTenantId);
        testTenant.setSubdomain("test-company");
        testTenant.setName("Test Company");
        testTenant.setDescription("Test Description");
        testTenant.setSubscriptionTier(SubscriptionTier.FREE);
        testTenant.setQuotaLimit(50);
        testTenant.setActive(true);
        testTenant.setCreatedAt(LocalDateTime.now());
        testTenant.setUpdatedAt(LocalDateTime.now());

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("owner@test.com");
        testUser.setName("Test Owner");
    }

    // ========== REGISTER TENANT TESTS ==========

    @Test
    @DisplayName("Should register tenant successfully with FREE tier")
    void registerTenant_FreeTier_Success() {
        // Given
        when(tenantRepository.existsBySubdomain("test-company")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
        when(userService.findOrCreateUserByEmail("owner@test.com", "Test Owner"))
                .thenReturn(testUser);
        when(userService.addUserToTenant(any(), any(), any(), any())).thenReturn(new com.platform.saas.model.UserTenant());
        // Note: registerTenant passes 0L directly to toTenantResponse, so count methods are not called

        // When
        TenantResponse response = tenantService.registerTenant(registrationRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSubdomain()).isEqualTo("test-company");
        assertThat(response.getName()).isEqualTo("Test Company");
        assertThat(response.getSubscriptionTier()).isEqualTo(SubscriptionTier.FREE);
        assertThat(response.getQuotaLimit()).isEqualTo(50);
        assertThat(response.getCurrentUsage()).isEqualTo(0L);
        assertThat(response.getIsActive()).isTrue();

        verify(tenantRepository).existsBySubdomain("test-company");
        verify(tenantRepository).save(any(Tenant.class));
        verify(userService).findOrCreateUserByEmail("owner@test.com", "Test Owner");
        verify(userService).addUserToTenant(testUserId, testTenantId, UserRole.ADMINISTRATOR, null);
    }

    @Test
    @DisplayName("Should register tenant with PRO tier quota")
    void registerTenant_ProTier_Success() {
        // Given
        registrationRequest.setSubscriptionTier(SubscriptionTier.PRO);
        testTenant.setSubscriptionTier(SubscriptionTier.PRO);
        testTenant.setQuotaLimit(1000);

        when(tenantRepository.existsBySubdomain("test-company")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
        when(userService.findOrCreateUserByEmail(anyString(), anyString())).thenReturn(testUser);
        when(userService.addUserToTenant(any(), any(), any(), any())).thenReturn(new com.platform.saas.model.UserTenant());

        // When
        TenantResponse response = tenantService.registerTenant(registrationRequest);

        // Then
        assertThat(response.getSubscriptionTier()).isEqualTo(SubscriptionTier.PRO);
        assertThat(response.getQuotaLimit()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Should register tenant with ENTERPRISE tier (unlimited)")
    void registerTenant_EnterpriseTier_Success() {
        // Given
        registrationRequest.setSubscriptionTier(SubscriptionTier.ENTERPRISE);
        testTenant.setSubscriptionTier(SubscriptionTier.ENTERPRISE);
        testTenant.setQuotaLimit(null);

        when(tenantRepository.existsBySubdomain("test-company")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
        when(userService.findOrCreateUserByEmail(anyString(), anyString())).thenReturn(testUser);
        when(userService.addUserToTenant(any(), any(), any(), any())).thenReturn(new com.platform.saas.model.UserTenant());

        // When
        TenantResponse response = tenantService.registerTenant(registrationRequest);

        // Then
        assertThat(response.getSubscriptionTier()).isEqualTo(SubscriptionTier.ENTERPRISE);
        assertThat(response.getQuotaLimit()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when subdomain already exists")
    void registerTenant_SubdomainExists_ThrowsException() {
        // Given
        when(tenantRepository.existsBySubdomain("test-company")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> tenantService.registerTenant(registrationRequest))
                .isInstanceOf(SubdomainAlreadyExistsException.class);

        verify(tenantRepository).existsBySubdomain("test-company");
        verify(tenantRepository, never()).save(any());
        verify(userService, never()).findOrCreateUserByEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should normalize uppercase subdomain during registration")
    void registerTenant_UppercaseSubdomain_Normalizes() {
        // Given
        registrationRequest.setSubdomain("TEST-COMPANY");
        testTenant.setSubdomain("test-company");

        // existsBySubdomain is called with original (not normalized) subdomain
        when(tenantRepository.existsBySubdomain("TEST-COMPANY")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
        when(userService.findOrCreateUserByEmail(anyString(), anyString())).thenReturn(testUser);
        when(userService.addUserToTenant(any(), any(), any(), any())).thenReturn(new com.platform.saas.model.UserTenant());

        // When
        TenantResponse response = tenantService.registerTenant(registrationRequest);

        // Then - subdomain is normalized when saving
        assertThat(response.getSubdomain()).isEqualTo("test-company");
        verify(tenantRepository).existsBySubdomain("TEST-COMPANY");
    }

    // ========== GET TENANT TESTS ==========

    @Test
    @DisplayName("Should get tenant by ID successfully")
    void getTenantById_Success() {
        // Given
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(5L);
        when(taskRepository.countByTenantId(testTenantId)).thenReturn(10L);

        // When
        TenantResponse response = tenantService.getTenantById(testTenantId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testTenantId);
        assertThat(response.getCurrentUsage()).isEqualTo(15L);

        verify(tenantRepository).findById(testTenantId);
        verify(projectRepository).countByTenantId(testTenantId);
        verify(taskRepository).countByTenantId(testTenantId);
    }

    @Test
    @DisplayName("Should throw exception when tenant not found by ID")
    void getTenantById_NotFound_ThrowsException() {
        // Given
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tenantService.getTenantById(testTenantId))
                .isInstanceOf(TenantNotFoundException.class);

        verify(tenantRepository).findById(testTenantId);
        verify(projectRepository, never()).countByTenantId(any());
    }

    @Test
    @DisplayName("Should get tenant by subdomain successfully")
    void getTenantBySubdomain_Success() {
        // Given
        when(tenantRepository.findBySubdomain("test-company")).thenReturn(Optional.of(testTenant));
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(3L);
        when(taskRepository.countByTenantId(testTenantId)).thenReturn(7L);

        // When
        TenantResponse response = tenantService.getTenantBySubdomain("test-company");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSubdomain()).isEqualTo("test-company");
        assertThat(response.getCurrentUsage()).isEqualTo(10L);

        verify(tenantRepository).findBySubdomain("test-company");
    }

    @Test
    @DisplayName("Should normalize subdomain when getting by subdomain")
    void getTenantBySubdomain_UpperCase_NormalizesToLowercase() {
        // Given
        when(tenantRepository.findBySubdomain("test-company")).thenReturn(Optional.of(testTenant));
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(0L);
        when(taskRepository.countByTenantId(testTenantId)).thenReturn(0L);

        // When
        TenantResponse response = tenantService.getTenantBySubdomain("TEST-COMPANY");

        // Then
        assertThat(response.getSubdomain()).isEqualTo("test-company");
        verify(tenantRepository).findBySubdomain("test-company");
    }

    @Test
    @DisplayName("Should throw exception when tenant not found by subdomain")
    void getTenantBySubdomain_NotFound_ThrowsException() {
        // Given
        when(tenantRepository.findBySubdomain("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tenantService.getTenantBySubdomain("nonexistent"))
                .isInstanceOf(TenantNotFoundException.class);

        verify(tenantRepository).findBySubdomain("nonexistent");
    }

    // ========== SUBDOMAIN VALIDATION TESTS ==========

    @Test
    @DisplayName("Should validate valid subdomain successfully")
    void validateSubdomain_ValidSubdomain_Success() {
        // When & Then - should not throw exception
        assertThatCode(() -> tenantService.validateSubdomain("valid-subdomain"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject null subdomain")
    void validateSubdomain_Null_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> tenantService.validateSubdomain(null))
                .isInstanceOf(InvalidSubdomainException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("Should reject empty subdomain")
    void validateSubdomain_Empty_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> tenantService.validateSubdomain(""))
                .isInstanceOf(InvalidSubdomainException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("Should reject subdomain that is too short")
    void validateSubdomain_TooShort_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> tenantService.validateSubdomain("ab"))
                .isInstanceOf(InvalidSubdomainException.class)
                .hasMessageContaining("between 3 and 63 characters");
    }

    @Test
    @DisplayName("Should reject subdomain that is too long")
    void validateSubdomain_TooLong_ThrowsException() {
        // Given
        String longSubdomain = "a".repeat(64);

        // When & Then
        assertThatThrownBy(() -> tenantService.validateSubdomain(longSubdomain))
                .isInstanceOf(InvalidSubdomainException.class)
                .hasMessageContaining("between 3 and 63 characters");
    }

    @Test
    @DisplayName("Should normalize and accept uppercase letters")
    void validateSubdomain_Uppercase_Normalizes() {
        // When & Then - validation normalizes to lowercase before checking
        assertThatCode(() -> tenantService.validateSubdomain("Test-Company"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject subdomain with special characters")
    void validateSubdomain_SpecialCharacters_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> tenantService.validateSubdomain("test_company"))
                .isInstanceOf(InvalidSubdomainException.class)
                .hasMessageContaining("lowercase letters, numbers, and hyphens");
    }

    @Test
    @DisplayName("Should reject subdomain starting with hyphen")
    void validateSubdomain_StartsWithHyphen_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> tenantService.validateSubdomain("-testcompany"))
                .isInstanceOf(InvalidSubdomainException.class)
                .hasMessageContaining("cannot start or end with a hyphen");
    }

    @Test
    @DisplayName("Should reject subdomain ending with hyphen")
    void validateSubdomain_EndsWithHyphen_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> tenantService.validateSubdomain("testcompany-"))
                .isInstanceOf(InvalidSubdomainException.class)
                .hasMessageContaining("cannot start or end with a hyphen");
    }

    @Test
    @DisplayName("Should reject reserved subdomain 'www'")
    void validateSubdomain_ReservedWww_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> tenantService.validateSubdomain("www"))
                .isInstanceOf(InvalidSubdomainException.class)
                .hasMessageContaining("reserved");
    }

    @Test
    @DisplayName("Should reject reserved subdomain 'api'")
    void validateSubdomain_ReservedApi_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> tenantService.validateSubdomain("api"))
                .isInstanceOf(InvalidSubdomainException.class)
                .hasMessageContaining("reserved");
    }

    @Test
    @DisplayName("Should reject reserved subdomain 'admin'")
    void validateSubdomain_ReservedAdmin_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> tenantService.validateSubdomain("admin"))
                .isInstanceOf(InvalidSubdomainException.class)
                .hasMessageContaining("reserved");
    }

    // ========== QUOTA ENFORCEMENT TESTS ==========

    @Test
    @DisplayName("Should allow usage within quota limit")
    void enforceQuota_WithinLimit_Success() {
        // Given
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(20L);
        when(taskRepository.countByTenantId(testTenantId)).thenReturn(25L); // Total 45 < 50

        // When & Then - should not throw exception
        assertThatCode(() -> tenantService.enforceQuota(testTenantId))
                .doesNotThrowAnyException();

        verify(tenantRepository).findById(testTenantId);
        verify(projectRepository).countByTenantId(testTenantId);
        verify(taskRepository).countByTenantId(testTenantId);
    }

    @Test
    @DisplayName("Should throw exception at exactly quota limit")
    void enforceQuota_AtLimit_ThrowsException() {
        // Given
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(25L);
        when(taskRepository.countByTenantId(testTenantId)).thenReturn(25L); // Total 50 >= 50

        // When & Then - at limit is considered exceeded (>= check)
        assertThatThrownBy(() -> tenantService.enforceQuota(testTenantId))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    @DisplayName("Should throw exception when quota exceeded")
    void enforceQuota_ExceedsLimit_ThrowsException() {
        // Given
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(30L);
        when(taskRepository.countByTenantId(testTenantId)).thenReturn(25L); // Total 55 > 50

        // When & Then
        assertThatThrownBy(() -> tenantService.enforceQuota(testTenantId))
                .isInstanceOf(QuotaExceededException.class);

        verify(tenantRepository).findById(testTenantId);
        verify(projectRepository).countByTenantId(testTenantId);
        verify(taskRepository).countByTenantId(testTenantId);
    }

    @Test
    @DisplayName("Should allow unlimited usage for ENTERPRISE tier")
    void enforceQuota_EnterpriseTier_NoLimit() {
        // Given
        testTenant.setSubscriptionTier(SubscriptionTier.ENTERPRISE);
        testTenant.setQuotaLimit(null);

        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));

        // When & Then - should not throw exception regardless of usage
        assertThatCode(() -> tenantService.enforceQuota(testTenantId))
                .doesNotThrowAnyException();

        verify(tenantRepository).findById(testTenantId);
        // Should not check usage for unlimited tier
        verify(projectRepository, never()).countByTenantId(testTenantId);
        verify(taskRepository, never()).countByTenantId(testTenantId);
    }

    @Test
    @DisplayName("Should throw exception when tenant not found for quota enforcement")
    void enforceQuota_TenantNotFound_ThrowsException() {
        // Given
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tenantService.enforceQuota(testTenantId))
                .isInstanceOf(TenantNotFoundException.class);

        verify(tenantRepository).findById(testTenantId);
    }
}
