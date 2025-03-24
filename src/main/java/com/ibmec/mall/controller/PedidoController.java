package com.ibmec.mall.controller;

import com.ibmec.mall.model.Pedido;
import com.ibmec.mall.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "API para gerenciamento de pedidos")
public class PedidoController {
    private final PedidoService pedidoService;

    @GetMapping
    @Operation(summary = "Listar todos os pedidos")
    public ResponseEntity<List<Pedido>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable String id) {
        return pedidoService.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Buscar pedidos por usuário")
    public ResponseEntity<List<Pedido>> buscarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(pedidoService.buscarPorUsuario(usuarioId));
    }

    @PostMapping
    @Operation(summary = "Criar novo pedido")
    public ResponseEntity<Pedido> criar(@RequestBody Pedido pedido) {
        return ResponseEntity.ok(pedidoService.criar(pedido));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido")
    public ResponseEntity<Pedido> atualizarStatus(
            @PathVariable String id,
            @RequestParam String novoStatus) {
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, novoStatus));
    }

    @GetMapping("/periodo")
    @Operation(summary = "Buscar pedidos por período")
    public ResponseEntity<List<Pedido>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(pedidoService.buscarPorPeriodo(inicio, fim));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar pedidos por status")
    public ResponseEntity<List<Pedido>> buscarPorStatus(@PathVariable String status) {
        return ResponseEntity.ok(pedidoService.buscarPorStatus(status));
    }
} 