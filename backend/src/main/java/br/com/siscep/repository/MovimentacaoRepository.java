package br.com.siscep.repository;

import br.com.siscep.domain.Movimentacao;
import br.com.siscep.domain.TipoMovimentacao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long>, JpaSpecificationExecutor<Movimentacao> {

    boolean existsByPastilhaId(Long pastilhaId);

    boolean existsByFornecedorId(Long fornecedorId);

    boolean existsByPastilhaFabricanteId(Long fabricanteId);

    boolean existsByPastilhaFornecedorPrincipalId(Long fornecedorId);

    long countByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    long countByTipoAndDataHoraBetween(TipoMovimentacao tipo, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
            select m.pastilha.id as pastilhaId, m.pastilha.codigo as codigo, m.pastilha.descricao as descricao,
                   sum(m.quantidade) as total, count(m) as quantidadeSaidas
            from Movimentacao m
            where m.tipo = br.com.siscep.domain.TipoMovimentacao.SAIDA
              and m.dataHora between :inicio and :fim
            group by m.pastilha.id, m.pastilha.codigo, m.pastilha.descricao
            order by sum(m.quantidade) desc
            """)
    List<ConsumoPorPastilha> consumoPorPastilha(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    @Query("""
            select cast(m.dataHora as LocalDate) as dia,
                   sum(case when m.tipo = br.com.siscep.domain.TipoMovimentacao.ENTRADA then m.quantidade else 0 end) as entradas,
                   sum(case when m.tipo = br.com.siscep.domain.TipoMovimentacao.SAIDA then m.quantidade else 0 end) as saidas
            from Movimentacao m
            where m.dataHora between :inicio and :fim
            group by cast(m.dataHora as LocalDate)
            order by cast(m.dataHora as LocalDate)
            """)
    List<MovimentoDiario> movimentoDiario(LocalDateTime inicio, LocalDateTime fim);

    interface ConsumoPorPastilha {
        Long getPastilhaId();

        String getCodigo();

        String getDescricao();

        Long getTotal();

        Long getQuantidadeSaidas();
    }

    interface MovimentoDiario {
        java.time.LocalDate getDia();

        Long getEntradas();

        Long getSaidas();
    }
}
