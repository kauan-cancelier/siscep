package br.com.siscep.dto;

import br.com.siscep.domain.Fabricante;
import br.com.siscep.domain.Fornecedor;
import br.com.siscep.domain.Pastilha;
import br.com.siscep.domain.StatusEstoque;

public record PastilhaResponse(Long id, String codigo, String descricao, String especificacao,
                               String unidadeMedida, Long fabricanteId, String fabricanteNome,
                               Long fornecedorPrincipalId, String fornecedorPrincipalNome, int estoqueMinimo,
                               int saldoAtual, StatusEstoque situacao, boolean ativo) {

    public static PastilhaResponse de(Pastilha p) {
        Fabricante fab = p.getFabricante();
        Fornecedor forn = p.getFornecedorPrincipal();
        return new PastilhaResponse(p.getId(), p.getCodigo(), p.getDescricao(), p.getEspecificacao(),
                p.getUnidadeMedida(),
                fab != null ? fab.getId() : null, fab != null ? fab.getNome() : null,
                forn != null ? forn.getId() : null, forn != null ? forn.getRazaoSocial() : null,
                p.getEstoqueMinimo(), p.getSaldoAtual(), p.situacao(), p.isAtivo());
    }
}
