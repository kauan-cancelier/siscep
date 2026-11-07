import { useState, type FormEvent } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { paginaInicial } from '../auth/permissoes';
import { Mensagem } from '../components/Ui';
import { mensagemErro } from '../utils/useCarregar';

export default function Login() {
  const { entrar, usuario } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  if (usuario) return <Navigate to={paginaInicial(usuario.perfil)} replace />;

  async function enviar(e: FormEvent) {
    e.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      const u = await entrar(email, senha);
      navigate(paginaInicial(u.perfil), { replace: true });
    } catch (err) {
      setErro(mensagemErro(err));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-marca-700 to-marca-800 p-4">
      <form onSubmit={enviar} className="w-full max-w-sm space-y-4 rounded-xl bg-white p-6 shadow-xl" noValidate>
        <div className="text-center">
          <p className="text-2xl font-bold text-marca-700">SISCEP</p>
          <p className="text-sm text-slate-500">Controle de Estoque de Pastilhas - DDA Usinagem</p>
        </div>
        {erro && <Mensagem tipo="erro">{erro}</Mensagem>}
        <div>
          <label htmlFor="email" className="rotulo">E-mail</label>
          <input id="email" type="email" autoComplete="username" className="campo" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div>
          <label htmlFor="senha" className="rotulo">Senha</label>
          <input id="senha" type="password" autoComplete="current-password" className="campo" value={senha} onChange={(e) => setSenha(e.target.value)} required />
        </div>
        <button type="submit" className="btn-primario w-full" disabled={enviando || !email || !senha}>
          {enviando ? 'Entrando...' : 'Entrar'}
        </button>
      </form>
    </div>
  );
}
