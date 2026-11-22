package br.com.siscep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(
        @NotBlank(message = "informe a razão social") @Size(max = 160) String razaoSocial,
        @Pattern(regexp = "^$|^\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}$", message = "CNPJ inválido") String cnpj,
        @Size(max = 120) String contato,
        @Size(max = 20) String telefone,
        @Email(message = "e-mail inválido") @Size(max = 160) String email,
        @Min(value = 0, message = "o prazo médio não pode ser negativo") Integer prazoMedioEntregaDias) {
}
