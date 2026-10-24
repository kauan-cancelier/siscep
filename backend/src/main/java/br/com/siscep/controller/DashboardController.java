package br.com.siscep.controller;

import br.com.siscep.dto.AlertaResponse;
import br.com.siscep.dto.DashboardResponse;
import br.com.siscep.security.Perfis;
import br.com.siscep.service.AlertaService;
import br.com.siscep.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Dashboard e alertas")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AlertaService alertaService;

    public DashboardController(DashboardService dashboardService, AlertaService alertaService) {
        this.dashboardService = dashboardService;
        this.alertaService = alertaService;
    }

    @Operation(summary = "Indicadores do estoque no periodo (RF15)")
    @GetMapping("/dashboard")
    @PreAuthorize(Perfis.GESTAO)
    public DashboardResponse dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return dashboardService.gerar(inicio, fim);
    }

    @Operation(summary = "Alertas de estoque abertos (RF12)")
    @GetMapping("/alertas")
    public List<AlertaResponse> alertas() {
        return alertaService.listarAbertos();
    }
}
