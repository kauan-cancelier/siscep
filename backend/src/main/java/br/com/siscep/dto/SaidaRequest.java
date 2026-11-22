package br.com.siscep.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaidaRequest(
        @NotNull(message = "informe a pastilha") Long pastilhaId,
        @NotNull(message = "informe a quantidade") @Min(value = 1, message = "a quantidade deve ser maior que zero")
        Integer quantidade,
        @NotBlank(message = "informe o destino (ordem de produção, máquina ou setor)") @Size(max = 120) String destino,
        @Size(max = 500) String observacao) {
}
