package com.smartserve.repository;

import com.smartserve.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Page<Complaint> findByUser(User user, Pageable pageable);

    List<Complaint> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:categoryId IS NULL OR c.category.id = :categoryId) AND " +
           "(:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.complaintNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.location) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Complaint> findWithFilters(
        @Param("status") ComplaintStatus status,
        @Param("priority") Priority priority,
        @Param("categoryId") Long categoryId,
        @Param("search") String search,
        Pageable pageable
    );

    long countByStatus(ComplaintStatus status);

    long countByPriority(Priority priority);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS')")
    long countActive();

    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT c.category.name, COUNT(c) FROM Complaint c GROUP BY c.category.name ORDER BY COUNT(c) DESC")
    List<Object[]> countByCategoryGrouped();

    @Query("SELECT c.priority, COUNT(c) FROM Complaint c GROUP BY c.priority")
    List<Object[]> countByPriorityGrouped();

    @Query("SELECT YEAR(c.createdAt), MONTH(c.createdAt), COUNT(c) FROM Complaint c " +
           "WHERE c.createdAt >= :since GROUP BY YEAR(c.createdAt), MONTH(c.createdAt) ORDER BY YEAR(c.createdAt), MONTH(c.createdAt)")
    List<Object[]> countByMonth(@Param("since") LocalDateTime since);

    @Query("SELECT c.location, COUNT(c) FROM Complaint c GROUP BY c.location ORDER BY COUNT(c) DESC")
    List<Object[]> countByLocation();

    @Query("SELECT c FROM Complaint c WHERE c.category.id = :categoryId " +
           "AND c.location = :location AND c.createdAt >= :since AND c.id != :excludeId")
    List<Complaint> findPotentialDuplicates(
        @Param("categoryId") Long categoryId,
        @Param("location") String location,
        @Param("since") LocalDateTime since,
        @Param("excludeId") Long excludeId
    );

    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, c.createdAt, c.resolvedAt)) FROM Complaint c " +
           "WHERE c.resolvedAt IS NOT NULL AND c.category.id = :categoryId")
    Double avgResolutionHoursByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, c.createdAt, c.resolvedAt)) FROM Complaint c " +
           "WHERE c.resolvedAt IS NOT NULL AND c.priority = :priority")
    Double avgResolutionHoursByPriority(@Param("priority") Priority priority);

    @Query("SELECT c.category.name, AVG(TIMESTAMPDIFF(HOUR, c.createdAt, c.resolvedAt)) " +
           "FROM Complaint c WHERE c.resolvedAt IS NOT NULL " +
           "GROUP BY c.category.name ORDER BY AVG(TIMESTAMPDIFF(HOUR, c.createdAt, c.resolvedAt))")
    List<Object[]> avgResolutionTimeByCategory();
}
