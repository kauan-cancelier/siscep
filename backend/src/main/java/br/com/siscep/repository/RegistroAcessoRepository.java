package br.com.siscep.repository;

import br.com.siscep.domain.RegistroAcesso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroAcessoRepository extends JpaRepository<RegistroAcesso, Long> {

    Page<RegistroAcesso> findAllByOrderByDataHoraDesc(Pageable pageable);
}
