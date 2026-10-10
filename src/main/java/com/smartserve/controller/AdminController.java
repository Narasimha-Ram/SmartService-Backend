package com.smartserve.controller;

import com.smartserve.dto.request.*;
import com.smartserve.dto.response.*;
import com.smartserve.entity.*;
import com.smartserve.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private ComplaintService complaintService;
    @Autowired private UserService userService;
    @Autowired private CategoryService categoryService;
    @Autowired private DepartmentService departmentService;
    @Autowired private StaffService staffService;

    // --- Complaints ---

    @GetMapping("/complaints")
    public ResponseEntity<ApiResponse<Page<ComplaintSummaryResponse>>> getAllComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.getAllComplaints(status, priority, categoryId, search, page, size)));
    }

    @GetMapping("/complaints/{id}")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> getComplaint(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.adminGetComplaintById(id)));
    }

    @PutMapping("/complaints/{id}/assign")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> assignComplaint(
            @PathVariable Long id,
            @Valid @RequestBody AssignRequest request,
            Principal principal) {
        User admin = userService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Complaint assigned.", complaintService.assignComplaint(id, request, admin)));
    }

    @PutMapping("/complaints/{id}/status")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            Principal principal) {
        User admin = userService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Status updated.", complaintService.updateStatus(id, request, admin)));
    }

    @PostMapping("/complaints/{id}/comments")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> addInternalComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            Principal principal) {
        User admin = userService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Comment added.", complaintService.addComment(id, request, admin)));
    }

    // --- Categories ---

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAll()));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Map<String, String> body) {
        Category cat = categoryService.create(body.get("name"), body.get("description"));
        return ResponseEntity.ok(ApiResponse.success("Category created.", cat));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Boolean isActive = body.get("isActive") != null ? (Boolean) body.get("isActive") : null;
        Category cat = categoryService.update(id, (String) body.get("name"), (String) body.get("description"), isActive);
        return ResponseEntity.ok(ApiResponse.success("Category updated.", cat));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Category deactivated.", null));
    }

    // --- Departments ---

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getAll()));
    }

    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<Department>> createDepartment(@RequestBody Map<String, String> body) {
        Department dept = departmentService.create(body.get("name"), body.get("description"), body.get("headName"));
        return ResponseEntity.ok(ApiResponse.success("Department created.", dept));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Boolean isActive = body.get("isActive") != null ? (Boolean) body.get("isActive") : null;
        Department dept = departmentService.update(id, (String) body.get("name"), (String) body.get("description"), (String) body.get("headName"), isActive);
        return ResponseEntity.ok(ApiResponse.success("Department updated.", dept));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Department deactivated.", null));
    }

    // --- Staff ---

    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getAllStaff() {
        return ResponseEntity.ok(ApiResponse.success(staffService.getAll()));
    }

    @GetMapping("/staff/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getStaffByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success(staffService.getByDepartment(departmentId)));
    }
}
