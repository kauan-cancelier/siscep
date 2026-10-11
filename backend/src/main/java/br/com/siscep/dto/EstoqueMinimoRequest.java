package br.com.siscep.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EstoqueMinimoRequest(
        @NotNull(message = "informe o estoque minimo") @Min(value = 0, message = "o estoque minimo deve ser >= 0")
        Integer estoqueMinimo) {
}
