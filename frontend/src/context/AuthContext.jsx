import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../api/auth';
import { getToken, onUnauthorized, setToken } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [initializing, setInitializing] = useState(true);

  const clearSession = useCallback(() => {
    setToken(null);
    setUser(null);
  }, []);

  useEffect(() => {
    onUnauthorized(() => clearSession());
  }, [clearSession]);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      setInitializing(false);
      return;
    }
    authApi
      .profile()
      .then((profile) => setUser(profile))
      .catch(() => clearSession())
      .finally(() => setInitializing(false));
  }, [clearSession]);

  const login = useCallback(async (email, password) => {
    const res = await authApi.login({ email, password });
    setToken(res.token);
    const profile = await authApi.profile();
    setUser(profile);
    return profile;
  }, []);

  const register = useCallback((payload) => authApi.register(payload), []);

  const logout = useCallback(async () => {
    const token = getToken();
    try {
      await authApi.logout(token);
    } catch {
      // best-effort; clear local session regardless
    }
    clearSession();
  }, [clearSession]);

  const refreshProfile = useCallback(async () => {
    const profile = await authApi.profile();
    setUser(profile);
    return profile;
  }, []);

  const value = useMemo(
    () => ({
      user,
      initializing,
      isAuthenticated: !!user,
      login,
      register,
      logout,
      refreshProfile,
      hasRole: (...roles) => !!user && roles.includes(user.role),
    }),
    [user, initializing, login, register, logout, refreshProfile]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components -- co-located hook for this context
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
