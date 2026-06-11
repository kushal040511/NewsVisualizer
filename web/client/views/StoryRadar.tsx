'use client'

import { motion } from 'framer-motion'
import { useQuery } from '@tanstack/react-query'
import { Radar, Target, TrendingUp } from 'lucide-react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card, Badge, EmptyState } from '@/components/ui'
import { fetchStoryClusters } from '@/services/api'
import { cn } from '@/lib/utils'

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { staggerChildren: 0.06 },
  },
}

const cardVariants = {
  hidden: { opacity: 0, y: 16 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.3, ease: 'easeOut' as const } },
}

export function StoryRadar() {
  const clustersQuery = useQuery({
    queryKey: ['storyClusters'],
    queryFn: fetchStoryClusters,
  })

  return (
    <div className="space-y-6">
      <PageHeader
        title="Story Radar"
        subtitle="Live narrative clusters built from the current article set."
      />

      {clustersQuery.isError ? (
        <Card className="bg-semantic-danger/5 border-semantic-danger/30 backdrop-blur-sm">
          <p className="text-sm text-semantic-danger">
            {clustersQuery.error instanceof Error ? clustersQuery.error.message : 'Unable to load story clusters.'}
          </p>
        </Card>
      ) : clustersQuery.data && clustersQuery.data.length > 0 ? (
        <motion.div
          variants={containerVariants}
          initial="hidden"
          animate="visible"
          className="grid grid-cols-1 md:grid-cols-2 gap-4"
        >
          {clustersQuery.data.map((cluster, index) => {
            const isFirst = index === 0
            return (
              <motion.div key={cluster.id} variants={cardVariants}>
                <Card
                  hover
                  className={cn(
                    'relative overflow-hidden bg-surface/80 backdrop-blur-sm border-border/50',
                    isFirst && 'shadow-glow border-accent-primary/30'
                  )}
                >
                  {/* Gradient top border */}
                  <div className="absolute top-0 left-0 right-0 h-[2px] bg-gradient-to-r from-accent-primary to-accent-cyan" />

                  {/* Background radar decoration */}
                  <div className="absolute top-0 right-0 w-20 h-20 opacity-[0.06] pointer-events-none">
                    <Radar className="w-full h-full text-accent-primary" />
                  </div>

                  <div className="flex items-start justify-between mb-4 pt-1">
                    <div className="flex items-center gap-3">
                      <div className={cn(
                        'w-10 h-10 rounded-lg flex items-center justify-center',
                        isFirst ? 'bg-accent-primary/15' : 'bg-surface-alt'
                      )}>
                        <Target className={cn('w-5 h-5', isFirst ? 'text-accent-primary' : 'text-text-muted')} />
                      </div>
                      <div>
                        <h3 className="text-base font-medium text-text-primary">{cluster.label}</h3>
                        <p className="text-xs text-text-muted">Narrative Cluster</p>
                      </div>
                    </div>
                    <Badge variant={isFirst ? 'success' : 'default'}>
                      <span className="font-mono">{cluster.confidence}%</span> confidence
                    </Badge>
                  </div>

                  {/* Confidence progress bar */}
                  <div className="mb-4">
                    <div className="h-1.5 rounded-full bg-surface-alt overflow-hidden">
                      <motion.div
                        className="h-full rounded-full bg-gradient-to-r from-accent-primary to-accent-cyan"
                        initial={{ width: 0 }}
                        animate={{ width: `${cluster.confidence}%` }}
                        transition={{ duration: 0.6, delay: index * 0.05 + 0.2, ease: 'easeOut' as const }}
                      />
                    </div>
                  </div>

                  {/* Stat cards */}
                  <div className="grid grid-cols-2 gap-3 mb-4">
                    <div className="bg-surface-alt/60 rounded-lg p-3">
                      <p className="text-xs text-text-muted">Articles</p>
                      <p className="text-2xl font-mono font-semibold text-text-primary">{cluster.articleCount}</p>
                    </div>
                    <div className="bg-surface-alt/60 rounded-lg p-3">
                      <p className="text-xs text-text-muted">Sources</p>
                      <p className="text-2xl font-mono font-semibold text-text-primary">{cluster.sourceCount}</p>
                    </div>
                  </div>

                  {/* Lead headlines */}
                  <div className="space-y-2">
                    <p className="text-xs font-medium text-text-muted uppercase tracking-wide">Lead Headlines</p>
                    {cluster.leadHeadlines.map((headline, headlineIndex) => (
                      <div key={`${cluster.id}-${headlineIndex}`} className="flex items-center gap-2.5 text-sm">
                        <span className="w-1.5 h-1.5 rounded-full bg-accent-primary/70 shrink-0" />
                        <span className="text-text-secondary truncate">{headline}</span>
                      </div>
                    ))}
                  </div>
                </Card>
              </motion.div>
            )
          })}
        </motion.div>
      ) : (
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <EmptyState
            icon={<Radar className="w-6 h-6" />}
            title="No live story clusters yet"
            description="Fetch a broader news set to detect dominant narratives and cluster-level lead headlines."
          />
        </Card>
      )}
    </div>
  )
}
