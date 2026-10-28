package br.com.siscep.service;

import br.com.siscep.domain.Pastilha;
import br.com.siscep.domain.StatusEstoque;
import br.com.siscep.exception.RegraNegocioException;
import br.com.siscep.repository.MovimentacaoRepository;
import br.com.siscep.repository.PastilhaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private MovimentacaoRepository movimentacaoRepository;
    @Mock
    private PastilhaRepository pastilhaRepository;
    @InjectMocks
    private RelatorioService service;

    record Consumo(Long pastilhaId, String codigo, String descricao, Long total, Long quantidadeSaidas)
            implements MovimentacaoRepository.ConsumoPorPastilha {
        public Long getPastilhaId() { return pastilhaId; }
        public String getCodigo() { return codigo; }
        public String getDescricao() { return descricao; }
        public Long getTotal() { return total; }
        public Long getQuantidadeSaidas() { return quantidadeSaidas; }
    }

    @Test
    void consumoCalculaMediasDiariaEMensal() {
        when(movimentacaoRepository.consumoPorPastilha(any(), any(), any()))
                .thenReturn(List.of(new Consumo(1L, "P1", "Pastilha 1", 60L, 12L)));

        var itens = service.consumo(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertThat(itens).hasSize(1);
        assertThat(itens.get(0).mediaDiaria()).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat(itens.get(0).mediaMensal()).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void periodoInvertidoEhRejeitado() {
        assertThatThrownBy(() -> service.consumo(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 1)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void reposicaoTrazSugestaoEFornecedor() {
        Pastilha p = Fixtures.pastilha(1, 2, 10);
        var f = Fixtures.fornecedor(2, true);
        f.setContato("Joao");
        f.setTelefone("49 3333-0000");
        f.setPrazoMedioEntregaDias(7);
        p.setFornecedorPrincipal(f);
        when(pastilhaRepository.listarParaReposicao()).thenReturn(List.of(p));

        var r = service.reposicao().get(0);

        assertThat(r.situacao()).isEqualTo(StatusEstoque.CRITICO);
        assertThat(r.quantidadeSugerida()).isEqualTo(18);
        assertThat(r.fornecedor()).isEqualTo("Fornecedor 2");
        assertThat(r.contatoFornecedor()).isEqualTo("Joao | 49 3333-0000");
        assertThat(r.prazoEntregaDias()).isEqualTo(7);
    }
}
