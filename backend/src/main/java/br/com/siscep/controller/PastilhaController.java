package br.com.siscep.controller;

import br.com.siscep.domain.StatusEstoque;
import br.com.siscep.dto.AlertaResponse;
import br.com.siscep.dto.EstoqueMinimoRequest;
import br.com.siscep.dto.HistoricoEstoqueMinimoResponse;
import br.com.siscep.dto.ImportacaoResultado;
import br.com.siscep.dto.PaginaResponse;
import br.com.siscep.dto.PastilhaFornecedorRequest;
import br.com.siscep.dto.PastilhaFornecedorResponse;
import br.com.siscep.dto.PastilhaRequest;
import br.com.siscep.dto.PastilhaResponse;
import br.com.siscep.dto.StatusRequest;
import br.com.siscep.exception.RegraNegocioException;
import br.com.siscep.security.Perfis;
import br.com.siscep.security.UsuarioAutenticado;
import br.com.siscep.service.AlertaService;
import br.com.siscep.service.ImportacaoService;
import br.com.siscep.service.PastilhaService;
import br.com.siscep.service.UsuarioLogadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pastilhas")
@Tag(name = "Pastilhas e estoque")
public class PastilhaController {

    private final PastilhaService service;
    private final AlertaService alertaService;
    private final ImportacaoService importacaoService;
    private final UsuarioLogadoService usuarioLogado;

    public PastilhaController(PastilhaService service, AlertaService alertaService,
                              ImportacaoService importacaoService, UsuarioLogadoService usuarioLogado) {
        this.service = service;
        this.alertaService = alertaService;
        this.importacaoService = importacaoService;
        this.usuarioLogado = usuarioLogado;
    }

    @Operation(summary = "Consulta o estoque atual com filtros, paginacao e ordenacao (RF11)")
    @GetMapping
    public PaginaResponse<PastilhaResponse> consultar(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) Long fabricanteId,
            @RequestParam(required = false) Long fornecedorId,
            @RequestParam(required = false) StatusEstoque situacao,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.consultar(termo, fabricanteId, fornecedorId, situacao, ativo, pageable);
    }

    @GetMapping("/{id}")
    public PastilhaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.CADASTROS)
    public PastilhaResponse criar(@Valid @RequestBody PastilhaRequest req,
                                  @AuthenticationPrincipal UsuarioAutenticado principal) {
        return service.criar(req, usuarioLogado.carregar(principal));
    }

    @PutMapping("/{id}")
    @PreAuthorize(Perfis.CADASTROS)
    public PastilhaResponse atualizar(@PathVariable Long id, @Valid @RequestBody PastilhaRequest req,
                                      @AuthenticationPrincipal UsuarioAutenticado principal) {
        return service.atualizar(id, req, usuarioLogado.carregar(principal));
    }

    @Operation(summary = "Define o estoque mínimo (RF06 / RN09)")
    @PatchMapping("/{id}/estoque-minimo")
    @PreAuthorize(Perfis.CADASTROS)
    public PastilhaResponse estoqueMinimo(@PathVariable Long id, @Valid @RequestBody EstoqueMinimoRequest req,
                                          @AuthenticationPrincipal UsuarioAutenticado principal) {
        return service.definirEstoqueMinimo(id, req.estoqueMinimo(), usuarioLogado.carregar(principal));
    }

    @GetMapping("/{id}/estoque-minimo/historico")
    public List<HistoricoEstoqueMinimoResponse> historicoMinimo(@PathVariable Long id) {
        return service.historicoEstoqueMinimo(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize(Perfis.CADASTROS)
    public PastilhaResponse status(@PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        return service.alterarStatus(id, req.ativo());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(Perfis.CADASTROS)
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/alertas")
    public List<AlertaResponse> alertas(@PathVariable Long id) {
        return alertaService.listarPorPastilha(id);
    }

    @Operation(summary = "Fornecedores alternativos da pastilha")
    @GetMapping("/{id}/fornecedores")
    public List<PastilhaFornecedorResponse> fornecedores(@PathVariable Long id) {
        return service.listarFornecedores(id);
    }

    @PostMapping("/{id}/fornecedores")
    @PreAuthorize(Perfis.CADASTROS)
    public PastilhaFornecedorResponse salvarFornecedor(@PathVariable Long id,
                                                       @Valid @RequestBody PastilhaFornecedorRequest req) {
        return service.salvarFornecedor(id, req);
    }

    @DeleteMapping("/{id}/fornecedores/{fornecedorId}")
    @PreAuthorize(Perfis.CADASTROS)
    public ResponseEntity<Void> removerFornecedor(@PathVariable Long id, @PathVariable Long fornecedorId) {
        service.removerFornecedor(id, fornecedorId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Importa a carga inicial a partir da planilha atual exportada em CSV")
    @PostMapping(value = "/importacao", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(Perfis.CADASTROS)
    public ImportacaoResultado importar(@RequestPart("arquivo") MultipartFile arquivo,
                                        @AuthenticationPrincipal UsuarioAutenticado principal) throws IOException {
        if (arquivo.isEmpty()) {
            throw new RegraNegocioException("Selecione um arquivo CSV para importar.");
        }
        return importacaoService.importarPastilhas(arquivo.getInputStream(), usuarioLogado.carregar(principal));
    }
}
