import { useCallback, useEffect, useState } from 'react';
import { ErroApi } from '../api/cliente';

/** Executa uma consulta assincrona e expoe dados, erro e recarga. */
export function useCarregar<T>(fn: () => Promise<T>, deps: unknown[]) {
  const [dados, setDados] = useState<T | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(true);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const executar = useCallback(fn, deps);

  const recarregar = useCallback(() => {
    setCarregando(true);
    setErro(null);
    executar()
      .then(setDados)
      .catch((e) => setErro(e instanceof ErroApi ? e.message : 'Erro ao carregar os dados.'))
      .finally(() => setCarregando(false));
  }, [executar]);

  useEffect(() => {
    recarregar();
  }, [recarregar]);

  return { dados, erro, carregando, recarregar, setDados };
}

export function mensagemErro(e: unknown): string {
  if (e instanceof ErroApi) {
    return e.detalhes.length ? `${e.message} ${e.detalhes.join('; ')}` : e.message;
  }
  return 'Ocorreu um erro inesperado.';
}
