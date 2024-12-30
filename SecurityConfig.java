package com.lockedin.myapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // or another encoder of your choice
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", 
                    "/signup", 
                    "/custom-login", 
                    "/forgot-password", 
                    "/reset-password",
                    "/products", 
                    "/sell", 
                    "/cart", // Allow access to the cart page
                    "/cart/add", // Allow adding to cart
                    "/cart/remove", // Allow removing from cart
                    "/styles.css", 
                    "/products.css", 
                    "/scripts.js",
                    "/home.html",
                    "/sell.css",
                    "/sell.html"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}