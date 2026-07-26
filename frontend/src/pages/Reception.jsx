import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import Pagination from '../components/Pagination';
import { useToast } from '../context/ToastContext';
import { useConfirm } from '../context/ConfirmContext';
import { statusLabel } from '../utils/statusLabels';

const PAGE_SIZE = 12;

export default function Reception() {
  const toast = useToast();
  const confirm = useConfirm();
  const [patients, setPatients] = useState([]);
  const [search, setSearch] = useState('');
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [records, setRecords] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [showNewPatient, setShowNewPatient] = useState(false);
  const [newPatient, setNewPatient] = useState({ name: '', cpf: '', phone: '', email: '', birthDate: '' });
  const [recordPage, setRecordPage] = useState(0);
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [showRegisterModal, setShowRegisterModal] = useState(false);
  const [modalType, setModalType] = useState('CHECKIN');
  const [modalNotes, setModalNotes] = useState('');
  const [filterSearch, setFilterSearch] = useState('');
  const [filterType, setFilterType] = useState('');
  const [filterContact, setFilterContact] = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  useEffect(() => { loadPatients(); }, []);
  useEffect(() => { loadRecords(); }, [selectedDate]);

  const loadPatients = async () => { const res = await api.get('/patients'); setPatients(res.data); };
  const loadRecords = async () => {
    const [recRes, aptRes] = await Promise.all([
      api.get('/reception', { params: { startDate: selectedDate, endDate: selectedDate } }),
      api.get('/appointments/scheduled/' + selectedDate).catch(() => ({ data: [] })),
    ]);
    setRecords(recRes.data);
    setAppointments(aptRes.data);
  };

  const filteredPatients = patients.filter(p =>
    (p.name?.toLowerCase().includes(search.toLowerCase()) || p.cpf?.includes(search) || p.phone?.includes(search))
    && search.length > 0
  );

  const openRegisterModal = () => {
    if (!selectedPatient) { toast.error('Selecione um paciente'); return; }
    setModalType('CHECKIN');
    setModalNotes('');
    setShowRegisterModal(true);
  };

  const handleRegister = async () => {
    try {
      const contactMap = { CHECKIN: 'PORTA', PHONE_CONTACT: 'TELEFONE', WALKIN: 'PORTA' };
      await api.post('/reception', {
        patientId: selectedPatient.id,
        type: modalType, contactType: contactMap[modalType], notes: modalNotes,
      });
      toast.success('Registro realizado com sucesso!');
      setSelectedPatient(null); setSearch('');
      setShowRegisterModal(false);
      loadRecords();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao registrar'); }
  };

  const handleCheckinFromAppointment = async (apt) => {
    const ok = await confirm(`Realizar check-in de ${apt.patient?.name}?`);
    if (!ok) return;
    try {
      await api.post('/reception', {
        patientId: apt.patient?.id,
        type: 'CHECKIN', contactType: 'AGENDAMENTO',
        notes: `Check-in a partir do agendamento as ${apt.time} com ${apt.professional?.name}`,
      });
      toast.success('Check-in realizado com sucesso!');
      loadRecords();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao realizar check-in'); }
  };

  const handleCreatePatient = async () => {
    try {
      await api.post('/patients', { ...newPatient, active: true });
      toast.success('Paciente cadastrado com sucesso!');
      setShowNewPatient(false);
      setNewPatient({ name: '', cpf: '', phone: '', email: '', birthDate: '' });
      loadPatients();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao cadastrar'); }
  };

  const filteredRecords = records.filter(r => {
    if (filterSearch && !r.patient?.name?.toLowerCase().includes(filterSearch.toLowerCase())) return false;
    if (filterType && r.type !== filterType) return false;
    if (filterContact && r.contactType !== filterContact) return false;
    if (filterStatus && (r.status || 'PENDENTE') !== filterStatus) return false;
    return true;
  });
  const recordTotalPages = Math.ceil(filteredRecords.length / PAGE_SIZE);
  const paginatedRecords = filteredRecords.slice(recordPage * PAGE_SIZE, (recordPage + 1) * PAGE_SIZE);
  const recordTotalFiltered = filteredRecords.length;
  const typeLabel = (type) => ({ CHECKIN: 'Check-in', PHONE_CONTACT: 'Telefone', WALKIN: 'Porta' })[type] || type;

  return (
    <div className="page-full">
      <div className="page-header" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <h1>Recepcao</h1>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <input type="date" value={selectedDate} onChange={e => setSelectedDate(e.target.value)}
            style={{ padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius)', fontSize: 13, fontFamily: 'inherit' }} />
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <div className="card">
          <div className="card-header" style={{ marginBottom: 0, paddingBottom: 0, border: 'none' }}>
            <h3>Novo Registro</h3>
          </div>
          <div className="card-body" style={{ padding: '0 0 8px' }}>
            <div className="search-bar">
              <input placeholder="Buscar paciente por nome, CPF ou telefone..." value={search}
                onChange={e => { setSearch(e.target.value); if (e.target.value === '') setSelectedPatient(null); }} />
              <button className="btn btn-success btn-sm" onClick={() => setShowNewPatient(true)}>+ Novo</button>
            </div>

            {search && !selectedPatient && filteredPatients.length > 0 && (
              <div className="scroll-container" style={{ maxHeight: 130, marginTop: 10, marginBottom: 10 }}>
                <table>
                  <thead><tr><th>Nome</th><th>CPF</th><th></th></tr></thead>
                  <tbody>
                    {filteredPatients.slice(0, 5).map(p => (
                      <tr key={p.id} style={{ cursor: 'pointer' }}
                        onClick={() => { setSelectedPatient(p); setSearch(p.name); }}>
                        <td>{p.name}</td><td>{p.cpf}</td>
                        <td><button className="btn btn-primary btn-sm" style={{ padding: '3px 8px', fontSize: 11 }}
                          onClick={(e) => { e.stopPropagation(); setSelectedPatient(p); setSearch(p.name); }}>Selecionar</button></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {selectedPatient && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginTop: 10, padding: '10px 14px', background: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: 'var(--radius-sm)' }}>
                <div style={{ flex: 1 }}>
                  <strong>{selectedPatient.name}</strong>
                  <span style={{ fontSize: 12, color: 'var(--text-muted)', marginLeft: 10 }}>CPF: {selectedPatient.cpf}</span>
                  <span style={{ fontSize: 12, color: 'var(--text-muted)', marginLeft: 10 }}>Tel: {selectedPatient.phone}</span>
                </div>
                <button className="btn btn-sm" style={{ padding: '2px 6px', fontSize: 11 }}
                  onClick={() => { setSelectedPatient(null); setSearch(''); }}>x</button>
              </div>
            )}

            {selectedPatient && (
              <div style={{ marginTop: 10, textAlign: 'right' }}>
                <button className="btn btn-primary" onClick={openRegisterModal}>Registrar</button>
              </div>
            )}
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h3>Registros</h3>
            <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
              {appointments.length} agendado(s) | {records.length} registro(s)
            </span>
          </div>
          <div className="card-body">
            {appointments.length > 0 && (
              <div style={{ marginBottom: 12 }}>
                <div style={{ fontSize: 11, fontWeight: 600, color: 'var(--primary)', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 6 }}>Agendamentos do Dia</div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                  {appointments.map(a => (
                    <div key={`apt-${a.id}`} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 10px', background: '#f0f4ff', border: '1px solid #d0dbff', borderRadius: 'var(--radius-sm)', fontSize: 13 }}>
                      <span style={{ fontWeight: 700, color: 'var(--primary)', minWidth: 45 }}>{a.time}</span>
                      <span style={{ fontWeight: 600 }}>{a.patient?.name}</span>
                      <span style={{ color: 'var(--text-muted)', fontSize: 12 }}>- {a.professional?.name}</span>
                      <span className={`badge ${a.status === 'RECEPCIONADO' ? 'badge-success' : a.status === 'ATENDIDO' ? 'badge-purple' : 'badge-info'}`} style={{ fontSize: 10, padding: '1px 6px' }}>{statusLabel(a.status)}</span>
                      <span className="badge badge-secondary" style={{ fontSize: 10, padding: '1px 6px' }}>{a.type}</span>
                      <button className="btn btn-primary btn-sm" style={{ marginLeft: 'auto', padding: '3px 10px', fontSize: 11 }}
                        onClick={() => handleCheckinFromAppointment(a)}>Check-in</button>
                    </div>
                  ))}
                </div>
              </div>
            )}
            <div className="filter-bar">
              <input placeholder="Buscar paciente..." value={filterSearch}
                onChange={e => { setFilterSearch(e.target.value); setRecordPage(0); }} />
              <select value={filterType} onChange={e => { setFilterType(e.target.value); setRecordPage(0); }}>
                <option value="">Todos Tipos</option>
                <option value="CHECKIN">Check-in</option>
                <option value="PHONE_CONTACT">Telefone</option>
                <option value="WALKIN">Porta</option>
              </select>
              <select value={filterContact} onChange={e => { setFilterContact(e.target.value); setRecordPage(0); }}>
                <option value="">Todas Origens</option>
                <option value="PORTA">Demanda</option>
                <option value="TELEFONE">Telefone</option>
                <option value="AGENDAMENTO">Agendamento</option>
              </select>
              <select value={filterStatus} onChange={e => { setFilterStatus(e.target.value); setRecordPage(0); }}>
                <option value="">Todas Situacoes</option>
                <option value="PENDENTE">Pendente</option>
                <option value="ATENDIDO">Atendido</option>
                <option value="CANCELADO">Cancelado</option>
              </select>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
              <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>{recordTotalFiltered} registro(s)</span>
              <button className="btn btn-secondary" style={{ fontSize: 11, padding: '5px 10px' }}
                onClick={() => { setFilterSearch(''); setFilterType(''); setFilterContact(''); setFilterStatus(''); setRecordPage(0); }}>
                Limpar Filtros
              </button>
            </div>
            <div className="scroll-container" style={{ flex: 1 }}>
              <table>
                <thead>
                  <tr><th>Data/Hora</th><th>Tipo</th><th>Origem</th><th>Paciente</th><th>SITUAÇÃO</th><th>Obs</th></tr>
                </thead>
                <tbody>
                  {paginatedRecords.map(r => {
                    const statusMap = { PENDENTE: 'badge-info', ATENDIDO: 'badge-purple', CANCELADO: 'badge-danger' };
                    const statusKey = r.status || 'PENDENTE';
                    const contactLabel = { AGENDAMENTO: 'Agendamento', TELEFONE: 'Telefone', PORTA: 'Demanda' };
                    const contactBadge = { AGENDAMENTO: 'badge-purple', TELEFONE: 'badge-info', PORTA: 'badge-teal' };
                    return (
                      <tr key={r.id}>
                        <td style={{ whiteSpace: 'nowrap' }}>{new Date(r.createdAt).toLocaleString('pt-BR')}</td>
                        <td><span className={`badge ${r.type === 'CHECKIN' ? 'badge-info' : r.type === 'PHONE_CONTACT' ? 'badge-success' : 'badge-warning'}`}>
                          {typeLabel(r.type)}</span></td>
                        <td><span className={`badge ${contactBadge[r.contactType] || 'badge-teal'}`} style={{ fontSize: 10, padding: '1px 6px' }}>{contactLabel[r.contactType] || r.contactType}</span></td>
                        <td>{r.patient?.name || 'N/A'}</td>
                        <td><span className={`badge ${statusMap[statusKey] || 'badge-info'}`} style={{ fontSize: 10, padding: '1px 6px' }}>{statusLabel(statusKey)}</span></td>
                        <td style={{ maxWidth: 120, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{r.notes}</td>
                      </tr>
                    );
                  })}
                  {records.length === 0 && (
                    <tr><td colSpan={6} className="empty-state" style={{ padding: 32 }}>Nenhum registro encontrado</td></tr>
                  )}
                </tbody>
              </table>
            </div>
            <Pagination page={recordPage} totalPages={recordTotalPages} onPageChange={setRecordPage} />
          </div>
        </div>
      </div>

      {showRegisterModal && (
        <div className="modal-overlay" onClick={() => setShowRegisterModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 440 }}>
            <div className="modal-header">
              <h2>Novo Registro</h2>
              <button className="modal-close" onClick={() => setShowRegisterModal(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <div style={{ padding: '10px 14px', background: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: 'var(--radius-sm)', marginBottom: 16 }}>
                <strong>{selectedPatient?.name}</strong>
                <span style={{ fontSize: 12, color: 'var(--text-muted)', marginLeft: 8 }}>CPF: {selectedPatient?.cpf}</span>
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                  <label>Tipo</label>
                  <select value={modalType} onChange={e => setModalType(e.target.value)}>
                    <option value="CHECKIN">Check-in</option>
                    <option value="PHONE_CONTACT">Contato Telefonico</option>
                    <option value="WALKIN">Porta / Visita</option>
                  </select>
                </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>Observacoes</label>
                <textarea rows={3} value={modalNotes} onChange={e => setModalNotes(e.target.value)}
                  placeholder="Observacoes sobre o registro..." />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowRegisterModal(false)}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleRegister}>Registrar</button>
            </div>
          </div>
        </div>
      )}

      {showNewPatient && (
        <div className="modal-overlay" onClick={() => setShowNewPatient(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Novo Paciente</h2>
              <button className="modal-close" onClick={() => setShowNewPatient(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <div className="form-grid">
                <div className="form-group full-width"><label>Nome Completo</label>
                  <input value={newPatient.name} onChange={e => setNewPatient({...newPatient, name: e.target.value})} /></div>
                <div className="form-group"><label>CPF</label>
                  <input value={newPatient.cpf} onChange={e => setNewPatient({...newPatient, cpf: e.target.value})} placeholder="000.000.000-00" /></div>
                <div className="form-group"><label>Telefone</label>
                  <input value={newPatient.phone} onChange={e => setNewPatient({...newPatient, phone: e.target.value})} placeholder="(00) 00000-0000" /></div>
                <div className="form-group"><label>Email</label>
                  <input value={newPatient.email} onChange={e => setNewPatient({...newPatient, email: e.target.value})} /></div>
                <div className="form-group"><label>Data de Nascimento</label>
                  <input type="date" value={newPatient.birthDate} onChange={e => setNewPatient({...newPatient, birthDate: e.target.value})} /></div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowNewPatient(false)}>Cancelar</button>
              <button className="btn btn-success" onClick={handleCreatePatient}>Cadastrar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
