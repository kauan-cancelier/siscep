import { useState } from 'react';
import { Download } from 'lucide-react';
import { api } from '../api/cliente';
import type { Fabricante, Fornecedor, Pagina, Pastilha, Situacao } from '../api/tipos';
import { useAuth } from '../auth/AuthContext';
import { Carregando, Mensagem, Paginacao, SituacaoBadge, Titulo, Vazio } from '../components/Ui';
import { useCarregar } from '../utils/useCarregar';

export default function Estoque() {
  const { pode } = useAuth();
  const [termo, setTermo] = useState('');
  const [fabricanteId, setFabricanteId] = useState('');
  const [fornecedorId, setFornecedorId] = useState('');
  const [situacao, setSituacao] = useState<Situacao | ''>('');
  const [ordem, setOrdem] = useState('codigo,asc');
  const [pagina, setPagina] = useState(0);
  const fabricantes = useCarregar(() => api.get<Fabricante[]>('/api/fabricantes'), []);
  const fornecedores = useCarregar(() => api.get<Fornecedor[]>('/api/fornecedores'), []);
  const { dados, erro, carregando } = useCarregar(
    () =>
      api.get<Pagina<Pastilha>>('/api/pastilhas', {
        termo, fabricanteId, fornecedorId, situacao, ativo: true, page: pagina, size: 20, sort: ordem,
      }),
    [termo, fabricanteId, fornecedorId, situacao, ordem, pagina],
  );
  const filtro = (fn: (v: string) => void) => (v: string) => {
    fn(v);
    setPagina(0);
  };

  return (
    <>
      <Titulo
        titulo="Estoque atual"
        descricao="Saldo de cada pastilha ativa; itens criticos e zerados sao destacados"
        acoes={
          pode('relatorios') && (
            <>
              <button type="button" className="btn-secundario" onClick={() => api.baixar('/api/relatorios/estoque/exportar', { formato: 'pdf' }, 'estoque.pdf')}>
                <Download className="h-4 w-4" aria-hidden /> PDF
              </button>
              <button type="button" className="btn-secundario" onClick={() => api.baixar('/api/relatorios/estoque/exportar', { formato: 'csv' }, 'estoque.csv')}>
                <Download className="h-4 w-4" aria-hidden /> CSV
              </button>
            </>
          )
        }
      />
      <div className="cartao mb-4 grid gap-3 p-4 sm:grid-cols-2 lg:grid-cols-5">
        <div className="lg:col-span-2">
          <label htmlFor="termo" className="rotulo">Codigo, descricao ou especificacao</label>
          <input id="termo" className="campo" value={termo} onChange={(e) => filtro(setTermo)(e.target.value)} placeholder="Ex.: CNMG" />
        </div>
        <div>
          <label htmlFor="fab" className="rotulo">Fabricante</label>
          <select id="fab" className="campo" value={fabricanteId} onChange={(e) => filtro(setFabricanteId)(e.target.value)}>
            <option value="">Todos</option>
            {fabricantes.dados?.map((f) => <option key={f.id} value={f.id}>{f.nome}</option>)}
          </select>
        </div>
        <div>
          <label htmlFor="forn" className="rotulo">Fornecedor</label>
          <select id="forn" className="campo" value={fornecedorId} onChange={(e) => filtro(setFornecedorId)(e.target.value)}>
            <option value="">Todos</option>
            {fornecedores.dados?.map((f) => <option key={f.id} value={f.id}>{f.razaoSocial}</option>)}
          </select>
        </div>
        <div>
          <label htmlFor="sit" className="rotulo">Situacao</label>
          <select id="sit" className="campo" value={situacao} onChange={(e) => filtro((v) => setSituacao(v as Situacao | ''))(e.target.value)}>
            <option value="">Todas</option>
            <option value="NORMAL">Normal</option>
            <option value="CRITICO">Critico</option>
            <option value="ZERADO">Zerado</option>
          </select>
        </div>
      </div>
      {erro && <Mensagem tipo="erro">{erro}</Mensagem>}
      <div className="cartao overflow-hidden">
        <div className="flex justify-end border-b border-slate-200 px-3 py-2">
          <label htmlFor="ordem" className="sr-only">Ordenar por</label>
          <select id="ordem" className="campo w-auto py-1" value={ordem} onChange={(e) => setOrdem(e.target.value)}>
            <option value="codigo,asc">Codigo (A-Z)</option>
            <option value="descricao,asc">Descricao (A-Z)</option>
            <option value="saldoAtual,asc">Menor saldo</option>
            <option value="saldoAtual,desc">Maior saldo</option>
          </select>
        </div>
        {carregando && !dados ? (
          <Carregando />
        ) : dados && dados.conteudo.length === 0 ? (
          <Vazio texto="Nenhuma pastilha encontrada para os filtros informados." />
        ) : (
          <div className="overflow-x-auto">
            <table className="tabela">
              <thead>
                <tr><th>Codigo</th><th>Descricao</th><th className="hidden md:table-cell">Fabricante</th><th className="hidden lg:table-cell">Fornecedor</th><th className="text-right">Saldo</th><th className="text-right">Minimo</th><th>Situacao</th></tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {dados?.conteudo.map((p) => (
                  <tr key={p.id} className={p.situacao === 'ZERADO' ? 'bg-red-50/60' : p.situacao === 'CRITICO' ? 'bg-amber-50/60' : ''}>
                    <td className="font-medium">{p.codigo}</td>
                    <td>{p.descricao}{p.especificacao && <span className="block text-xs text-slate-500">{p.especificacao}</span>}</td>
                    <td className="hidden md:table-cell">{p.fabricanteNome ?? '-'}</td>
                    <td className="hidden lg:table-cell">{p.fornecedorPrincipalNome ?? '-'}</td>
                    <td className="text-right font-semibold">{p.saldoAtual} <span className="text-xs font-normal text-slate-500">{p.unidadeMedida}</span></td>
                    <td className="text-right">{p.estoqueMinimo}</td>
                    <td><SituacaoBadge situacao={p.situacao} /></td>
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
