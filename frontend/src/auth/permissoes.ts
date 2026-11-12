import type { Perfil } from '../api/tipos';

/** Espelha a matriz de permissoes da secao 4.5 do documento do sistema (validada tambem no backend). */
export type Permissao =
  | 'usuarios'
  | 'cadastros'
  | 'estoqueMinimo'
  | 'entrada'
  | 'saida'
  | 'ajuste'
  | 'estoque'
  | 'historico'
  | 'dashboard'
  | 'relatorios'
  | 'auditoria';

const MATRIZ: Record<Permissao, Perfil[]> = {
  usuarios: ['ADMINISTRADOR'],
  cadastros: ['ADMINISTRADOR', 'ALMOXARIFE'],
  estoqueMinimo: ['ADMINISTRADOR', 'ALMOXARIFE'],
  entrada: ['ALMOXARIFE'],
  saida: ['ALMOXARIFE', 'OPERADOR'],
  ajuste: ['ALMOXARIFE'],
  estoque: ['ADMINISTRADOR', 'ALMOXARIFE', 'OPERADOR', 'GESTOR'],
  historico: ['ADMINISTRADOR', 'ALMOXARIFE', 'OPERADOR', 'GESTOR'],
  dashboard: ['ADMINISTRADOR', 'ALMOXARIFE', 'GESTOR'],
  relatorios: ['ADMINISTRADOR', 'ALMOXARIFE', 'GESTOR'],
  auditoria: ['ADMINISTRADOR'],
};

export function pode(perfil: Perfil | undefined, permissao: Permissao): boolean {
  return !!perfil && MATRIZ[permissao].includes(perfil);
}

export function paginaInicial(perfil: Perfil): string {
  if (pode(perfil, 'dashboard')) return '/dashboard';
  if (pode(perfil, 'saida')) return '/movimentacoes/saida';
  return '/estoque';
}
