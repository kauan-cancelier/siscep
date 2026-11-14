import { useState, type FormEvent } from 'react';
import { api } from '../api/cliente';
import { Campo, Mensagem, Titulo } from '../components/Ui';
import { mensagemErro } from '../utils/useCarregar';

export default function MinhaSenha() {
  const [senhaAtual, setSenhaAtual] = useState('');
  const [novaSenha, setNovaSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [msg, setMsg] = useState<{ tipo: 'erro' | 'sucesso'; texto: string } | null>(null);

  async function enviar(e: FormEvent) {
    e.preventDefault();
    if (novaSenha !== confirmacao) {
      setMsg({ tipo: 'erro', texto: 'A confirmacao nao confere com a nova senha.' });
      return;
    }
    try {
      await api.post('/api/auth/trocar-senha', { senhaAtual, novaSenha });
      setMsg({ tipo: 'sucesso', texto: 'Senha alterada com sucesso.' });
      setSenhaAtual('');
      setNovaSenha('');
      setConfirmacao('');
    } catch (err) {
      setMsg({ tipo: 'erro', texto: mensagemErro(err) });
    }
  }

  return (
    <>
      <Titulo titulo="Alterar minha senha" />
      <form onSubmit={enviar} className="cartao max-w-md space-y-4 p-5">
        {msg && <Mensagem tipo={msg.tipo}>{msg.texto}</Mensagem>}
        <Campo rotulo="Senha atual" id="atual">
          <input id="atual" type="password" className="campo" value={senhaAtual} onChange={(e) => setSenhaAtual(e.target.value)} required autoComplete="current-password" />
        </Campo>
        <Campo rotulo="Nova senha (minimo 8 caracteres)" id="nova">
          <input id="nova" type="password" className="campo" minLength={8} value={novaSenha} onChange={(e) => setNovaSenha(e.target.value)} required autoComplete="new-password" />
        </Campo>
        <Campo rotulo="Confirme a nova senha" id="conf">
          <input id="conf" type="password" className="campo" value={confirmacao} onChange={(e) => setConfirmacao(e.target.value)} required autoComplete="new-password" />
        </Campo>
        <button type="submit" className="btn-primario">Salvar nova senha</button>
      </form>
    </>
  );
}
