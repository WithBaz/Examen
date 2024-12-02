package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class FacturaDTO {
    private Double impuesto;
    private Cliente cliente;
    private List<Producto> productos;
    private List<MedioPago> medios_pago;
    private Vendedor vendedor;
    private Cajero cajero;

    @Data
    public static class Cliente {
        private String documento;
        private String nombre;
        private String tipo_documento;
    }

    @Data
    public static class Producto {
        private String referencia;
        private Integer cantidad;
        private Double descuento;
    }

    @Data
    public static class MedioPago {
        private String tipo_pago;
        private String tipo_tarjeta;
        private Integer cuotas;
        private Double valor;
    }

    @Data
    public static class Vendedor {
        private String documento;
    }

    @Data
    public static class Cajero {
        private String token;
    }
}