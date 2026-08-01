import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useConfirm } from '../context/ConfirmContext';
import Pagination from '../components/Pagination';
import AudiogramChart from '../components/AudiogramChart';
import PrintModal from '../components/PrintModal';
import { statusLabel } from '../utils/statusLabels';

const PAGE_SIZE = 10;
const FREQUENCIES = [250, 500, 1000, 2000, 3000, 4000, 6000, 8000];
const emptyAudiogram = {
  right250: '', right500: '', right1000: '', right2000: '', right3000: '', right4000: '', right6000: '', right8000: '',
  left250: '', left500: '', left1000: '', left2000: '', left3000: '', left4000: '', left6000: '', left8000: '',
  hearingLossType: 'NORMAL', observations: '',
};

export default function Consultation() {
  const { user } = useAuth();
  const toast = useToast();
  const confirm = useConfirm();
  const [view, setView] = useState('list');
  const [consultations, setConsultations] = useState([]);
  const [patients, setPatients] = useState([]);
  const [professionals, setProfessionals] = useState([]);
  const [readyPatients, setReadyPatients] = useState([]);
  const [lastRefresh, setLastRefresh] = useState(new Date());
  const [editingConsultation, setEditingConsultation] = useState(null);
  const [selectedConsultation, setSelectedConsultation] = useState(null);
  const [audiogramData, setAudiogramData] = useState({ ...emptyAudiogram });
  const [existingAudiogram, setExistingAudiogram] = useState(null);
  const [formTab, setFormTab] = useState('queixa');
  const [consultPage, setConsultPage] = useState(0);
  const [ear, setEar] = useState('right');
  const [searchFilter, setSearchFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [units, setUnits] = useState([]);
  const [selectedUnit, setSelectedUnit] = useState('');
  const [loaded, setLoaded] = useState(false);
  const [showPostSaveModal, setShowPostSaveModal] = useState(false);
  const [savedConsultationId, setSavedConsultationId] = useState(null);
  const [printModalHtml, setPrintModalHtml] = useState(null);

  const [form, setForm] = useState({
    patientId: '', professionalId: '', unitId: '', type: 'CONSULTA', status: 'AGENDADA',
    chiefComplaint: '', anamnesis: '', clinicalHistory: '', physicalExam: '',
    diagnosis: '', conduct: '', observations: '',
  });
  const [formErrors, setFormErrors] = useState({});

  const isFromReception = (c) => c.receptionRecordId != null;
  const isReadonly = false;
  const isLocked = (editingConsultation?.receptionRecordId != null) || (selectedConsultation?.receptionRecordId != null);
  const currentPatientName = patients.find(p => p.id === form.patientId)?.name || 'Paciente';

  const openReportModal = async (consultationId) => {
    try {
      const res = await api.get(`/consultations/${consultationId}/report`);
      setPrintModalHtml(res.data);
    } catch { toast.error('Erro ao gerar documento'); }
  };

  useEffect(() => {
    setConsultPage(0);
    setLoaded(false);
    setLastRefresh(null);
  }, [selectedDate, selectedUnit]);

  useEffect(() => {
    api.get('/service-units').then(r => setUnits(r.data)).catch(() => {});
  }, []);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && view === 'form') {
        setView('list');
        setSelectedConsultation(null);
        loadData();
      }
      if ((e.ctrlKey || e.metaKey) && e.key === 's' && view === 'form' && !isReadonly) {
        e.preventDefault();
        handleSaveConsultation();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [view, isReadonly]);

  useEffect(() => {
    let intervalId;
    const startPolling = async () => {
      try {
        const res = await api.get('/config');
        const pollConfig = res.data.find(c => c.configKey === 'reception_poll_interval');
        const interval = parseInt(pollConfig?.configValue, 10) || 10000;
        loadReadyPatients();
        intervalId = setInterval(loadReadyPatients, interval);
      } catch {
        intervalId = setInterval(loadReadyPatients, 10000);
      }
    };
    startPolling();
    return () => { if (intervalId) clearInterval(intervalId); };
  }, [selectedUnit, selectedDate]);

  const loadData = async () => {
    try {
      const params = { startDate: selectedDate, endDate: selectedDate };
      if (selectedUnit) params.unitId = selectedUnit;
      const [cRes, pRes, profRes, unitsRes] = await Promise.all([
        api.get('/consultations', { params }),
        api.get('/patients'),
        api.get('/users'),
        api.get('/service-units'),
      ]);
      setConsultations(cRes.data);
      setPatients(pRes.data);
      setProfessionals(profRes.data.filter(u => u.roleName === 'FONOAUDIOLOGO' || u.roleName === 'ADMINISTRADOR'));
      setUnits(unitsRes.data);
      setLoaded(true);
    } catch (err) {
      toast.error('Erro ao carregar dados');
    }
  };

  const loadReadyPatients = async () => {
    try {
      const readyParams = { startDate: selectedDate, endDate: selectedDate };
      const aptParams = {};
      if (selectedUnit) {
        readyParams.unitId = selectedUnit;
        aptParams.unitId = selectedUnit;
      }
      const [readyRes, aptRes] = await Promise.all([
        api.get('/reception/ready', { params: readyParams }),
        api.get('/appointments/scheduled/' + selectedDate, { params: aptParams }).catch(() => ({ data: [] })),
      ]);
      const mappedApts = aptRes.data.map(a => ({
        id: 'apt-' + a.id,
        type: 'APPOINTMENT',
        status: a.status,
        patient: a.patient,
        createdAt: a.date + 'T' + a.time,
        appointmentId: a.id,
        professional: a.professional,
        time: a.time,
        unit: a.unit,
      }));
      setReadyPatients([...readyRes.data, ...mappedApts]);
      setLastRefresh(new Date());
    } catch { setReadyPatients([]); }
  };

  const resetForm = () => {
    setForm({
      patientId: '', professionalId: '', unitId: selectedUnit || '', type: 'CONSULTA', status: 'AGENDADA',
      chiefComplaint: '', anamnesis: '', clinicalHistory: '', physicalExam: '',
      diagnosis: '', conduct: '', observations: '',
    });
    setFormErrors({});
  };

  const handleNewConsultation = () => {
    resetForm();
    setEditingConsultation(null);
    setSelectedConsultation(null);
    setExistingAudiogram(null);
    setAudiogramData({ ...emptyAudiogram });
    setFormTab('queixa');
    setView('form');
  };

  const handleStartFromReady = (record) => {
    const prof = record.professional || professionals.find(p => p.id === user?.id) || professionals[0];
    setForm({
      patientId: record.patient.id,
      professionalId: prof?.id || '',
      unitId: record.unit?.id || selectedUnit || '',
      type: record.type === 'APPOINTMENT' ? (record.time ? 'CONSULTA' : 'CONSULTA') : 'CONSULTA',
      status: 'EM_ANDAMENTO',
      chiefComplaint: '', anamnesis: '', clinicalHistory: '', physicalExam: '',
      diagnosis: '', conduct: '', observations: '',
    });
    setEditingConsultation({ receptionRecordId: record.type === 'APPOINTMENT' ? null : record.id });
    setSelectedConsultation(null);
    setExistingAudiogram(null);
    setAudiogramData({ ...emptyAudiogram });
    setFormTab('queixa');
    setView('form');
  };

  const handleEditConsultation = (c) => {
    setEditingConsultation(c);
    setForm({
      patientId: c.patient?.id || '', professionalId: c.professional?.id || '',
      unitId: c.unit?.id || '',
      type: c.type, status: c.status,
      chiefComplaint: c.chiefComplaint || '', anamnesis: c.anamnesis || '',
      clinicalHistory: c.clinicalHistory || '', physicalExam: c.physicalExam || '',
      diagnosis: c.diagnosis || '', conduct: c.conduct || '', observations: c.observations || '',
    });
    setExistingAudiogram(null);
    setAudiogramData({ ...emptyAudiogram });
    setFormTab('queixa');
    setView('form');
  };

  const handleViewConsultation = async (c) => {
    setSelectedConsultation(c);
    setEditingConsultation(c);
    setForm({
      patientId: c.patient?.id || '', professionalId: c.professional?.id || '',
      unitId: c.unit?.id || '',
      type: c.type, status: c.status,
      chiefComplaint: c.chiefComplaint || '', anamnesis: c.anamnesis || '',
      clinicalHistory: c.clinicalHistory || '', physicalExam: c.physicalExam || '',
      diagnosis: c.diagnosis || '', conduct: c.conduct || '', observations: c.observations || '',
    });
    setExistingAudiogram(null);
    setAudiogramData({ ...emptyAudiogram });
    if (c.status === 'CONCLUIDA') {
      try {
        const res = await api.get(`/audiograms/consultation/${c.id}`);
        if (res.data.length > 0) {
          const a = res.data[0];
          setExistingAudiogram(a);
          setAudiogramData({
            right250: a.right250 ?? '', right500: a.right500 ?? '', right1000: a.right1000 ?? '',
            right2000: a.right2000 ?? '', right3000: a.right3000 ?? '', right4000: a.right4000 ?? '',
            right6000: a.right6000 ?? '', right8000: a.right8000 ?? '',
            left250: a.left250 ?? '', left500: a.left500 ?? '', left1000: a.left1000 ?? '',
            left2000: a.left2000 ?? '', left3000: a.left3000 ?? '', left4000: a.left4000 ?? '',
            left6000: a.left6000 ?? '', left8000: a.left8000 ?? '',
            hearingLossType: a.hearingLossType || 'NORMAL', observations: a.observations || '',
          });
        }
      } catch { /* ignore */ }
    }
    setFormTab('queixa');
    setView('form');
  };

  const validateForm = () => {
    const errors = {};
    if (!form.patientId) errors.patientId = 'Selecione um paciente';
    if (!form.professionalId) errors.professionalId = 'Selecione um profissional';
    if (!form.unitId) errors.unitId = 'Selecione a unidade de atendimento';
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSaveConsultation = async () => {
    if (!validateForm()) {
      toast.warning('Preencha os campos obrigatorios');
      return;
    }
    try {
      const payload = { ...form, status: 'CONCLUIDA' };
      if (editingConsultation?.receptionRecordId && !editingConsultation?.id) {
        payload.receptionRecordId = editingConsultation.receptionRecordId;
      }
      let consultationId = editingConsultation?.id;
      if (consultationId) {
        await api.put(`/consultations/${consultationId}`, payload);
        toast.success('Consulta atualizada!');
      } else {
        const created = await api.post('/consultations', payload);
        consultationId = created.data.id;
        toast.success('Consulta criada!');
      }
      if (consultationId) {
        const hasData = Object.values(audiogramData).some(v => v !== '' && v !== null && v !== undefined && v !== 'NORMAL');
        if (hasData) {
          const aData = {
            consultationId,
            ...Object.fromEntries(Object.entries(audiogramData).map(([k, v]) => [k, v === '' ? null : Number(v)])),
            hearingLossType: audiogramData.hearingLossType, observations: audiogramData.observations,
          };
          if (existingAudiogram) {
            await api.put(`/audiograms/${existingAudiogram.id}`, aData);
          } else {
            await api.post('/audiograms', aData);
          }
        }
      }
      if (editingConsultation?.receptionRecordId && consultationId) {
        try {
          await api.put(`/reception/${editingConsultation.receptionRecordId}`, { status: 'ATENDIDO' });
        } catch { /* ignore */ }
      }
      setSavedConsultationId(consultationId);
      setShowPostSaveModal(true);
      loadData();
      loadReadyPatients();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao salvar'); }
  };

  const updateAudiogramField = (field, value) => {
    if (field === '_setEar') { setEar(value); return; }
    setAudiogramData(prev => ({ ...prev, [field]: value }));
  };

  const statusBadge = (s) => {
    const map = { AGENDADA: 'badge-warning', EM_ANDAMENTO: 'badge-info', CONCLUIDA: 'badge-purple', CANCELADA: 'badge-danger' };
    return <span className={`badge ${map[s] || 'badge-secondary'}`}>{statusLabel(s)}</span>;
  };

  const filteredConsultations = consultations.filter(c => {
    if (statusFilter && c.status !== statusFilter) return false;
    if (typeFilter && c.type !== typeFilter) return false;
    if (!searchFilter) return true;
    const s = searchFilter.toLowerCase();
    return c.patient?.name?.toLowerCase().includes(s) ||
      c.professional?.name?.toLowerCase().includes(s) ||
      c.type?.toLowerCase().includes(s) ||
      c.status?.toLowerCase().includes(s);
  });

  if (view === 'form') {
    const formTabs = [
      { id: 'queixa', label: 'Queixa e Anamnese' },
      { id: 'exame', label: 'Histórico e Exame' },
      { id: 'diagnóstico', label: 'Diagnóstico e Conduta' },
      { id: 'audiograma', label: 'Audiograma' },
    ];

    return (
    <>
      <div className="page-full">
        <div className="page-header">
          <h1>{editingConsultation?.id ? 'Editar' : 'Nova'} Consulta — {currentPatientName}</h1>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <button className="btn btn-secondary" onClick={() => { setView('list'); setSelectedConsultation(null); loadData(); }}>
              &larr; Voltar
            </button>
          </div>
        </div>
        <div className="form-view">
          <div className={`form-group required${formErrors.unitId ? ' error' : ''}`} style={{ marginBottom: 14, flexShrink: 0, maxWidth: 320 }}>
            <label>Unidade de Atendimento</label>
            <select value={form.unitId} onChange={e => { setForm({...form, unitId: e.target.value}); setFormErrors(prev => ({ ...prev, unitId: undefined })); }}
              disabled={isReadonly || isLocked}>
              <option value="">Selecione...</option>
              {units.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
            </select>
            {formErrors.unitId && <span className="form-error">{formErrors.unitId}</span>}
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: 12, marginBottom: 14, flexShrink: 0 }}>
            <div className={`form-group required${formErrors.patientId ? ' error' : ''}`} style={{ marginBottom: 0 }}>
              <label>Paciente</label>
              <select value={form.patientId} onChange={e => { setForm({...form, patientId: e.target.value}); setFormErrors(prev => ({ ...prev, patientId: undefined })); }}
                disabled={isReadonly || isLocked}>
                <option value="">Selecione...</option>
                {patients.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
              {formErrors.patientId && <span className="form-error">{formErrors.patientId}</span>}
            </div>
            <div className={`form-group required${formErrors.professionalId ? ' error' : ''}`} style={{ marginBottom: 0 }}>
              <label>Profissional</label>
              <select value={form.professionalId} onChange={e => { setForm({...form, professionalId: e.target.value}); setFormErrors(prev => ({ ...prev, professionalId: undefined })); }}
                disabled={isReadonly || isLocked}>
                <option value="">Selecione...</option>
                {professionals.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
              {formErrors.professionalId && <span className="form-error">{formErrors.professionalId}</span>}
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>Tipo</label>
              <select value={form.type} onChange={e => setForm({...form, type: e.target.value})}
                disabled={isReadonly || isLocked}>
                <option value="CONSULTA">Consulta</option>
                <option value="RETORNO">Retorno</option>
                <option value="AVALIACAO">Avaliação</option>
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>Status</label>
              <select value={form.status} onChange={e => setForm({...form, status: e.target.value})}
                disabled={isReadonly || isLocked}>
                <option value="AGENDADA">Agendada</option>
                <option value="EM_ANDAMENTO">Em Andamento</option>
                <option value="CONCLUIDA">Concluida</option>
                <option value="CANCELADA">Cancelada</option>
              </select>
            </div>
          </div>

          {(isLocked || isReadonly) && (
            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 8, flexShrink: 0, display: 'flex', alignItems: 'center', gap: 6 }}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
              {isReadonly ? 'Consulta concluída — somente leitura' : 'Origem da recepção — campos de identificação bloqueados'}
            </div>
          )}

          <div className="tabs" style={{ flexShrink: 0 }}>
            {formTabs.map(tab => (
              <button key={tab.id} className={`tab ${formTab === tab.id ? 'active' : ''}`}
                onClick={() => setFormTab(tab.id)}>
                {tab.label}
              </button>
            ))}
          </div>

          <div className="form-body">
            {formTab === 'queixa' && (
              <div className="form-section">
                <div className="form-section-title">Queixa Principal e Anamnese</div>
                <div className="form-group">
                  <label>Queixa Principal</label>
                  <textarea rows={4} value={form.chiefComplaint} onChange={e => setForm({...form, chiefComplaint: e.target.value})} disabled={isReadonly}
                    placeholder="Descreva a queixa principal do paciente..." />
                </div>
                <div className="form-group">
                  <label>Anamnese</label>
                  <textarea rows={6} value={form.anamnesis} onChange={e => setForm({...form, anamnesis: e.target.value})} disabled={isReadonly}
                    placeholder="Histórico detalhado, queixa, início, evolução..." />
                </div>
              </div>
            )}

            {formTab === 'exame' && (
              <div className="form-section">
                <div className="form-section-title">Histórico Clinico e Exame Fisico</div>
                <div className="form-group">
                  <label>Histórico Clinico</label>
                  <textarea rows={5} value={form.clinicalHistory} onChange={e => setForm({...form, clinicalHistory: e.target.value})} disabled={isReadonly}
                    placeholder="Histórico médico, cirurgias, medicações..." />
                </div>
                <div className="form-group">
                  <label>Exame Fisico / Fonoaudiologico</label>
                  <textarea rows={6} value={form.physicalExam} onChange={e => setForm({...form, physicalExam: e.target.value})} disabled={isReadonly}
                    placeholder="Resultado do exame clinico e fonoaudiologico..." />
                </div>
              </div>
            )}

            {formTab === 'diagnóstico' && (
              <div className="form-section">
                <div className="form-section-title">Diagnóstico e Conduta</div>
                <div className="form-group">
                  <label>Diagnóstico</label>
                  <textarea rows={4} value={form.diagnosis} onChange={e => setForm({...form, diagnosis: e.target.value})} disabled={isReadonly}
                    placeholder="Diagnóstico clinico..." />
                </div>
                <div className="form-group">
                  <label>Conduta / Plano de Tratamento</label>
                  <textarea rows={5} value={form.conduct} onChange={e => setForm({...form, conduct: e.target.value})} disabled={isReadonly}
                    placeholder="Plano de tratamento, encaminhamentos..." />
                </div>
                <div className="form-group">
                  <label>Observações</label>
                  <textarea rows={2} value={form.observations} onChange={e => setForm({...form, observations: e.target.value})} disabled={isReadonly}
                    placeholder="Observações gerais..." />
                </div>
              </div>
            )}

            {formTab === 'audiograma' && (
              <div className="form-section" style={{ marginBottom: 0 }}>
                <div className="form-section-title" style={{ marginBottom: 8, paddingBottom: 6 }}>Audiograma — Clique no grafico para marcar</div>
                <div className="form-row cols-3" style={{ marginBottom: 6 }}>
                  <div className="form-group">
                    <label>Tipo de Perda Auditiva</label>
                    <select value={audiogramData.hearingLossType} onChange={e => updateAudiogramField('hearingLossType', e.target.value)}>
                      <option value="NORMAL">Normal</option>
                      <option value="CONDUTIVA">Condutiva</option>
                      <option value="NEUROSENSORIAL">Neurosensorial</option>
                      <option value="MISTA">Mista</option>
                    </select>
                  </div>
                </div>

                <AudiogramChart data={audiogramData} onChange={updateAudiogramField} ear={ear} disabled={isReadonly}
                  observations={audiogramData.observations} onObservationsChange={(v) => updateAudiogramField('observations', v)} />
              </div>
            )}
          </div>

          {!isReadonly && (
            <div className="form-footer">
              <button className="btn btn-secondary" onClick={() => { setView('list'); setSelectedConsultation(null); loadData(); }}>
                Cancelar
              </button>
              <button className="btn btn-primary" onClick={handleSaveConsultation}>
                {editingConsultation?.id ? 'Atualizar Consulta' : 'Criar Consulta'}
              </button>
            </div>
          )}
        </div>
      </div>

      {showPostSaveModal && (
        <div className="modal-overlay" onClick={() => { setShowPostSaveModal(false); setView('list'); setSelectedConsultation(null); }}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Consulta salva com sucesso!</h3>
              <button className="modal-close" onClick={() => { setShowPostSaveModal(false); setView('list'); setSelectedConsultation(null); }}>&times;</button>
            </div>
            <div className="modal-body" style={{ textAlign: 'center', padding: '24px 32px' }}>
              <p style={{ marginBottom: 20, color: 'var(--text-secondary)' }}>Deseja imprimir a ficha de atendimento?</p>
              <div style={{ display: 'flex', gap: 10, justifyContent: 'center' }}>
                <button className="btn btn-primary" onClick={async () => {
                  setShowPostSaveModal(false);
                  setView('list');
                  setSelectedConsultation(null);
                  await openReportModal(savedConsultationId);
                }}>
                  Imprimir Ficha
                </button>
                <button className="btn btn-secondary" onClick={() => { setShowPostSaveModal(false); setView('list'); setSelectedConsultation(null); }}>
                  Fechar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
      {printModalHtml && <PrintModal html={printModalHtml} onClose={() => setPrintModalHtml(null)} />}
    </>
  );
  }

  return (
    <div className="page-full">
      <div className="page-header">
        <h1>Consultas</h1>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          {loaded && (
            <button className="btn btn-primary" onClick={handleNewConsultation}>+ Nova Consulta</button>
          )}
        </div>
      </div>

      <div className="page-body">
        <div style={{ display: 'flex', gap: 12, marginBottom: 14, flexShrink: 0, alignItems: 'center' }}>
          <input type="date" value={selectedDate} onChange={e => { setSelectedDate(e.target.value); setConsultPage(0); }}
            style={{ padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', width: 150 }} />
          <select value={selectedUnit} onChange={e => { setSelectedUnit(e.target.value); setConsultPage(0); }}
            style={{ padding: '8px 10px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', background: 'white', width: 200 }}>
            <option value="">Selecione a Unidade...</option>
            {units.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
          </select>
          <button className="btn btn-primary" onClick={() => { loadData(); }} disabled={!selectedUnit}>
            Carregar
          </button>
          <div style={{ flex: 1 }}>
            <input placeholder="Filtrar por paciente, profissional, tipo ou status..." value={searchFilter}
              onChange={e => { setSearchFilter(e.target.value); setConsultPage(0); }}
              style={{ width: '100%', padding: '10px 14px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 14, fontFamily: 'inherit' }} />
          </div>
          <select value={statusFilter} onChange={e => { setStatusFilter(e.target.value); setConsultPage(0); }}
            style={{ padding: '8px 10px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', background: 'white', width: 140 }}>
            <option value="">Todos Status</option>
            <option value="AGENDADA">Agendada</option>
            <option value="EM_ANDAMENTO">Em Andamento</option>
            <option value="CONCLUIDA">Concluida</option>
            <option value="CANCELADA">Cancelada</option>
          </select>
          <select value={typeFilter} onChange={e => { setTypeFilter(e.target.value); setConsultPage(0); }}
            style={{ padding: '8px 10px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', background: 'white', width: 130 }}>
            <option value="">Todos Tipos</option>
            <option value="CONSULTA">Consulta</option>
            <option value="RETORNO">Retorno</option>
            <option value="AVALIACAO">Avaliação</option>
          </select>
          {loaded && (
            <span style={{ fontSize: 12, color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
              {filteredConsultations.length} consulta(s)
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
            <h3>Consultas</h3>
            {readyPatients.length > 0 && (
              <span className="badge badge-info">{readyPatients.length} na recepção</span>
            )}
          </div>
          <div className="card-body" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
            <div className="scroll-container" style={{ flex: 1 }}>
              <table>
                <thead>
                  <tr><th>Data</th><th>Unidade</th><th>Paciente</th><th>Profissional</th><th>Tipo</th><th>Status</th><th>Origem</th><th>Ações</th></tr>
                </thead>
                <tbody>
                  {filteredConsultations.slice(consultPage * PAGE_SIZE, (consultPage + 1) * PAGE_SIZE).map(c => (
                    <tr key={c.id}>
                      <td style={{ whiteSpace: 'nowrap' }}>{new Date(c.createdAt).toLocaleString('pt-BR')}</td>
                      <td>{c.unit?.name || '-'}</td>
                      <td>{c.patient?.name}</td>
                      <td>{c.professional?.name}</td>
                      <td>{c.type}</td>
                      <td>{statusBadge(c.status)}</td>
                      <td>{isFromReception(c) ? <span className="badge badge-success">Recepção</span> : <span className="badge badge-secondary">Direto</span>}</td>
                      <td>
                        <div style={{ display: 'flex', gap: 4 }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => openReportModal(c.id)}>Imprimir</button>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleEditConsultation(c)}>Editar</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                  {filteredConsultations.length === 0 && (
                    <tr><td colSpan={8} className="empty-state" style={{ padding: 40 }}>Nenhuma consulta encontrada</td></tr>
                  )}
                </tbody>
              </table>
            </div>
            <Pagination page={consultPage} totalPages={Math.ceil(filteredConsultations.length / PAGE_SIZE)} onPageChange={setConsultPage} />
          </div>
        </div>

        <div className="card" style={{ flexShrink: 0, marginTop: 4 }}>
          <div className="card-header">
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <h3 style={{ margin: 0 }}>Pacientes na Recepção</h3>
              <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>
                Atualizado {lastRefresh.toLocaleTimeString('pt-BR')}
              </span>
              <button className="btn btn-secondary" style={{ fontSize: 12, padding: '4px 10px' }} onClick={loadReadyPatients}>
                Atualizar
              </button>
            </div>
          </div>
          <div className="card-body" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: 8, padding: '8px 16px' }}>
            {readyPatients.length === 0 ? (
              <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '16px 0', color: 'var(--text-muted)', fontSize: 13 }}>
                Nenhum paciente aguardando na data selecionada
              </div>
            ) : readyPatients.map(r => {
              const isAppt = r.type === 'APPOINTMENT';
              const statusText = isAppt ? (r.status || 'AGENDADO') : 'RECEPCIONADO';
              const borderColor = isAppt
                ? (r.status === 'RECEPCIONADO' ? 'var(--teal, #0d9488)' : 'var(--primary)')
                : 'var(--teal, #0d9488)';
              return (
                <div key={r.id} className="ready-card" style={{ marginBottom: 0, borderLeft: `3px solid ${borderColor}`, position: 'relative' }}>
                  <div className="ready-info" style={{ gap: 6, display: 'flex', flexDirection: 'column' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <div className="ready-name">{r.patient?.name}</div>
                      <span className={`badge ${isAppt ? (r.status === 'RECEPCIONADO' ? 'badge-success' : 'badge-info') : 'badge-teal'}`} style={{ fontSize: 10, padding: '1px 7px', whiteSpace: 'nowrap' }}>
                        {statusLabel(statusText)}
                      </span>
                    </div>
                    <div className="ready-detail">
                      {isAppt
                        ? <>{r.time} — {r.professional?.name || 'Sem profissional'}</>
                        : <>{r.patient?.cpf ? `CPF: ${r.patient.cpf}` : ''}{r.createdAt ? ` — Check-in: ${new Date(r.createdAt).toLocaleTimeString('pt-BR')}` : ''}</>
                      }
                    </div>
                  </div>
                  <button className="btn btn-primary btn-sm" onClick={() => handleStartFromReady(r)}>
                    Iniciar
                  </button>
                </div>
              );
            })}
          </div>
        </div>
        </>
        )}
      </div>
      {printModalHtml && <PrintModal html={printModalHtml} onClose={() => setPrintModalHtml(null)} />}
    </div>
  );
}
