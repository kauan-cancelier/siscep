package br.com.siscep.controller;

import br.com.siscep.domain.TipoMovimentacao;
import br.com.siscep.dto.AjusteRequest;
import br.com.siscep.dto.EntradaRequest;
import br.com.siscep.dto.MovimentacaoResponse;
import br.com.siscep.dto.PaginaResponse;
import br.com.siscep.dto.SaidaRequest;
import br.com.siscep.security.Perfis;
import br.com.siscep.security.UsuarioAutenticado;
import br.com.siscep.service.EstoqueService;
import br.com.siscep.service.UsuarioLogadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/movimentacoes")
@Tag(name = "Movimentações")
public class MovimentacaoController {

    private final EstoqueService service;
    private final UsuarioLogadoService usuarioLogado;

    public MovimentacaoController(EstoqueService service, UsuarioLogadoService usuarioLogado) {
        this.service = service;
        this.usuarioLogado = usuarioLogado;
    }

    @Operation(summary = "Registra entrada de material (RF07)")
    @PostMapping("/entradas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.ALMOXARIFE)
    public MovimentacaoResponse entrada(@Valid @RequestBody EntradaRequest req,
                                        @AuthenticationPrincipal UsuarioAutenticado principal) {
        return service.registrarEntrada(req, usuarioLogado.carregar(principal));
    }

    @Operation(summary = "Registra saída para produção (RF08)")
    @PostMapping("/saidas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.SAIDA)
    public MovimentacaoResponse saida(@Valid @RequestBody SaidaRequest req,
                                      @AuthenticationPrincipal UsuarioAutenticado principal) {
        return service.registrarSaida(req, usuarioLogado.carregar(principal));
    }

    @Operation(summary = "Registra ajuste de inventário com justificativa (RF09)")
    @PostMapping("/ajustes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.ALMOXARIFE)
    public MovimentacaoResponse ajuste(@Valid @RequestBody AjusteRequest req,
                                       @AuthenticationPrincipal UsuarioAutenticado principal) {
        return service.registrarAjuste(req, usuarioLogado.carregar(principal));
    }

    @Operation(summary = "Histórico de movimentações; o operador vê apenas as próprias (RF14)")
    @GetMapping
    public PaginaResponse<MovimentacaoResponse> historico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long pastilhaId,
            @RequestParam(required = false) TipoMovimentacao tipo,
            @RequestParam(required = false) Long usuarioId,
            @PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        return service.historico(inicio != null ? inicio.atStartOfDay() : null,
                fim != null ? fim.atTime(LocalTime.MAX) : null, pastilhaId, tipo, usuarioId,
                usuarioLogado.carregar(principal), pageable);
    }

    @GetMapping("/{id}")
    public MovimentacaoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado principal) {
        return service.buscar(id, usuarioLogado.carregar(principal));
    }
}
