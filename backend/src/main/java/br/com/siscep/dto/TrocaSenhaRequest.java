package br.com.siscep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrocaSenhaRequest(
        @NotBlank(message = "informe a senha atual") String senhaAtual,
        @NotBlank @Size(min = 8, max = 72, message = "a nova senha deve ter entre 8 e 72 caracteres") String novaSenha) {
}
