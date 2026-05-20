package com.catjard.operations.controller;

import com.catjard.operations.dto.MarcarHitoDTO;
import com.catjard.operations.dto.TrackingEventoDTO;
import com.catjard.operations.service.TrackingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tracking", description = "Eventos de tracking de pedidos")
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService service;

    @GetMapping("/pedido/{codigo}")
    @PreAuthorize("hasAnyRole('produccion','gerente','vendedor','almacen','cliente')")
    public List<TrackingEventoDTO> listar(@PathVariable String codigo) {
        return service.listar(codigo);
    }

    // 'vendedor' incluido: PATCH /api/pedidos lo permite y el front encadena el
    // hito de tracking automáticamente al cambiar estado; sin esto daba 403.
    @PostMapping("/pedido/{codigo}")
    @PreAuthorize("hasAnyRole('produccion','gerente','almacen','vendedor')")
    public TrackingEventoDTO marcar(@PathVariable String codigo, @Valid @RequestBody MarcarHitoDTO dto) {
        return service.marcar(codigo, dto);
    }
}
