package com.sbact1.repository;

import com.sbact1.model.Event;
import com.sbact1.model.EventStatus;
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
    List<Event> findByUser(User user);
    List<Event> findAllByOrderByStartDateDesc();
    List<Event> findByNameContainingIgnoreCase(String query);
    List<Event> findByLocationIsNotNull();
    
    Optional<Event> findByNameIgnoreCase(String query);
    long countByCategoryId(Long categoryId);
    void deleteByUserId(Integer userId);
    List<Event> findByCategoryId(Long id);


    List<Event> findByUserAndStatus(User user, EventStatus status);
    List<Event> findByCategoryAndStatusOrderByStartDateDesc(Category category, EventStatus status);
    List<Event> findByNameContainingIgnoreCaseAndStatus(String name, EventStatus status);
    List<Event> findByCategoryInAndUserNotOrderByRegistrationTimeDesc(List<Category> categories, User user);


    //Filtran los eventos por estado ACTIVO y DENUNCIADO
    List<Event> findByCategoryAndStatusInOrderByStartDateDesc(Category category, List<EventStatus> statuses);
    List<Event> findByUserNotAndStatusIn(User user, List<EventStatus> statuses);
    List<Event> findByUserAndStatusIn(User user, List<EventStatus> statuses);
    List<Event> findByNameContainingIgnoreCaseAndStatusIn(String name, List<EventStatus> statuses);


    Page<Event> findByNameContainingIgnoreCaseAndCategoryIdAndStatusIn(String name, Long categoryId, List<EventStatus> statuses, Pageable pageable);
    Page<Event> findByStatusIn(List<EventStatus> statuses, Pageable pageable);
    Page<Event> findByNameContainingIgnoreCaseAndCategoryId(String name, Long categoryId, Pageable pageable);
    Page<Event> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);


    
    @Query("SELECT e FROM Event e WHERE (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) "
     + "AND (:categoriaId IS NULL OR e.category.id = :categoriaId) "
     + "AND (:mes IS NULL OR FUNCTION('MONTH', e.startDate) = :mes) "
     + "AND (e.status = 'ACTIVO' OR e.status = 'DENUNCIADO')")
    Page<Event> buscarEventosFiltrados(@Param("name") String name,
                                   @Param("categoriaId") Long categoriaId,
                                   @Param("mes") Integer mes,
                                   Pageable pageable);
    Page<Event> findByCategoryIdAndStatusIn(Long categoriaId, List<EventStatus> estadosFiltro, Pageable pageable);
    List<Event> findByUserAndCategoryOrderByStartDateDesc(User user, Category cat);

    @Query("SELECT e.user.id, COUNT(e) FROM Event e GROUP BY e.user.id")
    List<Object[]> countEventsByUser();

}

