package com.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.request.LoginRequest;
import com.project.dto.request.RegisterRequest;
import com.project.entity.Role;
import com.project.entity.User;
import com.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterPatientSuccessfully() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Test Patient")
                .email("patient@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("patient@example.com"))
                .andExpect(jsonPath("$.user.role").value("PATIENT"));
    }

    @Test
    void shouldFailRegistrationIfEmailExists() throws Exception {
        User user = User.builder()
                .name("Existing User")
                .email("existing@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.PATIENT)
                .enabled(true)
                .build();
        userRepository.save(user);

        RegisterRequest request = RegisterRequest.builder()
                .name("New User")
                .email("existing@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        User user = User.builder()
                .name("Test User")
                .email("login@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.PATIENT)
                .enabled(true)
                .build();
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .email("login@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("login@example.com"));
    }

    @Test
    void shouldFailLoginWithWrongPassword() throws Exception {
        User user = User.builder()
                .name("Test User")
                .email("login2@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.PATIENT)
                .enabled(true)
                .build();
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .email("login2@example.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
