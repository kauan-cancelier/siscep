package br.com.siscep.controller;

import br.com.siscep.dto.LoginRequest;
import br.com.siscep.dto.LoginResponse;
import br.com.siscep.dto.TrocaSenhaRequest;
import br.com.siscep.dto.UsuarioResponse;
import br.com.siscep.security.UsuarioAutenticado;
import br.com.siscep.service.AuthService;
import br.com.siscep.service.UsuarioLogadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;
    private final UsuarioLogadoService usuarioLogado;
    private final Clock clock;

    public AuthController(AuthService authService, UsuarioLogadoService usuarioLogado, Clock clock) {
        this.authService = authService;
        this.usuarioLogado = usuarioLogado;
        this.clock = clock;
    }

    @Operation(summary = "Autentica o usuário e devolve o token JWT (RF01)")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return authService.login(req.email(), req.senha(), http.getRemoteAddr());
    }

    @Operation(summary = "Dados do usuário autenticado")
    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioAutenticado principal) {
        return UsuarioResponse.de(usuarioLogado.carregar(principal), LocalDateTime.now(clock));
    }

    @Operation(summary = "Altera a propria senha (RF21)")
    @PostMapping("/trocar-senha")
    public ResponseEntity<Void> trocarSenha(@AuthenticationPrincipal UsuarioAutenticado principal,
                                            @Valid @RequestBody TrocaSenhaRequest req) {
        authService.trocarSenha(usuarioLogado.carregar(principal), req);
        return ResponseEntity.noContent().build();
    }
}
