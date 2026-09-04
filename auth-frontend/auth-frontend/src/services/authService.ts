import { apiClient } from "./apiClient";
import { mockBackend } from "./mockBackend";
import {
  AuthResult,
  LoginRequest,
  RegisterRequest,
  ResetPasswordRequest,
  User,
} from "../types/auth";

/**
 * Single place the rest of the app calls for authentication.
 *
 * Right now it delegates to the in-memory mock backend so the UI is fully
 * functional standalone. When the Spring Boot API is ready (Phase 6), flip
 * VITE_USE_MOCK_AUTH to "false" (or delete the flag) and this file is the
 * only place that needs to change — swap each mock call for the matching
 * apiClient call, which are already sketched in the comments below.
 */
const USE_MOCK = import.meta.env.VITE_USE_MOCK_AUTH !== "false";

export async function login(payload: LoginRequest): Promise<AuthResult> {
  if (USE_MOCK) {
    const user = await mockBackend.login(payload);
    return { user };
  }
  // return apiClient.post<AuthResult>("/auth/login", payload);
  return apiClient.post<AuthResult>("/auth/login", payload);
}

export async function register(payload: RegisterRequest): Promise<AuthResult> {
  if (USE_MOCK) {
    const user = await mockBackend.register(payload);
    return { user };
  }
  return apiClient.post<AuthResult>("/auth/register", payload);
}

export async function logout(): Promise<void> {
  if (USE_MOCK) {
    return mockBackend.logout();
  }
  return apiClient.post<void>("/auth/logout");
}

export async function getCurrentUser(): Promise<User | null> {
  if (USE_MOCK) {
    return mockBackend.getCurrentUser();
  }
  try {
    return await apiClient.get<User>("/auth/me");
  } catch {
    // Not authenticated (401) — treat as "no current user" rather than an error.
    return null;
  }
}

export async function forgotPassword(email: string): Promise<void> {
  if (USE_MOCK) {
    return mockBackend.forgotPassword(email);
  }
  return apiClient.post<void>("/auth/forgot-password", { email });
}

export async function resetPassword(payload: ResetPasswordRequest): Promise<void> {
  if (USE_MOCK) {
    return mockBackend.resetPassword();
  }
  return apiClient.post<void>("/auth/reset-password", payload);
}
