package br.com.siscep.repository;

import br.com.siscep.domain.PastilhaFornecedor;
import br.com.siscep.domain.PastilhaFornecedorId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PastilhaFornecedorRepository extends JpaRepository<PastilhaFornecedor, PastilhaFornecedorId> {

    @EntityGraph(attributePaths = "fornecedor")
    List<PastilhaFornecedor> findByPastilhaId(Long pastilhaId);
}
