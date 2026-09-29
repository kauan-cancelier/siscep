package br.com.siscep.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PastilhaFornecedorId implements Serializable {

    @Column(name = "pastilha_id")
    private Long pastilhaId;

    @Column(name = "fornecedor_id")
    private Long fornecedorId;

    protected PastilhaFornecedorId() {
    }

    public PastilhaFornecedorId(Long pastilhaId, Long fornecedorId) {
        this.pastilhaId = pastilhaId;
        this.fornecedorId = fornecedorId;
    }

    public Long getPastilhaId() {
        return pastilhaId;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PastilhaFornecedorId that)) {
            return false;
        }
        return Objects.equals(pastilhaId, that.pastilhaId) && Objects.equals(fornecedorId, that.fornecedorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pastilhaId, fornecedorId);
    }
}
