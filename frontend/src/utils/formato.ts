import type { Perfil, Situacao, TipoMovimentacao } from '../api/tipos';

export const NOMES_PERFIL: Record<Perfil, string> = {
  ADMINISTRADOR: 'Administrador',
  ALMOXARIFE: 'Almoxarife',
  OPERADOR: 'Operador',
  GESTOR: 'Gestor',
};

export const NOMES_SITUACAO: Record<Situacao, string> = { NORMAL: 'Normal', CRITICO: 'Crítico', ZERADO: 'Zerado' };

export const NOMES_TIPO: Record<TipoMovimentacao, string> = { ENTRADA: 'Entrada', SAIDA: 'Saída', AJUSTE: 'Ajuste' };

export function dataHora(iso: string | null | undefined): string {
  if (!iso) return '-';
  const d = new Date(iso);
  return Number.isNaN(d.getTime())
    ? '-'
    : d.toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

export function dataCurta(iso: string): string {
  const [a, m, d] = iso.slice(0, 10).split('-');
  return `${d}/${m}/${a}`;
}

export function numero(n: number): string {
  return n.toLocaleString('pt-BR');
}

export function isoHoje(deslocDias = 0): string {
  const d = new Date();
  d.setDate(d.getDate() + deslocDias);
  const p = (x: number) => String(x).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

export function formatarCnpj(c: string | null): string {
  if (!c) return '-';
  const d = c.replace(/\D/g, '');
  return d.length === 14 ? d.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/, '$1.$2.$3/$4-$5') : c;
}
