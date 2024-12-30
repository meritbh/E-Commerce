package com.lockedin.myapp;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResetPasswordController {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/reset-password")
    @Transactional
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String newPassword) {
        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            return "redirect:/reset-password?error=Invalid token";
        }

        PasswordResetToken resetToken = optionalToken.get();

        if (resetToken.getExpiryDate().before(new Date())) {
            tokenRepository.delete(resetToken);
            return "redirect:/reset-password?error=Expired token";
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
        return "redirect:/?message=Password successfully reset!";
    }
}
