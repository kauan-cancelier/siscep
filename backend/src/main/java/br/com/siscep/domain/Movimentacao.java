package br.com.siscep.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/** Registro imutavel (RN10 / RNF11): nao possui setters nem exclusao. */
@Entity
@Immutable
@Table(name = "movimentacao")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pastilha_id", nullable = false)
    private Pastilha pastilha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "saldo_anterior", nullable = false)
    private int saldoAnterior;

    @Column(name = "saldo_resultante", nullable = false)
    private int saldoResultante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Column(name = "documento_referencia", length = 60)
    private String documentoReferencia;

    @Column(length = 120)
    private String destino;

    @Column(length = 500)
    private String justificativa;

    @Column(length = 500)
    private String observacao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    protected Movimentacao() {
    }

    private Movimentacao(Builder b) {
        this.pastilha = b.pastilha;
        this.usuario = b.usuario;
        this.tipo = b.tipo;
        this.quantidade = b.quantidade;
        this.saldoAnterior = b.saldoAnterior;
        this.saldoResultante = b.saldoResultante;
        this.fornecedor = b.fornecedor;
        this.documentoReferencia = b.documentoReferencia;
        this.destino = b.destino;
        this.justificativa = b.justificativa;
        this.observacao = b.observacao;
        this.dataHora = b.dataHora;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Pastilha getPastilha() {
        return pastilha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getSaldoAnterior() {
        return saldoAnterior;
    }

    public int getSaldoResultante() {
        return saldoResultante;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public String getDocumentoReferencia() {
        return documentoReferencia;
    }

    public String getDestino() {
        return destino;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public String getObservacao() {
        return observacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public static final class Builder {
        private Pastilha pastilha;
        private Usuario usuario;
        private TipoMovimentacao tipo;
        private int quantidade;
        private int saldoAnterior;
        private int saldoResultante;
        private Fornecedor fornecedor;
        private String documentoReferencia;
        private String destino;
        private String justificativa;
        private String observacao;
        private LocalDateTime dataHora;

        public Builder pastilha(Pastilha pastilha) {
            this.pastilha = pastilha;
            return this;
        }

        public Builder usuario(Usuario usuario) {
            this.usuario = usuario;
            return this;
        }

        public Builder tipo(TipoMovimentacao tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder quantidade(int quantidade) {
            this.quantidade = quantidade;
            return this;
        }

        public Builder saldoAnterior(int saldoAnterior) {
            this.saldoAnterior = saldoAnterior;
            return this;
        }

        public Builder saldoResultante(int saldoResultante) {
            this.saldoResultante = saldoResultante;
            return this;
        }

        public Builder fornecedor(Fornecedor fornecedor) {
            this.fornecedor = fornecedor;
            return this;
        }

        public Builder documentoReferencia(String documentoReferencia) {
            this.documentoReferencia = documentoReferencia;
            return this;
        }

        public Builder destino(String destino) {
            this.destino = destino;
            return this;
        }

        public Builder justificativa(String justificativa) {
            this.justificativa = justificativa;
            return this;
        }

        public Builder observacao(String observacao) {
            this.observacao = observacao;
            return this;
        }

        public Builder dataHora(LocalDateTime dataHora) {
            this.dataHora = dataHora;
            return this;
        }

        public Movimentacao build() {
            return new Movimentacao(this);
        }
    }
}
