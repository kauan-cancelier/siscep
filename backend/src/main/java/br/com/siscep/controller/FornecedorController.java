package br.com.siscep.controller;

import br.com.siscep.dto.FornecedorRequest;
import br.com.siscep.dto.FornecedorResponse;
import br.com.siscep.dto.StatusRequest;
import br.com.siscep.security.Perfis;
import br.com.siscep.service.FornecedorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fornecedores")
@Tag(name = "Fornecedors")
public class FornecedorController {

    private final FornecedorService service;

    public FornecedorController(FornecedorService service) {
        this.service = service;
    }

    @GetMapping
    public List<FornecedorResponse> listar(@RequestParam(defaultValue = "false") boolean apenasAtivos) {
        return service.listar(apenasAtivos);
    }

    @GetMapping("/{id}")
    public FornecedorResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.CADASTROS)
    public FornecedorResponse criar(@Valid @RequestBody FornecedorRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Perfis.CADASTROS)
    public FornecedorResponse atualizar(@PathVariable Long id, @Valid @RequestBody FornecedorRequest req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize(Perfis.CADASTROS)
    public FornecedorResponse status(@PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        return service.alterarStatus(id, req.ativo());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(Perfis.CADASTROS)
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
