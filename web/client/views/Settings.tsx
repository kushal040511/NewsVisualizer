'use client'

import { motion } from 'framer-motion'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Server, Database, Trash2, Info, Wifi, Palette, Globe, Tag } from 'lucide-react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card, Badge, Button } from '@/components/ui'
import { clearAllArticles, clearSearchHistory, fetchAllArticles, fetchSearchHistory, fetchSettings } from '@/services/api'
import { useAppStore } from '@/store'

export function Settings() {
  const queryClient = useQueryClient()
  const { setArticles, setSelectedArticleId } = useAppStore()
  const settingsQuery = useQuery({
    queryKey: ['settings'],
    queryFn: fetchSettings,
  })
  const articlesQuery = useQuery({
    queryKey: ['allArticles'],
    queryFn: fetchAllArticles,
  })
  const historyQuery = useQuery({
    queryKey: ['history'],
    queryFn: fetchSearchHistory,
  })

  const clearArticlesMutation = useMutation({
    mutationFn: clearAllArticles,
    onSuccess: () => {
      setArticles([])
      setSelectedArticleId(null)
      void queryClient.invalidateQueries({ queryKey: ['allArticles'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboardStats'] })
      void queryClient.invalidateQueries({ queryKey: ['analytics'] })
      void queryClient.invalidateQueries({ queryKey: ['storyClusters'] })
      void queryClient.invalidateQueries({ queryKey: ['sources'] })
      void queryClient.invalidateQueries({ queryKey: ['breaking'] })
      void queryClient.invalidateQueries({ queryKey: ['duplicates'] })
      void queryClient.invalidateQueries({ queryKey: ['sourceBalance'] })
      void queryClient.invalidateQueries({ queryKey: ['timelines'] })
    },
  })

  const clearHistoryMutation = useMutation({
    mutationFn: clearSearchHistory,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['history'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboardStats'] })
    },
  })

  const settings = settingsQuery.data
  const articleCount = articlesQuery.data?.length ?? 0
  const historyCount = historyQuery.data?.length ?? 0

  const providerRows = [
    {
      icon: Wifi,
      label: 'News API Provider',
      value: settings?.newsapiStatus ?? 'Loading...',
      badge: 'connected',
      badgeVariant: 'success' as const,
      dotColor: 'bg-semantic-success shadow-[0_0_8px_rgba(52,211,153,0.6)]',
    },
    {
      icon: Globe,
      label: 'Translation Provider',
      value: settings?.translationProvider ?? 'Loading...',
      badge: 'active',
      badgeVariant: 'info' as const,
      dotColor: 'bg-accent-primary shadow-[0_0_8px_rgba(59,130,246,0.6)]',
    },
    {
      icon: Palette,
      label: 'Theme',
      value: settings?.theme ?? 'dark',
      badge: 'current',
      badgeVariant: 'default' as const,
      dotColor: 'bg-accent-indigo shadow-[0_0_8px_rgba(99,102,241,0.6)]',
    },
    {
      icon: Tag,
      label: 'Application Version',
      value: settings?.appVersion ?? '2.0.0',
      badge: 'stable',
      badgeVariant: 'default' as const,
      dotColor: 'bg-text-muted shadow-[0_0_4px_rgba(100,116,139,0.4)]',
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Settings"
        subtitle="Provider diagnostics, workspace state, and maintenance actions for the web frontend."
      />

      {/* Provider Diagnostics */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
      >
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-11 h-11 rounded-lg bg-surface-alt/60 backdrop-blur-sm border border-border/30 flex items-center justify-center">
              <Server className="w-5 h-5 text-accent-light" />
            </div>
            <div>
              <h3 className="text-base font-medium text-text-primary">Provider Diagnostics</h3>
              <p className="text-sm text-text-muted">Backend status, translation provider, and visual theme details.</p>
            </div>
          </div>

          <div className="space-y-1">
            {providerRows.map((row, index) => (
              <div
                key={row.label}
                className={`flex items-center justify-between py-3.5 px-3 rounded-lg hover:bg-surface-alt/30 transition-colors duration-200 ${index < providerRows.length - 1 ? 'border-b border-border/20' : ''}`}
              >
                <div className="flex items-center gap-3">
                  <div className={`w-2.5 h-2.5 rounded-full ${row.dotColor}`} />
                  <div>
                    <p className="text-sm font-medium text-text-primary">{row.label}</p>
                    <p className="text-xs text-text-muted mt-0.5">{row.value}</p>
                  </div>
                </div>
                <Badge variant={row.badgeVariant}>{row.badge}</Badge>
              </div>
            ))}
          </div>
        </Card>
      </motion.div>

      {/* Workspace State */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.06 }}
      >
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-11 h-11 rounded-lg bg-surface-alt/60 backdrop-blur-sm border border-border/30 flex items-center justify-center">
              <Database className="w-5 h-5 text-accent-light" />
            </div>
            <div>
              <h3 className="text-base font-medium text-text-primary">Workspace State</h3>
              <p className="text-sm text-text-muted">Stored articles and persistent action history for the current environment.</p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div className="rounded-lg bg-surface-alt/40 backdrop-blur-sm border border-border/20 p-4 hover:border-border-strong transition-all duration-300">
              <p className="text-[10px] uppercase tracking-widest text-text-muted mb-1.5">Cached Articles</p>
              <p className="text-2xl font-bold font-mono text-text-primary">{articleCount}</p>
            </div>
            <div className="rounded-lg bg-surface-alt/40 backdrop-blur-sm border border-border/20 p-4 hover:border-border-strong transition-all duration-300">
              <p className="text-[10px] uppercase tracking-widest text-text-muted mb-1.5">Search History</p>
              <p className="text-2xl font-bold font-mono text-text-primary">{historyCount}</p>
            </div>
            <div className="rounded-lg bg-surface-alt/40 backdrop-blur-sm border border-border/20 p-4 hover:border-border-strong transition-all duration-300">
              <p className="text-[10px] uppercase tracking-widest text-text-muted mb-1.5">Workspace Mode</p>
              <p className="text-2xl font-bold font-mono text-text-primary">Web</p>
            </div>
          </div>

          <div className="flex flex-wrap gap-3">
            <Button
              variant="secondary"
              icon={<Trash2 className="w-4 h-4" />}
              onClick={() => clearArticlesMutation.mutate()}
              loading={clearArticlesMutation.isPending}
              disabled={articleCount === 0}
            >
              Clear Articles
            </Button>
            <Button
              variant="ghost"
              icon={<Trash2 className="w-4 h-4" />}
              onClick={() => clearHistoryMutation.mutate()}
              loading={clearHistoryMutation.isPending}
              disabled={historyCount === 0}
              className="hover:text-semantic-danger hover:border-semantic-danger/30 transition-colors duration-200"
            >
              Clear History
            </Button>
          </div>
        </Card>
      </motion.div>

      {/* About */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.12 }}
      >
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <div className="flex items-center gap-4">
            <div className="relative">
              <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-accent-deep via-accent-primary to-accent-indigo flex items-center justify-center shadow-[0_0_20px_rgba(59,130,246,0.3)]">
                <span className="text-lg font-bold text-white font-mono">NV</span>
              </div>
              <div className="absolute -inset-1 rounded-xl bg-accent-primary/15 blur-lg -z-10" />
            </div>
            <div>
              <h3 className="text-base font-medium text-text-primary">NewsVisualizer</h3>
              <p className="text-sm text-text-muted">Professional News Analysis Platform</p>
            </div>
            <Badge variant="info" className="ml-auto font-mono shadow-[0_0_8px_rgba(59,130,246,0.2)]">
              v{settings?.appVersion ?? '2.0.0'}
            </Badge>
          </div>
        </Card>
      </motion.div>
    </div>
  )
}
