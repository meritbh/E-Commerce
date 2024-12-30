package com.lockedin.myapp;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<CartItem, Long> {
    // You can add custom queries here if needed
}
