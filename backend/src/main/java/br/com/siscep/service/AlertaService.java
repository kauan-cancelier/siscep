package br.com.siscep.service;

import br.com.siscep.domain.AlertaEstoque;
import br.com.siscep.domain.Pastilha;
import br.com.siscep.domain.StatusEstoque;
import br.com.siscep.dto.AlertaResponse;
import br.com.siscep.repository.AlertaEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** Sinalizacao de itens criticos (RF12 / RN08): mantem um alerta aberto por pastilha fora do normal. */
@Service
public class AlertaService {

    private final AlertaEstoqueRepository alertaRepository;
    private final Clock clock;

    public AlertaService(AlertaEstoqueRepository alertaRepository, Clock clock) {
        this.alertaRepository = alertaRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void avaliar(Pastilha pastilha) {
        LocalDateTime agora = LocalDateTime.now(clock);
        var aberto = alertaRepository.findFirstByPastilhaIdAndResolvidoFalse(pastilha.getId());
        boolean emAlerta = pastilha.isAtivo() && pastilha.situacao() != StatusEstoque.NORMAL;
        if (emAlerta) {
            aberto.ifPresentOrElse(a -> a.atualizar(pastilha),
                    () -> alertaRepository.save(new AlertaEstoque(pastilha, agora)));
        } else {
            aberto.ifPresent(a -> a.resolver(agora));
        }
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listarAbertos() {
        return alertaRepository.findByResolvidoFalseOrderByGeradoEmDesc().stream().map(AlertaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listarPorPastilha(Long pastilhaId) {
        return alertaRepository.findByPastilhaIdOrderByGeradoEmDesc(pastilhaId).stream()
                .map(AlertaResponse::de).toList();
    }
}
