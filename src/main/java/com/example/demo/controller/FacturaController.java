package com.example.demo.controller;



import com.example.demo.dto.FacturaDTO;
import com.example.demo.dto.Response;
import com.example.demo.service.FacturacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturacionService facturaService;

    // Endpoint para crear una factura
    @PostMapping("/crear/{tiendaId}")
    public ResponseEntity<?> crearFactura(@PathVariable String tiendaId, @RequestBody FacturaDTO facturaDTO) {
        Response<?> resultado = facturaService.procesarFactura(tiendaId, facturaDTO);
        return ResponseEntity.ok(resultado);
    }

    // Endpoint para consultar una factura
    @PostMapping("/consultar/{tiendaId}")
    public ResponseEntity<Response<FacturaDTO.ResponseData>> consultarFactura(
            @PathVariable String tiendaId, 
            @RequestBody FacturaDTO consultaDTO) {

        // Llamamos al servicio para consultar la factura
        Response<FacturaDTO.ResponseData> response = facturaService.consultarFactura(tiendaId, consultaDTO);

        // Si la respuesta es exitosa, retornamos el status 200
        if ("success".equals(response.getStatus())) {
            return ResponseEntity.ok(response); // Retorna código 200
        } else {
            return ResponseEntity.status(404).body(response); // Retorna código 404 si hay error
        }
    }
}
 
