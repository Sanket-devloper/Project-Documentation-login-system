import { AuthLayout } from "../components/auth/AuthLayout";
import { LoginForm } from "../components/auth/LoginForm";

export default function Login() {
  return (
    <AuthLayout title="Welcome back" subtitle="Login to continue">
      <LoginForm />
    </AuthLayout>
  );
}
