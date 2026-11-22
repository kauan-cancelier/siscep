const CHAVE_TOKEN = 'siscep.token';
const BASE = import.meta.env.VITE_API_URL ?? '';

export class ErroApi extends Error {
  constructor(
    public readonly status: number,
    mensagem: string,
    public readonly detalhes: string[] = [],
  ) {
    super(mensagem);
  }
}

export const tokenStorage = {
  obter: () => localStorage.getItem(CHAVE_TOKEN),
  salvar: (t: string) => localStorage.setItem(CHAVE_TOKEN, t),
  limpar: () => localStorage.removeItem(CHAVE_TOKEN),
};

let aoExpirar: (() => void) | null = null;
export function registrarAoExpirar(fn: () => void) {
  aoExpirar = fn;
}

type Params = Record<string, string | number | boolean | null | undefined>;

export function montarQuery(params?: Params): string {
  if (!params) return '';
  const q = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') q.append(k, String(v));
  });
  const s = q.toString();
  return s ? `?${s}` : '';
}

async function requisicao(metodo: string, url: string, corpo?: unknown, params?: Params): Promise<Response> {
  const headers: Record<string, string> = {};
  const token = tokenStorage.obter();
  if (token) headers.Authorization = `Bearer ${token}`;
  let body: BodyInit | undefined;
  if (corpo instanceof FormData) {
    body = corpo;
  } else if (corpo !== undefined) {
    headers['Content-Type'] = 'application/json';
    body = JSON.stringify(corpo);
  }
  let resp: Response;
  try {
    resp = await fetch(`${BASE}${url}${montarQuery(params)}`, { method: metodo, headers, body });
  } catch {
    throw new ErroApi(0, 'Não foi possível conectar ao servidor. Verifique sua conexão.');
  }
  if (!resp.ok) {
    let mensagem = 'Ocorreu um erro inesperado. Tente novamente.';
    let detalhes: string[] = [];
    try {
      const erro = await resp.json();
      mensagem = erro.mensagem ?? mensagem;
      detalhes = erro.detalhes ?? [];
    } catch {
      /* resposta sem corpo JSON */
    }
    if (resp.status === 401 && token && !url.endsWith('/auth/login')) {
      tokenStorage.limpar();
      aoExpirar?.();
    }
    throw new ErroApi(resp.status, mensagem, detalhes);
  }
  return resp;
}

async function json<T>(metodo: string, url: string, corpo?: unknown, params?: Params): Promise<T> {
  const resp = await requisicao(metodo, url, corpo, params);
  if (resp.status === 204) return undefined as T;
  return (await resp.json()) as T;
}

export const api = {
  get: <T>(url: string, params?: Params) => json<T>('GET', url, undefined, params),
  post: <T>(url: string, corpo?: unknown) => json<T>('POST', url, corpo),
  put: <T>(url: string, corpo?: unknown) => json<T>('PUT', url, corpo),
  patch: <T>(url: string, corpo?: unknown) => json<T>('PATCH', url, corpo),
  delete: (url: string) => json<void>('DELETE', url),
  async baixar(url: string, params: Params, nomePadrao: string) {
    const resp = await requisicao('GET', url, undefined, params);
    const blob = await resp.blob();
    const disp = resp.headers.get('Content-Disposition') ?? '';
    const nome = /filename="?([^"]+)"?/.exec(disp)?.[1] ?? nomePadrao;
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = nome;
    link.click();
    URL.revokeObjectURL(link.href);
  },
};
