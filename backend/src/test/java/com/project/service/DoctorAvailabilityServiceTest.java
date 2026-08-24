package com.project.service;

import com.project.dto.response.SlotResponse;
import com.project.entity.DoctorProfile;
import com.project.entity.User;
import com.project.entity.WorkingSchedule;
import com.project.repository.DoctorLeaveRepository;
import com.project.repository.DoctorProfileRepository;
import com.project.repository.WorkingScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DoctorAvailabilityServiceTest {

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @Mock
    private WorkingScheduleRepository workingScheduleRepository;

    @Mock
    private DoctorLeaveRepository doctorLeaveRepository;

    @Mock
    private DoctorAdminService doctorAdminService;

    @InjectMocks
    private DoctorAvailabilityService doctorAvailabilityService;

    private DoctorProfile activeDoctor;
    private DoctorProfile inactiveDoctor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user = new User();
        user.setId(java.util.UUID.randomUUID());
        user.setName("Test Doctor");

        activeDoctor = new DoctorProfile();
        activeDoctor.setId(1L);
        activeDoctor.setUser(user);
        activeDoctor.setConsultationDuration(30);
        activeDoctor.setActiveStatus(true);

        inactiveDoctor = new DoctorProfile();
        inactiveDoctor.setId(2L);
        inactiveDoctor.setUser(user);
        inactiveDoctor.setConsultationDuration(30);
        inactiveDoctor.setActiveStatus(false);
    }

    @Test
    void testAvailability_InactiveDoctor_ReturnsEmpty() {
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(inactiveDoctor));
        List<SlotResponse> slots = doctorAvailabilityService.getAvailability(2L, LocalDate.now().plusDays(1));
        assertEquals(0, slots.size());
    }

    @Test
    void testAvailability_PastDate_ReturnsEmpty() {
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(activeDoctor));
        List<SlotResponse> slots = doctorAvailabilityService.getAvailability(1L, LocalDate.now().minusDays(1));
        assertEquals(0, slots.size());
    }

    @Test
    void testAvailability_OnLeave_ReturnsEmpty() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(activeDoctor));
        when(doctorLeaveRepository.existsByDoctorProfileIdAndLeaveDate(1L, tomorrow)).thenReturn(true);

        List<SlotResponse> slots = doctorAvailabilityService.getAvailability(1L, tomorrow);
        assertEquals(0, slots.size());
    }

    @Test
    void testAvailability_NormalWorkingDay_ReturnsSlots() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        DayOfWeek dayOfWeek = tomorrow.getDayOfWeek();
        
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(activeDoctor));
        when(doctorLeaveRepository.existsByDoctorProfileIdAndLeaveDate(1L, tomorrow)).thenReturn(false);

        WorkingSchedule schedule = new WorkingSchedule();
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(10, 30));

        when(workingScheduleRepository.findByDoctorProfileIdAndDayOfWeekOrderByStartTime(1L, dayOfWeek))
                .thenReturn(List.of(schedule));

        List<SlotResponse> slots = doctorAvailabilityService.getAvailability(1L, tomorrow);
        
        // 9:00 - 10:30 with 30 min duration = 3 slots
        assertEquals(3, slots.size());
        assertEquals(LocalTime.of(9, 0), slots.get(0).getStartTime());
        assertEquals(LocalTime.of(9, 30), slots.get(0).getEndTime());
        assertEquals(LocalTime.of(9, 30), slots.get(1).getStartTime());
        assertEquals(LocalTime.of(10, 0), slots.get(2).getStartTime());
        assertEquals(LocalTime.of(10, 30), slots.get(2).getEndTime());
    }

    @Test
    void testAvailability_NonWorkingDay_ReturnsEmpty() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        DayOfWeek dayOfWeek = tomorrow.getDayOfWeek();

        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(activeDoctor));
        when(doctorLeaveRepository.existsByDoctorProfileIdAndLeaveDate(1L, tomorrow)).thenReturn(false);
        when(workingScheduleRepository.findByDoctorProfileIdAndDayOfWeekOrderByStartTime(1L, dayOfWeek))
                .thenReturn(new ArrayList<>());

        List<SlotResponse> slots = doctorAvailabilityService.getAvailability(1L, tomorrow);
        assertEquals(0, slots.size());
    }
}
