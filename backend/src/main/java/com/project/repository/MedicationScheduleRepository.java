package com.project.repository;

import com.project.entity.MedicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationScheduleRepository extends JpaRepository<MedicationSchedule, Long> {
    List<MedicationSchedule> findByActiveTrue();
    List<MedicationSchedule> findByPatientIdAndActiveTrue(UUID patientId);
}
