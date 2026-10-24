package br.com.siscep.service;

import br.com.siscep.exception.RegraNegocioException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Validacao de intervalos de datas usados em consultas e relatorios. */
public final class Periodo {

    static final long MAXIMO_DIAS = 366 * 2;

    private Periodo() {
    }

    public static void validar(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new RegraNegocioException("Informe a data inicial e a data final do periodo.");
        }
        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data inicial nao pode ser posterior a data final.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) > MAXIMO_DIAS) {
            throw new RegraNegocioException("O periodo maximo de consulta e de dois anos.");
        }
    }

    public static long dias(LocalDate inicio, LocalDate fim) {
        return ChronoUnit.DAYS.between(inicio, fim) + 1;
    }
}
