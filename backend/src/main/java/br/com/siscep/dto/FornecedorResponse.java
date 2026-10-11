package br.com.siscep.dto;

import br.com.siscep.domain.Fornecedor;

public record FornecedorResponse(Long id, String razaoSocial, String cnpj, String contato, String telefone,
                                 String email, Integer prazoMedioEntregaDias, boolean ativo) {

    public static FornecedorResponse de(Fornecedor f) {
        return new FornecedorResponse(f.getId(), f.getRazaoSocial(), f.getCnpj(), f.getContato(), f.getTelefone(),
                f.getEmail(), f.getPrazoMedioEntregaDias(), f.isAtivo());
    }
}
