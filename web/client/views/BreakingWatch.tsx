'use client'

import { motion } from 'framer-motion'
import { useQuery } from '@tanstack/react-query'
import { BellRing, AlertTriangle, Clock } from 'lucide-react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card, EmptyState } from '@/components/ui'
import { fetchBreakingNews } from '@/services/api'
import { cn, formatRelativeTime } from '@/lib/utils'

const urgencyConfig = (score: number) => {
  if (score >= 80) return { border: 'border-l-semantic-danger', bg: 'bg-semantic-danger/10', text: 'text-semantic-danger', glow: 'shadow-[0_0_12px_rgba(248,113,113,0.3)]' }
  if (score >= 60) return { border: 'border-l-semantic-warning', bg: 'bg-semantic-warning/10', text: 'text-semantic-warning', glow: 'shadow-[0_0_12px_rgba(251,191,36,0.3)]' }
  return { border: 'border-l-accent-primary', bg: 'bg-accent-primary/10', text: 'text-accent-primary', glow: 'shadow-[0_0_12px_rgba(59,130,246,0.2)]' }
}

export function BreakingWatch() {
  const breakingQuery = useQuery({
    queryKey: ['breaking'],
    queryFn: fetchBreakingNews,
  })

  return (
    <div className="space-y-6">
      <PageHeader
        title="Breaking Watch"
        subtitle="Urgent and fast-moving headlines scored from recency, tone, and alert keywords."
      />

      {breakingQuery.isError ? (
        <Card className="border-semantic-danger/30 bg-semantic-danger/5 backdrop-blur-sm">
          <p className="text-sm text-semantic-danger">
            {breakingQuery.error instanceof Error ? breakingQuery.error.message : 'Unable to load breaking headlines.'}
          </p>
        </Card>
      ) : breakingQuery.data && breakingQuery.data.length > 0 ? (
        <div className="grid grid-cols-1 gap-4">
          {breakingQuery.data.map((item, index) => {
            const config = urgencyConfig(item.urgencyScore)
            return (
              <motion.div
                key={item.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3, delay: index * 0.05 }}
              >
                <Card className={cn(
                  'border-l-4 bg-surface/80 backdrop-blur-sm border-border/50',
                  'hover:border-border-strong transition-all duration-300',
                  config.border
                )}>
                  <div className="flex items-start gap-4">
                    <div className={cn(
                      'w-11 h-11 rounded-lg flex items-center justify-center flex-shrink-0 backdrop-blur-sm border border-border/20',
                      config.bg
                    )}>
                      <BellRing className={cn('w-5 h-5', config.text)} />
                    </div>

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <h3 className="text-base font-medium text-text-primary">{item.headline}</h3>
                        {item.urgencyScore >= 80 && (
                          <span className="relative flex h-2.5 w-2.5">
                            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-semantic-danger opacity-75" />
                            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-semantic-danger" />
                          </span>
                        )}
                      </div>
                      <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-text-muted">
                        <span className="text-text-secondary">{item.source}</span>
                        <span className="text-border-strong">|</span>
                        <span>{item.reason}</span>
                      </div>
                    </div>

                    <div className="text-right flex-shrink-0">
                      <div className={cn(
                        'inline-flex items-center gap-2 px-3 py-1.5 rounded-lg mb-2',
                        config.bg, config.glow
                      )}>
                        <AlertTriangle className={cn('w-4 h-4', config.text)} />
                        <span className={cn('text-xl font-bold font-mono', config.text)}>
                          {item.urgencyScore}
                        </span>
                      </div>
                      <div className="flex items-center gap-1.5 text-text-muted justify-end">
                        <Clock className="w-3 h-3" />
                        <span className="text-xs font-mono">{formatRelativeTime(item.publishTime)}</span>
                      </div>
                    </div>
                  </div>
                </Card>
              </motion.div>
            )
          })}
        </div>
      ) : (
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <EmptyState
            icon={<BellRing className="w-6 h-6" />}
            title="No breaking items detected"
            description="Once the feed contains higher-urgency headlines, they will surface here automatically."
          />
        </Card>
      )}
    </div>
  )
}
