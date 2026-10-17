package br.com.siscep.service;

import br.com.siscep.domain.Pastilha;
import br.com.siscep.domain.StatusEstoque;
import org.springframework.data.jpa.domain.Specification;

/** Filtros da consulta de estoque atual (RF11). */
final class PastilhaSpecs {

    private PastilhaSpecs() {
    }

    static Specification<Pastilha> termo(String termo) {
        if (termo == null || termo.isBlank()) {
            return null;
        }
        String like = "%" + termo.trim().toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("codigo")), like),
                cb.like(cb.lower(root.get("descricao")), like),
                cb.like(cb.lower(cb.coalesce(root.get("especificacao"), "")), like));
    }

    static Specification<Pastilha> fabricante(Long fabricanteId) {
        return fabricanteId == null ? null : (root, q, cb) -> cb.equal(root.get("fabricante").get("id"), fabricanteId);
    }

    static Specification<Pastilha> fornecedor(Long fornecedorId) {
        return fornecedorId == null ? null
                : (root, q, cb) -> cb.equal(root.get("fornecedorPrincipal").get("id"), fornecedorId);
    }

    static Specification<Pastilha> ativo(Boolean ativo) {
        return ativo == null ? null : (root, q, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    static Specification<Pastilha> situacao(StatusEstoque situacao) {
        if (situacao == null) {
            return null;
        }
        return (root, q, cb) -> switch (situacao) {
            case ZERADO -> cb.equal(root.get("saldoAtual"), 0);
            case CRITICO -> cb.and(cb.greaterThan(root.get("saldoAtual"), 0),
                    cb.lessThanOrEqualTo(root.<Integer>get("saldoAtual"), root.get("estoqueMinimo")));
            case NORMAL -> cb.greaterThan(root.<Integer>get("saldoAtual"), root.get("estoqueMinimo"));
        };
    }
}
