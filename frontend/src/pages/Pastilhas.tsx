import { useState, type FormEvent } from 'react';
import { Plus, Upload } from 'lucide-react';
import { api } from '../api/cliente';
import type { Fabricante, Fornecedor, HistoricoMinimo, ImportacaoResultado, Pagina, Pastilha } from '../api/tipos';
import { Campo, Carregando, Mensagem, Modal, Paginacao, SituacaoBadge, StatusBadge, Titulo, Vazio } from '../components/Ui';
import { dataHora } from '../utils/formato';
import { mensagemErro, useCarregar } from '../utils/useCarregar';

const VAZIO = { codigo: '', descricao: '', especificacao: '', unidadeMedida: 'UN', fabricanteId: '', fornecedorPrincipalId: '', estoqueMinimo: '0' };

export default function Pastilhas() {
  const [termo, setTermo] = useState('');
  const [pagina, setPagina] = useState(0);
  const { dados, erro, carregando, recarregar } = useCarregar(
    () => api.get<Pagina<Pastilha>>('/api/pastilhas', { termo, page: pagina, size: 20, sort: 'codigo' }),
    [termo, pagina],
  );
  const fabricantes = useCarregar(() => api.get<Fabricante[]>('/api/fabricantes', { apenasAtivos: true }), []);
  const fornecedores = useCarregar(() => api.get<Fornecedor[]>('/api/fornecedores', { apenasAtivos: true }), []);
  const [editando, setEditando] = useState<Pastilha | null>(null);
  const [aberto, setAberto] = useState(false);
  const [form, setForm] = useState(VAZIO);
  const [erroForm, setErroForm] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [historico, setHistorico] = useState<{ p: Pastilha; itens: HistoricoMinimo[] } | null>(null);
  const [importacao, setImportacao] = useState<ImportacaoResultado | null>(null);

  function abrir(p: Pastilha | null) {
    setEditando(p);
    setForm(p ? {
      codigo: p.codigo, descricao: p.descricao, especificacao: p.especificacao ?? '', unidadeMedida: p.unidadeMedida,
      fabricanteId: p.fabricanteId?.toString() ?? '', fornecedorPrincipalId: p.fornecedorPrincipalId?.toString() ?? '', estoqueMinimo: String(p.estoqueMinimo),
    } : VAZIO);
    setErroForm(null);
    setAberto(true);
  }

  async function salvar(e: FormEvent) {
    e.preventDefault();
    const corpo = {
      ...form,
      fabricanteId: form.fabricanteId ? Number(form.fabricanteId) : null,
      fornecedorPrincipalId: form.fornecedorPrincipalId ? Number(form.fornecedorPrincipalId) : null,
      estoqueMinimo: Number(form.estoqueMinimo),
    };
    try {
      if (editando) await api.put(`/api/pastilhas/${editando.id}`, corpo);
      else await api.post('/api/pastilhas', corpo);
      setAberto(false);
      recarregar();
    } catch (err) {
      setErroForm(mensagemErro(err));
    }
  }

  async function acao(fn: () => Promise<unknown>) {
    setAviso(null);
    try {
      await fn();
      recarregar();
    } catch (err) {
      setAviso(mensagemErro(err));
    }
  }

  async function importar(arquivo: File | undefined) {
    if (!arquivo) return;
    const fd = new FormData();
    fd.append('arquivo', arquivo);
    setAviso(null);
    try {
      setImportacao(await api.post<ImportacaoResultado>('/api/pastilhas/importacao', fd));
      recarregar();
    } catch (err) {
      setAviso(mensagemErro(err));
    }
  }

  const set = (k: keyof typeof VAZIO) => (e: { target: { value: string } }) => setForm({ ...form, [k]: e.target.value });

  return (
    <>
      <Titulo
        titulo="Pastilhas"
        descricao="Cadastro de pastilhas e estoque mínimo"
        acoes={
          <>
            <label className="btn-secundario cursor-pointer">
              <Upload className="h-4 w-4" aria-hidden /> Importar planilha (CSV)
              <input type="file" accept=".csv,text/csv" className="sr-only" onChange={(e) => { importar(e.target.files?.[0]); e.target.value = ''; }} />
            </label>
            <button type="button" className="btn-primario" onClick={() => abrir(null)}><Plus className="h-4 w-4" aria-hidden /> Nova pastilha</button>
          </>
        }
      />
      {importacao && (
        <div className="mb-3">
          <Mensagem tipo={importacao.erros.length ? 'aviso' : 'sucesso'}>
            Importacao concluida: {importacao.pastilhasCriadas} pastilha(s) criada(s), {importacao.saldosIniciais} saldo(s) inicial(is) registrado(s).
            {importacao.erros.length > 0 && <ul className="mt-1 list-disc pl-4">{importacao.erros.slice(0, 10).map((e) => <li key={e}>{e}</li>)}</ul>}
          </Mensagem>
        </div>
      )}
      {(erro || aviso) && <div className="mb-3"><Mensagem tipo="erro">{erro ?? aviso}</Mensagem></div>}
      <div className="mb-4 max-w-md">
        <label htmlFor="busca" className="sr-only">Buscar</label>
        <input id="busca" className="campo" placeholder="Buscar por código ou descrição" value={termo} onChange={(e) => { setTermo(e.target.value); setPagina(0); }} />
      </div>
      <div className="cartao overflow-hidden">
        {carregando && !dados ? <Carregando /> : dados?.conteudo.length === 0 ? <Vazio texto="Nenhuma pastilha cadastrada." /> : (
          <div className="overflow-x-auto">
            <table className="tabela">
              <thead><tr><th>Código</th><th>Descrição</th><th className="hidden md:table-cell">Fabricante</th><th className="text-right">Saldo</th><th className="text-right">Mínimo</th><th>Situação</th><th>Status</th><th className="text-right">Ações</th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {dados?.conteudo.map((p) => (
                  <tr key={p.id}>
                    <td className="font-medium">{p.codigo}</td><td>{p.descricao}</td><td className="hidden md:table-cell">{p.fabricanteNome ?? '-'}</td>
                    <td className="text-right">{p.saldoAtual}</td><td className="text-right">{p.estoqueMinimo}</td>
                    <td><SituacaoBadge situacao={p.situacao} /></td><td><StatusBadge ativo={p.ativo} /></td>
                    <td className="space-x-3 whitespace-nowrap text-right">
                      <button type="button" className="btn-link" onClick={() => abrir(p)}>Editar</button>
                      <button type="button" className="btn-link" onClick={() => api.get<HistoricoMinimo[]>(`/api/pastilhas/${p.id}/estoque-minimo/historico`).then((itens) => setHistorico({ p, itens }))}>Hist. mínimo</button>
                      <button type="button" className="btn-link" onClick={() => acao(() => api.patch(`/api/pastilhas/${p.id}/status`, { ativo: !p.ativo }))}>{p.ativo ? 'Inativar' : 'Ativar'}</button>
                      <button type="button" className="btn-link text-red-600" onClick={() => confirm(`Excluir ${p.codigo}?`) && acao(() => api.delete(`/api/pastilhas/${p.id}`))}>Excluir</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {dados && <Paginacao pagina={dados.pagina} totalPaginas={dados.totalPaginas} totalElementos={dados.totalElementos} aoMudar={setPagina} />}
      </div>

      <Modal aberto={aberto} titulo={editando ? `Editar ${editando.codigo}` : 'Nova pastilha'} aoFechar={() => setAberto(false)} largura="max-w-2xl">
        <form onSubmit={salvar} className="grid gap-4 sm:grid-cols-2">
          {erroForm && <div className="sm:col-span-2"><Mensagem tipo="erro">{erroForm}</Mensagem></div>}
          <Campo rotulo="Código (único)" id="pcod"><input id="pcod" className="campo uppercase" maxLength={40} value={form.codigo} onChange={set('codigo')} required /></Campo>
          <Campo rotulo="Unidade de medida" id="pun"><input id="pun" className="campo uppercase" maxLength={10} value={form.unidadeMedida} onChange={set('unidadeMedida')} required /></Campo>
          <div className="sm:col-span-2"><Campo rotulo="Descrição" id="pdesc"><input id="pdesc" className="campo" maxLength={160} value={form.descricao} onChange={set('descricao')} required /></Campo></div>
          <div className="sm:col-span-2"><Campo rotulo="Especificação técnica (geometria, classe, cobertura)" id="pesp"><textarea id="pesp" className="campo" rows={2} maxLength={500} value={form.especificacao} onChange={set('especificacao')} /></Campo></div>
          <Campo rotulo="Fabricante" id="pfab">
            <select id="pfab" className="campo" value={form.fabricanteId} onChange={set('fabricanteId')}>
              <option value="">Não informado</option>
              {fabricantes.dados?.map((f) => <option key={f.id} value={f.id}>{f.nome}</option>)}
            </select>
          </Campo>
          <Campo rotulo="Fornecedor principal" id="pforn">
            <select id="pforn" className="campo" value={form.fornecedorPrincipalId} onChange={set('fornecedorPrincipalId')}>
              <option value="">Não informado</option>
              {fornecedores.dados?.map((f) => <option key={f.id} value={f.id}>{f.razaoSocial}</option>)}
            </select>
          </Campo>
          <Campo rotulo="Estoque mínimo" id="pmin"><input id="pmin" type="number" min={0} step={1} className="campo" value={form.estoqueMinimo} onChange={set('estoqueMinimo')} required /></Campo>
          <div className="flex items-end justify-end gap-2"><button type="button" className="btn-secundario" onClick={() => setAberto(false)}>Cancelar</button><button type="submit" className="btn-primario">Salvar</button></div>
        </form>
      </Modal>

      <Modal aberto={!!historico} titulo={`Histórico do estoque mínimo - ${historico?.p.codigo ?? ''}`} aoFechar={() => setHistorico(null)}>
        {historico?.itens.length === 0 ? <Vazio texto="O estoque mínimo ainda não foi alterado." /> : (
          <table className="tabela">
            <thead><tr><th>Data</th><th>De</th><th>Para</th><th>Usuário</th></tr></thead>
            <tbody>{historico?.itens.map((h) => <tr key={h.id}><td>{dataHora(h.alteradoEm)}</td><td>{h.valorAnterior}</td><td>{h.valorNovo}</td><td>{h.usuarioNome}</td></tr>)}</tbody>
          </table>
        )}
      </Modal>
    </>
  );
}
