package com.sbact1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbact1.model.Category;
import com.sbact1.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
	long count();
    List<User> findByNotificacionesContaining(Category category);
    Optional<User> findByNameIgnoreCase(String query);
    List<User> findByNameContainingIgnoreCase(String query);
    List<User> findTop5ByOrderByRegistrationTimeDesc();
    List<User> findByRole(String rolFiltro);
    Page<User> findByRole(String role, Pageable pageable);
    boolean existsByEmail(String email);
    boolean existsByUsername(String candidate);

}
