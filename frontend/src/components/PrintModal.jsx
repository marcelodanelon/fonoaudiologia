import React, { useRef, useEffect } from 'react';

export default function PrintModal({ html, onClose }) {
  const iframeRef = useRef(null);

  useEffect(() => {
    if (iframeRef.current && html) {
      const doc = iframeRef.current.contentDocument;
      doc.open();
      doc.write(html);
      doc.close();
    }
  }, [html]);

  const handlePrint = () => {
    const iframe = iframeRef.current;
    if (!iframe) return;
    iframe.contentWindow.focus();
    iframe.contentWindow.print();
  };

  if (!html) return null;

  return (
    <div className="modal-overlay" onClick={onClose} style={{ zIndex: 200 }}>
      <div className="print-modal" onClick={e => e.stopPropagation()}>
        <div className="print-modal-header">
          <h3>Vizualizacao do Documento</h3>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn btn-primary btn-sm" onClick={handlePrint}>
              Imprimir / Salvar PDF
            </button>
            <button className="btn btn-secondary btn-sm" onClick={onClose}>
              Fechar
            </button>
          </div>
        </div>
        <div className="print-modal-body">
          <iframe
            ref={iframeRef}
            title="Documento"
            style={{ width: '100%', height: '100%', border: 'none', borderRadius: 'var(--radius)' }}
          />
        </div>
      </div>
    </div>
  );
}
