package org.jan.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around Spring's {@link JavaMailSender}.
 * If no mail sender is configured (e.g. in local dev / H2 mode) the send is skipped
 * and the reset link is logged instead.
 */
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    @Value("${app.mail.from:noreply@challengeme.app}")
    private String mailFrom;

    /**
     * Sends a password-reset link to the given email address.
     * When no {@link JavaMailSender} bean is present (no SMTP configured), logs the
     * link to stdout so you can still test the flow locally.
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = baseUrl + "/reset-password?token=" + token;

        if (mailSender == null) {
            System.out.println("[EmailService] No mail sender configured. Reset URL: " + resetUrl);
            return;
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(mailFrom);
        msg.setTo(toEmail);
        msg.setSubject("ChallengeMe – Password Reset");
        msg.setText(
            "Hello,\n\n" +
            "You requested a password reset for your ChallengeMe account.\n\n" +
            "Click the link below to set a new password (valid for 1 hour):\n" +
            resetUrl + "\n\n" +
            "If you did not request this, you can safely ignore this email.\n\n" +
            "— ChallengeMe Team"
        );

        mailSender.send(msg);
    }
}
