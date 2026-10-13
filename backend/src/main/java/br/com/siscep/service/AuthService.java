package br.com.siscep.service;

import br.com.siscep.domain.RegistroAcesso;
import br.com.siscep.domain.Usuario;
import br.com.siscep.dto.LoginResponse;
import br.com.siscep.dto.TrocaSenhaRequest;
import br.com.siscep.dto.UsuarioResponse;
import br.com.siscep.exception.AutenticacaoException;
import br.com.siscep.exception.RegraNegocioException;
import br.com.siscep.repository.RegistroAcessoRepository;
import br.com.siscep.repository.UsuarioRepository;
import br.com.siscep.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/** Autenticacao (RF01), bloqueio por tentativas (RN15) e troca da propria senha (RF21). */
@Service
public class AuthService {

    static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha invalidos.";

    private final UsuarioRepository usuarioRepository;
    private final RegistroAcessoRepository registroAcessoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Clock clock;

    public AuthService(UsuarioRepository usuarioRepository, RegistroAcessoRepository registroAcessoRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.registroAcessoRepository = registroAcessoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.clock = clock;
    }

    @Transactional(noRollbackFor = AutenticacaoException.class)
    public LoginResponse login(String email, String senha, String ip) {
        LocalDateTime agora = LocalDateTime.now(clock);
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (usuario == null) {
            registrar(email, false, "usuario inexistente", ip, agora);
            throw new AutenticacaoException(CREDENCIAIS_INVALIDAS);
        }
        if (!usuario.isAtivo()) {
            registrar(email, false, "usuario inativo", ip, agora);
            throw new AutenticacaoException("Usuario inativo. Procure o administrador do sistema.");
        }
        if (usuario.estaBloqueado(agora)) {
            registrar(email, false, "conta bloqueada", ip, agora);
            throw new AutenticacaoException(
                    "Conta bloqueada por excesso de tentativas. Tente novamente em alguns minutos.");
        }
        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            usuario.registrarFalhaLogin(agora);
            registrar(email, false, "senha incorreta", ip, agora);
            if (usuario.estaBloqueado(agora)) {
                throw new AutenticacaoException("Tres tentativas invalidas. Conta bloqueada por "
                        + Usuario.MINUTOS_BLOQUEIO + " minutos.");
            }
            throw new AutenticacaoException(CREDENCIAIS_INVALIDAS);
        }
        usuario.registrarLoginComSucesso(agora);
        registrar(email, true, null, ip, agora);
        return new LoginResponse(jwtService.gerarToken(usuario), "Bearer", jwtService.getExpiracaoSegundos(),
                UsuarioResponse.de(usuario, agora));
    }

    @Transactional
    public void trocarSenha(Usuario usuario, TrocaSenhaRequest req) {
        if (!passwordEncoder.matches(req.senhaAtual(), usuario.getSenhaHash())) {
            throw new RegraNegocioException("A senha atual informada esta incorreta.");
        }
        if (req.senhaAtual().equals(req.novaSenha())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da senha atual.");
        }
        usuario.setSenhaHash(passwordEncoder.encode(req.novaSenha()));
        usuarioRepository.save(usuario);
    }

    private void registrar(String email, boolean sucesso, String motivo, String ip, LocalDateTime agora) {
        registroAcessoRepository.save(new RegistroAcesso(email, sucesso, motivo, ip, agora));
    }
}
