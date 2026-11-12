package br.com.siscep.service;

import br.com.siscep.domain.Fornecedor;
import br.com.siscep.domain.Movimentacao;
import br.com.siscep.domain.Pastilha;
import br.com.siscep.domain.PerfilUsuario;
import br.com.siscep.domain.TipoMovimentacao;
import br.com.siscep.domain.Usuario;
import br.com.siscep.dto.AjusteRequest;
import br.com.siscep.dto.EntradaRequest;
import br.com.siscep.dto.MovimentacaoResponse;
import br.com.siscep.dto.PaginaResponse;
import br.com.siscep.dto.SaidaRequest;
import br.com.siscep.exception.RecursoNaoEncontradoException;
import br.com.siscep.exception.RegraNegocioException;
import br.com.siscep.repository.MovimentacaoRepository;
import br.com.siscep.repository.PastilhaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Registro de entradas (RF07), saidas (RF08/RF20) e ajustes (RF09) com atualizacao transacional
 * do saldo (RF10/RNF06). A pastilha e lida com bloqueio pessimista, serializando movimentacoes
 * concorrentes do mesmo item; o @Version e a CHECK do banco funcionam como redes de seguranca.
 */
@Service
public class EstoqueService {

    private final PastilhaRepository pastilhaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final FornecedorService fornecedorService;
    private final AlertaService alertaService;
    private final AuditoriaService auditoria;
    private final Clock clock;

    public EstoqueService(PastilhaRepository pastilhaRepository, MovimentacaoRepository movimentacaoRepository,
                          FornecedorService fornecedorService, AlertaService alertaService, AuditoriaService auditoria, Clock clock) {
        this.pastilhaRepository = pastilhaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.fornecedorService = fornecedorService;
        this.alertaService = alertaService;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    @Transactional
    public MovimentacaoResponse registrarEntrada(EntradaRequest req, Usuario usuario) {
        Pastilha p = carregarAtivaComBloqueio(req.pastilhaId());
        Fornecedor fornecedor = fornecedorService.obter(req.fornecedorId());
        if (!fornecedor.isAtivo()) {
            throw new RegraNegocioException("O fornecedor selecionado esta inativo.");
        }
        int anterior = p.getSaldoAtual();
        p.creditar(req.quantidade());
        return salvar(Movimentacao.builder()
                .tipo(TipoMovimentacao.ENTRADA)
                .quantidade(req.quantidade())
                .fornecedor(fornecedor)
                .documentoReferencia(limpar(req.documentoReferencia()))
                .observacao(limpar(req.observacao())), p, usuario, anterior);
    }

    @Transactional
    public MovimentacaoResponse registrarSaida(SaidaRequest req, Usuario usuario) {
        Pastilha p = carregarAtivaComBloqueio(req.pastilhaId());
        int anterior = p.getSaldoAtual();
        p.debitar(req.quantidade());
        return salvar(Movimentacao.builder()
                .tipo(TipoMovimentacao.SAIDA)
                .quantidade(req.quantidade())
                .destino(req.destino().trim())
                .observacao(limpar(req.observacao())), p, usuario, anterior);
    }

    @Transactional
    public MovimentacaoResponse registrarAjuste(AjusteRequest req, Usuario usuario) {
        Pastilha p = carregarAtivaComBloqueio(req.pastilhaId());
        int anterior = p.getSaldoAtual();
        int diferenca = p.ajustarPara(req.contagemFisica());
        return salvar(Movimentacao.builder()
                .tipo(TipoMovimentacao.AJUSTE)
                .quantidade(Math.abs(diferenca))
                .justificativa(req.justificativa().trim()), p, usuario, anterior);
    }

    /** RF14: operador visualiza somente as proprias movimentacoes (matriz 4.5). */
    @Transactional(readOnly = true)
    public PaginaResponse<MovimentacaoResponse> historico(LocalDateTime inicio, LocalDateTime fim, Long pastilhaId,
                                                          TipoMovimentacao tipo, Long usuarioId, Usuario solicitante,
                                                          Pageable pageable) {
        Long filtroUsuario = solicitante.getPerfil() == PerfilUsuario.OPERADOR ? solicitante.getId() : usuarioId;
        Specification<Movimentacao> spec = Specification.allOf(
                MovimentacaoSpecs.aPartirDe(inicio), MovimentacaoSpecs.ate(fim), MovimentacaoSpecs.pastilha(pastilhaId),
                MovimentacaoSpecs.tipo(tipo), MovimentacaoSpecs.usuario(filtroUsuario));
        return PaginaResponse.de(movimentacaoRepository.findAll(spec, pageable), MovimentacaoResponse::de);
    }

    @Transactional(readOnly = true)
    public MovimentacaoResponse buscar(Long id, Usuario solicitante) {
        Movimentacao m = movimentacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Movimentacao", id));
        if (solicitante.getPerfil() == PerfilUsuario.OPERADOR && !m.getUsuario().getId().equals(solicitante.getId())) {
            throw new RecursoNaoEncontradoException("Movimentacao", id);
        }
        return MovimentacaoResponse.de(m);
    }

    private Pastilha carregarAtivaComBloqueio(Long pastilhaId) {
        Pastilha p = pastilhaRepository.findByIdParaAtualizacao(pastilhaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pastilha", pastilhaId));
        if (!p.isAtivo()) {
            throw new RegraNegocioException("A pastilha " + p.getCodigo() + " esta inativa e não aceita movimentações.");
        }
        return p;
    }

    private MovimentacaoResponse salvar(Movimentacao.Builder builder, Pastilha p, Usuario usuario, int anterior) {
        Movimentacao m = builder
                .pastilha(p)
                .usuario(usuario)
                .saldoAnterior(anterior)
                .saldoResultante(p.getSaldoAtual())
                .dataHora(LocalDateTime.now(clock))
                .build();
        Movimentacao salva = movimentacaoRepository.save(m);
        pastilhaRepository.saveAndFlush(p);
        alertaService.avaliar(p);
        auditoria.registrar(usuario, "MOVIMENTACAO_" + salva.getTipo().name(), "movimentacao", salva.getId(),
                p.getCodigo() + ": " + salva.getTipo().name().toLowerCase() + " de " + salva.getQuantidade()
                        + " (saldo " + anterior + " -> " + p.getSaldoAtual() + ")");
        return MovimentacaoResponse.de(salva);
    }

    private static String limpar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
