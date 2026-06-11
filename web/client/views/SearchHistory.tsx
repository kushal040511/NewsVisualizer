'use client'

import { motion } from 'framer-motion'
import { History, Trash2, Search, Sparkles, Languages, Brain } from 'lucide-react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card, Badge, Button, EmptyState, Skeleton } from '@/components/ui'
import { clearSearchHistory, fetchSearchHistory } from '@/services/api'
import { formatRelativeTime } from '@/lib/utils'

const actionIcons: Record<string, typeof Search> = {
  'News Fetch': Search,
  'AI Summary': Sparkles,
  Translation: Languages,
  Define: Brain,
  Detect: Brain,
  Explain: Brain,
}

const actionColors: Record<string, string> = {
  'News Fetch': 'bg-accent-primary/10 text-accent-primary border-accent-primary/25',
  'AI Summary': 'bg-accent-indigo/10 text-accent-indigo border-accent-indigo/25',
  Translation: 'bg-accent-cyan/10 text-accent-cyan border-accent-cyan/25',
  Define: 'bg-semantic-success/10 text-semantic-success border-semantic-success/25',
  Detect: 'bg-semantic-warning/10 text-semantic-warning border-semantic-warning/25',
  Explain: 'bg-chart-balance/10 text-chart-balance border-chart-balance/25',
}

const actionLeftBorder: Record<string, string> = {
  'News Fetch': 'border-l-accent-primary',
  'AI Summary': 'border-l-accent-indigo',
  Translation: 'border-l-accent-cyan',
  Define: 'border-l-semantic-success',
  Detect: 'border-l-semantic-warning',
  Explain: 'border-l-chart-balance',
}

export function SearchHistory() {
  const queryClient = useQueryClient()
  const historyQuery = useQuery({
    queryKey: ['history'],
    queryFn: fetchSearchHistory,
  })

  const clearMutation = useMutation({
    mutationFn: clearSearchHistory,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['history'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboardStats'] })
    },
  })

  return (
    <div className="space-y-6">
      <PageHeader
        title="Search History"
        subtitle="A persistent record of fetches, AI summaries, translations, and analysis actions."
        action={
          <Button
            variant="ghost"
            icon={<Trash2 className="w-4 h-4" />}
            onClick={() => clearMutation.mutate()}
            loading={clearMutation.isPending}
            disabled={historyQuery.data?.length === 0}
            className="hover:text-semantic-danger hover:border-semantic-danger/30 transition-colors duration-200"
          >
            Clear History
          </Button>
        }
      />

      {historyQuery.isLoading ? (
        <div className="space-y-3">
          {[...Array(6)].map((_, index) => (
            <Card key={index} className="bg-surface/80 backdrop-blur-sm border-border/50">
              <div className="flex items-center gap-4">
                <Skeleton className="h-10 w-10 rounded-lg" />
                <div className="flex-1 space-y-2">
                  <Skeleton className="h-4 w-40" />
                  <Skeleton className="h-3 w-72" />
                </div>
                <Skeleton className="h-3 w-16" />
              </div>
            </Card>
          ))}
        </div>
      ) : historyQuery.isError ? (
        <Card className="border-semantic-danger/30 bg-semantic-danger/5 backdrop-blur-sm">
          <p className="text-sm text-semantic-danger">
            {historyQuery.error instanceof Error ? historyQuery.error.message : 'Unable to load history right now.'}
          </p>
        </Card>
      ) : historyQuery.data && historyQuery.data.length > 0 ? (
        <div className="space-y-3">
          {historyQuery.data.map((item, index) => {
            const Icon = actionIcons[item.actionType] ?? Search
            const colorClass = actionColors[item.actionType] ?? 'bg-surface-alt/40 text-text-muted border-border/30'
            const leftBorder = actionLeftBorder[item.actionType] ?? 'border-l-border-strong'
            return (
              <motion.div
                key={item.id}
                initial={{ opacity: 0, x: -16 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.25, delay: index * 0.03 }}
              >
                <Card className={`flex items-center gap-4 border-l-4 bg-surface/80 backdrop-blur-sm border-border/50 hover:border-border-strong transition-all duration-300 ${leftBorder}`}>
                  <div className={`w-10 h-10 rounded-lg border flex items-center justify-center flex-shrink-0 backdrop-blur-sm ${colorClass}`}>
                    <Icon className="w-5 h-5" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex flex-wrap items-center gap-2 mb-1">
                      <h3 className="text-sm font-medium text-text-primary">{item.actionType}</h3>
                      {item.query ? <Badge variant="default">{item.query}</Badge> : null}
                    </div>
                    <p className="text-sm text-text-muted line-clamp-2">{item.resultSummary || item.details || 'No result summary stored.'}</p>
                  </div>
                  <div className="flex items-center gap-1.5 text-text-muted flex-shrink-0">
                    <History className="w-3 h-3" />
                    <span className="text-xs font-mono">{formatRelativeTime(item.createdAt)}</span>
                  </div>
                </Card>
              </motion.div>
            )
          })}
        </div>
      ) : (
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <EmptyState
            icon={<History className="w-6 h-6" />}
            title="No history yet"
            description="Fetching news, generating summaries, and using translation will start populating this activity log."
          />
        </Card>
      )}
    </div>
  )
}
