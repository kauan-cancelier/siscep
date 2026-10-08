package br.com.siscep.security;

import br.com.siscep.domain.PerfilUsuario;
import br.com.siscep.domain.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/** Principal disponivel no SecurityContext apos a validacao do JWT. */
public record UsuarioAutenticado(Long id, String nome, String email, PerfilUsuario perfil) {

    public static UsuarioAutenticado de(Usuario u) {
        return new UsuarioAutenticado(u.getId(), u.getNome(), u.getEmail(), u.getPerfil());
    }

    public List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }
}
