'use client'

import { motion } from 'framer-motion'
import {
  Newspaper,
  Brain,
  Heart,
  Users,
  Copy,
  AlertTriangle,
  Activity,
  TrendingUp,
  Zap,
  Layers,
  Sparkles,
} from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card, Badge, Skeleton, EmptyState } from '@/components/ui'
import { cn, formatNumber } from '@/lib/utils'
import { fetchDashboardStats } from '@/services/api'

const metrics = [
  { key: 'totalArticles', label: 'Total Articles', icon: Newspaper, accent: 'border-accent-primary', iconColor: 'text-accent-primary', iconBg: 'bg-accent-primary/10', glowHover: 'hover:shadow-glow' },
  { key: 'analysesDone', label: 'Analyses Done', icon: Brain, accent: 'border-accent-indigo', iconColor: 'text-accent-indigo', iconBg: 'bg-accent-indigo/10', glowHover: 'hover:shadow-[0_0_20px_rgba(99,102,241,0.15)]' },
  { key: 'feedHealth', label: 'Feed Health', icon: Heart, accent: 'border-semantic-success', iconColor: 'text-semantic-success', iconBg: 'bg-semantic-success/10', glowHover: 'hover:shadow-[0_0_20px_rgba(34,197,94,0.15)]' },
  { key: 'sourceDiversity', label: 'Source Diversity', icon: Users, accent: 'border-accent-cyan', iconColor: 'text-accent-cyan', iconBg: 'bg-accent-cyan/10', glowHover: 'hover:shadow-[0_0_20px_rgba(6,182,212,0.15)]' },
  { key: 'duplicatePressure', label: 'Duplicate Pressure', icon: Copy, accent: 'border-semantic-warning', iconColor: 'text-semantic-warning', iconBg: 'bg-semantic-warning/10', glowHover: 'hover:shadow-[0_0_20px_rgba(245,158,11,0.15)]' },
  { key: 'urgencyLevel', label: 'Urgency Level', icon: AlertTriangle, accent: 'border-semantic-danger', iconColor: 'text-semantic-danger', iconBg: 'bg-semantic-danger/10', glowHover: 'hover:shadow-[0_0_20px_rgba(239,68,68,0.15)]' },
  { key: 'recentActivityCount', label: 'Recent Activity', icon: Activity, accent: 'border-accent-light', iconColor: 'text-accent-light', iconBg: 'bg-accent-light/10', glowHover: 'hover:shadow-glow' },
] as const

function MetricCard({
  label,
  icon: Icon,
  accent,
  iconColor,
  iconBg,
  glowHover,
  value,
  delay,
}: {
  label: string
  icon: typeof Newspaper
  accent: string
  iconColor: string
  iconBg: string
  glowHover: string
  value: number
  delay: number
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35, delay }}
    >
      <div
        className={cn(
          'relative rounded-lg border-l-2 bg-surface/80 backdrop-blur-sm border border-border',
          accent,
          glowHover,
          'transition-all duration-300 p-5 group'
        )}
      >
        <div className="flex items-start gap-4">
          <div className={cn('w-11 h-11 rounded-md flex items-center justify-center', iconBg)}>
            <Icon className={cn('w-5 h-5', iconColor)} />
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-[11px] font-medium text-text-muted uppercase tracking-wider">{label}</p>
            <p className="text-2xl font-semibold text-text-primary mt-1 font-mono tracking-tight">
              {formatNumber(value)}
            </p>
            <div className="flex items-center gap-1.5 mt-2">
              <TrendingUp className="w-3 h-3 text-semantic-success" />
              <span className="text-[11px] text-text-subtle">Live metric</span>
            </div>
          </div>
        </div>
      </div>
    </motion.div>
  )
}

function LoadingState() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
      {[...Array(7)].map((_, index) => (
        <div key={index} className="rounded-lg border border-border bg-surface/80 backdrop-blur-sm p-5">
          <div className="flex items-start gap-4">
            <Skeleton className="h-11 w-11 rounded-md" />
            <div className="flex-1 space-y-2">
              <Skeleton className="h-3 w-24" />
              <Skeleton className="h-8 w-16" />
              <Skeleton className="h-3 w-20" />
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

export function Dashboard() {
  const statsQuery = useQuery({
    queryKey: ['dashboardStats'],
    queryFn: fetchDashboardStats,
  })

  if (statsQuery.isLoading) {
    return (
      <div className="space-y-6">
        <PageHeader title="Dashboard" subtitle="Loading newsroom intelligence overview..." />
        <LoadingState />
      </div>
    )
  }

  if (statsQuery.isError) {
    return (
      <div className="space-y-6">
        <PageHeader title="Dashboard" subtitle="Newsroom overview" />
        <div className="rounded-lg border border-semantic-danger/30 bg-semantic-danger/5 backdrop-blur-sm p-5">
          <p className="text-sm text-semantic-danger">
            {statsQuery.error instanceof Error ? statsQuery.error.message : 'Failed to load dashboard statistics.'}
          </p>
        </div>
      </div>
    )
  }

  if (!statsQuery.data) {
    return (
      <div className="space-y-6">
        <PageHeader title="Dashboard" subtitle="Newsroom overview" />
        <Card>
          <EmptyState
            icon={<Newspaper className="w-6 h-6" />}
            title="Dashboard data is not ready yet"
            description="Fetch articles to activate the newsroom command center."
          />
        </Card>
      </div>
    )
  }

  const stats = statsQuery.data
  const concentrationValue = Math.round(stats.concentrationScore ?? 0)
  const isHighConcentration = concentrationValue > 45

  return (
    <div className="space-y-6">
      <PageHeader
        title="Dashboard"
        subtitle="Your newsroom command center with live feed health, source balance, urgency, and duplicate pressure."
      />

      {/* AI Pulse Card */}
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35, delay: 0.05 }}
      >
        <div className="relative rounded-lg p-[1px] bg-gradient-to-r from-accent-primary via-accent-indigo to-accent-amber">
          <div className="rounded-[7px] bg-surface/90 backdrop-blur-sm px-6 py-5">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div className="flex items-center gap-4">
                <div className="relative">
                  <div className="w-11 h-11 rounded-md bg-accent-primary/15 flex items-center justify-center">
                    <Sparkles className="w-5 h-5 text-accent-primary" />
                  </div>
                  <motion.div
                    className="absolute inset-0 rounded-md bg-accent-primary/20"
                    animate={{ scale: [1, 1.4, 1], opacity: [0.5, 0, 0.5] }}
                    transition={{ duration: 2.5, repeat: Infinity, ease: 'easeInOut' as const }}
                  />
                </div>
                <div>
                  <p className="text-sm font-semibold text-text-primary tracking-wide">AI Pulse</p>
                  <p className="text-sm text-text-muted mt-0.5 leading-relaxed">
                    {stats.aiPulseSummary || 'Fetch news to activate feed intelligence.'}
                  </p>
                </div>
              </div>
              <Badge variant={stats.totalArticles > 0 ? 'success' : 'default'}>
                {stats.totalArticles > 0 ? 'Live' : 'Waiting for data'}
              </Badge>
            </div>
          </div>
        </div>
      </motion.div>

      {stats.totalArticles === 0 ? (
        <Card>
          <EmptyState
            icon={<Newspaper className="w-6 h-6" />}
            title="No feed data yet"
            description="Fetch news to populate the dashboard metrics, AI briefing, analytics, timelines, and source intelligence."
          />
        </Card>
      ) : (
        <>
          {/* Metric Cards Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
            {metrics.map((metric, index) => (
              <MetricCard
                key={metric.key}
                label={metric.label}
                icon={metric.icon}
                accent={metric.accent}
                iconColor={metric.iconColor}
                iconBg={metric.iconBg}
                glowHover={metric.glowHover}
                value={Number(stats[metric.key] ?? 0)}
                delay={0.08 + index * 0.04}
              />
            ))}
          </div>

          {/* Source Balance + Concentration Row */}
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.35, delay: 0.2 }}
            className="grid grid-cols-1 xl:grid-cols-3 gap-4"
          >
            {/* Source Balance Snapshot */}
            <div className="xl:col-span-2 rounded-lg border border-border bg-surface/80 backdrop-blur-sm p-6">
              <div className="flex items-center justify-between mb-5">
                <h3 className="text-sm font-semibold text-text-primary tracking-wide">Source Balance Snapshot</h3>
                <Badge
                  variant={
                    stats.sourceBalanceSnapshot?.coverageRisk === 'Low'
                      ? 'success'
                      : stats.sourceBalanceSnapshot?.coverageRisk === 'Moderate'
                        ? 'warning'
                        : 'danger'
                  }
                >
                  {stats.sourceBalanceSnapshot?.coverageRisk ?? 'No data'}
                </Badge>
              </div>
              <div className="space-y-4">
                {stats.sourceBalanceSnapshot?.sources?.slice(0, 5).map((source, index) => (
                  <div key={source.name} className="flex items-center gap-3">
                    <Layers className="w-4 h-4 text-text-subtle shrink-0" />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between mb-1.5">
                        <span className="text-sm text-text-secondary truncate">{source.name}</span>
                        <span className="text-sm text-text-muted font-mono tabular-nums ml-2">
                          {source.share}%
                        </span>
                      </div>
                      <div className="h-2 bg-surface-alt rounded-full overflow-hidden">
                        <motion.div
                          initial={{ width: 0 }}
                          animate={{ width: `${Math.min(100, source.share)}%` }}
                          transition={{ duration: 0.5, delay: 0.25 + index * 0.07 }}
                          className="h-full rounded-full bg-gradient-to-r from-accent-deep to-accent-primary"
                        />
                      </div>
                    </div>
                    <Badge
                      variant={
                        source.tone === 'Positive' ? 'success' : source.tone === 'Negative' ? 'danger' : 'default'
                      }
                    >
                      {source.tone}
                    </Badge>
                  </div>
                ))}
              </div>
            </div>

            {/* Concentration + Narrative Shift */}
            <div className="rounded-lg border border-border bg-surface/80 backdrop-blur-sm p-6 flex flex-col gap-6">
              {/* Concentration */}
              <div>
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-sm font-semibold text-text-primary tracking-wide">Concentration</h3>
                  <span
                    className={cn(
                      'text-2xl font-semibold font-mono tabular-nums',
                      isHighConcentration ? 'text-accent-amber' : 'text-text-primary'
                    )}
                  >
                    {concentrationValue}%
                  </span>
                </div>
                <div
                  className={cn(
                    'h-3 bg-surface-alt rounded-full overflow-hidden',
                    isHighConcentration && 'shadow-glow-amber'
                  )}
                >
                  <motion.div
                    initial={{ width: 0 }}
                    animate={{ width: `${Math.min(100, concentrationValue)}%` }}
                    transition={{ duration: 0.55, delay: 0.35 }}
                    className={cn(
                      'h-full rounded-full',
                      isHighConcentration
                        ? 'bg-gradient-to-r from-accent-amber to-yellow-400'
                        : 'bg-gradient-to-r from-semantic-success to-emerald-400'
                    )}
                  />
                </div>
                <p className="text-[11px] text-text-subtle mt-2">Maximum share held by the dominant source.</p>
              </div>

              {/* Narrative Shift */}
              <div className="rounded-md border border-border-glow bg-surface/60 backdrop-blur-sm px-4 py-4">
                <div className="flex items-center gap-2 mb-2">
                  <Zap className="w-3.5 h-3.5 text-accent-primary" />
                  <p className="text-[11px] uppercase tracking-wider font-medium text-text-muted">Narrative Shift</p>
                </div>
                <p className="text-sm text-text-secondary leading-relaxed">
                  {stats.latestNarrativeShifts || 'No narrative shift detected yet.'}
                </p>
              </div>
            </div>
          </motion.div>
        </>
      )}
    </div>
  )
}
