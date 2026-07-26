import React, { useState, useEffect } from 'react';
import api from '../api/axios';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [stats, setStats] = useState(null);

  useEffect(() => {
    api.get('/dashboard').then(res => setData(res.data));
    api.get('/dashboard/stats').then(res => setStats(res.data));
  }, []);

  if (!data) return <div className="empty-state"><h3>Carregando...</h3></div>;

  const maxConsultations = stats && stats.monthlyData
    ? Math.max(...stats.monthlyData.map(m => m.consultations), 1)
    : 1;

  return (
    <div className="page-full">
      <div className="page-header">
        <h1>Dashboard</h1>
      </div>

      <div className="stats-grid" style={{ flexShrink: 0 }}>
        <div className="stat-card">
          <div className="stat-icon indigo">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4-4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
          </div>
          <div className="stat-info">
            <div className="value">{data.totalPatients}</div>
            <div className="label">Pacientes Cadastrados</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon emerald">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11"/></svg>
          </div>
          <div className="stat-info">
            <div className="value">{data.completedConsultations}</div>
            <div className="label">Consultas Concluidas</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon amber">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          </div>
          <div className="stat-info">
            <div className="value">{data.scheduledConsultations}</div>
            <div className="label">Consultas Agendadas</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon violet">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4-4v-2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
          </div>
          <div className="stat-info">
            <div className="value">{data.totalOperators}</div>
            <div className="label">Operadores Ativos</div>
          </div>
        </div>
      </div>

      {stats && (
        <div className="stats-grid" style={{ flexShrink: 0, gridTemplateColumns: 'repeat(3, 1fr)' }}>
          <div className="stat-card">
            <div className="stat-icon sky">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            </div>
            <div className="stat-info">
              <div className="value">{stats.totalConsultations}</div>
              <div className="label">Total de Consultas</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon emerald">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
            </div>
            <div className="stat-info">
              <div className="value">{stats.consultationsThisMonth}</div>
              <div className="label">Consultas Este Mes</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon amber">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            </div>
            <div className="stat-info">
              <div className="value">{stats.pendingReception}</div>
              <div className="label">Recepcoes Pendentes</div>
            </div>
          </div>
        </div>
      )}

      {stats && stats.monthlyData && stats.monthlyData.length > 0 && (
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="card-header">
            <h3>Atendimentos por Mes</h3>
          </div>
          <div className="card-body">
            <div className="bar-chart">
              {stats.monthlyData.map((item, index) => (
                <div key={index} className="bar-wrapper">
                  <div className="bar-value">{item.consultations}</div>
                  <div
                    className="bar"
                    style={{ height: (item.consultations / maxConsultations) * 140 + 'px' }}
                  />
                  <div className="bar-label">{item.month}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, flex: 1, minHeight: 0 }}>
        <div className="card" style={{ display: 'flex', flexDirection: 'column' }}>
          <div className="card-header">
            <h3>Recepcao Hoje</h3>
          </div>
          <div className="card-body" style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
            <div className="stats-grid" style={{ marginBottom: 0, flex: 1, alignItems: 'center' }}>
              <div className="stat-card" style={{ boxShadow: 'none', border: '1px solid var(--border)' }}>
                <div className="stat-icon sky" style={{ width: 40, height: 40 }}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="9 11 12 14 22 4"/></svg>
                </div>
                <div className="stat-info">
                  <div className="value" style={{ fontSize: 22 }}>{data.checkins}</div>
                  <div className="label">Check-ins</div>
                </div>
              </div>
              <div className="stat-card" style={{ boxShadow: 'none', border: '1px solid var(--border)' }}>
                <div className="stat-icon emerald" style={{ width: 40, height: 40 }}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>
                </div>
                <div className="stat-info">
                  <div className="value" style={{ fontSize: 22 }}>{data.phoneContacts}</div>
                  <div className="label">Contatos Telefonicos</div>
                </div>
              </div>
              <div className="stat-card" style={{ boxShadow: 'none', border: '1px solid var(--border)' }}>
                <div className="stat-icon teal" style={{ width: 40, height: 40 }}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg>
                </div>
                <div className="stat-info">
                  <div className="value" style={{ fontSize: 22 }}>{data.walkins}</div>
                  <div className="label">Porta / Visitas</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="card" style={{ display: 'flex', flexDirection: 'column' }}>
          <div className="card-header">
            <h3>Resumo do Sistema</h3>
          </div>
          <div className="card-body" style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
            <div className="scroll-container" style={{ flex: 1 }}>
              <table>
                <thead><tr><th>Metrica</th><th style={{ textAlign: 'right' }}>Valor</th></tr></thead>
                <tbody>
                  <tr><td>Total de Pacientes</td><td style={{ textAlign: 'right', fontWeight: 700, fontSize: 15 }}>{data.totalPatients}</td></tr>
                  <tr><td>Consultas Concluidas</td><td style={{ textAlign: 'right', fontWeight: 700, fontSize: 15 }}>{data.completedConsultations}</td></tr>
                  <tr><td>Consultas Agendadas</td><td style={{ textAlign: 'right', fontWeight: 700, fontSize: 15 }}>{data.scheduledConsultations}</td></tr>
                  <tr><td>Total de Recepcoes</td><td style={{ textAlign: 'right', fontWeight: 700, fontSize: 15 }}>{data.totalReceptions}</td></tr>
                  <tr><td>Operadores Ativos</td><td style={{ textAlign: 'right', fontWeight: 700, fontSize: 15 }}>{data.totalOperators}</td></tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
