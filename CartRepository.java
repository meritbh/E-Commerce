package com.lockedin.myapp;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT c FROM CartItem c WHERE c.user = :user AND c.product = :product")
    Optional<CartItem> findByUserAndProduct(@Param("user") User user, @Param("product") Product product);

    @Query("SELECT c FROM CartItem c WHERE c.user = :user")
    List<CartItem> findByUser(@Param("user") User user);
}
