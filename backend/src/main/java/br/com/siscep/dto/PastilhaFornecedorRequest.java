package br.com.siscep.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PastilhaFornecedorRequest(
        @NotNull(message = "informe o fornecedor") Long fornecedorId,
        @DecimalMin(value = "0.0", message = "o preco nao pode ser negativo") BigDecimal precoReferencia,
        @Min(value = 0, message = "o prazo nao pode ser negativo") Integer prazoEntregaDias) {
}
