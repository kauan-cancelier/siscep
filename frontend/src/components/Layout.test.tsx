import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { Perfil } from '../api/tipos';
import { mockFetch, paginaVazia, renderizar, usuarioFake } from '../test/utils';

function logadoComo(perfil: Perfil) {
  localStorage.setItem('siscep.token', 't');
  mockFetch((url) => {
    if (url.includes('/auth/me')) return { corpo: usuarioFake(perfil) };
    if (url.includes('/api/pastilhas') || url.includes('/api/movimentacoes')) return { corpo: paginaVazia };
    return undefined;
  });
}

describe('menu por perfil', () => {
  it('operador ve apenas estoque, saida e historico', async () => {
    logadoComo('OPERADOR');
    renderizar('/estoque');
    expect(await screen.findByRole('link', { name: /Registrar saida/ })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Registrar entrada/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Usuarios/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Relatorios/ })).not.toBeInTheDocument();
  });

  it('administrador gerencia usuarios mas nao movimenta estoque', async () => {
    logadoComo('ADMINISTRADOR');
    renderizar('/estoque');
    expect(await screen.findByRole('link', { name: /Usuarios/ })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Registrar saida/ })).not.toBeInTheDocument();
  });

  it('bloqueia acesso direto por URL a tela sem permissao', async () => {
    logadoComo('GESTOR');
    renderizar('/movimentacoes/entrada');
    expect(await screen.findByText(/nao tem permissao/)).toBeInTheDocument();
  });
});
