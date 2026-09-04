import { InputHTMLAttributes, forwardRef } from "react";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  error?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ error, className = "", id, ...props }, ref) => {
    return (
      <div>
        <input
          ref={ref}
          id={id}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${id}-error` : undefined}
          className={`w-full rounded-lg border px-3 py-2.5 text-sm text-slate-900 placeholder:text-slate-400
            ${error ? "border-red-400 focus:ring-red-400" : "border-slate-300"}
            focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent
            ${className}`}
          {...props}
        />
        {error && (
          <p id={`${id}-error`} role="alert" className="mt-1 text-sm text-red-600">
            {error}
          </p>
        )}
      </div>
    );
  }
);
Input.displayName = "Input";
