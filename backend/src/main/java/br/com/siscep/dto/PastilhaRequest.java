package br.com.siscep.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PastilhaRequest(
        @NotBlank(message = "informe o código") @Size(max = 40) String codigo,
        @NotBlank(message = "informe a descrição") @Size(max = 160) String descricao,
        @Size(max = 500) String especificacao,
        @NotBlank(message = "informe a unidade de medida") @Size(max = 10) String unidadeMedida,
        Long fabricanteId,
        Long fornecedorPrincipalId,
        @NotNull(message = "informe o estoque mínimo") @Min(value = 0, message = "o estoque mínimo deve ser >= 0")
        Integer estoqueMinimo) {
}
