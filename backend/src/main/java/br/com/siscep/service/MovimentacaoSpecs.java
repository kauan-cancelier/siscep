package br.com.siscep.service;

import br.com.siscep.domain.Movimentacao;
import br.com.siscep.domain.TipoMovimentacao;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/** Filtros do historico de movimentacoes (RF14). */
final class MovimentacaoSpecs {

    private MovimentacaoSpecs() {
    }

    static Specification<Movimentacao> aPartirDe(LocalDateTime inicio) {
        return inicio == null ? null : (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("dataHora"), inicio);
    }

    static Specification<Movimentacao> ate(LocalDateTime fim) {
        return fim == null ? null : (r, q, cb) -> cb.lessThanOrEqualTo(r.get("dataHora"), fim);
    }

    static Specification<Movimentacao> pastilha(Long pastilhaId) {
        return pastilhaId == null ? null : (r, q, cb) -> cb.equal(r.get("pastilha").get("id"), pastilhaId);
    }

    static Specification<Movimentacao> tipo(TipoMovimentacao tipo) {
        return tipo == null ? null : (r, q, cb) -> cb.equal(r.get("tipo"), tipo);
    }

    static Specification<Movimentacao> usuario(Long usuarioId) {
        return usuarioId == null ? null : (r, q, cb) -> cb.equal(r.get("usuario").get("id"), usuarioId);
    }
}
