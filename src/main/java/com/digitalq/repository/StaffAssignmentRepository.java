package com.digitalq.repository;

import com.digitalq.model.StaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {
    Optional<StaffAssignment> findByUserId(Long userId);
    List<StaffAssignment> findByCounterId(Long counterId);
    void deleteByUserId(Long userId);
}
