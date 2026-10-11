package br.com.siscep.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EntradaRequest(
        @NotNull(message = "informe a pastilha") Long pastilhaId,
        @NotNull(message = "informe a quantidade") @Min(value = 1, message = "a quantidade deve ser maior que zero")
        Integer quantidade,
        @NotNull(message = "informe o fornecedor de origem") Long fornecedorId,
        @Size(max = 60) String documentoReferencia,
        @Size(max = 500) String observacao) {
}
