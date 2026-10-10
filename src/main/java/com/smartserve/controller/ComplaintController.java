package com.smartserve.controller;

import com.smartserve.dto.request.*;
import com.smartserve.dto.response.*;
import com.smartserve.entity.User;
import com.smartserve.service.ComplaintService;
import com.smartserve.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Autowired private ComplaintService complaintService;
    @Autowired private UserService userService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> createComplaint(
            @RequestPart("complaint") @Valid ComplaintRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        ComplaintDetailResponse response = complaintService.createComplaint(request, user, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Complaint submitted successfully.", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ComplaintSummaryResponse>>> getMyComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        Page<ComplaintSummaryResponse> complaints = complaintService.getMyComplaints(user, page, size, "createdAt");
        return ResponseEntity.ok(ApiResponse.success(complaints));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> getComplaint(@PathVariable Long id, Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        ComplaintDetailResponse response = complaintService.getComplaintById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> submitFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequest request,
            Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Feedback submitted.", complaintService.submitFeedback(id, request, user)));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Comment added.", complaintService.addComment(id, request, user)));
    }

    @PostMapping("/check-duplicates")
    public ResponseEntity<ApiResponse<List<ComplaintSummaryResponse>>> checkDuplicates(
            @Valid @RequestBody ComplaintRequest request,
            Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(complaintService.checkDuplicates(request, user)));
    }
}
