package br.com.siscep.dto;

import br.com.siscep.domain.StatusEstoque;

import java.math.BigDecimal;

public record RelatorioReposicaoItem(Long pastilhaId, String codigo, String descricao, StatusEstoque situacao,
                                     int saldoAtual, int estoqueMinimo, int quantidadeSugerida, String fornecedor,
                                     String contatoFornecedor, Integer prazoEntregaDias,
                                     BigDecimal consumoMedioDiario, Integer diasParaEsgotar) {
}
