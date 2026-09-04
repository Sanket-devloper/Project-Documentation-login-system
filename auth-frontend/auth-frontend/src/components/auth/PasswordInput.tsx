import { InputHTMLAttributes, forwardRef, useState } from "react";
import { Eye, EyeOff } from "lucide-react";

interface PasswordInputProps extends InputHTMLAttributes<HTMLInputElement> {
  error?: string;
}

export const PasswordInput = forwardRef<HTMLInputElement, PasswordInputProps>(
  ({ error, className = "", id, ...props }, ref) => {
    const [visible, setVisible] = useState(false);

    return (
      <div>
        <div className="relative">
          <input
            ref={ref}
            id={id}
            type={visible ? "text" : "password"}
            aria-invalid={Boolean(error)}
            aria-describedby={error ? `${id}-error` : undefined}
            className={`w-full rounded-lg border px-3 py-2.5 pr-10 text-sm text-slate-900 placeholder:text-slate-400
              ${error ? "border-red-400 focus:ring-red-400" : "border-slate-300"}
              focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent
              ${className}`}
            {...props}
          />
          <button
            type="button"
            onClick={() => setVisible((v) => !v)}
            className="absolute inset-y-0 right-0 flex items-center px-3 text-slate-400 hover:text-slate-600"
            aria-label={visible ? "Hide password" : "Show password"}
            tabIndex={-1}
          >
            {visible ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        </div>
        {error && (
          <p id={`${id}-error`} role="alert" className="mt-1 text-sm text-red-600">
            {error}
          </p>
        )}
      </div>
    );
  }
);
PasswordInput.displayName = "PasswordInput";
