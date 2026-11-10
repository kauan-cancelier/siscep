import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/cliente';
import type { Fornecedor, Movimentacao, Pastilha, TipoMovimentacao } from '../api/tipos';
import SeletorPastilha from '../components/SeletorPastilha';
import { Campo, Mensagem, Titulo } from '../components/Ui';
import { NOMES_TIPO } from '../utils/formato';
import { mensagemErro } from '../utils/useCarregar';

const TEXTOS: Record<TipoMovimentacao, { titulo: string; descricao: string; url: string }> = {
  ENTRADA: { titulo: 'Registrar entrada', descricao: 'Recebimento de pastilhas de um fornecedor', url: '/api/movimentacoes/entradas' },
  SAIDA: { titulo: 'Registrar saida', descricao: 'Retirada de pastilhas para producao', url: '/api/movimentacoes/saidas' },
  AJUSTE: { titulo: 'Ajuste de inventario', descricao: 'Corrige o saldo do sistema a partir da contagem fisica', url: '/api/movimentacoes/ajustes' },
};

export default function Movimentar({ tipo }: { tipo: TipoMovimentacao }) {
  const [pastilha, setPastilha] = useState<Pastilha | null>(null);
  const [quantidade, setQuantidade] = useState('');
  const [fornecedorId, setFornecedorId] = useState('');
  const [documento, setDocumento] = useState('');
  const [destino, setDestino] = useState('');
  const [justificativa, setJustificativa] = useState('');
  const [observacao, setObservacao] = useState('');
  const [fornecedores, setFornecedores] = useState<Fornecedor[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [ultima, setUltima] = useState<Movimentacao | null>(null);
  const [enviando, setEnviando] = useState(false);
  const t = TEXTOS[tipo];

  useEffect(() => {
    if (tipo === 'ENTRADA') api.get<Fornecedor[]>('/api/fornecedores', { apenasAtivos: true }).then(setFornecedores).catch(() => setFornecedores([]));
  }, [tipo]);

  useEffect(() => {
    if (tipo === 'ENTRADA' && pastilha?.fornecedorPrincipalId) setFornecedorId(String(pastilha.fornecedorPrincipalId));
  }, [pastilha, tipo]);

  const qtd = Number(quantidade);
  const saldoInsuficiente = tipo === 'SAIDA' && pastilha !== null && qtd > pastilha.saldoAtual;

  async function enviar(e: FormEvent) {
    e.preventDefault();
    if (!pastilha) return;
    setErro(null);
    setEnviando(true);
    const corpo =
      tipo === 'ENTRADA'
        ? { pastilhaId: pastilha.id, quantidade: qtd, fornecedorId: Number(fornecedorId), documentoReferencia: documento, observacao }
        : tipo === 'SAIDA'
          ? { pastilhaId: pastilha.id, quantidade: qtd, destino, observacao }
          : { pastilhaId: pastilha.id, contagemFisica: qtd, justificativa };
    try {
      const m = await api.post<Movimentacao>(t.url, corpo);
      setUltima(m);
      setPastilha(null);
      setQuantidade('');
      setDocumento('');
      setDestino('');
      setJustificativa('');
      setObservacao('');
    } catch (err) {
      setErro(mensagemErro(err));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <>
      <Titulo titulo={t.titulo} descricao={t.descricao} />
      <form onSubmit={enviar} className="cartao max-w-2xl space-y-4 p-5" aria-label={t.titulo}>
        {ultima && (
          <Mensagem tipo="sucesso">
            {NOMES_TIPO[ultima.tipo]} registrada para <strong>{ultima.pastilhaCodigo}</strong>: saldo {ultima.saldoAnterior} → {ultima.saldoResultante}.{' '}
            <Link to="/movimentacoes" className="underline">Ver historico</Link>
          </Mensagem>
        )}
        {erro && <Mensagem tipo="erro">{erro}</Mensagem>}
        <Campo rotulo="Pastilha" id="pastilha">
          <SeletorPastilha valor={pastilha} aoSelecionar={setPastilha} />
        </Campo>
        <Campo
          rotulo={tipo === 'AJUSTE' ? 'Contagem fisica (quantidade real em estoque)' : 'Quantidade'}
          id="quantidade"
          erro={saldoInsuficiente ? `Saldo insuficiente: disponivel ${pastilha?.saldoAtual}.` : undefined}
        >
          <input id="quantidade" type="number" inputMode="numeric" min={tipo === 'AJUSTE' ? 0 : 1} step={1} className="campo" value={quantidade} onChange={(e) => setQuantidade(e.target.value)} required />
        </Campo>
        {tipo === 'AJUSTE' && pastilha && quantidade !== '' && (
          <p className="text-sm text-slate-600">Diferenca a registrar: <strong>{qtd - pastilha.saldoAtual > 0 ? '+' : ''}{qtd - pastilha.saldoAtual}</strong></p>
        )}
        {tipo === 'ENTRADA' && (
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo rotulo="Fornecedor de origem" id="fornecedor">
              <select id="fornecedor" className="campo" value={fornecedorId} onChange={(e) => setFornecedorId(e.target.value)} required>
                <option value="">Selecione</option>
                {fornecedores.map((f) => <option key={f.id} value={f.id}>{f.razaoSocial}</option>)}
              </select>
            </Campo>
            <Campo rotulo="Documento de referencia (nota fiscal)" id="documento">
              <input id="documento" className="campo" maxLength={60} value={documento} onChange={(e) => setDocumento(e.target.value)} />
            </Campo>
          </div>
        )}
        {tipo === 'SAIDA' && (
          <Campo rotulo="Destino (ordem de producao, maquina ou setor)" id="destino">
            <input id="destino" className="campo" maxLength={120} value={destino} onChange={(e) => setDestino(e.target.value)} required />
          </Campo>
        )}
        {tipo === 'AJUSTE' ? (
          <Campo rotulo="Justificativa (obrigatoria)" id="justificativa">
            <textarea id="justificativa" className="campo" rows={3} maxLength={500} value={justificativa} onChange={(e) => setJustificativa(e.target.value)} required />
          </Campo>
        ) : (
          <Campo rotulo="Observacao" id="observacao">
            <textarea id="observacao" className="campo" rows={2} maxLength={500} value={observacao} onChange={(e) => setObservacao(e.target.value)} />
          </Campo>
        )}
        <button type="submit" className="btn-primario" disabled={enviando || !pastilha || quantidade === '' || saldoInsuficiente}>
          {enviando ? 'Registrando...' : `Confirmar ${NOMES_TIPO[tipo].toLowerCase()}`}
        </button>
      </form>
    </>
  );
}
