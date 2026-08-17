package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionService;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EncryptionService encryptionService;

    private String userToken;
    private Long testUserId;

    @BeforeEach
    void setUp() throws Exception {
        // Создаём тестового пользователя
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("pass123"));
        user.setEmail("user@test.com");
        user.setRole(Role.USER);
        userRepository.save(user);
        this.testUserId = user.getId();

        // Создаём 2 карты с зашифрованными номерами
        Card card1 = new Card();
        card1.setCardNumber(encryptionService.encrypt("4111111111111111"));
        card1.setOwnerName("testuser");
        card1.setExpiryDate(LocalDate.of(2027, 12, 31));
        card1.setStatus(CardStatus.ACTIVE);
        card1.setBalance(BigDecimal.valueOf(1000));
        card1.setUser(user);
        cardRepository.save(card1);

        Card card2 = new Card();
        card2.setCardNumber(encryptionService.encrypt("4222222222222222"));
        card2.setOwnerName("testuser");
        card2.setExpiryDate(LocalDate.of(2027, 12, 31));
        card2.setStatus(CardStatus.ACTIVE);
        card2.setBalance(BigDecimal.valueOf(500));
        card2.setUser(user);
        cardRepository.save(card2);

        // Получаем JWT токен
        AuthRequest loginReq = new AuthRequest();
        loginReq.setUsername("testuser");
        loginReq.setPassword("pass123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
        this.userToken = "Bearer " + token;
    }

    @Test
    void getMyCards_requiresAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyCards_success() throws Exception {
        mockMvc.perform(get("/api/cards")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void blockCard_requiresAuth_returns401() throws Exception {
        mockMvc.perform(put("/api/cards/1/block"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void transfer_requiresAuth_returns401() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
