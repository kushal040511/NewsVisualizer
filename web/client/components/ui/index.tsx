'use client'

import type { ReactNode, ButtonHTMLAttributes, InputHTMLAttributes } from 'react'
import { cn } from '@/lib/utils'

// ─── Button ──────────────────────────────────────────────────────────────────

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger' | 'amber'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  icon?: ReactNode
}

export function Button({
  variant = 'primary',
  size = 'md',
  loading = false,
  icon,
  children,
  className,
  disabled,
  ...props
}: ButtonProps) {
  const variants = {
    primary:
      'bg-accent-primary text-white hover:bg-blue-500 active:scale-[0.97] shadow-glow',
    secondary:
      'bg-surface-alt/60 text-text-secondary hover:bg-surface-elevated hover:text-text-primary border border-border hover:border-border-strong backdrop-blur-sm',
    ghost:
      'text-text-muted hover:bg-surface/50 hover:text-text-secondary',
    danger:
      'bg-semantic-danger text-white hover:bg-red-500 active:scale-[0.97]',
    amber:
      'bg-accent-amber text-black hover:bg-amber-400 active:scale-[0.97] shadow-glow-amber',
  }

  const sizes = {
    sm: 'px-3 py-1.5 text-xs',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base',
  }

  return (
    <button
      className={cn(
        'inline-flex items-center justify-center gap-2 font-sans font-medium rounded-md',
        'transition-all duration-200 cursor-pointer',
        'disabled:opacity-40 disabled:cursor-not-allowed disabled:shadow-none',
        variants[variant],
        sizes[size],
        className
      )}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? (
        <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
        </svg>
      ) : icon ? (
        <span className="w-4 h-4 flex items-center justify-center">{icon}</span>
      ) : null}
      {children}
    </button>
  )
}

// ─── Input ───────────────────────────────────────────────────────────────────

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  icon?: ReactNode
}

export function Input({ label, error, icon, className, ...props }: InputProps) {
  return (
    <div className="space-y-1.5">
      {label && (
        <label className="block text-sm font-sans font-medium text-text-secondary">
          {label}
        </label>
      )}
      <div className="relative">
        {icon && (
          <div className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted">
            {icon}
          </div>
        )}
        <input
          className={cn(
            'w-full px-4 py-2.5 bg-surface-alt/60 border border-border rounded-md',
            'font-sans text-text-primary placeholder:text-text-subtle',
            'focus:outline-none focus:border-accent-primary focus:shadow-glow',
            'focus:ring-1 focus:ring-accent-primary/30',
            'backdrop-blur-sm transition-all duration-200 cursor-pointer',
            icon && 'pl-10',
            error && 'border-semantic-danger focus:border-semantic-danger focus:shadow-none focus:ring-semantic-danger/30',
            className
          )}
          {...props}
        />
      </div>
      {error && <p className="text-xs font-sans text-semantic-danger">{error}</p>}
    </div>
  )
}

// ─── Select ──────────────────────────────────────────────────────────────────

interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string
  options: { value: string; label: string }[]
}

export function Select({ label, options, className, ...props }: SelectProps) {
  return (
    <div className="space-y-1.5">
      {label && (
        <label className="block text-sm font-sans font-medium text-text-secondary">
          {label}
        </label>
      )}
      <select
        className={cn(
          'w-full px-4 py-2.5 bg-surface-alt/60 border border-border rounded-md',
          'font-sans text-text-primary',
          'focus:outline-none focus:border-accent-primary focus:shadow-glow',
          'focus:ring-1 focus:ring-accent-primary/30',
          'backdrop-blur-sm transition-all duration-200 cursor-pointer',
          className
        )}
        {...props}
      >
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  )
}

// ─── Card ────────────────────────────────────────────────────────────────────

interface CardProps {
  children: ReactNode
  className?: string
  hover?: boolean
}

export function Card({ children, className, hover = false }: CardProps) {
  return (
    <div
      className={cn(
        'bg-surface/80 backdrop-blur-sm border border-border rounded-lg p-5',
        'transition-all duration-200',
        hover && 'hover:border-border-strong hover:shadow-elevated hover:bg-surface/90 cursor-pointer',
        className
      )}
    >
      {children}
    </div>
  )
}

// ─── Badge ───────────────────────────────────────────────────────────────────

interface BadgeProps {
  children: ReactNode
  variant?: 'default' | 'success' | 'warning' | 'danger' | 'info'
  className?: string
}

export function Badge({ children, variant = 'default', className }: BadgeProps) {
  const variants = {
    default: 'bg-surface-alt/80 text-text-secondary border-border',
    success: 'bg-semantic-success/15 text-semantic-success border-semantic-success/30',
    warning: 'bg-semantic-warning/15 text-semantic-warning border-semantic-warning/30',
    danger: 'bg-semantic-danger/15 text-semantic-danger border-semantic-danger/30',
    info: 'bg-accent-primary/15 text-accent-primary border-accent-primary/30',
  }

  return (
    <span
      className={cn(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-mono font-medium',
        'border backdrop-blur-sm',
        variants[variant],
        className
      )}
    >
      {children}
    </span>
  )
}

// ─── Spinner ─────────────────────────────────────────────────────────────────

export function Spinner({ size = 'md' }: { size?: 'sm' | 'md' | 'lg' }) {
  const sizes = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-8 h-8',
  }

  return (
    <svg className={cn('animate-spin text-accent-primary drop-shadow-[0_0_6px_rgba(59,130,246,0.5)]', sizes[size])} viewBox="0 0 24 24">
      <circle className="opacity-20" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
      <path className="opacity-80" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
    </svg>
  )
}

// ─── Empty State ─────────────────────────────────────────────────────────────

interface EmptyStateProps {
  icon?: ReactNode
  title: string
  description?: string
  action?: ReactNode
}

export function EmptyState({ icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      {icon && (
        <div
          className={cn(
            'w-16 h-16 rounded-lg flex items-center justify-center text-text-muted mb-4',
            'bg-gradient-to-br from-accent-primary/10 via-accent-deep/10 to-accent-indigo/10',
            'animate-gradient-shift bg-[length:200%_200%]',
            'border border-border/50'
          )}
        >
          {icon}
        </div>
      )}
      <h3 className="text-lg font-sans font-medium text-text-primary mb-1">{title}</h3>
      {description && (
        <p className="text-sm font-sans text-text-muted max-w-sm mb-4">{description}</p>
      )}
      {action && <div>{action}</div>}
    </div>
  )
}

// ─── Skeleton ────────────────────────────────────────────────────────────────

interface SkeletonProps {
  className?: string
}

export function Skeleton({ className }: SkeletonProps) {
  return (
    <div
      className={cn(
        'bg-gradient-to-r from-surface-alt via-accent-primary/5 to-surface-alt',
        'bg-[length:200%_100%] animate-shimmer rounded-md',
        className
      )}
    />
  )
}
