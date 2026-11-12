package br.com.siscep.service;

import br.com.siscep.domain.Fornecedor;
import br.com.siscep.dto.FornecedorRequest;
import br.com.siscep.dto.FornecedorResponse;
import br.com.siscep.exception.ConflitoException;
import br.com.siscep.exception.RecursoNaoEncontradoException;
import br.com.siscep.exception.RegraNegocioException;
import br.com.siscep.repository.FornecedorRepository;
import br.com.siscep.repository.MovimentacaoRepository;
import br.com.siscep.repository.PastilhaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Cadastro de fornecedores (RF05) com exclusao restrita pela RN11. */
@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final PastilhaRepository pastilhaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final AuditoriaService auditoria;

    public FornecedorService(FornecedorRepository fornecedorRepository, PastilhaRepository pastilhaRepository,
                             MovimentacaoRepository movimentacaoRepository, AuditoriaService auditoria) {
        this.fornecedorRepository = fornecedorRepository;
        this.pastilhaRepository = pastilhaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<FornecedorResponse> listar(boolean apenasAtivos) {
        var lista = apenasAtivos ? fornecedorRepository.findByAtivoTrueOrderByRazaoSocialAsc()
                : fornecedorRepository.findAllByOrderByRazaoSocialAsc();
        return lista.stream().map(FornecedorResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public FornecedorResponse buscar(Long id) {
        return FornecedorResponse.de(obter(id));
    }

    @Transactional
    public FornecedorResponse criar(FornecedorRequest req) {
        String cnpj = normalizarCnpj(req.cnpj());
        if (cnpj != null && fornecedorRepository.existsByCnpj(cnpj)) {
            throw new ConflitoException("Já existe um fornecedor com o CNPJ informado.");
        }
        Fornecedor f = new Fornecedor(req.razaoSocial().trim(), cnpj);
        preencher(f, req);
        Fornecedor salvo = fornecedorRepository.save(f);
        auditoria.registrar("CRIAR", "fornecedor", salvo.getId(), "Fornecedor " + salvo.getRazaoSocial() + " criado.");
        return FornecedorResponse.de(salvo);
    }

    @Transactional
    public FornecedorResponse atualizar(Long id, FornecedorRequest req) {
        Fornecedor f = obter(id);
        String cnpj = normalizarCnpj(req.cnpj());
        if (cnpj != null && fornecedorRepository.existsByCnpjAndIdNot(cnpj, id)) {
            throw new ConflitoException("Já existe um fornecedor com o CNPJ informado.");
        }
        f.setRazaoSocial(req.razaoSocial().trim());
        f.setCnpj(cnpj);
        preencher(f, req);
        auditoria.registrar("ATUALIZAR", "fornecedor", id, "Fornecedor " + f.getRazaoSocial() + " atualizado.");
        return FornecedorResponse.de(f);
    }

    @Transactional
    public FornecedorResponse alterarStatus(Long id, boolean ativo) {
        Fornecedor f = obter(id);
        f.setAtivo(ativo);
        auditoria.registrar(ativo ? "ATIVAR" : "INATIVAR", "fornecedor", id, "Fornecedor " + f.getRazaoSocial() + (ativo ? " ativado." : " inativado."));
        return FornecedorResponse.de(f);
    }

    @Transactional
    public void excluir(Long id) {
        Fornecedor f = obter(id);
        if (pastilhaRepository.existsByFornecedorPrincipalId(id) || movimentacaoRepository.existsByFornecedorId(id)) {
            throw new RegraNegocioException(
                    "O fornecedor possui pastilhas ou movimentações vinculadas e não pode ser excluido; inative-o.");
        }
        fornecedorRepository.delete(f);
        auditoria.registrar("EXCLUIR", "fornecedor", id, "Fornecedor " + f.getRazaoSocial() + " excluído.");
    }

    Fornecedor obter(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor", id));
    }

    private static void preencher(Fornecedor f, FornecedorRequest req) {
        f.setContato(req.contato());
        f.setTelefone(req.telefone());
        f.setEmail(req.email());
        f.setPrazoMedioEntregaDias(req.prazoMedioEntregaDias());
    }

    static String normalizarCnpj(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            return null;
        }
        return cnpj.replaceAll("\\D", "");
    }
}
