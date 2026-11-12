import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import clsx from 'clsx';
import {
  ArrowDownToLine,
  ArrowUpFromLine,
  Bell,
  Boxes,
  ClipboardList,
  Factory,
  FileBarChart,
  History,
  KeyRound,
  LayoutDashboard,
  LogOut,
  Menu,
  ScrollText,
  SlidersHorizontal,
  Truck,
  Users,
  X,
} from 'lucide-react';
import { useAuth } from '../auth/AuthContext';
import type { Permissao } from '../auth/permissoes';
import { NOMES_PERFIL } from '../utils/formato';

interface Item {
  para: string;
  rotulo: string;
  icone: typeof Boxes;
  permissao: Permissao;
}

const MENU: { grupo: string; itens: Item[] }[] = [
  {
    grupo: 'Visão geral',
    itens: [
      { para: '/dashboard', rotulo: 'Dashboard', icone: LayoutDashboard, permissao: 'dashboard' },
      { para: '/estoque', rotulo: 'Estoque atual', icone: Boxes, permissao: 'estoque' },
      { para: '/alertas', rotulo: 'Alertas', icone: Bell, permissao: 'estoque' },
    ],
  },
  {
    grupo: 'Movimentações',
    itens: [
      { para: '/movimentacoes/entrada', rotulo: 'Registrar entrada', icone: ArrowDownToLine, permissao: 'entrada' },
      { para: '/movimentacoes/saida', rotulo: 'Registrar saída', icone: ArrowUpFromLine, permissao: 'saida' },
      { para: '/movimentacoes/ajuste', rotulo: 'Ajuste de inventário', icone: SlidersHorizontal, permissao: 'ajuste' },
      { para: '/movimentacoes', rotulo: 'Histórico', icone: History, permissao: 'historico' },
    ],
  },
  {
    grupo: 'Cadastros',
    itens: [
      { para: '/pastilhas', rotulo: 'Pastilhas', icone: ClipboardList, permissao: 'cadastros' },
      { para: '/fabricantes', rotulo: 'Fabricantes', icone: Factory, permissao: 'cadastros' },
      { para: '/fornecedores', rotulo: 'Fornecedores', icone: Truck, permissao: 'cadastros' },
      { para: '/usuarios', rotulo: 'Usuários', icone: Users, permissao: 'usuarios' },
      { para: '/auditoria', rotulo: 'Auditoria', icone: ScrollText, permissao: 'auditoria' },
    ],
  },
  {
    grupo: 'Análise',
    itens: [{ para: '/relatorios', rotulo: 'Relatórios', icone: FileBarChart, permissao: 'relatorios' }],
  },
];

export default function Layout() {
  const { usuario, sair, pode } = useAuth();
  const [menuAberto, setMenuAberto] = useState(false);
  const navigate = useNavigate();

  const nav = (
    <nav aria-label="Menu principal" className="flex-1 space-y-5 overflow-y-auto px-3 py-4">
      {MENU.map((g) => {
        const itens = g.itens.filter((i) => pode(i.permissao));
        if (itens.length === 0) return null;
        return (
          <div key={g.grupo}>
            <p className="px-2 text-xs font-semibold uppercase tracking-wider text-marca-100/70">{g.grupo}</p>
            <ul className="mt-1 space-y-0.5">
              {itens.map((i) => (
                <li key={i.para}>
                  <NavLink
                    to={i.para}
                    end={i.para === '/movimentacoes'}
                    onClick={() => setMenuAberto(false)}
                    className={({ isActive }) =>
                      clsx(
                        'flex items-center gap-2 rounded-md px-2 py-2 text-sm',
                        isActive ? 'bg-white/15 font-semibold text-white' : 'text-marca-50 hover:bg-white/10',
                      )
                    }
                  >
                    <i.icone aria-hidden className="h-4 w-4" />
                    {i.rotulo}
                  </NavLink>
                </li>
              ))}
            </ul>
          </div>
        );
      })}
    </nav>
  );

  return (
    <div className="flex min-h-screen">
      <aside
        className={clsx(
          'fixed inset-y-0 left-0 z-30 flex w-64 flex-col bg-marca-700 transition-transform lg:static lg:translate-x-0',
          menuAberto ? 'translate-x-0' : '-translate-x-full',
        )}
      >
        <div className="flex items-center justify-between px-5 py-4">
          <div>
            <p className="text-lg font-bold tracking-wide text-white">SISCEP</p>
            <p className="text-xs text-marca-100">DDA Usinagem Industrial</p>
          </div>
          <button type="button" className="text-white lg:hidden" onClick={() => setMenuAberto(false)} aria-label="Fechar menu">
            <X className="h-5 w-5" />
          </button>
        </div>
        {nav}
        <div className="border-t border-white/10 p-3 text-sm text-marca-50">
          <p className="truncate font-medium text-white">{usuario?.nome}</p>
          <p className="text-xs text-marca-100">{usuario && NOMES_PERFIL[usuario.perfil]}</p>
          <div className="mt-2 flex gap-3">
            <NavLink to="/minha-senha" onClick={() => setMenuAberto(false)} className="inline-flex items-center gap-1 text-xs hover:underline">
              <KeyRound aria-hidden className="h-3 w-3" /> Alterar senha
            </NavLink>
            <button
              type="button"
              className="inline-flex items-center gap-1 text-xs hover:underline"
              onClick={() => {
                sair();
                navigate('/login');
              }}
            >
              <LogOut aria-hidden className="h-3 w-3" /> Sair
            </button>
          </div>
        </div>
      </aside>
      {menuAberto && <div className="fixed inset-0 z-20 bg-slate-900/40 lg:hidden" onClick={() => setMenuAberto(false)} />}
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-10 flex items-center gap-3 border-b border-slate-200 bg-white px-4 py-3 lg:hidden">
          <button type="button" onClick={() => setMenuAberto(true)} aria-label="Abrir menu">
            <Menu className="h-6 w-6 text-slate-700" />
          </button>
          <span className="font-semibold text-marca-700">SISCEP</span>
        </header>
        <main className="mx-auto w-full max-w-7xl flex-1 p-4 sm:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
