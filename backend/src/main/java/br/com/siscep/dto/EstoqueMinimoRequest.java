package br.com.siscep.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EstoqueMinimoRequest(
        @NotNull(message = "informe o estoque mínimo") @Min(value = 0, message = "o estoque mínimo deve ser >= 0")
        Integer estoqueMinimo) {
}
