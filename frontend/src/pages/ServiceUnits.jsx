import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useToast } from '../context/ToastContext';
import { useConfirm } from '../context/ConfirmContext';

const emptyForm = { name: '', address: '', phone: '' };

export default function ServiceUnits() {
  const toast = useToast();
  const confirm = useConfirm();
  const [units, setUnits] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editUnit, setEditUnit] = useState(null);
  const [form, setForm] = useState({ ...emptyForm });

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const res = await api.get('/service-units', { params: { includeInactive: true } });
      setUnits(res.data);
    } catch { toast.error('Erro ao carregar unidades'); }
  };

  const handleSave = async () => {
    if (!form.name.trim()) {
      toast.warning('Informe o nome da unidade');
      return;
    }
    try {
      if (editUnit) {
        await api.put(`/service-units/${editUnit.id}`, form);
        toast.success('Unidade atualizada!');
      } else {
        await api.post('/service-units', form);
        toast.success('Unidade criada!');
      }
      setShowForm(false);
      setEditUnit(null);
      setForm({ ...emptyForm });
      loadData();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro ao salvar'); }
  };

  const handleEdit = (u) => {
    setEditUnit(u);
    setForm({ name: u.name, address: u.address || '', phone: u.phone || '' });
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    const ok = await confirm('Desativar esta unidade de atendimento?');
    if (!ok) return;
    try {
      await api.delete(`/service-units/${id}`);
      toast.success('Unidade desativada!');
      loadData();
    } catch (err) { toast.error(err.response?.data?.message || 'Erro'); }
  };

  return (
    <div className="page-full">
      <div className="page-header">
        <h1>Unidades de Atendimento</h1>
        <button className="btn btn-primary" onClick={() => { setEditUnit(null); setForm({ ...emptyForm }); setShowForm(true); }}>
          + Nova Unidade
        </button>
      </div>
      <div className="page-body">
        <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <div className="card-body" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
            <div className="scroll-container" style={{ flex: 1 }}>
              <table>
                <thead>
                  <tr><th>Nome</th><th>Endereço</th><th>Telefone</th><th>Situação</th><th>Ações</th></tr>
                </thead>
                <tbody>
                  {units.map(u => (
                    <tr key={u.id}>
                      <td><strong>{u.name}</strong></td>
                      <td>{u.address || '-'}</td>
                      <td>{u.phone || '-'}</td>
                      <td>
                        <span className={`badge ${u.active ? 'badge-success' : 'badge-danger'}`}>
                          {u.active ? 'Ativa' : 'Inativa'}
                        </span>
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: 4 }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleEdit(u)}>Editar</button>
                          {u.active && (
                            <button className="btn btn-secondary btn-sm" style={{ color: 'var(--danger)' }} onClick={() => handleDelete(u.id)}>Desativar</button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                  {units.length === 0 && <tr><td colSpan={5} className="empty-state" style={{ padding: 40 }}>Nenhuma unidade cadastrada</td></tr>}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editUnit ? 'Editar Unidade' : 'Nova Unidade'}</h3>
              <button className="modal-close" onClick={() => setShowForm(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <div className="form-grid">
                <div className="form-group full-width required">
                  <label>Nome da Unidade</label>
                  <input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Ex.: Unidade Centro" />
                </div>
                <div className="form-group full-width">
                  <label>Endereço</label>
                  <input value={form.address} onChange={e => setForm({ ...form, address: e.target.value })} placeholder="Rua, número, bairro, cidade..." />
                </div>
                <div className="form-group">
                  <label>Telefone</label>
                  <input value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} placeholder="(00) 0000-0000" />
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleSave}>{editUnit ? 'Atualizar' : 'Salvar'}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
