import { useState } from 'react';
import { Download } from 'lucide-react';
import { api } from '../api/cliente';
import type { Movimentacao, Pagina, TipoMovimentacao, Usuario } from '../api/tipos';
import { useAuth } from '../auth/AuthContext';
import { Carregando, Mensagem, Paginacao, Titulo, Vazio } from '../components/Ui';
import { dataHora, isoHoje, NOMES_TIPO } from '../utils/formato';
import { useCarregar } from '../utils/useCarregar';

const COR_TIPO: Record<TipoMovimentacao, string> = {
  ENTRADA: 'bg-emerald-100 text-emerald-800',
  SAIDA: 'bg-sky-100 text-sky-800',
  AJUSTE: 'bg-violet-100 text-violet-800',
};

export default function Historico() {
  const { usuario, pode } = useAuth();
  const [inicio, setInicio] = useState(isoHoje(-29));
  const [fim, setFim] = useState(isoHoje());
  const [tipo, setTipo] = useState('');
  const [usuarioId, setUsuarioId] = useState('');
  const [codigo, setCodigo] = useState('');
  const [pagina, setPagina] = useState(0);
  const operador = usuario?.perfil === 'OPERADOR';
  const usuarios = useCarregar(
    () => (pode('usuarios') ? api.get<Usuario[]>('/api/usuarios') : Promise.resolve([] as Usuario[])),
    [],
  );
  const pastilhaFiltro = useCarregar(
    () => (codigo.trim() ? api.get<Pagina<{ id: number }>>('/api/pastilhas', { termo: codigo, size: 1 }) : Promise.resolve(null)),
    [codigo],
  );
  const pastilhaId = codigo.trim() ? (pastilhaFiltro.dados?.conteudo[0]?.id ?? -1) : undefined;
  const filtros = { inicio, fim, tipo, usuarioId, pastilhaId };
  const { dados, erro, carregando } = useCarregar(
    () => api.get<Pagina<Movimentacao>>('/api/movimentacoes', { ...filtros, page: pagina, size: 20 }),
    [inicio, fim, tipo, usuarioId, pastilhaId, pagina],
  );

  return (
    <>
      <Titulo
        titulo="Histórico de movimentações"
        descricao={operador ? 'Exibindo somente as movimentações registradas por você' : 'Registros imutáveis de entradas, saídas e ajustes'}
        acoes={
          pode('relatorios') && (
            <>
              <button type="button" className="btn-secundario" onClick={() => api.baixar('/api/relatorios/movimentacoes/exportar', { ...filtros, formato: 'pdf' }, 'movimentacoes.pdf')}>
                <Download className="h-4 w-4" aria-hidden /> PDF
              </button>
              <button type="button" className="btn-secundario" onClick={() => api.baixar('/api/relatorios/movimentacoes/exportar', { ...filtros, formato: 'csv' }, 'movimentacoes.csv')}>
                <Download className="h-4 w-4" aria-hidden /> CSV
              </button>
            </>
          )
        }
      />
      <div className="cartao mb-4 grid gap-3 p-4 sm:grid-cols-2 lg:grid-cols-5">
        <div><label htmlFor="hi" className="rotulo">De</label><input id="hi" type="date" className="campo" value={inicio} onChange={(e) => { setInicio(e.target.value); setPagina(0); }} /></div>
        <div><label htmlFor="hf" className="rotulo">Até</label><input id="hf" type="date" className="campo" value={fim} onChange={(e) => { setFim(e.target.value); setPagina(0); }} /></div>
        <div><label htmlFor="hc" className="rotulo">Pastilha (código)</label><input id="hc" className="campo" value={codigo} onChange={(e) => { setCodigo(e.target.value); setPagina(0); }} /></div>
        <div>
          <label htmlFor="ht" className="rotulo">Tipo</label>
          <select id="ht" className="campo" value={tipo} onChange={(e) => { setTipo(e.target.value); setPagina(0); }}>
            <option value="">Todos</option><option value="ENTRADA">Entrada</option><option value="SAIDA">Saída</option><option value="AJUSTE">Ajuste</option>
          </select>
        </div>
        {pode('usuarios') && (
          <div>
            <label htmlFor="hu" className="rotulo">Usuário</label>
            <select id="hu" className="campo" value={usuarioId} onChange={(e) => { setUsuarioId(e.target.value); setPagina(0); }}>
              <option value="">Todos</option>
              {usuarios.dados?.map((u) => <option key={u.id} value={u.id}>{u.nome}</option>)}
            </select>
          </div>
        )}
      </div>
      {erro && <Mensagem tipo="erro">{erro}</Mensagem>}
      <div className="cartao overflow-hidden">
        {carregando && !dados ? <Carregando /> : dados && dados.conteudo.length === 0 ? <Vazio texto="Nenhuma movimentação encontrada." /> : (
          <div className="overflow-x-auto">
            <table className="tabela">
              <thead><tr><th>Data/hora</th><th>Tipo</th><th>Pastilha</th><th className="text-right">Qtd</th><th className="text-right">Saldo</th><th className="hidden md:table-cell">Usuário</th><th className="hidden lg:table-cell">Detalhes</th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {dados?.conteudo.map((m) => (
                  <tr key={m.id}>
                    <td className="whitespace-nowrap">{dataHora(m.dataHora)}</td>
                    <td><span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${COR_TIPO[m.tipo]}`}>{NOMES_TIPO[m.tipo]}</span></td>
                    <td><span className="font-medium">{m.pastilhaCodigo}</span><span className="block text-xs text-slate-500">{m.pastilhaDescricao}</span></td>
                    <td className="text-right">{m.quantidade}</td>
                    <td className="whitespace-nowrap text-right">{m.saldoAnterior} → <strong>{m.saldoResultante}</strong></td>
                    <td className="hidden md:table-cell">{m.usuarioNome}</td>
                    <td className="hidden max-w-xs text-xs text-slate-600 lg:table-cell">
                      {[m.fornecedorNome, m.documentoReferencia && `NF ${m.documentoReferencia}`, m.destino, m.justificativa, m.observacao].filter(Boolean).join(' | ') || '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {dados && <Paginacao pagina={dados.pagina} totalPaginas={dados.totalPaginas} totalElementos={dados.totalElementos} aoMudar={setPagina} />}
      </div>
    </>
  );
}
