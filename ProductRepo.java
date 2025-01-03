package com.lockedin.myapp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// This interface extends JpaRepository to provide CRUD operations on the Product entity.
@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {
    // Custom query method to find products by their name
    List<Product> findByName(String name);

    // Custom query method to find products within a price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}
