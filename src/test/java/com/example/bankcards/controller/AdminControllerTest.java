package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private Long testUserId;

    @BeforeEach
    void setUp() throws Exception {
        // Создаём тестового администратора
        User user = new User();
        user.setUsername("testadmin");
        user.setPassword(passwordEncoder.encode("pass123"));
        user.setEmail("admin@test.com");
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        this.testUserId = user.getId();

        // Получаем JWT токен
        AuthRequest loginReq = new AuthRequest();
        loginReq.setUsername("testadmin");
        loginReq.setPassword("pass123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
        this.adminToken = "Bearer " + token;
    }

    @Test
    void createCard_success() throws Exception {
        CardRequest request = new CardRequest();
        request.setCardNumber("4111222233334444");
        request.setExpiryDate(LocalDate.of(2027, 12, 31));
        request.setUserId(testUserId);

        mockMvc.perform(post("/api/admin/cards")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.cardNumber").exists());
    }

    @Test
    void createCard_invalidData_returns400() throws Exception {
        CardRequest request = new CardRequest();
        request.setCardNumber(""); // @NotBlank нарушен
        request.setExpiryDate(null); // @NotNull нарушен
        request.setUserId(testUserId);

        mockMvc.perform(post("/api/admin/cards")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCards_requiresAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blockCard_requiresAuth_returns401() throws Exception {
        mockMvc.perform(put("/api/admin/cards/1/block"))
                .andExpect(status().isUnauthorized());
    }
}
