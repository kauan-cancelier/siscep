package br.com.siscep.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/** Log de tentativas de login consultado pelo administrador (secao 4.1). */
@Entity
@Immutable
@Table(name = "registro_acesso")
public class RegistroAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false)
    private boolean sucesso;

    @Column(length = 120)
    private String motivo;

    @Column(length = 45)
    private String ip;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    protected RegistroAcesso() {
    }

    public RegistroAcesso(String email, boolean sucesso, String motivo, String ip, LocalDateTime dataHora) {
        this.email = email;
        this.sucesso = sucesso;
        this.motivo = motivo;
        this.ip = ip;
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getIp() {
        return ip;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
