package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.exception.UserNotFoundException;
import com.ecommerce.ecommerce.repository.*;
import lombok.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service handling user registration logic.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokenRepository sessionTokenRepository;
    private final WishlistRepository wishlistRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final FeedbackRepository feedbackRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final JavaMailSender mailSender;

    private final RestTemplate restTemplate;

    private String recaptchaSecret = "TEST";

    private String crmApiUrl = "https://www.google.com/recaptcha/api/siteverify";

    private static final Set<String> TANZANIA_REGIONS = Set.of(
            "Arusha", "Dar es Salaam", "Dodoma", "Mwanza", "Mbeya", "Kilimanjaro"
    );

    private String uploadDir = Paths.get("").toAbsolutePath().toString() + "/uploads/profile_photos/";

    // A simple list of valid Tanzanian regions — extend as needed
    private static final Set<String> VALID_REGIONS = new HashSet<>(Arrays.asList(
            "Dar es Salaam", "Arusha", "Dodoma", "Mwanza", "Zanzibar", "Mbeya", "Iringa", "Tanga", "Kilimanjaro", "Morogoro"
    ));

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SessionTokenRepository sessionTokenRepository, WishlistRepository wishlistRepository, CartRepository cartRepository, OrderRepository orderRepository, ReviewRepository reviewRepository, FeedbackRepository feedbackRepository, ApiKeyRepository apiKeyRepository, JavaMailSender mailSender, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionTokenRepository = sessionTokenRepository;
        this.wishlistRepository = wishlistRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.feedbackRepository = feedbackRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.mailSender = mailSender;
        this.restTemplate = restTemplate;
    }

    /**
     * Register a new user. Throws IllegalArgumentException for invalid region or email already used.
     */
    @Transactional
    public User registerUser(RegisterRequest req) {
        // Validate region (business rule)
        if (!isValidRegion(req.getRegion())) {
            throw new IllegalArgumentException("Invalid region. Supported regions: " + VALID_REGIONS);
        }

        // Check email uniqueness
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Hash the password using BCryptPasswordEncoder
        String hashed = passwordEncoder.encode(req.getPassword());

        // Create user entity and set default values (balance=0, role=user, profilePhotoPath=null)
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(hashed);
        user.setRegion(req.getRegion());
        user.setBalance(BigDecimal.ZERO);
        user.setRole(User.Role.USER); // or use enum if your entity defines Role
        user.setProfilePhotoPath(null);

        try {
            // Persist using JPA
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // Handle DB-level uniqueness/constraint issues gracefully
            throw new IllegalArgumentException("Could not create user: " + ex.getRootCause().getMessage());
        }
    }

    private boolean isValidRegion(String region) {
        if (region == null) return false;
        // Simple case-insensitive check
        return VALID_REGIONS.stream().anyMatch(r -> r.equalsIgnoreCase(region.trim()));
    }

    /**
     * Fetch user by ID
     *
     * @param userId the ID of the user
     * @return Optional<User>
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Updates user's shipping address and saves it as a formatted text.
     */
    public User updateShippingAddress(Long userId, ShippingAddressRequest request) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        User user = optionalUser.get();

        // Store formatted text (could be JSON if preferred)
        String formattedAddress = String.format(
                "%s, %s, %s, %s, %s",
                request.getStreet(),
                request.getCity(),
                request.getRegion(),
                request.getPostalCode(),
                request.getCountry()
        );

        user.setShippingAddress(formattedAddress);
        user.setRegion(request.getRegion()); // update region if changed

        return userRepository.save(user);
    }

    /**
     * Uploads profile photo for a user and updates the database.
     */
    public String uploadProfilePhoto(Long userId, MultipartFile file) throws IOException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("No file provided or file is empty.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        if (!extension.equalsIgnoreCase(".jpg") && !extension.equalsIgnoreCase(".jpeg") &&
                !extension.equalsIgnoreCase(".png")) {
            throw new RuntimeException("Invalid file type. Only JPG, JPEG, PNG allowed.");
        }

        // Ensure upload directory exists
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        // Generate unique filename
        String uniqueFilename = UUID.randomUUID() + extension;
        String filePath = Paths.get(uploadDir, uniqueFilename).toString();

        // Save file to disk
        file.transferTo(new File(filePath));

        // Update user's profile_photo_path
        User user = optionalUser.get();
        user.setProfilePhotoPath(filePath);
        userRepository.save(user);

        // Return URL (replace domain with actual API URL)
        return "https://api.myecommerce.co.tz/uploads/profile_photos/" + uniqueFilename;
    }

    /**
     * Delete user and all related data in a transaction.
     * Rollback on any failure to ensure data integrity.
     */
    @Transactional
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Delete related entities
        sessionTokenRepository.deleteByUserId(userId);
        wishlistRepository.deleteByUserId(userId);
        cartRepository.deleteByUserId(userId);
        reviewRepository.deleteByUserId(userId);
        feedbackRepository.deleteByUserId(userId);

        // Delete orders and cascade will handle order_items if configured
        orderRepository.deleteByUserId(userId);

        // Finally delete user
        userRepository.delete(user);
    }

    /**
     * Fetch a user's role by their ID
     */
    public UserRoleDto getUserRole(Long userId) {
        // Fetch user by ID, throw exception if not found
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Map to DTO
        return new UserRoleDto(user.getId(), user.getRoleStr());
    }

    // Fetch all registered users
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Map User entity to DTO
    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRegion(),
                user.getCreatedAt()
        );
    }

    public AdvancedUserRegistrationResponse registerAdvanced(AdvancedUserRegistrationRequest request) {

        // 1) Verify CAPTCHA
        String recaptchaVerifyUrl = "https://www.google.com/recaptcha/api/siteverify" +
                "?secret=" + recaptchaSecret +
                "&response=" + request.getCaptchaToken();

        var captchaResponse = restTemplate.postForObject(recaptchaVerifyUrl, null, CaptchaResponse.class);
        if (captchaResponse == null || !captchaResponse.isSuccess()) {
            throw new RuntimeException("CAPTCHA verification failed");
        }

        // 2) Check unique email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 3) Validate region
        if (!TANZANIA_REGIONS.contains(request.getRegion())) {
            throw new RuntimeException("Invalid Tanzanian region");
        }

        // 4) Save user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRegion(request.getRegion());
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 5) Send welcome email
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("Welcome to Our E-Commerce Platform");
        mail.setText("Hello " + user.getName() + ",\n\nYour account has been created successfully!");
        mailSender.send(mail);

        // 6) Sync with CRM
        try {
            restTemplate.postForObject(crmApiUrl, user, Void.class);
        } catch (Exception e) {
            // Log error but do not block registration
            System.err.println("CRM sync failed: " + e.getMessage());
        }

        // 7) Return response DTO
        AdvancedUserRegistrationResponse.Profile profile =
                new AdvancedUserRegistrationResponse.Profile();
        profile.setName(user.getName());
        profile.setRegion(user.getRegion());

        AdvancedUserRegistrationResponse response = new AdvancedUserRegistrationResponse();
        response.setStatus("success");
        response.setUserId(user.getId());
        response.setMessage("User registered successfully");
        response.setProfile(profile);

        return response;
    }

    // Inner DTO for CAPTCHA response
    private static class CaptchaResponse {
        private boolean success;
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
}