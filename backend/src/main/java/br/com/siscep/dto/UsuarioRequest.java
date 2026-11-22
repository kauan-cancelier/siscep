package br.com.siscep.dto;

import br.com.siscep.domain.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A senha e obrigatoria apenas na criacao; na edicao e ignorada (use a redefinicao de senha). */
public record UsuarioRequest(
        @NotBlank(message = "informe o nome") @Size(max = 120) String nome,
        @NotBlank(message = "informe o e-mail") @Email(message = "e-mail inválido") @Size(max = 160) String email,
        @Size(min = 8, max = 72, message = "a senha deve ter entre 8 e 72 caracteres") String senha,
        @NotNull(message = "informe o perfil") PerfilUsuario perfil) {
}
