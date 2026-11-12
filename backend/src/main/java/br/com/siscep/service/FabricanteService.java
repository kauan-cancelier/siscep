package br.com.siscep.service;

import br.com.siscep.domain.Fabricante;
import br.com.siscep.dto.FabricanteRequest;
import br.com.siscep.dto.FabricanteResponse;
import br.com.siscep.exception.ConflitoException;
import br.com.siscep.exception.RecursoNaoEncontradoException;
import br.com.siscep.exception.RegraNegocioException;
import br.com.siscep.repository.FabricanteRepository;
import br.com.siscep.repository.PastilhaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Cadastro de fabricantes (RF04) com exclusao restrita pela RN11. */
@Service
public class FabricanteService {

    private final FabricanteRepository fabricanteRepository;
    private final PastilhaRepository pastilhaRepository;
    private final AuditoriaService auditoria;

    public FabricanteService(FabricanteRepository fabricanteRepository, PastilhaRepository pastilhaRepository,
                             AuditoriaService auditoria) {
        this.fabricanteRepository = fabricanteRepository;
        this.pastilhaRepository = pastilhaRepository;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<FabricanteResponse> listar(boolean apenasAtivos) {
        var lista = apenasAtivos ? fabricanteRepository.findByAtivoTrueOrderByNomeAsc()
                : fabricanteRepository.findAllByOrderByNomeAsc();
        return lista.stream().map(FabricanteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public FabricanteResponse buscar(Long id) {
        return FabricanteResponse.de(obter(id));
    }

    @Transactional
    public FabricanteResponse criar(FabricanteRequest req) {
        String nome = req.nome().trim();
        if (fabricanteRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe um fabricante com o nome " + nome + ".");
        }
        Fabricante salvo = fabricanteRepository.save(new Fabricante(nome, req.pais(), req.site()));
        auditoria.registrar("CRIAR", "fabricante", salvo.getId(), "Fabricante " + nome + " criado.");
        return FabricanteResponse.de(salvo);
    }

    @Transactional
    public FabricanteResponse atualizar(Long id, FabricanteRequest req) {
        Fabricante f = obter(id);
        String nome = req.nome().trim();
        if (fabricanteRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe um fabricante com o nome " + nome + ".");
        }
        f.setNome(nome);
        f.setPais(req.pais());
        f.setSite(req.site());
        auditoria.registrar("ATUALIZAR", "fabricante", id, "Fabricante " + nome + " atualizado.");
        return FabricanteResponse.de(f);
    }

    @Transactional
    public FabricanteResponse alterarStatus(Long id, boolean ativo) {
        Fabricante f = obter(id);
        f.setAtivo(ativo);
        auditoria.registrar(ativo ? "ATIVAR" : "INATIVAR", "fabricante", id, "Fabricante " + f.getNome() + (ativo ? " ativado." : " inativado."));
        return FabricanteResponse.de(f);
    }

    @Transactional
    public void excluir(Long id) {
        Fabricante f = obter(id);
        if (pastilhaRepository.existsByFabricanteId(id)) {
            throw new RegraNegocioException(
                    "O fabricante possui pastilhas vinculadas e não pode ser excluido; inative-o.");
        }
        fabricanteRepository.delete(f);
        auditoria.registrar("EXCLUIR", "fabricante", id, "Fabricante " + f.getNome() + " excluído.");
    }

    Fabricante obter(Long id) {
        return fabricanteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fabricante", id));
    }
}
