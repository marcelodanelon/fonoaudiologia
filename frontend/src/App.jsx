import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import { ConfirmProvider } from './context/ConfirmContext';
import Login from './pages/Login';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Reception from './pages/Reception';
import Consultation from './pages/Consultation';
import PatientHistory from './pages/PatientHistory';
import Operators from './pages/Operators';
import SystemConfig from './pages/SystemConfig';
import AuditLog from './pages/AuditLog';
import Reports from './pages/Reports';
import Horarios from './pages/Horarios';
import Agendamentos from './pages/Agendamentos';
import ServiceUnits from './pages/ServiceUnits';
import Insumos from './pages/Insumos';
import Entradas from './pages/Entradas';
import Saidas from './pages/Saidas';

function ProtectedRoute({ children, permission }) {
  const { token, hasPermission } = useAuth();
  if (!token) return <Navigate to="/login" />;
  if (permission && !hasPermission(permission)) {
    return <Navigate to="/" />;
  }
  return <Layout>{children}</Layout>;
}

function AppRoutes() {
  const { token, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', color: 'var(--text-muted)', fontSize: 14 }}>
        Carregando...
      </div>
    );
  }

  return (
    <Routes>
      <Route path="/login" element={token ? <Navigate to="/" /> : <Login />} />
      <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
      <Route path="/recepcao" element={<ProtectedRoute permission="reception"><Reception /></ProtectedRoute>} />
      <Route path="/atendimentos" element={<ProtectedRoute permission="consultation"><Consultation /></ProtectedRoute>} />
      <Route path="/horarios" element={<ProtectedRoute permission="consultation"><Horarios /></ProtectedRoute>} />
      <Route path="/agendamentos" element={<ProtectedRoute permission="consultation"><Agendamentos /></ProtectedRoute>} />
      <Route path="/pacientes" element={<ProtectedRoute permission="patients"><PatientHistory /></ProtectedRoute>} />
      <Route path="/operadores" element={<ProtectedRoute permission="operators"><Operators /></ProtectedRoute>} />
      <Route path="/unidades" element={<ProtectedRoute permission="systemConfig"><ServiceUnits /></ProtectedRoute>} />
      <Route path="/estoque/insumos" element={<ProtectedRoute permission="inventory"><Insumos /></ProtectedRoute>} />
      <Route path="/estoque/entradas" element={<ProtectedRoute permission="inventory"><Entradas /></ProtectedRoute>} />
      <Route path="/estoque/saidas" element={<ProtectedRoute permission="inventory"><Saidas /></ProtectedRoute>} />
      <Route path="/estoque" element={<Navigate to="/estoque/insumos" />} />
      <Route path="/configuracoes" element={<ProtectedRoute permission="systemConfig"><SystemConfig /></ProtectedRoute>} />
      <Route path="/auditoria" element={<ProtectedRoute permission="auditLog"><AuditLog /></ProtectedRoute>} />
      <Route path="/relatorios/pacientes" element={<ProtectedRoute permission="consultation"><Reports type="patients" /></ProtectedRoute>} />
      <Route path="/relatorios/atendimentos" element={<ProtectedRoute permission="consultation"><Reports type="consultations" /></ProtectedRoute>} />
      <Route path="/relatorios/recepcoes" element={<ProtectedRoute permission="reception"><Reports type="receptions" /></ProtectedRoute>} />
      <Route path="/relatorios" element={<Navigate to="/relatorios/pacientes" />} />
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <AuthProvider>
        <ToastProvider>
          <ConfirmProvider>
            <AppRoutes />
          </ConfirmProvider>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
