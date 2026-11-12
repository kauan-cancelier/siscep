export type Perfil = 'ADMINISTRADOR' | 'ALMOXARIFE' | 'OPERADOR' | 'GESTOR';
export type Situacao = 'NORMAL' | 'CRITICO' | 'ZERADO';
export type TipoMovimentacao = 'ENTRADA' | 'SAIDA' | 'AJUSTE';

export interface Pagina<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
  ativo: boolean;
  bloqueado: boolean;
  ultimoAcesso: string | null;
  criadoEm: string;
}

export interface LoginResposta {
  token: string;
  tipo: string;
  expiraEmSegundos: number;
  usuario: Usuario;
}

export interface Fabricante {
  id: number;
  nome: string;
  pais: string | null;
  site: string | null;
  ativo: boolean;
}

export interface Fornecedor {
  id: number;
  razaoSocial: string;
  cnpj: string | null;
  contato: string | null;
  telefone: string | null;
  email: string | null;
  prazoMedioEntregaDias: number | null;
  ativo: boolean;
}

export interface Pastilha {
  id: number;
  codigo: string;
  descricao: string;
  especificacao: string | null;
  unidadeMedida: string;
  fabricanteId: number | null;
  fabricanteNome: string | null;
  fornecedorPrincipalId: number | null;
  fornecedorPrincipalNome: string | null;
  estoqueMinimo: number;
  saldoAtual: number;
  situacao: Situacao;
  ativo: boolean;
}

export interface Movimentacao {
  id: number;
  tipo: TipoMovimentacao;
  pastilhaId: number;
  pastilhaCodigo: string;
  pastilhaDescricao: string;
  quantidade: number;
  saldoAnterior: number;
  saldoResultante: number;
  usuarioId: number;
  usuarioNome: string;
  fornecedorId: number | null;
  fornecedorNome: string | null;
  documentoReferencia: string | null;
  destino: string | null;
  justificativa: string | null;
  observacao: string | null;
  dataHora: string;
}

export interface Alerta {
  id: number;
  pastilhaId: number;
  pastilhaCodigo: string;
  pastilhaDescricao: string;
  situacao: Situacao;
  saldo: number;
  estoqueMinimo: number;
  geradoEm: string;
  resolvido: boolean;
  resolvidoEm: string | null;
}

export interface Dashboard {
  inicio: string;
  fim: string;
  totalItens: number;
  itensCriticos: number;
  itensZerados: number;
  itensNormais: number;
  saldoTotal: number;
  movimentacoesPeriodo: number;
  entradasPeriodo: number;
  saidasPeriodo: number;
  ajustesPeriodo: number;
  alertasAbertos: number;
  maiorConsumo: { pastilhaId: number; codigo: string; descricao: string; total: number }[];
  movimentoDiario: { dia: string; entradas: number; saidas: number }[];
  itensEmAlerta: Pastilha[];
}

export interface ConsumoItem {
  pastilhaId: number;
  codigo: string;
  descricao: string;
  totalConsumido: number;
  quantidadeSaidas: number;
  mediaDiaria: number;
  mediaMensal: number;
}

export interface ReposicaoItem {
  pastilhaId: number;
  codigo: string;
  descricao: string;
  situacao: Situacao;
  saldoAtual: number;
  estoqueMinimo: number;
  quantidadeSugerida: number;
  fornecedor: string | null;
  contatoFornecedor: string | null;
  prazoEntregaDias: number | null;
  consumoMedioDiario: number;
  diasParaEsgotar: number | null;
}

export interface Auditoria {
  id: number;
  usuarioNome: string;
  acao: string;
  entidade: string;
  entidadeId: number | null;
  detalhes: string | null;
  criadoEm: string;
}

export interface HistoricoMinimo {
  id: number;
  valorAnterior: number;
  valorNovo: number;
  usuarioNome: string;
  alteradoEm: string;
}

export interface RegistroAcesso {
  id: number;
  email: string;
  sucesso: boolean;
  motivo: string | null;
  ip: string | null;
  dataHora: string;
}

export interface ImportacaoResultado {
  linhasProcessadas: number;
  pastilhasCriadas: number;
  saldosIniciais: number;
  erros: string[];
}
