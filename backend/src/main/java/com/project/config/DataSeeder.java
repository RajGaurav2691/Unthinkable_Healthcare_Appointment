package com.project.config;

import com.project.entity.Role;
import com.project.entity.User;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${application.admin.email}")
    private String adminEmail;

    @Value("${application.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .name("Super Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            System.out.println("Default admin user seeded.");
        }

        // Phase 4: Create Partial Unique Index for Appointments to prevent double-booking
        try {
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_appointment " +
                    "ON appointments (doctor_profile_id, appointment_date, start_time) " +
                    "WHERE status IN ('HELD', 'CONFIRMED', 'COMPLETED', 'NO_SHOW');");
            System.out.println("Partial unique index for appointments created/verified.");
        } catch (Exception e) {
            System.err.println("Warning: Could not create unique index. If it already exists, this is fine. " + e.getMessage());
        }
    }
}
