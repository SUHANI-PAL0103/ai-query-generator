package com.suhani.aiquerygenerator.controller;

import com.suhani.aiquerygenerator.entity.RegisteredUser;
import com.suhani.aiquerygenerator.repository.RegisteredUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Handles user registration and tracking via Clerk auth.
 * Stores only registry data in MongoDB - no query data.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final RegisteredUserRepository userRepository;

    public AuthController(RegisteredUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> body) {
        try {
            String clerkId = body.get("clerkId");
            String email = body.get("email");
            String name = body.get("name");
            String imageUrl = body.get("imageUrl");

            if (clerkId == null || email == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "clerkId and email required"));
            }

            Optional<RegisteredUser> existing = userRepository.findByClerkId(clerkId);
            RegisteredUser user;
            if (existing.isPresent()) {
                user = existing.get();
                user.setName(name != null ? name : user.getName());
                user.setImageUrl(imageUrl != null ? imageUrl : user.getImageUrl());
                user.setLastActiveAt(java.time.LocalDateTime.now());
            } else {
                user = new RegisteredUser(clerkId, email, name, imageUrl);
            }

            userRepository.save(user);
            logger.info("User registered/updated: {} ({})", email, clerkId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", user.getId(),
                "isAdmin", user.isAdmin()
            ));
        } catch (Exception e) {
            logger.error("User registration error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("X-Clerk-User-Id") String clerkId) {
        Optional<RegisteredUser> user = userRepository.findByClerkId(clerkId);
        if (user.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "registered", true,
                "isAdmin", user.get().isAdmin(),
                "name", user.get().getName(),
                "email", user.get().getEmail()
            ));
        }
        return ResponseEntity.ok(Map.of("registered", false));
    }
}