package com.example.demo.dto;


import lombok.Data;
import java.util.List;

@Data
public class FacturaDTO {
    private Double impuesto;
    private String clienteDocumento;
    private String clienteNombre;
    private String clienteTipoDocumento;
    private List<ProductoFactura> productos;
    private List<MedioPago> mediosPago;
    private String vendedorDocumento;
    private String cajeroToken;

    @Data
    public static class ProductoFactura {
        private String referencia;
        private Integer cantidad;
        private Double descuento;
    }

    @Data
    public static class MedioPago {
        private String tipoPago;
        private String tipoTarjeta;
        private Integer cuotas;
        private Double valor;
    }
}