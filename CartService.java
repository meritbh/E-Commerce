package com.lockedin.myapp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    // Add a product to the cart
    public void addToCart(Product product, int quantity) {
        CartItem cartItem = new CartItem(product, quantity);
        cartRepository.save(cartItem); // Save to database
    }

    // Remove a product from the cart
    public void removeFromCart(Long cartItemId) {
        cartRepository.deleteById(cartItemId);
    }

    // Get all cart items
    public List<CartItem> getCartItems() {
        return cartRepository.findAll(); // Fetch all cart items
    }

    // Calculate the total price of the cart
    public double getTotalPrice() {
        return cartRepository.findAll().stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }
}
