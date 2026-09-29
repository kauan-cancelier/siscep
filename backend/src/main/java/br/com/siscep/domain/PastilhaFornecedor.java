package br.com.siscep.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Fornecedores alternativos de uma pastilha, com preco de referencia e prazo (RF22). */
@Entity
@Table(name = "pastilha_fornecedor")
public class PastilhaFornecedor {

    @EmbeddedId
    private PastilhaFornecedorId id;

    @MapsId("pastilhaId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pastilha_id")
    private Pastilha pastilha;

    @MapsId("fornecedorId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Column(name = "preco_referencia", precision = 12, scale = 2)
    private BigDecimal precoReferencia;

    @Column(name = "prazo_entrega_dias")
    private Integer prazoEntregaDias;

    protected PastilhaFornecedor() {
    }

    public PastilhaFornecedor(Pastilha pastilha, Fornecedor fornecedor, BigDecimal precoReferencia,
                              Integer prazoEntregaDias) {
        this.id = new PastilhaFornecedorId(pastilha.getId(), fornecedor.getId());
        this.pastilha = pastilha;
        this.fornecedor = fornecedor;
        this.precoReferencia = precoReferencia;
        this.prazoEntregaDias = prazoEntregaDias;
    }

    public PastilhaFornecedorId getId() {
        return id;
    }

    public Pastilha getPastilha() {
        return pastilha;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public BigDecimal getPrecoReferencia() {
        return precoReferencia;
    }

    public void setPrecoReferencia(BigDecimal precoReferencia) {
        this.precoReferencia = precoReferencia;
    }

    public Integer getPrazoEntregaDias() {
        return prazoEntregaDias;
    }

    public void setPrazoEntregaDias(Integer prazoEntregaDias) {
        this.prazoEntregaDias = prazoEntregaDias;
    }
}
