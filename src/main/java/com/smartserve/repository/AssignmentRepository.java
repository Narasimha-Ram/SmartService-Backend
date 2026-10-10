package com.smartserve.repository;

import com.smartserve.entity.Assignment;
import com.smartserve.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Optional<Assignment> findTopByComplaintOrderByAssignedAtDesc(Complaint complaint);
}
