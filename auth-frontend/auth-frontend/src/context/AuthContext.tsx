import {
  createContext,
  ReactNode,
  useCallback,
  useEffect,
  useState,
} from "react";
import * as authService from "../services/authService";
import { LoginRequest, RegisterRequest, User } from "../types/auth";

export interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
}

// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  // Startup check: don't assume the user is logged in just because a
  // previous UI state existed — always confirm with the backend.
  useEffect(() => {
    let cancelled = false;

    authService
      .getCurrentUser()
      .then((currentUser) => {
        if (!cancelled) setUser(currentUser);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const login = useCallback(async (payload: LoginRequest) => {
    const { user: loggedInUser } = await authService.login(payload);
    setUser(loggedInUser);
  }, []);

  const register = useCallback(async (payload: RegisterRequest) => {
    const { user: registeredUser } = await authService.register(payload);
    setUser(registeredUser);
  }, []);

  const logout = useCallback(async () => {
    await authService.logout();
    setUser(null);
  }, []);

  const value: AuthContextValue = {
    user,
    isAuthenticated: user !== null,
    loading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
