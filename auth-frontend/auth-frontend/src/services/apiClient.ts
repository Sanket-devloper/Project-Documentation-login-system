import { ApiError } from "../types/auth";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
}

/**
 * Thin wrapper around fetch() for talking to the Spring Boot API.
 *
 * - Sends/receives JSON.
 * - Uses `credentials: "include"` so an HttpOnly session cookie set by the
 *   backend is sent automatically. We never read or write auth tokens from
 *   JS/localStorage/sessionStorage here — that's the whole point.
 * - Normalizes failures into ApiError so callers get a consistent shape.
 */
async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    method: options.method ?? "GET",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });

  let payload: unknown = null;
  const text = await response.text();
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = text;
    }
  }

  if (!response.ok) {
    const message = toErrorMessage(response.status, payload);
    throw new ApiError(message, response.status);
  }

  return payload as T;
}

function toErrorMessage(status: number, payload: unknown): string {
  const serverMessage =
    payload && typeof payload === "object" && "message" in payload
      ? String((payload as { message?: unknown }).message ?? "")
      : undefined;

  switch (status) {
    case 400:
      return serverMessage || "That request wasn't valid.";
    case 401:
      return "Invalid email or password.";
    case 403:
      return "You don't have permission to do that.";
    case 404:
      return "We couldn't find what you were looking for.";
    case 409:
      return serverMessage || "That already exists.";
    case 422:
      return serverMessage || "Please check the highlighted fields.";
    case 429:
      return "Too many attempts. Please wait and try again.";
    case 500:
      return "Something went wrong on our end. Please try again.";
    default:
      return serverMessage || "Something went wrong. Please try again.";
  }
}

export const apiClient = {
  get: <T>(path: string) => request<T>(path, { method: "GET" }),
  post: <T>(path: string, body?: unknown) => request<T>(path, { method: "POST", body }),
  put: <T>(path: string, body?: unknown) => request<T>(path, { method: "PUT", body }),
  patch: <T>(path: string, body?: unknown) => request<T>(path, { method: "PATCH", body }),
  delete: <T>(path: string) => request<T>(path, { method: "DELETE" }),
};
