package com.smartserve.repository;

import com.smartserve.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByIsActiveTrueOrderByName();
    boolean existsByNameIgnoreCase(String name);
}
