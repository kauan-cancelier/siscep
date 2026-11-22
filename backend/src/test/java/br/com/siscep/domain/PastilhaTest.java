package br.com.siscep.domain;

import br.com.siscep.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PastilhaTest {

    private Pastilha pastilha(int saldo, int minimo) {
        Pastilha p = new Pastilha("CNMG120408", "Pastilha de torneamento", minimo);
        if (saldo > 0) {
            p.creditar(saldo);
        }
        return p;
    }

    @Test
    void entradaSomaAoSaldo() {
        Pastilha p = pastilha(10, 5);
        p.creditar(15);
        assertThat(p.getSaldoAtual()).isEqualTo(25);
    }

    @Test
    void saidaSubtraiDoSaldo() {
        Pastilha p = pastilha(10, 5);
        p.debitar(4);
        assertThat(p.getSaldoAtual()).isEqualTo(6);
    }

    @Test
    void saidaSuperiorAoSaldoEhBloqueadaSemAlterarSaldo() {
        Pastilha p = pastilha(3, 5);
        assertThatThrownBy(() -> p.debitar(4))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Saldo insuficiente");
        assertThat(p.getSaldoAtual()).isEqualTo(3);
    }

    @Test
    void saidaDeTodoOSaldoZeraOItem() {
        Pastilha p = pastilha(3, 5);
        p.debitar(3);
        assertThat(p.getSaldoAtual()).isZero();
        assertThat(p.situacao()).isEqualTo(StatusEstoque.ZERADO);
    }

    @ParameterizedTest
    @CsvSource({"0", "-1"})
    void quantidadeDeveSerMaiorQueZero(int quantidade) {
        Pastilha p = pastilha(10, 5);
        assertThatThrownBy(() -> p.creditar(quantidade)).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> p.debitar(quantidade)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void ajusteGravaADiferencaEntreContagemESaldo() {
        Pastilha p = pastilha(10, 5);
        assertThat(p.ajustarPara(7)).isEqualTo(-3);
        assertThat(p.getSaldoAtual()).isEqualTo(7);
        assertThat(p.ajustarPara(12)).isEqualTo(5);
    }

    @Test
    void ajusteNaoAceitaContagemNegativaNemIgualAoSaldo() {
        Pastilha p = pastilha(10, 5);
        assertThatThrownBy(() -> p.ajustarPara(-1)).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> p.ajustarPara(10)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void estoqueMinimoNaoPodeSerNegativo() {
        Pastilha p = pastilha(10, 5);
        assertThatThrownBy(() -> p.definirEstoqueMinimo(-1)).isInstanceOf(RegraNegocioException.class);
        p.definirEstoqueMinimo(0);
        assertThat(p.getEstoqueMinimo()).isZero();
    }

    @ParameterizedTest(name = "saldo {0}, mínimo {1} -> {2}")
    @CsvSource({"0,5,ZERADO", "0,0,ZERADO", "1,5,CRITICO", "5,5,CRITICO", "6,5,NORMAL", "1,0,NORMAL"})
    void situacaoSegueRn08(int saldo, int minimo, StatusEstoque esperado) {
        assertThat(pastilha(saldo, minimo).situacao()).isEqualTo(esperado);
    }

    @ParameterizedTest(name = "saldo {0}, mínimo {1} -> sugerir {2}")
    @CsvSource({"2,10,18", "10,10,10", "0,10,20", "0,0,1", "11,10,0", "19,10,0"})
    void quantidadeSugeridaSegueRn12(int saldo, int minimo, int esperado) {
        assertThat(pastilha(saldo, minimo).quantidadeSugeridaCompra()).isEqualTo(esperado);
    }
}
