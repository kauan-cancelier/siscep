import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import type { Pastilha } from '../api/tipos';
import { mockFetch, renderizar, usuarioFake } from '../test/utils';

const pastilha: Pastilha = {
  id: 7, codigo: 'CNMG120408', descricao: 'Pastilha torneamento', especificacao: null, unidadeMedida: 'UN',
  fabricanteId: null, fabricanteNome: null, fornecedorPrincipalId: null, fornecedorPrincipalNome: null,
  estoqueMinimo: 5, saldoAtual: 3, situacao: 'CRITICO', ativo: true,
};

describe('Registrar saída', () => {
  it('impede saída acima do saldo e registra saída válida', async () => {
    localStorage.setItem('siscep.token', 't');
    const fetch = mockFetch((url, init) => {
      if (url.includes('/auth/me')) return { corpo: usuarioFake('OPERADOR') };
      if (url.includes('/api/pastilhas')) return { corpo: { conteudo: [pastilha], pagina: 0, tamanho: 10, totalElementos: 1, totalPaginas: 1 } };
      if (url.includes('/movimentacoes/saidas') && init?.method === 'POST')
        return { status: 201, corpo: { id: 1, tipo: 'SAIDA', pastilhaCodigo: 'CNMG120408', saldoAnterior: 3, saldoResultante: 1 } };
      return undefined;
    });
    renderizar('/movimentacoes/saida');
    await userEvent.click(await screen.findByLabelText('Pastilha'));
    await userEvent.click(await screen.findByRole('button', { name: /CNMG120408/ }));
    await userEvent.type(screen.getByLabelText('Quantidade'), '4');
    expect(screen.getByText(/Saldo insuficiente: disponível 3/)).toBeInTheDocument();
    const confirmar = screen.getByRole('button', { name: 'Confirmar saída' });
    expect(confirmar).toBeDisabled();

    await userEvent.clear(screen.getByLabelText('Quantidade'));
    await userEvent.type(screen.getByLabelText('Quantidade'), '2');
    await userEvent.type(screen.getByLabelText(/Destino/), 'OP-1020 Torno CNC 2');
    await userEvent.click(confirmar);
    expect(await screen.findByText(/saldo 3 → 1/)).toBeInTheDocument();
    const post = fetch.mock.calls.find(([u]) => String(u).includes('/saidas'));
    expect(JSON.parse(String(post?.[1]?.body))).toMatchObject({ pastilhaId: 7, quantidade: 2, destino: 'OP-1020 Torno CNC 2' });
  });
});
