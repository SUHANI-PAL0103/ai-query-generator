package com.suhani.aiquerygenerator.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * MongoDB document for tracking registered users (admin registry only).
 * Contains minimal user info - no query data stored here.
 */
@Document(collection = "registered_users")
public class RegisteredUser {

    @Id
    private String id;
    private String clerkId;
    private String email;
    private String name;
    private String imageUrl;
    private LocalDateTime registeredAt;
    private LocalDateTime lastActiveAt;
    private long totalQueriesGenerated;
    private boolean isAdmin;

    public RegisteredUser() {}

    public RegisteredUser(String clerkId, String email, String name, String imageUrl) {
        this.clerkId = clerkId;
        this.email = email;
        this.name = name;
        this.imageUrl = imageUrl;
        this.registeredAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
        this.totalQueriesGenerated = 0;
        this.isAdmin = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClerkId() { return clerkId; }
    public void setClerkId(String clerkId) { this.clerkId = clerkId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public long getTotalQueriesGenerated() { return totalQueriesGenerated; }
    public void setTotalQueriesGenerated(long totalQueriesGenerated) { this.totalQueriesGenerated = totalQueriesGenerated; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
}