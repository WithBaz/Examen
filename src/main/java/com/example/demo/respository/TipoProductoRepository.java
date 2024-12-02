package com.example.demo.respository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Tipo_Producto;

@Repository
public interface TipoProductoRepository extends JpaRepository<Tipo_Producto, Long> {
    Tipo_Producto findByNombre(String nombre);
}
