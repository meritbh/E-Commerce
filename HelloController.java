package com.lockedin.myapp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class HelloController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ProductRepository productRepository;
    private final CartService cartService;

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

    @GetMapping("/products")
    public String listingPage(@RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "minPrice", required = false) Double minPrice,
                            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                            Model model) {
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
    public String sell() {
        return "sell";
    }

    @PostMapping("/sell")
    public String processSell(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("images") MultipartFile[] images,  // Array to handle multiple images
            Model model) {
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
        String uploadDir = "/Users/jineshpatel/Documents/Projects/FullStack/Ecom/uploads/";
        return new FileSystemResource(uploadDir + filename);
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

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        userRepository.save(user);

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
