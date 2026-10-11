package br.com.siscep.dto;

import br.com.siscep.domain.RegistroAcesso;

import java.time.LocalDateTime;

public record RegistroAcessoResponse(Long id, String email, boolean sucesso, String motivo, String ip,
                                     LocalDateTime dataHora) {

    public static RegistroAcessoResponse de(RegistroAcesso r) {
        return new RegistroAcessoResponse(r.getId(), r.getEmail(), r.isSucesso(), r.getMotivo(), r.getIp(),
                r.getDataHora());
    }
}
