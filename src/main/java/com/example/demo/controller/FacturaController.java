package com.example.demo.controller;



import com.example.demo.dto.FacturaDTO;
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
    public ResponseEntity<String> crearFactura(@PathVariable String tiendaId, @RequestBody FacturaDTO facturaDTO) {
        String resultado = facturaService.procesarFactura(tiendaId, facturaDTO);
        return ResponseEntity.ok(resultado);
    }
}