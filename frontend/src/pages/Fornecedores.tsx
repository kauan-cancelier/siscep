import { useState, type FormEvent } from 'react';
import { Plus } from 'lucide-react';
import { api } from '../api/cliente';
import type { Fornecedor } from '../api/tipos';
import { Campo, Carregando, Confirmar, Mensagem, Modal, StatusBadge, Titulo, Vazio } from '../components/Ui';
import { formatarCnpj } from '../utils/formato';
import { mensagemErro, useCarregar } from '../utils/useCarregar';

const VAZIO = { razaoSocial: '', cnpj: '', contato: '', telefone: '', email: '', prazoMedioEntregaDias: '' };

export default function Fornecedores() {
  const { dados, erro, carregando, recarregar } = useCarregar(() => api.get<Fornecedor[]>('/api/fornecedores'), []);
  const [editando, setEditando] = useState<Fornecedor | null>(null);
  const [aberto, setAberto] = useState(false);
  const [form, setForm] = useState(VAZIO);
  const [erroForm, setErroForm] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  function abrir(f: Fornecedor | null) {
    setEditando(f);
    setForm(f ? { razaoSocial: f.razaoSocial, cnpj: f.cnpj ?? '', contato: f.contato ?? '', telefone: f.telefone ?? '', email: f.email ?? '', prazoMedioEntregaDias: f.prazoMedioEntregaDias?.toString() ?? '' } : VAZIO);
    setErroForm(null);
    setAberto(true);
  }

  async function salvar(e: FormEvent) {
    e.preventDefault();
    const corpo = { ...form, email: form.email || null, prazoMedioEntregaDias: form.prazoMedioEntregaDias === '' ? null : Number(form.prazoMedioEntregaDias) };
    try {
      if (editando) await api.put(`/api/fornecedores/${editando.id}`, corpo);
      else await api.post('/api/fornecedores', corpo);
      setAberto(false);
      recarregar();
    } catch (err) {
      setErroForm(mensagemErro(err));
    }
  }

  const [excluindo, setExcluindo] = useState<Fornecedor | null>(null);
  const [sucesso, setSucesso] = useState<string | null>(null);

  async function acao(fn: () => Promise<unknown>, ok?: string) {
    setAviso(null);
    setSucesso(null);
    try {
      await fn();
      if (ok) setSucesso(ok);
      recarregar();
    } catch (err) {
      setAviso(mensagemErro(err));
    }
  }

  const campo = (k: keyof typeof VAZIO, rotulo: string, extra: Record<string, unknown> = {}) => (
    <Campo rotulo={rotulo} id={`fo-${k}`}>
      <input id={`fo-${k}`} className="campo" value={form[k]} onChange={(e) => setForm({ ...form, [k]: e.target.value })} {...extra} />
    </Campo>
  );

  return (
    <>
      <Titulo titulo="Fornecedores" acoes={<button type="button" className="btn-primario" onClick={() => abrir(null)}><Plus className="h-4 w-4" aria-hidden /> Novo fornecedor</button>} />
      {(erro || aviso || sucesso) && <div className="mb-3"><Mensagem tipo={sucesso && !erro && !aviso ? 'sucesso' : 'erro'}>{erro ?? aviso ?? sucesso}</Mensagem></div>}
      <div className="cartao overflow-x-auto">
        {carregando ? <Carregando /> : dados?.length === 0 ? <Vazio texto="Nenhum fornecedor cadastrado." /> : (
          <table className="tabela">
            <thead><tr><th>Razão social</th><th className="hidden md:table-cell">CNPJ</th><th className="hidden lg:table-cell">Contato</th><th className="hidden sm:table-cell">Prazo</th><th>Status</th><th className="text-right">Ações</th></tr></thead>
            <tbody className="divide-y divide-slate-100">
              {dados?.map((f) => (
                <tr key={f.id}>
                  <td className="font-medium">{f.razaoSocial}</td><td className="hidden md:table-cell">{formatarCnpj(f.cnpj)}</td>
                  <td className="hidden text-xs lg:table-cell">{[f.contato, f.telefone, f.email].filter(Boolean).join(' | ') || '-'}</td>
                  <td className="hidden sm:table-cell">{f.prazoMedioEntregaDias != null ? `${f.prazoMedioEntregaDias} dias` : '-'}</td>
                  <td><StatusBadge ativo={f.ativo} /></td>
                  <td className="space-x-3 whitespace-nowrap text-right">
                    <button type="button" className="btn-link" onClick={() => abrir(f)}>Editar</button>
                    <button type="button" className="btn-link" onClick={() => acao(() => api.patch(`/api/fornecedores/${f.id}/status`, { ativo: !f.ativo }), f.ativo ? `${f.razaoSocial} inativado.` : `${f.razaoSocial} ativado.`)}>{f.ativo ? 'Inativar' : 'Ativar'}</button>
                    <button type="button" className="btn-link text-red-600" onClick={() => setExcluindo(f)}>Excluir</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
      <Modal aberto={aberto} titulo={editando ? 'Editar fornecedor' : 'Novo fornecedor'} aoFechar={() => setAberto(false)}>
        <form onSubmit={salvar} className="grid gap-4 sm:grid-cols-2">
          {erroForm && <div className="sm:col-span-2"><Mensagem tipo="erro">{erroForm}</Mensagem></div>}
          <div className="sm:col-span-2">{campo('razaoSocial', 'Razão social', { required: true, maxLength: 160 })}</div>
          {campo('cnpj', 'CNPJ', { placeholder: '00.000.000/0000-00', maxLength: 18 })}
          {campo('prazoMedioEntregaDias', 'Prazo médio de entrega (dias)', { type: 'number', min: 0 })}
          {campo('contato', 'Contato', { maxLength: 120 })}
          {campo('telefone', 'Telefone', { maxLength: 20 })}
          <div className="sm:col-span-2">{campo('email', 'E-mail', { type: 'email', maxLength: 160 })}</div>
          <div className="flex justify-end gap-2 sm:col-span-2"><button type="button" className="btn-secundario" onClick={() => setAberto(false)}>Cancelar</button><button type="submit" className="btn-primario">Salvar</button></div>
        </form>
      </Modal>
      {excluindo && (
        <Confirmar
          titulo="Excluir fornecedor"
          texto={<>Excluir definitivamente <strong>{excluindo.razaoSocial}</strong>? Se houver pastilhas ou entradas vinculadas, prefira Inativar.</>}
          aoCancelar={() => setExcluindo(null)}
          aoConfirmar={() => { const f = excluindo; setExcluindo(null); acao(() => api.delete(`/api/fornecedores/${f.id}`), `${f.razaoSocial} excluído.`); }}
        />
      )}
    </>
  );
}