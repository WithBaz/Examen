package com.example.demo.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Tienda;

@Repository
public interface TiendaRepository extends JpaRepository<Tienda, String> {

}
