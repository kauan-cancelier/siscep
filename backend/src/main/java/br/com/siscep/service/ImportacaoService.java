package br.com.siscep.service;

import br.com.siscep.domain.Fabricante;
import br.com.siscep.domain.Movimentacao;
import br.com.siscep.domain.Pastilha;
import br.com.siscep.domain.TipoMovimentacao;
import br.com.siscep.domain.Usuario;
import br.com.siscep.dto.ImportacaoResultado;
import br.com.siscep.exception.RegraNegocioException;
import br.com.siscep.repository.FabricanteRepository;
import br.com.siscep.repository.MovimentacaoRepository;
import br.com.siscep.repository.PastilhaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carga inicial a partir das planilhas atuais exportadas em CSV (lacuna apontada na analise do documento).
 * Colunas: codigo;descricao;especificacao;unidade;fabricante;estoque_minimo;saldo_inicial.
 * O saldo inicial gera um AJUSTE com justificativa, preservando a rastreabilidade (RN03/RN05).
 */
@Service
public class ImportacaoService {

    static final int MAX_LINHAS = 5000;
    private static final int COLUNAS = 7;

    private final PastilhaRepository pastilhaRepository;
    private final FabricanteRepository fabricanteRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final AlertaService alertaService;
    private final Clock clock;

    public ImportacaoService(PastilhaRepository pastilhaRepository, FabricanteRepository fabricanteRepository,
                             MovimentacaoRepository movimentacaoRepository, AlertaService alertaService, Clock clock) {
        this.pastilhaRepository = pastilhaRepository;
        this.fabricanteRepository = fabricanteRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.alertaService = alertaService;
        this.clock = clock;
    }

    @Transactional
    public ImportacaoResultado importarPastilhas(InputStream csv, Usuario usuario) {
        List<String> erros = new ArrayList<>();
        Map<String, Fabricante> fabricantes = new HashMap<>();
        fabricanteRepository.findAll().forEach(f -> fabricantes.put(f.getNome().toLowerCase(), f));
        int processadas = 0;
        int criadas = 0;
        int saldos = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csv, StandardCharsets.UTF_8))) {
            String linha = reader.readLine();
            if (linha == null) {
                throw new RegraNegocioException("O arquivo esta vazio.");
            }
            int numero = 1;
            while ((linha = reader.readLine()) != null) {
                numero++;
                if (linha.isBlank()) {
                    continue;
                }
                if (++processadas > MAX_LINHAS) {
                    throw new RegraNegocioException("O arquivo excede o limite de " + MAX_LINHAS + " linhas.");
                }
                String[] c = linha.split(";", -1);
                if (c.length < COLUNAS) {
                    erros.add("Linha " + numero + ": esperado " + COLUNAS + " colunas separadas por ';'.");
                    continue;
                }
                String codigo = PastilhaService.normalizarCodigo(c[0]);
                if (codigo.isEmpty() || c[1].isBlank()) {
                    erros.add("Linha " + numero + ": código e descrição são obrigatórios.");
                    continue;
                }
                if (pastilhaRepository.existsByCodigoIgnoreCase(codigo)) {
                    erros.add("Linha " + numero + ": a pastilha " + codigo + " já está cadastrada.");
                    continue;
                }
                int minimo;
                int saldo;
                try {
                    minimo = Integer.parseInt(c[5].trim());
                    saldo = c[6].isBlank() ? 0 : Integer.parseInt(c[6].trim());
                } catch (NumberFormatException e) {
                    erros.add("Linha " + numero + ": estoque mínimo e saldo devem ser números inteiros.");
                    continue;
                }
                if (minimo < 0 || saldo < 0) {
                    erros.add("Linha " + numero + ": estoque mínimo e saldo não podem ser negativos.");
                    continue;
                }
                Pastilha p = new Pastilha(codigo, c[1].trim(), minimo);
                p.setEspecificacao(c[2].isBlank() ? null : c[2].trim());
                p.setUnidadeMedida(c[3].isBlank() ? "UN" : c[3].trim().toUpperCase());
                if (!c[4].isBlank()) {
                    p.setFabricante(fabricantes.computeIfAbsent(c[4].trim().toLowerCase(),
                            k -> fabricanteRepository.save(new Fabricante(c[4].trim(), null, null))));
                }
                p = pastilhaRepository.save(p);
                criadas++;
                if (saldo > 0) {
                    p.ajustarPara(saldo);
                    movimentacaoRepository.save(Movimentacao.builder()
                            .tipo(TipoMovimentacao.AJUSTE).quantidade(saldo).pastilha(p).usuario(usuario)
                            .saldoAnterior(0).saldoResultante(saldo)
                            .justificativa("Saldo inicial importado da planilha de controle")
                            .dataHora(LocalDateTime.now(clock)).build());
                    saldos++;
                }
                alertaService.avaliar(p);
            }
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível ler o arquivo enviado.");
        }
        return new ImportacaoResultado(processadas, criadas, saldos, erros);
    }
}
