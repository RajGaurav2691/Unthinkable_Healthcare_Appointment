package com.project.repository;

import com.project.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    Optional<DoctorProfile> findByUserId(UUID userId);
    
    // For patient search
    List<DoctorProfile> findByActiveStatusTrue();
    List<DoctorProfile> findByActiveStatusTrueAndSpecializationContainingIgnoreCase(String specialization);
}
