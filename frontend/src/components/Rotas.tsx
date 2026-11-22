import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { paginaInicial, type Permissao } from '../auth/permissoes';
import { Carregando, Mensagem } from './Ui';

export function RotaPrivada({ children }: { children: ReactNode }) {
  const { usuario, carregando } = useAuth();
  const location = useLocation();
  if (carregando) return <Carregando />;
  if (!usuario) return <Navigate to="/login" replace state={{ de: location.pathname }} />;
  return <>{children}</>;
}

export function ComPermissao({ permissao, children }: { permissao: Permissao; children: ReactNode }) {
  const { pode } = useAuth();
  if (!pode(permissao)) {
    return <Mensagem tipo="aviso">Seu perfil não tem permissão para acessar esta funcionalidade.</Mensagem>;
  }
  return <>{children}</>;
}

export function RedirecionaInicio() {
  const { usuario } = useAuth();
  return <Navigate to={usuario ? paginaInicial(usuario.perfil) : '/login'} replace />;
}
