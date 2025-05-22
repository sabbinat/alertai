package com.sbact1.repository;

import com.sbact1.model.Report;
import com.sbact1.model.ReportReason;
import com.sbact1.model.Event;
import com.sbact1.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report,Long> {
    boolean existsByEventAndUser(Event event, User user);
    List<Report> findByReviewedFalse();
    List<Report> findByReviewedFalseAndReason(ReportReason reason);
}
