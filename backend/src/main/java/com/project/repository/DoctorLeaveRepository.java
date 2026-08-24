package com.project.repository;

import com.project.entity.DoctorLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave, Long> {
    List<DoctorLeave> findByDoctorProfileIdOrderByLeaveDateAsc(Long doctorProfileId);
    boolean existsByDoctorProfileIdAndLeaveDate(Long doctorProfileId, LocalDate leaveDate);
}
