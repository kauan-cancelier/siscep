import { describe, expect, it } from 'vitest';
import { dataCurta, dataHora, formatarCnpj, numero } from './formato';
import { montarQuery } from '../api/cliente';

describe('formatacao', () => {
  it('formata CNPJ com 14 digitos', () => {
    expect(formatarCnpj('12345678000190')).toBe('12.345.678/0001-90');
    expect(formatarCnpj(null)).toBe('-');
  });
  it('formata datas no padrão brasileiro', () => {
    expect(dataCurta('2026-10-14')).toBe('14/10/2026');
    expect(dataHora('2026-10-14T08:05:00')).toContain('14/10/2026');
    expect(dataHora(null)).toBe('-');
  });
  it('formata números com separador de milhar', () => {
    expect(numero(12500)).toBe('12.500');
  });
  it('monta query string ignorando valores vazios', () => {
    expect(montarQuery({ a: 1, b: '', c: undefined, d: null, e: 'x y' })).toBe('?a=1&e=x+y');
    expect(montarQuery({})).toBe('');
  });
});
