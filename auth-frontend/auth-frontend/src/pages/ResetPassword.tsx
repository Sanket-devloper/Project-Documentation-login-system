import { FormEvent, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { AuthLayout } from "../components/auth/AuthLayout";
import { Button } from "../components/ui/Button";
import { Label } from "../components/ui/Label";
import { PasswordInput } from "../components/auth/PasswordInput";
import { validateRegisterPassword, validateConfirmPassword, FieldErrors } from "../utils/validation";
import * as authService from "../services/authService";
import { ApiError } from "../types/auth";

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") ?? "";
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [formError, setFormError] = useState("");
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [loading, setLoading] = useState(false);

  function validate(): boolean {
    const errors: FieldErrors = {
      newPassword: validateRegisterPassword(newPassword),
      confirmPassword: validateConfirmPassword(newPassword, confirmPassword),
    };
    setFieldErrors(errors);
    return !Object.values(errors).some(Boolean);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setFormError("");
    if (!token) {
      setFormError("This reset link is missing or invalid. Please request a new one.");
      return;
    }
    if (!validate()) return;

    setLoading(true);
    try {
      await authService.resetPassword({ token, newPassword });
      navigate("/login", { replace: true });
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout
      title="Reset your password"
      subtitle="Choose a new password for your account"
      footer={
        <Link to="/login" className="font-medium text-brand-600 hover:text-brand-700">
          Back to login
        </Link>
      }
    >
      <form onSubmit={handleSubmit} noValidate className="space-y-5">
        {formError && (
          <div role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            {formError}
          </div>
        )}

        <div>
          <Label htmlFor="newPassword">New password</Label>
          <PasswordInput
            id="newPassword"
            autoComplete="new-password"
            placeholder="At least 8 characters"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            error={fieldErrors.newPassword}
          />
        </div>

        <div>
          <Label htmlFor="confirmPassword">Confirm new password</Label>
          <PasswordInput
            id="confirmPassword"
            autoComplete="new-password"
            placeholder="Re-enter your new password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            error={fieldErrors.confirmPassword}
          />
        </div>

        <Button type="submit" isLoading={loading}>
          {loading ? "Resetting…" : "Reset password"}
        </Button>
      </form>
    </AuthLayout>
  );
}
