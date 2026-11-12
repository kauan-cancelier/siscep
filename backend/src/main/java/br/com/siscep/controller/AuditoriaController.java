package br.com.siscep.controller;

import br.com.siscep.dto.AuditoriaResponse;
import br.com.siscep.dto.PaginaResponse;
import br.com.siscep.security.Perfis;
import br.com.siscep.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
@Tag(name = "Auditoria", description = "Trilha de auditoria das acoes do sistema")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    @Operation(summary = "Lista a trilha de auditoria (somente administrador)")
    @GetMapping
    @PreAuthorize(Perfis.ADMIN)
    public PaginaResponse<AuditoriaResponse> listar(@PageableDefault(size = 20) Pageable pageable) {
        return service.listar(pageable);
    }
}
