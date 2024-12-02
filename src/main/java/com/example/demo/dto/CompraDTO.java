package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraDTO {
    private Long id;
    private ClienteDTO cliente;
    private VendedorDTO vendedor;
    private CajeroDTO cajero;
    private Double total;
    private Double impuestos;
    private LocalDateTime fecha;
    private String observaciones;
    private List<DetallesCompraDTO> detallesCompra;
}