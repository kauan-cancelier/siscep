import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { mockFetch, paginaVazia, renderizar, usuarioFake } from '../test/utils';

describe('Login', () => {
  it('exibe a mensagem do servidor quando as credenciais sao invalidas', async () => {
    mockFetch(() => ({ status: 401, corpo: { status: 401, mensagem: 'E-mail ou senha invalidos.' } }));
    renderizar('/login');
    await userEvent.type(screen.getByLabelText('E-mail'), 'x@dda.com.br');
    await userEvent.type(screen.getByLabelText('Senha'), 'errada');
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('E-mail ou senha invalidos.');
    expect(localStorage.getItem('siscep.token')).toBeNull();
  });

  it('autentica, guarda o token e redireciona o operador para a tela de saida', async () => {
    mockFetch((url) =>
      url.includes('/auth/login')
        ? { corpo: { token: 'abc', tipo: 'Bearer', expiraEmSegundos: 28800, usuario: usuarioFake('OPERADOR') } }
        : url.includes('/api/pastilhas') ? { corpo: paginaVazia } : undefined,
    );
    renderizar('/login');
    await userEvent.type(screen.getByLabelText('E-mail'), 'op@dda.com.br');
    await userEvent.type(screen.getByLabelText('Senha'), 'Senha@123');
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));
    expect(await screen.findByRole('heading', { name: 'Registrar saida' })).toBeInTheDocument();
    expect(localStorage.getItem('siscep.token')).toBe('abc');
  });

  it('redireciona para o login quando nao ha sessao', async () => {
    mockFetch(() => undefined);
    renderizar('/estoque');
    await waitFor(() => expect(screen.getByRole('button', { name: 'Entrar' })).toBeInTheDocument());
  });
});
