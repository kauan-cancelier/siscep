package br.com.siscep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FabricanteRequest(
        @NotBlank(message = "informe o nome") @Size(max = 120) String nome,
        @Size(max = 60) String pais,
        @Size(max = 160) String site) {
}
