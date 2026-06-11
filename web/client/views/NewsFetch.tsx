'use client'

import { motion } from 'framer-motion'
import { Globe, Tag, Zap, Newspaper, Info } from 'lucide-react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button, Select, Card, Badge, EmptyState } from '@/components/ui'
import { COUNTRIES, CATEGORIES } from '@/types'
import { useAppStore } from '@/store'
import { fetchNews } from '@/services/api'
import { cn, formatRelativeTime } from '@/lib/utils'

export function NewsFetch() {
  const queryClient = useQueryClient()
  const {
    selectedCountry,
    setSelectedCountry,
    selectedCategory,
    setSelectedCategory,
    articles,
    setArticles,
    selectedArticleId,
    setSelectedArticleId,
  } = useAppStore()

  const fetchMutation = useMutation({
    mutationFn: () => fetchNews(selectedCountry, selectedCategory),
    onSuccess: (items) => {
      setArticles(items)
      setSelectedArticleId(items[0]?.id ?? null)
      void queryClient.invalidateQueries({ queryKey: ['dashboardStats'] })
      void queryClient.invalidateQueries({ queryKey: ['analytics'] })
      void queryClient.invalidateQueries({ queryKey: ['storyClusters'] })
      void queryClient.invalidateQueries({ queryKey: ['sources'] })
      void queryClient.invalidateQueries({ queryKey: ['breaking'] })
      void queryClient.invalidateQueries({ queryKey: ['duplicates'] })
      void queryClient.invalidateQueries({ queryKey: ['sourceBalance'] })
      void queryClient.invalidateQueries({ queryKey: ['timelines'] })
      void queryClient.invalidateQueries({ queryKey: ['history'] })
      void queryClient.invalidateQueries({ queryKey: ['allArticles'] })
    },
  })

  const handleFetch = async () => {
    await fetchMutation.mutateAsync()
  }

  const getSentimentBorder = (score?: number) => {
    if (score === undefined) return 'border-l-border'
    if (score > 0.15) return 'border-l-semantic-success'
    if (score < -0.15) return 'border-l-semantic-danger'
    return 'border-l-text-subtle'
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="News Fetch"
        subtitle="Fetch a live feed by country and category, then drive the rest of the workspace from the selected article."
      />

      {/* Controls Card */}
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
      >
        <div className="rounded-lg border border-border bg-surface/80 backdrop-blur-sm p-6">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-end">
            <div className="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4">
              <Select
                label="Country"
                value={selectedCountry}
                onChange={(e) => setSelectedCountry(e.target.value as typeof selectedCountry)}
                options={COUNTRIES.map((country) => ({ value: country.value, label: country.label }))}
              />
              <Select
                label="Category"
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value as typeof selectedCategory)}
                options={CATEGORIES.map((category) => ({ value: category.value, label: category.label }))}
              />
            </div>
            <Button
              onClick={handleFetch}
              loading={fetchMutation.isPending}
              icon={<Zap className="w-4 h-4" />}
              className="w-full lg:w-auto bg-accent-amber text-background hover:bg-yellow-400 shadow-glow-amber font-semibold"
            >
              Fetch News
            </Button>
          </div>

          <div className="mt-4 pt-4 border-t border-border/50 flex flex-wrap items-center gap-2 text-sm text-text-muted">
            <Globe className="w-4 h-4 text-text-subtle" />
            <span>{COUNTRIES.find((country) => country.value === selectedCountry)?.label}</span>
            <span className="text-text-subtle">|</span>
            <Tag className="w-4 h-4 text-text-subtle" />
            <span>{CATEGORIES.find((category) => category.value === selectedCategory)?.label}</span>
            {fetchMutation.isPending ? <Badge variant="info">Fetching latest headlines...</Badge> : null}
            {!fetchMutation.isPending && articles.length > 0 ? (
              <Badge variant="success">{articles.length} visible articles</Badge>
            ) : null}
          </div>

          {fetchMutation.isError ? (
            <div className="mt-4 rounded-md border border-semantic-danger/30 bg-semantic-danger/5 backdrop-blur-sm px-4 py-3 text-sm text-semantic-danger">
              Unable to fetch news at the moment.{' '}
              {fetchMutation.error instanceof Error ? fetchMutation.error.message : 'Please try again.'}
            </div>
          ) : null}
        </div>
      </motion.div>

      {/* Article List */}
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.05 }}
      >
        <div className="rounded-lg border border-border bg-surface/80 backdrop-blur-sm p-6 overflow-hidden">
          <div className="flex items-center justify-between gap-3 mb-5">
            <div>
              <h3 className="text-lg font-semibold text-text-primary tracking-wide">Latest Headlines</h3>
              <p className="text-sm text-text-muted mt-0.5">
                Pick an article to drive AI summary and cross-page analysis.
              </p>
            </div>
            {selectedArticleId ? <Badge variant="info">Article selected</Badge> : null}
          </div>

          {articles.length === 0 ? (
            <EmptyState
              icon={<Newspaper className="w-6 h-6" />}
              title="No articles loaded yet"
              description="Fetch a feed to populate the dashboard, AI summary, timelines, duplicates, and source intelligence."
              action={
                <Button
                  onClick={handleFetch}
                  loading={fetchMutation.isPending}
                  icon={<Zap className="w-4 h-4" />}
                  className="bg-accent-amber text-background hover:bg-yellow-400 shadow-glow-amber"
                >
                  Fetch News
                </Button>
              }
            />
          ) : (
            <div className="space-y-3 max-h-[680px] overflow-y-auto pr-1">
              {articles.map((article, index) => {
                const isSelected = article.id === selectedArticleId
                return (
                  <motion.button
                    key={`${article.id}-${article.url}`}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.2, delay: index * 0.02 }}
                    type="button"
                    onClick={() => setSelectedArticleId(article.id)}
                    className={cn(
                      'w-full rounded-lg border-l-2 border bg-surface/80 backdrop-blur-sm p-4 text-left transition-all duration-250',
                      getSentimentBorder(article.sentimentScore),
                      isSelected
                        ? 'border-accent-primary shadow-glow bg-accent-primary/5'
                        : 'border-border hover:border-border-strong hover:bg-surface-elevated/60'
                    )}
                  >
                    <div className="flex items-start gap-4">
                      <div
                        className={cn(
                          'mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-md text-sm font-mono font-semibold tabular-nums transition-colors duration-200',
                          isSelected
                            ? 'bg-accent-primary text-white shadow-glow'
                            : 'bg-surface-alt text-text-secondary'
                        )}
                      >
                        {index + 1}
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="flex flex-wrap items-center gap-2 mb-2">
                          {article.category ? (
                            <span className="inline-flex items-center rounded-full bg-accent-indigo/10 text-accent-indigo px-2.5 py-0.5 text-[11px] font-medium shadow-[0_0_8px_rgba(99,102,241,0.1)]">
                              {article.category}
                            </span>
                          ) : null}
                          {article.sourceName ? (
                            <span className="inline-flex items-center rounded-full bg-accent-cyan/10 text-accent-cyan px-2.5 py-0.5 text-[11px] font-medium shadow-[0_0_8px_rgba(6,182,212,0.1)]">
                              {article.sourceName}
                            </span>
                          ) : null}
                          {article.sentimentScore !== undefined ? (
                            <span
                              className={cn(
                                'inline-flex items-center rounded-full px-2.5 py-0.5 text-[11px] font-medium',
                                article.sentimentScore > 0.15
                                  ? 'bg-semantic-success/10 text-semantic-success shadow-[0_0_8px_rgba(34,197,94,0.1)]'
                                  : article.sentimentScore < -0.15
                                    ? 'bg-semantic-danger/10 text-semantic-danger shadow-[0_0_8px_rgba(239,68,68,0.1)]'
                                    : 'bg-surface-alt text-text-muted'
                              )}
                            >
                              {article.sentimentScore > 0.15
                                ? 'Positive'
                                : article.sentimentScore < -0.15
                                  ? 'Negative'
                                  : 'Neutral'}
                            </span>
                          ) : null}
                        </div>
                        <h4 className="text-sm font-medium text-text-primary leading-6">{article.title}</h4>
                        {article.description ? (
                          <p className="mt-2 text-sm text-text-muted line-clamp-2 leading-relaxed">
                            {article.description}
                          </p>
                        ) : null}
                        <div className="mt-3 flex flex-wrap items-center gap-2 text-[11px] text-text-subtle">
                          <span className="font-mono tabular-nums">{formatRelativeTime(article.publishedAt)}</span>
                          {article.author ? (
                            <>
                              <span className="text-border">|</span>
                              <span>{article.author}</span>
                            </>
                          ) : null}
                          {article.url ? (
                            <>
                              <span className="text-border">|</span>
                              <span className="truncate max-w-[280px]">{article.url}</span>
                            </>
                          ) : null}
                        </div>
                      </div>
                    </div>
                  </motion.button>
                )
              })}
            </div>
          )}
        </div>
      </motion.div>

      {/* Status Bar */}
      {articles.length > 0 ? (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.1 }}
        >
          <div className="rounded-lg border border-border bg-surface/80 backdrop-blur-sm px-5 py-4">
            <div className="flex items-start gap-3">
              <Info className="w-4 h-4 text-accent-primary mt-0.5 shrink-0" />
              <div>
                <p className="text-sm font-medium text-text-secondary">Feed status</p>
                <p className="text-[13px] text-text-muted mt-0.5 leading-relaxed">
                  The latest fetch is now stored in the shared workspace state. Open AI Summary, Analytics, Duplicate
                  Stories, or Story Timeline to work with the current feed.
                </p>
              </div>
            </div>
          </div>
        </motion.div>
      ) : null}
    </div>
  )
}
