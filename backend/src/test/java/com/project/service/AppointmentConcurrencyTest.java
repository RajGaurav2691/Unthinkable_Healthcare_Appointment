package com.project.service;

import com.project.dto.request.HoldRequest;
import com.project.entity.DoctorProfile;
import com.project.entity.Role;
import com.project.entity.User;
import com.project.entity.WorkingSchedule;
import com.project.repository.AppointmentRepository;
import com.project.repository.DoctorProfileRepository;
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

    private DoctorProfile doctor;
    private List<User> patients = new ArrayList<>();
    private LocalDate testDate;
    private LocalTime testTime;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        workingScheduleRepository.deleteAll();
        doctorProfileRepository.deleteAll();
        userRepository.deleteAll();

        // Create Doctor
        User doctorUser = userRepository.save(User.builder().name("Dr. Test").email("drtest@test.com").password("pass").role(Role.DOCTOR).enabled(true).build());
        doctor = doctorProfileRepository.save(DoctorProfile.builder()
                .user(doctorUser)
                .specialization("Test")
                .qualification("MD")
                .experience(10)
                .consultationDuration(30)
                .activeStatus(true)
                .build());
        
        testDate = LocalDate.now().plusDays(1);
        testTime = LocalTime.of(10, 0);

        workingScheduleRepository.save(WorkingSchedule.builder()
                .doctorProfile(doctor)
                .dayOfWeek(testDate.getDayOfWeek())
                .startTime(testTime)
                .endTime(LocalTime.of(12, 0))
                .build());

        // Create 10 Patients
        for (int i = 0; i < 10; i++) {
            User patient = userRepository.save(User.builder().name("Patient " + i).email("patient" + i + "@test.com").password("pass").role(Role.PATIENT).enabled(true).build());
            patients.add(patient);
        }
    }

    @AfterEach
    void tearDown() {
        appointmentRepository.deleteAll();
        workingScheduleRepository.deleteAll();
        doctorProfileRepository.deleteAll();
        userRepository.deleteAll();
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
