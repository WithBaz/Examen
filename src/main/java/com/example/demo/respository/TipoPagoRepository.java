package com.example.demo.respository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Tipo_Pago;

@Repository
public interface TipoPagoRepository extends JpaRepository<Tipo_Pago, Long> {
	Optional<Tipo_Pago> findByNombre(String nombre);}
