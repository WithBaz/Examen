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

    @PostMapping("/crear/{tiendaId}")
    public ResponseEntity<?> crearFactura(@PathVariable String tiendaId, @RequestBody FacturaDTO facturaDTO) {
        Response<?> resultado = facturaService.procesarFactura(tiendaId, facturaDTO);
        return ResponseEntity.ok(resultado);
    }
}