'use client'

import { motion } from 'framer-motion'
import { useQuery } from '@tanstack/react-query'
import { Binoculars, Clock, TrendingUp } from 'lucide-react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card, Badge, EmptyState } from '@/components/ui'
import { fetchSources } from '@/services/api'
import { cn } from '@/lib/utils'

const toneColor = (tone: string) => {
  if (tone === 'Positive') return 'border-l-semantic-success'
  if (tone === 'Negative') return 'border-l-semantic-danger'
  return 'border-l-accent-primary'
}

export function SourceMonitor() {
  const sourcesQuery = useQuery({
    queryKey: ['sources'],
    queryFn: fetchSources,
  })

  return (
    <div className="space-y-6">
      <PageHeader
        title="Source Monitor"
        subtitle="Compare outlet volume, tone, and freshness across the current feed."
      />

      {sourcesQuery.isError ? (
        <Card className="border-semantic-danger/30 bg-semantic-danger/5 backdrop-blur-sm">
          <p className="text-sm text-semantic-danger">
            {sourcesQuery.error instanceof Error ? sourcesQuery.error.message : 'Unable to load source monitoring data.'}
          </p>
        </Card>
      ) : sourcesQuery.data && sourcesQuery.data.length > 0 ? (
        <div className="grid grid-cols-1 gap-4">
          {sourcesQuery.data.map((source, index) => (
            <motion.div
              key={source.name}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.3, delay: index * 0.05 }}
            >
              <Card
                hover
                className={cn(
                  'border-l-4 bg-surface/80 backdrop-blur-sm border-border/50',
                  'hover:border-border-strong hover:shadow-glow transition-all duration-300',
                  toneColor(source.averageTone)
                )}
              >
                <div className="flex items-center gap-5">
                  <div className="w-12 h-12 rounded-lg bg-surface-alt/60 backdrop-blur-sm border border-border/30 flex items-center justify-center flex-shrink-0">
                    <Binoculars className="w-5 h-5 text-accent-light" />
                  </div>

                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2.5 mb-1.5">
                      <h3 className="text-base font-medium text-text-primary">{source.name}</h3>
                      <Badge variant={source.averageTone === 'Positive' ? 'success' : source.averageTone === 'Negative' ? 'danger' : 'default'}>
                        {source.averageTone}
                      </Badge>
                    </div>
                    <p className="text-sm text-text-muted truncate">{source.latestHeadline}</p>
                  </div>

                  <div className="text-right flex-shrink-0">
                    <p className="text-lg font-semibold font-mono text-text-primary">{source.articleCount}</p>
                    <p className="text-xs text-text-muted">articles</p>
                  </div>

                  <div className="flex-shrink-0">
                    <Badge variant="default" className="gap-1.5">
                      <Clock className="w-3 h-3 text-accent-light" />
                      <span className="font-mono text-xs">{source.freshness}</span>
                    </Badge>
                  </div>

                  <div className="w-28 flex-shrink-0">
                    <div className="flex items-center justify-between mb-1.5">
                      <div className="flex items-center gap-1">
                        <TrendingUp className="w-3 h-3 text-text-muted" />
                        <span className="text-xs text-text-muted">Share</span>
                      </div>
                      <span className="text-xs font-mono font-medium text-accent-light">{source.coverageShare}%</span>
                    </div>
                    <div className="h-2 bg-surface-alt/60 rounded-full overflow-hidden border border-border/20">
                      <motion.div
                        initial={{ width: 0 }}
                        animate={{ width: `${Math.min(100, source.coverageShare)}%` }}
                        transition={{ duration: 0.6, delay: 0.2 + index * 0.05, ease: 'easeOut' as const }}
                        className="h-full rounded-full bg-gradient-to-r from-accent-deep to-accent-primary"
                      />
                    </div>
                  </div>
                </div>
              </Card>
            </motion.div>
          ))}
        </div>
      ) : (
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <EmptyState
            icon={<Binoculars className="w-6 h-6" />}
            title="No source monitoring data yet"
            description="Fetch a set of articles to compare outlets by volume, tone, and freshness."
          />
        </Card>
      )}
    </div>
  )
}
