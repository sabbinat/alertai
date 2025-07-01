package com.sbact1.repository;

import com.sbact1.model.Comment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventId(Long eventId);
    
    @Query("SELECT c FROM Comment c WHERE c.event.user.id = :userId AND c.viewed = false")
    List<Comment> findByEventOwnerIdAndVistoFalse(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Comment c SET c.viewed = true WHERE c.event.user.id = :userId AND c.viewed = false")
    void marcarComentariosComoVistos(@Param("userId") Long userId);

}

