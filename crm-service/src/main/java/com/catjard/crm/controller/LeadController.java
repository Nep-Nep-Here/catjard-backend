package com.catjard.crm.controller;

import com.catjard.crm.dto.ActualizarLeadDTO;
import com.catjard.crm.dto.ConversionResultDTO;
import com.catjard.crm.dto.ConvertirLeadDTO;
import com.catjard.crm.dto.CrearLeadDTO;
import com.catjard.crm.dto.LeadDTO;
import com.catjard.crm.service.LeadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Tag(name = "Leads", description = "Leads del CRM")
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('vendedor','gerente')")
    public List<LeadDTO> listar(@RequestParam Optional<String> estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','gerente')")
    public LeadDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasAnyRole('vendedor','gerente')")
    public LeadDTO obtenerPorCodigo(@PathVariable String codigo) {
        return service.obtenerPorCodigo(codigo);
    }

    @PostMapping
    public ResponseEntity<LeadDTO> crear(@Valid @RequestBody CrearLeadDTO dto) {
        LeadDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/leads/" + creado.id())).body(creado);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','gerente')")
    public LeadDTO actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarLeadDTO dto) {
        return service.actualizar(id, dto);
    }

    @PostMapping("/{id}/convertir")
    @PreAuthorize("hasAnyRole('vendedor','gerente')")
    public ResponseEntity<ConversionResultDTO> convertir(@PathVariable Long id,
                                                         @Valid @RequestBody ConvertirLeadDTO dto) {
        return ResponseEntity.ok(service.convertirEnCliente(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
