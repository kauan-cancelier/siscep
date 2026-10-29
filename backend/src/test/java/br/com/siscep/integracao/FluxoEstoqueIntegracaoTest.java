package br.com.siscep.integracao;

import br.com.siscep.repository.PastilhaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FluxoEstoqueIntegracaoTest extends IntegracaoBase {

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PastilhaRepository pastilhaRepository;

    private String admin;
    private String almox;
    private String operador;
    private String gestor;
    private long fornecedorId;

    @BeforeAll
    void preparar() throws Exception {
        admin = tokenAdmin();
        String sufixo = UUID.randomUUID().toString().substring(0, 6);
        almox = criarUsuario(admin, "almox" + sufixo + "@dda.com.br", "ALMOXARIFE");
        operador = criarUsuario(admin, "oper" + sufixo + "@dda.com.br", "OPERADOR");
        gestor = criarUsuario(admin, "gestor" + sufixo + "@dda.com.br", "GESTOR");
        fornecedorId = postJson("/api/fornecedores", almox,
                Map.of("razaoSocial", "Ferramentas Sul " + sufixo, "prazoMedioEntregaDias", 5), 201).get("id").asLong();
    }

    private long novaPastilha(int minimo) throws Exception {
        String codigo = "IT-" + UUID.randomUUID().toString().substring(0, 8);
        return postJson("/api/pastilhas", almox, Map.of("codigo", codigo, "descricao", "Pastilha " + codigo,
                "unidadeMedida", "UN", "estoqueMinimo", minimo, "fornecedorPrincipalId", fornecedorId), 201)
                .get("id").asLong();
    }

    private JsonNode entrada(long pastilhaId, int qtd) throws Exception {
        return postJson("/api/movimentacoes/entradas", almox, Map.of("pastilhaId", pastilhaId, "quantidade", qtd,
                "fornecedorId", fornecedorId, "documentoReferencia", "NF-1"), 201);
    }

    @Test
    void fluxoCompletoDeEntradaSaidaEAjuste() throws Exception {
        long id = novaPastilha(5);
        assertThat(entrada(id, 10).get("saldoResultante").asInt()).isEqualTo(10);

        JsonNode saida = postJson("/api/movimentacoes/saidas", operador,
                Map.of("pastilhaId", id, "quantidade", 6, "destino", "OP-100"), 201);
        assertThat(saida.get("saldoResultante").asInt()).isEqualTo(4);

        postJson("/api/movimentacoes/saidas", operador,
                Map.of("pastilhaId", id, "quantidade", 5, "destino", "OP-101"), 422);

        JsonNode ajuste = postJson("/api/movimentacoes/ajustes", almox,
                Map.of("pastilhaId", id, "contagemFisica", 3, "justificativa", "Quebra no manuseio"), 201);
        assertThat(ajuste.get("quantidade").asInt()).isEqualTo(1);

        mvc.perform(get("/api/pastilhas/" + id).header("Authorization", gestor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoAtual").value(3))
                .andExpect(jsonPath("$.situacao").value("CRITICO"));

        mvc.perform(get("/api/alertas").header("Authorization", gestor))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"pastilhaId\":" + id)));

        mvc.perform(get("/api/movimentacoes").param("pastilhaId", String.valueOf(id)).header("Authorization", gestor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(3));

        mvc.perform(get("/api/movimentacoes").param("pastilhaId", String.valueOf(id))
                        .header("Authorization", operador))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void matrizDePermissoesEhRespeitada() throws Exception {
        long id = novaPastilha(1);
        postJson("/api/movimentacoes/entradas", operador,
                Map.of("pastilhaId", id, "quantidade", 1, "fornecedorId", fornecedorId), 403);
        postJson("/api/movimentacoes/entradas", admin,
                Map.of("pastilhaId", id, "quantidade", 1, "fornecedorId", fornecedorId), 403);
        postJson("/api/movimentacoes/ajustes", gestor,
                Map.of("pastilhaId", id, "contagemFisica", 1, "justificativa", "x"), 403);
        postJson("/api/pastilhas", operador, Map.of("codigo", "X", "descricao", "X", "unidadeMedida", "UN",
                "estoqueMinimo", 0), 403);
        mvc.perform(get("/api/dashboard").header("Authorization", operador)).andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios").header("Authorization", gestor)).andExpect(status().isForbidden());
        mvc.perform(get("/api/dashboard").header("Authorization", gestor)).andExpect(status().isOk());
        mvc.perform(get("/api/pastilhas")).andExpect(status().isUnauthorized());
    }

    @Test
    void codigoNaoMudaAposMovimentacaoEPastilhaComHistoricoNaoEhExcluida() throws Exception {
        long id = novaPastilha(1);
        entrada(id, 2);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/pastilhas/" + id)
                        .header("Authorization", almox).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("codigo", "NOVO-" + id, "descricao", "x",
                                "unidadeMedida", "UN", "estoqueMinimo", 1))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem", containsString("codigo")));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/pastilhas/" + id)
                        .header("Authorization", almox))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void movimentacaoEhImutavelNoBanco() throws Exception {
        long id = novaPastilha(1);
        long movId = entrada(id, 2).get("id").asLong();
        assertThatThrownBy(() -> jdbc.update("update movimentacao set quantidade = 99 where id = ?", movId))
                .hasMessageContaining("imutaveis");
        assertThatThrownBy(() -> jdbc.update("delete from movimentacao where id = ?", movId))
                .hasMessageContaining("imutaveis");
    }

    @Test
    void saidasConcorrentesNuncaDeixamSaldoNegativo() throws Exception {
        long id = novaPastilha(0);
        entrada(id, 10);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Callable<Integer>> tarefas = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            tarefas.add(() -> mvc.perform(post("/api/movimentacoes/saidas").header("Authorization", operador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(Map.of("pastilhaId", id, "quantidade", 1,
                                    "destino", "Torno CNC"))))
                    .andReturn().getResponse().getStatus());
        }
        int sucesso = 0;
        for (Future<Integer> f : pool.invokeAll(tarefas)) {
            if (f.get() == 201) {
                sucesso++;
            }
        }
        pool.shutdown();
        assertThat(sucesso).isEqualTo(10);
        assertThat(pastilhaRepository.findById(id).orElseThrow().getSaldoAtual()).isZero();
    }

    @Test
    void relatoriosEExportacoes() throws Exception {
        long id = novaPastilha(4);
        entrada(id, 3);
        mvc.perform(get("/api/relatorios/reposicao").header("Authorization", gestor))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"quantidadeSugerida\":5")));
        mvc.perform(get("/api/relatorios/reposicao/exportar").param("formato", "csv").header("Authorization", gestor))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("siscep-reposicao.csv")));
        mvc.perform(get("/api/relatorios/movimentacoes/exportar").param("inicio", "2020-01-01")
                        .param("fim", "2021-12-31").param("formato", "pdf").header("Authorization", gestor))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
        mvc.perform(get("/api/relatorios/consumo").param("inicio", "2026-01-01").param("fim", "2026-12-31")
                        .header("Authorization", operador))
                .andExpect(status().isForbidden());
    }

    @Test
    void importacaoCsvCriaPastilhasComSaldoInicialRastreavel() throws Exception {
        String cod = "IMP-" + UUID.randomUUID().toString().substring(0, 6);
        String csv = "codigo;descricao;especificacao;unidade;fabricante;estoque_minimo;saldo_inicial\n"
                + cod + ";Pastilha importada;ISO;UN;Sandvik;5;12\n"
                + ";sem codigo;;;;1;1\n";
        mvc.perform(multipart("/api/pastilhas/importacao")
                        .file(new MockMultipartFile("arquivo", "carga.csv", "text/csv",
                                csv.getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", almox))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pastilhasCriadas").value(1))
                .andExpect(jsonPath("$.saldosIniciais").value(1))
                .andExpect(jsonPath("$.erros.length()").value(1));
        assertThat(pastilhaRepository.findByCodigoIgnoreCase(cod).orElseThrow().getSaldoAtual()).isEqualTo(12);
    }
}
