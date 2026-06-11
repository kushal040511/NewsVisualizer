'use client'

import { motion } from 'framer-motion'
import { useQuery } from '@tanstack/react-query'
import { Copy, Network, Users, Repeat2 } from 'lucide-react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card, Badge, EmptyState } from '@/components/ui'
import { fetchDuplicates } from '@/services/api'

export function DuplicateStories() {
  const duplicatesQuery = useQuery({
    queryKey: ['duplicates'],
    queryFn: fetchDuplicates,
  })

  return (
    <div className="space-y-6">
      <PageHeader
        title="Duplicate Stories"
        subtitle="Repeated and syndicated coverage detected from headline-level clustering."
      />

      {duplicatesQuery.isError ? (
        <Card className="border-semantic-danger/30 bg-semantic-danger/5 backdrop-blur-sm">
          <p className="text-sm text-semantic-danger">
            {duplicatesQuery.error instanceof Error ? duplicatesQuery.error.message : 'Unable to load duplicate story data.'}
          </p>
        </Card>
      ) : duplicatesQuery.data && duplicatesQuery.data.length > 0 ? (
        <div className="grid grid-cols-1 gap-4">
          {duplicatesQuery.data.map((item, index) => (
            <motion.div
              key={item.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: index * 0.05 }}
            >
              <Card className="bg-surface/80 backdrop-blur-sm border-border/50 hover:border-border-strong hover:shadow-glow-amber transition-all duration-300">
                <div className="flex items-start gap-4 mb-5">
                  <div className="w-11 h-11 rounded-lg bg-accent-amber/10 backdrop-blur-sm border border-accent-amber/20 flex items-center justify-center flex-shrink-0">
                    <Copy className="w-5 h-5 text-accent-amber" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3 className="text-base font-medium text-text-primary mb-2.5">{item.representativeHeadline}</h3>
                    <Badge
                      variant="warning"
                      className="shadow-[0_0_8px_rgba(245,158,11,0.25)]"
                    >
                      <Repeat2 className="w-3 h-3 mr-1" />
                      {item.syndicationSignal}
                    </Badge>
                  </div>
                  <div className="text-right flex-shrink-0">
                    <div className="inline-flex flex-col items-center px-3 py-2 rounded-lg bg-accent-amber/10 border border-accent-amber/20">
                      <span className="text-2xl font-bold font-mono text-accent-amber">{item.duplicateScore}%</span>
                      <span className="text-[10px] uppercase tracking-wider text-accent-amber/70 mt-0.5">duplicate</span>
                    </div>
                  </div>
                </div>

                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 pt-4 border-t border-border/30">
                  <div className="flex items-center gap-2.5 px-3 py-2 rounded-lg bg-surface-alt/40 border border-border/20">
                    <Network className="w-4 h-4 text-accent-light" />
                    <span className="text-sm text-text-secondary"><span className="font-mono font-medium text-text-primary">{item.sourceCount}</span> sources</span>
                  </div>
                  <div className="flex items-center gap-2.5 px-3 py-2 rounded-lg bg-surface-alt/40 border border-border/20">
                    <Users className="w-4 h-4 text-accent-indigo" />
                    <span className="text-sm text-text-secondary"><span className="font-mono font-medium text-text-primary">{item.articleCount}</span> articles</span>
                  </div>
                  <div className="md:col-span-2">
                    <p className="text-[10px] uppercase tracking-widest text-text-muted mb-2.5">Repeated Headlines</p>
                    <div className="space-y-1.5">
                      {item.repeatedHeadlines.map((headline, headlineIndex) => (
                        <div key={`${item.id}-${headlineIndex}`} className="flex items-start gap-2.5 px-3 py-2 rounded-md bg-surface-alt/30 border border-border/15">
                          <span className="font-mono text-xs text-accent-amber/60 mt-0.5 flex-shrink-0">{String(headlineIndex + 1).padStart(2, '0')}</span>
                          <p className="text-sm text-text-muted line-clamp-1">{headline}</p>
                        </div>
                      ))}
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
            icon={<Copy className="w-6 h-6" />}
            title="No duplicate clusters detected"
            description="Fetch a wider feed to expose syndicated or repeated stories across sources."
          />
        </Card>
      )}
    </div>
  )
}
