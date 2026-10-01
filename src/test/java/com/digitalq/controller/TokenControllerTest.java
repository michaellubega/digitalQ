package com.digitalq.controller;

import com.digitalq.dto.TokenRequest;
import com.digitalq.model.Counter;
import com.digitalq.model.Token;
import com.digitalq.service.CounterService;
import com.digitalq.service.QueueService;
import com.digitalq.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private CounterService counterService;

    @MockBean
    private QueueService queueService;

    @Test
    @WithMockUser
    void customerHome_ReturnsIndexPage() throws Exception {
        Counter counter = Counter.builder()
                .id(1L)
                .name("Counter 1")
                .serviceType("General")
                .active(true)
                .build();

        when(counterService.findActive()).thenReturn(List.of(counter));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/index"))
                .andExpect(model().attributeExists("counters"));
    }

    @Test
    @WithMockUser
    void createToken_Success() throws Exception {
        TokenRequest request = new TokenRequest();
        request.setCustomerName("John Doe");
        request.setCounterId(1L);

        Token token = Token.builder()
                .id(1L)
                .tokenNumber("A-001")
                .customerName("John Doe")
                .counterId(1L)
                .status(Token.TokenStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        when(tokenService.createToken(anyString(), anyLong())).thenReturn(token);
        when(tokenService.getPosition(anyString())).thenReturn(1);
        when(tokenService.generateQrCode(anyString())).thenReturn("data:image/png;base64,test");

        mockMvc.perform(post("/api/tokens")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenNumber").value("A-001"))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    @WithMockUser
    void createToken_InvalidName() throws Exception {
        TokenRequest request = new TokenRequest();
        request.setCustomerName("A");
        request.setCounterId(1L);

        mockMvc.perform(post("/api/tokens")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getTokenStatus_Success() throws Exception {
        Token token = Token.builder()
                .id(1L)
                .tokenNumber("A-001")
                .customerName("John Doe")
                .counterId(1L)
                .status(Token.TokenStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        when(tokenService.findByTokenNumber("A-001")).thenReturn(Optional.of(token));
        when(tokenService.getPosition("A-001")).thenReturn(1);
        when(tokenService.generateQrCode("A-001")).thenReturn("data:image/png;base64,test");

        mockMvc.perform(get("/api/tokens/A-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenNumber").value("A-001"))
                .andExpect(jsonPath("$.position").value(1));
    }

    @Test
    @WithMockUser
    void getTokenStatus_NotFound() throws Exception {
        when(tokenService.findByTokenNumber("Z-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tokens/Z-999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser
    void tokenPage_Success() throws Exception {
        Token token = Token.builder()
                .id(1L)
                .tokenNumber("A-001")
                .customerName("John Doe")
                .counterId(1L)
                .status(Token.TokenStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        when(tokenService.findByTokenNumber("A-001")).thenReturn(Optional.of(token));
        when(tokenService.getPosition("A-001")).thenReturn(1);
        when(tokenService.generateQrCode("A-001")).thenReturn("data:image/png;base64,test");

        mockMvc.perform(get("/token/A-001"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/token"))
                .andExpect(model().attributeExists("token"));
    }

    @Test
    @WithMockUser
    void tokenPage_NotFound() throws Exception {
        when(tokenService.findByTokenNumber("Z-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/token/Z-999"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/error"))
                .andExpect(model().attributeExists("error"));
    }
}
