package br.com.siscep.repository;

import br.com.siscep.domain.AlertaEstoque;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertaEstoqueRepository extends JpaRepository<AlertaEstoque, Long> {

    Optional<AlertaEstoque> findFirstByPastilhaIdAndResolvidoFalse(Long pastilhaId);

    @EntityGraph(attributePaths = "pastilha")
    List<AlertaEstoque> findByResolvidoFalseOrderByGeradoEmDesc();

    @EntityGraph(attributePaths = "pastilha")
    List<AlertaEstoque> findByPastilhaIdOrderByGeradoEmDesc(Long pastilhaId);

    long countByResolvidoFalse();
}
