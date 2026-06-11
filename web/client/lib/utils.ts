import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatDate(dateString?: string): string {
  if (!dateString) return 'Unknown'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    })
  } catch {
    return dateString
  }
}

export function formatTime(dateString?: string): string {
  if (!dateString) return 'Unknown'
  try {
    const date = new Date(dateString)
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return dateString
  }
}

export function formatRelativeTime(dateString?: string): string {
  if (!dateString) return 'Unknown'
  try {
    const date = new Date(dateString)
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (minutes < 1) return 'Just now'
    if (minutes < 60) return `${minutes}m ago`
    if (hours < 24) return `${hours}h ago`
    if (days < 7) return `${days}d ago`
    return formatDate(dateString)
  } catch {
    return dateString
  }
}

export function formatNumber(num: number): string {
  if (num >= 1000000) return `${(num / 1000000).toFixed(1)}M`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}K`
  return num.toString()
}

export function formatPercentage(num: number): string {
  return `${Math.round(num * 100)}%`
}

export function getSentimentColor(score?: number): string {
  if (score === undefined) return 'text-text-muted'
  if (score > 0.2) return 'text-semantic-success'
  if (score < -0.2) return 'text-semantic-danger'
  return 'text-text-secondary'
}

export function getUrgencyColor(score: number): string {
  if (score >= 80) return 'text-semantic-danger bg-semantic-danger/10'
  if (score >= 60) return 'text-semantic-warning bg-semantic-warning/10'
  return 'text-text-secondary bg-surface-alt'
}