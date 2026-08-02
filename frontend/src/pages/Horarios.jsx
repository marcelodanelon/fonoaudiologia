import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useToast } from '../context/ToastContext';
import { useConfirm } from '../context/ConfirmContext';

const WEEKDAY_LABELS = [
  { value: 'MONDAY', label: 'Seg' },
  { value: 'TUESDAY', label: 'Ter' },
  { value: 'WEDNESDAY', label: 'Qua' },
  { value: 'THURSDAY', label: 'Qui' },
  { value: 'FRIDAY', label: 'Sex' },
  { value: 'SATURDAY', label: 'Sab' },
  { value: 'SUNDAY', label: 'Dom' },
];

const WEEKDAY_MAP = {};
WEEKDAY_LABELS.forEach(w => { WEEKDAY_MAP[w.value] = w.label; });

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const [y, m, d] = dateStr.split('-');
  return `${d}/${m}/${y}`;
};

const emptyForm = {
  professionalId: '',
  unitId: '',
  startDate: '',
  endDate: '',
  weekdays: [],
  startTime: '08:00',
  endTime: '12:00',
  capacity: 1,
  slotType: 'QUANTIDADE',
  durationMinutes: '',
};

export default function Horários() {
  const toast = useToast();
  const confirm = useConfirm();
  const [slots, setSlots] = useState([]);
  const [professionals, setProfessionals] = useState([]);
  const [units, setUnits] = useState([]);
  const [selectedDate, setSelectedDate] = useState('');
  const [selectedUnit, setSelectedUnit] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editItem, setEditItem] = useState(null);
  const [form, setForm] = useState({ ...emptyForm });
  const [availModal, setAvailModal] = useState(null);

  useEffect(() => { loadData(); }, [selectedDate, selectedUnit]);

  const loadData = async () => {
    try {
      const params = {};
      if (selectedDate) params.date = selectedDate;
      if (selectedUnit) params.unitId = selectedUnit;
      const slotsUrl = selectedDate ? '/schedule-slots/date/' + selectedDate : '/schedule-slots';
      const [slotsRes, profsRes, unitsRes] = await Promise.all([
        api.get(slotsUrl, { params }),
        api.get('/users'),
        api.get('/service-units'),
      ]);
      const slotsData = await Promise.all(slotsRes.data.map(async s => {
        try {
          const availParams = selectedDate ? { date: selectedDate } : {};
          const avail = await api.get(`/schedule-slots/${s.id}/availability`, { params: availParams });
          return { ...s, remaining: avail.data.remaining, occupied: avail.data.occupied, capacity: avail.data.capacity };
        } catch {
          return { ...s, remaining: s.capacity, occupied: 0 };
        }
      }));
      setSlots(slotsData);
      setProfessionals(profsRes.data.filter(u => u.roleName === 'FONOAUDIOLOGO' || u.roleName === 'PROFISSIONAL'));
      setUnits(unitsRes.data);
    } catch { toast.error('Erro ao carregar'); }
  };

  const toggleWeekday = (day) => {
    setForm(f => {
      const days = [...f.weekdays];
      const idx = days.indexOf(day);
      if (idx >= 0) days.splice(idx, 1);
      else days.push(day);
      return { ...f, weekdays: days };
    });
  };

  const openNew = () => {
    setEditItem(null);
    setForm({ ...emptyForm, startDate: selectedDate, endDate: selectedDate });
    setShowForm(true);
  };

  const openEdit = (slot) => {
    setEditItem(slot);
    const days = slot.weekdays ? slot.weekdays.split(',') : [];
    setForm({
      professionalId: slot.professional?.id || '',
      unitId: slot.unit?.id || '',
      startDate: slot.startDate || '',
      endDate: slot.endDate || '',
      weekdays: days,
      startTime: slot.startTime || '08:00',
      endTime: slot.endTime || '12:00',
      capacity: slot.capacity || 1,
      slotType: slot.slotType || 'QUANTIDADE',
      durationMinutes: slot.durationMinutes || '',
    });
    setShowForm(true);
  };

  const handleSave = async () => {
    if (!form.professionalId || !form.unitId || !form.startDate || !form.endDate || form.weekdays.length === 0 || !form.startTime || !form.endTime) {
      toast.warning('Preencha todos os campos obrigatorios');
      return;
    }
    if (form.slotType === 'TEMPO' && (!form.durationMinutes || Number(form.durationMinutes) < 1)) {
      toast.warning('Informe a duração em minutos de cada consulta');
      return;
    }
    try {
      const payload = {
        professionalId: Number(form.professionalId),
        unitId: Number(form.unitId),
        startDate: form.startDate,
        endDate: form.endDate,
        weekdays: form.weekdays.join(','),
        startTime: form.startTime,
        endTime: form.endTime,
        capacity: form.slotType === 'TEMPO' ? 1 : Number(form.capacity),
        slotType: form.slotType,
        durationMinutes: form.slotType === 'TEMPO' ? Number(form.durationMinutes) : null,
      };
      if (editItem) {
        await api.put(`/schedule-slots/${editItem.id}`, payload);
        toast.success('Horário atualizado!');
      } else {
        await api.post('/schedule-slots', payload);
        toast.success('Horário criado!');
      }
      setShowForm(false);
      loadData();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao salvar'); }
  };

  const handleDelete = async (id) => {
    const ok = await confirm('Remover este horário?');
    if (!ok) return;
    try {
      await api.delete(`/schedule-slots/${id}`);
      toast.success('Horário removido!');
      loadData();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao remover'); }
  };

  const showAvailability = async (slot) => {
    try {
      const res = await api.get(`/schedule-slots/${slot.id}/availability`, { params: { date: selectedDate } });
      setAvailModal({ slot, ...res.data });
    } catch { toast.error('Erro ao verificar vagas'); }
  };

  const formatWeekdays = (wd) => {
    if (!wd) return '-';
    return wd.split(',').map(d => WEEKDAY_MAP[d] || d).join(', ');
  };

  return (
    <div className="page-full">
      <div className="page-header">
        <h1>Horários de Atendimento</h1>
        <button className="btn btn-primary" onClick={openNew}>+ Novo Horário</button>
      </div>
      <div className="page-body">
        <div style={{ display: 'flex', gap: 12, marginBottom: 14, flexShrink: 0, alignItems: 'center' }}>
          <input type="date" value={selectedDate} onChange={e => setSelectedDate(e.target.value)}
            style={{ padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', width: 150 }} />
          <select value={selectedUnit} onChange={e => setSelectedUnit(e.target.value)}
            style={{ padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', background: 'white', width: 200 }}>
            <option value="">Todas as Unidades</option>
            {units.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
          </select>
          <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>{slots.length} horário(s){selectedDate ? ' nesta data' : ' no total'}</span>
        </div>

        <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <div className="card-body" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
            <div className="scroll-container" style={{ flex: 1 }}>
              <table>
                <thead>
                  <tr><th>Unidade</th><th>Profissional</th><th>Período</th><th>Dias</th><th>Horário</th><th>Tipo</th><th>Vagas</th><th>Ações</th></tr>
                </thead>
                <tbody>
                  {slots.map(s => (
                    <tr key={s.id}>
                      <td>{s.unit?.name || '-'}</td>
                      <td>{s.professional?.name}</td>
                      <td style={{ whiteSpace: 'nowrap' }}>{formatDate(s.startDate)} a {formatDate(s.endDate)}</td>
                      <td style={{ fontSize: 12 }}>{formatWeekdays(s.weekdays)}</td>
                      <td style={{ whiteSpace: 'nowrap', fontWeight: 600 }}>{s.startTime} - {s.endTime}</td>
                      <td>
                        {s.slotType === 'TEMPO'
                          ? <span className="badge badge-info">{s.durationMinutes || '-'} min/consulta</span>
                          : <span className="badge badge-secondary">Quantidade</span>}
                      </td>
                      <td>
                        <button className="btn btn-secondary btn-sm" onClick={() => showAvailability(s)}
                          style={{ padding: '2px 8px', fontSize: 12 }}>
                          {s.remaining}/{s.capacity} vaga(s)
                        </button>
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: 4 }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => openEdit(s)}>Editar</button>
                          <button className="btn btn-secondary btn-sm" style={{ color: 'var(--danger)' }} onClick={() => handleDelete(s.id)}>Remover</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                  {slots.length === 0 && <tr><td colSpan={8} className="empty-state" style={{ padding: 40 }}>Nenhum horário para esta data</td></tr>}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {showForm && (
        <div className="modal-overlay">
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editItem ? 'Editar Horário' : 'Novo Horário'}</h3>
              <button className="modal-close" onClick={() => setShowForm(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <div className="form-grid compact">
                <div className="form-group full-width required">
                  <label>Unidade de Atendimento</label>
                  <select value={form.unitId} onChange={e => setForm({ ...form, unitId: e.target.value })}>
                    <option value="">Selecione...</option>
                    {units.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
                  </select>
                </div>
                <div className="form-group full-width required">
                  <label>Profissional</label>
                  <select value={form.professionalId} onChange={e => setForm({ ...form, professionalId: e.target.value })}>
                    <option value="">Selecione...</option>
                    {professionals.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                  </select>
                </div>
                <div className="form-group required">
                  <label>Data Início</label>
                  <input type="date" value={form.startDate} onChange={e => setForm({ ...form, startDate: e.target.value })} />
                </div>
                <div className="form-group required">
                  <label>Data Fim</label>
                  <input type="date" value={form.endDate} onChange={e => setForm({ ...form, endDate: e.target.value })} />
                </div>
                <div className="form-group required">
                  <label>Horário Início</label>
                  <input type="time" value={form.startTime} onChange={e => setForm({ ...form, startTime: e.target.value })} />
                </div>
                <div className="form-group required">
                  <label>Horário Fim</label>
                  <input type="time" value={form.endTime} onChange={e => setForm({ ...form, endTime: e.target.value })} />
                </div>
                <div className="form-group required">
                  <label>Tipo de Vaga</label>
                  <select value={form.slotType} onChange={e => setForm({ ...form, slotType: e.target.value })}>
                    <option value="QUANTIDADE">Por Quantidade</option>
                    <option value="TEMPO">Por Tempo</option>
                  </select>
                </div>
                {form.slotType === 'TEMPO' ? (
                  <div className="form-group required">
                    <label>Duração de cada consulta (minutos)</label>
                    <input type="number" min="5" step="5" value={form.durationMinutes}
                      onChange={e => setForm({ ...form, durationMinutes: e.target.value })} placeholder="Ex.: 30" />
                    <span style={{ display: 'block', fontSize: 11, color: 'var(--text-muted)', lineHeight: 1.5, marginTop: 6 }}>
                      Os horários das vagas serão gerados automaticamente entre o horário de início e fim (ex.: 08:00, 08:30, 09:00...).
                    </span>
                  </div>
                ) : (
                  <div className="form-group required">
                    <label>Vagas por Dia (nesse horário)</label>
                    <input type="number" min="1" value={form.capacity} onChange={e => setForm({ ...form, capacity: Number(e.target.value) })} />
                  </div>
                )}
                <div className="form-group full-width">
                  <label>Dias da Semana</label>
                  <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 4 }}>
                    {WEEKDAY_LABELS.map(w => (
                      <label key={w.value}
                        style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer', padding: '4px 8px', borderRadius: 'var(--radius-sm)', border: '1.5px solid var(--border)', fontSize: 12, background: form.weekdays.includes(w.value) ? 'var(--primary-light, #e0f0ff)' : 'white' }}>
                        <input type="checkbox" checked={form.weekdays.includes(w.value)} onChange={() => toggleWeekday(w.value)} style={{ width: 14, height: 14 }} />
                        {w.label}
                      </label>
                    ))}
                  </div>
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleSave}>{editItem ? 'Atualizar' : 'Salvar'}</button>
            </div>
          </div>
        </div>
      )}

      {availModal && (
        <div className="modal-overlay">
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Vagas - {selectedDate}</h3>
              <button className="modal-close" onClick={() => setAvailModal(null)}>&times;</button>
            </div>
            <div className="modal-body" style={{ textAlign: 'center', padding: 24 }}>
              <div style={{ fontSize: 14, marginBottom: 4 }}>{availModal.slot?.professional?.name}</div>
              <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 8 }}>{availModal.slot?.unit?.name || ''}</div>
              <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 16 }}>{availModal.slot?.startTime} - {availModal.slot?.endTime}</div>
              {availModal.slotType === 'TEMPO' ? (
                <>
                  <div style={{ fontSize: 48, fontWeight: 700, color: availModal.remaining > 0 ? 'var(--primary)' : 'var(--danger)' }}>{availModal.remaining}</div>
                  <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>horários disponíveis</div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, justifyContent: 'center', marginTop: 12, maxWidth: 320, margin: '12px auto 0' }}>
                    {(availModal.availableTimes || []).map(t => (
                      <span key={t} style={{ padding: '6px 12px', borderRadius: 'var(--radius-sm)', border: '1.5px solid var(--border)', fontSize: 13, background: 'white' }}>{t}</span>
                    ))}
                    {(availModal.availableTimes || []).length === 0 && <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>Sem horários livres nesta data</span>}
                  </div>
                  <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 12 }}>Total de horários: {availModal.capacity} | Duração: {availModal.durationMinutes} min</div>
                </>
              ) : (
                <>
                  <div style={{ fontSize: 48, fontWeight: 700, color: availModal.remaining > 0 ? 'var(--primary)' : 'var(--danger)' }}>{availModal.remaining}</div>
                  <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>vagas disponíveis</div>
                  <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 8 }}>Total: {availModal.capacity} | Ocupadas: {availModal.occupied}</div>
                </>
              )}
            </div>
            <div className="modal-footer">
              <button className="btn btn-primary" onClick={() => setAvailModal(null)}>Fechar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
