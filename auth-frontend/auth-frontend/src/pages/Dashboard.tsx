import { useAuth } from "../hooks/useAuth";
import { Button } from "../components/ui/Button";

/**
 * Placeholder protected page. This is where your actual business UI
 * (real estate / e-commerce / SaaS / etc.) takes over — the auth module's
 * job ends here.
 */
export default function Dashboard() {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-12">
      <div className="mx-auto max-w-2xl rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-xl font-semibold text-slate-900">Dashboard</h1>
            <p className="mt-1 text-sm text-slate-500">You're logged in.</p>
          </div>
          <Button variant="secondary" className="w-auto" onClick={() => logout()}>
            Logout
          </Button>
        </div>

        <dl className="mt-8 space-y-3 text-sm">
          <div className="flex justify-between border-b border-slate-100 pb-2">
            <dt className="text-slate-500">Name</dt>
            <dd className="font-medium text-slate-900">{user?.name}</dd>
          </div>
          <div className="flex justify-between border-b border-slate-100 pb-2">
            <dt className="text-slate-500">Email</dt>
            <dd className="font-medium text-slate-900">{user?.email}</dd>
          </div>
          <div className="flex justify-between pb-2">
            <dt className="text-slate-500">Role</dt>
            <dd className="font-medium text-slate-900">{user?.role}</dd>
          </div>
        </dl>
      </div>
    </div>
  );
}
