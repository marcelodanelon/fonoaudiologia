import React, { useState, useEffect, useRef } from 'react';

const displayName = (p) => (p ? `${p.name}${p.cpf ? ` (${p.cpf})` : ''}` : '');

export default function PatientAutocomplete({ patients, value, onSelect, onClear, placeholder = 'Buscar paciente...', disabled = false }) {
  const [search, setSearch] = useState('');
  const [focus, setFocus] = useState(false);
  const wrapperRef = useRef(null);

  useEffect(() => {
    const handler = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) setFocus(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const filtered = patients
    .filter(p => {
      const s = search.toLowerCase();
      return p.name?.toLowerCase().includes(s) || (p.cpf || '').includes(search) || (p.phone || '').includes(search);
    })
    .slice(0, 30);

  const clear = () => {
    setSearch('');
    setFocus(false);
    if (onClear) onClear();
  };

  return (
    <div className="autocomplete" ref={wrapperRef}>
      <div className="autocomplete-input-wrap">
        <input
          placeholder={value ? 'Paciente selecionado' : placeholder}
          value={value ? displayName(value) : search}
          readOnly={Boolean(value)}
          disabled={disabled}
          onFocus={() => setFocus(true)}
          onBlur={() => setTimeout(() => setFocus(false), 120)}
          onChange={e => {
            setSearch(e.target.value);
            if (value && onClear) onClear();
          }}
        />
        {value && (
          <button type="button" className="autocomplete-clear" title="Trocar paciente"
            onClick={clear}>
            &times;
          </button>
        )}
      </div>
      {focus && !value && search.trim() !== '' && (
        <div className="autocomplete-list">
          {filtered.length === 0
            ? <div className="autocomplete-empty">Nenhum paciente encontrado</div>
            : filtered.map(p => (
                <div
                  key={p.id}
                  className="autocomplete-item"
                  onMouseDown={e => e.preventDefault()}
                  onClick={() => {
                    setSearch(displayName(p));
                    setFocus(false);
                    onSelect(p);
                  }}
                >
                  {displayName(p)}
                </div>
              ))}
        </div>
      )}
    </div>
  );
}
