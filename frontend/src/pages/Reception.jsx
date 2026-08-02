import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import Pagination from '../components/Pagination';
import PatientAutocomplete from '../components/PatientAutocomplete';
import { useToast } from '../context/ToastContext';
import { useConfirm } from '../context/ConfirmContext';
import { statusLabel } from '../utils/statusLabels';

const PAGE_SIZE = 12;

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const [y, m, d] = dateStr.split('-');
  return `${d}/${m}/${y}`;
};

export default function Reception() {
  const toast = useToast();
  const confirm = useConfirm();
  const [patients, setPatients] = useState([]);
  const [records, setRecords] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [showNewPatient, setShowNewPatient] = useState(false);
  const [newPatient, setNewPatient] = useState({ name: '', cpf: '', phone: '', email: '', birthDate: '' });
  const [recordPage, setRecordPage] = useState(0);
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [units, setUnits] = useState([]);
  const [selectedUnit, setSelectedUnit] = useState('');
  const [loaded, setLoaded] = useState(false);
  const [showRegisterModal, setShowRegisterModal] = useState(false);
  const [modalType, setModalType] = useState('CHECKIN');
  const [modalNotes, setModalNotes] = useState('');
  const [modalUnit, setModalUnit] = useState('');
  const [modalPatient, setModalPatient] = useState(null);
  const [filterSearch, setFilterSearch] = useState('');
  const [filterType, setFilterType] = useState('');
  const [filterContact, setFilterContact] = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  useEffect(() => { loadPatients(); }, []);
  useEffect(() => { setLoaded(false); }, [selectedDate, selectedUnit]);

  const loadPatients = async () => { const res = await api.get('/patients'); setPatients(res.data); };

  const loadUnits = async () => {
    try {
      const res = await api.get('/service-units');
      setUnits(res.data);
    } catch { setUnits([]); }
  };

  useEffect(() => { loadUnits(); }, []);

  const loadRecords = async () => {
    try {
      const params = { startDate: selectedDate, endDate: selectedDate };
      const aptParams = {};
      if (selectedUnit) {
        params.unitId = selectedUnit;
        aptParams.unitId = selectedUnit;
      }
      const [recRes, aptRes] = await Promise.all([
        api.get('/reception', { params }),
        api.get('/appointments/scheduled/' + selectedDate, { params: aptParams }).catch(() => ({ data: [] })),
      ]);
      setRecords(recRes.data);
      setAppointments(aptRes.data);
      setLoaded(true);
    } catch { toast.error('Erro ao carregar registros'); }
  };

  const openRegisterModal = () => {
    setModalType('CHECKIN');
    setModalNotes('');
    setModalUnit(selectedUnit);
    setModalPatient(null);
    setShowRegisterModal(true);
  };

  const handleRegister = async () => {
    if (!modalPatient) { toast.error('Selecione um paciente'); return; }
    if (!modalUnit) { toast.error('Selecione a unidade de atendimento'); return; }
    try {
      const contactMap = { CHECKIN: 'PORTA', PHONE_CONTACT: 'TELEFONE', WALKIN: 'PORTA' };
      await api.post('/reception', {
        patientId: modalPatient.id,
        unitId: Number(modalUnit),
        type: modalType, contactType: contactMap[modalType], notes: modalNotes,
      });
      toast.success('Registro realizado com sucesso!');
      setShowRegisterModal(false);
      loadRecords();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao registrar'); }
  };

  const handleCheckinFromAppointment = async (apt) => {
    const ok = await confirm(`Realizar check-in de ${apt.patient?.name}?`);
    if (!ok) return;
    try {
      const unitId = selectedUnit || apt.unit?.id;
      await api.post('/reception', {
        appointmentId: apt.id,
        patientId: apt.patient?.id,
        unitId: Number(unitId),
        type: 'CHECKIN', contactType: 'AGENDAMENTO',
        notes: `Check-in a partir do agendamento as ${apt.time} com ${apt.professional?.name}`,
      });
      toast.success('Check-in realizado com sucesso!');
      loadRecords();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao realizar check-in'); }
  };

  const handleCreatePatient = async () => {
    try {
      const created = await api.post('/patients', { ...newPatient, active: true });
      toast.success('Paciente cadastrado com sucesso!');
      setShowNewPatient(false);
      setNewPatient({ name: '', cpf: '', phone: '', email: '', birthDate: '' });
      loadPatients();
      if (created.data) {
        setModalPatient(created.data);
      }
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
      <div className="page-header">
        <h1>Recepção</h1>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          {loaded && (
            <button className="btn btn-primary" onClick={openRegisterModal}>+ Novo Registro</button>
          )}
        </div>
      </div>

      <div className="page-body">
        <div style={{ display: 'flex', gap: 12, marginBottom: 14, flexShrink: 0, alignItems: 'center' }}>
          <input type="date" value={selectedDate} onChange={e => { setSelectedDate(e.target.value); setRecordPage(0); }}
            style={{ padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', width: 150 }} />
          <select value={selectedUnit} onChange={e => { setSelectedUnit(e.target.value); setRecordPage(0); }}
            style={{ padding: '8px 10px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', background: 'white', width: 200 }}>
            <option value="">Selecione a Unidade...</option>
            {units.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
          </select>
          <button className="btn btn-primary" onClick={() => {
            if (!selectedUnit) { toast.warning('Selecione a unidade de atendimento primeiro'); return; }
            if (!selectedDate) { toast.warning('Selecione a data'); return; }
            loadRecords();
          }}>
            Carregar
          </button>
          <div style={{ flex: 1 }}>
            <input placeholder="Buscar paciente..." value={filterSearch}
              onChange={e => { setFilterSearch(e.target.value); setRecordPage(0); }}
              style={{ width: '100%', padding: '10px 14px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 14, fontFamily: 'inherit' }} />
          </div>
          <select value={filterStatus} onChange={e => { setFilterStatus(e.target.value); setRecordPage(0); }}
            style={{ padding: '8px 10px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', background: 'white', width: 140 }}>
            <option value="">Todos Status</option>
            <option value="PENDENTE">Pendente</option>
            <option value="ATENDIDO">Atendido</option>
            <option value="CANCELADO">Cancelado</option>
          </select>
          <select value={filterType} onChange={e => { setFilterType(e.target.value); setRecordPage(0); }}
            style={{ padding: '8px 10px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', background: 'white', width: 130 }}>
            <option value="">Todos Tipos</option>
            <option value="CHECKIN">Check-in</option>
            <option value="PHONE_CONTACT">Telefone</option>
            <option value="WALKIN">Porta</option>
          </select>
          <select value={filterContact} onChange={e => { setFilterContact(e.target.value); setRecordPage(0); }}
            style={{ padding: '8px 10px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', background: 'white', width: 130 }}>
            <option value="">Todas Origens</option>
            <option value="PORTA">Demanda</option>
            <option value="TELEFONE">Telefone</option>
            <option value="AGENDAMENTO">Agendamento</option>
          </select>
          {loaded && (
            <span style={{ fontSize: 12, color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
              {filteredRecords.length} registro(s)
            </span>
          )}
        </div>

        {!loaded && (
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '45vh', gap: 10, color: 'var(--text-muted)', textAlign: 'center' }}>
            <div style={{ width: 64, height: 64, borderRadius: '50%', background: '#eef2ff', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="6" y="2" width="12" height="20" rx="2"/><line x1="12" y1="18" x2="12.01" y2="18"/></svg>
            </div>
            <div style={{ fontSize: 15, fontWeight: 600, color: 'var(--text-secondary)' }}>Aguardando registros...</div>
            <div style={{ fontSize: 12, maxWidth: 420, lineHeight: 1.6 }}>
              Selecione a unidade de atendimento e a data e clique em <strong>Carregar</strong> para visualizar os registros.
            </div>
          </div>
        )}

        {loaded && (
        <>
        <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <div className="card-header">
            <h3>Registros</h3>
            {appointments.length > 0 && (
              <span className="badge badge-info">{appointments.length} agendado(s)</span>
            )}
          </div>
          <div className="card-body" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
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
                    <tr><td colSpan={6} className="empty-state" style={{ padding: 40 }}>Nenhum registro encontrado</td></tr>
                  )}
                </tbody>
              </table>
            </div>
            <Pagination page={recordPage} totalPages={recordTotalPages} onPageChange={setRecordPage} />
          </div>
        </div>

        <div className="card" style={{ flexShrink: 0, marginTop: 4 }}>
          <div className="card-header">
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <h3 style={{ margin: 0 }}>Agendamentos do Dia</h3>
              <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>
                {formatDate(selectedDate)}
              </span>
              <button className="btn btn-secondary" style={{ fontSize: 12, padding: '4px 10px' }} onClick={loadRecords}>
                Atualizar
              </button>
            </div>
          </div>
          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 4, padding: '8px 16px' }}>
            {appointments.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '16px 0', color: 'var(--text-muted)', fontSize: 13 }}>
                Nenhum agendamento para a data selecionada
              </div>
            ) : appointments.map(a => (
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
        </>
        )}
      </div>

      {showRegisterModal && (
        <div className="modal-overlay">
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Novo Registro</h2>
              <button className="modal-close" onClick={() => setShowRegisterModal(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <div className="form-group required">
                <label>Paciente</label>
                <PatientAutocomplete
                  patients={patients}
                  value={modalPatient}
                  onSelect={p => setModalPatient(p)}
                  onClear={() => setModalPatient(null)}
                  placeholder="Buscar paciente por nome, CPF ou telefone..."
                />
                <div style={{ marginTop: 6 }}>
                  <button className="btn btn-secondary btn-sm" onClick={() => setShowNewPatient(true)}>+ Novo Paciente</button>
                </div>
              </div>

              <div className="form-group required" style={{ marginBottom: 0 }}>
                <label>Unidade de Atendimento</label>
                <select value={modalUnit} onChange={e => setModalUnit(e.target.value)}>
                  <option value="">Selecione...</option>
                  {units.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
                </select>
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
                <label>Observações</label>
                <textarea rows={3} value={modalNotes} onChange={e => setModalNotes(e.target.value)}
                  placeholder="Observações sobre o registro..." />
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
        <div className="modal-overlay">
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Novo Paciente</h2>
              <button className="modal-close" onClick={() => setShowNewPatient(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <div className="form-grid compact">
                <div className="form-group full-width"><label>Nome Completo</label>
                  <input value={newPatient.name} onChange={e => setNewPatient({...newPatient, name: e.target.value})} /></div>
                <div className="form-group"><label>CPF</label>
                  <input value={newPatient.cpf} onChange={e => setNewPatient({...newPatient, cpf: e.target.value})} placeholder="000.000.000-00" /></div>
                <div className="form-group"><label>Telefone</label>
                  <input value={newPatient.phone} onChange={e => setNewPatient({...newPatient, phone: e.target.value})} placeholder="(00) 00000-0000" /></div>
                <div className="form-group"><label>E-mail</label>
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
