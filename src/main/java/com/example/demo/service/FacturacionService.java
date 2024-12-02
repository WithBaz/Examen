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
import java.util.List;

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
    public Response<FacturaDTO.ResponseData> procesarFactura(String tiendaId, FacturaDTO facturaDTO) {
        try {
            Tienda tienda = validarTienda(tiendaId);
            Cliente cliente = obtenerOCrearCliente(facturaDTO);
            Vendedor vendedor = validarVendedor(facturaDTO.getVendedorDocumento());
            Cajero cajero = validarCajero(facturaDTO.getCajeroToken(), tiendaId);
            validarProductos(facturaDTO.getProductos());
            validarMediosPago(facturaDTO.getMediosPago());

            Compra compra = crearCompra(tienda, cliente, vendedor, cajero, facturaDTO);

            FacturaDTO.ResponseData responseData = new FacturaDTO.ResponseData(
                    compra.getId(),
                    compra.getTotal(),
                    compra.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            );

            return new Response<>("success", "La factura se ha creado correctamente con el número: " + compra.getId(), responseData);

        } catch (RuntimeException e) {
            return new Response<>("error", e.getMessage(), null);
        }
    }

    @Transactional(readOnly = true)
    public Response<FacturaDTO.ResponseData> consultarFactura(String tiendaId, FacturaDTO consultaDTO) {
        try {
            Tienda tienda = validarTienda(tiendaId);
            Cajero cajero = validarCajero(consultaDTO.getCajeroToken(), tiendaId);
            Cliente cliente = clienteRepository.findByDocumento(consultaDTO.getClienteDocumento())
                    .orElseThrow(() -> new RuntimeException("El cliente no existe"));

            Compra compra = compraRepository.findById(consultaDTO.getNumero())
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

            if (!compra.getTienda().getId().equals(tiendaId)) {
                throw new RuntimeException("La factura no pertenece a esta tienda");
            }

            FacturaDTO.ResponseData responseData = new FacturaDTO.ResponseData(
                    compra.getId(),
                    compra.getTotal(),
                    compra.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            );

            return new Response<>("success", "Factura consultada correctamente", responseData);

        } catch (RuntimeException e) {
            return new Response<>("error", e.getMessage(), null);
        }
    }

    // Métodos privados para validaciones y lógica

    private Tienda validarTienda(String tiendaId) {
        return tiendaRepository.findById(tiendaId)
                .orElseThrow(() -> new RuntimeException("La tienda no existe en el sistema"));
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

    private Vendedor validarVendedor(String documentoVendedor) {
        return vendedorRepository.findByDocumento(documentoVendedor)
                .orElseThrow(() -> new RuntimeException("El vendedor no existe en la tienda"));
    }

    private Cajero validarCajero(String tokenCajero, String tiendaId) {
        Cajero cajero = cajeroRepository.findByToken(tokenCajero)
                .orElseThrow(() -> new RuntimeException("El token no corresponde a ningún cajero en la tienda"));

        if (!cajero.getTienda().getId().equals(tiendaId)) {
            throw new RuntimeException("El cajero no está asignado a esta tienda");
        }
        return cajero;
    }

    private void validarProductos(List<FacturaDTO.ProductoFactura> productos) {
        if (productos == null || productos.isEmpty()) {
            throw new RuntimeException("No hay productos asignados para esta compra");
        }

        for (FacturaDTO.ProductoFactura productoFactura : productos) {
            Producto producto = productoRepository.findByReferencia(productoFactura.getReferencia())
                    .orElseThrow(() -> new RuntimeException("La referencia del producto " + productoFactura.getReferencia() + " no existe, por favor revisar los datos"));

            if (producto.getCantidad() < productoFactura.getCantidad()) {
                throw new RuntimeException("La cantidad a comprar supera el máximo del producto en tienda");
            }
        }
    }

    private void validarMediosPago(List<FacturaDTO.MedioPago> mediosPago) {
        if (mediosPago == null || mediosPago.isEmpty()) {
            throw new RuntimeException("No hay medios de pagos asignados para esta compra");
        }
    }

    private Compra crearCompra(Tienda tienda, Cliente cliente, Vendedor vendedor, Cajero cajero, FacturaDTO facturaDTO) {
        Compra compra = new Compra();
        compra.setTienda(tienda);
        compra.setCliente(cliente);
        compra.setVendedor(vendedor);
        compra.setCajero(cajero);
        compra.setFecha(LocalDateTime.now());
        compra.setTotal(calcularTotal(facturaDTO));
        compra.setImpuestos(facturaDTO.getImpuesto());
        compraRepository.save(compra);

        for (FacturaDTO.ProductoFactura productoFactura : facturaDTO.getProductos()) {
            Producto producto = productoRepository.findByReferencia(productoFactura.getReferencia()).get();

            Detalles_Compra detalle = new Detalles_Compra();
            detalle.setCompra(compra);
            detalle.setProducto(producto);
            detalle.setCantidad(productoFactura.getCantidad());
            detalle.setPrecio(producto.getPrecio());
            detalle.setDescuento(productoFactura.getDescuento());
            detallesCompraRepository.save(detalle);
        }

        double totalPago = facturaDTO.getMediosPago().stream()
                .mapToDouble(FacturaDTO.MedioPago::getValor)
                .sum();

        if (compra.getTotal() != totalPago) {
            throw new RuntimeException("El valor de la factura no coincide con el valor total de los pagos");
        }

        for (FacturaDTO.MedioPago medioPago : facturaDTO.getMediosPago()) {
            Pago pago = new Pago();
            pago.setCompra(compra);
            pago.setValor(medioPago.getValor());
            pago.setCuotas(medioPago.getCuotas());
            pago.setTarjetaTipo(medioPago.getTipoTarjeta());
            pagoRepository.save(pago);
        }

        return compra;
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



