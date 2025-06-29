package com.sbact1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sbact1.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{
    List<Category> findCategoryByNameLike(String name);
    <Optional> Category findByName(String name);
    List<Category> findByNameContainingIgnoreCase(String query);
    Optional<Category> findByNameLike(String name);


}

