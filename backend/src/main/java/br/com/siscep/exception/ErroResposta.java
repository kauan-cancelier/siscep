package br.com.siscep.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErroResposta(int status, String mensagem, List<String> detalhes, LocalDateTime dataHora) {

    public static ErroResposta de(int status, String mensagem) {
        return new ErroResposta(status, mensagem, List.of(), LocalDateTime.now());
    }

    public static ErroResposta de(int status, String mensagem, List<String> detalhes) {
        return new ErroResposta(status, mensagem, detalhes, LocalDateTime.now());
    }
}
