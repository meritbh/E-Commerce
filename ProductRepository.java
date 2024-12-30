package com.lockedin.myapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    private List<Product> products = new ArrayList<>();

    // Find a product by its ID
    public Optional<Product> findById(Long id) {
        return products.stream()
            .filter(product -> product.getId().equals(id))
            .findFirst();
    }

    // Find products by name
    public List<Product> findByName(String name) {
        return products.stream()
            .filter(product -> product.getName().equalsIgnoreCase(name))
            .collect(Collectors.toList());
    }

    // Find products within a price range
    public List<Product> findByPriceRange(Double minPrice, Double maxPrice) {
        return products.stream()
            .filter(product -> product.getPrice() >= minPrice && product.getPrice() <= maxPrice)
            .collect(Collectors.toList());
    }

    // Other methods like findAll and save
    public List<Product> findAll() {
        return new ArrayList<>(products);
    }

    public void save(Product product) {
        products.add(product);
    }
}
