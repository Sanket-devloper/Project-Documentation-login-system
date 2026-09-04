import { ApiError, LoginRequest, RegisterRequest, User } from "../types/auth";

/**
 * A tiny in-memory fake backend so the frontend is fully clickable before
 * Spring Boot exists (see Phase 5 — Mock API).
 *
 * Everything lives in module state, never in localStorage/sessionStorage —
 * that mirrors how a real server-side session/cookie behaves (it survives
 * client-side navigation, but resets on a hard page reload), and keeps us
 * honest about not storing auth data in the browser.
 *
 * DELETE THIS FILE once the real Spring Boot API is wired up in Phase 6 —
 * authService.ts is the only file that should import it.
 */

interface StoredUser extends User {
  password: string;
}

const DEMO_USER: StoredUser = {
  id: "1",
  name: "Demo User",
  email: "demo@example.com",
  role: "USER",
  password: "password123",
};

const users: StoredUser[] = [DEMO_USER];
let currentSessionUserId: string | null = null;

function delay<T>(value: T, ms = 600): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(value), ms));
}

function toPublicUser(user: StoredUser): User {
  const { password: _password, ...publicUser } = user;
  return publicUser;
}

export const mockBackend = {
  async login({ email, password }: LoginRequest): Promise<User> {
    await delay(null);
    const user = users.find((u) => u.email.toLowerCase() === email.toLowerCase());
    if (!user || user.password !== password) {
      throw new ApiError("Invalid email or password.", 401);
    }
    currentSessionUserId = user.id;
    return toPublicUser(user);
  },

  async register({ name, email, password }: RegisterRequest): Promise<User> {
    await delay(null);
    const exists = users.some((u) => u.email.toLowerCase() === email.toLowerCase());
    if (exists) {
      throw new ApiError("An account with this email already exists.", 409);
    }
    const user: StoredUser = {
      id: String(users.length + 1),
      name,
      email,
      password,
      role: "USER",
    };
    users.push(user);
    currentSessionUserId = user.id;
    return toPublicUser(user);
  },

  async logout(): Promise<void> {
    await delay(null, 300);
    currentSessionUserId = null;
  },

  async getCurrentUser(): Promise<User | null> {
    await delay(null, 300);
    if (!currentSessionUserId) return null;
    const user = users.find((u) => u.id === currentSessionUserId);
    return user ? toPublicUser(user) : null;
  },

  async forgotPassword(email: string): Promise<void> {
    await delay(null);
    // Always resolves the same way whether or not the email exists, to
    // avoid leaking account existence (OWASP guidance).
    void email;
  },

  async resetPassword(): Promise<void> {
    await delay(null);
  },
};
