package br.com.siscep.service;

import br.com.siscep.domain.Movimentacao;
import br.com.siscep.domain.Pastilha;
import br.com.siscep.domain.PerfilUsuario;
import br.com.siscep.domain.TipoMovimentacao;
import br.com.siscep.domain.Usuario;
import br.com.siscep.dto.AjusteRequest;
import br.com.siscep.dto.EntradaRequest;
import br.com.siscep.dto.MovimentacaoResponse;
import br.com.siscep.dto.SaidaRequest;
import br.com.siscep.exception.RecursoNaoEncontradoException;
import br.com.siscep.exception.RegraNegocioException;
import br.com.siscep.repository.MovimentacaoRepository;
import br.com.siscep.repository.PastilhaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private PastilhaRepository pastilhaRepository;
    @Mock
    private MovimentacaoRepository movimentacaoRepository;
    @Mock
    private FornecedorService fornecedorService;
    @Mock
    private AlertaService alertaService;

    private EstoqueService service;
    private final Usuario almoxarife = Fixtures.usuario(1, PerfilUsuario.ALMOXARIFE);

    @BeforeEach
    void setUp() {
        service = new EstoqueService(pastilhaRepository, movimentacaoRepository, fornecedorService, alertaService,
                Fixtures.CLOCK);
    }

    private Pastilha comPastilha(int saldo, int minimo) {
        Pastilha p = Fixtures.pastilha(10, saldo, minimo);
        when(pastilhaRepository.findByIdParaAtualizacao(10L)).thenReturn(Optional.of(p));
        return p;
    }

    private void salvarDevolveArgumento() {
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void entradaCreditaSaldoERegistraMovimentacaoComFornecedor() {
        Pastilha p = comPastilha(5, 10);
        when(fornecedorService.obter(3L)).thenReturn(Fixtures.fornecedor(3, true));
        salvarDevolveArgumento();

        MovimentacaoResponse r = service.registrarEntrada(new EntradaRequest(10L, 20, 3L, "NF 123", " "), almoxarife);

        assertThat(p.getSaldoAtual()).isEqualTo(25);
        assertThat(r.tipo()).isEqualTo(TipoMovimentacao.ENTRADA);
        assertThat(r.saldoAnterior()).isEqualTo(5);
        assertThat(r.saldoResultante()).isEqualTo(25);
        assertThat(r.fornecedorId()).isEqualTo(3L);
        assertThat(r.documentoReferencia()).isEqualTo("NF 123");
        assertThat(r.observacao()).isNull();
        assertThat(r.dataHora()).isEqualTo(LocalDateTime.now(Fixtures.CLOCK));
        verify(alertaService).avaliar(p);
    }

    @Test
    void entradaComFornecedorInativoEhRejeitada() {
        Pastilha p = comPastilha(5, 10);
        when(fornecedorService.obter(3L)).thenReturn(Fixtures.fornecedor(3, false));

        assertThatThrownBy(() -> service.registrarEntrada(new EntradaRequest(10L, 1, 3L, null, null), almoxarife))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(p.getSaldoAtual()).isEqualTo(5);
        verify(movimentacaoRepository, never()).save(any());
    }

    @Test
    void saidaDebitaSaldoEGuardaDestino() {
        Pastilha p = comPastilha(8, 5);
        salvarDevolveArgumento();

        MovimentacaoResponse r = service.registrarSaida(new SaidaRequest(10L, 3, " OP-4471 ", null), almoxarife);

        assertThat(p.getSaldoAtual()).isEqualTo(5);
        assertThat(r.destino()).isEqualTo("OP-4471");
        assertThat(r.saldoResultante()).isEqualTo(5);
        verify(alertaService).avaliar(p);
    }

    @Test
    void saidaAcimaDoSaldoNaoGravaNada() {
        Pastilha p = comPastilha(2, 5);

        assertThatThrownBy(() -> service.registrarSaida(new SaidaRequest(10L, 3, "Torno 2", null), almoxarife))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("disponível 2");
        assertThat(p.getSaldoAtual()).isEqualTo(2);
        verify(movimentacaoRepository, never()).save(any());
        verify(pastilhaRepository, never()).saveAndFlush(any());
    }

    @Test
    void ajusteRegistraDiferencaAbsolutaEJustificativa() {
        Pastilha p = comPastilha(10, 5);
        salvarDevolveArgumento();
        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);

        service.registrarAjuste(new AjusteRequest(10L, 6, "Inventário mensal"), almoxarife);

        verify(movimentacaoRepository).save(captor.capture());
        Movimentacao m = captor.getValue();
        assertThat(m.getTipo()).isEqualTo(TipoMovimentacao.AJUSTE);
        assertThat(m.getQuantidade()).isEqualTo(4);
        assertThat(m.getSaldoAnterior()).isEqualTo(10);
        assertThat(m.getSaldoResultante()).isEqualTo(6);
        assertThat(m.getJustificativa()).isEqualTo("Inventário mensal");
        assertThat(p.getSaldoAtual()).isEqualTo(6);
    }

    @Test
    void pastilhaInativaNaoAceitaMovimentacao() {
        Pastilha p = comPastilha(10, 5);
        p.setAtivo(false);

        assertThatThrownBy(() -> service.registrarSaida(new SaidaRequest(10L, 1, "Setor A", null), almoxarife))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativa");
    }

    @Test
    void pastilhaInexistenteRetornaNaoEncontrado() {
        when(pastilhaRepository.findByIdParaAtualizacao(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.registrarSaida(new SaidaRequest(99L, 1, "Setor A", null), almoxarife))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void historicoDoOperadorIgnoraFiltroDeOutroUsuario() {
        Usuario operador = Fixtures.usuario(7, PerfilUsuario.OPERADOR);
        Page<Movimentacao> vazio = new PageImpl<>(List.of());
        when(movimentacaoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(vazio);

        var r = service.historico(null, null, null, null, 1L, operador, Pageable.unpaged());

        assertThat(r.conteudo()).isEmpty();
    }

    @Test
    void operadorNaoAcessaMovimentacaoDeOutroUsuario() {
        Usuario operador = Fixtures.usuario(7, PerfilUsuario.OPERADOR);
        Movimentacao m = Movimentacao.builder().tipo(TipoMovimentacao.SAIDA).quantidade(1)
                .pastilha(Fixtures.pastilha(1, 0, 0)).usuario(almoxarife).saldoAnterior(1).saldoResultante(0)
                .destino("x").dataHora(LocalDateTime.now(Fixtures.CLOCK)).build();
        when(movimentacaoRepository.findById(5L)).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> service.buscar(5L, operador)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThat(service.buscar(5L, almoxarife).quantidade()).isEqualTo(1);
    }
}
