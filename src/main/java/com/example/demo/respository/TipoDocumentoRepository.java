package com.example.demo.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Tipo_Documento;


@Repository
public interface TipoDocumentoRepository extends JpaRepository<Tipo_Documento, Long> {
    Tipo_Documento findByNombre(String nombre);
}
