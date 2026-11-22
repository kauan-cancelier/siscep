package br.com.siscep.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AjusteRequest(
        @NotNull(message = "informe a pastilha") Long pastilhaId,
        @NotNull(message = "informe a contagem física") @Min(value = 0, message = "a contagem não pode ser negativa")
        Integer contagemFisica,
        @NotBlank(message = "a justificativa do ajuste é obrigatória") @Size(max = 500) String justificativa) {
}
