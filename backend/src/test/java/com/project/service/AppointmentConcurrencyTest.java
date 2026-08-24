package com.project.service;

import com.project.dto.request.HoldRequest;
import com.project.entity.DoctorProfile;
import com.project.entity.Role;
import com.project.entity.User;
import com.project.entity.WorkingSchedule;
import com.project.repository.AppointmentRepository;
import com.project.repository.CalendarEventRepository;
import com.project.repository.DoctorProfileRepository;
import com.project.repository.MedicationScheduleRepository;
import com.project.repository.NotificationRepository;
import com.project.repository.UserRepository;
import com.project.repository.WorkingScheduleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class AppointmentConcurrencyTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private WorkingScheduleRepository workingScheduleRepository;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private MedicationScheduleRepository medicationScheduleRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private DoctorProfile doctor;
    private List<User> patients = new ArrayList<>();
    private LocalDate testDate;
    private LocalTime testTime;

    @BeforeEach
    void setUp() {
        medicationScheduleRepository.deleteAll();
        notificationRepository.deleteAll();
        calendarEventRepository.deleteAll();
        appointmentRepository.deleteAll();
        workingScheduleRepository.deleteAll();
        doctorProfileRepository.deleteAll();
        userRepository.deleteAll();

        // Create Doctor
        User docUser = User.builder()
                .name("Dr. Smith")
                .email("smith@example.com")
                .password("password123")
                .role(Role.DOCTOR)
                .enabled(true)
                .build();
        docUser = userRepository.save(docUser);

        doctor = DoctorProfile.builder()
                .user(docUser)
                .specialization("General")
                .qualification("MD")
                .experience(10)
                .consultationDuration(30)
                .activeStatus(true)
                .build();
        doctor = doctorProfileRepository.save(doctor);

        WorkingSchedule ws = WorkingSchedule.builder()
                .doctorProfile(doctor)
                .dayOfWeek(java.time.DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();
        workingScheduleRepository.save(ws);

        // Patients
        for (int i = 0; i < 10; i++) {
            User p = User.builder()
                    .name("Patient " + i)
                    .email("patient" + i + "@example.com")
                    .password("password123")
                    .role(Role.PATIENT)
                    .enabled(true)
                    .build();
            patients.add(userRepository.save(p));
        }

        testDate = LocalDate.now().plusDays(1);
        while (testDate.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            testDate = testDate.plusDays(1);
        }
        testTime = LocalTime.of(10, 0);
    }

    @AfterEach
    void tearDown() {
        medicationScheduleRepository.deleteAll();
        notificationRepository.deleteAll();
        calendarEventRepository.deleteAll();
        appointmentRepository.deleteAll();
    }

    @Test
    void testConcurrentBooking() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    latch.await(); // wait for all threads to be ready
                    HoldRequest request = HoldRequest.builder()
                            .doctorId(doctor.getId())
                            .appointmentDate(testDate)
                            .startTime(testTime)
                            .build();

                    appointmentService.holdSlot(request, patients.get(index).getEmail());
                    successCount.incrementAndGet();
                } catch (ResponseStatusException e) {
                    if (e.getStatusCode().value() == 409) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        latch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "Exactly one appointment should succeed");
        assertEquals(9, conflictCount.get(), "Exactly 9 appointments should fail with 409 Conflict");
        assertEquals(1, appointmentRepository.findAll().size(), "Only one appointment should be saved in DB");
    }
}
