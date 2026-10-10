package com.smartserve.repository;

import com.smartserve.entity.ComplaintStatusHistory;
import com.smartserve.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory, Long> {
    List<ComplaintStatusHistory> findByComplaintOrderByChangedAtAsc(Complaint complaint);
}
