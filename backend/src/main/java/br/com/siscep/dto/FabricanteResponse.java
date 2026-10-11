package br.com.siscep.dto;

import br.com.siscep.domain.Fabricante;

public record FabricanteResponse(Long id, String nome, String pais, String site, boolean ativo) {

    public static FabricanteResponse de(Fabricante f) {
        return new FabricanteResponse(f.getId(), f.getNome(), f.getPais(), f.getSite(), f.isAtivo());
    }
}
