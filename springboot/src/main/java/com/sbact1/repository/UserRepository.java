package com.sbact1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbact1.model.Category;
import com.sbact1.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository extends JpaRepository<User, Integer> {
	public User findByEmail(String emaill);
	long count();
    public List<User> findByNotificacionesContaining(Category category);
    public Optional<User> findByNameIgnoreCase(String query);
    public List<User> findByNameContainingIgnoreCase(String query);
    public List<User> findTop5ByOrderByRegistrationTimeDesc();
    public List<User> findByRole(String rolFiltro);

    Page<User> findByRole(String role, Pageable pageable);

}
