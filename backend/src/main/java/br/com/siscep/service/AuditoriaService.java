package br.com.siscep.service;

import br.com.siscep.domain.Auditoria;
import br.com.siscep.domain.Usuario;
import br.com.siscep.dto.AuditoriaResponse;
import br.com.siscep.dto.PaginaResponse;
import br.com.siscep.repository.AuditoriaRepository;
import br.com.siscep.security.UsuarioAutenticado;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/** Trilha de auditoria (RNF de rastreabilidade): registra quem executou cada acao de escrita. */
@Service
public class AuditoriaService {

    private final AuditoriaRepository repository;
    private final Clock clock;

    public AuditoriaService(AuditoriaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Registra a acao atribuindo-a ao usuario autenticado; participa da transacao do chamador. */
    @Transactional(propagation = Propagation.REQUIRED)
    public void registrar(String acao, String entidade, Long entidadeId, String detalhes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long usuarioId = null;
        String usuarioNome = "sistema";
        if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado u) {
            usuarioId = u.id();
            usuarioNome = u.nome();
        }
        repository.save(new Auditoria(usuarioId, usuarioNome, acao, entidade, entidadeId, detalhes,
                LocalDateTime.now(clock)));
    }

    /** Variante para chamadas que ja carregaram a entidade Usuario. */
    @Transactional(propagation = Propagation.REQUIRED)
    public void registrar(Usuario usuario, String acao, String entidade, Long entidadeId, String detalhes) {
        repository.save(new Auditoria(usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getNome() : "sistema", acao, entidade, entidadeId, detalhes,
                LocalDateTime.now(clock)));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<AuditoriaResponse> listar(Pageable pageable) {
        return PaginaResponse.de(repository.findAllByOrderByCriadoEmDesc(pageable), AuditoriaResponse::de);
    }
}
