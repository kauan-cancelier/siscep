import { describe, expect, it } from 'vitest';
import { paginaInicial, pode } from './permissoes';

describe('matriz de permissoes', () => {
  it('somente o almoxarife registra entradas e ajustes', () => {
    expect(pode('ALMOXARIFE', 'entrada')).toBe(true);
    expect(pode('ALMOXARIFE', 'ajuste')).toBe(true);
    for (const p of ['ADMINISTRADOR', 'OPERADOR', 'GESTOR'] as const) {
      expect(pode(p, 'entrada')).toBe(false);
      expect(pode(p, 'ajuste')).toBe(false);
    }
  });

  it('operador registra saidas mas nao acessa relatorios nem cadastros', () => {
    expect(pode('OPERADOR', 'saida')).toBe(true);
    expect(pode('OPERADOR', 'relatorios')).toBe(false);
    expect(pode('OPERADOR', 'cadastros')).toBe(false);
  });

  it('gestor apenas consulta', () => {
    expect(pode('GESTOR', 'dashboard')).toBe(true);
    expect(pode('GESTOR', 'relatorios')).toBe(true);
    expect(pode('GESTOR', 'saida')).toBe(false);
    expect(pode('GESTOR', 'cadastros')).toBe(false);
  });

  it('somente o administrador gerencia usuarios', () => {
    expect(pode('ADMINISTRADOR', 'usuarios')).toBe(true);
    expect(pode('ALMOXARIFE', 'usuarios')).toBe(false);
    expect(pode(undefined, 'estoque')).toBe(false);
  });

  it('define a pagina inicial por perfil', () => {
    expect(paginaInicial('GESTOR')).toBe('/dashboard');
    expect(paginaInicial('OPERADOR')).toBe('/movimentacoes/saida');
  });
});
