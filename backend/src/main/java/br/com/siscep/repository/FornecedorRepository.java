package br.com.siscep.repository;

import br.com.siscep.domain.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    List<Fornecedor> findAllByOrderByRazaoSocialAsc();

    List<Fornecedor> findByAtivoTrueOrderByRazaoSocialAsc();

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
