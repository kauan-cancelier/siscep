import { api } from '../api/cliente';
import type { Alerta } from '../api/tipos';
import { Carregando, Mensagem, SituacaoBadge, Titulo, Vazio } from '../components/Ui';
import { dataHora } from '../utils/formato';
import { useCarregar } from '../utils/useCarregar';

export default function Alertas() {
  const { dados, erro, carregando } = useCarregar(() => api.get<Alerta[]>('/api/alertas'), []);
  return (
    <>
      <Titulo titulo="Alertas de estoque" descricao="Pastilhas com saldo igual ou inferior ao estoque mínimo" />
      {erro && <Mensagem tipo="erro">{erro}</Mensagem>}
      {carregando && <Carregando />}
      {dados && dados.length === 0 && <Vazio texto="Nenhum alerta aberto. Todo o estoque está acima do mínimo." />}
      <ul className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {dados?.map((a) => (
          <li key={a.id} className={`cartao border-l-4 p-4 ${a.situacao === 'ZERADO' ? 'border-l-red-500' : 'border-l-amber-500'}`}>
            <div className="flex items-start justify-between gap-2">
              <p className="font-semibold">{a.pastilhaCodigo}</p>
              <SituacaoBadge situacao={a.situacao} />
            </div>
            <p className="text-sm text-slate-600">{a.pastilhaDescricao}</p>
            <p className="mt-2 text-sm">Saldo <strong>{a.saldo}</strong> / minimo {a.estoqueMinimo}</p>
            <p className="text-xs text-slate-500">Desde {dataHora(a.geradoEm)}</p>
          </li>
        ))}
      </ul>
    </>
  );
}
