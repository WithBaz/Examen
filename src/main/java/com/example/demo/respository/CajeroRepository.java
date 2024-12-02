package com.example.demo.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Cajero;

import java.util.Optional;

@Repository
public interface CajeroRepository extends JpaRepository<Cajero, Long> {
    Optional<Cajero> findByToken(String token);
}