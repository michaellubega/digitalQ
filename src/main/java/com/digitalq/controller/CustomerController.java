package com.digitalq.controller;

import com.digitalq.dto.TokenRequest;
import com.digitalq.dto.TokenResponse;
import com.digitalq.model.Counter;
import com.digitalq.model.Token;
import com.digitalq.service.CounterService;
import com.digitalq.service.QueueService;
import com.digitalq.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CustomerController {

    private final TokenService tokenService;
    private final CounterService counterService;
    private final QueueService queueService;

    @GetMapping("/")
    public String customerHome(Model model) {
        List<Counter> activeCounters = counterService.findActive().stream()
                .filter(c -> !c.isPaused())
                .toList();
        model.addAttribute("counters", activeCounters);
        return "customer/index";
    }

    @PostMapping("/api/tokens")
    @ResponseBody
    public ResponseEntity<?> createToken(@Valid @RequestBody TokenRequest request) {
        try {
            Token token = tokenService.createToken(request.getCustomerName(), request.getCounterId());
            Integer position = tokenService.getPosition(token.getTokenNumber());
            String qrCode = tokenService.generateQrCode(token.getTokenNumber());

            TokenResponse response = TokenResponse.fromToken(token, position, qrCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/api/tokens/{tokenNumber}")
    @ResponseBody
    public ResponseEntity<?> getTokenStatus(@PathVariable String tokenNumber) {
        try {
            Token token = tokenService.findByTokenNumber(tokenNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Token not found"));
            Integer position = tokenService.getPosition(tokenNumber);
            String qrCode = tokenService.generateQrCode(token.getTokenNumber());

            TokenResponse response = TokenResponse.fromToken(token, position, qrCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/token/{tokenNumber}")
    public String tokenPage(@PathVariable String tokenNumber, Model model) {
        try {
            Token token = tokenService.findByTokenNumber(tokenNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Token not found"));
            Integer position = tokenService.getPosition(tokenNumber);
            String qrCode = tokenService.generateQrCode(token.getTokenNumber());

            TokenResponse response = TokenResponse.fromToken(token, position, qrCode);
            model.addAttribute("token", response);
            return "customer/token";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "customer/error";
        }
    }
}
