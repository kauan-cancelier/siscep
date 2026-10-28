package br.com.siscep.controller;

import br.com.siscep.domain.TipoMovimentacao;
import br.com.siscep.dto.MovimentacaoResponse;
import br.com.siscep.dto.PastilhaResponse;
import br.com.siscep.dto.RelatorioConsumoItem;
import br.com.siscep.dto.RelatorioReposicaoItem;
import br.com.siscep.exception.RegraNegocioException;
import br.com.siscep.security.Perfis;
import br.com.siscep.security.UsuarioAutenticado;
import br.com.siscep.service.EstoqueService;
import br.com.siscep.service.ExportacaoService;
import br.com.siscep.service.PastilhaService;
import br.com.siscep.service.Periodo;
import br.com.siscep.service.RelatorioService;
import br.com.siscep.service.UsuarioLogadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@PreAuthorize(Perfis.GESTAO)
@Tag(name = "Relatorios")
public class RelatorioController {

    /** Limite de linhas por exportacao, suficiente para o volume previsto no RNF07. */
    static final int LIMITE_EXPORTACAO = 100_000;
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RelatorioService relatorioService;
    private final EstoqueService estoqueService;
    private final PastilhaService pastilhaService;
    private final ExportacaoService exportacao;
    private final UsuarioLogadoService usuarioLogado;

    public RelatorioController(RelatorioService relatorioService, EstoqueService estoqueService,
                               PastilhaService pastilhaService, ExportacaoService exportacao,
                               UsuarioLogadoService usuarioLogado) {
        this.relatorioService = relatorioService;
        this.estoqueService = estoqueService;
        this.pastilhaService = pastilhaService;
        this.exportacao = exportacao;
        this.usuarioLogado = usuarioLogado;
    }

    @Operation(summary = "Relatorio de consumo por pastilha (RF17)")
    @GetMapping("/consumo")
    public List<RelatorioConsumoItem> consumo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return relatorioService.consumo(inicio, fim);
    }

    @Operation(summary = "Relatorio de necessidade de reposicao (RF18)")
    @GetMapping("/reposicao")
    public List<RelatorioReposicaoItem> reposicao() {
        return relatorioService.reposicao();
    }

    @Operation(summary = "Exporta o relatorio de movimentacoes por periodo em PDF ou CSV (RF16 / RF19)")
    @GetMapping("/movimentacoes/exportar")
    public ResponseEntity<byte[]> exportarMovimentacoes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long pastilhaId,
            @RequestParam(required = false) TipoMovimentacao tipo,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(defaultValue = "pdf") String formato,
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        Periodo.validar(inicio, fim);
        List<MovimentacaoResponse> movs = estoqueService.historico(inicio.atStartOfDay(), fim.atTime(LocalTime.MAX),
                pastilhaId, tipo, usuarioId, usuarioLogado.carregar(principal),
                PageRequest.of(0, LIMITE_EXPORTACAO, Sort.by("dataHora"))).conteudo();
        List<String> cab = List.of("Data/hora", "Tipo", "Codigo", "Descricao", "Qtd", "Saldo ant.", "Saldo result.",
                "Usuario", "Fornecedor/Destino", "Documento", "Justificativa/Obs.");
        List<List<String>> linhas = movs.stream().map(m -> List.of(
                ExportacaoService.formatar(m.dataHora()), m.tipo().name(), m.pastilhaCodigo(), m.pastilhaDescricao(),
                String.valueOf(m.quantidade()), String.valueOf(m.saldoAnterior()),
                String.valueOf(m.saldoResultante()), m.usuarioNome(),
                nvl(m.fornecedorNome() != null ? m.fornecedorNome() : m.destino()), nvl(m.documentoReferencia()),
                nvl(m.justificativa() != null ? m.justificativa() : m.observacao()))).toList();
        return exportar(formato, "movimentacoes", "Relatorio de movimentacoes", periodo(inicio, fim), cab, linhas);
    }

    @Operation(summary = "Exporta o relatorio de consumo em PDF ou CSV (RF17 / RF19)")
    @GetMapping("/consumo/exportar")
    public ResponseEntity<byte[]> exportarConsumo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "pdf") String formato) {
        List<String> cab = List.of("Codigo", "Descricao", "Total consumido", "N. de saidas", "Media diaria",
                "Media mensal");
        List<List<String>> linhas = relatorioService.consumo(inicio, fim).stream().map(c -> List.of(
                c.codigo(), c.descricao(), String.valueOf(c.totalConsumido()), String.valueOf(c.quantidadeSaidas()),
                c.mediaDiaria().toPlainString().replace('.', ','),
                c.mediaMensal().toPlainString().replace('.', ','))).toList();
        return exportar(formato, "consumo", "Relatorio de consumo por pastilha", periodo(inicio, fim), cab, linhas);
    }

    @Operation(summary = "Exporta o relatorio de reposicao em PDF ou CSV (RF18 / RF19)")
    @GetMapping("/reposicao/exportar")
    public ResponseEntity<byte[]> exportarReposicao(@RequestParam(defaultValue = "pdf") String formato) {
        List<String> cab = List.of("Codigo", "Descricao", "Situacao", "Saldo", "Minimo", "Qtd. sugerida",
                "Fornecedor", "Contato", "Prazo (dias)");
        List<List<String>> linhas = relatorioService.reposicao().stream().map(r -> List.of(
                r.codigo(), r.descricao(), r.situacao().name(), String.valueOf(r.saldoAtual()),
                String.valueOf(r.estoqueMinimo()), String.valueOf(r.quantidadeSugerida()), nvl(r.fornecedor()),
                nvl(r.contatoFornecedor()), r.prazoEntregaDias() == null ? "" : r.prazoEntregaDias().toString()))
                .toList();
        return exportar(formato, "reposicao", "Relatorio de necessidade de reposicao",
                "Quantidade sugerida = 2 x estoque minimo - saldo atual (RN12)", cab, linhas);
    }

    @Operation(summary = "Exporta a posicao atual do estoque em PDF ou CSV")
    @GetMapping("/estoque/exportar")
    public ResponseEntity<byte[]> exportarEstoque(@RequestParam(defaultValue = "pdf") String formato) {
        List<PastilhaResponse> itens = pastilhaService.consultar(null, null, null, null, true,
                PageRequest.of(0, LIMITE_EXPORTACAO, Sort.by("codigo"))).conteudo();
        List<String> cab = List.of("Codigo", "Descricao", "Unidade", "Fabricante", "Fornecedor", "Saldo", "Minimo",
                "Situacao");
        List<List<String>> linhas = itens.stream().map(p -> List.of(p.codigo(), p.descricao(), p.unidadeMedida(),
                nvl(p.fabricanteNome()), nvl(p.fornecedorPrincipalNome()), String.valueOf(p.saldoAtual()),
                String.valueOf(p.estoqueMinimo()), p.situacao().name())).toList();
        return exportar(formato, "estoque", "Posicao atual do estoque", null, cab, linhas);
    }

    private ResponseEntity<byte[]> exportar(String formato, String nome, String titulo, String subtitulo,
                                            List<String> cab, List<List<String>> linhas) {
        String f = formato.toLowerCase();
        byte[] conteudo;
        MediaType tipo;
        if ("csv".equals(f)) {
            conteudo = exportacao.csv(cab, linhas);
            tipo = new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8);
        } else if ("pdf".equals(f)) {
            conteudo = exportacao.pdf(titulo, subtitulo, cab, linhas);
            tipo = MediaType.APPLICATION_PDF;
        } else {
            throw new RegraNegocioException("Formato de exportacao invalido. Use pdf ou csv.");
        }
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("siscep-" + nome + "." + f).build().toString())
                .body(conteudo);
    }

    private static String periodo(LocalDate inicio, LocalDate fim) {
        return "Periodo: " + BR.format(inicio) + " a " + BR.format(fim);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
