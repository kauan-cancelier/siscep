import { useState, type FormEvent } from 'react';
import { Plus } from 'lucide-react';
import { api } from '../api/cliente';
import type { Fabricante } from '../api/tipos';
import { Campo, Carregando, Mensagem, Modal, StatusBadge, Titulo, Vazio } from '../components/Ui';
import { mensagemErro, useCarregar } from '../utils/useCarregar';

const VAZIO = { nome: '', pais: '', site: '' };

export default function Fabricantes() {
  const { dados, erro, carregando, recarregar } = useCarregar(() => api.get<Fabricante[]>('/api/fabricantes'), []);
  const [editando, setEditando] = useState<Fabricante | null>(null);
  const [aberto, setAberto] = useState(false);
  const [form, setForm] = useState(VAZIO);
  const [erroForm, setErroForm] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  function abrir(f: Fabricante | null) {
    setEditando(f);
    setForm(f ? { nome: f.nome, pais: f.pais ?? '', site: f.site ?? '' } : VAZIO);
    setErroForm(null);
    setAberto(true);
  }

  async function salvar(e: FormEvent) {
    e.preventDefault();
    try {
      if (editando) await api.put(`/api/fabricantes/${editando.id}`, form);
      else await api.post('/api/fabricantes', form);
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

  return (
    <>
      <Titulo titulo="Fabricantes" acoes={<button type="button" className="btn-primario" onClick={() => abrir(null)}><Plus className="h-4 w-4" aria-hidden /> Novo fabricante</button>} />
      {(erro || aviso) && <div className="mb-3"><Mensagem tipo="erro">{erro ?? aviso}</Mensagem></div>}
      <div className="cartao overflow-x-auto">
        {carregando ? <Carregando /> : dados?.length === 0 ? <Vazio texto="Nenhum fabricante cadastrado." /> : (
          <table className="tabela">
            <thead><tr><th>Nome</th><th className="hidden sm:table-cell">País</th><th className="hidden md:table-cell">Site</th><th>Status</th><th className="text-right">Ações</th></tr></thead>
            <tbody className="divide-y divide-slate-100">
              {dados?.map((f) => (
                <tr key={f.id}>
                  <td className="font-medium">{f.nome}</td><td className="hidden sm:table-cell">{f.pais ?? '-'}</td><td className="hidden md:table-cell">{f.site ?? '-'}</td>
                  <td><StatusBadge ativo={f.ativo} /></td>
                  <td className="space-x-3 whitespace-nowrap text-right">
                    <button type="button" className="btn-link" onClick={() => abrir(f)}>Editar</button>
                    <button type="button" className="btn-link" onClick={() => acao(() => api.patch(`/api/fabricantes/${f.id}/status`, { ativo: !f.ativo }))}>{f.ativo ? 'Inativar' : 'Ativar'}</button>
                    <button type="button" className="btn-link text-red-600" onClick={() => confirm(`Excluir ${f.nome}?`) && acao(() => api.delete(`/api/fabricantes/${f.id}`))}>Excluir</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
      <Modal aberto={aberto} titulo={editando ? 'Editar fabricante' : 'Novo fabricante'} aoFechar={() => setAberto(false)}>
        <form onSubmit={salvar} className="space-y-4">
          {erroForm && <Mensagem tipo="erro">{erroForm}</Mensagem>}
          <Campo rotulo="Nome" id="fnome"><input id="fnome" className="campo" maxLength={120} value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} required /></Campo>
          <Campo rotulo="País" id="fpais"><input id="fpais" className="campo" maxLength={60} value={form.pais} onChange={(e) => setForm({ ...form, pais: e.target.value })} /></Campo>
          <Campo rotulo="Site" id="fsite"><input id="fsite" className="campo" maxLength={160} value={form.site} onChange={(e) => setForm({ ...form, site: e.target.value })} /></Campo>
          <div className="flex justify-end gap-2"><button type="button" className="btn-secundario" onClick={() => setAberto(false)}>Cancelar</button><button type="submit" className="btn-primario">Salvar</button></div>
        </form>
      </Modal>
    </>
  );
}
