package com.example.demo.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Pago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
}