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
  it('operador vê apenas estoque, saída e histórico', async () => {
    logadoComo('OPERADOR');
    renderizar('/estoque');
    expect(await screen.findByRole('link', { name: /Registrar saída/ })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Registrar entrada/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Usuários/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Relatórios/ })).not.toBeInTheDocument();
  });

  it('administrador gerencia usuários mas não movimenta estoque', async () => {
    logadoComo('ADMINISTRADOR');
    renderizar('/estoque');
    expect(await screen.findByRole('link', { name: /Usuários/ })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Registrar saída/ })).not.toBeInTheDocument();
  });

  it('bloqueia acesso direto por URL a tela sem permissão', async () => {
    logadoComo('GESTOR');
    renderizar('/movimentacoes/entrada');
    expect(await screen.findByText(/não tem permissão/)).toBeInTheDocument();
  });
});
