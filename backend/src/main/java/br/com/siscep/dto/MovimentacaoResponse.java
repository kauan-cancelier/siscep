package br.com.siscep.dto;

import br.com.siscep.domain.Movimentacao;
import br.com.siscep.domain.TipoMovimentacao;

import java.time.LocalDateTime;

public record MovimentacaoResponse(Long id, TipoMovimentacao tipo, Long pastilhaId, String pastilhaCodigo,
                                   String pastilhaDescricao, int quantidade, int saldoAnterior, int saldoResultante,
                                   Long usuarioId, String usuarioNome, Long fornecedorId, String fornecedorNome,
                                   String documentoReferencia, String destino, String justificativa,
                                   String observacao, LocalDateTime dataHora) {

    public static MovimentacaoResponse de(Movimentacao m) {
        var forn = m.getFornecedor();
        return new MovimentacaoResponse(m.getId(), m.getTipo(), m.getPastilha().getId(), m.getPastilha().getCodigo(),
                m.getPastilha().getDescricao(), m.getQuantidade(), m.getSaldoAnterior(), m.getSaldoResultante(),
                m.getUsuario().getId(), m.getUsuario().getNome(),
                forn != null ? forn.getId() : null, forn != null ? forn.getRazaoSocial() : null,
                m.getDocumentoReferencia(), m.getDestino(), m.getJustificativa(), m.getObservacao(), m.getDataHora());
    }
}
