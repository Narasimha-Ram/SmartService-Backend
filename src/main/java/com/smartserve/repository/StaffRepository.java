package com.smartserve.repository;

import com.smartserve.entity.Staff;
import com.smartserve.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByDepartmentAndIsActiveTrue(Department department);
    List<Staff> findByIsActiveTrueOrderByUserName();
    boolean existsByUserEmail(String email);
}
