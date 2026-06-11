'use client'

import { motion } from 'framer-motion'
import { useQuery } from '@tanstack/react-query'
import { Clock, Radio, Newspaper, ArrowUpRight, MessageCircle, RefreshCw } from 'lucide-react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card, Badge, EmptyState } from '@/components/ui'
import { fetchTimelines } from '@/services/api'
import { cn, formatRelativeTime } from '@/lib/utils'

const eventTypeConfig = {
  initial_report: { icon: Radio, color: 'text-accent-primary', bg: 'bg-accent-primary/15 border-accent-primary/30', glow: 'shadow-[0_0_8px_rgba(59,130,246,0.3)]', label: 'Initial Report' },
  rapid_followup: { icon: RefreshCw, color: 'text-accent-indigo', bg: 'bg-accent-indigo/15 border-accent-indigo/30', glow: 'shadow-[0_0_8px_rgba(99,102,241,0.3)]', label: 'Rapid Follow-up' },
  new_source: { icon: Newspaper, color: 'text-accent-cyan', bg: 'bg-accent-cyan/15 border-accent-cyan/30', glow: 'shadow-[0_0_8px_rgba(34,211,238,0.3)]', label: 'New Source' },
  escalation: { icon: ArrowUpRight, color: 'text-semantic-warning', bg: 'bg-semantic-warning/15 border-semantic-warning/30', glow: 'shadow-[0_0_8px_rgba(251,191,36,0.3)]', label: 'Escalation' },
  tone_shift: { icon: MessageCircle, color: 'text-chart-balance', bg: 'bg-chart-balance/15 border-chart-balance/30', glow: 'shadow-[0_0_8px_rgba(168,85,247,0.3)]', label: 'Tone Shift' },
  followup: { icon: Clock, color: 'text-text-muted', bg: 'bg-surface-alt/60 border-border/30', glow: '', label: 'Follow-up' },
} as const

export function StoryTimeline() {
  const timelinesQuery = useQuery({
    queryKey: ['timelines'],
    queryFn: fetchTimelines,
  })

  return (
    <div className="space-y-6">
      <PageHeader
        title="Story Timeline"
        subtitle="Track how coverage evolves over time as new outlets join a narrative."
      />

      {timelinesQuery.isError ? (
        <Card className="border-semantic-danger/30 bg-semantic-danger/5 backdrop-blur-sm">
          <p className="text-sm text-semantic-danger">
            {timelinesQuery.error instanceof Error ? timelinesQuery.error.message : 'Unable to load story timelines.'}
          </p>
        </Card>
      ) : timelinesQuery.data && timelinesQuery.data.length > 0 ? (
        <div className="grid grid-cols-1 gap-6">
          {timelinesQuery.data.map((timeline, index) => (
            <motion.div
              key={timeline.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: index * 0.05 }}
            >
              <Card className="bg-surface/80 backdrop-blur-sm border-border/50 hover:border-border-strong transition-all duration-300">
                <div className="flex items-center justify-between mb-6 gap-3">
                  <div>
                    <h3 className="text-lg font-medium text-text-primary">{timeline.label}</h3>
                    <p className="text-sm font-mono text-text-muted mt-0.5">{timeline.coverageWindow}</p>
                  </div>
                  <Badge variant="info" className="shadow-[0_0_8px_rgba(59,130,246,0.2)]">
                    {timeline.sourceCount} sources
                  </Badge>
                </div>

                <div className="relative">
                  {/* Glowing timeline line */}
                  <div className="absolute left-[15px] top-0 bottom-0 w-0.5 bg-gradient-to-b from-accent-primary via-accent-primary/50 to-accent-primary/10 shadow-[0_0_6px_rgba(59,130,246,0.4)]" />

                  <div className="space-y-6">
                    {timeline.events.map((event, eventIndex) => {
                      const config = eventTypeConfig[event.type]
                      return (
                        <motion.div
                          key={event.id ?? `${timeline.id}-${eventIndex}`}
                          initial={{ opacity: 0, x: -10 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ duration: 0.25, delay: 0.1 + eventIndex * 0.04 }}
                          className="relative flex items-start gap-4 pl-10"
                        >
                          {/* Event dot */}
                          <div className={cn(
                            'absolute left-0 w-8 h-8 rounded-lg border flex items-center justify-center backdrop-blur-sm',
                            config.bg, config.glow
                          )}>
                            <config.icon className={cn('w-4 h-4', config.color)} />
                          </div>

                          <div className="flex-1 pt-0.5 pb-1">
                            <div className="flex flex-wrap items-center gap-2 mb-1.5">
                              <Badge variant="default" className={cn('text-xs', config.glow)}>
                                {config.label}
                              </Badge>
                              <span className="text-xs font-mono text-text-muted">{formatRelativeTime(event.timestamp)}</span>
                            </div>
                            <h4 className="text-sm font-medium text-text-primary">{event.headline}</h4>
                            <p className="text-xs text-text-muted mt-1">{event.source}</p>
                          </div>
                        </motion.div>
                      )
                    })}
                  </div>
                </div>
              </Card>
            </motion.div>
          ))}
        </div>
      ) : (
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <EmptyState
            icon={<Clock className="w-6 h-6" />}
            title="No story timelines yet"
            description="Fetch a live feed to assemble chronological story development across sources."
          />
        </Card>
      )}
    </div>
  )
}
