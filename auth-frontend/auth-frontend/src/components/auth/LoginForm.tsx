import { FormEvent, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Button } from "../ui/Button";
import { Input } from "../ui/Input";
import { Label } from "../ui/Label";
import { PasswordInput } from "./PasswordInput";
import { useAuth } from "../../hooks/useAuth";
import { validateEmail, validateLoginPassword, FieldErrors } from "../../utils/validation";
import { ApiError } from "../../types/auth";

interface LoginFormProps {
  title?: string;
  subtitle?: string;
  redirectTo?: string;
}

/**
 * Reusable login form. Configure `redirectTo` per project (e.g. "/dashboard"
 * for one app, "/admin" for another) — the component itself has no
 * business-domain knowledge.
 */
export function LoginForm({ redirectTo = "/dashboard" }: LoginFormProps) {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [loading, setLoading] = useState(false);
  const [formError, setFormError] = useState("");
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

  function validate(): boolean {
    const errors: FieldErrors = {
      email: validateEmail(email),
      password: validateLoginPassword(password),
    };
    setFieldErrors(errors);
    return !Object.values(errors).some(Boolean);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setFormError("");
    if (!validate()) return;

    setLoading(true);
    try {
      await login({ email, password });
      const from = (location.state as { from?: Location })?.from?.pathname ?? redirectTo;
      navigate(from, { replace: true });
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-5">
      {formError && (
        <div role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {formError}
        </div>
      )}

      <div>
        <Label htmlFor="email">Email</Label>
        <Input
          id="email"
          type="email"
          autoComplete="email"
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={fieldErrors.email}
        />
      </div>

      <div>
        <Label htmlFor="password">Password</Label>
        <PasswordInput
          id="password"
          autoComplete="current-password"
          placeholder="••••••••"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={fieldErrors.password}
        />
      </div>

      <div className="flex items-center justify-between text-sm">
        <label className="flex items-center gap-2 text-slate-600">
          <input
            type="checkbox"
            checked={rememberMe}
            onChange={(e) => setRememberMe(e.target.checked)}
            className="h-4 w-4 rounded border-slate-300 text-brand-600 focus:ring-brand-500"
          />
          Remember me
        </label>
        <Link to="/forgot-password" className="font-medium text-brand-600 hover:text-brand-700">
          Forgot password?
        </Link>
      </div>

      <Button type="submit" isLoading={loading}>
        {loading ? "Logging in…" : "Login"}
      </Button>

      <p className="text-center text-sm text-slate-500">
        Don&apos;t have an account?{" "}
        <Link to="/register" className="font-medium text-brand-600 hover:text-brand-700">
          Register
        </Link>
      </p>
    </form>
  );
}
