package br.com.siscep.exception;

/** Violacao de regra de negocio; mensagem sempre em portugues para o usuario final (RNF13). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
