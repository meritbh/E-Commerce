package com.lockedin.myapp;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // Find token by its value
    Optional<PasswordResetToken> findByToken(String token);

    // Count tokens by user
    long countByUser(User user);

    // Delete all tokens associated with a specific user
    @Transactional
    void deleteByUser(User user);
}
