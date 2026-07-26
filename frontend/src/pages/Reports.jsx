import React, { useState, useEffect, useMemo } from 'react';
import api from '../api/axios';
import { useToast } from '../context/ToastContext';
import PrintModal from '../components/PrintModal';
import Pagination from '../components/Pagination';

const PAGE_SIZE = 12;

function openPrintWindow(title, tableHtml, date, count) {
  const html = `<!DOCTYPE html><html><head><meta charset="UTF-8"><title>${title}</title>
    <style>
      @page { margin: 15mm; size: A4; }
      body { font-family: Arial, sans-serif; font-size: 12px; color: #333; margin: 20px; }
      h1 { font-size: 18px; border-bottom: 2px solid #333; padding-bottom: 8px; }
      table { width: 100%; border-collapse: collapse; margin-top: 10px; }
      th, td { border: 1px solid #ddd; padding: 6px 8px; text-align: left; font-size: 11px; }
      th { background: #f1f5f9; font-weight: 600; }
      .info { margin: 4px 0; font-size: 12px; }
      .info .label { font-weight: 600; }
    </style></head><body>
    <h1>${title}</h1>
    <div class="info"><span class="label">Data:</span> ${date}</div>
    <div class="info"><span class="label">Total:</span> ${count} registro(s)</div>
    ${tableHtml}
  </body></html>`;
  return html;
}

function PatientsReport({ onPrint, onSetPrintHtml }) {
  const toast = useToast();
  const [patients, setPatients] = useState([]);
  const [searchFilter, setSearchFilter] = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => { setPage(0); }, [searchFilter]);

  useEffect(() => {
    api.get('/patients').then(res => setPatients(res.data)).catch(() => toast.error('Erro ao carregar'));
  }, []);

  const filtered = useMemo(() => {
    if (!searchFilter) return patients;
    const s = searchFilter.toLowerCase();
    return patients.filter(p =>
      p.name?.toLowerCase().includes(s) || p.cpf?.includes(s) || p.phone?.includes(s) || p.email?.toLowerCase().includes(s)
    );
  }, [patients, searchFilter]);

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paged = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  const print = () => {
    const rows = filtered.map(p => `<tr><td>${p.name||''}</td><td>${p.cpf||'-'}</td><td>${p.phone||'-'}</td><td>${p.email||'-'}</td><td>${p.city||'-'}</td><td>${p.active?'Ativo':'Inativo'}</td></tr>`).join('');
    onSetPrintHtml(openPrintWindow('Relatorio de Pacientes',
      `<table><thead><tr><th>Nome</th><th>CPF</th><th>Telefone</th><th>Email</th><th>Cidade</th><th>Status</th></tr></thead><tbody>${rows}</tbody></table>`,
      new Date().toLocaleDateString('pt-BR'), filtered.length));
  };

  useEffect(() => { if (onPrint) onPrint.current = print; });

  return (
    <>
      <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 14, flexShrink: 0 }}>
        <input placeholder="Filtrar por nome, CPF, telefone..." value={searchFilter}
          onChange={e => setSearchFilter(e.target.value)}
          style={{ flex: 1, padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit' }} />
        <span style={{ fontSize: 12, color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>{filtered.length} registro(s)</span>
      </div>
      <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <div className="card-body" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
          <div className="scroll-container" style={{ flex: 1 }}>
            <table>
              <thead><tr><th>Nome</th><th>CPF</th><th>Telefone</th><th>Email</th><th>Cidade</th><th>Status</th></tr></thead>
              <tbody>
                {paged.map(p => (
                  <tr key={p.id}>
                    <td>{p.name}</td>
                    <td>{p.cpf || '-'}</td>
                    <td>{p.phone || '-'}</td>
                    <td>{p.email || '-'}</td>
                    <td>{p.city || '-'}</td>
                    <td><span className={`badge ${p.active ? 'badge-success' : 'badge-danger'}`}>{p.active ? 'Ativo' : 'Inativo'}</span></td>
                  </tr>
                ))}
                {filtered.length === 0 && <tr><td colSpan={6} className="empty-state" style={{ padding: 40 }}>Nenhum paciente encontrado</td></tr>}
              </tbody>
            </table>
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      </div>
    </>
  );
}

function ConsultationsReport({ onPrint, onSetPrintHtml }) {
  const toast = useToast();
  const [consultations, setConsultations] = useState([]);
  const [searchFilter, setSearchFilter] = useState('');
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [page, setPage] = useState(0);

  useEffect(() => { setPage(0); }, [selectedDate, searchFilter]);

  useEffect(() => {
    const params = { startDate: selectedDate, endDate: selectedDate };
    api.get('/consultations', { params }).then(res => setConsultations(res.data)).catch(() => toast.error('Erro ao carregar'));
  }, [selectedDate]);

  const filtered = useMemo(() => {
    if (!searchFilter) return consultations;
    const s = searchFilter.toLowerCase();
    return consultations.filter(c =>
      c.patient?.name?.toLowerCase().includes(s) || c.professional?.name?.toLowerCase().includes(s) || c.type?.toLowerCase().includes(s) || c.status?.toLowerCase().includes(s)
    );
  }, [consultations, searchFilter]);

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paged = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  const statusBadge = (s) => {
    const map = { AGENDADA: 'badge-warning', EM_ANDAMENTO: 'badge-info', CONCLUIDA: 'badge-success', CANCELADA: 'badge-danger' };
    return <span className={`badge ${map[s] || 'badge-secondary'}`}>{s?.replace('_', ' ')}</span>;
  };

  const print = () => {
    const rows = filtered.map(c => `<tr><td>${new Date(c.createdAt).toLocaleString('pt-BR')}</td><td>${c.patient?.name||''}</td><td>${c.professional?.name||''}</td><td>${c.type||''}</td><td>${(c.status||'').replace('_',' ')}</td><td>${c.receptionRecordId?'Recepcao':'Direto'}</td></tr>`).join('');
    onSetPrintHtml(openPrintWindow('Relatorio de Atendimentos',
      `<table><thead><tr><th>Data</th><th>Paciente</th><th>Profissional</th><th>Tipo</th><th>Status</th><th>Origem</th></tr></thead><tbody>${rows}</tbody></table>`,
      new Date().toLocaleDateString('pt-BR'), filtered.length));
  };

  useEffect(() => { if (onPrint) onPrint.current = print; });

  return (
    <>
      <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 14, flexShrink: 0 }}>
        <input type="date" value={selectedDate} onChange={e => setSelectedDate(e.target.value)}
          style={{ padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', width: 150 }} />
        <input placeholder="Filtrar por paciente, profissional, tipo ou status..." value={searchFilter}
          onChange={e => setSearchFilter(e.target.value)}
          style={{ flex: 1, padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit' }} />
        <span style={{ fontSize: 12, color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>{filtered.length} registro(s)</span>
      </div>
      <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <div className="card-body" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
          <div className="scroll-container" style={{ flex: 1 }}>
            <table>
              <thead><tr><th>Data</th><th>Paciente</th><th>Profissional</th><th>Tipo</th><th>Status</th><th>Origem</th><th>Acoes</th></tr></thead>
              <tbody>
                {paged.map(c => (
                  <tr key={c.id}>
                    <td style={{ whiteSpace: 'nowrap' }}>{new Date(c.createdAt).toLocaleString('pt-BR')}</td>
                    <td>{c.patient?.name}</td>
                    <td>{c.professional?.name}</td>
                    <td>{c.type}</td>
                    <td>{statusBadge(c.status)}</td>
                    <td>{c.receptionRecordId ? <span className="badge badge-purple">Recepcao</span> : <span className="badge badge-secondary">Direto</span>}</td>
                    <td>
                      <button className="btn btn-primary btn-sm" onClick={async () => { try { const res = await api.get(`/consultations/${c.id}/report`); onSetPrintHtml(res.data); } catch {} }}>
                        Laudo
                      </button>
                    </td>
                  </tr>
                ))}
                {filtered.length === 0 && <tr><td colSpan={7} className="empty-state" style={{ padding: 40 }}>Nenhuma consulta encontrada para esta data</td></tr>}
              </tbody>
            </table>
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      </div>
    </>
  );
}

function ReceptionsReport({ onPrint, onSetPrintHtml }) {
  const toast = useToast();
  const [records, setRecords] = useState([]);
  const [searchFilter, setSearchFilter] = useState('');
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [page, setPage] = useState(0);

  useEffect(() => { setPage(0); }, [selectedDate, searchFilter]);

  useEffect(() => {
    const params = { startDate: selectedDate, endDate: selectedDate };
    api.get('/reception', { params }).then(res => setRecords(res.data)).catch(() => toast.error('Erro ao carregar'));
  }, [selectedDate]);

  const filtered = useMemo(() => {
    if (!searchFilter) return records;
    const s = searchFilter.toLowerCase();
    return records.filter(r =>
      r.patient?.name?.toLowerCase().includes(s) || r.patient?.cpf?.includes(s) || r.status?.toLowerCase().includes(s)
    );
  }, [records, searchFilter]);

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paged = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  const statusBadge = (s) => {
    const map = { CHECKIN: 'badge-success', CANCELADO: 'badge-danger' };
    return <span className={`badge ${map[s] || 'badge-secondary'}`}>{s}</span>;
  };

  const print = () => {
    const rows = filtered.map(r => `<tr><td>${new Date(r.createdAt).toLocaleString('pt-BR')}</td><td>${r.patient?.name||''}</td><td>${r.patient?.cpf||'-'}</td><td>${r.status||''}</td><td>${r.observations||'-'}</td></tr>`).join('');
    onSetPrintHtml(openPrintWindow('Relatorio de Recepcoes',
      `<table><thead><tr><th>Data Check-in</th><th>Paciente</th><th>CPF</th><th>Status</th><th>Observacoes</th></tr></thead><tbody>${rows}</tbody></table>`,
      new Date().toLocaleDateString('pt-BR'), filtered.length));
  };

  useEffect(() => { if (onPrint) onPrint.current = print; });

  return (
    <>
      <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 14, flexShrink: 0 }}>
        <input type="date" value={selectedDate} onChange={e => setSelectedDate(e.target.value)}
          style={{ padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit', width: 150 }} />
        <input placeholder="Filtrar por paciente, CPF ou status..." value={searchFilter}
          onChange={e => setSearchFilter(e.target.value)}
          style={{ flex: 1, padding: '8px 12px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: 13, fontFamily: 'inherit' }} />
        <span style={{ fontSize: 12, color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>{filtered.length} registro(s)</span>
      </div>
      <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <div className="card-body" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
          <div className="scroll-container" style={{ flex: 1 }}>
            <table>
              <thead><tr><th>Data Check-in</th><th>Paciente</th><th>CPF</th><th>Status</th><th>Observacoes</th></tr></thead>
              <tbody>
                {paged.map(r => (
                  <tr key={r.id}>
                    <td style={{ whiteSpace: 'nowrap' }}>{new Date(r.createdAt).toLocaleString('pt-BR')}</td>
                    <td>{r.patient?.name}</td>
                    <td>{r.patient?.cpf || '-'}</td>
                    <td>{statusBadge(r.status)}</td>
                    <td>{r.observations || '-'}</td>
                  </tr>
                ))}
                {filtered.length === 0 && <tr><td colSpan={5} className="empty-state" style={{ padding: 40 }}>Nenhum registro de recepcao encontrado para esta data</td></tr>}
              </tbody>
            </table>
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      </div>
    </>
  );
}

export default function Reports({ type = 'patients' }) {
  const printRef = React.useRef(null);
  const [printModalHtml, setPrintModalHtml] = useState(null);
  const ReportMap = { patients: PatientsReport, consultations: ConsultationsReport, receptions: ReceptionsReport };
  const Component = ReportMap[type] || PatientsReport;
  const titleMap = { patients: 'Relatorio de Pacientes', consultations: 'Relatorio de Atendimentos', receptions: 'Relatorio de Recepcoes' };

  return (
    <div className="page-full">
      <div className="page-header">
        <h1>{titleMap[type]}</h1>
        <button className="btn btn-primary" style={{ fontSize: 12, padding: '6px 14px' }}
          onClick={() => printRef.current && printRef.current()}>
          Imprimir
        </button>
      </div>
      <div className="page-body">
        <Component onPrint={printRef} onSetPrintHtml={setPrintModalHtml} />
      </div>
      {printModalHtml && <PrintModal html={printModalHtml} onClose={() => setPrintModalHtml(null)} />}
    </div>
  );
}
