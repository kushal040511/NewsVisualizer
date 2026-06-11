'use client'

import { motion } from 'framer-motion'
import { useQuery } from '@tanstack/react-query'
import { PieChart, BarChart3, TrendingUp, Users, AlertCircle } from 'lucide-react'
import { PieChart as RechartsPie, Pie, Cell, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from 'recharts'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card, Badge, EmptyState, Skeleton } from '@/components/ui'
import { fetchAnalytics } from '@/services/api'

const sentimentPalette = {
  positive: '#4ade80',
  neutral: '#6e6e76',
  negative: '#f87171',
}

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { staggerChildren: 0.06 },
  },
}

const itemVariants = {
  hidden: { opacity: 0, y: 14 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.35, ease: 'easeOut' as const } },
}

function CustomBarTooltip({ active, payload, label }: any) {
  if (!active || !payload?.length) return null
  return (
    <div className="bg-surface/95 backdrop-blur-md border border-border rounded-lg px-4 py-3 shadow-lg">
      <p className="text-xs font-medium text-text-primary mb-1">{label}</p>
      <p className="text-sm font-mono text-accent-primary">
        {payload[0].value} articles
      </p>
    </div>
  )
}

export function Analytics() {
  const analyticsQuery = useQuery({
    queryKey: ['analytics'],
    queryFn: fetchAnalytics,
  })

  if (analyticsQuery.isLoading) {
    return (
      <div className="space-y-6">
        <PageHeader title="Analytics" subtitle="Loading feed intelligence…" />
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {[...Array(2)].map((_, index) => (
            <Card key={index} className="bg-surface/80 backdrop-blur-sm border-border/50">
              <Skeleton className="h-6 w-40 mb-4" />
              <Skeleton className="h-56 w-full" />
            </Card>
          ))}
        </div>
      </div>
    )
  }

  if (analyticsQuery.isError) {
    return (
      <div className="space-y-6">
        <PageHeader title="Analytics" subtitle="Deep insights into your current feed" />
        <Card className="bg-semantic-danger/5 border-semantic-danger/30 backdrop-blur-sm">
          <div className="flex items-center gap-3">
            <AlertCircle className="w-5 h-5 text-semantic-danger shrink-0" />
            <p className="text-sm text-semantic-danger">
              {analyticsQuery.error instanceof Error ? analyticsQuery.error.message : 'Failed to load analytics.'}
            </p>
          </div>
        </Card>
      </div>
    )
  }

  if (!analyticsQuery.data) {
    return (
      <div className="space-y-6">
        <PageHeader title="Analytics" subtitle="Deep insights into your current feed" />
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <EmptyState
            icon={<BarChart3 className="w-6 h-6" />}
            title="Analytics are not ready yet"
            description="Fetch a feed to populate this workspace."
          />
        </Card>
      </div>
    )
  }

  const analytics = analyticsQuery.data
  const totalSentiment = analytics.sentimentDistribution.positive + analytics.sentimentDistribution.neutral + analytics.sentimentDistribution.negative
  const sentimentData = [
    { name: 'Positive', value: analytics.sentimentDistribution.positive, color: sentimentPalette.positive },
    { name: 'Neutral', value: analytics.sentimentDistribution.neutral, color: sentimentPalette.neutral },
    { name: 'Negative', value: analytics.sentimentDistribution.negative, color: sentimentPalette.negative },
  ].filter((item) => item.value > 0)

  const metricCards = [
    {
      label: 'Source Diversity',
      value: `${Math.round(analytics.sourceDiversity)}%`,
      icon: TrendingUp,
      borderColor: 'border-l-semantic-success',
      iconBg: 'bg-semantic-success/10',
      iconColor: 'text-semantic-success',
    },
    {
      label: 'Unique Sources',
      value: analytics.topSources.length,
      icon: Users,
      borderColor: 'border-l-accent-cyan',
      iconBg: 'bg-accent-cyan/10',
      iconColor: 'text-accent-cyan',
    },
    {
      label: 'Coverage Concentration',
      value: `${Math.round(analytics.coverageConcentration)}%`,
      icon: AlertCircle,
      borderColor: 'border-l-semantic-warning',
      iconBg: 'bg-semantic-warning/10',
      iconColor: 'text-semantic-warning',
    },
    {
      label: 'Dominant Cluster',
      value: analytics.dominantCluster,
      icon: TrendingUp,
      borderColor: 'border-l-accent-indigo',
      iconBg: 'bg-accent-indigo/10',
      iconColor: 'text-accent-indigo',
      isText: true,
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Analytics"
        subtitle="Live distribution, source mix, and concentration metrics derived from the current feed."
      />

      {totalSentiment === 0 ? (
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <EmptyState
            icon={<BarChart3 className="w-6 h-6" />}
            title="No analytics data yet"
            description="Fetch a feed first to unlock sentiment distribution, top sources, and source concentration metrics."
          />
        </Card>
      ) : (
        <motion.div variants={containerVariants} initial="hidden" animate="visible" className="space-y-6">
          {/* Charts row */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Sentiment Pie */}
            <motion.div variants={itemVariants}>
              <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
                <div className="flex items-center justify-between mb-6">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-accent-primary/10 flex items-center justify-center">
                      <PieChart className="w-5 h-5 text-accent-primary" />
                    </div>
                    <div>
                      <h3 className="text-sm font-medium text-text-primary">Sentiment Distribution</h3>
                      <p className="text-xs text-text-muted">Positive, neutral, and negative article mix</p>
                    </div>
                  </div>
                  <Badge variant="success">Live</Badge>
                </div>

                <div className="h-56">
                  <ResponsiveContainer width="100%" height="100%">
                    <RechartsPie>
                      <Pie
                        data={sentimentData}
                        cx="50%"
                        cy="50%"
                        innerRadius={62}
                        outerRadius={84}
                        paddingAngle={5}
                        dataKey="value"
                        stroke="transparent"
                      >
                        {sentimentData.map((entry) => (
                          <Cell key={entry.name} fill={entry.color} />
                        ))}
                      </Pie>
                    </RechartsPie>
                  </ResponsiveContainer>
                </div>

                <div className="flex flex-wrap justify-center gap-6 mt-4">
                  {sentimentData.map((item) => {
                    const pct = totalSentiment > 0 ? ((item.value / totalSentiment) * 100).toFixed(1) : '0'
                    return (
                      <div key={item.name} className="flex items-center gap-2">
                        <div className="w-3 h-3 rounded-full" style={{ backgroundColor: item.color }} />
                        <span className="text-xs text-text-muted">
                          {item.name}{' '}
                          <span className="font-mono text-text-secondary">({pct}%)</span>
                        </span>
                      </div>
                    )
                  })}
                </div>
              </Card>
            </motion.div>

            {/* Top Sources Bar */}
            <motion.div variants={itemVariants}>
              <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
                <div className="flex items-center justify-between mb-6">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-accent-indigo/10 flex items-center justify-center">
                      <BarChart3 className="w-5 h-5 text-accent-indigo" />
                    </div>
                    <div>
                      <h3 className="text-sm font-medium text-text-primary">Top Sources</h3>
                      <p className="text-xs text-text-muted">Highest article volume in the current feed</p>
                    </div>
                  </div>
                </div>

                <div className="h-56">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={analytics.topSources.slice(0, 6)} layout="vertical" margin={{ left: 12, right: 12 }}>
                      <XAxis type="number" hide />
                      <YAxis
                        type="category"
                        dataKey="name"
                        width={110}
                        tick={{ fontSize: 12, fill: '#6e6e76' }}
                      />
                      <Tooltip content={<CustomBarTooltip />} cursor={{ fill: 'rgba(59,130,246,0.06)' }} />
                      <Bar dataKey="count" fill="#00f0ff" radius={[0, 6, 6, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </Card>
            </motion.div>
          </div>

          {/* Metric cards */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            {metricCards.map((card, index) => {
              const Icon = card.icon
              return (
                <motion.div key={card.label} variants={itemVariants}>
                  <Card className={`bg-surface/80 backdrop-blur-sm border-border/50 border-l-2 ${card.borderColor} flex items-center gap-4`}>
                    <div className={`w-10 h-10 rounded-lg ${card.iconBg} flex items-center justify-center shrink-0`}>
                      <Icon className={`w-5 h-5 ${card.iconColor}`} />
                    </div>
                    <div className="min-w-0">
                      <p className="text-xs text-text-muted">{card.label}</p>
                      {card.isText ? (
                        <p className="text-base font-semibold text-text-primary line-clamp-1">{card.value}</p>
                      ) : (
                        <p className="text-2xl font-mono font-semibold text-text-primary">{card.value}</p>
                      )}
                    </div>
                  </Card>
                </motion.div>
              )
            })}
          </div>
        </motion.div>
      )}
    </div>
  )
}
