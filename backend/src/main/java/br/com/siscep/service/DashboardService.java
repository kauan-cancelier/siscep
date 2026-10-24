package br.com.siscep.service;

import br.com.siscep.domain.TipoMovimentacao;
import br.com.siscep.dto.DashboardResponse;
import br.com.siscep.dto.PastilhaResponse;
import br.com.siscep.repository.AlertaEstoqueRepository;
import br.com.siscep.repository.MovimentacaoRepository;
import br.com.siscep.repository.PastilhaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Painel de indicadores (RF15). */
@Service
public class DashboardService {

    static final int DIAS_PADRAO = 30;

    private final PastilhaRepository pastilhaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final AlertaEstoqueRepository alertaRepository;
    private final Clock clock;

    public DashboardService(PastilhaRepository pastilhaRepository, MovimentacaoRepository movimentacaoRepository,
                            AlertaEstoqueRepository alertaRepository, Clock clock) {
        this.pastilhaRepository = pastilhaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.alertaRepository = alertaRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardResponse gerar(LocalDate inicio, LocalDate fim) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDate f = fim != null ? fim : hoje;
        LocalDate i = inicio != null ? inicio : f.minusDays(DIAS_PADRAO - 1L);
        Periodo.validar(i, f);
        LocalDateTime de = i.atStartOfDay();
        LocalDateTime ate = f.atTime(LocalTime.MAX);

        long total = pastilhaRepository.countByAtivoTrue();
        long criticos = pastilhaRepository.contarCriticas();
        long zerados = pastilhaRepository.contarZeradas();
        var consumo = movimentacaoRepository.consumoPorPastilha(de, ate, PageRequest.of(0, 10)).stream()
                .map(c -> new DashboardResponse.ConsumoItem(c.getPastilhaId(), c.getCodigo(), c.getDescricao(),
                        c.getTotal()))
                .toList();
        var diario = movimentacaoRepository.movimentoDiario(de, ate).stream()
                .map(d -> new DashboardResponse.MovimentoDia(d.getDia(), d.getEntradas(), d.getSaidas()))
                .toList();
        var emAlerta = pastilhaRepository.listarParaReposicao().stream().limit(10).map(PastilhaResponse::de).toList();

        return new DashboardResponse(i, f, total, criticos, zerados, total - criticos - zerados,
                pastilhaRepository.somarSaldo(),
                movimentacaoRepository.countByDataHoraBetween(de, ate),
                movimentacaoRepository.countByTipoAndDataHoraBetween(TipoMovimentacao.ENTRADA, de, ate),
                movimentacaoRepository.countByTipoAndDataHoraBetween(TipoMovimentacao.SAIDA, de, ate),
                movimentacaoRepository.countByTipoAndDataHoraBetween(TipoMovimentacao.AJUSTE, de, ate),
                alertaRepository.countByResolvidoFalse(), consumo, diario, emAlerta);
    }
}
