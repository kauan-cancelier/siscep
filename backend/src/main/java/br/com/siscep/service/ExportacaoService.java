package br.com.siscep.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Exportacao de relatorios em CSV e PDF (RF19). */
@Service
public class ExportacaoService {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final byte[] BOM_UTF8 = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final Clock clock;

    public ExportacaoService(Clock clock) {
        this.clock = clock;
    }

    /** CSV com separador ';' e BOM para abrir corretamente no Excel em portugues. */
    public byte[] csv(List<String> cabecalho, List<List<String>> linhas) {
        StringBuilder sb = new StringBuilder();
        sb.append(linhaCsv(cabecalho));
        linhas.forEach(l -> sb.append(linhaCsv(l)));
        byte[] corpo = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] saida = new byte[BOM_UTF8.length + corpo.length];
        System.arraycopy(BOM_UTF8, 0, saida, 0, BOM_UTF8.length);
        System.arraycopy(corpo, 0, saida, BOM_UTF8.length, corpo.length);
        return saida;
    }

    public byte[] pdf(String titulo, String subtitulo, List<String> cabecalho, List<List<String>> linhas) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(cabecalho.size() > 6 ? PageSize.A4.rotate() : PageSize.A4, 28, 28, 28, 28);
        PdfWriter.getInstance(doc, out);
        doc.open();
        Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        Font fCab = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
        Font fCel = FontFactory.getFont(FontFactory.HELVETICA, 8);

        doc.add(new Paragraph("SISCEP - DDA Usinagem Industrial Ltda.", fSub));
        doc.add(new Paragraph(titulo, fTitulo));
        if (subtitulo != null) {
            doc.add(new Paragraph(subtitulo, fSub));
        }
        doc.add(new Paragraph("Gerado em " + FORMATO_DATA_HORA.format(LocalDateTime.now(clock)), fSub));
        doc.add(new Paragraph(" "));

        PdfPTable tabela = new PdfPTable(cabecalho.size());
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1);
        for (String c : cabecalho) {
            PdfPCell cell = new PdfPCell(new Phrase(c, fCab));
            cell.setBackgroundColor(new Color(30, 64, 120));
            cell.setPadding(4);
            tabela.addCell(cell);
        }
        boolean zebra = false;
        for (List<String> linha : linhas) {
            for (String v : linha) {
                PdfPCell cell = new PdfPCell(new Phrase(v == null ? "" : v, fCel));
                cell.setPadding(3);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                if (zebra) {
                    cell.setBackgroundColor(new Color(240, 244, 250));
                }
                tabela.addCell(cell);
            }
            zebra = !zebra;
        }
        if (linhas.isEmpty()) {
            PdfPCell vazio = new PdfPCell(new Phrase("Nenhum registro encontrado para os filtros informados.", fCel));
            vazio.setColspan(cabecalho.size());
            vazio.setPadding(6);
            tabela.addCell(vazio);
        }
        doc.add(tabela);
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Total de registros: " + linhas.size(), fSub));
        doc.close();
        return out.toByteArray();
    }

    static String linhaCsv(List<String> valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(escapar(valores.get(i)));
        }
        return sb.append("\r\n").toString();
    }

    /** Escapa aspas/separadores e neutraliza injecao de formulas em planilhas. */
    static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        String v = valor;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0 && !v.matches("-?\\d+([.,]\\d+)?")) {
            v = "'" + v;
        }
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    public static String formatar(LocalDateTime dataHora) {
        return dataHora == null ? "" : FORMATO_DATA_HORA.format(dataHora);
    }
}
