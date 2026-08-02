import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useToast } from '../context/ToastContext';
import { useConfirm } from '../context/ConfirmContext';

const unitMeasures = ['UN', 'CX', 'PC', 'FR', 'ML', 'L', 'KG', 'G', 'MG'];

const emptySupplyForm = { name: '', description: '', unitMeasure: 'UN', category: '', minimumQuantity: '' };

const formatQty = (q) => {
  if (q === null || q === undefined || q === '') return '-';
  const n = Number(q);
  return Number.isInteger(n) ? String(n) : String(parseFloat(n.toFixed(2)));
};

export default function Insumos() {
  const toast = useToast();
  const confirm = useConfirm();

  const [units, setUnits] = useState([]);
  const [supplies, setSupplies] = useState([]);
  const [supplyFormOpen, setSupplyFormOpen] = useState(false);
  const [editingSupply, setEditingSupply] = useState(null);
  const [supplyForm, setSupplyForm] = useState({ ...emptySupplyForm });
  const [supplyErrors, setSupplyErrors] = useState({});
  const [supplyTab, setSupplyTab] = useState('dados');
  const [supplyStocks, setSupplyStocks] = useState([]);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const [unitsRes, supRes] = await Promise.all([
        api.get('/service-units'),
        api.get('/supplies'),
      ]);
      setUnits(unitsRes.data);
      setSupplies(supRes.data);
    } catch { toast.error('Erro ao carregar insumos'); }
  };

  const openNewSupply = () => {
    setEditingSupply(null);
    setSupplyForm({ ...emptySupplyForm });
    setSupplyErrors({});
    setSupplyTab('dados');
    setSupplyStocks([]);
    setSupplyFormOpen(true);
  };

  const openEditSupply = (s) => {
    setEditingSupply(s);
    setSupplyForm({ name: s.name, description: s.description || '', unitMeasure: s.unitMeasure, category: s.category || '', minimumQuantity: s.minimumQuantity ?? '' });
    setSupplyErrors({});
    setSupplyTab('dados');
    setSupplyStocks([]);
    setSupplyFormOpen(true);
  };

  const openStockSupply = async (s) => {
    setEditingSupply(s);
    setSupplyForm({ name: s.name, description: s.description || '', unitMeasure: s.unitMeasure, category: s.category || '', minimumQuantity: s.minimumQuantity ?? '' });
    setSupplyErrors({});
    setSupplyTab('estoque');
    setSupplyStocks([]);
    setSupplyFormOpen(true);
    try {
      const res = await api.get(`/supplies/${s.id}/stocks`);
      setSupplyStocks(res.data);
    } catch { toast.error('Erro ao carregar estoque por unidade'); }
  };

  const saveSupply = async () => {
    const errors = {};
    if (!supplyForm.name.trim()) errors.name = 'Informe o nome do insumo';
    if (!supplyForm.unitMeasure) errors.unitMeasure = 'Informe a unidade de medida';
    setSupplyErrors(errors);
    if (Object.keys(errors).length > 0) return;
    const payload = {
      name: supplyForm.name,
      description: supplyForm.description,
      unitMeasure: supplyForm.unitMeasure,
      category: supplyForm.category,
      minimumQuantity: supplyForm.minimumQuantity === '' ? null : Number(supplyForm.minimumQuantity),
    };
    try {
      if (editingSupply) {
        await api.put(`/supplies/${editingSupply.id}`, payload);
        toast.success('Insumo atualizado!');
      } else {
        await api.post('/supplies', payload);
        toast.success('Insumo criado!');
      }
      setSupplyFormOpen(false);
      loadData();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao salvar'); }
  };

  const handleDeleteSupply = async (id) => {
    try {
      const res = await api.get(`/supplies/${id}/stocks`);
      const withStock = res.data.filter(s => Number(s.quantity) > 0);
      if (withStock.length > 0) {
        const first = withStock[0];
        toast.warning(`Não é possível desativar este insumo: há saldo de ${formatQty(first.quantity)} ${first.supply?.unitMeasure || ''} na unidade ${first.unit?.name || '-'}`);
        return;
      }
    } catch { /* prossegue para a confirmação abaixo */ }
    const ok = await confirm('Desativar este insumo?');
    if (!ok) return;
    try {
      await api.delete(`/supplies/${id}`);
      toast.success('Insumo desativado!');
      loadData();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro'); }
  };

  return (
    <div className="page-full">
      <div className="page-header">
        <h1>Insumos</h1>
      </div>

      <div className="page-body" style={{ overflow: 'visible' }}>
        <div style={{ display: 'flex', gap: 12, marginBottom: 14, alignItems: 'center', flexShrink: 0 }}>
          <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>{supplies.length} insumo(s)</span>
          <div style={{ flex: 1 }} />
          <button className="btn btn-primary" onClick={openNewSupply}>+ Novo Insumo</button>
        </div>
        <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <div className="card-body" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
            <div className="scroll-container" style={{ flex: 1 }}>
              <table>
                <thead>
                  <tr><th>Nome</th><th>Categoria</th><th>Un. Medida</th><th>Estoque Mín.</th><th>Situação</th><th>Ações</th></tr>
                </thead>
                <tbody>
                  {supplies.map(s => (
                    <tr key={s.id}>
                      <td><strong>{s.name}</strong>{s.description && <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{s.description}</div>}</td>
                      <td>{s.category || '-'}</td>
                      <td>{s.unitMeasure}</td>
                      <td>{s.minimumQuantity != null ? formatQty(s.minimumQuantity) : '-'}</td>
                      <td>
                        <span className={`badge ${s.active ? 'badge-success' : 'badge-danger'}`}>
                          {s.active ? 'Ativo' : 'Inativo'}
                        </span>
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: 4 }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => openStockSupply(s)}>Estoque</button>
                          <button className="btn btn-secondary btn-sm" onClick={() => openEditSupply(s)}>Editar</button>
                          {s.active && (
                            <button className="btn btn-secondary btn-sm" style={{ color: 'var(--danger)' }} onClick={() => handleDeleteSupply(s.id)}>Desativar</button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                  {supplies.length === 0 && <tr><td colSpan={6} className="empty-state" style={{ padding: 40 }}>Nenhum insumo cadastrado</td></tr>}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {supplyFormOpen && (
        <div className="modal-overlay">
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editingSupply ? 'Editar Insumo' : 'Novo Insumo'}</h3>
              <button className="modal-close" onClick={() => setSupplyFormOpen(false)}>&times;</button>
            </div>
            <div className="tabs" style={{ padding: '0 24px' }}>
              <button className={`tab ${supplyTab === 'dados' ? 'active' : ''}`} onClick={() => setSupplyTab('dados')}>Dados</button>
              <button className={`tab ${supplyTab === 'estoque' ? 'active' : ''}`} onClick={() => setSupplyTab('estoque')}>Estoque por Unidade</button>
            </div>
            <div className="modal-body" style={{ minHeight: 520, maxHeight: 520 }}>
              {supplyTab === 'dados' && (
                <div className="form-grid compact">
                  <div className="form-group full-width required">
                    <label>Nome</label>
                    <input value={supplyForm.name} onChange={e => setSupplyForm({ ...supplyForm, name: e.target.value })} placeholder="Ex.: Otoscópio descartável" />
                    {supplyErrors.name && <span className="form-error">{supplyErrors.name}</span>}
                  </div>
                  <div className="form-group full-width">
                    <label>Descrição</label>
                    <input value={supplyForm.description} onChange={e => setSupplyForm({ ...supplyForm, description: e.target.value })} placeholder="Descrição opcional" />
                  </div>
                  <div className="form-group required">
                    <label>Unidade de Medida</label>
                    <select value={supplyForm.unitMeasure} onChange={e => setSupplyForm({ ...supplyForm, unitMeasure: e.target.value })}>
                      {unitMeasures.map(m => <option key={m} value={m}>{m}</option>)}
                    </select>
                    {supplyErrors.unitMeasure && <span className="form-error">{supplyErrors.unitMeasure}</span>}
                  </div>
                  <div className="form-group">
                    <label>Categoria</label>
                    <input value={supplyForm.category} onChange={e => setSupplyForm({ ...supplyForm, category: e.target.value })} placeholder="Ex.: Descartáveis" />
                  </div>
                  <div className="form-group">
                    <label>Estoque Mínimo</label>
                    <input type="number" step="0.01" min="0" value={supplyForm.minimumQuantity} onChange={e => setSupplyForm({ ...supplyForm, minimumQuantity: e.target.value })} placeholder="Ex.: 10" />
                  </div>
                </div>
              )}
              {supplyTab === 'estoque' && (
                <div>
                  <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 10 }}>
                    Entradas, saídas e saldo de <strong>{editingSupply?.name || supplyForm.name}</strong> em cada unidade de atendimento.
                  </div>
                  <table>
                    <thead>
                      <tr><th>Unidade</th><th>Qtd. Inicial</th><th>Qtd. Utilizada</th><th>Quantidade</th><th>Situação</th></tr>
                    </thead>
                    <tbody>
                      {units.map(u => {
                        const st = supplyStocks.find(s => String(s.unit?.id) === String(u.id));
                        const qty = st ? st.quantity : 0;
                        const initial = st && st.initialQuantity != null ? st.initialQuantity : 0;
                        const used = st && st.usedQuantity != null ? st.usedQuantity : 0;
                        return (
                          <tr key={u.id}>
                            <td>{u.name}</td>
                            <td>{formatQty(initial)}</td>
                            <td>{formatQty(used)}</td>
                            <td><strong>{formatQty(qty)}</strong> {supplyForm.unitMeasure}</td>
                            <td>
                              {qty > 0 ? (
                                <span className="badge badge-success">COM SALDO</span>
                              ) : (
                                <span className="badge badge-danger">SEM SALDO</span>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                      {units.length === 0 && <tr><td colSpan={5} className="empty-state" style={{ padding: 24 }}>Nenhuma unidade cadastrada</td></tr>}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
            {supplyTab === 'dados' && (
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setSupplyFormOpen(false)}>Cancelar</button>
                <button className="btn btn-primary" onClick={saveSupply}>{editingSupply ? 'Atualizar' : 'Salvar'}</button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
