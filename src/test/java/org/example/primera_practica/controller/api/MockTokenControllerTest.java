package org.example.primera_practica.controller.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.primera_practica.dto.MockEndpointDTO;
import org.example.primera_practica.service.MockEndpointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MockTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MockEndpointService mockEndpointService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Test
    @WithMockUser(username = "tester")
    void generatesMockTokenWithExpirationNotAfterMockExpiry() throws Exception {
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(2);
        MockEndpointDTO mockEndpoint = new MockEndpointDTO();
        mockEndpoint.setExpirationDate(expirationDate);
        when(mockEndpointService.getMockEndpointById(1L)).thenReturn(mockEndpoint);

        MvcResult result = mockMvc.perform(get("/api/mocks/1/jwt"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode payload = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = payload.get("token").asText();

        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        Date exp = claims.getExpiration();
        Instant expectedExpiration = expirationDate.atZone(ZoneId.systemDefault()).toInstant();
        assertThat(exp.toInstant()).isBeforeOrEqualTo(expectedExpiration);
    }

    @Test
    @WithMockUser(username = "tester")
    void returnsConflictWhenMockEndpointAlreadyExpired() throws Exception {
        LocalDateTime expirationDate = LocalDateTime.now().minusMinutes(5);
        MockEndpointDTO mockEndpoint = new MockEndpointDTO();
        mockEndpoint.setExpirationDate(expirationDate);
        when(mockEndpointService.getMockEndpointById(2L)).thenReturn(mockEndpoint);

        mockMvc.perform(get("/api/mocks/2/jwt"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Mock endpoint has already expired"));
    }
}
