package com.digitalq.controller;

import com.digitalq.dto.StatsResponse;
import com.digitalq.model.Counter;
import com.digitalq.model.User;
import com.digitalq.service.CounterService;
import com.digitalq.service.QueueService;
import com.digitalq.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CounterService counterService;
    private final UserService userService;
    private final QueueService queueService;

    @GetMapping("")
    public String adminDashboard(Model model) {
        StatsResponse stats = queueService.getStats();
        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    @GetMapping("/counters")
    public String countersPage(Model model) {
        List<Counter> counters = counterService.findAll();
        model.addAttribute("counters", counters);
        return "admin/counters";
    }

    @PostMapping("/counters")
    @ResponseBody
    public ResponseEntity<?> createCounter(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String serviceType = request.get("serviceType");
            Counter counter = counterService.createCounter(name, serviceType);
            queueService.notifyAdminStatsUpdate();
            return ResponseEntity.ok(counter);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/counters/{id}")
    @ResponseBody
    public ResponseEntity<?> updateCounter(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String serviceType = request.get("serviceType");
            Counter counter = counterService.updateCounter(id, name, serviceType);
            return ResponseEntity.ok(counter);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/counters/{id}/deactivate")
    @ResponseBody
    public ResponseEntity<?> deactivateCounter(@PathVariable Long id) {
        try {
            counterService.deactivateCounter(id);
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

    @PostMapping("/counters/{id}/activate")
    @ResponseBody
    public ResponseEntity<?> activateCounter(@PathVariable Long id) {
        try {
            counterService.activateCounter(id);
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

    @GetMapping("/staff")
    public String staffPage(Model model) {
        List<User> staff = userService.findAllStaff();
        List<Counter> counters = counterService.findAll();
        model.addAttribute("staff", staff);
        model.addAttribute("counters", counters);
        return "admin/staff";
    }

    @PostMapping("/staff")
    @ResponseBody
    public ResponseEntity<?> createStaff(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            Long counterId = request.get("counterId") != null ? Long.parseLong(request.get("counterId")) : null;
            User staff = userService.createStaff(username, password, counterId);
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/staff/{id}")
    @ResponseBody
    public ResponseEntity<?> updateStaff(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            Long counterId = request.get("counterId") != null ? Long.parseLong(request.get("counterId")) : null;
            User staff = userService.updateStaff(id, password, counterId);
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/staff/{id}/deactivate")
    @ResponseBody
    public ResponseEntity<?> deactivateStaff(@PathVariable Long id) {
        try {
            userService.deactivateStaff(id);
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

    @PostMapping("/staff/{id}/activate")
    @ResponseBody
    public ResponseEntity<?> activateStaff(@PathVariable Long id) {
        try {
            userService.activateStaff(id);
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

    @GetMapping("/reports")
    public String reportsPage(Model model) {
        StatsResponse stats = queueService.getStats();
        model.addAttribute("stats", stats);
        return "admin/reports";
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(queueService.getStats());
    }
}
