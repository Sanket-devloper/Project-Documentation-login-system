import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "../ui/Button";
import { Input } from "../ui/Input";
import { Label } from "../ui/Label";
import { PasswordInput } from "./PasswordInput";
import { useAuth } from "../../hooks/useAuth";
import {
  validateEmail,
  validateName,
  validateRegisterPassword,
  validateConfirmPassword,
  FieldErrors,
} from "../../utils/validation";
import { ApiError } from "../../types/auth";

interface RegisterFormProps {
  redirectTo?: string;
}

export function RegisterForm({ redirectTo = "/dashboard" }: RegisterFormProps) {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [formError, setFormError] = useState("");
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

  function validate(): boolean {
    const errors: FieldErrors = {
      name: validateName(name),
      email: validateEmail(email),
      password: validateRegisterPassword(password),
      confirmPassword: validateConfirmPassword(password, confirmPassword),
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
      await register({ name, email, password });
      navigate(redirectTo, { replace: true });
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
        <Label htmlFor="name">Full name</Label>
        <Input
          id="name"
          autoComplete="name"
          placeholder="Jane Doe"
          value={name}
          onChange={(e) => setName(e.target.value)}
          error={fieldErrors.name}
        />
      </div>

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
          autoComplete="new-password"
          placeholder="At least 8 characters"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={fieldErrors.password}
        />
      </div>

      <div>
        <Label htmlFor="confirmPassword">Confirm password</Label>
        <PasswordInput
          id="confirmPassword"
          autoComplete="new-password"
          placeholder="Re-enter your password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          error={fieldErrors.confirmPassword}
        />
      </div>

      <Button type="submit" isLoading={loading}>
        {loading ? "Creating account…" : "Create account"}
      </Button>

      <p className="text-center text-sm text-slate-500">
        Already have an account?{" "}
        <Link to="/login" className="font-medium text-brand-600 hover:text-brand-700">
          Login
        </Link>
      </p>
    </form>
  );
}
