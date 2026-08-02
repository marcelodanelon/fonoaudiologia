import React, { useState, useEffect } from 'react';

const DAY_LABELS = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'];
const MONTH_LABELS = [
  'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
];

const pad = n => String(n).padStart(2, '0');
const toISO = (y, m, d) => `${y}-${pad(m + 1)}-${pad(d)}`;

export default function DateCalendar({ dates = [], value = '', onSelect }) {
  const dateSet = new Set(dates);
  const [year, setYear] = useState(2026);
  const [month, setMonth] = useState(0);

  useEffect(() => {
    const base = value || dates[0] || toISO(new Date().getFullYear(), new Date().getMonth(), 1);
    const d = new Date(base + 'T00:00:00');
    if (!isNaN(d.getTime())) {
      setYear(d.getFullYear());
      setMonth(d.getMonth());
    }
  }, [value, dates]);

  const prevMonth = () => {
    if (month === 0) { setMonth(11); setYear(y => y - 1); }
    else setMonth(m => m - 1);
  };
  const nextMonth = () => {
    if (month === 11) { setMonth(0); setYear(y => y + 1); }
    else setMonth(m => m + 1);
  };

  const firstWeekday = (new Date(year, month, 1).getDay() + 6) % 7;
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const cells = [];
  for (let i = 0; i < firstWeekday; i++) cells.push(null);
  for (let d = 1; d <= daysInMonth; d++) cells.push(d);

  const today = new Date();
  const todayISO = toISO(today.getFullYear(), today.getMonth(), today.getDate());

  return (
    <div style={{ userSelect: 'none' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
        <button type="button" className="btn btn-secondary btn-sm" onClick={prevMonth}>&laquo;</button>
        <span style={{ fontSize: 14, fontWeight: 600 }}>{MONTH_LABELS[month]} {year}</span>
        <button type="button" className="btn btn-secondary btn-sm" onClick={nextMonth}>&raquo;</button>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 4, textAlign: 'center' }}>
        {DAY_LABELS.map(l => (
          <div key={l} style={{ fontSize: 11, fontWeight: 700, color: 'var(--text-muted)', padding: '4px 0' }}>{l}</div>
        ))}
        {cells.map((d, i) => {
          if (d === null) return <div key={`b${i}`} />;
          const iso = toISO(year, month, d);
          const available = dateSet.has(iso);
          const selected = iso === value;
          const isToday = iso === todayISO;
          return (
            <button
              key={iso}
              type="button"
              disabled={!available}
              onClick={() => onSelect(iso)}
              style={{
                padding: '7px 0', borderRadius: 'var(--radius-sm)', fontSize: 13,
                border: selected ? '1.5px solid var(--primary)' : '1.5px solid transparent',
                background: selected ? 'var(--primary)' : available ? 'var(--primary-light, #e0f0ff)' : 'transparent',
                color: selected ? 'white' : available ? 'var(--primary)' : 'var(--text-muted)',
                cursor: available ? 'pointer' : 'default',
                opacity: available ? 1 : 0.35,
                fontWeight: isToday ? 700 : 400,
              }}
            >{d}</button>
          );
        })}
      </div>
      <div style={{ marginTop: 8, fontSize: 11, color: 'var(--text-muted)' }}>
        {dates.length} data(s) com vagas no período
      </div>
    </div>
  );
}
