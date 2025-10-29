package com.platform.saas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailService.
 *
 * Sprint 4 - Service Layer Testing
 * Target: 291 missed instructions → ~233 covered (80% of class)
 *
 * Test Categories:
 * 1. Constructor - SES enabled mode
 * 2. Constructor - SES disabled mode
 * 3. sendEmail(to, subject, body) - SES enabled, successful send
 * 4. sendEmail(to, subject, body) - SES disabled, local logging only
 * 5. sendEmail(to, subject, htmlBody, textBody) - overloaded method
 * 6. sendEmail() - SES exception handling
 * 7. sendEmail() - Generic exception handling
 * 8. Edge cases - null/empty parameters, special characters
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private SesClient sesClient;

    @Captor
    private ArgumentCaptor<SendEmailRequest> sendEmailRequestCaptor;

    private EmailService emailService;

    private final String fromEmail = "noreply@test.com";
    private final String region = "us-east-1";

    @BeforeEach
    void setUp() {
        // Tests will create service instances with different configurations
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create EmailService with SES disabled")
    void constructor_SesDisabled_ClientNull() {
        // When
        emailService = new EmailService(
                fromEmail,
                region,
                false // SES disabled
        );

        // Then - Service should be created (client will be null internally)
        assertThat(emailService).isNotNull();
    }

    @Test
    @DisplayName("Should create EmailService with SES enabled")
    void constructor_SesEnabled_ClientInitialized() {
        // When
        emailService = new EmailService(
                fromEmail,
                region,
                true // SES enabled
        );

        // Then
        assertThat(emailService).isNotNull();
        // Note: In production, this would initialize the real SES client
        // For integration tests, we'd need to mock the AWS SDK client builder
    }

    // ==================== sendEmail() - SES Disabled ====================

    @Test
    @DisplayName("Should log email locally when SES disabled")
    void sendEmail_SesDisabled_LogsOnly() {
        // Given
        emailService = new EmailService(fromEmail, region, false);

        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // When - Should not throw exception
        emailService.sendEmail(to, subject, body);

        // Then - Method completes without sending actual email
        // Note: We can't verify internal logging without capturing logs
        // But we can verify no exception is thrown
    }

    @Test
    @DisplayName("Should handle null recipient when SES disabled")
    void sendEmail_NullRecipient_SesDisabled_LogsOnly() {
        // Given
        emailService = new EmailService(fromEmail, region, false);

        // When - Should not throw exception
        emailService.sendEmail(null, "Subject", "Body");

        // Then - Method completes without error
    }

    @Test
    @DisplayName("Should handle empty subject when SES disabled")
    void sendEmail_EmptySubject_SesDisabled_LogsOnly() {
        // Given
        emailService = new EmailService(fromEmail, region, false);

        // When - Should not throw exception
        emailService.sendEmail("test@example.com", "", "Body");

        // Then - Method completes without error
    }

    // ==================== sendEmail() - SES Enabled ====================

    @Test
    @DisplayName("Should send email via SES when enabled")
    void sendEmail_SesEnabled_SendsSuccessfully() {
        // Given - Create service with mocked SES client
        emailService = createEmailServiceWithMockedClient(true);

        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test email body content";

        SendEmailResponse response = SendEmailResponse.builder()
                .messageId("message-123")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(response);

        // When
        emailService.sendEmail(to, subject, body);

        // Then - Verify SES client was called
        verify(sesClient, times(1)).sendEmail(sendEmailRequestCaptor.capture());

        SendEmailRequest request = sendEmailRequestCaptor.getValue();
        assertThat(request.source()).isEqualTo(fromEmail);
        assertThat(request.destination().toAddresses()).contains(to);
        assertThat(request.message().subject().data()).isEqualTo(subject);
        assertThat(request.message().body().html().data()).isEqualTo(body); // HTML body, not text
    }

    @Test
    @DisplayName("Should handle SES exception")
    void sendEmail_SesException_ThrowsRuntimeException() {
        // Given
        emailService = createEmailServiceWithMockedClient(true);

        String to = "test@example.com";
        String subject = "Subject";
        String body = "Body";

        // Mock SES exception with proper AWS error details
        SesException sesException = (SesException) SesException.builder()
                .message("SES service unavailable")
                .awsErrorDetails(software.amazon.awssdk.awscore.exception.AwsErrorDetails.builder()
                        .errorCode("ServiceUnavailable")
                        .errorMessage("SES service unavailable")
                        .build())
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow(sesException);

        // When / Then - Should wrap in RuntimeException
        assertThatThrownBy(() -> emailService.sendEmail(to, subject, body))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send email via AWS SES")
                .hasCauseInstanceOf(SesException.class);

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    @DisplayName("Should handle generic runtime exception")
    void sendEmail_RuntimeException_WrapsException() {
        // Given
        emailService = createEmailServiceWithMockedClient(true);

        String to = "test@example.com";
        String subject = "Subject";
        String body = "Body";

        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(new RuntimeException("Network timeout"));

        // When / Then - Should wrap in RuntimeException with "Failed to send email" message
        assertThatThrownBy(() -> emailService.sendEmail(to, subject, body))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send email");

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    // ==================== sendEmail() Overloaded Method (HTML + Text) Tests ====================

    @Test
    @DisplayName("Should send email with both HTML and text when SES disabled")
    void sendEmailWithBothFormats_SesDisabled_LogsOnly() {
        // Given
        emailService = new EmailService(fromEmail, region, false);

        String to = "test@example.com";
        String subject = "Test Email";
        String htmlBody = "<html><body><h1>Hello</h1><p>This is an HTML email</p></body></html>";
        String textBody = "Hello\n\nThis is a plain text email";

        // When - Should not throw exception
        emailService.sendEmail(to, subject, htmlBody, textBody);

        // Then - Method completes without error
    }

    @Test
    @DisplayName("Should send email with both HTML and text via SES when enabled")
    void sendEmailWithBothFormats_SesEnabled_SendsSuccessfully() {
        // Given
        emailService = createEmailServiceWithMockedClient(true);

        String to = "recipient@example.com";
        String subject = "Rich Email";
        String htmlBody = "<html><body><h1>Welcome</h1><p>This is a rich HTML email</p></body></html>";
        String textBody = "Welcome\n\nThis is a plain text version";

        SendEmailResponse response = SendEmailResponse.builder()
                .messageId("rich-message-123")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(response);

        // When
        emailService.sendEmail(to, subject, htmlBody, textBody);

        // Then
        verify(sesClient, times(1)).sendEmail(sendEmailRequestCaptor.capture());

        SendEmailRequest request = sendEmailRequestCaptor.getValue();
        assertThat(request.source()).isEqualTo(fromEmail);
        assertThat(request.destination().toAddresses()).contains(to);
        assertThat(request.message().subject().data()).isEqualTo(subject);
        assertThat(request.message().body().html().data()).isEqualTo(htmlBody);
        assertThat(request.message().body().text().data()).isEqualTo(textBody);
    }

    @Test
    @DisplayName("Should handle SES exception in overloaded method")
    void sendEmailWithBothFormats_SesException_ThrowsException() {
        // Given
        emailService = createEmailServiceWithMockedClient(true);

        String to = "test@example.com";
        String subject = "Subject";
        String htmlBody = "<p>HTML</p>";
        String textBody = "Text";

        // Mock SES exception with proper AWS error details
        SesException sesException = (SesException) SesException.builder()
                .message("Rate limit exceeded")
                .awsErrorDetails(software.amazon.awssdk.awscore.exception.AwsErrorDetails.builder()
                        .errorCode("ThrottlingException")
                        .errorMessage("Rate limit exceeded")
                        .build())
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow(sesException);

        // When / Then
        assertThatThrownBy(() -> emailService.sendEmail(to, subject, htmlBody, textBody))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send email via AWS SES")
                .hasCauseInstanceOf(SesException.class);

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle very long email body")
    void sendEmail_LongBody_SendsSuccessfully() {
        // Given
        emailService = createEmailServiceWithMockedClient(true);

        String to = "test@example.com";
        String subject = "Long Email Test";
        StringBuilder longBody = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longBody.append("This is a very long email body line number ").append(i).append(". ");
        }

        SendEmailResponse response = SendEmailResponse.builder()
                .messageId("long-message-123")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(response);

        // When
        emailService.sendEmail(to, subject, longBody.toString());

        // Then
        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    @DisplayName("Should send multiple emails successfully")
    void sendEmail_MultipleRecipients_SendsMultiple() {
        // Given
        emailService = createEmailServiceWithMockedClient(true);

        String[] recipients = {"user1@example.com", "user2@example.com", "user3@example.com"};
        String subject = "Bulk Email";
        String body = "<p>Test bulk email</p>";

        SendEmailResponse response = SendEmailResponse.builder()
                .messageId("bulk-message")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(response);

        // When
        for (String recipient : recipients) {
            emailService.sendEmail(recipient, subject, body);
        }

        // Then
        verify(sesClient, times(3)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    @DisplayName("Should handle special characters in email body")
    void sendEmail_SpecialCharacters_SendsSuccessfully() {
        // Given
        emailService = createEmailServiceWithMockedClient(true);

        String to = "test@example.com";
        String subject = "Special Characters Test";
        String body = "Email with special characters: <>&\"' àéïöü 中文 日本語 한글";

        SendEmailResponse response = SendEmailResponse.builder()
                .messageId("special-chars-123")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(response);

        // When
        emailService.sendEmail(to, subject, body);

        // Then
        verify(sesClient, times(1)).sendEmail(sendEmailRequestCaptor.capture());

        SendEmailRequest request = sendEmailRequestCaptor.getValue();
        assertThat(request.message().body().html().data()).isEqualTo(body); // HTML body, not text
    }

    // ==================== Helper Methods ====================

    /**
     * Create EmailService with mocked SES client using reflection.
     */
    private EmailService createEmailServiceWithMockedClient(boolean sesEnabled) {
        EmailService service = new EmailService(
                fromEmail,
                region,
                false // Disable to prevent real client creation
        );

        // Use reflection to inject mocked SES client and enable flag
        try {
            java.lang.reflect.Field clientField = EmailService.class.getDeclaredField("sesClient");
            clientField.setAccessible(true);
            clientField.set(service, sesClient);

            java.lang.reflect.Field enabledField = EmailService.class.getDeclaredField("sesEnabled");
            enabledField.setAccessible(true);
            enabledField.set(service, sesEnabled);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mocked SES client", e);
        }

        return service;
    }
}
