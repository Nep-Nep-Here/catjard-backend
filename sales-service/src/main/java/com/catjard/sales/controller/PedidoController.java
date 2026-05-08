package com.catjard.sales.controller;

import com.catjard.sales.dto.ActualizarPedidoDTO;
import com.catjard.sales.dto.PedidoDTO;
import com.catjard.sales.dto.VoucherDTO;
import com.catjard.sales.service.PedidoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Pedidos", description = "Pedidos generados a partir de cotizaciones aprobadas")
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('vendedor','gerente','almacen','produccion','cliente')")
    public List<PedidoDTO> listar(@RequestParam Optional<String> estado,
                                  @RequestParam Optional<Long> clienteId) {
        return service.listar(estado, clienteId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','gerente','almacen','produccion','cliente')")
    public PedidoDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasAnyRole('vendedor','gerente','almacen','produccion','cliente')")
    public PedidoDTO obtenerPorCodigo(@PathVariable String codigo) {
        return service.obtenerPorCodigo(codigo);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('vendedor','gerente','almacen','produccion')")
    public PedidoDTO actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarPedidoDTO dto) {
        return service.actualizar(id, dto);
    }

    @PatchMapping("/codigo/{codigo}")
    @PreAuthorize("hasAnyRole('vendedor','gerente','almacen','produccion')")
    public PedidoDTO actualizarPorCodigo(@PathVariable String codigo, @Valid @RequestBody ActualizarPedidoDTO dto) {
        return service.actualizarPorCodigo(codigo, dto);
    }

    @PostMapping("/{id}/voucher")
    @PreAuthorize("hasAnyRole('cliente','vendedor','gerente','almacen','produccion')")
    public PedidoDTO subirVoucher(@PathVariable Long id, @Valid @RequestBody VoucherDTO dto) {
        return service.subirVoucher(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('gerente')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
