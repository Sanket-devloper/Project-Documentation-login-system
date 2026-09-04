import { FormEvent, useState } from "react";
import { Link } from "react-router-dom";
import { AuthLayout } from "../components/auth/AuthLayout";
import { Button } from "../components/ui/Button";
import { Input } from "../components/ui/Input";
import { Label } from "../components/ui/Label";
import { validateEmail } from "../utils/validation";
import * as authService from "../services/authService";
import { ApiError } from "../types/auth";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const validationError = validateEmail(email);
    setError(validationError ?? "");
    if (validationError) return;

    setLoading(true);
    try {
      await authService.forgotPassword(email);
      setSubmitted(true);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout
      title="Forgot your password?"
      subtitle="We'll send you a link to reset it"
      footer={
        <Link to="/login" className="font-medium text-brand-600 hover:text-brand-700">
          Back to login
        </Link>
      }
    >
      {submitted ? (
        <div role="status" className="rounded-lg bg-green-50 px-3 py-3 text-sm text-green-700">
          If an account exists for {email}, we&apos;ve sent password reset instructions to it.
        </div>
      ) : (
        <form onSubmit={handleSubmit} noValidate className="space-y-5">
          <div>
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              autoComplete="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={error}
            />
          </div>
          <Button type="submit" isLoading={loading}>
            {loading ? "Sending…" : "Send reset link"}
          </Button>
        </form>
      )}
    </AuthLayout>
  );
}
