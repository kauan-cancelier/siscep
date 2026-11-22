package br.com.siscep.domain;

import br.com.siscep.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "pastilha")
public class Pastilha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String descricao;

    @Column(length = 500)
    private String especificacao;

    @Column(name = "unidade_medida", nullable = false, length = 10)
    private String unidadeMedida = "PC";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fabricante_id")
    private Fabricante fabricante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_principal_id")
    private Fornecedor fornecedorPrincipal;

    @Column(name = "estoque_minimo", nullable = false)
    private int estoqueMinimo;

    @Column(name = "saldo_atual", nullable = false)
    private int saldoAtual;

    @Column(nullable = false)
    private boolean ativo = true;

    @Version
    @Column(nullable = false)
    private long versao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    protected Pastilha() {
    }

    public Pastilha(String codigo, String descricao, int estoqueMinimo) {
        this.codigo = codigo;
        this.descricao = descricao;
        definirEstoqueMinimo(estoqueMinimo);
    }

    /** RN07: entradas somam ao saldo. */
    public void creditar(int quantidade) {
        validarQuantidade(quantidade);
        saldoAtual += quantidade;
    }

    /** RN05/RN06: saida nunca deixa saldo negativo. */
    public void debitar(int quantidade) {
        validarQuantidade(quantidade);
        if (quantidade > saldoAtual) {
            throw new RegraNegocioException(
                    "Saldo insuficiente para a pastilha " + codigo + ": disponível " + saldoAtual
                            + ", solicitado " + quantidade + ".");
        }
        saldoAtual -= quantidade;
    }

    /** RN07: ajuste substitui o saldo pela contagem fisica e devolve a diferenca. */
    public int ajustarPara(int contagemFisica) {
        if (contagemFisica < 0) {
            throw new RegraNegocioException("A contagem física não pode ser negativa.");
        }
        int diferenca = contagemFisica - saldoAtual;
        if (diferenca == 0) {
            throw new RegraNegocioException("A contagem física é igual ao saldo atual; não há ajuste a registrar.");
        }
        saldoAtual = contagemFisica;
        return diferenca;
    }

    public void definirEstoqueMinimo(int estoqueMinimo) {
        if (estoqueMinimo < 0) {
            throw new RegraNegocioException("O estoque mínimo deve ser maior ou igual a zero.");
        }
        this.estoqueMinimo = estoqueMinimo;
    }

    public StatusEstoque situacao() {
        return StatusEstoque.de(saldoAtual, estoqueMinimo);
    }

    /** RN12 revisada: sugestao = max(1, 2 x minimo - saldo) para itens fora da situacao normal. */
    public int quantidadeSugeridaCompra() {
        if (situacao() == StatusEstoque.NORMAL) {
            return 0;
        }
        return Math.max(1, estoqueMinimo * 2 - saldoAtual);
    }

    private static void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade deve ser um número inteiro maior que zero.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getEspecificacao() {
        return especificacao;
    }

    public void setEspecificacao(String especificacao) {
        this.especificacao = especificacao;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public Fabricante getFabricante() {
        return fabricante;
    }

    public void setFabricante(Fabricante fabricante) {
        this.fabricante = fabricante;
    }

    public Fornecedor getFornecedorPrincipal() {
        return fornecedorPrincipal;
    }

    public void setFornecedorPrincipal(Fornecedor fornecedorPrincipal) {
        this.fornecedorPrincipal = fornecedorPrincipal;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public int getSaldoAtual() {
        return saldoAtual;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public long getVersao() {
        return versao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
