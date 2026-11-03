import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api, registrarAoExpirar, tokenStorage } from '../api/cliente';
import type { LoginResposta, Usuario } from '../api/tipos';
import { pode, type Permissao } from './permissoes';

interface AuthValor {
  usuario: Usuario | null;
  carregando: boolean;
  entrar: (email: string, senha: string) => Promise<Usuario>;
  sair: () => void;
  pode: (p: Permissao) => boolean;
}

const AuthContext = createContext<AuthValor | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [carregando, setCarregando] = useState(() => !!tokenStorage.obter());

  const sair = useCallback(() => {
    tokenStorage.limpar();
    setUsuario(null);
  }, []);

  useEffect(() => {
    registrarAoExpirar(() => setUsuario(null));
    if (!tokenStorage.obter()) return;
    api
      .get<Usuario>('/api/auth/me')
      .then(setUsuario)
      .catch(() => tokenStorage.limpar())
      .finally(() => setCarregando(false));
  }, []);

  const entrar = useCallback(async (email: string, senha: string) => {
    const r = await api.post<LoginResposta>('/api/auth/login', { email, senha });
    tokenStorage.salvar(r.token);
    setUsuario(r.usuario);
    return r.usuario;
  }, []);

  const valor = useMemo<AuthValor>(
    () => ({ usuario, carregando, entrar, sair, pode: (p) => pode(usuario?.perfil, p) }),
    [usuario, carregando, entrar, sair],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthValor {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth deve ser usado dentro de AuthProvider');
  return ctx;
}
