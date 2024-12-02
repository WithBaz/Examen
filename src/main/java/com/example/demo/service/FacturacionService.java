package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.CompraDTO;
import com.example.demo.dto.FacturaDTO;
import com.example.demo.entity.Producto;
import com.example.demo.entity.Tipo_Pago;
import com.example.demo.entity.Vendedor;
import com.example.demo.entity.Cajero;
import com.example.demo.entity.Cliente;
import com.example.demo.entity.Compra;
import com.example.demo.entity.Detalles_Compra;
import com.example.demo.respository.CajeroRepository;
import com.example.demo.respository.ClienteRepository;
import com.example.demo.respository.CompraRepository;
import com.example.demo.respository.DetallesCompraRepository;
import com.example.demo.respository.PagoRepository;
import com.example.demo.respository.ProductoRepository;
import com.example.demo.respository.TipoPagoRepository;
import com.example.demo.respository.VendedorRepository;

import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacturacionService {
    private final CompraRepository compraRepository;
    private final DetallesCompraRepository detallesCompraRepository;
    private final PagoRepository pagoRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final VendedorRepository vendedorRepository;
    private final CajeroRepository cajeroRepository;
    private final TipoPagoRepository tipoPagoRepository;

    @Transactional
    public CompraDTO procesarFactura(String tiendaId, FacturaDTO facturaDTO) {
        // Validar y obtener entidades
        Cliente cliente = clienteRepository.findByDocumento(facturaDTO.getCliente().getDocumento())
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            
        Vendedor vendedor = vendedorRepository.findByDocumento(facturaDTO.getVendedor().getDocumento())
            .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
            
        Cajero cajero = cajeroRepository.findByToken(facturaDTO.getCajero().getToken())
            .orElseThrow(() -> new RuntimeException("Cajero no encontrado"));

        // Crear la compra
        Compra compra = new Compra();
        compra.setCliente(cliente);
        compra.setVendedor(vendedor);
        compra.setCajero(cajero);
        compra.setFecha(LocalDateTime.now());
        compra.setImpuestos(facturaDTO.getImpuesto());
        
        // Procesar productos
        List<Detalles_Compra> detallesCompraList = new ArrayList<>();
        double total = 0.0;
        
        for (FacturaDTO.Producto productoDTO : facturaDTO.getProductos()) {
            Producto producto = productoRepository.findByReferencia(productoDTO.getReferencia())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoDTO.getReferencia()));
                
            Detalles_Compra detalle = new Detalles_Compra();
            detalle.setCompra(compra);
            detalle.setProducto(producto);
            detalle.setCantidad(productoDTO.getCantidad());
            detalle.setPrecio(producto.getPrecio());
            detalle.setDescuento(productoDTO.getDescuento());
            
            total += (producto.getPrecio() * productoDTO.getCantidad()) * (1 - productoDTO.getDescuento());
            detallesCompraList.add(detalle);
        }
        
        compra.setTotal(total);
        compra.setDetallesCompra(detallesCompraList);
        
        // Guardar la compra
        Compra compraGuardada = compraRepository.save(compra);
        
        // Procesar pagos
        for (FacturaDTO.MedioPago pagoDTO : facturaDTO.getMedios_pago()) {
            Tipo_Pago tipoPago = tipoPagoRepository.findByNombre(pagoDTO.getTipo_pago())
                .orElseThrow(() -> new RuntimeException("Tipo de pago no encontrado"));
                
            Pago pago = new Pago();
            pago.setCompra(compraGuardada);
            pago.setTipoPago(tipoPago);
            pago.setTarjetaTipo(pagoDTO.getTipo_tarjeta());
            pago.setCuotas(pagoDTO.getCuotas());
            pago.setValor(pagoDTO.getValor());
            
            pagoRepository.save(pago);
        }
        
        return convertToDTO(compraGuardada);
    }

    private CompraDTO convertToDTO(Compra compra) {
        CompraDTO compraDTO = new CompraDTO();
        compraDTO.setId(compra.getId());
        compraDTO.setTotal(compra.getTotal());
        compraDTO.setImpuestos(compra.getImpuestos());
        compraDTO.setFecha(compra.getFecha());
        compraDTO.setObservaciones(compra.getObservaciones());
        
        // Convertir cliente, vendedor, cajero, detalles de compra y pagos
        // (Implementa estos métodos según tus necesidades específicas)
        compraDTO.setCliente(convertClienteToDTO(compra.getCliente()));
        compraDTO.setVendedor(convertVendedorToDTO(compra.getVendedor()));
        compraDTO.setCajero(convertCajeroToDTO(compra.getCajero()));
        compraDTO.setDetallesCompra(convertDetallesCompraToDTO(compra.getDetallesCompra()));
        compraDTO.setPagos(convertPagosToDTO(compra.getPagos()));
        
        return compraDTO;
    }

    // Implementa los métodos de conversión restantes aquí
    // ...
}