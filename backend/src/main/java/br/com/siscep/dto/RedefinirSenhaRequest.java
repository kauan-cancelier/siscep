package br.com.siscep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank @Size(min = 8, max = 72, message = "a senha deve ter entre 8 e 72 caracteres") String novaSenha) {
}
