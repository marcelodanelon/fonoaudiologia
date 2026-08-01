import React, { createContext, useContext, useState, useCallback } from 'react';

const ConfirmContext = createContext();

export function useConfirm() {
  return useContext(ConfirmContext);
}

export function ConfirmProvider({ children }) {
  const [confirmState, setConfirmState] = useState(null);

  const confirm = useCallback((message, title = 'Confirmar') => {
    return new Promise(resolve => {
      setConfirmState({ message, title, resolve });
    });
  }, []);

  const handleConfirm = () => {
    confirmState?.resolve(true);
    setConfirmState(null);
  };

  const handleCancel = () => {
    confirmState?.resolve(false);
    setConfirmState(null);
  };

  return (
    <ConfirmContext.Provider value={confirm}>
      {children}
      {confirmState && (
        <div className="confirm-overlay" onClick={handleCancel}>
          <div className="confirm-modal" onClick={e => e.stopPropagation()}>
            <div className="confirm-header">
              <h3>{confirmState.title}</h3>
            </div>
            <div className="confirm-body">
              <p>{confirmState.message}</p>
            </div>
            <div className="confirm-footer">
              <button className="btn btn-secondary" onClick={handleCancel}>Cancelar</button>
              <button className="btn btn-danger" onClick={handleConfirm}>Confirmar</button>
            </div>
          </div>
        </div>
      )}
    </ConfirmContext.Provider>
  );
}
