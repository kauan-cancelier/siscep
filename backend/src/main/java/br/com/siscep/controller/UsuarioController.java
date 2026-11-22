package br.com.siscep.controller;

import br.com.siscep.dto.PaginaResponse;
import br.com.siscep.dto.RedefinirSenhaRequest;
import br.com.siscep.dto.RegistroAcessoResponse;
import br.com.siscep.dto.StatusRequest;
import br.com.siscep.dto.UsuarioRequest;
import br.com.siscep.dto.UsuarioResponse;
import br.com.siscep.security.Perfis;
import br.com.siscep.security.UsuarioAutenticado;
import br.com.siscep.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize(Perfis.ADMIN)
@Tag(name = "Usuários")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @Operation(summary = "Lista os usuários (RF02)")
    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest req,
                                     @AuthenticationPrincipal UsuarioAutenticado principal) {
        return service.atualizar(id, req, principal.id());
    }

    @Operation(summary = "Ativa ou inativa o usuário; o acesso é revogado imediatamente")
    @PatchMapping("/{id}/status")
    public UsuarioResponse status(@PathVariable Long id, @Valid @RequestBody StatusRequest req,
                                  @AuthenticationPrincipal UsuarioAutenticado principal) {
        return service.alterarStatus(id, req.ativo(), principal.id());
    }

    @Operation(summary = "Redefine a senha de um usuário (RF21)")
    @PostMapping("/{id}/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@PathVariable Long id, @Valid @RequestBody RedefinirSenhaRequest req) {
        service.redefinirSenha(id, req.novaSenha());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove o bloqueio por tentativas inválidas (RN15)")
    @PostMapping("/{id}/desbloquear")
    public UsuarioResponse desbloquear(@PathVariable Long id) {
        return service.desbloquear(id);
    }

    @Operation(summary = "Registro de acessos ao sistema (auditoria de login)")
    @GetMapping("/acessos")
    public PaginaResponse<RegistroAcessoResponse> acessos(@PageableDefault(size = 20) Pageable pageable) {
        return service.listarAcessos(pageable);
    }
}
