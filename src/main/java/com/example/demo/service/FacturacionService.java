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
    public Response<FacturaDTO.ResponseData> procesarFactura(String tiendaId, FacturaDTO facturaDTO) {
        try {
            // Validación: Si la tienda no existe
            Tienda tienda = tiendaRepository.findById(tiendaId)
                    .orElseThrow(() -> new RuntimeException("La tienda no existe en el sistema"));

            // Validación: Cliente no proporcionado
            if (facturaDTO.getClienteDocumento() == null || facturaDTO.getClienteNombre() == null) {
                throw new RuntimeException("No hay información del cliente");
            }

            // Obtener o crear cliente
            Cliente cliente = obtenerOCrearCliente(facturaDTO);

            // Validación: Vendedor no proporcionado
            if (facturaDTO.getVendedorDocumento() == null) {
                throw new RuntimeException("No hay información del vendedor");
            }

            // Verificar existencia de vendedor
            Vendedor vendedor = vendedorRepository.findByDocumento(facturaDTO.getVendedorDocumento())
                    .orElseThrow(() -> new RuntimeException("El vendedor no existe en la tienda"));

            // Validación: Cajero no proporcionado
            if (facturaDTO.getCajeroToken() == null) {
                throw new RuntimeException("No hay información del cajero");
            }

            // Verificar existencia de cajero
            Cajero cajero = cajeroRepository.findByToken(facturaDTO.getCajeroToken())
                    .orElseThrow(() -> new RuntimeException("El token no corresponde a ningún cajero en la tienda"));

            // Validación: Cajero asignado a otra tienda
            if (!cajero.getTienda().getId().equals(tiendaId)) {
                throw new RuntimeException("El cajero no está asignado a esta tienda");
            }

            // Validación: Si no hay productos asignados
            if (facturaDTO.getProductos() == null || facturaDTO.getProductos().isEmpty()) {
                throw new RuntimeException("No hay productos asignados para esta compra");
            }

            // Validación: Si no hay medios de pago asignados
            if (facturaDTO.getMediosPago() == null || facturaDTO.getMediosPago().isEmpty()) {
                throw new RuntimeException("No hay medios de pagos asignados para esta compra");
            }

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
                // Validación: Producto no existe
                Producto producto = productoRepository.findByReferencia(productoFactura.getReferencia())
                        .orElseThrow(() -> new RuntimeException("La referencia del producto " + productoFactura.getReferencia() + " no existe, por favor revisar los datos"));

                // Validación: Cantidad supera el máximo disponible
                if (producto.getCantidad() < productoFactura.getCantidad()) {
                    throw new RuntimeException("La cantidad a comprar supera el máximo del producto en tienda");
                }

                Detalles_Compra detalle = new Detalles_Compra();
                detalle.setCompra(compra);
                detalle.setProducto(producto);
                detalle.setCantidad(productoFactura.getCantidad());
                detalle.setPrecio(producto.getPrecio());
                detalle.setDescuento(productoFactura.getDescuento());
                detallesCompraRepository.save(detalle);
            }

            // Procesar los pagos
            double totalPago = 0;
            for (FacturaDTO.MedioPago medioPago : facturaDTO.getMediosPago()) {
                // Validación: Tipo de pago no existe
                if (medioPago.getTipoTarjeta() == null || medioPago.getTipoTarjeta().isEmpty()) {
                    throw new RuntimeException("Tipo de pago no permitido en la tienda");
                }

                Pago pago = new Pago();
                pago.setCompra(compra);
                pago.setValor(medioPago.getValor());
                pago.setCuotas(medioPago.getCuotas());
                pago.setTarjetaTipo(medioPago.getTipoTarjeta());
                pagoRepository.save(pago);
                totalPago += medioPago.getValor();
            }

            // Validación: El valor de la factura no coincide con el valor total de los pagos
            if (compra.getTotal() != totalPago) {
                throw new RuntimeException("El valor de la factura no coincide con el valor total de los pagos");
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
    public Response<FacturaDTO.ResponseData> consultarFactura(String tiendaId, FacturaDTO facturaDTO) {
        try {
            // Validar tienda
            Tienda tienda = tiendaRepository.findById(tiendaId)
                    .orElseThrow(() -> new RuntimeException("La tienda no existe"));

            // Validar cajero
            Cajero cajero = cajeroRepository.findByToken(facturaDTO.getCajeroToken())
                    .orElseThrow(() -> new RuntimeException("El token no corresponde a ningún cajero"));

            // Validar si el cajero está asignado a la tienda
            if (!cajero.getTienda().getId().equals(tiendaId)) {
                throw new RuntimeException("El cajero no está asignado a esta tienda");
            }

            // Consultar cliente
            Cliente cliente = clienteRepository.findByDocumento(facturaDTO.getClienteDocumento())
                    .orElseThrow(() -> new RuntimeException("El cliente no existe"));

            // Consultar factura (compra)
            Compra compra = compraRepository.findById(facturaDTO.getNumero()) // Aquí utilizamos el número de factura
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

            // Verificar si la factura pertenece a la tienda
            if (!compra.getTienda().getId().equals(tiendaId)) {
                throw new RuntimeException("La factura no pertenece a esta tienda");
            }

            // Crear respuesta con los detalles de la factura
            FacturaDTO.ResponseData responseData = new FacturaDTO.ResponseData(
                    compra.getId(),
                    compra.getTotal(),
                    compra.getFecha().toString() // Formatear la fecha como string
            );

            return new Response<>(
                    "success",
                    "Factura consultada correctamente",
                    responseData
            );
        } catch (RuntimeException e) {
            // Manejo de errores
            return new Response<>(
                    "error",
                    e.getMessage(),
                    null
            );
        }
    }
}


