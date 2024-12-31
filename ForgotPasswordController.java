package com.lockedin.myapp;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("/forgot-password")
    @Transactional
    public String processForgotPassword(@RequestParam("email") String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Delete any existing tokens for this user
            tokenRepository.deleteByUser(user);

            // Confirm deletion by logging remaining tokens for the user
            long remainingTokens = tokenRepository.countByUser(user);
            System.out.println("Remaining tokens for user after deletion: " + remainingTokens);

            if (remainingTokens > 0) {
                System.out.println("Failed to delete tokens for user: " + user.getEmail());
                return "redirect:/forgot-password?error=Error processing reset request";
            }

            // Create a new token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(
                    token,
                    user,
                    new Date(System.currentTimeMillis() + 3600000) // Token valid for 1 hour
            );

            tokenRepository.save(resetToken);

            // Send email with the reset link
            String resetUrl = "http://localhost:8080/reset-password?token=" + token;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Password Reset Request");
            message.setText("To reset your password, click the link below:\n" + resetUrl);

            mailSender.send(message);

            System.out.println("Reset email sent to: " + email);
        } else {
            System.out.println("No user found with email: " + email);
        }

        return "forgot-password";
    }
}
