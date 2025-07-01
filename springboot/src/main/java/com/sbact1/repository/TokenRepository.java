package com.sbact1.repository;

import com.sbact1.model.Token;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);
    void deleteByUserId(Integer userId);

}

