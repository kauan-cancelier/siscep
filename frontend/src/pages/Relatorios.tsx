import { useState } from 'react';
import { Download } from 'lucide-react';
import { api } from '../api/cliente';
import type { ConsumoItem, ReposicaoItem } from '../api/tipos';
import { Carregando, Mensagem, SituacaoBadge, Titulo, Vazio } from '../components/Ui';
import { isoHoje } from '../utils/formato';
import { mensagemErro, useCarregar } from '../utils/useCarregar';

function BotoesExportar({ url, params, nome }: { url: string; params: Record<string, string>; nome: string }) {
  const [erro, setErro] = useState<string | null>(null);
  const baixar = (formato: string) => api.baixar(url, { ...params, formato }, `${nome}.${formato}`).catch((e) => setErro(mensagemErro(e)));
  return (
    <div className="flex flex-wrap items-center gap-2">
      <button type="button" className="btn-secundario" onClick={() => baixar('pdf')}><Download className="h-4 w-4" aria-hidden /> PDF</button>
      <button type="button" className="btn-secundario" onClick={() => baixar('csv')}><Download className="h-4 w-4" aria-hidden /> CSV</button>
      {erro && <span className="text-sm text-red-600">{erro}</span>}
    </div>
  );
}

export default function Relatorios() {
  const [aba, setAba] = useState<'reposicao' | 'consumo' | 'movimentacoes'>('reposicao');
  const [inicio, setInicio] = useState(isoHoje(-29));
  const [fim, setFim] = useState(isoHoje());
  const reposicao = useCarregar(() => api.get<ReposicaoItem[]>('/api/relatorios/reposicao'), []);
  const consumo = useCarregar(
    () => (aba === 'consumo' ? api.get<ConsumoItem[]>('/api/relatorios/consumo', { inicio, fim }) : Promise.resolve(null)),
    [aba, inicio, fim],
  );
  const abas = [
    { id: 'reposicao', rotulo: 'Necessidade de reposição' },
    { id: 'consumo', rotulo: 'Consumo por pastilha' },
    { id: 'movimentacoes', rotulo: 'Movimentações por período' },
  ] as const;
  const periodo = (
    <div className="flex flex-wrap items-end gap-2">
      <div><label htmlFor="ri" className="rotulo">De</label><input id="ri" type="date" className="campo" value={inicio} onChange={(e) => setInicio(e.target.value)} /></div>
      <div><label htmlFor="rf" className="rotulo">Até</label><input id="rf" type="date" className="campo" value={fim} onChange={(e) => setFim(e.target.value)} /></div>
    </div>
  );

  return (
    <>
      <Titulo titulo="Relatórios" descricao="Emissão e exportação em PDF ou CSV" />
      <div role="tablist" className="mb-4 flex flex-wrap gap-1 border-b border-slate-200">
        {abas.map((a) => (
          <button key={a.id} role="tab" aria-selected={aba === a.id} type="button" onClick={() => setAba(a.id)}
            className={`-mb-px border-b-2 px-3 py-2 text-sm font-medium ${aba === a.id ? 'border-marca-600 text-marca-700' : 'border-transparent text-slate-500 hover:text-slate-700'}`}>
            {a.rotulo}
          </button>
        ))}
      </div>

      {aba === 'reposicao' && (
        <section className="cartao" role="tabpanel">
          <div className="flex flex-col gap-2 border-b border-slate-200 p-4 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-sm text-slate-600">Itens criticos e zerados. Quantidade sugerida = 2 x estoque minimo - saldo atual.</p>
            <BotoesExportar url="/api/relatorios/reposicao/exportar" params={{}} nome="reposicao" />
          </div>
          {reposicao.erro && <Mensagem tipo="erro">{reposicao.erro}</Mensagem>}
          {reposicao.carregando ? <Carregando /> : reposicao.dados?.length === 0 ? <Vazio texto="Nenhum item precisa de reposição." /> : (
            <div className="overflow-x-auto">
              <table className="tabela">
                <thead><tr><th>Código</th><th>Descrição</th><th>Situação</th><th className="text-right">Saldo</th><th className="text-right">Mínimo</th><th className="text-right">Sugerido</th><th>Fornecedor</th><th className="hidden lg:table-cell">Contato</th><th className="text-right">Prazo</th></tr></thead>
                <tbody className="divide-y divide-slate-100">
                  {reposicao.dados?.map((r) => (
                    <tr key={r.pastilhaId}>
                      <td className="font-medium">{r.codigo}</td><td>{r.descricao}</td><td><SituacaoBadge situacao={r.situacao} /></td>
                      <td className="text-right">{r.saldoAtual}</td><td className="text-right">{r.estoqueMinimo}</td>
                      <td className="text-right font-semibold text-marca-700">{r.quantidadeSugerida}</td>
                      <td>{r.fornecedor ?? '-'}</td><td className="hidden text-xs lg:table-cell">{r.contatoFornecedor ?? '-'}</td>
                      <td className="text-right">{r.prazoEntregaDias != null ? `${r.prazoEntregaDias} d` : '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}

      {aba === 'consumo' && (
        <section className="cartao" role="tabpanel">
          <div className="flex flex-col gap-3 border-b border-slate-200 p-4 sm:flex-row sm:items-end sm:justify-between">
            {periodo}
            <BotoesExportar url="/api/relatorios/consumo/exportar" params={{ inicio, fim }} nome="consumo" />
          </div>
          {consumo.erro && <div className="p-4"><Mensagem tipo="erro">{consumo.erro}</Mensagem></div>}
          {consumo.carregando ? <Carregando /> : consumo.dados?.length === 0 ? <Vazio texto="Nenhuma saída no período." /> : (
            <div className="overflow-x-auto">
              <table className="tabela">
                <thead><tr><th>Código</th><th>Descrição</th><th className="text-right">Total consumido</th><th className="text-right">N. saídas</th><th className="text-right">Média diária</th><th className="text-right">Média mensal</th></tr></thead>
                <tbody className="divide-y divide-slate-100">
                  {consumo.dados?.map((c) => (
                    <tr key={c.pastilhaId}>
                      <td className="font-medium">{c.codigo}</td><td>{c.descricao}</td><td className="text-right font-semibold">{c.totalConsumido}</td>
                      <td className="text-right">{c.quantidadeSaidas}</td><td className="text-right">{Number(c.mediaDiaria).toLocaleString('pt-BR')}</td>
                      <td className="text-right">{Number(c.mediaMensal).toLocaleString('pt-BR')}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}

      {aba === 'movimentacoes' && (
        <section className="cartao space-y-4 p-4" role="tabpanel">
          <p className="text-sm text-slate-600">Relatório completo de entradas, saídas e ajustes no período, com usuário, saldo resultante e justificativas.</p>
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
            {periodo}
            <BotoesExportar url="/api/relatorios/movimentacoes/exportar" params={{ inicio, fim }} nome="movimentacoes" />
          </div>
        </section>
      )}
    </>
  );
}
