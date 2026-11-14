import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import Layout from './components/Layout';
import { ComPermissao, RedirecionaInicio, RotaPrivada } from './components/Rotas';
import Alertas from './pages/Alertas';
import Dashboard from './pages/Dashboard';
import Estoque from './pages/Estoque';
import Fabricantes from './pages/Fabricantes';
import Fornecedores from './pages/Fornecedores';
import Historico from './pages/Historico';
import Login from './pages/Login';
import MinhaSenha from './pages/MinhaSenha';
import Movimentar from './pages/Movimentar';
import Pastilhas from './pages/Pastilhas';
import Relatorios from './pages/Relatorios';
import Usuarios from './pages/Usuarios';

export function Rotas() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<RotaPrivada><Layout /></RotaPrivada>}>
        <Route index element={<RedirecionaInicio />} />
        <Route path="dashboard" element={<ComPermissao permissao="dashboard"><Dashboard /></ComPermissao>} />
        <Route path="estoque" element={<Estoque />} />
        <Route path="alertas" element={<Alertas />} />
        <Route path="movimentacoes" element={<Historico />} />
        <Route path="movimentacoes/entrada" element={<ComPermissao permissao="entrada"><Movimentar key="e" tipo="ENTRADA" /></ComPermissao>} />
        <Route path="movimentacoes/saida" element={<ComPermissao permissao="saida"><Movimentar key="s" tipo="SAIDA" /></ComPermissao>} />
        <Route path="movimentacoes/ajuste" element={<ComPermissao permissao="ajuste"><Movimentar key="a" tipo="AJUSTE" /></ComPermissao>} />
        <Route path="pastilhas" element={<ComPermissao permissao="cadastros"><Pastilhas /></ComPermissao>} />
        <Route path="fabricantes" element={<ComPermissao permissao="cadastros"><Fabricantes /></ComPermissao>} />
        <Route path="fornecedores" element={<ComPermissao permissao="cadastros"><Fornecedores /></ComPermissao>} />
        <Route path="usuarios" element={<ComPermissao permissao="usuarios"><Usuarios /></ComPermissao>} />
        <Route path="relatorios" element={<ComPermissao permissao="relatorios"><Relatorios /></ComPermissao>} />
        <Route path="minha-senha" element={<MinhaSenha />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <AuthProvider>
        <Rotas />
      </AuthProvider>
    </BrowserRouter>
  );
}
