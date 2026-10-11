package br.com.siscep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "informe o e-mail") @Email(message = "e-mail invalido") String email,
        @NotBlank(message = "informe a senha") String senha) {
}
