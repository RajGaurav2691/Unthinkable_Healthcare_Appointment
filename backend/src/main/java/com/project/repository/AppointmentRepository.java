package com.project.repository;

import com.project.entity.Appointment;
import com.project.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(UUID patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(Long doctorId);

    List<Appointment> findByDoctorIdAndAppointmentDateOrderByStartTimeAsc(Long doctorId, LocalDate appointmentDate);

    @Query("SELECT a FROM Appointment a WHERE a.status = :status AND a.createdAt < :expiryTime")
    List<Appointment> findExpiredHolds(@Param("status") AppointmentStatus status, @Param("expiryTime") LocalDateTime expiryTime);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.startTime = :time AND a.status IN ('HELD', 'CONFIRMED', 'COMPLETED', 'NO_SHOW')")
    boolean existsActiveAppointment(@Param("doctorId") Long doctorId, @Param("date") LocalDate date, @Param("time") java.time.LocalTime time);
}
