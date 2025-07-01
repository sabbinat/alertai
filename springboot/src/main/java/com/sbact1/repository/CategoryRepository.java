package com.sbact1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sbact1.model.Category;
import com.sbact1.model.User;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{
    List<Category> findByNameContainingIgnoreCase(String query);
    Optional<Category> findByName(String name);

    @Query("SELECT DISTINCT c FROM Event e JOIN e.category c WHERE e.user = :user")
    List<Category> findCategoriesWithEventsByUser(@Param("user") User user);
}

