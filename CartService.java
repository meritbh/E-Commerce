package com.lockedin.myapp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserService userService; // A service to fetch the logged-in user

    // Add a product to the cart
    public void addToCart(Product product, int quantity) {
        User currentUser = userService.getLoggedInUser(); // Fetch the logged-in user
        Optional<CartItem> existingCartItem = cartRepository.findByUserAndProduct(currentUser, product);

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity); // Update quantity
            cartRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem(product, currentUser, quantity);
            cartRepository.save(cartItem);
        }
    }

    // Remove a product from the cart
    public void removeFromCart(Long cartItemId) {
        cartRepository.deleteById(cartItemId);
    }

    // Get all cart items for the logged-in user
    public List<CartItem> getCartItemsForUser() {
        User currentUser = userService.getLoggedInUser();
        return cartRepository.findByUser(currentUser);
    }

    // Calculate the total price for the logged-in user's cart
    public double getTotalPriceForUser() {
        return getCartItemsForUser().stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }
}
