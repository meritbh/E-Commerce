package com.lockedin.myapp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HelloController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private boolean authUser = true; // Tracks user authentication


    public HelloController(UserRepository userRepository, 
                           EmailService emailService,
                           ProductRepository productRepository,
                           CartService cartService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public String defaultLink() {
        return "custom-login";
    }

    @GetMapping("/logout1")
    public String logoutLink() {
        authUser = false; // Reset authentication
        return "redirect:/custom-login";
    }

    @GetMapping("/custom-login")
    public String main() {
        return "custom-login";
    }

    @PostMapping("/custom-login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               Model model) {
        System.out.println("processLogin method called");
        email = email.trim().toLowerCase();
        System.out.println("Entered email for query: " + email);
    
        Optional<User> userOptional = userRepository.findByEmail(email);
    
        if (userOptional.isPresent()) {
            System.out.println("User found for email: " + email);
    
            User user = userOptional.get();
            System.out.println("Entered password: " + password);
            System.out.println("Stored password: " + user.getPassword());
    
            if (user.getPassword().equals(password)) {
                authUser = true; // Set authUser to true upon successful login
                return "redirect:/products";
            } else {
                model.addAttribute("errorMessage", "Password mismatch for: " + email);
                return "custom-login";
            }
        } else {
            model.addAttribute("errorMessage", "No user found for: " + email);
            return "custom-login";
        }
    }
    private boolean isAuthenticated(Model model) {
        if (!authUser) {
        model.addAttribute("errorMessage", "You must be logged in to access this page.");
        return false;
        }
        return true;
    
    }

    @GetMapping("/products")
    public String listingPage(@RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "minPrice", required = false) Double minPrice,
                            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                            Model model, HttpServletRequest request) {
        
        // Extract CSRF token and add to model
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        model.addAttribute("_csrf", csrfToken);

        List<Product> products;

        if (search != null && !search.isEmpty()) {
            // Search by name if the search parameter is provided
            products = productRepository.findByName(search);
        } else if (minPrice != null && maxPrice != null) {
            // Filter by price range
            products = productRepository.findByPriceRange(minPrice, maxPrice);
        } else {
            // Otherwise, show all products
            products = productRepository.findAll();
        }

        model.addAttribute("products", products);
        return "home";
    }

    @GetMapping("/sell")
    public String sell(Model model) {
        if (!isAuthenticated(model)) return "custom-login";
        return "sell";
        }

    @PostMapping("/sell")
    public String processSell(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("images") MultipartFile[] images,  // Array to handle multiple images
            Model model) {
            if (!isAuthenticated(model)) return "custom-login";
        try {
            // Define the absolute path for the upload directory
            String uploadDir = "/Users/meritbhusal/src/main/resources/static/uploads";


            // Create the directory if it doesn't exist
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs(); // Create the directory
            }

            // Create a product object
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            productRepository.save(product);

            // Loop through each image and save it
            for (MultipartFile image : images) 
            {
                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                File savedFile = new File(uploadPath, fileName);
                image.transferTo(savedFile);
            
                // Save only the filename to the product's imagePaths
                product.getImagePaths().add(fileName);  // Save only the filename
            }
            

            // Save the product with its images
            productRepository.save(product);

            model.addAttribute("message", "Product listed successfully with images!");
            return "sell";
        } catch (IOException e) {
            model.addAttribute("error", "An error occurred while uploading the images.");
            return "sell";
        }
    }

    @GetMapping("/uploads/{filename}")
    @ResponseBody
    public Resource getImage(@PathVariable String filename) {
        String uploadDir = "/Users/meritbhusal/src/main/resources/static/uploads";
        return new FileSystemResource(uploadDir + "/" + filename);
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "reset-password";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@RequestParam String email,
                              @RequestParam String password,
                              Model model) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            model.addAttribute("error", "Email is already registered!");
            return "signup";
        }

        // Create and save the new user
        User newUser = new User();
        newUser.setEmail(email.trim().toLowerCase());
        newUser.setPassword(password); // Ideally, passwords should be hashed before saving
        userRepository.save(newUser);

        // Authenticate the user
        authUser = true;

        // Send welcome email
        try {
            emailService.sendResetPasswordEmail(email, "Welcome to LockedIn! Your account has been created.");
        } catch (Exception e) {
            model.addAttribute("error", "Could not send welcome email. Please try again.");
        }
        
        return "redirect:/products";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password"; // Loads templates/forgot-password.html
    }

    @GetMapping("/cart")
    public String showCart(Model model) {
        if (!isAuthenticated(model)) return "custom-login";
        System.out.println("Accessing /cart endpoint...");
        List<CartItem> cartItems = cartService.getCartItemsForUser();
        System.out.println("Cart Items: " + cartItems);
    
        if (cartItems == null || cartItems.isEmpty()) {
            model.addAttribute("emptyCartMessage", "Your cart is empty.");
            System.out.println("Cart is empty.");
        } else {
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalPrice", cartService.getTotalPriceForUser());
            System.out.println("Cart contains items. Total Price: " + cartService.getTotalPriceForUser());
        }
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam("quantity") int quantity) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            cartService.addToCart(productOptional.get(), quantity);
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("cartItemId") Long cartItemId) {
        cartService.removeFromCart(cartItemId);
        return "redirect:/cart";
    }
}
