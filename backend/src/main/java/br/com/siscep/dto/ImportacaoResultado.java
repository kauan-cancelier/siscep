package br.com.siscep.dto;

import java.util.List;

public record ImportacaoResultado(int linhasProcessadas, int pastilhasCriadas, int saldosIniciais,
                                  List<String> erros) {
}
