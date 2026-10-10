package com.smartserve.service;

import com.smartserve.dto.response.StaffResponse;
import com.smartserve.entity.*;
import com.smartserve.exception.*;
import com.smartserve.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StaffService {

    @Autowired private StaffRepository staffRepository;
    @Autowired private DepartmentRepository departmentRepository;

    public List<StaffResponse> getAll() {
        return staffRepository.findByIsActiveTrueOrderByUserName()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<StaffResponse> getByDepartment(Long departmentId) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        return staffRepository.findByDepartmentAndIsActiveTrue(dept)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public StaffResponse toResponse(Staff staff) {
        StaffResponse response = new StaffResponse();
        response.setId(staff.getId());
        response.setUserId(staff.getUser().getId());
        response.setUserName(staff.getUser().getName());
        response.setUserEmail(staff.getUser().getEmail());
        response.setDepartmentName(staff.getDepartment().getName());
        response.setDepartmentId(staff.getDepartment().getId());
        response.setDesignation(staff.getDesignation());
        response.setIsActive(staff.getIsActive());
        response.setCreatedAt(staff.getCreatedAt());
        return response;
    }
}
