package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.Wishlist;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.repository.WishlistRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public WishlistService(WishlistRepository wishlistRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    /**
     * Add a product to user's wishlist
     *
     * @param userId    ID of the user
     * @param productId ID of the product
     * @return List of user's wishlist entries
     * @throws NoSuchElementException if user or product not found
     */
    @Transactional
    public List<Map<String, Object>> addToWishlist(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        // Prevent duplicates
        wishlistRepository.findByUserAndProduct(user, product)
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setUser(user);
                    wishlist.setProduct(product);
                    return wishlistRepository.save(wishlist);
                });

        // Fetch updated wishlist for user
        List<Wishlist> wishlistEntries = wishlistRepository.findByUser(user);
        List<Map<String, Object>> wishlistResponse = new ArrayList<>();
        for (Wishlist w : wishlistEntries) {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", w.getProduct().getId());
            map.put("name", w.getProduct().getName());
            map.put("price", w.getProduct().getPrice());
            wishlistResponse.add(map);
        }

        return wishlistResponse;
    }
}