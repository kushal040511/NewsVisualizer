'use client'

import { motion } from 'framer-motion'
import { useQuery } from '@tanstack/react-query'
import { Scale, Shield, TrendingUp } from 'lucide-react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card, Badge, EmptyState } from '@/components/ui'
import { fetchSourceBalance } from '@/services/api'
import { cn } from '@/lib/utils'

const riskColor = (risk: string) => {
  if (risk === 'Low') return { variant: 'success' as const, glow: 'shadow-[0_0_20px_rgba(52,211,153,0.15)]' }
  if (risk === 'Moderate') return { variant: 'warning' as const, glow: 'shadow-[0_0_20px_rgba(251,191,36,0.15)]' }
  return { variant: 'danger' as const, glow: 'shadow-[0_0_20px_rgba(248,113,113,0.15)]' }
}

export function SourceBalance() {
  const balanceQuery = useQuery({
    queryKey: ['sourceBalance'],
    queryFn: fetchSourceBalance,
  })

  const balance = balanceQuery.data

  return (
    <div className="space-y-6">
      <PageHeader
        title="Source Balance"
        subtitle="Coverage concentration, dominant source pressure, and tone spread across the feed."
      />

      {balanceQuery.isError ? (
        <Card className="border-semantic-danger/30 bg-semantic-danger/5 backdrop-blur-sm">
          <p className="text-sm text-semantic-danger">
            {balanceQuery.error instanceof Error ? balanceQuery.error.message : 'Unable to load source balance.'}
          </p>
        </Card>
      ) : balance && balance.sources.length > 0 ? (
        <>
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.35 }}
          >
            <Card className={cn(
              'bg-surface/80 backdrop-blur-sm border-border/50',
              'bg-gradient-to-br from-surface/90 via-surface-elevated/60 to-surface/90'
            )}>
              <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
                <div className="flex items-center gap-5">
                  <div className="relative">
                    <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-accent-deep/30 to-accent-primary/20 backdrop-blur-sm border border-accent-primary/25 flex items-center justify-center shadow-[0_0_30px_rgba(59,130,246,0.2)]">
                      <Scale className="w-8 h-8 text-accent-light" />
                    </div>
                    <div className="absolute -inset-1 rounded-2xl bg-accent-primary/10 blur-xl -z-10" />
                  </div>
                  <div>
                    <p className="text-xs uppercase tracking-widest text-text-muted mb-1">Balance Score</p>
                    <div className="flex items-baseline gap-1">
                      <span className="text-5xl font-bold font-mono text-text-primary drop-shadow-[0_0_12px_rgba(59,130,246,0.3)]">
                        {balance.score}
                      </span>
                      <span className="text-xl font-mono text-text-muted">/100</span>
                    </div>
                  </div>
                </div>

                <div className="flex flex-col items-start lg:items-end gap-3">
                  <Badge
                    variant={riskColor(balance.coverageRisk).variant}
                    className={riskColor(balance.coverageRisk).glow}
                  >
                    <Shield className="w-3 h-3 mr-1" />
                    {balance.coverageRisk} risk
                  </Badge>
                  <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-surface-alt/40 border border-border/20">
                    <TrendingUp className="w-3.5 h-3.5 text-text-muted" />
                    <span className="text-xs text-text-muted">Dominant:</span>
                    <span className="text-xs font-medium text-text-secondary">{balance.dominantSource}</span>
                    <span className="text-xs font-mono text-accent-amber">({balance.dominantShare}%)</span>
                  </div>
                </div>
              </div>
            </Card>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.35, delay: 0.06 }}
          >
            <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
              <div className="flex items-center justify-between mb-5">
                <h3 className="text-sm font-medium text-text-primary">Source Coverage Breakdown</h3>
                <Badge variant="info" className="shadow-[0_0_8px_rgba(59,130,246,0.2)]">
                  Tone spread: {balance.toneSpread}
                </Badge>
              </div>
              <div className="space-y-4">
                {balance.sources.map((source, index) => (
                  <div key={source.name} className="grid grid-cols-[minmax(0,140px)_1fr_auto_auto] gap-4 items-center">
                    <span className="text-sm text-text-secondary">{source.name}</span>
                    <div className="h-7 bg-surface-alt/40 rounded-lg overflow-hidden border border-border/20">
                      <motion.div
                        initial={{ width: 0 }}
                        animate={{ width: `${Math.min(100, source.share)}%` }}
                        transition={{ duration: 0.5, delay: 0.15 + index * 0.05, ease: 'easeOut' as const }}
                        className="h-full rounded-lg bg-gradient-to-r from-accent-deep via-accent-primary to-accent-light"
                      />
                    </div>
                    <span className="text-sm font-mono font-medium text-text-primary">{source.share}%</span>
                    <Badge variant={source.tone === 'Positive' ? 'success' : source.tone === 'Negative' ? 'danger' : 'default'}>
                      {source.tone}
                    </Badge>
                  </div>
                ))}
              </div>
            </Card>
          </motion.div>
        </>
      ) : (
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <EmptyState
            icon={<Scale className="w-6 h-6" />}
            title="No source balance data yet"
            description="Fetch articles first so the platform can compute source concentration and balance."
          />
        </Card>
      )}
    </div>
  )
}
