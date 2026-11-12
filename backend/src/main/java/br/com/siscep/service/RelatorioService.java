package br.com.siscep.service;

import br.com.siscep.domain.Fornecedor;
import br.com.siscep.domain.Pastilha;
import br.com.siscep.dto.RelatorioConsumoItem;
import br.com.siscep.dto.RelatorioReposicaoItem;
import br.com.siscep.repository.MovimentacaoRepository;
import br.com.siscep.repository.MovimentacaoRepository.ConsumoPorPastilha;
import br.com.siscep.repository.PastilhaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Relatorio de consumo (RF17) e relatorio de necessidade de reposicao (RF18 / RN12). */
@Service
public class RelatorioService {

    private static final BigDecimal DIAS_MES = new BigDecimal("30");

    private final MovimentacaoRepository movimentacaoRepository;
    private final PastilhaRepository pastilhaRepository;

    public RelatorioService(MovimentacaoRepository movimentacaoRepository, PastilhaRepository pastilhaRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.pastilhaRepository = pastilhaRepository;
    }

    @Transactional(readOnly = true)
    public List<RelatorioConsumoItem> consumo(LocalDate inicio, LocalDate fim) {
        Periodo.validar(inicio, fim);
        BigDecimal dias = BigDecimal.valueOf(Periodo.dias(inicio, fim));
        return movimentacaoRepository.consumoPorPastilha(inicio.atStartOfDay(), fim.atTime(LocalTime.MAX),
                        Pageable.unpaged()).stream()
                .map(c -> {
                    BigDecimal total = BigDecimal.valueOf(c.getTotal());
                    BigDecimal mediaDiaria = total.divide(dias, 2, RoundingMode.HALF_UP);
                    BigDecimal mediaMensal = total.multiply(DIAS_MES).divide(dias, 2, RoundingMode.HALF_UP);
                    return new RelatorioConsumoItem(c.getPastilhaId(), c.getCodigo(), c.getDescricao(), c.getTotal(),
                            c.getQuantidadeSaidas(), mediaDiaria, mediaMensal);
                })
                .toList();
    }

    private static final int JANELA_PREVISAO_DIAS = 30;

    /** RF18 / RN12: itens em situacao critica ou zerada com quantidade sugerida e previsao de esgotamento. */
    @Transactional(readOnly = true)
    public List<RelatorioReposicaoItem> reposicao() {
        LocalDate hoje = LocalDate.now();
        Map<Long, Long> consumo = movimentacaoRepository
                .consumoPorPastilha(hoje.minusDays(JANELA_PREVISAO_DIAS).atStartOfDay(),
                        hoje.atTime(LocalTime.MAX), Pageable.unpaged())
                .stream().collect(Collectors.toMap(ConsumoPorPastilha::getPastilhaId, ConsumoPorPastilha::getTotal));
        return pastilhaRepository.listarParaReposicao().stream()
                .map(p -> itemReposicao(p, consumo.getOrDefault(p.getId(), 0L))).toList();
    }

    private static RelatorioReposicaoItem itemReposicao(Pastilha p, long consumidoJanela) {
        Fornecedor f = p.getFornecedorPrincipal();
        String contato = null;
        Integer prazo = null;
        if (f != null) {
            contato = juntar(f.getContato(), f.getTelefone(), f.getEmail());
            prazo = f.getPrazoMedioEntregaDias();
        }
        BigDecimal mediaDiaria = BigDecimal.valueOf(consumidoJanela)
                .divide(BigDecimal.valueOf(JANELA_PREVISAO_DIAS), 2, RoundingMode.HALF_UP);
        Integer diasParaEsgotar = mediaDiaria.signum() > 0
                ? BigDecimal.valueOf(p.getSaldoAtual()).divide(mediaDiaria, 0, RoundingMode.CEILING).intValue()
                : null;
        return new RelatorioReposicaoItem(p.getId(), p.getCodigo(), p.getDescricao(), p.situacao(), p.getSaldoAtual(),
                p.getEstoqueMinimo(), p.quantidadeSugeridaCompra(), f != null ? f.getRazaoSocial() : null, contato,
                prazo, mediaDiaria, diasParaEsgotar);
    }

    private static String juntar(String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String s : partes) {
            if (s != null && !s.isBlank()) {
                if (!sb.isEmpty()) {
                    sb.append(" | ");
                }
                sb.append(s.trim());
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }
}
