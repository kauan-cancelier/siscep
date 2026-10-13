package br.com.siscep.service;

import br.com.siscep.domain.Usuario;
import br.com.siscep.exception.AutenticacaoException;
import br.com.siscep.repository.UsuarioRepository;
import br.com.siscep.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;

@Service
public class UsuarioLogadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioLogadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario carregar(UsuarioAutenticado principal) {
        if (principal == null) {
            throw new AutenticacaoException("Autenticacao necessaria.");
        }
        return usuarioRepository.findById(principal.id())
                .orElseThrow(() -> new AutenticacaoException("Usuario autenticado nao encontrado."));
    }
}
