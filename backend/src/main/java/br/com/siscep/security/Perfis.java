package br.com.siscep.security;

/** Expressoes de autorizacao derivadas da matriz de permissoes (secao 4.5 do documento do sistema). */
public final class Perfis {

    public static final String ADMIN = "hasRole('ADMINISTRADOR')";
    public static final String CADASTROS = "hasAnyRole('ADMINISTRADOR','ALMOXARIFE')";
    public static final String ALMOXARIFE = "hasRole('ALMOXARIFE')";
    public static final String SAIDA = "hasAnyRole('ALMOXARIFE','OPERADOR')";
    public static final String GESTAO = "hasAnyRole('ADMINISTRADOR','ALMOXARIFE','GESTOR')";

    private Perfis() {
    }
}
