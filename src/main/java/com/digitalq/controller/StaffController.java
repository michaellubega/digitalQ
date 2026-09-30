package com.digitalq.controller;

import com.digitalq.dto.VerifyTokenRequest;
import com.digitalq.model.Counter;
import com.digitalq.model.Token;
import com.digitalq.service.CounterService;
import com.digitalq.service.QueueService;
import com.digitalq.service.TokenService;
import com.digitalq.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class StaffController {

    private final TokenService tokenService;
    private final CounterService counterService;
    private final UserService userService;
    private final QueueService queueService;

    @GetMapping("/staff")
    public String staffDashboard(Authentication authentication, Model model) {
        Long userId = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();

        Counter counter = userService.getStaffCounter(userId).orElse(null);
        model.addAttribute("counter", counter);

        if (counter != null) {
            List<Token> waitingTokens = tokenService.findWaitingTokens(counter.getId());
            model.addAttribute("waitingTokens", waitingTokens);
        }

        return "staff/dashboard";
    }

    @PostMapping("/staff/verify")
    @ResponseBody
    public ResponseEntity<?> verifyToken(@Valid @RequestBody VerifyTokenRequest request) {
        try {
            Token token = tokenService.verifyToken(request.getTokenNumber());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tokenId", token.getId());
            response.put("tokenNumber", token.getTokenNumber());
            response.put("customerName", token.getCustomerName());
            response.put("status", token.getStatus().name());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/staff/serve/{tokenId}")
    @ResponseBody
    public ResponseEntity<?> serveToken(@PathVariable Long tokenId) {
        try {
            Token token = tokenService.serveToken(tokenId);
            queueService.notifyQueueUpdate(token.getCounterId());
            queueService.notifyAdminStatsUpdate();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tokenNumber", token.getTokenNumber());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/staff/next/{counterId}")
    @ResponseBody
    public ResponseEntity<?> callNextToken(@PathVariable Long counterId) {
        try {
            Token token = tokenService.callNextToken(counterId);
            queueService.notifyQueueUpdate(counterId);
            queueService.notifyTokenCalled(token.getTokenNumber());
            queueService.notifyAdminStatsUpdate();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tokenNumber", token.getTokenNumber());
            response.put("customerName", token.getCustomerName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/staff/pause/{counterId}")
    @ResponseBody
    public ResponseEntity<?> pauseCounter(@PathVariable Long counterId) {
        try {
            counterService.pauseCounter(counterId);
            queueService.notifyAdminStatsUpdate();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/staff/resume/{counterId}")
    @ResponseBody
    public ResponseEntity<?> resumeCounter(@PathVariable Long counterId) {
        try {
            counterService.resumeCounter(counterId);
            queueService.notifyAdminStatsUpdate();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/staff/queue/{counterId}")
    @ResponseBody
    public ResponseEntity<?> getQueue(@PathVariable Long counterId) {
        try {
            List<Token> waitingTokens = tokenService.findWaitingTokens(counterId);
            return ResponseEntity.ok(waitingTokens);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
