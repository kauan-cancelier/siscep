import { useEffect, useRef, type ReactNode } from 'react';
import clsx from 'clsx';
import { AlertTriangle, CheckCircle2, Loader2, X } from 'lucide-react';
import type { Situacao } from '../api/tipos';
import { NOMES_SITUACAO } from '../utils/formato';

export function Titulo({ titulo, descricao, acoes }: { titulo: string; descricao?: string; acoes?: ReactNode }) {
  return (
    <div className="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="text-xl font-semibold text-slate-900 sm:text-2xl">{titulo}</h1>
        {descricao && <p className="mt-1 text-sm text-slate-500">{descricao}</p>}
      </div>
      {acoes && <div className="flex flex-wrap gap-2">{acoes}</div>}
    </div>
  );
}

export function SituacaoBadge({ situacao }: { situacao: Situacao }) {
  const cores: Record<Situacao, string> = {
    NORMAL: 'bg-emerald-100 text-emerald-800',
    CRITICO: 'bg-amber-100 text-amber-800',
    ZERADO: 'bg-red-100 text-red-800',
  };
  return (
    <span className={clsx('inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-semibold', cores[situacao])}>
      {situacao !== 'NORMAL' && <AlertTriangle aria-hidden className="h-3 w-3" />}
      {NOMES_SITUACAO[situacao]}
    </span>
  );
}

export function StatusBadge({ ativo }: { ativo: boolean }) {
  return (
    <span
      className={clsx(
        'rounded-full px-2 py-0.5 text-xs font-semibold',
        ativo ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-600',
      )}
    >
      {ativo ? 'Ativo' : 'Inativo'}
    </span>
  );
}

export function Mensagem({ tipo, children }: { tipo: 'erro' | 'sucesso' | 'aviso'; children: ReactNode }) {
  const estilos = {
    erro: 'border-red-200 bg-red-50 text-red-800',
    sucesso: 'border-emerald-200 bg-emerald-50 text-emerald-800',
    aviso: 'border-amber-200 bg-amber-50 text-amber-800',
  };
  const Icone = tipo === 'sucesso' ? CheckCircle2 : AlertTriangle;
  return (
    <div role={tipo === 'erro' ? 'alert' : 'status'} className={clsx('flex gap-2 rounded-md border p-3 text-sm', estilos[tipo])}>
      <Icone aria-hidden className="mt-0.5 h-4 w-4 shrink-0" />
      <div>{children}</div>
    </div>
  );
}

export function Carregando({ texto = 'Carregando...' }: { texto?: string }) {
  return (
    <div className="flex items-center justify-center gap-2 py-10 text-sm text-slate-500" role="status">
      <Loader2 aria-hidden className="h-4 w-4 animate-spin" /> {texto}
    </div>
  );
}

export function Vazio({ texto, dica }: { texto: string; dica?: string }) {
  return (
    <div className="flex flex-col items-center gap-1 py-10 text-center">
      <InboxIcon />
      <p className="text-sm text-slate-500">{texto}</p>
      {dica && <p className="text-xs text-slate-400">{dica}</p>}
    </div>
  );
}

function InboxIcon() {
  return (
    <svg aria-hidden className="h-8 w-8 text-slate-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M22 12h-6l-2 3h-4l-2-3H2" />
      <path d="M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z" />
    </svg>
  );
}

export function Confirmar({
  titulo,
  texto,
  confirmarRotulo = 'Excluir',
  aoConfirmar,
  aoCancelar,
}: {
  titulo: string;
  texto: ReactNode;
  confirmarRotulo?: string;
  aoConfirmar: () => void;
  aoCancelar: () => void;
}) {
  return (
    <Modal aberto titulo={titulo} aoFechar={aoCancelar} largura="max-w-sm">
      <p className="text-sm text-slate-600">{texto}</p>
      <div className="mt-5 flex justify-end gap-2">
        <button type="button" className="btn-secundario" onClick={aoCancelar}>Cancelar</button>
        <button type="button" className="btn-perigo" onClick={aoConfirmar}>{confirmarRotulo}</button>
      </div>
    </Modal>
  );
}

export function Campo({ rotulo, id, erro, children }: { rotulo: string; id: string; erro?: string; children: ReactNode }) {
  return (
    <div>
      <label htmlFor={id} className="rotulo">
        {rotulo}
      </label>
      {children}
      {erro && <p className="mt-1 text-xs text-red-600">{erro}</p>}
    </div>
  );
}

export function Modal({
  aberto,
  titulo,
  aoFechar,
  children,
  largura = 'max-w-lg',
}: {
  aberto: boolean;
  titulo: string;
  aoFechar: () => void;
  children: ReactNode;
  largura?: string;
}) {
  const ref = useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (!aberto) return;
    const esc = (e: KeyboardEvent) => e.key === 'Escape' && aoFechar();
    document.addEventListener('keydown', esc);
    ref.current?.querySelector<HTMLElement>('input,select,textarea,button')?.focus();
    return () => document.removeEventListener('keydown', esc);
  }, [aberto, aoFechar]);
  if (!aberto) return null;
  return (
    <div className="fixed inset-0 z-40 flex items-end justify-center bg-slate-900/40 p-0 sm:items-center sm:p-4">
      <div
        ref={ref}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-titulo"
        className={clsx('max-h-[92vh] w-full overflow-y-auto rounded-t-xl bg-white shadow-xl sm:rounded-xl', largura)}
      >
        <div className="flex items-center justify-between border-b border-slate-200 px-5 py-3">
          <h2 id="modal-titulo" className="text-base font-semibold">
            {titulo}
          </h2>
          <button type="button" onClick={aoFechar} className="rounded p-1 text-slate-500 hover:bg-slate-100" aria-label="Fechar">
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="p-5">{children}</div>
      </div>
    </div>
  );
}

export function Paginacao({
  pagina,
  totalPaginas,
  totalElementos,
  aoMudar,
}: {
  pagina: number;
  totalPaginas: number;
  totalElementos: number;
  aoMudar: (p: number) => void;
}) {
  return (
    <div className="flex flex-col items-center justify-between gap-2 border-t border-slate-200 px-3 py-2 text-sm text-slate-600 sm:flex-row">
      <span>
        {totalElementos} registro(s) — página {totalPaginas === 0 ? 0 : pagina + 1} de {totalPaginas}
      </span>
      <div className="flex gap-2">
        <button type="button" className="btn-secundario" disabled={pagina <= 0} onClick={() => aoMudar(pagina - 1)}>
          Anterior
        </button>
        <button
          type="button"
          className="btn-secundario"
          disabled={pagina + 1 >= totalPaginas}
          onClick={() => aoMudar(pagina + 1)}
        >
          Próxima
        </button>
      </div>
    </div>
  );
}

export function Indicador({ rotulo, valor, destaque }: { rotulo: string; valor: ReactNode; destaque?: 'alerta' | 'perigo' }) {
  return (
    <div
      className={clsx(
        'cartao p-4',
        destaque === 'alerta' && 'border-amber-300 bg-amber-50',
        destaque === 'perigo' && 'border-red-300 bg-red-50',
      )}
    >
      <p className="text-xs font-medium uppercase tracking-wide text-slate-500">{rotulo}</p>
      <p className="mt-1 text-2xl font-semibold text-slate-900">{valor}</p>
    </div>
  );
}
