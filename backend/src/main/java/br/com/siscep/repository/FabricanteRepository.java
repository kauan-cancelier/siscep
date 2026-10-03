package br.com.siscep.repository;

import br.com.siscep.domain.Fabricante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FabricanteRepository extends JpaRepository<Fabricante, Long> {

    List<Fabricante> findAllByOrderByNomeAsc();

    List<Fabricante> findByAtivoTrueOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
