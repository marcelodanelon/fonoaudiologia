import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';

const chevron = (open) => (
  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"
    style={{ transition: 'transform 0.2s', transform: open ? 'rotate(90deg)' : 'rotate(0deg)', flexShrink: 0, opacity: 0.6 }}>
    <polyline points="9 18 15 12 9 6"/>
  </svg>
);

export default function Layout({ children }) {
  const { user, logout, hasPermission, showWarning, extendSession } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [readyCount, setReadyCount] = useState(0);
  const [openSection, setOpenSection] = useState(null);

  const toggle = (key) => setOpenSection(prev => prev === key ? null : key);

  useEffect(() => {
    const path = location.pathname;
    if (path === '/') setOpenSection(null);
    else if (path === '/recepcao' || path === '/atendimentos') setOpenSection('atendimento');
    else if (path === '/pacientes' || path === '/operadores' || path === '/unidades') setOpenSection('cadastros');
    else if (path.startsWith('/relatorios')) setOpenSection('relatorios');
    else if (path === '/auditoria' || path === '/configuracoes') setOpenSection('administracao');
  }, [location.pathname]);

  useEffect(() => {
    let id;
    const check = async () => {
      try {
        const today = new Date().toISOString().slice(0, 10);
        const [readyRes, aptRes] = await Promise.all([
          api.get('/reception/ready', { params: { startDate: today, endDate: today } }),
          api.get('/appointments/scheduled/' + today).catch(() => ({ data: [] })),
        ]);
        setReadyCount(readyRes.data.length + aptRes.data.length);
      } catch { setReadyCount(0); }
    };
    check();
    id = setInterval(check, 10000);
    return () => clearInterval(id);
  }, []);

  const handleLogout = () => { logout(); navigate('/login'); };
  const initials = user?.name?.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase() || '??';

  return (
    <div className="layout">
      {showWarning && (
        <div className="modal-overlay" style={{ zIndex: 300 }}>
          <div className="modal" style={{ textAlign: 'center' }}>
            <div className="modal-body" style={{ padding: '36px 32px' }}>
              <div style={{ width: 56, height: 56, borderRadius: 16, background: '#fffbeb', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', marginBottom: 20 }}>
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" strokeWidth="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              </div>
              <h3 style={{ marginBottom: 8, fontWeight: 700, fontSize: 18 }}>Sessão Expirando</h3>
              <p style={{ marginBottom: 24, color: 'var(--text-muted)', fontSize: 14, lineHeight: 1.6 }}>
                Sua sessão expira em menos de 1 minuto por inatividade.
              </p>
              <div style={{ display: 'flex', gap: 10, justifyContent: 'center' }}>
                <button className="btn btn-primary" onClick={extendSession} style={{ width: 170 }}>Continuar Logado</button>
                <button className="btn btn-secondary" onClick={handleLogout} style={{ width: 110 }}>Sair</button>
              </div>
            </div>
          </div>
        </div>
      )}

      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="sidebar-brand">
            <div className="brand-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>
            </div>
            <div>
              <h2>FonoSystem</h2>
              <p>Gestão de Fonoaudiologia</p>
            </div>
          </div>
        </div>

        <nav className="sidebar-nav">
          {hasPermission('dashboard') && (
            <NavLink to="/" className="nav-section nav-link-section" end>
              Início
            </NavLink>
          )}

          {hasPermission('inventory') && (
            <div>
              <div className="nav-section clickable" onClick={() => toggle('estoque')}>
                <span>Estoque</span>{chevron(openSection === 'estoque')}
              </div>
              <div className={`nav-items-group ${openSection === 'estoque' ? 'expanded' : 'collapsed'}`}>
                <div>
                  <NavLink to="/estoque/insumos" className="nav-item">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>
                    Insumos
                  </NavLink>
                  <NavLink to="/estoque/entradas" className="nav-item">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></svg>
                    Entradas
                  </NavLink>
                  <NavLink to="/estoque/saidas" className="nav-item">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></svg>
                    Saídas
                  </NavLink>
                </div>
              </div>
            </div>
          )}

          {hasPermission('reception') || hasPermission('consultation') ? (
            <div>
              <div className="nav-section clickable" onClick={() => toggle('atendimento')}>
                <span>Atendimento</span>{chevron(openSection === 'atendimento')}
              </div>
              <div className={`nav-items-group ${openSection === 'atendimento' ? 'expanded' : 'collapsed'}`}>
                <div>
                  {hasPermission('reception') && (
                    <NavLink to="/recepcao" className="nav-item">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4-4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
                      Recepção
                    </NavLink>
                  )}
                  {hasPermission('consultation') && (
                    <NavLink to="/atendimentos" className="nav-item">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14,2 14,8 20,8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
                      Consultas
                      {readyCount > 0 && <span className="badge badge-info" style={{ marginLeft: 6, fontSize: 10, padding: '1px 6px' }}>{readyCount}</span>}
                    </NavLink>
                  )}
                </div>
              </div>
            </div>
          ) : null}

          {hasPermission('consultation') || hasPermission('reception') ? (
            <div>
              <div className="nav-section clickable" onClick={() => toggle('agendamento')}>
                <span>Agendamento</span>{chevron(openSection === 'agendamento')}
              </div>
              <div className={`nav-items-group ${openSection === 'agendamento' ? 'expanded' : 'collapsed'}`}>
                <div>
                  <NavLink to="/horarios" className="nav-item">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    Horários
                  </NavLink>
                  <NavLink to="/agendamentos" className="nav-item">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                    Agendamentos
                  </NavLink>
                </div>
              </div>
            </div>
          ) : null}

          {hasPermission('patients') || hasPermission('operators') ? (
            <div>
              <div className="nav-section clickable" onClick={() => toggle('cadastros')}>
                <span>Cadastros</span>{chevron(openSection === 'cadastros')}
              </div>
              <div className={`nav-items-group ${openSection === 'cadastros' ? 'expanded' : 'collapsed'}`}>
                <div>
                  {hasPermission('patients') && (
                    <NavLink to="/pacientes" className="nav-item">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4-4v2"/><circle cx="12" cy="7" r="4"/></svg>
                      Pacientes
                    </NavLink>
                  )}
                  {hasPermission('operators') && (
                    <NavLink to="/operadores" className="nav-item">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                      Operadores
                    </NavLink>
                  )}
                  {hasPermission('systemConfig') && (
                    <NavLink to="/unidades" className="nav-item">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                      Unidades
                    </NavLink>
                  )}
                </div>
              </div>
            </div>
          ) : null}

          {hasPermission('consultation') || hasPermission('reception') ? (
            <div>
              <div className="nav-section clickable" onClick={() => toggle('relatorios')}>
                <span>Relatórios</span>{chevron(openSection === 'relatorios')}
              </div>
              <div className={`nav-items-group ${openSection === 'relatorios' ? 'expanded' : 'collapsed'}`}>
                <div>
                  {hasPermission('consultation') && (
                    <NavLink to="/relatorios/pacientes" className="nav-item">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4-4v2"/><circle cx="12" cy="7" r="4"/></svg>
                      Rel. Pacientes
                    </NavLink>
                  )}
                  {hasPermission('consultation') && (
                    <NavLink to="/relatorios/atendimentos" className="nav-item">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14,2 14,8 20,8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
                      Rel. Atendimentos
                    </NavLink>
                  )}
                  {hasPermission('reception') && (
                    <NavLink to="/relatorios/recepcoes" className="nav-item">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4-4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
                      Rel. Recepções
                    </NavLink>
                  )}
                </div>
              </div>
            </div>
          ) : null}

          {hasPermission('auditLog') || hasPermission('systemConfig') ? (
            <div>
              <div className="nav-section clickable" onClick={() => toggle('administracao')}>
                <span>Administração</span>{chevron(openSection === 'administracao')}
              </div>
              <div className={`nav-items-group ${openSection === 'administracao' ? 'expanded' : 'collapsed'}`}>
                <div>
                  {hasPermission('auditLog') && (
                    <NavLink to="/auditoria" className="nav-item">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="12 8 12 12 14 14"/><circle cx="12" cy="12" r="10"/></svg>
                      Auditoria
                    </NavLink>
                  )}
                  {hasPermission('systemConfig') && (
                    <NavLink to="/configuracoes" className="nav-item">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/></svg>
                      Configurações
                    </NavLink>
                  )}
                </div>
              </div>
            </div>
          ) : null}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">{initials}</div>
            <div className="user-details">
              <div className="user-name">{user?.name}</div>
              <div className="user-role">{user?.roleName}</div>
            </div>
          </div>
          <button className="btn-logout" onClick={handleLogout}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
            Sair do Sistema
          </button>
        </div>
      </aside>

      <main className="main-content">
        {children}
      </main>
    </div>
  );
}
