package br.com.siscep.service;

import br.com.siscep.domain.Fornecedor;
import br.com.siscep.domain.Pastilha;
import br.com.siscep.domain.PerfilUsuario;
import br.com.siscep.domain.Usuario;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

final class Fixtures {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    private Fixtures() {
    }

    static Usuario usuario(long id, PerfilUsuario perfil) {
        Usuario u = new Usuario("Usuario " + id, "u" + id + "@dda.com.br", "hash", perfil);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    static Pastilha pastilha(long id, int saldo, int minimo) {
        Pastilha p = new Pastilha("P" + id, "Pastilha " + id, minimo);
        if (saldo > 0) {
            p.creditar(saldo);
        }
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    static Fornecedor fornecedor(long id, boolean ativo) {
        Fornecedor f = new Fornecedor("Fornecedor " + id, null);
        f.setAtivo(ativo);
        ReflectionTestUtils.setField(f, "id", id);
        return f;
    }
}
