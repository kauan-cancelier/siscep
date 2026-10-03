package br.com.siscep.repository;

import br.com.siscep.domain.Pastilha;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PastilhaRepository extends JpaRepository<Pastilha, Long>, JpaSpecificationExecutor<Pastilha> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    Optional<Pastilha> findByCodigoIgnoreCase(String codigo);

    boolean existsByFabricanteId(Long fabricanteId);

    boolean existsByFornecedorPrincipalId(Long fornecedorId);

    /** Bloqueio pessimista por linha: serializa movimentacoes concorrentes da mesma pastilha. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pastilha p where p.id = :id")
    Optional<Pastilha> findByIdParaAtualizacao(Long id);

    long countByAtivoTrue();

    @Query("select count(p) from Pastilha p where p.ativo = true and p.saldoAtual = 0")
    long contarZeradas();

    @Query("select count(p) from Pastilha p where p.ativo = true and p.saldoAtual > 0 and p.saldoAtual <= p.estoqueMinimo")
    long contarCriticas();

    @Query("select coalesce(sum(p.saldoAtual), 0) from Pastilha p where p.ativo = true")
    long somarSaldo();

    @EntityGraph(attributePaths = {"fabricante", "fornecedorPrincipal"})
    @Query("select p from Pastilha p where p.ativo = true and p.saldoAtual <= p.estoqueMinimo order by p.saldoAtual asc, p.codigo asc")
    List<Pastilha> listarParaReposicao();
}
