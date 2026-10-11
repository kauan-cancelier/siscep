package br.com.siscep.dto;

import br.com.siscep.domain.HistoricoEstoqueMinimo;

import java.time.LocalDateTime;

public record HistoricoEstoqueMinimoResponse(Long id, int valorAnterior, int valorNovo, String usuarioNome,
                                             LocalDateTime alteradoEm) {

    public static HistoricoEstoqueMinimoResponse de(HistoricoEstoqueMinimo h) {
        return new HistoricoEstoqueMinimoResponse(h.getId(), h.getValorAnterior(), h.getValorNovo(),
                h.getUsuario().getNome(), h.getAlteradoEm());
    }
}
