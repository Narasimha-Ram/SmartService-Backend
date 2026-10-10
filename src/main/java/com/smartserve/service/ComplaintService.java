package com.smartserve.service;

import com.smartserve.dto.request.*;
import com.smartserve.dto.response.*;
import com.smartserve.entity.*;
import com.smartserve.exception.*;
import com.smartserve.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ComplaintService {

    @Autowired private ComplaintRepository complaintRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private ComplaintStatusHistoryRepository statusHistoryRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private IntelligentService intelligentService;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private UserService userService;

    private static final Map<ComplaintStatus, Set<ComplaintStatus>> VALID_TRANSITIONS = new HashMap<>();
    static {
        VALID_TRANSITIONS.put(ComplaintStatus.PENDING,     Set.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS));
        VALID_TRANSITIONS.put(ComplaintStatus.ASSIGNED,    Set.of(ComplaintStatus.IN_PROGRESS));
        VALID_TRANSITIONS.put(ComplaintStatus.IN_PROGRESS, Set.of(ComplaintStatus.RESOLVED));
        VALID_TRANSITIONS.put(ComplaintStatus.RESOLVED,    Set.of(ComplaintStatus.CLOSED));
        VALID_TRANSITIONS.put(ComplaintStatus.CLOSED,      Set.of());
    }

    public ComplaintDetailResponse createComplaint(ComplaintRequest request, User user, MultipartFile file) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));

        String complaintNumber = generateComplaintNumber();

        Complaint complaint = new Complaint();
        complaint.setComplaintNumber(complaintNumber);
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setCategory(category);
        complaint.setLocation(request.getLocation());
        complaint.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        complaint.setStatus(ComplaintStatus.PENDING);
        complaint.setUser(user);
        complaint = complaintRepository.save(complaint);

        // Status history entry
        recordStatusChange(complaint, null, ComplaintStatus.PENDING, user, "Complaint submitted");

        // Handle attachment
        if (file != null && !file.isEmpty()) {
            try {
                String storedName = fileStorageService.storeFile(file);
                Attachment attachment = new Attachment();
                attachment.setComplaint(complaint);
                attachment.setFileName(file.getOriginalFilename());
                attachment.setFilePath(storedName);
                attachment.setFileType(file.getContentType());
                attachment.setFileSize(file.getSize());
                complaint.getAttachments().add(attachment);
                complaint = complaintRepository.save(complaint);
            } catch (Exception e) {
                // Attachment failure is non-critical
            }
        }

        // Notify admin users (simplified: find admin by role)
        // For now just saving the complaint - admin notification handled via polling

        return toDetailResponse(complaint, true, user);
    }

    public Page<ComplaintSummaryResponse> getMyComplaints(User user, int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Complaint> complaints = complaintRepository.findByUser(user, pageable);
        return complaints.map(this::toSummaryResponse);
    }

    public ComplaintDetailResponse getComplaintById(Long id, User currentUser) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint #" + id + " not found."));

        // Users can only view their own complaints; admins can view all
        if (!currentUser.getRole().getName().equals("ADMIN") &&
            !complaint.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this complaint.");
        }

        boolean isAdmin = currentUser.getRole().getName().equals("ADMIN");
        return toDetailResponse(complaint, isAdmin, currentUser);
    }

    public ComplaintDetailResponse submitFeedback(Long complaintId, FeedbackRequest request, User user) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found."));

        if (!complaint.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only provide feedback for your own complaints.");
        }
        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new BadRequestException("Feedback can only be submitted for RESOLVED complaints.");
        }
        if (complaint.getFeedback() != null) {
            throw new BadRequestException("Feedback has already been submitted for this complaint.");
        }

        Feedback feedback = new Feedback();
        feedback.setComplaint(complaint);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        complaint.setFeedback(feedback);

        // Auto-close complaint after feedback
        complaint.setStatus(ComplaintStatus.CLOSED);
        complaint.setClosedAt(LocalDateTime.now());
        complaint = complaintRepository.save(complaint);

        recordStatusChange(complaint, ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED, user, "User confirmed resolution and submitted feedback.");
        return toDetailResponse(complaint, false, user);
    }

    public ComplaintDetailResponse addComment(Long complaintId, CommentRequest request, User user) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found."));

        // Internal comments only for admin/staff
        boolean isAdmin = user.getRole().getName().equals("ADMIN") || user.getRole().getName().equals("STAFF");
        if (request.getIsInternal() && !isAdmin) {
            throw new UnauthorizedException("Only admins can add internal notes.");
        }

        ComplaintComment comment = new ComplaintComment();
        comment.setComplaint(complaint);
        comment.setAuthor(user);
        comment.setMessage(request.getMessage());
        comment.setIsInternal(request.getIsInternal() != null ? request.getIsInternal() : false);
        complaint.getComments().add(comment);
        complaintRepository.save(complaint);

        return toDetailResponse(complaint, isAdmin, user);
    }

    // ------- Admin actions -------

    public Page<ComplaintSummaryResponse> getAllComplaints(ComplaintStatus status, Priority priority,
                                                           Long categoryId, String search, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Complaint> complaints = complaintRepository.findWithFilters(status, priority, categoryId, search, pageable);
        return complaints.map(this::toSummaryResponse);
    }

    public ComplaintDetailResponse adminGetComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found."));
        return toDetailResponse(complaint, true, null);
    }

    public ComplaintDetailResponse assignComplaint(Long complaintId, AssignRequest request, User admin) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found."));

        if (complaint.getStatus() == ComplaintStatus.CLOSED || complaint.getStatus() == ComplaintStatus.RESOLVED) {
            throw new BadRequestException("Cannot assign a complaint that is already " + complaint.getStatus().name().toLowerCase() + ".");
        }
        return toDetailResponse(complaint, true, admin);
    }

    public ComplaintDetailResponse updateStatus(Long complaintId, StatusUpdateRequest request, User admin) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found."));

        ComplaintStatus current = complaint.getStatus();
        ComplaintStatus next = request.getStatus();

        Set<ComplaintStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new BadRequestException("Invalid status transition from " + current + " to " + next +
                    ". Allowed next statuses: " + allowed);
        }

        complaint.setStatus(next);
        if (next == ComplaintStatus.RESOLVED) complaint.setResolvedAt(LocalDateTime.now());
        if (next == ComplaintStatus.CLOSED) complaint.setClosedAt(LocalDateTime.now());

        complaintRepository.save(complaint);
        recordStatusChange(complaint, current, next, admin, request.getNotes());

        // Notify the complaint owner
        String notifMessage = "Your complaint " + complaint.getComplaintNumber() + " status has been updated to: " + next.name();
        notificationService.createNotification(complaint.getUser(), "Complaint Status Updated", notifMessage, "STATUS_UPDATE", complaint);

        return toDetailResponse(complaint, true, admin);
    }

    public List<ComplaintSummaryResponse> checkDuplicates(ComplaintRequest request, User user) {
        List<ComplaintSummaryResponse> result = new ArrayList<>();
        long excludeId = -1L;

        List<Complaint> candidates = complaintRepository.findPotentialDuplicates(
                request.getCategoryId(),
                request.getLocation(),
                LocalDateTime.now().minusDays(7),
                excludeId
        );

        for (Complaint c : candidates) {
            double similarity = intelligentService.textSimilarity(request.getDescription(), c.getDescription());
            if (similarity >= 0.3) { // 30% word overlap threshold
                result.add(toSummaryResponse(c));
            }
        }
        return result;
    }

    // ------- Helpers -------

    private void recordStatusChange(Complaint complaint, ComplaintStatus oldStatus, ComplaintStatus newStatus, User changedBy, String notes) {
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setNotes(notes);
        statusHistoryRepository.save(history);
    }

    private String generateComplaintNumber() {
        long count = complaintRepository.count() + 1;
        return String.format("CMP-%06d", count);
    }

    public ComplaintSummaryResponse toSummaryResponse(Complaint complaint) {
        ComplaintSummaryResponse response = new ComplaintSummaryResponse();
        response.setId(complaint.getId());
        response.setComplaintNumber(complaint.getComplaintNumber());
        response.setTitle(complaint.getTitle());
        response.setCategoryName(complaint.getCategory() != null ? complaint.getCategory().getName() : "");
        response.setLocation(complaint.getLocation());
        response.setPriority(complaint.getPriority());
        response.setStatus(complaint.getStatus());
        response.setUserName(complaint.getUser() != null ? complaint.getUser().getName() : "");
        response.setCreatedAt(complaint.getCreatedAt());
        response.setUpdatedAt(complaint.getUpdatedAt());
        return response;
    }

    public ComplaintDetailResponse toDetailResponse(Complaint complaint, boolean includeInternal, User currentUser) {
        ComplaintDetailResponse response = new ComplaintDetailResponse();
        response.setId(complaint.getId());
        response.setComplaintNumber(complaint.getComplaintNumber());
        response.setTitle(complaint.getTitle());
        response.setDescription(complaint.getDescription());
        response.setLocation(complaint.getLocation());
        response.setPriority(complaint.getPriority());
        response.setStatus(complaint.getStatus());
        response.setCreatedAt(complaint.getCreatedAt());
        response.setUpdatedAt(complaint.getUpdatedAt());
        response.setResolvedAt(complaint.getResolvedAt());
        response.setClosedAt(complaint.getClosedAt());

        if (complaint.getCategory() != null) {
            response.setCategoryName(complaint.getCategory().getName());
            response.setCategoryId(complaint.getCategory().getId());
        }
        if (complaint.getUser() != null) {
            response.setUserId(complaint.getUser().getId());
            response.setUserName(complaint.getUser().getName());
            response.setUserEmail(complaint.getUser().getEmail());
        }

        // Latest assignment
        assignmentRepository.findTopByComplaintOrderByAssignedAtDesc(complaint).ifPresent(a -> {
            if (a.getDepartment() != null) response.setDepartmentName(a.getDepartment().getName());
            if (a.getStaff() != null && a.getStaff().getUser() != null) response.setStaffName(a.getStaff().getUser().getName());
            response.setAssignmentNotes(a.getNotes());
            response.setAssignedAt(a.getAssignedAt());
        });

        // Status history
        List<ComplaintStatusHistory> history = statusHistoryRepository.findByComplaintOrderByChangedAtAsc(complaint);
        response.setStatusHistory(history.stream().map(h -> {
            StatusHistoryResponse r = new StatusHistoryResponse();
            r.setOldStatus(h.getOldStatus());
            r.setNewStatus(h.getNewStatus());
            r.setChangedByName(h.getChangedBy() != null ? h.getChangedBy().getName() : "System");
            r.setChangedAt(h.getChangedAt());
            r.setNotes(h.getNotes());
            return r;
        }).collect(Collectors.toList()));

        // Comments (filter internal if not admin)
        List<ComplaintComment> comments = complaint.getComments().stream()
                .filter(c -> includeInternal || !c.getIsInternal())
                .collect(Collectors.toList());
        response.setComments(comments.stream().map(c -> {
            CommentResponse r = new CommentResponse();
            r.setId(c.getId());
            r.setMessage(c.getMessage());
            r.setIsInternal(c.getIsInternal());
            r.setCreatedAt(c.getCreatedAt());
            if (c.getAuthor() != null) {
                r.setAuthorName(c.getAuthor().getName());
                r.setAuthorRole(c.getAuthor().getRole().getName());
            }
            return r;
        }).collect(Collectors.toList()));

        // Attachments
        response.setAttachments(complaint.getAttachments().stream().map(a -> {
            AttachmentResponse r = new AttachmentResponse();
            r.setId(a.getId());
            r.setFileName(a.getFileName());
            r.setFileType(a.getFileType());
            r.setFileSize(a.getFileSize());
            r.setDownloadUrl("/api/files/" + a.getFilePath());
            r.setUploadedAt(a.getUploadedAt());
            return r;
        }).collect(Collectors.toList()));

        // Feedback
        if (complaint.getFeedback() != null) {
            FeedbackResponse fr = new FeedbackResponse();
            fr.setRating(complaint.getFeedback().getRating());
            fr.setComment(complaint.getFeedback().getComment());
            fr.setSubmittedAt(complaint.getFeedback().getSubmittedAt());
            response.setFeedback(fr);
        }

        // Intelligent features (always show recommendation for new/pending complaints)
        if (complaint.getCategory() != null) {
            String rec = intelligentService.recommendPriority(
                    complaint.getTitle(), complaint.getDescription(), complaint.getCategory().getName());
            response.setRecommendedPriority(rec);
            response.setEstimatedResolutionTime(
                    intelligentService.estimateResolutionTime(complaint.getCategory().getId(), complaint.getPriority()));
        }

        return response;
    }
}
