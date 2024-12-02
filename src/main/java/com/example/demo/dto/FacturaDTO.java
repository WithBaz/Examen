package com.example.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FacturaDTO {
    
    private String clienteDocumento;
    private String clienteNombre;
    private String clienteTipoDocumento;
    private String vendedorDocumento;
    private String cajeroToken;
    private double impuesto;
    private List<ProductoFactura> productos;
    private List<MedioPago> mediosPago;

    @Data
    @AllArgsConstructor
    public static class ProductoFactura {
        private String referencia;
        private int cantidad;
        private double descuento;
    }

    @Data
    @AllArgsConstructor
    public static class MedioPago {
        private double valor;
        private int cuotas;
        private String tipoTarjeta;
    }

    @Data
    @AllArgsConstructor
    public static class ResponseData {
        private Long numero;
        private double total;
        private String fecha;
    }
}
