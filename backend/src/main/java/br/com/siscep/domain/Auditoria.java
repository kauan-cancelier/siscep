package br.com.siscep.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/** Trilha de auditoria das acoes de escrita do sistema (quem fez o que e quando). */
@Entity
@Immutable
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_nome", nullable = false, length = 150)
    private String usuarioNome;

    @Column(nullable = false, length = 40)
    private String acao;

    @Column(nullable = false, length = 40)
    private String entidade;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(length = 500)
    private String detalhes;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected Auditoria() {
    }

    public Auditoria(Long usuarioId, String usuarioNome, String acao, String entidade, Long entidadeId,
                     String detalhes, LocalDateTime criadoEm) {
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.detalhes = detalhes;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public String getAcao() {
        return acao;
    }

    public String getEntidade() {
        return entidade;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
