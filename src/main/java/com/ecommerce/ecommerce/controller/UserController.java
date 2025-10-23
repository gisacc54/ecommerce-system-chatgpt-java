package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.RegisterRequest;
import com.ecommerce.ecommerce.dto.RegisterResponse;
import com.ecommerce.ecommerce.dto.ShippingAddressRequest;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for user-related endpoints.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /users/register
     * Registers a new user after validating input and hashing password.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Delegate to service which performs additional validation and persistence
            User created = userService.registerUser(request);

            RegisterResponse resp = new RegisterResponse(true, created.getId(), "User registered successfully");

            // Return HTTP 201 Created with response body
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException ex) {
            // Bad request (validation or business rule)
            RegisterResponse resp = new RegisterResponse(false, null, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        } catch (Exception ex) {
            // Generic server error
            RegisterResponse resp = new RegisterResponse(false, null, "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();

        // Fetch user from DB
        return userService.getUserById(id).map(user -> {
            // User found - return user info
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("region", user.getRegion());
            userData.put("shippingAddress", user.getShippingAddress());
            userData.put("balance", user.getBalance());
            userData.put("role", user.getRole());
            userData.put("profilePhotoPath", user.getProfilePhotoPath());

            response.put("success", true);
            response.put("user", userData);
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            // User not found
            response.put("success", false);
            response.put("message", "User with ID " + id + " not found.");
            return ResponseEntity.status(404).body(response);
        });
    }

    /**
     * PUT /users/shipping/{id}
     * Updates the user's shipping address.
     */
    @PutMapping("/shipping/{id}")
    public ResponseEntity<?> updateShippingAddress(
            @PathVariable Long id,
            @Valid @RequestBody ShippingAddressRequest request) {

        try {
            User updatedUser = userService.updateShippingAddress(id, request);

            return ResponseEntity.ok(new java.util.HashMap<>() {{
                put("status", "success");
                put("message", "Shipping address updated successfully.");
                put("user", updatedUser);
            }});
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new java.util.HashMap<>() {{
                put("status", "error");
                put("message", e.getMessage());
            }});
        }
    }

    /**
     * POST /users/photo/upload/{id}
     * Upload profile photo for a user
     */
    @PostMapping("/photo/upload/{id}")
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        try {
            String fileUrl = userService.uploadProfilePhoto(id, file);

            HashMap<String, Object> response = new HashMap<>();
            response.put("message", "Profile photo uploaded successfully");
            response.put("fileUrl", fileUrl);
            response.put("userId", id);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            HashMap<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            HashMap<String, Object> error = new HashMap<>();
            error.put("message", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * DELETE /users/{id} - Deletes a user account and all related data
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUserById(id);
            return ResponseEntity.ok().body(
                    new ApiResponse(true, "User account deleted successfully", id)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(false, e.getMessage(), id)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new ApiResponse(false, "Internal server error: " + e.getMessage(), id)
            );
        }
    }

    // Simple response wrapper
    record ApiResponse(boolean success, String message, Long deletedUserId) {}
}