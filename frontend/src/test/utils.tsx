import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi } from 'vitest';
import { AuthProvider } from '../auth/AuthContext';
import { Rotas } from '../App';
import type { Perfil, Usuario } from '../api/tipos';

export function usuarioFake(perfil: Perfil): Usuario {
  return { id: 1, nome: `Usuario ${perfil}`, email: 'u@dda.com.br', perfil, ativo: true, bloqueado: false, ultimoAcesso: null, criadoEm: '2026-10-01T10:00:00' };
}

type Rota = (url: string, init?: RequestInit) => { status?: number; corpo?: unknown } | undefined;

export function mockFetch(rota: Rota) {
  const fn = vi.fn(async (url: string, init?: RequestInit) => {
    const r = rota(url, init) ?? { status: 200, corpo: [] };
    const status = r.status ?? 200;
    return new Response(r.corpo === undefined ? null : JSON.stringify(r.corpo), {
      status,
      headers: { 'Content-Type': 'application/json' },
    });
  });
  vi.stubGlobal('fetch', fn);
  return fn;
}

export function renderizar(caminho: string) {
  return render(
    <MemoryRouter initialEntries={[caminho]} future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <AuthProvider>
        <Rotas />
      </AuthProvider>
    </MemoryRouter>,
  );
}

export const paginaVazia = { conteudo: [], pagina: 0, tamanho: 20, totalElementos: 0, totalPaginas: 0 };
