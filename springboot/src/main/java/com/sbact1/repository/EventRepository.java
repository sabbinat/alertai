package com.sbact1.repository;

import com.sbact1.model.Event;
import com.sbact1.model.User;

import java.util.List;
import java.util.Optional;

import com.sbact1.model.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    long countByCategoryId(Long categoryId);



    Page<Event> findByNameContainingIgnoreCaseAndCategoryId(String name, Long categoryId, Pageable pageable);
    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Event> findByCategoryId(Long categoryId, Pageable pageable);
    void deleteByUserId(Integer userId);
    @Query("SELECT e FROM Event e WHERE FUNCTION('MONTH', e.startDate) = :mes")
    Page<Event> findByMonth(@Param("mes") Integer mes, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')) AND FUNCTION('MONTH', e.startDate) = :mes")
    Page<Event> findByNameContainingIgnoreCaseAndMonth(@Param("name") String name, @Param("mes") Integer mes, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.category.id = :categoriaId AND FUNCTION('MONTH', e.startDate) = :mes")
    Page<Event> findByCategoryIdAndMonth(@Param("categoriaId") Long categoriaId, @Param("mes") Integer mes, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')) AND e.category.id = :categoriaId AND FUNCTION('MONTH', e.startDate) = :mes")
    Page<Event> findByNameContainingIgnoreCaseAndCategoryIdAndMonth(@Param("name") String name,
                                                                    @Param("categoriaId") Long categoriaId,
                                                                    @Param("mes") Integer mes,
                                                                    Pageable pageable);


    }

