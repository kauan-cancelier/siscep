import { useState, type FormEvent } from 'react';
import { Plus } from 'lucide-react';
import { api } from '../api/cliente';
import type { Pagina, Perfil, RegistroAcesso, Usuario } from '../api/tipos';
import { useAuth } from '../auth/AuthContext';
import { Campo, Carregando, Mensagem, Modal, StatusBadge, Titulo, Vazio } from '../components/Ui';
import { dataHora, NOMES_PERFIL } from '../utils/formato';
import { mensagemErro, useCarregar } from '../utils/useCarregar';

const VAZIO = { nome: '', email: '', senha: '', perfil: 'OPERADOR' as Perfil };

export default function Usuarios() {
  const { usuario: logado } = useAuth();
  const { dados, erro, carregando, recarregar } = useCarregar(() => api.get<Usuario[]>('/api/usuarios'), []);
  const acessos = useCarregar(() => api.get<Pagina<RegistroAcesso>>('/api/usuarios/acessos', { size: 15 }), []);
  const [editando, setEditando] = useState<Usuario | null>(null);
  const [aberto, setAberto] = useState(false);
  const [form, setForm] = useState(VAZIO);
  const [erroForm, setErroForm] = useState<string | null>(null);
  const [msg, setMsg] = useState<{ tipo: 'erro' | 'sucesso'; texto: string } | null>(null);
  const [senhaDe, setSenhaDe] = useState<Usuario | null>(null);
  const [novaSenha, setNovaSenha] = useState('');

  function abrir(u: Usuario | null) {
    setEditando(u);
    setForm(u ? { nome: u.nome, email: u.email, senha: '', perfil: u.perfil } : VAZIO);
    setErroForm(null);
    setAberto(true);
  }

  async function salvar(e: FormEvent) {
    e.preventDefault();
    try {
      if (editando) await api.put(`/api/usuarios/${editando.id}`, { ...form, senha: null });
      else await api.post('/api/usuarios', form);
      setAberto(false);
      recarregar();
    } catch (err) {
      setErroForm(mensagemErro(err));
    }
  }

  async function acao(fn: () => Promise<unknown>, sucesso?: string) {
    setMsg(null);
    try {
      await fn();
      if (sucesso) setMsg({ tipo: 'sucesso', texto: sucesso });
      recarregar();
    } catch (err) {
      setMsg({ tipo: 'erro', texto: mensagemErro(err) });
    }
  }

  return (
    <>
      <Titulo titulo="Usuarios" descricao="Cadastro, perfis de acesso e auditoria de login" acoes={<button type="button" className="btn-primario" onClick={() => abrir(null)}><Plus className="h-4 w-4" aria-hidden /> Novo usuario</button>} />
      {(erro || msg) && <div className="mb-3"><Mensagem tipo={msg?.tipo ?? 'erro'}>{erro ?? msg?.texto}</Mensagem></div>}
      <div className="cartao overflow-x-auto">
        {carregando ? <Carregando /> : (
          <table className="tabela">
            <thead><tr><th>Nome</th><th className="hidden md:table-cell">E-mail</th><th>Perfil</th><th>Status</th><th className="hidden lg:table-cell">Ultimo acesso</th><th className="text-right">Acoes</th></tr></thead>
            <tbody className="divide-y divide-slate-100">
              {dados?.map((u) => (
                <tr key={u.id}>
                  <td className="font-medium">{u.nome}</td><td className="hidden md:table-cell">{u.email}</td><td>{NOMES_PERFIL[u.perfil]}</td>
                  <td className="space-x-1"><StatusBadge ativo={u.ativo} />{u.bloqueado && <span className="rounded-full bg-red-100 px-2 py-0.5 text-xs font-semibold text-red-800">Bloqueado</span>}</td>
                  <td className="hidden lg:table-cell">{dataHora(u.ultimoAcesso)}</td>
                  <td className="space-x-3 whitespace-nowrap text-right">
                    <button type="button" className="btn-link" onClick={() => abrir(u)}>Editar</button>
                    <button type="button" className="btn-link" onClick={() => { setSenhaDe(u); setNovaSenha(''); }}>Redefinir senha</button>
                    {u.bloqueado && <button type="button" className="btn-link" onClick={() => acao(() => api.post(`/api/usuarios/${u.id}/desbloquear`), 'Usuario desbloqueado.')}>Desbloquear</button>}
                    {u.id !== logado?.id && <button type="button" className="btn-link" onClick={() => acao(() => api.patch(`/api/usuarios/${u.id}/status`, { ativo: !u.ativo }))}>{u.ativo ? 'Inativar' : 'Ativar'}</button>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <section className="cartao mt-6 overflow-x-auto" aria-labelledby="acessos">
        <h2 id="acessos" className="border-b border-slate-200 px-4 py-3 font-semibold">Ultimos acessos</h2>
        {acessos.dados?.conteudo.length === 0 ? <Vazio texto="Sem registros." /> : (
          <table className="tabela">
            <thead><tr><th>Data/hora</th><th>E-mail</th><th>Resultado</th><th className="hidden md:table-cell">IP</th></tr></thead>
            <tbody className="divide-y divide-slate-100">
              {acessos.dados?.conteudo.map((a) => (
                <tr key={a.id}>
                  <td className="whitespace-nowrap">{dataHora(a.dataHora)}</td><td>{a.email}</td>
                  <td>{a.sucesso ? <span className="text-emerald-700">Sucesso</span> : <span className="text-red-700">Falha ({a.motivo})</span>}</td>
                  <td className="hidden md:table-cell">{a.ip ?? '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <Modal aberto={aberto} titulo={editando ? 'Editar usuario' : 'Novo usuario'} aoFechar={() => setAberto(false)}>
        <form onSubmit={salvar} className="space-y-4">
          {erroForm && <Mensagem tipo="erro">{erroForm}</Mensagem>}
          <Campo rotulo="Nome" id="unome"><input id="unome" className="campo" maxLength={120} value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} required /></Campo>
          <Campo rotulo="E-mail" id="uemail"><input id="uemail" type="email" className="campo" maxLength={160} value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required /></Campo>
          {!editando && <Campo rotulo="Senha inicial (minimo 8 caracteres)" id="usenha"><input id="usenha" type="password" className="campo" minLength={8} value={form.senha} onChange={(e) => setForm({ ...form, senha: e.target.value })} required autoComplete="new-password" /></Campo>}
          <Campo rotulo="Perfil" id="uperfil">
            <select id="uperfil" className="campo" value={form.perfil} disabled={editando?.id === logado?.id} onChange={(e) => setForm({ ...form, perfil: e.target.value as Perfil })}>
              {Object.entries(NOMES_PERFIL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
          </Campo>
          <div className="flex justify-end gap-2"><button type="button" className="btn-secundario" onClick={() => setAberto(false)}>Cancelar</button><button type="submit" className="btn-primario">Salvar</button></div>
        </form>
      </Modal>

      <Modal aberto={!!senhaDe} titulo={`Redefinir senha de ${senhaDe?.nome ?? ''}`} aoFechar={() => setSenhaDe(null)}>
        <form className="space-y-4" onSubmit={(e) => { e.preventDefault(); const u = senhaDe; setSenhaDe(null); if (u) acao(() => api.post(`/api/usuarios/${u.id}/redefinir-senha`, { novaSenha }), 'Senha redefinida.'); }}>
          <Campo rotulo="Nova senha (minimo 8 caracteres)" id="rsenha"><input id="rsenha" type="password" className="campo" minLength={8} value={novaSenha} onChange={(e) => setNovaSenha(e.target.value)} required autoComplete="new-password" /></Campo>
          <div className="flex justify-end gap-2"><button type="button" className="btn-secundario" onClick={() => setSenhaDe(null)}>Cancelar</button><button type="submit" className="btn-primario">Redefinir</button></div>
        </form>
      </Modal>
    </>
  );
}
