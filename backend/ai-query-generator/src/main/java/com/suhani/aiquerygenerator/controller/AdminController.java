package com.suhani.aiquerygenerator.controller;

import com.suhani.aiquerygenerator.entity.RegisteredUser;
import com.suhani.aiquerygenerator.repository.RegisteredUserRepository;
import com.suhani.aiquerygenerator.repository.QueryHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Admin-only endpoints. Only users with isAdmin=true can access these.
 * Provides user registry data and aggregate query stats.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final RegisteredUserRepository userRepository;
    private final QueryHistoryRepository queryHistoryRepository;

    public AdminController(RegisteredUserRepository userRepository,
                           QueryHistoryRepository queryHistoryRepository) {
        this.userRepository = userRepository;
        this.queryHistoryRepository = queryHistoryRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestHeader(value = "X-Clerk-User-Id", required = false) String clerkId) {
        try {
            // Check if requester is admin
            Optional<RegisteredUser> requester = userRepository.findByClerkId(clerkId);
            if (requester.isEmpty() || !requester.get().isAdmin()) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin only."));
            }

            List<RegisteredUser> users = userRepository.findAll();
            List<Map<String, Object>> userList = new ArrayList<>();

            for (RegisteredUser user : users) {
                long queryCount = queryHistoryRepository.countByClerkId(user.getClerkId());
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("id", user.getId());
                entry.put("clerkId", user.getClerkId());
                entry.put("email", user.getEmail());
                entry.put("name", user.getName());
                entry.put("registeredAt", user.getRegisteredAt());
                entry.put("lastActiveAt", user.getLastActiveAt());
                entry.put("totalQueriesGenerated", queryCount);
                entry.put("isAdmin", user.isAdmin());
                userList.add(entry);
            }

            return ResponseEntity.ok(Map.of(
                "users", userList,
                "totalUsers", userList.size()
            ));
        } catch (Exception e) {
            logger.error("Admin users fetch error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("users", Collections.emptyList(), "totalUsers", 0));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestHeader(value = "X-Clerk-User-Id", required = false) String clerkId) {
        try {
            Optional<RegisteredUser> requester = userRepository.findByClerkId(clerkId);
            if (requester.isEmpty() || !requester.get().isAdmin()) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin only."));
            }

            long totalUsers = userRepository.count();
            long totalQueries = queryHistoryRepository.count();
            long adminCount = userRepository.countByIsAdmin(true);

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("totalQueries", totalQueries);
            stats.put("adminCount", adminCount);
            stats.put("avgQueriesPerUser", totalUsers > 0 ? totalQueries / totalUsers : 0);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Admin stats fetch error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "totalUsers", 0, "totalQueries", 0, "adminCount", 0
            ));
        }
    }
}