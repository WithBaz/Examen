package com.example.demo.dto;
import lombok.Data;

@Data
public class DetallesCompraDTO {
    private Long id;
    private ProductoDTO producto;
    private Integer cantidad;
    private Double precio;
    private Double descuento;
}