package com.example.demo.dto;
import lombok.Data;

@Data
public class CajeroDTO {
    private Long id;
    private String nombre;
    private String documento;
    private String email;
    private TiendaDTO tienda;
}