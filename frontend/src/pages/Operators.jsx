import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useToast } from '../context/ToastContext';
import { useConfirm } from '../context/ConfirmContext';

export default function Operators() {
  const toast = useToast();
  const confirm = useConfirm();
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editUser, setEditUser] = useState(null);
  const [form, setForm] = useState({
    username: '', password: '', name: '', email: '', cpf: '', phone: '', roleId: '', active: true,
  });

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    const [uRes, rRes] = await Promise.all([api.get('/users'), api.get('/users/roles')]);
    setUsers(uRes.data);
    setRoles(rRes.data);
  };

  const handleSave = async () => {
    try {
      if (editUser) {
        await api.put(`/users/${editUser.id}`, form);
        toast.success('Operador atualizado!');
      } else {
        await api.post('/users', form);
        toast.success('Operador criado!');
      }
      setShowForm(false);
      setEditUser(null);
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Erro ao salvar');
    }
  };

  const handleEdit = (u) => {
    setEditUser(u);
    setForm({
      username: u.username, password: '', name: u.name, email: u.email || '',
      cpf: u.cpf || '', phone: u.phone || '', roleId: u.roleId, active: u.active,
    });
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    const ok = await confirm('Tem certeza que deseja excluir?');
    if (!ok) return;
    try {
      await api.delete(`/users/${id}`);
      toast.success('Operador desativado!');
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Erro');
    }
  };

  const roleBadge = (roleName) => {
    const map = {
      'ADMINISTRADOR': 'badge-danger',
      'RECEPCIONISTA': 'badge-info',
      'FONOAUDIOLOGO': 'badge-success',
    };
    return <span className={`badge ${map[roleName] || 'badge-secondary'}`}>{roleName}</span>;
  };

  return (
    <div>
      <div className="page-header">
        <h1>Operadores</h1>
        <button className="btn btn-primary" onClick={() => { setEditUser(null); setForm({ username: '', password: '', name: '', email: '', cpf: '', phone: '', roleId: '', active: true }); setShowForm(true); }}>
          + Novo Operador
        </button>
      </div>

      <div className="card">
        <div className="card-header">
          <h3>Lista de Operadores</h3>
        </div>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>Usuário</th>
                <th>E-mail</th>
                <th>Perfil</th>
                <th>Situação</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id}>
                  <td>{u.name}</td>
                  <td>{u.username}</td>
                  <td>{u.email || '-'}</td>
                  <td>{roleBadge(u.roleName)}</td>
                  <td><span className={`badge ${u.active ? 'badge-success' : 'badge-danger'}`}>{u.active ? 'Ativo' : 'Inativo'}</span></td>
                  <td>
                    <div style={{ display: 'flex', gap: 4 }}>
                      <button className="btn btn-secondary btn-sm" onClick={() => handleEdit(u)}>Editar</button>
                      <button className="btn btn-danger btn-sm" onClick={() => handleDelete(u.id)}>Excluir</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h3>Perfis de Acesso</h3>
        </div>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Perfil</th>
                <th>Descrição</th>
                <th>Dashboard</th>
                <th>Recepção</th>
                <th>Consultas</th>
                <th>Pacientes</th>
                <th>Operadores</th>
                <th>Auditoria</th>
                <th>Config</th>
                <th>Estoque</th>
              </tr>
            </thead>
            <tbody>
              {roles.map(r => (
                <tr key={r.id}>
                  <td><strong>{r.name}</strong></td>
                  <td>{r.description}</td>
                  <td>{r.canAccessDashboard ? <span className="badge badge-success">Sim</span> : <span className="badge badge-danger">Não</span>}</td>
                  <td>{r.canAccessReception ? <span className="badge badge-success">Sim</span> : <span className="badge badge-danger">Não</span>}</td>
                  <td>{r.canAccessConsultation ? <span className="badge badge-success">Sim</span> : <span className="badge badge-danger">Não</span>}</td>
                  <td>{r.canAccessPatients ? <span className="badge badge-success">Sim</span> : <span className="badge badge-danger">Não</span>}</td>
                  <td>{r.canAccessOperators ? <span className="badge badge-success">Sim</span> : <span className="badge badge-danger">Não</span>}</td>
                  <td>{r.canAccessAuditLog ? <span className="badge badge-success">Sim</span> : <span className="badge badge-danger">Não</span>}</td>
                  <td>{r.canAccessSystemConfig ? <span className="badge badge-success">Sim</span> : <span className="badge badge-danger">Não</span>}</td>
                  <td>{r.canAccessInventory ? <span className="badge badge-success">Sim</span> : <span className="badge badge-danger">Não</span>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editUser ? 'Editar Operador' : 'Novo Operador'}</h2>
              <button className="modal-close" onClick={() => setShowForm(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <div className="form-grid">
                <div className="form-group">
                  <label>Nome Completo *</label>
                  <input value={form.name} onChange={e => setForm({...form, name: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Usuário *</label>
                  <input value={form.username} onChange={e => setForm({...form, username: e.target.value})} disabled={!!editUser} />
                </div>
                <div className="form-group">
                  <label>{editUser ? 'Nova Senha (deixe vazio para manter)' : 'Senha *'}</label>
                  <input type="password" value={form.password} onChange={e => setForm({...form, password: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Perfil *</label>
                  <select value={form.roleId} onChange={e => setForm({...form, roleId: e.target.value})}>
                    <option value="">Selecione...</option>
                    {roles.map(r => <option key={r.id} value={r.id}>{r.name} — {r.description}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>CPF</label>
                  <input value={form.cpf} onChange={e => setForm({...form, cpf: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Telefone</label>
                  <input value={form.phone} onChange={e => setForm({...form, phone: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>E-mail</label>
                  <input value={form.email} onChange={e => setForm({...form, email: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Situação</label>
                  <select value={form.active} onChange={e => setForm({...form, active: e.target.value === 'true'})}>
                    <option value="true">Ativo</option>
                    <option value="false">Inativo</option>
                  </select>
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleSave} style={{ width: 'auto' }}>
                {editUser ? 'Atualizar' : 'Criar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
