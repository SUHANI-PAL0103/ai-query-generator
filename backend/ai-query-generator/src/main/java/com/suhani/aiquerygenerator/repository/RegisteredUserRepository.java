package com.suhani.aiquerygenerator.repository;

import com.suhani.aiquerygenerator.entity.RegisteredUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegisteredUserRepository extends MongoRepository<RegisteredUser, String> {
    Optional<RegisteredUser> findByClerkId(String clerkId);
    Optional<RegisteredUser> findByEmail(String email);
    long countByIsAdmin(boolean isAdmin);
}