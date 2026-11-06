import { useEffect, useState } from 'react';
import { api } from '../api/cliente';
import type { Pagina, Pastilha } from '../api/tipos';
import { SituacaoBadge } from './Ui';

/** Busca de pastilhas ativas por codigo ou descricao (itens inativos nao aparecem - RN11). */
export default function SeletorPastilha({
  valor,
  aoSelecionar,
  id = 'pastilha',
}: {
  valor: Pastilha | null;
  aoSelecionar: (p: Pastilha | null) => void;
  id?: string;
}) {
  const [termo, setTermo] = useState('');
  const [opcoes, setOpcoes] = useState<Pastilha[]>([]);
  const [aberto, setAberto] = useState(false);

  useEffect(() => {
    if (!aberto) return;
    const t = setTimeout(() => {
      api
        .get<Pagina<Pastilha>>('/api/pastilhas', { termo, ativo: true, size: 10, sort: 'codigo' })
        .then((p) => setOpcoes(p.conteudo))
        .catch(() => setOpcoes([]));
    }, 250);
    return () => clearTimeout(t);
  }, [termo, aberto]);

  if (valor) {
    return (
      <div className="flex items-center justify-between rounded-md border border-marca-100 bg-marca-50 px-3 py-2 text-sm">
        <div>
          <p className="font-semibold">
            {valor.codigo} <SituacaoBadge situacao={valor.situacao} />
          </p>
          <p className="text-slate-600">
            {valor.descricao} - saldo atual: <strong>{valor.saldoAtual}</strong> {valor.unidadeMedida} (minimo {valor.estoqueMinimo})
          </p>
        </div>
        <button type="button" className="btn-link" onClick={() => aoSelecionar(null)}>
          Trocar
        </button>
      </div>
    );
  }

  return (
    <div className="relative">
      <input
        id={id}
        className="campo"
        placeholder="Digite o codigo ou a descricao"
        value={termo}
        autoComplete="off"
        role="combobox"
        aria-expanded={aberto}
        aria-controls={`${id}-lista`}
        onFocus={() => setAberto(true)}
        onBlur={() => setTimeout(() => setAberto(false), 150)}
        onChange={(e) => setTermo(e.target.value)}
      />
      {aberto && (
        <ul id={`${id}-lista`} role="listbox" className="absolute z-20 mt-1 max-h-64 w-full overflow-auto rounded-md border border-slate-200 bg-white shadow-lg">
          {opcoes.length === 0 && <li className="px-3 py-2 text-sm text-slate-500">Nenhuma pastilha ativa encontrada.</li>}
          {opcoes.map((p) => (
            <li key={p.id} role="option" aria-selected={false}>
              <button
                type="button"
                className="flex w-full items-center justify-between gap-2 px-3 py-2 text-left text-sm hover:bg-marca-50"
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => {
                  aoSelecionar(p);
                  setTermo('');
                }}
              >
                <span>
                  <strong>{p.codigo}</strong> - {p.descricao}
                </span>
                <span className="shrink-0 text-xs text-slate-500">saldo {p.saldoAtual}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
