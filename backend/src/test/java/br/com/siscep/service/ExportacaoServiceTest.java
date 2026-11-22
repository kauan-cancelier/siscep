package br.com.siscep.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExportacaoServiceTest {

    private final ExportacaoService service = new ExportacaoService(Fixtures.CLOCK);

    @Test
    void csvUsaPontoEVirgulaEBom() {
        byte[] csv = service.csv(List.of("Código", "Descrição"), List.of(List.of("P1", "Pastilha; especial")));
        String texto = new String(csv, StandardCharsets.UTF_8);
        assertThat(texto).startsWith("\uFEFFCódigo;Descrição\r\n");
        assertThat(texto).contains("P1;\"Pastilha; especial\"");
    }

    @Test
    void csvNeutralizaFormulas() {
        assertThat(ExportacaoService.escapar("=HYPERLINK(\"x\")")).startsWith("\"'=");
        assertThat(ExportacaoService.escapar("-5")).isEqualTo("-5");
        assertThat(ExportacaoService.escapar(null)).isEmpty();
    }

    @Test
    void pdfGeraDocumentoValido() {
        byte[] pdf = service.pdf("Titulo", "Sub", List.of("A", "B"), List.of(List.of("1", "2")));
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        byte[] vazio = service.pdf("Titulo", null, List.of("A"), List.of());
        assertThat(vazio.length).isGreaterThan(100);
    }
}
