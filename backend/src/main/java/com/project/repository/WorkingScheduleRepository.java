package com.project.repository;

import com.project.entity.WorkingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface WorkingScheduleRepository extends JpaRepository<WorkingSchedule, Long> {
    List<WorkingSchedule> findByDoctorProfileIdAndDayOfWeekOrderByStartTime(Long doctorProfileId, DayOfWeek dayOfWeek);
    List<WorkingSchedule> findByDoctorProfileIdOrderByDayOfWeekAscStartTimeAsc(Long doctorProfileId);
}
