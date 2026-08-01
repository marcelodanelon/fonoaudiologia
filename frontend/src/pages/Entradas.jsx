import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useToast } from '../context/ToastContext';
import Pagination from '../components/Pagination';

const PAGE_SIZE = 8;
const today = () => new Date().toISOString().split('T')[0];
const newItem = () => ({ supplyId: '', quantity: '' });

const formatQty = (q) => {
  if (q === null || q === undefined || q === '') return '-';
  const n = Number(q);
  return Number.isInteger(n) ? String(n) : String(parseFloat(n.toFixed(2)));
};

export default function Entradas() {
  const toast = useToast();

  const [units, setUnits] = useState([]);
  const [supplies, setSupplies] = useState([]);
  const [entries, setEntries] = useState([]);
  const [entryUnitFilter, setEntryUnitFilter] = useState('');
  const [entryPage, setEntryPage] = useState(0);
  const [entryFormOpen, setEntryFormOpen] = useState(false);
  const [entryForm, setEntryForm] = useState({ unitId: '', entryDate: today(), supplier: '', reference: '', notes: '', items: [newItem()] });
  const [entryErrors, setEntryErrors] = useState({});
  const [editingEntry, setEditingEntry] = useState(null);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const [unitsRes, supRes, entRes] = await Promise.all([
        api.get('/service-units'),
        api.get('/supplies'),
        api.get('/supply-entries'),
      ]);
      setUnits(unitsRes.data);
      setSupplies(supRes.data);
      setEntries(entRes.data);
    } catch { toast.error('Erro ao carregar entradas'); }
  };

  const activeSupplies = supplies.filter(s => s.active);

  const filteredEntries = entryUnitFilter ? entries.filter(e => String(e.unit?.id) === String(entryUnitFilter)) : entries;
  const entryPages = Math.ceil(filteredEntries.length / PAGE_SIZE);
  const entryList = filteredEntries.slice(entryPage * PAGE_SIZE, (entryPage + 1) * PAGE_SIZE);

  const openNewEntry = () => {
    setEditingEntry(null);
    setEntryForm({ unitId: '', entryDate: today(), supplier: '', reference: '', notes: '', items: [newItem()] });
    setEntryErrors({});
    setEntryFormOpen(true);
  };

  const openEditEntry = (entry) => {
    setEditingEntry(entry);
    setEntryForm({
      unitId: String(entry.unit?.id || ''),
      entryDate: entry.entryDate,
      supplier: entry.supplier || '',
      reference: entry.reference || '',
      notes: entry.notes || '',
      items: entry.items?.map(it => ({ supplyId: String(it.supply?.id || ''), quantity: String(it.quantity) })) || [newItem()],
    });
    setEntryErrors({});
    setEntryFormOpen(true);
  };

  const updateEntryItem = (idx, field, value) => {
    setEntryForm(prev => {
      const items = prev.items.map((it, i) => i === idx ? { ...it, [field]: value } : it);
      return { ...prev, items };
    });
  };

  const addEntryItem = () => setEntryForm(prev => ({ ...prev, items: [...prev.items, newItem()] }));
  const removeEntryItem = (idx) => setEntryForm(prev => ({ ...prev, items: prev.items.filter((_, i) => i !== idx) }));

  const saveEntry = async () => {
    const errors = {};
    if (!entryForm.unitId) errors.unitId = 'Selecione a unidade';
    const validItems = entryForm.items.filter(i => i.supplyId && i.quantity !== '' && Number(i.quantity) > 0);
    if (validItems.length === 0) errors.items = 'Adicione ao menos um insumo com quantidade';
    setEntryErrors(errors);
    if (Object.keys(errors).length > 0) return;
    const payload = {
      unitId: Number(entryForm.unitId),
      entryDate: entryForm.entryDate,
      supplier: entryForm.supplier,
      reference: entryForm.reference,
      notes: entryForm.notes,
      items: validItems.map(i => ({ supplyId: Number(i.supplyId), quantity: Number(i.quantity) })),
    };
    try {
      if (editingEntry) {
        await api.put(`/supply-entries/${editingEntry.id}`, payload);
        toast.success('Entrada atualizada!');
      } else {
        await api.post('/supply-entries', payload);
        toast.success('Entrada registrada!');
      }
      setEntryFormOpen(false);
      setEditingEntry(null);
      loadData();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao salvar'); }
  };

  return (
    <div className="page-full">
      <div className="page-header">
        <h1>Entradas de Insumos</h1>
      </div>

      <div className="page-body" style={{ overflow: 'visible' }}>
        <div style={{ display: 'flex', gap: 12, marginBottom: 14, alignItems: 'center', flexShrink: 0 }}>
          <select value={entryUnitFilter} onChange={e => { setEntryUnitFilter(e.target.value); setEntryPage(0); }}
            style={{ padding: '8px 10px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', background: 'white', width: 200 }}>
            <option value="">Todas Unidades</option>
            {units.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
          </select>
          <div style={{ flex: 1 }} />
          <button className="btn btn-primary" onClick={openNewEntry}>+ Nova Entrada</button>
        </div>
        <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <div className="card-body" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
            <div className="scroll-container" style={{ flex: 1 }}>
              <table>
                <thead>
                  <tr><th>Data</th><th>Unidade</th><th>Fornecedor</th><th>Referencia</th><th>Itens</th><th>Operador</th><th>Ações</th></tr>
                </thead>
                <tbody>
                  {entryList.map(e => (
                    <tr key={e.id}>
                      <td style={{ whiteSpace: 'nowrap' }}>{new Date(e.entryDate + 'T12:00:00').toLocaleDateString('pt-BR')}</td>
                      <td>{e.unit?.name || '-'}</td>
                      <td>{e.supplier || '-'}</td>
                      <td>{e.reference || '-'}</td>
                      <td>
                        <div style={{ fontSize: 12 }}>
                          {e.items?.map(it => (
                            <div key={it.id} style={{ whiteSpace: 'nowrap' }}>
                              {it.supply?.name} ({formatQty(it.quantity)} {it.supply?.unitMeasure || ''})
                            </div>
                          ))}
                        </div>
                      </td>
                      <td>{e.operator?.name || '-'}</td>
                      <td>
                        <div style={{ display: 'flex', gap: 4 }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => openEditEntry(e)}>Editar</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                  {entryList.length === 0 && <tr><td colSpan={7} className="empty-state" style={{ padding: 40 }}>Nenhuma entrada registrada</td></tr>}
                </tbody>
              </table>
            </div>
            <Pagination page={entryPage} totalPages={entryPages} onPageChange={setEntryPage} />
          </div>
        </div>
      </div>

      {entryFormOpen && (
        <div className="modal-overlay" onClick={() => setEntryFormOpen(false)}>
          <div className="modal modal-lg" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editingEntry ? 'Editar Entrada de Insumos' : 'Nova Entrada de Insumos'}</h3>
              <button className="modal-close" onClick={() => setEntryFormOpen(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <div className="form-grid">
                <div className="form-group required">
                  <label>Unidade de Atendimento</label>
                  <select value={entryForm.unitId} disabled={Boolean(editingEntry)} onChange={e => setEntryForm({ ...entryForm, unitId: e.target.value })}>
                    <option value="">Selecione...</option>
                    {units.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
                  </select>
                  {entryErrors.unitId && <span className="form-error">{entryErrors.unitId}</span>}
                </div>
                <div className="form-group required">
                  <label>Data</label>
                  <input type="date" value={entryForm.entryDate} onChange={e => setEntryForm({ ...entryForm, entryDate: e.target.value })} />
                </div>
                <div className="form-group">
                  <label>Fornecedor</label>
                  <input value={entryForm.supplier} onChange={e => setEntryForm({ ...entryForm, supplier: e.target.value })} placeholder="Opcional" />
                </div>
                <div className="form-group">
                  <label>Referencia</label>
                  <input value={entryForm.reference} onChange={e => setEntryForm({ ...entryForm, reference: e.target.value })} placeholder="Nota fiscal, lote..." />
                </div>
                <div className="form-group full-width">
                  <label>Observações</label>
                  <input value={entryForm.notes} onChange={e => setEntryForm({ ...entryForm, notes: e.target.value })} placeholder="Opcional" />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '10px 0 8px' }}>
                <label style={{ fontSize: 13, fontWeight: 600 }}>Insumos da Entrada</label>
                <button className="btn btn-secondary btn-sm" onClick={addEntryItem}>+ Adicionar Insumo</button>
              </div>
              {entryErrors.items && <div className="form-error" style={{ marginBottom: 8 }}>{entryErrors.items}</div>}
              <div style={{ maxHeight: 260, overflowY: 'auto', padding: 2 }}>
                {entryForm.items.length > 0 && (
                  <div className="form-row" style={{ gridTemplateColumns: '1fr 150px 40px', marginBottom: 4, alignItems: 'center' }}>
                    <span style={{ fontSize: 11, fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>Insumo</span>
                    <span style={{ fontSize: 11, fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>Quantidade</span>
                    <span></span>
                  </div>
                )}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {entryForm.items.map((item, idx) => (
                    <div key={idx} className="form-row" style={{ gridTemplateColumns: '1fr 150px 40px', marginBottom: 0, alignItems: 'center' }}>
                      <div className="form-group" style={{ marginBottom: 0 }}>
                        <select value={item.supplyId} onChange={e => updateEntryItem(idx, 'supplyId', e.target.value)}>
                          <option value="">Selecione o insumo...</option>
                          {activeSupplies.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                        </select>
                      </div>
                      <div className="form-group" style={{ marginBottom: 0 }}>
                        <input type="number" step="0.01" min="0" placeholder="Qtd" value={item.quantity}
                          onChange={e => updateEntryItem(idx, 'quantity', e.target.value)} />
                      </div>
                      <div className="form-group" style={{ marginBottom: 0, textAlign: 'center' }}>
                        <button className="btn btn-secondary btn-sm" style={{ color: 'var(--danger)' }} onClick={() => removeEntryItem(idx)} title="Remover">&times;</button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setEntryFormOpen(false)}>Cancelar</button>
              <button className="btn btn-primary" onClick={saveEntry}>{editingEntry ? 'Salvar Alterações' : 'Registrar Entrada'}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
