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

import java.time.LocalDateTime;

/** Alerta gerado quando a pastilha atinge o estoque minimo (RN08); resolvido na reposicao. */
@Entity
@Table(name = "alerta_estoque")
public class AlertaEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pastilha_id", nullable = false)
    private Pastilha pastilha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusEstoque situacao;

    @Column(name = "saldo_no_alerta", nullable = false)
    private int saldoNoAlerta;

    @Column(name = "estoque_minimo_no_alerta", nullable = false)
    private int estoqueMinimoNoAlerta;

    @Column(name = "gerado_em", nullable = false)
    private LocalDateTime geradoEm;

    @Column(nullable = false)
    private boolean resolvido;

    @Column(name = "resolvido_em")
    private LocalDateTime resolvidoEm;

    protected AlertaEstoque() {
    }

    public AlertaEstoque(Pastilha pastilha, LocalDateTime geradoEm) {
        this.pastilha = pastilha;
        this.geradoEm = geradoEm;
        atualizar(pastilha);
    }

    public void atualizar(Pastilha p) {
        this.situacao = p.situacao();
        this.saldoNoAlerta = p.getSaldoAtual();
        this.estoqueMinimoNoAlerta = p.getEstoqueMinimo();
    }

    public void resolver(LocalDateTime quando) {
        this.resolvido = true;
        this.resolvidoEm = quando;
    }

    public Long getId() {
        return id;
    }

    public Pastilha getPastilha() {
        return pastilha;
    }

    public StatusEstoque getSituacao() {
        return situacao;
    }

    public int getSaldoNoAlerta() {
        return saldoNoAlerta;
    }

    public int getEstoqueMinimoNoAlerta() {
        return estoqueMinimoNoAlerta;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    public boolean isResolvido() {
        return resolvido;
    }

    public LocalDateTime getResolvidoEm() {
        return resolvidoEm;
    }
}
