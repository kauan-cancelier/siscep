package br.com.siscep.dto;

import br.com.siscep.domain.PastilhaFornecedor;

import java.math.BigDecimal;

public record PastilhaFornecedorResponse(Long fornecedorId, String razaoSocial, BigDecimal precoReferencia,
                                         Integer prazoEntregaDias) {

    public static PastilhaFornecedorResponse de(PastilhaFornecedor pf) {
        return new PastilhaFornecedorResponse(pf.getFornecedor().getId(), pf.getFornecedor().getRazaoSocial(),
                pf.getPrecoReferencia(), pf.getPrazoEntregaDias());
    }
}
