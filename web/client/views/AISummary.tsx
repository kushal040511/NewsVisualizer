'use client'

import { useMemo, useState } from 'react'
import { motion } from 'framer-motion'
import { Sparkles, Link, FileText, Zap, Clock, Newspaper } from 'lucide-react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button, Input, Card, Badge, EmptyState } from '@/components/ui'
import { useAppStore } from '@/store'
import { generateFeedBriefing, generateSummary, summarizeArticle } from '@/services/api'
import type { AISummary as AISummaryResult } from '@/types'
import { formatRelativeTime } from '@/lib/utils'

export function AISummary() {
  const [url, setUrl] = useState('')
  const [result, setResult] = useState<AISummaryResult | null>(null)
  const queryClient = useQueryClient()
  const { selectedArticleId, articles } = useAppStore()

  const selectedArticle = useMemo(
    () => articles.find((article) => article.id === selectedArticleId) ?? null,
    [articles, selectedArticleId]
  )

  const summaryMutation = useMutation({
    mutationFn: generateSummary,
    onSuccess: (data) => {
      setResult(data)
      void queryClient.invalidateQueries({ queryKey: ['history'] })
    },
  })

  const selectedSummaryMutation = useMutation({
    mutationFn: summarizeArticle,
    onSuccess: (data) => {
      setResult(data)
      void queryClient.invalidateQueries({ queryKey: ['history'] })
    },
  })

  const feedBriefingMutation = useMutation({
    mutationFn: generateFeedBriefing,
    onSuccess: (data) => {
      setResult(data)
      void queryClient.invalidateQueries({ queryKey: ['history'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboardStats'] })
    },
  })

  const loading = summaryMutation.isPending || selectedSummaryMutation.isPending || feedBriefingMutation.isPending
  const error = summaryMutation.error || selectedSummaryMutation.error || feedBriefingMutation.error

  const handleGenerate = async () => {
    if (!url.trim()) return
    await summaryMutation.mutateAsync(url.trim())
  }

  const handleSummarizeSelected = async () => {
    if (!selectedArticleId) return
    await selectedSummaryMutation.mutateAsync(selectedArticleId)
  }

  const handleFeedBriefing = async () => {
    await feedBriefingMutation.mutateAsync()
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="AI Summary"
        subtitle="Generate URL summaries, selected-article analysis, and feed-wide newsroom briefings from the active workspace."
      />

      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
      >
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <div className="flex flex-col xl:flex-row gap-4">
            <div className="flex-1">
              <Input
                label="Article URL"
                placeholder="Paste an article URL for direct summarization"
                value={url}
                onChange={(event) => setUrl(event.target.value)}
                icon={<Link className="w-4 h-4" />}
                className="focus-within:shadow-[0_0_12px_rgba(59,130,246,0.15)] transition-shadow duration-300"
              />
            </div>
            <div className="flex flex-col sm:flex-row gap-2 xl:self-end">
              <Button
                onClick={handleGenerate}
                loading={summaryMutation.isPending}
                icon={<Sparkles className="w-4 h-4" />}
                disabled={!url.trim()}
                className="bg-gradient-to-r from-accent-amber to-accent-amber/80 hover:shadow-glow-amber text-background border-0"
              >
                Generate Summary
              </Button>
              <Button
                variant="secondary"
                onClick={handleSummarizeSelected}
                loading={selectedSummaryMutation.isPending}
                icon={<FileText className="w-4 h-4" />}
                disabled={!selectedArticleId}
              >
                Summarize Selected Article
              </Button>
              <Button
                variant="ghost"
                onClick={handleFeedBriefing}
                loading={feedBriefingMutation.isPending}
                icon={<Zap className="w-4 h-4" />}
                disabled={articles.length === 0}
              >
                Generate Feed Briefing
              </Button>
            </div>
          </div>

          <div className="mt-4 grid gap-3 lg:grid-cols-2">
            <div className="rounded-lg border border-border/30 bg-surface-alt/40 backdrop-blur-sm px-4 py-3">
              <p className="text-[10px] uppercase tracking-widest text-text-muted">Selected article</p>
              {selectedArticle ? (
                <>
                  <p className="mt-2 text-sm font-medium text-text-primary">{selectedArticle.title}</p>
                  <p className="mt-1 text-xs text-text-muted">
                    {selectedArticle.sourceName} <span className="text-border-strong">|</span> {formatRelativeTime(selectedArticle.publishedAt)}
                  </p>
                </>
              ) : (
                <p className="mt-2 text-sm text-text-muted">Pick an article in News Fetch to enable selected-article summarization.</p>
              )}
            </div>
            <div className="rounded-lg border border-border/30 bg-surface-alt/40 backdrop-blur-sm px-4 py-3">
              <p className="text-[10px] uppercase tracking-widest text-text-muted">Current feed</p>
              <p className="mt-2 text-sm font-medium text-text-primary">
                <span className="font-mono text-accent-light">{articles.length}</span> stored articles ready for briefing
              </p>
              <p className="mt-1 text-xs text-text-muted">Feed briefing builds a workspace-level overview from the latest fetched set.</p>
            </div>
          </div>

          {error ? (
            <div className="mt-4 rounded-lg border border-semantic-danger/30 bg-semantic-danger/5 backdrop-blur-sm px-4 py-3 text-sm text-semantic-danger">
              {error instanceof Error ? error.message : 'Unable to generate a summary right now.'}
            </div>
          ) : null}
        </Card>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.06 }}
      >
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50 bg-gradient-to-br from-surface/90 via-surface-elevated/50 to-surface/90">
          <div className="flex items-center justify-between gap-3 mb-5">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-lg bg-accent-primary/15 border border-accent-primary/25 flex items-center justify-center shadow-[0_0_12px_rgba(59,130,246,0.2)]">
                <Sparkles className="w-5 h-5 text-accent-light" />
              </div>
              <div>
                <h3 className="text-base font-medium text-text-primary">AI Analysis Result</h3>
                <p className="text-sm text-text-muted">URL summary, selected article, or feed briefing output.</p>
              </div>
            </div>
            {loading ? (
              <Badge variant="info" className="animate-pulse">Processing...</Badge>
            ) : result ? (
              <Badge variant="success" className="shadow-[0_0_8px_rgba(52,211,153,0.2)]">Ready</Badge>
            ) : null}
          </div>

          {!result ? (
            <EmptyState
              icon={<Newspaper className="w-6 h-6" />}
              title="No summary generated yet"
              description="Use a URL, summarize the selected article from News Fetch, or generate a full feed briefing."
            />
          ) : (
            <div className="space-y-6">
              {result.title ? (
                <div>
                  <p className="text-[10px] uppercase tracking-widest text-text-muted mb-2">Headline</p>
                  <h4 className="text-xl font-semibold text-text-primary leading-8">{result.title}</h4>
                </div>
              ) : null}

              <div className="flex flex-wrap gap-2">
                <Badge variant="default" className="font-mono">{result.metadata.wordCount} words</Badge>
                <Badge variant="info" className="font-mono">{result.metadata.readingTime} min read</Badge>
                {result.metadata.source ? <Badge variant="default">{result.metadata.source}</Badge> : null}
                {result.metadata.publishedAt ? <Badge variant="default" className="font-mono">{formatRelativeTime(result.metadata.publishedAt)}</Badge> : null}
                <Badge variant="success" className="font-mono">
                  <Clock className="w-3 h-3 mr-1" />
                  {formatRelativeTime(result.metadata.processedAt)}
                </Badge>
              </div>

              <div>
                <p className="text-[10px] uppercase tracking-widest text-text-muted mb-2">Summary</p>
                <p className="text-sm leading-7 text-text-secondary whitespace-pre-wrap">{result.summary}</p>
              </div>

              <div>
                <p className="text-[10px] uppercase tracking-widest text-text-muted mb-3">Key Insights</p>
                <div className="space-y-2.5">
                  {result.keyInsights.length > 0 ? (
                    result.keyInsights.map((insight, index) => (
                      <div key={`${insight}-${index}`} className="flex items-start gap-3 rounded-lg bg-surface-alt/40 backdrop-blur-sm border border-border/20 px-4 py-3">
                        <div className="mt-1 h-2 w-2 rounded-full bg-accent-primary shadow-[0_0_6px_rgba(59,130,246,0.5)] shrink-0" />
                        <p className="text-sm text-text-secondary">{insight}</p>
                      </div>
                    ))
                  ) : (
                    <p className="text-sm text-text-muted">No key insights were returned for this request.</p>
                  )}
                </div>
              </div>
            </div>
          )}
        </Card>
      </motion.div>
    </div>
  )
}
