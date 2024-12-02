package com.example.demo.service;

import com.example.demo.dto.FacturaDTO;
import com.example.demo.dto.Response;
import com.example.demo.entity.*;
import com.example.demo.respository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FacturacionService {

    private final TiendaRepository tiendaRepository;
    private final ClienteRepository clienteRepository;
    private final VendedorRepository vendedorRepository;
    private final CajeroRepository cajeroRepository;
    private final ProductoRepository productoRepository;
    private final CompraRepository compraRepository;
    private final DetallesCompraRepository detallesCompraRepository;
    private final PagoRepository pagoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    @Transactional
    public Response<?> procesarFactura(String tiendaId, FacturaDTO facturaDTO) {
        try {
            // Verificar existencia de la tienda
            Tienda tienda = tiendaRepository.findById(tiendaId)
                    .orElseThrow(() -> new RuntimeException("Tienda no encontrada con ID: " + tiendaId));

            // Obtener o crear cliente
            Cliente cliente = obtenerOCrearCliente(facturaDTO);

            // Verificar existencia de vendedor
            Vendedor vendedor = vendedorRepository.findByDocumento(facturaDTO.getVendedorDocumento())
                    .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

            // Verificar existencia de cajero
            Cajero cajero = cajeroRepository.findByToken(facturaDTO.getCajeroToken())
                    .orElseThrow(() -> new RuntimeException("Cajero no encontrado"));

            // Crear la compra
            Compra compra = new Compra();
            compra.setTienda(tienda);
            compra.setCliente(cliente);
            compra.setVendedor(vendedor);
            compra.setCajero(cajero);
            compra.setFecha(LocalDateTime.now());
            compra.setTotal(calcularTotal(facturaDTO));
            compra.setImpuestos(facturaDTO.getImpuesto());
            compraRepository.save(compra);

            // Procesar los detalles de la compra
            for (FacturaDTO.ProductoFactura productoFactura : facturaDTO.getProductos()) {
                Producto producto = productoRepository.findByReferencia(productoFactura.getReferencia())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                Detalles_Compra detalle = new Detalles_Compra();
                detalle.setCompra(compra);
                detalle.setProducto(producto);
                detalle.setCantidad(productoFactura.getCantidad());
                detalle.setPrecio(producto.getPrecio());
                detalle.setDescuento(productoFactura.getDescuento());
                detallesCompraRepository.save(detalle);
            }

            // Procesar los pagos
            for (FacturaDTO.MedioPago medioPago : facturaDTO.getMediosPago()) {
                Pago pago = new Pago();
                pago.setCompra(compra);
                pago.setValor(medioPago.getValor());
                pago.setCuotas(medioPago.getCuotas());
                pago.setTarjetaTipo(medioPago.getTipoTarjeta());
                pagoRepository.save(pago);
            }

            // Respuesta exitosa
            FacturaDTO.ResponseData responseData = new FacturaDTO.ResponseData(
                compra.getId(),
                compra.getTotal(),
                compra.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            );

            return new Response<>(
                "success",
                "La factura se ha creado correctamente con el número: " + compra.getId(),
                responseData
            );
        } catch (RuntimeException e) {
            // Si ocurre un error, devolvemos un error con el mensaje
            return new Response<>(
                "error",
                e.getMessage(),
                null
            );
        }
    }

    private Cliente obtenerOCrearCliente(FacturaDTO facturaDTO) {
        return clienteRepository.findByDocumento(facturaDTO.getClienteDocumento())
                .orElseGet(() -> {
                    Cliente nuevoCliente = new Cliente();
                    nuevoCliente.setDocumento(facturaDTO.getClienteDocumento());
                    nuevoCliente.setNombre(facturaDTO.getClienteNombre());
                    nuevoCliente.setTipoDocumento(obtenerOCrearTipoDocumento(facturaDTO.getClienteTipoDocumento()));
                    return clienteRepository.save(nuevoCliente);
                });
    }

    private Tipo_Documento obtenerOCrearTipoDocumento(String nombreTipoDocumento) {
        return tipoDocumentoRepository.findByNombre(nombreTipoDocumento)
                .orElseGet(() -> {
                    Tipo_Documento nuevoTipoDocumento = new Tipo_Documento();
                    nuevoTipoDocumento.setNombre(nombreTipoDocumento);
                    return tipoDocumentoRepository.save(nuevoTipoDocumento);
                });
    }

    private double calcularTotal(FacturaDTO facturaDTO) {
        return facturaDTO.getProductos().stream()
                .mapToDouble(p -> {
                    Producto producto = productoRepository.findByReferencia(p.getReferencia())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                    double subtotal = producto.getPrecio() * p.getCantidad();
                    return subtotal - (subtotal * (p.getDescuento() / 100));
                })
                .sum();
    }
}


