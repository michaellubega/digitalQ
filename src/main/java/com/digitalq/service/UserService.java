package com.digitalq.service;

import com.digitalq.model.User;
import com.digitalq.repository.UserRepository;
import com.digitalq.repository.StaffAssignmentRepository;
import com.digitalq.repository.CounterRepository;
import com.digitalq.model.Counter;
import com.digitalq.model.StaffAssignment;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StaffAssignmentRepository staffAssignmentRepository;
    private final CounterRepository counterRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAllStaff() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.STAFF)
                .toList();
    }

    @Transactional
    public User createStaff(String username, String password, Long counterId) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        User staff = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(User.Role.STAFF)
                .active(true)
                .build();

        User saved = userRepository.save(staff);

        if (counterId != null) {
            StaffAssignment assignment = StaffAssignment.builder()
                    .userId(saved.getId())
                    .counterId(counterId)
                    .build();
            staffAssignmentRepository.save(assignment);
        }

        return saved;
    }

    @Transactional
    public User updateStaff(Long id, String password, Long counterId) {
        User staff = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + id));

        if (password != null && !password.isBlank()) {
            staff.setPassword(passwordEncoder.encode(password));
        }

        User saved = userRepository.save(staff);

        if (counterId != null) {
            staffAssignmentRepository.deleteByUserId(id);
            StaffAssignment assignment = StaffAssignment.builder()
                    .userId(id)
                    .counterId(counterId)
                    .build();
            staffAssignmentRepository.save(assignment);
        }

        return saved;
    }

    @Transactional
    public void deactivateStaff(Long id) {
        User staff = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + id));
        staff.setActive(false);
        userRepository.save(staff);
    }

    @Transactional
    public void activateStaff(Long id) {
        User staff = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + id));
        staff.setActive(true);
        userRepository.save(staff);
    }

    public Optional<Counter> getStaffCounter(Long userId) {
        return staffAssignmentRepository.findByUserId(userId)
                .flatMap(assignment -> counterRepository.findById(assignment.getCounterId()));
    }

    public List<Counter> getAllCounters() {
        return counterRepository.findAll();
    }
}
