import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useToast } from '../context/ToastContext';

const PERMISSIONS = [
  { key: 'canAccessDashboard', label: 'Dashboard', desc: 'Acesso ao painel inicial com metricas' },
  { key: 'canAccessReception', label: 'Recepcao', desc: 'Registro de check-in, contatos e visitas' },
  { key: 'canAccessConsultation', label: 'Consultas', desc: 'Atendimentos, prontuarios e audiogramas' },
  { key: 'canAccessPatients', label: 'Pacientes', desc: 'Cadastro e historico de pacientes' },
  { key: 'canAccessOperators', label: 'Operadores', desc: 'Gerenciamento de usuarios do sistema' },
  { key: 'canAccessAuditLog', label: 'Auditoria', desc: 'Consulta ao log de auditoria' },
  { key: 'canAccessSystemConfig', label: 'Configuracoes', desc: 'Configuracoes gerais e perfis de acesso' },
];

export default function SystemConfig() {
  const toast = useToast();
  const [configs, setConfigs] = useState([]);
  const [roles, setRoles] = useState([]);
  const [editingRole, setEditingRole] = useState(null);
  const [roleForm, setRoleForm] = useState({});
  const [tab, setTab] = useState('general');

  useEffect(() => { loadConfigs(); loadRoles(); }, []);

  const loadConfigs = async () => { const res = await api.get('/config'); setConfigs(res.data); };
  const loadRoles = async () => { const res = await api.get('/users/roles'); setRoles(res.data); };

  const handleUpdateConfig = async (key, value) => {
    try {
      await api.put('/config', { configKey: key, configValue: value });
      toast.success('Configuracao atualizada!');
      loadConfigs();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao atualizar'); }
  };

  const handleEditRole = (role) => {
    setEditingRole(role);
    setRoleForm({
      description: role.description || '',
      canAccessDashboard: role.canAccessDashboard,
      canAccessReception: role.canAccessReception,
      canAccessConsultation: role.canAccessConsultation,
      canAccessPatients: role.canAccessPatients,
      canAccessOperators: role.canAccessOperators,
      canAccessAuditLog: role.canAccessAuditLog,
      canAccessSystemConfig: role.canAccessSystemConfig,
    });
  };

  const handleSaveRole = async () => {
    try {
      await api.put(`/users/roles/${editingRole.id}`, roleForm);
      toast.success('Perfis atualizados com sucesso!');
      setEditingRole(null);
      loadRoles();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao salvar'); }
  };

  const configDescriptions = {
    'session_timeout_minutes': { label: 'Tempo de Sessao (minutos)', description: 'Tempo de inatividade antes do logout automatico.' },
    'clinic_name': { label: 'Nome da Clinica', description: 'Nome exibido no cabecalho do sistema' },
    'reception_poll_interval': { label: 'Intervalo de Verificacao (ms)', description: 'Tempo em milissegundos para verificar novos pacientes na recepcao. Padrao: 10000ms (10s).' },
  };

  return (
    <div className="page-full">
      <div className="page-header">
        <h1>Configuracoes do Sistema</h1>
      </div>

      <div className="tabs" style={{ flexShrink: 0, marginBottom: 16 }}>
        <button className={`tab ${tab === 'general' ? 'active' : ''}`} onClick={() => setTab('general')}>
          Configuracoes Gerais
        </button>
        <button className={`tab ${tab === 'roles' ? 'active' : ''}`} onClick={() => setTab('roles')}>
          Perfis de Acesso
        </button>
        <button className={`tab ${tab === 'about' ? 'active' : ''}`} onClick={() => setTab('about')}>
          Sobre o Sistema
        </button>
      </div>

      {tab === 'general' && (
        <div className="card" style={{ flex: 1 }}>
          <div className="card-header"><h3>Configuracoes Gerais</h3></div>
          <div className="card-body">
            {configs.map(c => {
              const desc = configDescriptions[c.configKey] || { label: c.configKey, description: c.description };
              return (
                <div key={c.id} style={{ marginBottom: 20, padding: 20, background: 'var(--bg)', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ fontWeight: 600, fontSize: 14, color: 'var(--text)' }}>{desc.label}</div>
                      <p style={{ fontSize: 13, color: 'var(--text-muted)', margin: 0, marginTop: 2 }}>{desc.description}</p>
                    </div>
                    <div className="form-group" style={{ marginBottom: 0, flexShrink: 0, width: 240 }}>
                      <input
                        type={c.configKey.includes('timeout') || c.configKey.includes('poll') ? 'number' : 'text'}
                        defaultValue={c.configValue}
                        id={`config-${c.configKey}`}
                      />
                    </div>
                    <button className="btn btn-primary btn-sm" style={{ flexShrink: 0 }} onClick={() => {
                      const val = document.getElementById(`config-${c.configKey}`).value;
                      handleUpdateConfig(c.configKey, val);
                    }}>Salvar</button>
                  </div>
                  {c.updatedBy && (
                    <p style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 10 }}>
                      Ultima atualizacao por {c.updatedBy.name} em {c.updatedAt ? new Date(c.updatedAt).toLocaleString('pt-BR') : new Date(c.createdAt).toLocaleString('pt-BR')}
                    </p>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {tab === 'roles' && (
        <div style={{ display: 'grid', gridTemplateColumns: editingRole ? '1fr 1fr' : '1fr', gap: 20, flex: 1, minHeight: 0 }}>
          <div className="card" style={{ display: 'flex', flexDirection: 'column' }}>
            <div className="card-header"><h3>Perfis Cadastrados</h3></div>
            <div className="card-body" style={{ flex: 1 }}>
              {roles.map(r => (
                <div key={r.id}
                  style={{ padding: '14px 16px', border: '1px solid var(--border)', borderRadius: 'var(--radius)', marginBottom: 10, cursor: 'pointer', background: editingRole?.id === r.id ? 'var(--primary-light)' : 'white', transition: 'var(--transition)', borderLeft: editingRole?.id === r.id ? '3px solid var(--primary)' : '3px solid transparent' }}
                  onClick={() => handleEditRole(r)}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
                    <strong style={{ fontSize: 15, color: 'var(--text)' }}>{r.name}</strong>
                    <button className="btn btn-secondary btn-sm" style={{ fontSize: 11, padding: '4px 10px' }}>Editar</button>
                  </div>
                  <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 8 }}>{r.description}</p>
                  <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                    {PERMISSIONS.filter(p => r[p.key]).map(p => (
                      <span key={p.key} className="badge badge-success" style={{ fontSize: 10, padding: '2px 8px' }}>{p.label}</span>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {editingRole && (
            <div className="card" style={{ display: 'flex', flexDirection: 'column' }}>
              <div className="card-header">
                <h3>Editar: {editingRole.name}</h3>
                <button className="btn btn-secondary btn-sm" onClick={() => setEditingRole(null)}>Fechar</button>
              </div>
              <div className="card-body" style={{ flex: 1 }}>
                <div className="form-group">
                  <label>Descricao do Perfil</label>
                  <input value={roleForm.description || ''} onChange={e => setRoleForm({...roleForm, description: e.target.value})} />
                </div>

                <div className="form-section-title" style={{ marginTop: 20 }}>Privilegios de Acesso</div>
                <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 14 }}>
                  Selecione os modulos que os usuarios deste perfil podem acessar.
                </p>

                {PERMISSIONS.map(p => (
                  <label key={p.key} style={{ display: 'flex', alignItems: 'flex-start', gap: 12, padding: '10px 14px', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', marginBottom: 8, cursor: 'pointer', background: roleForm[p.key] ? 'var(--primary-light)' : 'white', transition: 'var(--transition)' }}>
                    <input type="checkbox" checked={roleForm[p.key] || false}
                      onChange={e => setRoleForm({...roleForm, [p.key]: e.target.checked})}
                      style={{ marginTop: 2, width: 16, height: 16, accentColor: 'var(--primary)' }} />
                    <div>
                      <div style={{ fontWeight: 600, fontSize: 14, color: 'var(--text)' }}>{p.label}</div>
                      <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>{p.desc}</div>
                    </div>
                  </label>
                ))}

                <div style={{ display: 'flex', gap: 10, marginTop: 20 }}>
                  <button className="btn btn-secondary" onClick={() => setEditingRole(null)}>Cancelar</button>
                  <button className="btn btn-primary" onClick={handleSaveRole}>Salvar Privilegios</button>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {tab === 'about' && (
        <div className="card">
          <div className="card-header"><h3>Sobre o Sistema</h3></div>
          <div className="table-container">
            <table>
              <tbody>
                <tr><td style={{ fontWeight: 600, width: 220 }}>Versao</td><td>1.0.0</td></tr>
                <tr><td style={{ fontWeight: 600 }}>Tecnologia Backend</td><td>Spring Boot 2.7 + Java 8</td></tr>
                <tr><td style={{ fontWeight: 600 }}>Tecnologia Frontend</td><td>React 18 + Vite</td></tr>
                <tr><td style={{ fontWeight: 600 }}>Banco de Dados</td><td>H2 (embebido)</td></tr>
                <tr><td style={{ fontWeight: 600 }}>Autenticacao</td><td>JWT (JSON Web Token)</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
