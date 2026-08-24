package com.project.repository;

import com.project.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DoctorProfile d WHERE d.id = :id")
    Optional<DoctorProfile> findByIdWithLock(@Param("id") Long id);

    Optional<DoctorProfile> findByUserId(UUID userId);
    
    // For patient search
    List<DoctorProfile> findByActiveStatusTrue();
    List<DoctorProfile> findByActiveStatusTrueAndSpecializationContainingIgnoreCase(String specialization);
}
