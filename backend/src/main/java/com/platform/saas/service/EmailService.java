package com.platform.saas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

/**
 * Email service for sending emails via AWS SES.
 */
@Service
@Slf4j
public class EmailService {

    private final SesClient sesClient;
    private final String fromEmail;
    private final boolean sesEnabled;

    public EmailService(
            @Value("${aws.ses.from-email:noreply@example.com}") String fromEmail,
            @Value("${aws.ses.region:us-east-1}") String region,
            @Value("${aws.ses.enabled:false}") boolean sesEnabled) {
        this.fromEmail = fromEmail;
        this.sesEnabled = sesEnabled;

        if (sesEnabled) {
            try {
                this.sesClient = SesClient.builder()
                        .region(Region.of(region))
                        .credentialsProvider(DefaultCredentialsProvider.create())
                        .build();
                log.info("AWS SES client initialized for region: {}", region);
            } catch (Exception e) {
                log.error("Failed to initialize AWS SES client: {}", e.getMessage());
                throw new RuntimeException("Failed to initialize AWS SES client", e);
            }
        } else {
            this.sesClient = null;
            log.warn("AWS SES is disabled - emails will only be logged");
        }
    }

    /**
     * Send an email to a recipient.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body (HTML format)
     * @throws RuntimeException if email sending fails
     */
    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email to: {} with subject: {}", to, subject);

        if (!sesEnabled || sesClient == null) {
            log.warn("AWS SES is disabled - email not actually sent");
            log.debug("Email details - To: {}, Subject: {}, Body: {}", to, subject, body);
            return;
        }

        try {
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(Destination.builder()
                            .toAddresses(to)
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .charset("UTF-8")
                                    .data(subject)
                                    .build())
                            .body(Body.builder()
                                    .html(Content.builder()
                                            .charset("UTF-8")
                                            .data(body)
                                            .build())
                                    .build())
                            .build())
                    .source(fromEmail)
                    .build();

            SendEmailResponse response = sesClient.sendEmail(emailRequest);
            log.info("Email sent successfully to {}. Message ID: {}", to, response.messageId());
        } catch (SesException e) {
            log.error("Failed to send email to {}: {} (Error code: {})",
                    to, e.getMessage(), e.awsErrorDetails().errorCode());
            throw new RuntimeException("Failed to send email via AWS SES", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send an email with both HTML and plain text versions.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody Email body in HTML format
     * @param textBody Email body in plain text format
     */
    public void sendEmail(String to, String subject, String htmlBody, String textBody) {
        log.info("Sending email to: {} with subject: {}", to, subject);

        if (!sesEnabled || sesClient == null) {
            log.warn("AWS SES is disabled - email not actually sent");
            return;
        }

        try {
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(Destination.builder()
                            .toAddresses(to)
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .charset("UTF-8")
                                    .data(subject)
                                    .build())
                            .body(Body.builder()
                                    .html(Content.builder()
                                            .charset("UTF-8")
                                            .data(htmlBody)
                                            .build())
                                    .text(Content.builder()
                                            .charset("UTF-8")
                                            .data(textBody)
                                            .build())
                                    .build())
                            .build())
                    .source(fromEmail)
                    .build();

            SendEmailResponse response = sesClient.sendEmail(emailRequest);
            log.info("Email sent successfully to {}. Message ID: {}", to, response.messageId());
        } catch (SesException e) {
            log.error("Failed to send email to {}: {} (Error code: {})",
                    to, e.getMessage(), e.awsErrorDetails().errorCode());
            throw new RuntimeException("Failed to send email via AWS SES", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
