import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { statusLabel } from '../utils/statusLabels';

export default function PatientHistory() {
  const [patients, setPatients] = useState([]);
  const [search, setSearch] = useState('');
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [consultations, setConsultations] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editPatient, setEditPatient] = useState(null);
  const [message, setMessage] = useState({ text: '', type: '' });
  const [form, setForm] = useState({
    name: '', cpf: '', rg: '', birthDate: '', phone: '', phone2: '',
    email: '', address: '', city: '', state: '', observations: '', active: true,
  });
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');

  useEffect(() => { loadPatients(); }, []);

  const loadPatients = async () => {
    const res = await api.get('/patients');
    setPatients(res.data);
  };

  const showMessage = (text, type = 'success') => {
    setMessage({ text, type });
    setTimeout(() => setMessage({ text: '', type: '' }), 3000);
  };

  const loadPatientHistory = async (patient) => {
    setSelectedPatient(patient);
    const [cRes, aRes] = await Promise.all([
      api.get(`/consultations/patient/${patient.id}`),
      api.get(`/audit/entity/PATIENT/${patient.id}`),
    ]);
    setConsultations(cRes.data);
    setAuditLogs(aRes.data);
  };

  const handleSave = async () => {
    try {
      if (editPatient) {
        await api.put(`/patients/${editPatient.id}`, form);
        showMessage('Paciente atualizado!');
      } else {
        await api.post('/patients', form);
        showMessage('Paciente cadastrado!');
      }
      setShowForm(false);
      setEditPatient(null);
      loadPatients();
    } catch (err) {
      showMessage(err.response?.data?.message || 'Erro ao salvar', 'error');
    }
  };

  const handleEdit = (p) => {
    setEditPatient(p);
    setForm({
      name: p.name || '', cpf: p.cpf || '', rg: p.rg || '',
      birthDate: p.birthDate || '', phone: p.phone || '', phone2: p.phone2 || '',
      email: p.email || '', address: p.address || '', city: p.city || '',
      state: p.state || '', observations: p.observations || '', active: p.active,
    });
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!confirm('Deseja desativar este paciente?')) return;
    try {
      await api.delete(`/patients/${id}`);
      showMessage('Paciente desativado!');
      loadPatients();
      if (selectedPatient?.id === id) setSelectedPatient(null);
    } catch (err) {
      showMessage(err.response?.data?.message || 'Erro', 'error');
    }
  };

  const filteredPatients = patients.filter(p => {
    if (dateFrom && p.createdAt && new Date(p.createdAt) < new Date(dateFrom)) return false;
    if (dateTo && p.createdAt && new Date(p.createdAt) > new Date(dateTo + 'T23:59:59')) return false;
    if (!search) return true;
    const s = search.toLowerCase();
    return p.name?.toLowerCase().includes(s) || p.cpf?.includes(s) || p.phone?.includes(s) || p.email?.toLowerCase().includes(s);
  });

  return (
    <div>
      <div className="page-header">
        <h1>Pacientes</h1>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <button className="btn btn-primary" onClick={() => { setEditPatient(null); setForm({ name: '', cpf: '', rg: '', birthDate: '', phone: '', phone2: '', email: '', address: '', city: '', state: '', observations: '', active: true }); setShowForm(true); }}>
            + Novo Paciente
          </button>
        </div>
      </div>

      {message.text && <div className={`alert alert-${message.type}`}>{message.text}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: selectedPatient ? '1fr 1fr' : '1fr', gap: 20 }}>
        <div className="card">
          <div className="search-bar">
            <input placeholder="Buscar por nome ou CPF..." value={search} onChange={e => setSearch(e.target.value)} />
            <input type="date" value={dateFrom} onChange={e => setDateFrom(e.target.value)}
              placeholder="De" style={{ padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', width: 140 }} />
            <input type="date" value={dateTo} onChange={e => setDateTo(e.target.value)}
              placeholder="Ate" style={{ padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', width: 140 }} />
          </div>
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>CPF</th>
                  <th>Telefone</th>
                  <th>Situacao</th>
                  <th>Acoes</th>
                </tr>
              </thead>
              <tbody>
                {filteredPatients.map(p => (
                  <tr key={p.id}
                    style={{ cursor: 'pointer', background: selectedPatient?.id === p.id ? 'var(--primary-light)' : '' }}
                    onClick={() => loadPatientHistory(p)}>
                    <td>{p.name}</td>
                    <td>{p.cpf}</td>
                    <td>{p.phone}</td>
                    <td><span className={`badge ${p.active ? 'badge-success' : 'badge-danger'}`}>{p.active ? 'Ativo' : 'Inativo'}</span></td>
                    <td>
                      <div style={{ display: 'flex', gap: 4 }}>
                        <button className="btn btn-secondary btn-sm" onClick={(e) => { e.stopPropagation(); handleEdit(p); }}>Editar</button>
                        <button className="btn btn-danger btn-sm" onClick={(e) => { e.stopPropagation(); handleDelete(p.id); }}>Excluir</button>
                      </div>
                    </td>
                  </tr>
                ))}
                {filteredPatients.length === 0 && (
                  <tr><td colSpan={5} className="empty-state">Nenhum paciente encontrado</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {selectedPatient && (
          <div>
            <div className="card">
              <div className="card-header">
                <h3>Dados do Paciente</h3>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, fontSize: 14 }}>
                <div><strong>Nome:</strong> {selectedPatient.name}</div>
                <div><strong>CPF:</strong> {selectedPatient.cpf}</div>
                <div><strong>RG:</strong> {selectedPatient.rg || '-'}</div>
                <div><strong>Nascimento:</strong> {selectedPatient.birthDate || '-'}</div>
                <div><strong>Telefone:</strong> {selectedPatient.phone}</div>
                <div><strong>Telefone 2:</strong> {selectedPatient.phone2 || '-'}</div>
                <div><strong>Email:</strong> {selectedPatient.email || '-'}</div>
                <div><strong>Cidade:</strong> {selectedPatient.city || '-'}</div>
                <div style={{ gridColumn: '1/-1' }}><strong>Endereco:</strong> {selectedPatient.address || '-'}</div>
                {selectedPatient.observations && <div style={{ gridColumn: '1/-1' }}><strong>Observacoes:</strong> {selectedPatient.observations}</div>}
              </div>
            </div>

            <div className="card">
              <div className="card-header">
                <h3>Historico de Atendimentos</h3>
              </div>
              {consultations.length > 0 ? (
                <div className="timeline">
                  {consultations.map(c => (
                    <div key={c.id} className="timeline-item">
                      <div className="time">{new Date(c.createdAt).toLocaleString('pt-BR')} — {c.professional?.name}</div>
                      <div className="content">
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                          <strong>{c.type}</strong>
                          <span className={`badge ${c.status === 'CONCLUIDA' ? 'badge-success' : c.status === 'CANCELADA' ? 'badge-danger' : 'badge-warning'}`}>
                            {statusLabel(c.status)}
                          </span>
                        </div>
                        {c.chiefComplaint && <p><strong>Queixa:</strong> {c.chiefComplaint}</p>}
                        {c.diagnosis && <p><strong>Diagnostico:</strong> {c.diagnosis}</p>}
                        {c.conduct && <p><strong>Conduta:</strong> {c.conduct}</p>}
                        {c.operator && <p style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 8 }}>Registrado por: {c.operator?.name}</p>}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="empty-state">Nenhum atendimento registrado</p>
              )}
            </div>

            <div className="card">
              <div className="card-header">
                <h3>Log de Auditoria</h3>
              </div>
              {auditLogs.length > 0 ? (
                <div className="timeline">
                  {auditLogs.slice(0, 10).map(log => (
                    <div key={log.id} className="timeline-item">
                      <div className="time">{new Date(log.createdAt).toLocaleString('pt-BR')}</div>
                      <div className="content" style={{ fontSize: 13 }}>
                        <span className={`badge ${log.action === 'CREATE' ? 'badge-success' : log.action === 'UPDATE' ? 'badge-info' : 'badge-danger'}`}>
                          {log.action}
                        </span>
                        {' '}{log.details}
                        {log.user && <span style={{ color: 'var(--text-muted)' }}> — por {log.user.name}</span>}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="empty-state">Nenhum registro de auditoria</p>
              )}
            </div>
          </div>
        )}
      </div>

      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 600 }}>
            <div className="modal-header">
              <h2>{editPatient ? 'Editar Paciente' : 'Novo Paciente'}</h2>
              <button className="modal-close" onClick={() => setShowForm(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <div className="form-grid">
                <div className="form-group full-width">
                  <label>Nome Completo *</label>
                  <input value={form.name} onChange={e => setForm({...form, name: e.target.value})} required />
                </div>
                <div className="form-group">
                  <label>CPF</label>
                  <input value={form.cpf} onChange={e => setForm({...form, cpf: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>RG</label>
                  <input value={form.rg} onChange={e => setForm({...form, rg: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Data de Nascimento</label>
                  <input type="date" value={form.birthDate} onChange={e => setForm({...form, birthDate: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Telefone *</label>
                  <input value={form.phone} onChange={e => setForm({...form, phone: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Telefone 2</label>
                  <input value={form.phone2} onChange={e => setForm({...form, phone2: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Email</label>
                  <input value={form.email} onChange={e => setForm({...form, email: e.target.value})} />
                </div>
                <div className="form-group full-width">
                  <label>Endereco</label>
                  <input value={form.address} onChange={e => setForm({...form, address: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Cidade</label>
                  <input value={form.city} onChange={e => setForm({...form, city: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Estado</label>
                  <input value={form.state} onChange={e => setForm({...form, state: e.target.value})} />
                </div>
                <div className="form-group full-width">
                  <label>Observacoes</label>
                  <textarea rows={2} value={form.observations} onChange={e => setForm({...form, observations: e.target.value})} />
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleSave} style={{ width: 'auto' }}>
                {editPatient ? 'Atualizar' : 'Cadastrar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
