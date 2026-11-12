import { useState } from 'react';
import { api } from '../api/cliente';
import type { Auditoria, Pagina } from '../api/tipos';
import { Carregando, Mensagem, Paginacao, Titulo, Vazio } from '../components/Ui';
import { useCarregar } from '../utils/useCarregar';
import { dataHora } from '../utils/formato';

const ROTULOS_ACAO: Record<string, string> = {
  CRIAR: 'Criou',
  ATUALIZAR: 'Atualizou',
  ATIVAR: 'Ativou',
  INATIVAR: 'Inativou',
  EXCLUIR: 'Excluiu',
  REDEFINIR_SENHA: 'Redefiniu senha',
  DESBLOQUEAR: 'Desbloqueou',
  MOVIMENTACAO_ENTRADA: 'Entrada',
  MOVIMENTACAO_SAIDA: 'Saída',
  MOVIMENTACAO_AJUSTE: 'Ajuste',
};

const CORES_ACAO: Record<string, string> = {
  CRIAR: 'bg-emerald-100 text-emerald-800',
  ATUALIZAR: 'bg-sky-100 text-sky-800',
  ATIVAR: 'bg-emerald-100 text-emerald-800',
  INATIVAR: 'bg-amber-100 text-amber-800',
  EXCLUIR: 'bg-red-100 text-red-800',
  REDEFINIR_SENHA: 'bg-violet-100 text-violet-800',
  DESBLOQUEAR: 'bg-violet-100 text-violet-800',
  MOVIMENTACAO_ENTRADA: 'bg-emerald-100 text-emerald-800',
  MOVIMENTACAO_SAIDA: 'bg-amber-100 text-amber-800',
  MOVIMENTACAO_AJUSTE: 'bg-sky-100 text-sky-800',
};

export default function AuditoriaPagina() {
  const [pagina, setPagina] = useState(0);
  const { dados, erro, carregando } = useCarregar(
    () => api.get<Pagina<Auditoria>>('/api/auditoria', { page: pagina, size: 20 }),
    [pagina],
  );

  return (
    <>
      <Titulo titulo="Auditoria" descricao="Trilha de auditoria: quem executou cada ação de escrita no sistema" />
      {erro && <div className="mb-3"><Mensagem tipo="erro">{erro}</Mensagem></div>}
      <div className="cartao overflow-x-auto">
        {carregando ? <Carregando /> : dados?.conteudo.length === 0 ? (
          <Vazio texto="Nenhuma ação registrada ainda." dica="Criações, edições, movimentações e exclusões passam a aparecer aqui." />
        ) : (
          <table className="tabela">
            <thead>
              <tr><th>Data/hora</th><th>Usuário</th><th>Ação</th><th>Entidade</th><th>Detalhes</th></tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {dados?.conteudo.map((a) => (
                <tr key={a.id}>
                  <td className="whitespace-nowrap text-sm text-slate-500">{dataHora(a.criadoEm)}</td>
                  <td className="font-medium">{a.usuarioNome}</td>
                  <td>
                    <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${CORES_ACAO[a.acao] ?? 'bg-slate-200 text-slate-600'}`}>
                      {ROTULOS_ACAO[a.acao] ?? a.acao}
                    </span>
                  </td>
                  <td className="text-sm">{a.entidade}{a.entidadeId != null ? ` #${a.entidadeId}` : ''}</td>
                  <td className="text-sm text-slate-600">{a.detalhes ?? '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
      {dados && <Paginacao pagina={dados.pagina} totalPaginas={dados.totalPaginas} totalElementos={dados.totalElementos} aoMudar={setPagina} />}
    </>
  );
}
