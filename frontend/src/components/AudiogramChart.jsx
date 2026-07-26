import React, { useRef, useCallback } from 'react';

const FREQUENCIES = [250, 500, 1000, 2000, 3000, 4000, 6000, 8000];
const DB_MIN = -10;
const DB_MAX = 120;
const DB_STEP = 20;

export default function AudiogramChart({ data, onChange, ear, disabled, observations, onObservationsChange }) {
  const svgRef = useRef(null);

  const margin = { top: 10, right: 12, bottom: 24, left: 28 };
  const width = 440;
  const height = 160;
  const plotW = width - margin.left - margin.right;
  const plotH = height - margin.top - margin.bottom;

  const freqToX = (freq) => {
    const idx = FREQUENCIES.indexOf(freq);
    return margin.left + (idx / (FREQUENCIES.length - 1)) * plotW;
  };

  const dbToY = (db) => {
    return margin.top + ((db - DB_MIN) / (DB_MAX - DB_MIN)) * plotH;
  };

  const xToFreq = (x) => {
    const idx = Math.round(((x - margin.left) / plotW) * (FREQUENCIES.length - 1));
    return FREQUENCIES[Math.max(0, Math.min(FREQUENCIES.length - 1, idx))];
  };

  const yToDb = (y) => {
    const db = DB_MIN + ((y - margin.top) / plotH) * (DB_MAX - DB_MIN);
    return Math.round(db / 5) * 5;
  };

  const handleClick = useCallback((e) => {
    if (disabled) return;
    const svg = svgRef.current;
    const rect = svg.getBoundingClientRect();
    const x = ((e.clientX - rect.left) / rect.width) * width;
    const y = ((e.clientY - rect.top) / rect.height) * height;

    if (x < margin.left || x > width - margin.right || y < margin.top || y > height - margin.bottom) return;

    const freq = xToFreq(x);
    const db = Math.max(DB_MIN, Math.min(DB_MAX, yToDb(y)));

    if (ear === 'right') {
      onChange(`right${freq}`, db);
    } else {
      onChange(`left${freq}`, db);
    }
  }, [disabled, ear, onChange]);

  const getPoints = (prefix) => {
    return FREQUENCIES.map(freq => {
      const val = data[`${prefix}${freq}`];
      if (val === '' || val === null || val === undefined) return null;
      return { x: freqToX(freq), y: dbToY(Number(val)), freq, db: Number(val) };
    }).filter(Boolean);
  };

  const rightPoints = getPoints('right');
  const leftPoints = getPoints('left');

  const buildPath = (points) => {
    if (points.length < 2) return '';
    return points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
  };

  const rightPath = buildPath(rightPoints);
  const leftPath = buildPath(leftPoints);

  const dbLabels = [];
  for (let db = DB_MIN; db <= DB_MAX; db += DB_STEP) dbLabels.push(db);

  const MARKER_R = 3.5;

  return (
    <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
      <div style={{ width: '55%', flexShrink: 0, minWidth: 0 }}>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 4 }}>
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 3, fontSize: 10, color: 'var(--text-muted)' }}>
            <svg width="10" height="10"><circle cx="5" cy="5" r="3" fill="none" stroke="#f43f5e" strokeWidth="1.5" /></svg>
            OD
          </span>
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 3, fontSize: 10, color: 'var(--text-muted)' }}>
            <svg width="10" height="10">
              <line x1="1" y1="1" x2="9" y2="9" stroke="#6366f1" strokeWidth="1.5" />
              <line x1="9" y1="1" x2="1" y2="9" stroke="#6366f1" strokeWidth="1.5" />
            </svg>
            OE
          </span>
          <span style={{ fontSize: 10, color: disabled ? 'var(--text-muted)' : ear === 'right' ? '#f43f5e' : '#6366f1', fontWeight: 500 }}>
            {disabled ? '' : `Clique: ${ear === 'right' ? 'OD' : 'OE'}`}
          </span>
        </div>

        <svg ref={svgRef} viewBox={`0 0 ${width} ${height}`} preserveAspectRatio="xMidYMid meet"
          style={{ width: '100%', display: 'block', cursor: disabled ? 'default' : 'crosshair', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)' }}
          onClick={handleClick}>
          <defs>
            <linearGradient id="normalZone" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#10b981" stopOpacity="0.06" />
              <stop offset="100%" stopColor="#10b981" stopOpacity="0.01" />
            </linearGradient>
          </defs>

          <rect x={margin.left} y={margin.top} width={plotW} height={plotH} fill="white" />
          <rect x={margin.left} y={margin.top} width={plotW} height={dbToY(20) - margin.top} fill="url(#normalZone)" />

          {dbLabels.map(db => (
            <g key={db}>
              <line x1={margin.left} y1={dbToY(db)} x2={width - margin.right} y2={dbToY(db)}
                stroke="#e2e8f0" strokeWidth={db === 0 ? 1 : 0.4} />
              <text x={margin.left - 5} y={dbToY(db) + 3} textAnchor="end" fontSize="8" fill="#94a3b8">
                {db}
              </text>
            </g>
          ))}

          {FREQUENCIES.map(freq => (
            <g key={freq}>
              <line x1={freqToX(freq)} y1={margin.top} x2={freqToX(freq)} y2={height - margin.bottom}
                stroke="#e2e8f0" strokeWidth={0.4} />
              <text x={freqToX(freq)} y={height - margin.bottom + 12} textAnchor="middle" fontSize="8" fill="#64748b" fontWeight="600">
                {freq}
              </text>
            </g>
          ))}

          <text x={width / 2} y={height - 2} textAnchor="middle" fontSize="8" fill="#64748b" fontWeight="600">Hz</text>

          {rightPath && <path d={rightPath} fill="none" stroke="#f43f5e" strokeWidth="1.5" />}
          {leftPath && <path d={leftPath} fill="none" stroke="#6366f1" strokeWidth="1.5" />}

          {rightPoints.map((p, i) => (
            <g key={`r-${i}`}>
              <circle cx={p.x} cy={p.y} r={MARKER_R} fill="white" stroke="#f43f5e" strokeWidth="1.5" />
              <text x={p.x} y={p.y - MARKER_R - 3} textAnchor="middle" fontSize="7" fill="#f43f5e" fontWeight="700">{p.db}</text>
            </g>
          ))}

          {leftPoints.map((p, i) => (
            <g key={`l-${i}`}>
              <line x1={p.x - MARKER_R} y1={p.y - MARKER_R} x2={p.x + MARKER_R} y2={p.y + MARKER_R}
                stroke="#6366f1" strokeWidth="1.5" />
              <line x1={p.x + MARKER_R} y1={p.y - MARKER_R} x2={p.x - MARKER_R} y2={p.y + MARKER_R}
                stroke="#6366f1" strokeWidth="1.5" />
              <text x={p.x} y={p.y - MARKER_R - 3} textAnchor="middle" fontSize="7" fill="#6366f1" fontWeight="700">{p.db}</text>
            </g>
          ))}
        </svg>

        <div style={{ display: 'flex', gap: 4, marginTop: 6 }}>
          <button className={`btn btn-sm ${ear === 'right' ? 'btn-danger' : 'btn-secondary'}`}
            style={{ flex: 1, fontSize: 11 }} onClick={() => onChange('_setEar', 'right')} disabled={disabled}>
            Marcar OD
          </button>
          <button className={`btn btn-sm ${ear === 'left' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ flex: 1, fontSize: 11 }} onClick={() => onChange('_setEar', 'left')} disabled={disabled}>
            Marcar OE
          </button>
        </div>

        <div className="form-group" style={{ marginTop: 8, marginBottom: 0 }}>
          <label style={{ fontSize: 11 }}>Observacoes</label>
          <textarea rows={4} value={observations || ''}
            onChange={e => onObservationsChange(e.target.value)} disabled={disabled}
            style={{ fontSize: 11, padding: '6px 8px' }} />
        </div>
      </div>

      <div style={{ width: 200, flexShrink: 0 }}>
        <table className="audiogram-table" style={{ fontSize: 13 }}>
          <thead>
            <tr><th style={{ fontSize: 12 }}>Hz</th><th style={{ color: '#f43f5e', fontSize: 12 }}>OD</th><th style={{ color: '#6366f1', fontSize: 12 }}>OE</th></tr>
          </thead>
          <tbody>
            {FREQUENCIES.map(f => (
              <tr key={f}>
                <td style={{ fontWeight: 600, fontSize: 12, padding: '6px 6px' }}>{f}</td>
                <td style={{ padding: '5px 3px' }}>
                  <input type="number" min="-10" max="120" step="5"
                    value={data[`right${f}`]}
                    onChange={e => onChange(`right${f}`, e.target.value === '' ? '' : Number(e.target.value))}
                    placeholder="--" disabled={disabled} style={{ fontSize: 13, padding: '7px 6px' }} />
                </td>
                <td style={{ padding: '5px 3px' }}>
                  <input type="number" min="-10" max="120" step="5"
                    value={data[`left${f}`]}
                    onChange={e => onChange(`left${f}`, e.target.value === '' ? '' : Number(e.target.value))}
                    placeholder="--" disabled={disabled} style={{ fontSize: 13, padding: '7px 6px' }} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
