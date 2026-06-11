// News Article types
export interface NewsArticle {
  id: number
  title: string
  description?: string
  content?: string
  url?: string
  imageUrl?: string
  publishedAt?: string
  sourceName?: string
  author?: string
  category?: string
  country?: string
  sentimentScore?: number
  createdAt?: string
}

// Search History types
export interface SearchHistory {
  id: number
  userId?: number
  actionType: string
  query?: string
  details?: string
  resultSummary?: string
  createdAt: string
}

// API Response types
export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: string
}

// Dashboard Stats
export interface DashboardStats {
  totalArticles: number
  analysesDone: number
  feedHealth: number
  sourceDiversity: number
  duplicatePressure: number
  urgencyLevel: number
  recentActivityCount: number
  aiPulseSummary?: string
  latestNarrativeShifts?: string
  concentrationScore?: number
  sourceBalanceSnapshot?: SourceBalanceData
}

export interface SourceBalanceData {
  score: number
  coverageRisk: string
  dominantSource: string
  dominantShare: number
  toneSpread: string
  sources: SourceShare[]
}

export interface SourceShare {
  name: string
  share: number
  tone: string
}

// Story Cluster
export interface StoryCluster {
  id: string
  label: string
  articleCount: number
  sourceCount: number
  leadHeadlines: string[]
  confidence: number
}

// Source Monitor
export interface SourceData {
  name: string
  articleCount: number
  averageTone: string
  latestHeadline: string
  freshness: string
  coverageShare: number
}

// Breaking News
export interface BreakingNews {
  id: string
  headline: string
  source: string
  reason: string
  urgencyScore: number
  publishTime: string
}

// Duplicate Cluster
export interface DuplicateCluster {
  id: string
  representativeHeadline: string
  duplicateScore: number
  sourceCount: number
  articleCount: number
  repeatedHeadlines: string[]
  syndicationSignal: string
}

// Timeline Event
export interface TimelineEvent {
  id: string
  type: 'initial_report' | 'rapid_followup' | 'new_source' | 'escalation' | 'tone_shift' | 'followup'
  headline: string
  source: string
  timestamp: string
}

export interface StoryTimeline {
  id: string
  label: string
  coverageWindow: string
  sourceCount: number
  events: TimelineEvent[]
}

// AI Summary
export interface AISummary {
  url?: string
  title?: string
  summary: string
  keyInsights: string[]
  metadata: {
    wordCount: number
    readingTime: number
    processedAt: string
    sentiment?: number
    source?: string
    publishedAt?: string
  }
}

// Translation
export interface TranslationResult {
  originalText: string
  translatedText: string
  detectedLanguage?: string
  targetLanguage: string
}

export type TranslationMode = 'translate' | 'define' | 'explain' | 'detect'

// Settings
export interface AppSettings {
  newsapiStatus: string
  translationProvider: string
  appVersion: string
  theme: string
}

// Country and Category options
export const COUNTRIES = [
  { value: 'us', label: 'United States' },
  { value: 'in', label: 'India' },
  { value: 'gb', label: 'United Kingdom' },
  { value: 'jp', label: 'Japan' },
  { value: 'au', label: 'Australia' },
  { value: 'ca', label: 'Canada' },
  { value: 'de', label: 'Germany' },
] as const

export const CATEGORIES = [
  { value: 'general', label: 'General' },
  { value: 'business', label: 'Business' },
  { value: 'technology', label: 'Technology' },
  { value: 'health', label: 'Health' },
  { value: 'science', label: 'Science' },
  { value: 'sports', label: 'Sports' },
  { value: 'entertainment', label: 'Entertainment' },
] as const

export type CountryCode = typeof COUNTRIES[number]['value']
export type CategoryValue = typeof CATEGORIES[number]['value']
