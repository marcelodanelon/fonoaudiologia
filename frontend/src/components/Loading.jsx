import React from 'react';

export default function Loading({ text = 'Carregando...' }) {
  return (
    <div className="loading-container">
      <div className="spinner"></div>
      <span className="loading-text">{text}</span>
    </div>
  );
}

export function Skeleton({ rows = 3, style }) {
  return (
    <div style={style}>
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="skeleton-row" style={{ width: `${60 + Math.random() * 40}%` }} />
      ))}
    </div>
  );
}
