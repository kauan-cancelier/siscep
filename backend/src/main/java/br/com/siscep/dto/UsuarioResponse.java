package br.com.siscep.dto;

import br.com.siscep.domain.PerfilUsuario;
import br.com.siscep.domain.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(Long id, String nome, String email, PerfilUsuario perfil, boolean ativo,
                              boolean bloqueado, LocalDateTime ultimoAcesso, LocalDateTime criadoEm) {

    public static UsuarioResponse de(Usuario u, LocalDateTime agora) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.isAtivo(),
                u.estaBloqueado(agora), u.getUltimoAcesso(), u.getCriadoEm());
    }
}
