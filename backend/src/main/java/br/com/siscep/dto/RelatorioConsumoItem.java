package br.com.siscep.dto;

import java.math.BigDecimal;

public record RelatorioConsumoItem(Long pastilhaId, String codigo, String descricao, long totalConsumido,
                                   long quantidadeSaidas, BigDecimal mediaDiaria, BigDecimal mediaMensal) {
}
