import React, { useState, useEffect } from 'react';
import api from '../api/axios';

export default function AuditLog() {
  const [logs, setLogs] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  useEffect(() => { loadLogs(); }, [page]);

  const loadLogs = async () => {
    const res = await api.get(`/audit?page=${page}&size=50`);
    if (page === 0) {
      setLogs(res.data.content);
    } else {
      setLogs(prev => [...prev, ...res.data.content]);
    }
    setHasMore(!res.data.last);
  };

  const actionBadge = (action) => {
    const map = { CREATE: 'badge-success', UPDATE: 'badge-info', DELETE: 'badge-danger', LOGIN: 'badge-warning', LOGOUT: 'badge-secondary', VIEW: 'badge-secondary' };
    return <span className={`badge ${map[action] || 'badge-secondary'}`}>{action}</span>;
  };

  return (
    <div>
      <div className="page-header">
        <h1>Registro de Auditoria</h1>
      </div>

      <div className="card">
        <div className="card-header">
          <h3>Logs do Sistema</h3>
        </div>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Data/Hora</th>
                <th>Usuario</th>
                <th>Acao</th>
                <th>Entidade</th>
                <th>Detalhes</th>
                <th>IP</th>
              </tr>
            </thead>
            <tbody>
              {logs.map(log => (
                <tr key={log.id}>
                  <td style={{ whiteSpace: 'nowrap' }}>{new Date(log.createdAt).toLocaleString('pt-BR')}</td>
                  <td>{log.user?.name || 'Sistema'}</td>
                  <td>{actionBadge(log.action)}</td>
                  <td>{log.entityType} {log.entityId ? `#${log.entityId}` : ''}</td>
                  <td style={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{log.details}</td>
                  <td>{log.ipAddress}</td>
                </tr>
              ))}
              {logs.length === 0 && (
                <tr><td colSpan={6} className="empty-state">Nenhum registro encontrado</td></tr>
              )}
            </tbody>
          </table>
        </div>

        {hasMore && (
          <div style={{ textAlign: 'center', padding: 16 }}>
            <button className="btn btn-secondary" onClick={() => setPage(p => p + 1)}>
              Carregar mais
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
