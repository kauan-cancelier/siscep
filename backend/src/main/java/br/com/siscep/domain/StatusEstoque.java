package br.com.siscep.domain;

/**
 * Situacao do item (RN08): ZERADO quando saldo = 0, CRITICO quando 0 &lt; saldo &lt;= minimo,
 * NORMAL quando saldo &gt; minimo.
 */
public enum StatusEstoque {
    NORMAL,
    CRITICO,
    ZERADO;

    public static StatusEstoque de(int saldo, int estoqueMinimo) {
        if (saldo <= 0) {
            return ZERADO;
        }
        return saldo <= estoqueMinimo ? CRITICO : NORMAL;
    }
}
