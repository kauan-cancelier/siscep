package br.com.siscep.dto;

import br.com.siscep.domain.Auditoria;

import java.time.LocalDateTime;

public record AuditoriaResponse(Long id, String usuarioNome, String acao, String entidade, Long entidadeId,
                                String detalhes, LocalDateTime criadoEm) {

    public static AuditoriaResponse de(Auditoria a) {
        return new AuditoriaResponse(a.getId(), a.getUsuarioNome(), a.getAcao(), a.getEntidade(),
                a.getEntidadeId(), a.getDetalhes(), a.getCriadoEm());
    }
}
