package br.com.siscep.service;

import br.com.siscep.domain.Fabricante;
import br.com.siscep.domain.Fornecedor;
import br.com.siscep.domain.HistoricoEstoqueMinimo;
import br.com.siscep.domain.Pastilha;
import br.com.siscep.domain.PastilhaFornecedor;
import br.com.siscep.domain.PastilhaFornecedorId;
import br.com.siscep.domain.StatusEstoque;
import br.com.siscep.domain.Usuario;
import br.com.siscep.dto.HistoricoEstoqueMinimoResponse;
import br.com.siscep.dto.PaginaResponse;
import br.com.siscep.dto.PastilhaFornecedorRequest;
import br.com.siscep.dto.PastilhaFornecedorResponse;
import br.com.siscep.dto.PastilhaRequest;
import br.com.siscep.dto.PastilhaResponse;
import br.com.siscep.exception.ConflitoException;
import br.com.siscep.exception.RecursoNaoEncontradoException;
import br.com.siscep.exception.RegraNegocioException;
import br.com.siscep.repository.HistoricoEstoqueMinimoRepository;
import br.com.siscep.repository.MovimentacaoRepository;
import br.com.siscep.repository.PastilhaFornecedorRepository;
import br.com.siscep.repository.PastilhaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** Cadastro de pastilhas (RF03), estoque minimo (RF06), consulta de estoque (RF11) e fornecedores alternativos. */
@Service
public class PastilhaService {

    private final PastilhaRepository pastilhaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final HistoricoEstoqueMinimoRepository historicoMinimoRepository;
    private final PastilhaFornecedorRepository pastilhaFornecedorRepository;
    private final FabricanteService fabricanteService;
    private final FornecedorService fornecedorService;
    private final AlertaService alertaService;
    private final Clock clock;

    public PastilhaService(PastilhaRepository pastilhaRepository, MovimentacaoRepository movimentacaoRepository,
                           HistoricoEstoqueMinimoRepository historicoMinimoRepository,
                           PastilhaFornecedorRepository pastilhaFornecedorRepository,
                           FabricanteService fabricanteService, FornecedorService fornecedorService,
                           AlertaService alertaService, Clock clock) {
        this.pastilhaRepository = pastilhaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.historicoMinimoRepository = historicoMinimoRepository;
        this.pastilhaFornecedorRepository = pastilhaFornecedorRepository;
        this.fabricanteService = fabricanteService;
        this.fornecedorService = fornecedorService;
        this.alertaService = alertaService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PastilhaResponse> consultar(String termo, Long fabricanteId, Long fornecedorId,
                                                      StatusEstoque situacao, Boolean ativo, Pageable pageable) {
        Specification<Pastilha> spec = Specification.allOf(
                PastilhaSpecs.termo(termo), PastilhaSpecs.fabricante(fabricanteId),
                PastilhaSpecs.fornecedor(fornecedorId), PastilhaSpecs.situacao(situacao), PastilhaSpecs.ativo(ativo));
        return PaginaResponse.de(pastilhaRepository.findAll(spec, pageable), PastilhaResponse::de);
    }

    @Transactional(readOnly = true)
    public PastilhaResponse buscar(Long id) {
        return PastilhaResponse.de(obter(id));
    }

    @Transactional
    public PastilhaResponse criar(PastilhaRequest req, Usuario usuario) {
        String codigo = normalizarCodigo(req.codigo());
        if (pastilhaRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ConflitoException("Já existe uma pastilha com o código " + codigo + ".");
        }
        Pastilha p = new Pastilha(codigo, req.descricao().trim(), req.estoqueMinimo());
        preencher(p, req);
        Pastilha salva = pastilhaRepository.save(p);
        alertaService.avaliar(salva);
        return PastilhaResponse.de(salva);
    }

    @Transactional
    public PastilhaResponse atualizar(Long id, PastilhaRequest req, Usuario usuario) {
        Pastilha p = obter(id);
        String codigo = normalizarCodigo(req.codigo());
        if (!codigo.equalsIgnoreCase(p.getCodigo())) {
            if (movimentacaoRepository.existsByPastilhaId(id)) {
                throw new RegraNegocioException(
                        "O código da pastilha não pode ser alterado após o primeiro registro de movimentação.");
            }
            if (pastilhaRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
                throw new ConflitoException("Já existe uma pastilha com o código " + codigo + ".");
            }
            p.setCodigo(codigo);
        }
        p.setDescricao(req.descricao().trim());
        preencher(p, req);
        alterarMinimo(p, req.estoqueMinimo(), usuario);
        return PastilhaResponse.de(p);
    }

    @Transactional
    public PastilhaResponse definirEstoqueMinimo(Long id, int estoqueMinimo, Usuario usuario) {
        Pastilha p = obter(id);
        alterarMinimo(p, estoqueMinimo, usuario);
        return PastilhaResponse.de(p);
    }

    @Transactional
    public PastilhaResponse alterarStatus(Long id, boolean ativo) {
        Pastilha p = obter(id);
        p.setAtivo(ativo);
        alertaService.avaliar(p);
        return PastilhaResponse.de(p);
    }

    @Transactional
    public void excluir(Long id) {
        Pastilha p = obter(id);
        if (movimentacaoRepository.existsByPastilhaId(id)) {
            throw new RegraNegocioException(
                    "A pastilha possui movimentações registradas e não pode ser excluída; inative-a.");
        }
        pastilhaFornecedorRepository.deleteAll(pastilhaFornecedorRepository.findByPastilhaId(id));
        pastilhaRepository.delete(p);
    }

    @Transactional(readOnly = true)
    public List<HistoricoEstoqueMinimoResponse> historicoEstoqueMinimo(Long id) {
        obter(id);
        return historicoMinimoRepository.findByPastilhaIdOrderByAlteradoEmDesc(id).stream()
                .map(HistoricoEstoqueMinimoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<PastilhaFornecedorResponse> listarFornecedores(Long id) {
        obter(id);
        return pastilhaFornecedorRepository.findByPastilhaId(id).stream().map(PastilhaFornecedorResponse::de).toList();
    }

    @Transactional
    public PastilhaFornecedorResponse salvarFornecedor(Long id, PastilhaFornecedorRequest req) {
        Pastilha p = obter(id);
        Fornecedor f = fornecedorService.obter(req.fornecedorId());
        PastilhaFornecedor pf = pastilhaFornecedorRepository.findById(new PastilhaFornecedorId(id, f.getId()))
                .orElseGet(() -> new PastilhaFornecedor(p, f, null, null));
        pf.setPrecoReferencia(req.precoReferencia());
        pf.setPrazoEntregaDias(req.prazoEntregaDias());
        return PastilhaFornecedorResponse.de(pastilhaFornecedorRepository.save(pf));
    }

    @Transactional
    public void removerFornecedor(Long id, Long fornecedorId) {
        var chave = new PastilhaFornecedorId(id, fornecedorId);
        if (!pastilhaFornecedorRepository.existsById(chave)) {
            throw new RecursoNaoEncontradoException("Fornecedor alternativo", fornecedorId);
        }
        pastilhaFornecedorRepository.deleteById(chave);
    }

    Pastilha obter(Long id) {
        return pastilhaRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pastilha", id));
    }

    /** RN09: registra quem alterou o minimo e quando; reavalia o alerta do item. */
    private void alterarMinimo(Pastilha p, int novoMinimo, Usuario usuario) {
        int anterior = p.getEstoqueMinimo();
        if (anterior == novoMinimo) {
            return;
        }
        p.definirEstoqueMinimo(novoMinimo);
        historicoMinimoRepository.save(
                new HistoricoEstoqueMinimo(p, usuario, anterior, novoMinimo, LocalDateTime.now(clock)));
        alertaService.avaliar(p);
    }

    private void preencher(Pastilha p, PastilhaRequest req) {
        p.setEspecificacao(req.especificacao());
        p.setUnidadeMedida(req.unidadeMedida().trim().toUpperCase());
        Fabricante fab = req.fabricanteId() == null ? null : fabricanteService.obter(req.fabricanteId());
        Fornecedor forn = req.fornecedorPrincipalId() == null ? null
                : fornecedorService.obter(req.fornecedorPrincipalId());
        p.setFabricante(fab);
        p.setFornecedorPrincipal(forn);
    }

    static String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }
}
