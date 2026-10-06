package br.com.siscep.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    private final LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 8, 0);

    private Usuario usuario() {
        return new Usuario("Ana", "ana@dda.com.br", "hash", PerfilUsuario.ALMOXARIFE);
    }

    @Test
    void bloqueiaAposTresFalhasConsecutivasPorCincoMinutos() {
        Usuario u = usuario();
        u.registrarFalhaLogin(agora);
        u.registrarFalhaLogin(agora);
        assertThat(u.estaBloqueado(agora)).isFalse();
        u.registrarFalhaLogin(agora);
        assertThat(u.estaBloqueado(agora)).isTrue();
        assertThat(u.estaBloqueado(agora.plusMinutes(4).plusSeconds(59))).isTrue();
        assertThat(u.estaBloqueado(agora.plusMinutes(5))).isFalse();
    }

    @Test
    void loginComSucessoZeraAsTentativas() {
        Usuario u = usuario();
        u.registrarFalhaLogin(agora);
        u.registrarFalhaLogin(agora);
        u.registrarLoginComSucesso(agora);
        u.registrarFalhaLogin(agora);
        u.registrarFalhaLogin(agora);
        assertThat(u.estaBloqueado(agora)).isFalse();
        assertThat(u.getUltimoAcesso()).isEqualTo(agora);
    }

    @Test
    void desbloquearLiberaAContaImediatamente() {
        Usuario u = usuario();
        for (int i = 0; i < 3; i++) {
            u.registrarFalhaLogin(agora);
        }
        u.desbloquear();
        assertThat(u.estaBloqueado(agora)).isFalse();
    }
}
