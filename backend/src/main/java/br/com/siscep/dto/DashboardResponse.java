package br.com.siscep.dto;

import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(LocalDate inicio, LocalDate fim, long totalItens, long itensCriticos,
                                long itensZerados, long itensNormais, long saldoTotal, long movimentacoesPeriodo,
                                long entradasPeriodo, long saidasPeriodo, long ajustesPeriodo, long alertasAbertos,
                                List<ConsumoItem> maiorConsumo, List<MovimentoDia> movimentoDiario,
                                List<PastilhaResponse> itensEmAlerta) {

    public record ConsumoItem(Long pastilhaId, String codigo, String descricao, long total) {
    }

    public record MovimentoDia(LocalDate dia, long entradas, long saidas) {
    }
}
