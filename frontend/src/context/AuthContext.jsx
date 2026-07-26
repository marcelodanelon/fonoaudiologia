import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

function getStoredAuth() {
  try {
    const userStr = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    const lastActivityStr = localStorage.getItem('lastActivity');
    if (!userStr || !token) return { user: null, token: null, timeoutMinutes: 30 };

    const user = JSON.parse(userStr);
    const timeoutMinutes = user.sessionTimeoutMinutes || 30;

    if (lastActivityStr) {
      const elapsed = Date.now() - Number(lastActivityStr);
      const timeoutMs = timeoutMinutes * 60 * 1000;
      if (elapsed >= timeoutMs) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('lastActivity');
        return { user: null, token: null, timeoutMinutes };
      }
      return { user, token, timeoutMinutes, lastActivity: Number(lastActivityStr) };
    }

    return { user, token, timeoutMinutes };
  } catch {
    return { user: null, token: null, timeoutMinutes: 30 };
  }
}

export function AuthProvider({ children }) {
  const stored = getStoredAuth();
  const [user, setUser] = useState(stored.user);
  const [token, setToken] = useState(stored.token);
  const [loading, setLoading] = useState(!!stored.token);
  const [sessionTimeoutMinutes, setSessionTimeoutMinutes] = useState(stored.timeoutMinutes);

  const [lastActivity, setLastActivity] = useState(stored.lastActivity || Date.now());
  const [showWarning, setShowWarning] = useState(false);

  useEffect(() => {
    if (!token) return;
    api.get('/dashboard').then(() => {
      setLoading(false);
    }).catch(() => {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('lastActivity');
      setToken(null);
      setUser(null);
      setLoading(false);
    });
  }, []);

  const login = async (username, password) => {
    const response = await api.post('/auth/login', { username, password });
    const data = response.data;
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data));
    localStorage.setItem('lastActivity', Date.now());
    setToken(data.token);
    setUser(data);
    setSessionTimeoutMinutes(data.sessionTimeoutMinutes);
    setLastActivity(Date.now());
    setShowWarning(false);
  };

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('lastActivity');
    setToken(null);
    setUser(null);
  }, []);

  const checkActivity = useCallback(() => {
    if (!token) return;

    const timeoutMs = sessionTimeoutMinutes * 60 * 1000;
    const warningMs = timeoutMs - 60000;
    const elapsed = Date.now() - lastActivity;

    if (elapsed >= timeoutMs) {
      (async () => {
        try {
          const res = await api.post('/auth/refresh', { token });
          const newToken = res.data.token;
          localStorage.setItem('token', newToken);
          const now = Date.now();
          localStorage.setItem('lastActivity', now);
          setToken(newToken);
          setLastActivity(now);
          setShowWarning(false);
        } catch {
          logout();
        }
      })();
    } else if (elapsed >= warningMs) {
      setShowWarning(true);
    }
  }, [token, lastActivity, sessionTimeoutMinutes, logout]);

  useEffect(() => {
    if (!token) return;
    const interval = setInterval(checkActivity, 5000);
    return () => clearInterval(interval);
  }, [checkActivity, token]);

  useEffect(() => {
    if (!token) return;

    const handleActivity = () => {
      const now = Date.now();
      localStorage.setItem('lastActivity', now);
      setLastActivity(now);
      setShowWarning(false);
    };

    const events = ['mousedown', 'keypress', 'scroll', 'touchstart'];
    events.forEach(e => window.addEventListener(e, handleActivity));
    return () => events.forEach(e => window.removeEventListener(e, handleActivity));
  }, [token]);

  const extendSession = () => {
    const now = Date.now();
    localStorage.setItem('lastActivity', now);
    setLastActivity(now);
    setShowWarning(false);
  };

  const hasPermission = (permission) => {
    return user?.permissions?.includes(permission) || false;
  };

  return (
    <AuthContext.Provider value={{
      user, token, loading, login, logout, hasPermission,
      showWarning, extendSession, sessionTimeoutMinutes
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
