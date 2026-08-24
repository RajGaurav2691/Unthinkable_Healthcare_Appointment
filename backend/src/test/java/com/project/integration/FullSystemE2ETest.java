package com.project.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.request.*;
import com.project.entity.*;
import com.project.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FullSystemE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MedicationScheduleRepository medicationScheduleRepository;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String patientToken;
    private String doctorToken;

    @BeforeEach
    void setUp() throws Exception {
        calendarEventRepository.deleteAll();
        medicationScheduleRepository.deleteAll();
        notificationRepository.deleteAll();
        appointmentRepository.deleteAll();
        doctorProfileRepository.deleteAll();
        userRepository.deleteAll();

        // Seed Admin
        User admin = User.builder()
                .name("Super Admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);

        // Login Admin
        LoginRequest adminLogin = LoginRequest.builder().email("admin@example.com").password("admin123").build();
        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(adminLoginResult.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void shouldPerformE2EHealthcareJourney() throws Exception {
        // 1. ADMIN: Create Doctor (Dr. House, Cardiologist, consultation duration 30m, Monday schedule)
        WorkingScheduleRequest mondaySchedule = new WorkingScheduleRequest();
        mondaySchedule.setDayOfWeek(java.time.DayOfWeek.MONDAY);
        mondaySchedule.setStartTime(LocalTime.of(9, 0));
        mondaySchedule.setEndTime(LocalTime.of(17, 0));

        DoctorCreateRequest drRequest = new DoctorCreateRequest();
        drRequest.setName("Dr. House");
        drRequest.setEmail("house@example.com");
        drRequest.setPassword("doctor123");
        drRequest.setSpecialization("Cardiology");
        drRequest.setQualification("MD");
        drRequest.setExperience(15);
        drRequest.setConsultationDuration(30);
        drRequest.setSchedules(List.of(mondaySchedule));

        MvcResult drResult = mockMvc.perform(post("/api/admin/doctors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drRequest)))
                .andExpect(status().isOk())
                .andReturn();

        long doctorId = objectMapper.readTree(drResult.getResponse().getContentAsString()).get("id").asLong();

        // Login Doctor
        LoginRequest doctorLogin = LoginRequest.builder().email("house@example.com").password("doctor123").build();
        MvcResult doctorLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctorLogin)))
                .andExpect(status().isOk())
                .andReturn();
        doctorToken = objectMapper.readTree(doctorLoginResult.getResponse().getContentAsString()).get("token").asText();

        // 2. PATIENT: Register
        RegisterRequest registerReq = RegisterRequest.builder()
                .name("Alice Cooper")
                .email("alice@example.com")
                .password("patient123")
                .build();

        MvcResult patientResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andReturn();
        patientToken = objectMapper.readTree(patientResult.getResponse().getContentAsString()).get("token").asText();

        // 3. PATIENT: Search Doctor
        mockMvc.perform(get("/api/doctors")
                        .header("Authorization", "Bearer " + patientToken)
                        .param("specialization", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dr. House"));

        // 4. PATIENT: Get Availability Slots (Choose next Monday)
        LocalDate nextMonday = LocalDate.now().plusDays(1);
        while (nextMonday.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            nextMonday = nextMonday.plusDays(1);
        }

        MvcResult slotsResult = mockMvc.perform(get("/api/doctors/" + doctorId + "/availability")
                        .header("Authorization", "Bearer " + patientToken)
                        .param("date", nextMonday.toString()))
                .andExpect(status().isOk())
                .andReturn();

        // 5. PATIENT: Hold Appointment Slot
        HoldRequest holdReq = HoldRequest.builder()
                .doctorId(doctorId)
                .appointmentDate(nextMonday)
                .startTime(LocalTime.of(9, 0))
                .build();

        MvcResult holdResult = mockMvc.perform(post("/api/appointments/hold")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HELD"))
                .andReturn();

        long appointmentId = objectMapper.readTree(holdResult.getResponse().getContentAsString()).get("id").asLong();

        // 6. PATIENT: Confirm Appointment with Symptoms -> Triggers pre-visit summary
        ConfirmRequest confirmReq = ConfirmRequest.builder()
                .symptoms("Persistent heart palpitation and mild chest pain for 3 days.")
                .build();

        mockMvc.perform(post("/api/appointments/" + appointmentId + "/confirm")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.aiSummary").exists());

        // Verify notification queued
        assertTrue(notificationRepository.existsByRelatedAppointmentIdAndType(appointmentId, NotificationType.APPOINTMENT_CONFIRMATION));

        // 7. DOCTOR: View Appointment
        mockMvc.perform(get("/api/doctor/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symptoms").value("Persistent heart palpitation and mild chest pain for 3 days."))
                .andExpect(jsonPath("$.aiSummary").exists());

        // 8. DOCTOR: Attend & Complete Appointment -> Triggers post-visit AI summary + medication schedule
        AppointmentCompleteRequest completeReq = new AppointmentCompleteRequest();
        completeReq.setClinicalNotes("Patient diagnosed with arrhythmias. Advised rest.");
        completeReq.setPrescription("Atenolol 25mg ONCE_DAILY for 10 days.");

        mockMvc.perform(patch("/api/doctor/appointments/" + appointmentId + "/complete")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.postVisitAiSummary").exists());

        // Verify medication schedule parsed & created
        List<MedicationSchedule> schedules = medicationScheduleRepository.findAll();
        assertFalse(schedules.isEmpty(), "Medication schedule should be created by parsed AI summary");
        assertEquals("Mock Medication", schedules.get(0).getMedicationName());
        assertEquals("As prescribed: Atenolol 25mg ONCE_DAILY for 10 days.", schedules.get(0).getDosage());
        assertEquals(MedicationFrequency.CUSTOM, schedules.get(0).getFrequency());

        // 9. PATIENT: View Appointment History & Dashboard
        mockMvc.perform(get("/api/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.postVisitAiSummary").exists());
    }
}
