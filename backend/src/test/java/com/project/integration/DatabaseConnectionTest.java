package com.project.integration;

import com.project.entity.Role;
import com.project.entity.User;
import com.project.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles("dev") // Use dev profile to connect to real PostgreSQL, not H2 test profile
@Transactional
public class DatabaseConnectionTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testRealDatabasePersistence() {
        // Create a test user
        User testUser = User.builder()
                .name("DB Integration Test User")
                .email("dbtest@example.com")
                .password("hashedPassword")
                .role(Role.PATIENT)
                .enabled(true)
                .build();

        // Persist to the real database
        User savedUser = userRepository.save(testUser);
        
        Assertions.assertNotNull(savedUser.getId());
        
        // Retrieve it
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());
        Assertions.assertTrue(retrievedUser.isPresent());
        Assertions.assertEquals("dbtest@example.com", retrievedUser.get().getEmail());
    }
}
