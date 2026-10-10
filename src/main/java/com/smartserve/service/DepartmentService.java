package com.smartserve.service;

import com.smartserve.entity.*;
import com.smartserve.exception.*;
import com.smartserve.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentService {

    @Autowired private DepartmentRepository departmentRepository;

    public List<Department> getAllActive() {
        return departmentRepository.findByIsActiveTrueOrderByName();
    }

    public List<Department> getAll() {
        return departmentRepository.findAll();
    }

    public Department create(String name, String description, String headName) {
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("A department named '" + name + "' already exists.");
        }
        Department dept = new Department();
        dept.setName(name);
        dept.setDescription(description);
        dept.setHeadName(headName);
        dept.setIsActive(true);
        return departmentRepository.save(dept);
    }

    public Department update(Long id, String name, String description, String headName, Boolean isActive) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        dept.setName(name);
        dept.setDescription(description);
        dept.setHeadName(headName);
        if (isActive != null) dept.setIsActive(isActive);
        return departmentRepository.save(dept);
    }

    public void delete(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        dept.setIsActive(false);
        departmentRepository.save(dept);
    }
}
