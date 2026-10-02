package br.com.siscep.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/** RN09: toda alteracao de estoque minimo e registrada com usuario e data. */
@Entity
@Immutable
@Table(name = "historico_estoque_minimo")
public class HistoricoEstoqueMinimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pastilha_id", nullable = false)
    private Pastilha pastilha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "valor_anterior", nullable = false)
    private int valorAnterior;

    @Column(name = "valor_novo", nullable = false)
    private int valorNovo;

    @Column(name = "alterado_em", nullable = false)
    private LocalDateTime alteradoEm;

    protected HistoricoEstoqueMinimo() {
    }

    public HistoricoEstoqueMinimo(Pastilha pastilha, Usuario usuario, int valorAnterior, int valorNovo,
                                  LocalDateTime alteradoEm) {
        this.pastilha = pastilha;
        this.usuario = usuario;
        this.valorAnterior = valorAnterior;
        this.valorNovo = valorNovo;
        this.alteradoEm = alteradoEm;
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

    public int getValorAnterior() {
        return valorAnterior;
    }

    public int getValorNovo() {
        return valorNovo;
    }

    public LocalDateTime getAlteradoEm() {
        return alteradoEm;
    }
}
