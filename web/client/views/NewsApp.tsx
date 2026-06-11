'use client'

import { useEffect, useMemo } from 'react'
import { motion } from 'framer-motion'
import { useQuery } from '@tanstack/react-query'
import { ExternalLink, Clock, Newspaper, FileText } from 'lucide-react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card, Badge, Button, EmptyState } from '@/components/ui'
import { fetchAllArticles } from '@/services/api'
import { cn, formatRelativeTime } from '@/lib/utils'
import { useAppStore } from '@/store'

const categoryVariant: Record<string, 'info' | 'success' | 'warning' | 'default'> = {
  business: 'info',
  technology: 'default',
  health: 'success',
  science: 'info',
  sports: 'warning',
  entertainment: 'default',
  general: 'default',
}

const categoryBorder: Record<string, string> = {
  business: 'border-t-accent-primary',
  technology: 'border-t-accent-indigo',
  health: 'border-t-semantic-success',
  science: 'border-t-accent-cyan',
  sports: 'border-t-accent-amber',
  entertainment: 'border-t-accent-light',
  general: 'border-t-border-strong',
}

export function NewsApp() {
  const { selectedArticleId, setSelectedArticleId } = useAppStore()
  const articlesQuery = useQuery({
    queryKey: ['allArticles'],
    queryFn: fetchAllArticles,
  })

  const articles = articlesQuery.data ?? []
  const selectedArticle = useMemo(
    () => articles.find((article) => article.id === selectedArticleId) ?? articles[0] ?? null,
    [articles, selectedArticleId]
  )

  useEffect(() => {
    if (articles.length > 0 && (!selectedArticleId || !articles.some((article) => article.id === selectedArticleId))) {
      setSelectedArticleId(articles[0].id)
    }
    if (articles.length === 0 && selectedArticleId !== null) {
      setSelectedArticleId(null)
    }
  }, [articles, selectedArticleId, setSelectedArticleId])

  const canOpenSource = (() => {
    if (!selectedArticle?.url) return false
    try {
      const parsed = new URL(selectedArticle.url)
      return !parsed.hostname.endsWith('example.com')
    } catch {
      return false
    }
  })()

  const openSelectedSource = () => {
    if (!selectedArticle?.url || !canOpenSource) return
    window.open(selectedArticle.url, '_blank', 'noopener,noreferrer')
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="NewsApp"
        subtitle="Browse the stored feed in a reader-style layout. Click a card to open the article in the in-app reader."
      />

      {articlesQuery.isError ? (
        <Card className="border-semantic-danger/30 bg-semantic-danger/5 backdrop-blur-sm">
          <p className="text-sm text-semantic-danger">
            {articlesQuery.error instanceof Error ? articlesQuery.error.message : 'Unable to load stored articles.'}
          </p>
        </Card>
      ) : articles.length > 0 ? (
        <div className="grid grid-cols-1 xl:grid-cols-[1.35fr_0.95fr] gap-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {articles.slice(0, 24).map((article, index) => {
              const isSelected = article.id === selectedArticle?.id
              const catKey = article.category?.toLowerCase() ?? 'general'
              const hasRealSource = (() => {
                if (!article.url) return false
                try {
                  const parsed = new URL(article.url)
                  return !parsed.hostname.endsWith('example.com')
                } catch {
                  return false
                }
              })()

              return (
                <motion.div
                  key={`${article.id}-${article.url}`}
                  initial={{ opacity: 0, y: 16 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.25, delay: index * 0.02 }}
                >
                  <Card
                    hover
                    className={cn(
                      'h-full flex flex-col border-t-2 bg-surface/80 backdrop-blur-sm transition-all duration-300',
                      categoryBorder[catKey] ?? 'border-t-border-strong',
                      isSelected
                        ? 'border-accent-primary shadow-glow ring-1 ring-accent-primary/30'
                        : 'border-border/50 hover:border-border-strong'
                    )}
                  >
                    <button
                      type="button"
                      onClick={() => setSelectedArticleId(article.id)}
                      className="flex h-full flex-col text-left"
                    >
                      <div className="flex items-center justify-between gap-2 mb-3">
                        {article.category ? (
                          <Badge variant={categoryVariant[catKey] ?? 'default'}>
                            {article.category}
                          </Badge>
                        ) : <span />}
                        {hasRealSource ? <ExternalLink className="w-4 h-4 text-text-muted" /> : <FileText className="w-4 h-4 text-text-muted" />}
                      </div>
                      <h3 className="text-base font-medium text-text-primary mb-2 line-clamp-2">{article.title}</h3>
                      <p className="text-sm text-text-muted mb-4 flex-1 line-clamp-4">
                        {article.description || article.content || 'No article description available.'}
                      </p>
                      <div className="flex flex-wrap items-center justify-between gap-3 pt-3 border-t border-border/30 mt-auto">
                        <div className="flex items-center gap-2 text-xs text-text-muted">
                          <Newspaper className="w-3 h-3" />
                          <span>{article.sourceName || 'Unknown Source'}</span>
                        </div>
                        <div className="flex items-center gap-2 text-xs font-mono text-text-muted">
                          <Clock className="w-3 h-3" />
                          <span>{formatRelativeTime(article.publishedAt)}</span>
                        </div>
                      </div>
                    </button>
                  </Card>
                </motion.div>
              )
            })}
          </div>

          {selectedArticle ? (
            <motion.div
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.3 }}
            >
              <Card className="sticky top-6 bg-surface/80 backdrop-blur-sm border-border/50 bg-gradient-to-br from-surface/90 via-surface-elevated/50 to-surface/90">
                <div className="flex items-center justify-between gap-3 mb-5">
                  <div>
                    <p className="text-[10px] uppercase tracking-widest text-text-muted">Reader</p>
                    <h3 className="text-lg font-semibold text-text-primary mt-1">Selected Article</h3>
                  </div>
                  <div className="flex gap-2">
                    <Badge variant="info">{selectedArticle.sourceName || 'Unknown Source'}</Badge>
                    {selectedArticle.category ? (
                      <Badge variant={categoryVariant[selectedArticle.category.toLowerCase()] ?? 'default'}>
                        {selectedArticle.category}
                      </Badge>
                    ) : null}
                  </div>
                </div>

                <h4 className="text-xl font-semibold text-text-primary leading-8">{selectedArticle.title}</h4>
                <div className="mt-3 flex flex-wrap items-center gap-2 text-xs font-mono text-text-muted">
                  <span>{formatRelativeTime(selectedArticle.publishedAt)}</span>
                  {selectedArticle.author ? (
                    <>
                      <span className="text-border-strong">|</span>
                      <span className="font-sans">{selectedArticle.author}</span>
                    </>
                  ) : null}
                </div>

                {selectedArticle.description ? (
                  <div className="mt-5 rounded-lg border border-border/30 bg-surface-alt/40 backdrop-blur-sm px-4 py-3">
                    <p className="text-sm font-medium text-text-primary mb-2">Lead</p>
                    <p className="text-sm text-text-secondary leading-7">{selectedArticle.description}</p>
                  </div>
                ) : null}

                <div className="mt-5">
                  <p className="text-sm font-medium text-text-primary mb-2">Article</p>
                  <div className="rounded-lg border border-border/30 bg-surface-alt/40 backdrop-blur-sm px-4 py-4">
                    <p className="text-sm text-text-secondary leading-7 whitespace-pre-wrap">
                      {selectedArticle.content || selectedArticle.description || 'No full article content is available for this item yet.'}
                    </p>
                  </div>
                </div>

                <div className="mt-5 flex flex-wrap gap-3">
                  <Button
                    variant="secondary"
                    icon={<FileText className="w-4 h-4" />}
                    onClick={() => setSelectedArticleId(selectedArticle.id)}
                  >
                    Reading in App
                  </Button>
                  {canOpenSource ? (
                    <Button
                      icon={<ExternalLink className="w-4 h-4" />}
                      onClick={openSelectedSource}
                    >
                      Open Source
                    </Button>
                  ) : (
                    <Badge variant="default">Generated article: external source not available</Badge>
                  )}
                </div>
              </Card>
            </motion.div>
          ) : null}
        </div>
      ) : (
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <EmptyState
            icon={<Newspaper className="w-6 h-6" />}
            title="No articles stored yet"
            description="Fetch a feed first to turn the reader surface into a real news browsing experience."
          />
        </Card>
      )}
    </div>
  )
}
