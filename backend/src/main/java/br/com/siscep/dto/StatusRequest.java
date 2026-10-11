package br.com.siscep.dto;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull(message = "informe se o registro esta ativo") Boolean ativo) {
}
