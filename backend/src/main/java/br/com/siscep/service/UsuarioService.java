package br.com.siscep.service;

import br.com.siscep.domain.Usuario;
import br.com.siscep.dto.PaginaResponse;
import br.com.siscep.dto.RegistroAcessoResponse;
import br.com.siscep.dto.UsuarioRequest;
import br.com.siscep.dto.UsuarioResponse;
import br.com.siscep.exception.ConflitoException;
import br.com.siscep.exception.RecursoNaoEncontradoException;
import br.com.siscep.exception.RegraNegocioException;
import br.com.siscep.repository.RegistroAcessoRepository;
import br.com.siscep.repository.UsuarioRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** Gestao de usuarios e perfis (RF02). */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RegistroAcessoRepository registroAcessoRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UsuarioService(UsuarioRepository usuarioRepository, RegistroAcessoRepository registroAcessoRepository,
                          PasswordEncoder passwordEncoder, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.registroAcessoRepository = registroAcessoRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        LocalDateTime agora = LocalDateTime.now(clock);
        return usuarioRepository.findAll(Sort.by("nome")).stream().map(u -> UsuarioResponse.de(u, agora)).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return UsuarioResponse.de(obter(id), LocalDateTime.now(clock));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest req) {
        if (req.senha() == null || req.senha().isBlank()) {
            throw new RegraNegocioException("Informe a senha inicial do usuário.");
        }
        String email = normalizarEmail(req.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um usuário com o e-mail " + email + ".");
        }
        Usuario u = new Usuario(req.nome().trim(), email, passwordEncoder.encode(req.senha()), req.perfil());
        return UsuarioResponse.de(usuarioRepository.save(u), LocalDateTime.now(clock));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest req, Long idUsuarioLogado) {
        Usuario u = obter(id);
        String email = normalizarEmail(req.email());
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflitoException("Já existe um usuário com o e-mail " + email + ".");
        }
        if (id.equals(idUsuarioLogado) && u.getPerfil() != req.perfil()) {
            throw new RegraNegocioException("Você não pode alterar o seu próprio perfil.");
        }
        u.setNome(req.nome().trim());
        u.setEmail(email);
        u.setPerfil(req.perfil());
        return UsuarioResponse.de(u, LocalDateTime.now(clock));
    }

    @Transactional
    public UsuarioResponse alterarStatus(Long id, boolean ativo, Long idUsuarioLogado) {
        if (!ativo && id.equals(idUsuarioLogado)) {
            throw new RegraNegocioException("Você não pode inativar o seu próprio usuário.");
        }
        Usuario u = obter(id);
        u.setAtivo(ativo);
        return UsuarioResponse.de(u, LocalDateTime.now(clock));
    }

    @Transactional
    public void redefinirSenha(Long id, String novaSenha) {
        Usuario u = obter(id);
        u.setSenhaHash(passwordEncoder.encode(novaSenha));
        u.desbloquear();
    }

    @Transactional
    public UsuarioResponse desbloquear(Long id) {
        Usuario u = obter(id);
        u.desbloquear();
        return UsuarioResponse.de(u, LocalDateTime.now(clock));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<RegistroAcessoResponse> listarAcessos(Pageable pageable) {
        return PaginaResponse.de(registroAcessoRepository.findAllByOrderByDataHoraDesc(pageable),
                RegistroAcessoResponse::de);
    }

    private Usuario obter(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", id));
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
