package br.com.siscep.dto;

import br.com.siscep.domain.AlertaEstoque;
import br.com.siscep.domain.StatusEstoque;

import java.time.LocalDateTime;

public record AlertaResponse(Long id, Long pastilhaId, String pastilhaCodigo, String pastilhaDescricao,
                             StatusEstoque situacao, int saldo, int estoqueMinimo, LocalDateTime geradoEm,
                             boolean resolvido, LocalDateTime resolvidoEm) {

    public static AlertaResponse de(AlertaEstoque a) {
        return new AlertaResponse(a.getId(), a.getPastilha().getId(), a.getPastilha().getCodigo(),
                a.getPastilha().getDescricao(), a.getSituacao(), a.getSaldoNoAlerta(), a.getEstoqueMinimoNoAlerta(),
                a.getGeradoEm(), a.isResolvido(), a.getResolvidoEm());
    }
}
