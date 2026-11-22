import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Bar, BarChart, CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api } from '../api/cliente';
import type { Dashboard as DashboardDados } from '../api/tipos';
import { useAuth } from '../auth/AuthContext';
import { Carregando, Indicador, Mensagem, SituacaoBadge, Titulo, Vazio } from '../components/Ui';
import { dataCurta, isoHoje, numero } from '../utils/formato';
import { useCarregar } from '../utils/useCarregar';

export default function Dashboard() {
  const { pode } = useAuth();
  const [inicio, setInicio] = useState(isoHoje(-29));
  const [fim, setFim] = useState(isoHoje());
  const { dados, erro, carregando } = useCarregar(
    () => api.get<DashboardDados>('/api/dashboard', { inicio, fim }),
    [inicio, fim],
  );

  return (
    <>
      <Titulo
        titulo="Dashboard"
        descricao="Indicadores consolidados do estoque de pastilhas"
        acoes={
          <div className="flex flex-wrap items-end gap-2">
            <div>
              <label htmlFor="ini" className="rotulo">De</label>
              <input id="ini" type="date" className="campo" value={inicio} max={fim} onChange={(e) => setInicio(e.target.value)} />
            </div>
            <div>
              <label htmlFor="fim" className="rotulo">Até</label>
              <input id="fim" type="date" className="campo" value={fim} min={inicio} onChange={(e) => setFim(e.target.value)} />
            </div>
          </div>
        }
      />
      {erro && <Mensagem tipo="erro">{erro}</Mensagem>}
      {carregando && !dados && <Carregando />}
      {dados && (
        <div className="space-y-6">
          <div className="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-6">
            <Indicador rotulo="Itens cadastrados" valor={numero(dados.totalItens)} />
            <Indicador rotulo="Itens críticos" valor={numero(dados.itensCriticos)} destaque={dados.itensCriticos ? 'alerta' : undefined} />
            <Indicador rotulo="Itens zerados" valor={numero(dados.itensZerados)} destaque={dados.itensZerados ? 'perigo' : undefined} />
            <Indicador rotulo="Saldo total (un.)" valor={numero(dados.saldoTotal)} />
            <Indicador rotulo="Movimentações" valor={numero(dados.movimentacoesPeriodo)} />
            <Indicador rotulo="Saídas no período" valor={numero(dados.saidasPeriodo)} />
          </div>
          <div className="grid gap-6 lg:grid-cols-2">
            <section className="cartao p-4" aria-labelledby="g1">
              <h2 id="g1" className="mb-3 font-semibold">Entradas x saídas por dia</h2>
              {dados.movimentoDiario.length === 0 ? (
                <Vazio texto="Sem movimentações no período." />
              ) : (
                <ResponsiveContainer width="100%" height={260}>
                  <LineChart data={dados.movimentoDiario.map((d) => ({ ...d, dia: dataCurta(d.dia).slice(0, 5) }))}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="dia" fontSize={12} />
                    <YAxis fontSize={12} allowDecimals={false} />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="entradas" name="Entradas" stroke="#059669" strokeWidth={2} />
                    <Line type="monotone" dataKey="saidas" name="Saídas" stroke="#dc2626" strokeWidth={2} />
                  </LineChart>
                </ResponsiveContainer>
              )}
            </section>
            <section className="cartao p-4" aria-labelledby="g2">
              <h2 id="g2" className="mb-3 font-semibold">Itens de maior consumo</h2>
              {dados.maiorConsumo.length === 0 ? (
                <Vazio texto="Sem saídas no período." />
              ) : (
                <ResponsiveContainer width="100%" height={260}>
                  <BarChart data={dados.maiorConsumo} layout="vertical" margin={{ left: 20 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis type="number" fontSize={12} allowDecimals={false} />
                    <YAxis type="category" dataKey="codigo" width={110} fontSize={11} />
                    <Tooltip />
                    <Bar dataKey="total" name="Consumido" fill="#1e4078" />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </section>
          </div>
          <section className="cartao" aria-labelledby="g3">
            <div className="flex items-center justify-between border-b border-slate-200 px-4 py-3">
              <h2 id="g3" className="font-semibold">Itens que precisam de atenção ({dados.alertasAbertos} alertas)</h2>
              {pode('relatorios') && <Link to="/relatorios" className="btn-link">Relatório de reposição</Link>}
            </div>
            {dados.itensEmAlerta.length === 0 ? (
              <Vazio texto="Nenhum item crítico ou zerado." />
            ) : (
              <div className="overflow-x-auto">
                <table className="tabela">
                  <thead><tr><th>Código</th><th>Descrição</th><th>Saldo</th><th>Mínimo</th><th>Situação</th></tr></thead>
                  <tbody className="divide-y divide-slate-100">
                    {dados.itensEmAlerta.map((p) => (
                      <tr key={p.id}>
                        <td className="font-medium">{p.codigo}</td><td>{p.descricao}</td><td>{p.saldoAtual}</td><td>{p.estoqueMinimo}</td>
                        <td><SituacaoBadge situacao={p.situacao} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </div>
      )}
    </>
  );
}
