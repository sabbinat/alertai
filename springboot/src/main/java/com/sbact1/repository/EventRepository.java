package com.sbact1.repository;

import com.sbact1.model.Event;
import com.sbact1.model.User;

import java.util.List;
import java.util.Optional;

import com.sbact1.model.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findEventByNameLike(String name);
    List<Event> findByCategoryId(Long categoryId);
    List<Event> findByUser(User user);
    List<Event> findAllByOrderByStartDateDesc();
    List<Event> findByCategoryOrderByStartDateDesc(Category category);
    List<Event> findByUserAndCategoryOrderByStartDateDesc(User user, Category category);
    Page<Event> findEventByNameLike(String name, Pageable pageable);
    List<Event> findByNameContainingIgnoreCase(String query);
    Optional<Event> findByNameIgnoreCase(String query);
    List<Event> findByLocationIsNotNull();


    Page<Event> findByNameContainingIgnoreCaseAndCategoryId(String name, Long categoryId, Pageable pageable);
    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Event> findByCategoryId(Long categoryId, Pageable pageable);
}

