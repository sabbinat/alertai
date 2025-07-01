package com.sbact1.repository;

import com.sbact1.model.Report;
import com.sbact1.model.ReportReason;
import com.sbact1.model.Event;
import com.sbact1.model.EventStatus;
import com.sbact1.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report,Long> {
    boolean existsByEventAndUser(Event event, User user);
    List<Report> findByReviewedFalse();
    List<Report> findByReviewedFalseAndReason(ReportReason reason);
    int countByEventId(Long id);
    List<Report> findByReviewedFalseAndEventStatus(EventStatus status);
    List<Report> findByReviewedFalseAndReasonAndEventStatus(ReportReason reason, EventStatus status);
    List<Report> findByEventId(Long id);


    @Query("SELECT r FROM Report r WHERE r.event.user.id = :userId AND r.viewed = false")
    List<Report> findByEventOwnerIdAndVistoFalse(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Report r SET r.viewed = true WHERE r.event.user.id = :userId AND r.viewed = false")
    void marcarReportesComoVistos(@Param("userId") Long userId);

}
