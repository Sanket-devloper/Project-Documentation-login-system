export interface FieldErrors {
  [field: string]: string | undefined;
}

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validateEmail(email: string): string | undefined {
  if (!email.trim()) return "Email is required.";
  if (!EMAIL_REGEX.test(email)) return "Enter a valid email address.";
  return undefined;
}

/**
 * Login password validation intentionally stays light — the backend is the
 * source of truth for whether credentials are correct. We only check that
 * something was typed.
 */
export function validateLoginPassword(password: string): string | undefined {
  if (!password) return "Password is required.";
  return undefined;
}

/**
 * Registration gets a minimum-length rule for basic UX guidance. This does
 * NOT replace server-side password policy enforcement.
 */
export function validateRegisterPassword(password: string): string | undefined {
  if (!password) return "Password is required.";
  if (password.length < 8) return "Password must be at least 8 characters.";
  return undefined;
}

export function validateName(name: string): string | undefined {
  if (!name.trim()) return "Name is required.";
  return undefined;
}

export function validateConfirmPassword(
  password: string,
  confirmPassword: string
): string | undefined {
  if (!confirmPassword) return "Please confirm your password.";
  if (password !== confirmPassword) return "Passwords do not match.";
  return undefined;
}
