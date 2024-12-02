package com.example.demo.dto;

import lombok.Data;

@Data
public class PagoDTO {
    private Long id;
    private TipoPagoDTO tipoPago;
    private String tarjetaTipo;
    private Integer cuotas;
    private Double valor;
}
