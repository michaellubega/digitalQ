package com.digitalq.config;

import com.digitalq.model.Counter;
import com.digitalq.model.User;
import com.digitalq.repository.CounterRepository;
import com.digitalq.repository.UserRepository;
import com.digitalq.repository.StaffAssignmentRepository;
import com.digitalq.model.StaffAssignment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CounterRepository counterRepository;
    private final StaffAssignmentRepository staffAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${digitalq.admin.username:admin}")
    private String adminUsername;

    @Value("${digitalq.admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        // Create default admin if not exists
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(User.Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            System.out.println("Default admin created: " + adminUsername);
        }

        // Create default staff if not exists
        if (!userRepository.existsByUsername("staff1")) {
            User staff = User.builder()
                    .username("staff1")
                    .password(passwordEncoder.encode("staff123"))
                    .role(User.Role.STAFF)
                    .active(true)
                    .build();
            User savedStaff = userRepository.save(staff);

            // Create default counter if not exists
            if (!counterRepository.existsByName("Counter 1")) {
                Counter counter = Counter.builder()
                        .name("Counter 1")
                        .serviceType("General")
                        .active(true)
                        .paused(false)
                        .build();
                Counter savedCounter = counterRepository.save(counter);

                // Assign staff to counter
                StaffAssignment assignment = StaffAssignment.builder()
                        .userId(savedStaff.getId())
                        .counterId(savedCounter.getId())
                        .build();
                staffAssignmentRepository.save(assignment);
            }

            System.out.println("Default staff created: staff1");
        }

        // Create additional sample counters
        if (!counterRepository.existsByName("Counter 2")) {
            counterRepository.save(Counter.builder()
                    .name("Counter 2")
                    .serviceType("Billing")
                    .active(true)
                    .paused(false)
                    .build());
        }

        if (!counterRepository.existsByName("Counter 3")) {
            counterRepository.save(Counter.builder()
                    .name("Counter 3")
                    .serviceType("Support")
                    .active(true)
                    .paused(false)
                    .build());
        }
    }
}
