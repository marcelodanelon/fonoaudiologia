import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useToast } from '../context/ToastContext';
import { useConfirm } from '../context/ConfirmContext';
import Pagination from '../components/Pagination';

const PAGE_SIZE = 12;

export default function Agendamentos() {
  const toast = useToast();
  const confirm = useConfirm();
  const [appointments, setAppointments] = useState([]);
  const [patients, setPatients] = useState([]);
  const [professionals, setProfessionals] = useState([]);
  const [availableSlots, setAvailableSlots] = useState([]);
  const [selectedDate, setSelectedDate] = useState('');
  const [searchFilter, setSearchFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [editItem, setEditItem] = useState(null);
  const [form, setForm] = useState({
    patientId: '', professionalId: '', scheduleSlotId: '', date: '', time: '08:00', type: 'CONSULTA', observations: '',
  });

  useEffect(() => { setPage(0); }, [selectedDate, searchFilter, statusFilter]);
  useEffect(() => { loadData(); }, [selectedDate]);

  const loadData = async () => {
    try {
      const params = selectedDate ? { startDate: selectedDate, endDate: selectedDate } : {};
      const [aptsRes, patientsRes, profsRes] = await Promise.all([
        api.get('/appointments', { params }),
        api.get('/patients'),
        api.get('/users'),
      ]);
      setAppointments(aptsRes.data);
      setPatients(patientsRes.data);
      setProfessionals(profsRes.data.filter(u => u.roleName === 'FONOAUDIOLOGO' || u.roleName === 'PROFISSIONAL'));
    } catch { toast.error('Erro ao carregar'); }
  };

  const loadSlotsForDate = async (date) => {
    try {
      const res = await api.get(`/schedule-slots/date/${date}`);
      const slotsWithAvail = await Promise.all(res.data.map(async s => {
        try {
          const avail = await api.get(`/schedule-slots/${s.id}/availability`, { params: { date } });
          return { ...s, remaining: avail.data.remaining, occupied: avail.data.occupied };
        } catch {
          return { ...s, remaining: s.capacity, occupied: 0 };
        }
      }));
      setAvailableSlots(slotsWithAvail);
    } catch {
      setAvailableSlots([]);
    }
  };

  const filtered = appointments.filter(a => {
    if (statusFilter && a.status !== statusFilter) return false;
    if (!searchFilter) return true;
    const s = searchFilter.toLowerCase();
    return a.patient?.name?.toLowerCase().includes(s) || a.professional?.name?.toLowerCase().includes(s) || a.type?.toLowerCase().includes(s);
  });

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paged = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  const openNew = () => {
    setEditItem(null);
    setForm({ patientId: '', professionalId: '', scheduleSlotId: '', date: selectedDate, time: '08:00', type: 'CONSULTA', observations: '' });
    loadSlotsForDate(selectedDate);
    setShowForm(true);
  };

  const openEdit = (a) => {
    setEditItem(a);
    setForm({
      patientId: a.patient?.id || '', professionalId: a.professional?.id || '',
      scheduleSlotId: a.scheduleSlot?.id || '',
      date: a.date || '', time: a.time || '08:00', type: a.type || 'CONSULTA', observations: a.observations || '',
    });
    if (a.date) loadSlotsForDate(a.date);
    setShowForm(true);
  };

  const handleDateChangeInForm = (date) => {
    setForm(f => ({ ...f, date, scheduleSlotId: '', professionalId: '' }));
    loadSlotsForDate(date);
  };

  const handleSlotSelect = (slot) => {
    setForm(f => ({ ...f, scheduleSlotId: slot.id, professionalId: slot.professional?.id || '' }));
  };

  const handleSave = async () => {
    if (!form.patientId || !form.date || !form.time) {
      toast.warning('Preencha os campos obrigatorios');
      return;
    }
    if (form.scheduleSlotId && !form.professionalId) {
      toast.warning('Selecione um horario valido');
      return;
    }
    try {
      const payload = {
        patientId: Number(form.patientId),
        date: form.date, time: form.time, type: form.type, observations: form.observations,
      };
      if (form.professionalId) payload.professionalId = Number(form.professionalId);
      if (form.scheduleSlotId) payload.scheduleSlotId = Number(form.scheduleSlotId);

      if (editItem) {
        await api.put(`/appointments/${editItem.id}`, payload);
        toast.success('Agendamento atualizado!');
      } else {
        await api.post('/appointments', payload);
        toast.success('Agendamento criado!');
      }
      setShowForm(false);
      loadData();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao salvar'); }
  };

  const handleCancel = async (id) => {
    const ok = await confirm('Cancelar este agendamento?');
    if (!ok) return;
    try {
      await api.delete(`/appointments/${id}`);
      toast.success('Agendamento cancelado!');
      loadData();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao cancelar'); }
  };

  const statusBadge = (s) => {
    const map = { AGENDADO: 'badge-info', RECEPCIONADO: 'badge-success', ATENDIDO: 'badge-purple', CANCELADO: 'badge-danger' };
    return <span className={`badge ${map[s] || 'badge-secondary'}`}>{s}</span>;
  };

  const selectedSlot = form.scheduleSlotId ? availableSlots.find(s => s.id === Number(form.scheduleSlotId)) : null;

  return (
    <div className="page-full">
      <div className="page-header">
        <h1>Agendamentos</h1>
        <button className="btn btn-primary" onClick={openNew}>+ Novo Agendamento</button>
      </div>
      <div className="page-body">
        <div style={{ display: 'flex', gap: 12, marginBottom: 14, flexShrink: 0, alignItems: 'center' }}>
          <input type="date" value={selectedDate} onChange={e => setSelectedDate(e.target.value)}
            style={{ padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', width: 150 }} />
          <div style={{ flex: 1 }}>
            <input placeholder="Filtrar por paciente, profissional ou tipo..." value={searchFilter}
              onChange={e => setSearchFilter(e.target.value)}
              style={{ width: '100%', padding: '10px 14px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 14, fontFamily: 'inherit' }} />
          </div>
          <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}
            style={{ padding: '8px 10px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', background: 'white', width: 140 }}>
            <option value="">Todos Status</option>
            <option value="AGENDADO">Agendado</option>
            <option value="RECEPCIONADO">Recepcionado</option>
            <option value="ATENDIDO">Atendido</option>
            <option value="CANCELADO">Cancelado</option>
          </select>
          <span style={{ fontSize: 12, color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>{filtered.length} agendamento(s){selectedDate ? ' nesta data' : ' no total'}</span>
        </div>

        <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <div className="card-body" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
            <div className="scroll-container" style={{ flex: 1 }}>
              <table>
                <thead>
                  <tr><th>Data</th><th>Horario</th><th>Paciente</th><th>Profissional</th><th>Tipo</th><th>Status</th><th>Obs</th><th>Acoes</th></tr>
                </thead>
                <tbody>
                  {paged.map(a => (
                    <tr key={a.id}>
                      <td style={{ whiteSpace: 'nowrap' }}>{a.date ? new Date(a.date + 'T00:00:00').toLocaleDateString('pt-BR') : '-'}</td>
                      <td style={{ whiteSpace: 'nowrap', fontWeight: 600 }}>{a.time}</td>
                      <td>{a.patient?.name}</td>
                      <td>{a.professional?.name}</td>
                      <td>{a.type}</td>
                      <td>{statusBadge(a.status)}</td>
                      <td style={{ maxWidth: 150, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{a.observations || '-'}</td>
                      <td>
                        <div style={{ display: 'flex', gap: 4 }}>
                          {a.status !== 'CANCELADO' && a.status !== 'ATENDIDO' && (
                            <>
                              <button className="btn btn-secondary btn-sm" onClick={() => openEdit(a)}>Editar</button>
                              <button className="btn btn-secondary btn-sm" style={{ color: 'var(--danger)' }} onClick={() => handleCancel(a.id)}>Cancelar</button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                  {filtered.length === 0 && <tr><td colSpan={8} className="empty-state" style={{ padding: 40 }}>Nenhum agendamento encontrado</td></tr>}
                </tbody>
              </table>
            </div>
            <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
          </div>
        </div>
      </div>

      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 560 }}>
            <div className="modal-header">
              <h3>{editItem ? 'Editar Agendamento' : 'Novo Agendamento'}</h3>
              <button className="modal-close" onClick={() => setShowForm(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <div className="form-grid">
                <div className="form-group required">
                  <label>Data</label>
                  <input type="date" value={form.date} onChange={e => handleDateChangeInForm(e.target.value)} />
                </div>
                <div className="form-group required">
                  <label>Paciente</label>
                  <select value={form.patientId} onChange={e => setForm({ ...form, patientId: e.target.value })}>
                    <option value="">Selecione...</option>
                    {patients.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                  </select>
                </div>
              </div>

              {form.date && (
                <div style={{ marginTop: 12 }}>
                  <label style={{ fontSize: 13, fontWeight: 600, display: 'block', marginBottom: 8 }}>Horarios Disponiveis</label>
                  {availableSlots.length === 0 ? (
                    <div style={{ padding: 16, textAlign: 'center', color: 'var(--text-muted)', fontSize: 13, border: '1.5px dashed var(--border)', borderRadius: 'var(--radius-sm)' }}>
                      Nenhum horario disponivel para esta data. Cadastre horarios na aba Horarios.
                    </div>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                      {availableSlots.map(s => (
                        <div key={s.id}
                          onClick={() => s.remaining > 0 && handleSlotSelect(s)}
                          style={{
                            padding: '8px 12px', borderRadius: 'var(--radius-sm)',
                            border: `1.5px solid ${form.scheduleSlotId == s.id ? 'var(--primary)' : 'var(--border)'}`,
                            background: form.scheduleSlotId == s.id ? 'var(--primary-light, #e0f0ff)' : s.remaining === 0 ? '#f5f5f5' : 'white',
                            cursor: s.remaining === 0 ? 'not-allowed' : 'pointer',
                            opacity: s.remaining === 0 ? 0.6 : 1,
                            display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 13,
                          }}>
                          <div>
                            <span style={{ fontWeight: 600 }}>{s.startTime} - {s.endTime}</span>
                            <span style={{ marginLeft: 8, color: 'var(--text-muted)' }}>Dr(a). {s.professional?.name}</span>
                          </div>
                          <span className={`badge ${s.remaining > 0 ? 'badge-success' : 'badge-danger'}`} style={{ fontSize: 11 }}>
                            {s.remaining} vaga(s)
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {selectedSlot && (
                <div className="form-grid" style={{ marginTop: 12 }}>
                  <div className="form-group required">
                    <label>Horario</label>
                    <input type="time" value={form.time} onChange={e => setForm({ ...form, time: e.target.value })}
                      min={selectedSlot.startTime} max={selectedSlot.endTime} />
                  </div>
                  <div className="form-group">
                    <label>Tipo</label>
                    <select value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}>
                      <option value="CONSULTA">Consulta</option>
                      <option value="RETORNO">Retorno</option>
                      <option value="AVALIACAO">Avaliacao</option>
                    </select>
                  </div>
                  <div className="form-group full-width">
                    <label>Observacoes</label>
                    <textarea rows={2} value={form.observations} onChange={e => setForm({ ...form, observations: e.target.value })}
                      placeholder="Observacoes sobre o agendamento..." />
                  </div>
                </div>
              )}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleSave} disabled={!selectedSlot}>{editItem ? 'Atualizar' : 'Salvar'}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
