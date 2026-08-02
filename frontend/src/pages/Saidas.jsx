import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useToast } from '../context/ToastContext';
import { useConfirm } from '../context/ConfirmContext';
import Pagination from '../components/Pagination';

const PAGE_SIZE = 8;
const today = () => new Date().toISOString().split('T')[0];
const newItem = () => ({ supplyId: '', quantity: '' });

const formatQty = (q) => {
  if (q === null || q === undefined || q === '') return '-';
  const n = Number(q);
  return Number.isInteger(n) ? String(n) : String(parseFloat(n.toFixed(2)));
};

export default function Saídas() {
  const toast = useToast();
  const confirm = useConfirm();

  const [units, setUnits] = useState([]);
  const [supplies, setSupplies] = useState([]);
  const [patients, setPatients] = useState([]);
  const [exits, setExits] = useState([]);
  const [exitUnitFilter, setExitUnitFilter] = useState('');
  const [exitPage, setExitPage] = useState(0);
  const [exitFormOpen, setExitFormOpen] = useState(false);
  const [exitForm, setExitForm] = useState({ unitId: '', exitDate: today(), patientId: '', notes: '', items: [newItem()] });
  const [exitErrors, setExitErrors] = useState({});
  const [unitStocks, setUnitStocks] = useState([]);
  const [patientSearch, setPatientSearch] = useState('');
  const [patientFocus, setPatientFocus] = useState(false);
  const [editingExit, setEditingExit] = useState(null);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const [unitsRes, supRes, extRes, patRes] = await Promise.all([
        api.get('/service-units'),
        api.get('/supplies'),
        api.get('/supply-exits'),
        api.get('/patients'),
      ]);
      setUnits(unitsRes.data);
      setSupplies(supRes.data);
      setExits(extRes.data);
      setPatients(patRes.data);
    } catch { toast.error('Erro ao carregar saídas'); }
  };

  const activeSupplies = supplies.filter(s => s.active);

  const filteredExits = exitUnitFilter ? exits.filter(e => String(e.unit?.id) === String(exitUnitFilter)) : exits;
  const exitPages = Math.ceil(filteredExits.length / PAGE_SIZE);
  const exitList = filteredExits.slice(exitPage * PAGE_SIZE, (exitPage + 1) * PAGE_SIZE);

  const openNewExit = async () => {
    setEditingExit(null);
    setExitForm({ unitId: '', exitDate: today(), patientId: '', notes: '', items: [newItem()] });
    setExitErrors({});
    setUnitStocks([]);
    setPatientSearch('');
    setExitFormOpen(true);
  };

  const openEditExit = async (exit) => {
    setEditingExit(exit);
    setExitForm({
      unitId: String(exit.unit?.id || ''),
      exitDate: exit.exitDate,
      patientId: exit.patient?.id ? String(exit.patient.id) : '',
      notes: exit.notes || '',
      items: exit.items?.map(it => ({ supplyId: String(it.supply?.id || ''), quantity: String(it.quantity) })) || [newItem()],
    });
    setPatientSearch(exit.patient ? `${exit.patient.name}${exit.patient.cpf ? ` (${exit.patient.cpf})` : ''}` : '');
    setExitErrors({});
    setUnitStocks([]);
    setExitFormOpen(true);
    if (exit.unit?.id) {
      try {
        const res = await api.get('/supplies/stock', { params: { unitId: exit.unit.id } });
        setUnitStocks(res.data);
      } catch { toast.error('Erro ao carregar saldos'); }
    }
  };

  const setExitUnit = async (unitId) => {
    setExitForm(prev => ({ ...prev, unitId, items: [newItem()] }));
    setUnitStocks([]);
    if (!unitId) return;
    try {
      const res = await api.get('/supplies/stock', { params: { unitId } });
      setUnitStocks(res.data);
    } catch { toast.error('Erro ao carregar saldos'); }
  };

  const stockBySupplyId = {};
  unitStocks.forEach(s => { stockBySupplyId[s.supply?.id] = s; });

  const originalQtyBySupply = {};
  if (editingExit?.items) {
    editingExit.items.forEach(it => {
      const sid = it.supply?.id;
      if (sid != null) originalQtyBySupply[sid] = (originalQtyBySupply[sid] || 0) + Number(it.quantity || 0);
    });
  }

  const updateExitItem = (idx, field, value) => {
    setExitForm(prev => {
      const items = prev.items.map((it, i) => i === idx ? { ...it, [field]: value } : it);
      return { ...prev, items };
    });
  };

  const addExitItem = () => setExitForm(prev => ({ ...prev, items: [...prev.items, newItem()] }));
  const removeExitItem = (idx) => setExitForm(prev => ({ ...prev, items: prev.items.filter((_, i) => i !== idx) }));

  const saveExit = async () => {
    const errors = {};
    if (!exitForm.unitId) errors.unitId = 'Selecione a unidade';
    const validItems = exitForm.items.filter(i => i.supplyId && i.quantity !== '' && Number(i.quantity) > 0);
    if (validItems.length === 0) errors.items = 'Adicione ao menos um insumo com quantidade';
    else {
      for (const i of validItems) {
        const stock = stockBySupplyId[Number(i.supplyId)];
        if (!stock || Number(stock.quantity) < Number(i.quantity)) {
          errors.items = 'Saldo insuficiente para um dos insumos';
          break;
        }
      }
    }
    setExitErrors(errors);
    if (Object.keys(errors).length > 0) return;
    const payload = {
      unitId: Number(exitForm.unitId),
      exitDate: exitForm.exitDate,
      patientId: exitForm.patientId ? Number(exitForm.patientId) : null,
      notes: exitForm.notes,
      items: validItems.map(i => ({ supplyId: Number(i.supplyId), quantity: Number(i.quantity) })),
    };
    try {
      if (editingExit) {
        await api.put(`/supply-exits/${editingExit.id}`, payload);
        toast.success('Saída atualizada!');
      } else {
        await api.post('/supply-exits', payload);
        toast.success('Saída registrada!');
      }
      setExitFormOpen(false);
      setEditingExit(null);
      loadData();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao salvar'); }
  };

  const handleDeleteExit = async (id) => {
    const ok = await confirm('Excluir esta saída? O saldo dos insumos será devolvido ao estoque.', 'Excluir Saída');
    if (!ok) return;
    try {
      await api.delete(`/supply-exits/${id}`);
      toast.success('Saída excluida!');
      loadData();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao excluir'); }
  };

  const filteredPatients = patients.filter(p => {
    const s = patientSearch.toLowerCase();
    return p.name.toLowerCase().includes(s) || (p.cpf || '').includes(patientSearch);
  }).slice(0, 60);

  const supplyName = (id) => activeSupplies.find(s => String(s.id) === String(id))?.name || 'Insumo';
  const supplyMeasure = (id) => activeSupplies.find(s => String(s.id) === String(id))?.unitMeasure || '';

  return (
    <div className="page-full">
      <div className="page-header">
        <h1>Saídas de Insumos</h1>
      </div>

      <div className="page-body" style={{ overflow: 'visible' }}>
        <div style={{ display: 'flex', gap: 12, marginBottom: 14, alignItems: 'center', flexShrink: 0 }}>
          <select value={exitUnitFilter} onChange={e => { setExitUnitFilter(e.target.value); setExitPage(0); }}
            style={{ padding: '8px 10px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', background: 'white', width: 200 }}>
            <option value="">Todas Unidades</option>
            {units.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
          </select>
          <div style={{ flex: 1 }} />
          <button className="btn btn-primary" onClick={openNewExit}>+ Nova Saída</button>
        </div>
        <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <div className="card-body" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
            <div className="scroll-container" style={{ flex: 1 }}>
              <table>
                <thead>
                  <tr><th>Data</th><th>Unidade</th><th>Paciente</th><th>Itens</th><th>Observações</th><th>Ações</th></tr>
                </thead>
                <tbody>
                  {exitList.map(e => (
                    <tr key={e.id}>
                      <td style={{ whiteSpace: 'nowrap' }}>{new Date(e.exitDate + 'T12:00:00').toLocaleDateString('pt-BR')}</td>
                      <td>{e.unit?.name || '-'}</td>
                      <td>{e.patient?.name || '-'}</td>
                      <td>
                        <div style={{ fontSize: 12 }}>
                          {e.items?.map(it => (
                            <div key={it.id} style={{ whiteSpace: 'nowrap' }}>
                              {it.supply?.name} ({formatQty(it.quantity)} {it.supply?.unitMeasure || ''})
                            </div>
                          ))}
                        </div>
                      </td>
                      <td>{e.notes || '-'}</td>
                      <td>
                        <div style={{ display: 'flex', gap: 4 }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => openEditExit(e)}>Editar</button>
                          <button className="btn btn-secondary btn-sm" style={{ color: 'var(--danger)' }} onClick={() => handleDeleteExit(e.id)}>Excluir</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                  {exitList.length === 0 && <tr><td colSpan={6} className="empty-state" style={{ padding: 40 }}>Nenhuma saída registrada</td></tr>}
                </tbody>
              </table>
            </div>
            <Pagination page={exitPage} totalPages={exitPages} onPageChange={setExitPage} />
          </div>
        </div>
      </div>

      {exitFormOpen && (
        <div className="modal-overlay">
          <div className="modal modal-lg" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editingExit ? 'Editar Saída de Insumos' : 'Nova Saída de Insumos'}</h3>
              <button className="modal-close" onClick={() => setExitFormOpen(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <div className="form-grid compact">
                <div className="form-group required">
                  <label>Unidade de Atendimento</label>
                  <select value={exitForm.unitId} disabled={Boolean(editingExit)} onChange={e => setExitUnit(e.target.value)}>
                    <option value="">Selecione...</option>
                    {units.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
                  </select>
                  {exitErrors.unitId && <span className="form-error">{exitErrors.unitId}</span>}
                </div>
                <div className="form-group required">
                  <label>Data</label>
                  <input type="date" value={exitForm.exitDate} onChange={e => setExitForm({ ...exitForm, exitDate: e.target.value })} />
                </div>
                <div className="form-group full-width">
                  <label>Paciente (opcional)</label>
                  <div className="autocomplete">
                    <div className="autocomplete-input-wrap">
                      <input
                        placeholder={exitForm.patientId ? 'Paciente selecionado' : 'Buscar paciente...'}
                        value={patientSearch}
                        readOnly={Boolean(exitForm.patientId)}
                        onFocus={() => setPatientFocus(true)}
                        onBlur={() => setPatientFocus(false)}
                        onChange={e => {
                          setPatientSearch(e.target.value);
                          if (exitForm.patientId) setExitForm({ ...exitForm, patientId: '' });
                        }}
                      />
                      {exitForm.patientId && (
                        <button type="button" className="autocomplete-clear" title="Trocar paciente"
                          onClick={() => { setExitForm({ ...exitForm, patientId: '' }); setPatientSearch(''); }}>
                          &times;
                        </button>
                      )}
                    </div>
                    {patientFocus && !exitForm.patientId && patientSearch.trim() !== '' && (
                      <div className="autocomplete-list">
                        {filteredPatients.length === 0
                          ? <div className="autocomplete-empty">Nenhum paciente encontrado</div>
                          : filteredPatients.map(p => (
                              <div
                                key={p.id}
                                className="autocomplete-item"
                                onMouseDown={e => e.preventDefault()}
                                onClick={() => {
                                  setExitForm({ ...exitForm, patientId: String(p.id) });
                                  setPatientSearch(p.name + (p.cpf ? ` (${p.cpf})` : ''));
                                  setPatientFocus(false);
                                }}
                              >
                                {p.name}{p.cpf ? ` (${p.cpf})` : ''}
                              </div>
                            ))}
                      </div>
                    )}
                  </div>
                </div>
                <div className="form-group full-width">
                  <label>Observações</label>
                  <input value={exitForm.notes} onChange={e => setExitForm({ ...exitForm, notes: e.target.value })} placeholder="Opcional" />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '10px 0 8px' }}>
                <label style={{ fontSize: 13, fontWeight: 600 }}>Insumos da Saída</label>
                <button className="btn btn-secondary btn-sm" onClick={addExitItem}>+ Adicionar Insumo</button>
              </div>
              {exitErrors.items && <div className="form-error" style={{ marginBottom: 8 }}>{exitErrors.items}</div>}
              <div style={{ maxHeight: 260, overflowY: 'auto', padding: 2 }}>
                {exitForm.items.length > 0 && (
                  <div className="form-row" style={{ gridTemplateColumns: '1fr 150px 1fr 40px', marginBottom: 4, alignItems: 'center' }}>
                    <span style={{ fontSize: 11, fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>Insumo</span>
                    <span style={{ fontSize: 11, fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>Quantidade</span>
                    <span style={{ fontSize: 11, fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>Saldo</span>
                    <span></span>
                  </div>
                )}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {exitForm.items.map((item, idx) => {
                    const stock = item.supplyId ? stockBySupplyId[Number(item.supplyId)] : null;
                    const available = stock ? stock.quantity + (originalQtyBySupply[Number(item.supplyId)] || 0) : null;
                    const over = available != null && item.quantity !== '' && Number(item.quantity) > available;
                    return (
                      <div key={idx} className="form-row" style={{ gridTemplateColumns: '1fr 150px 1fr 40px', marginBottom: 0, alignItems: 'center' }}>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                          <select value={item.supplyId} onChange={e => updateExitItem(idx, 'supplyId', e.target.value)}>
                            <option value="">Selecione o insumo...</option>
                            {activeSupplies.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                          </select>
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                          <input type="number" step="0.01" min="0" placeholder="Qtd" value={item.quantity}
                            onChange={e => updateExitItem(idx, 'quantity', e.target.value)} />
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                          <span style={{ fontSize: 12, color: over ? 'var(--danger)' : 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                            {available != null ? `Saldo: ${formatQty(available)} ${supplyMeasure(item.supplyId)}` : 'Saldo: -'}
                            {over && ' — insuficiente'}
                          </span>
                        </div>
                        <div className="form-group" style={{ marginBottom: 0, textAlign: 'center' }}>
                          <button className="btn btn-secondary btn-sm" style={{ color: 'var(--danger)' }} onClick={() => removeExitItem(idx)} title="Remover">&times;</button>
                        </div>
                      </div>
                    );
                  })}
                  {!exitForm.unitId && (
                    <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Selecione a unidade para visualizar os saldos disponíveis.</div>
                  )}
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setExitFormOpen(false)}>Cancelar</button>
              <button className="btn btn-primary" onClick={saveExit}>{editingExit ? 'Salvar Alterações' : 'Registrar Saída'}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
