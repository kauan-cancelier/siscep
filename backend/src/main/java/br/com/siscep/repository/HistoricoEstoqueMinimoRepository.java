package br.com.siscep.repository;

import br.com.siscep.domain.HistoricoEstoqueMinimo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoEstoqueMinimoRepository extends JpaRepository<HistoricoEstoqueMinimo, Long> {

    @EntityGraph(attributePaths = "usuario")
    List<HistoricoEstoqueMinimo> findByPastilhaIdOrderByAlteradoEmDesc(Long pastilhaId);
}
