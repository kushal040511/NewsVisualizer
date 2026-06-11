import type {
  NewsArticle,
  SearchHistory,
  DashboardStats,
  StoryCluster,
  SourceData,
  BreakingNews,
  DuplicateCluster,
  StoryTimeline,
  AISummary,
  TranslationResult,
  AppSettings,
  CountryCode,
  CategoryValue,
} from '@/types'

const API_BASE = '/api'

function mapActionType(value?: string) {
  const normalized = (value || '').toLowerCase()
  if (normalized === 'fetch') return 'News Fetch'
  if (normalized === 'ai summary' || normalized === 'summary') return 'AI Summary'
  if (normalized === 'translation' || normalized === 'translate') return 'Translation'
  if (normalized === 'define') return 'Define'
  if (normalized === 'detect') return 'Detect'
  if (normalized === 'explain') return 'Explain'
  return value || 'Activity'
}

async function readJson<T>(response: Response): Promise<T> {
  const payload = await response.json().catch(() => null)
  if (!response.ok) {
    const message = payload && typeof payload === 'object' && 'error' in payload ? String(payload.error) : 'Request failed'
    throw new Error(message)
  }
  return payload as T
}

function normalizeArticle(raw: Record<string, any>): NewsArticle {
  return {
    id: Number(raw.id ?? 0),
    title: raw.title ?? 'Untitled article',
    description: raw.description ?? '',
    content: raw.content ?? '',
    url: raw.url ?? '',
    imageUrl: raw.imageUrl ?? raw.image_url ?? '',
    publishedAt: raw.publishedAt ?? raw.published_at ?? raw.createdAt ?? raw.created_at,
    sourceName: raw.sourceName ?? raw.source_name ?? 'Unknown Source',
    author: raw.author ?? '',
    category: raw.category ?? '',
    country: raw.country ?? '',
    sentimentScore: raw.sentimentScore ?? raw.sentiment_score ?? 0,
    createdAt: raw.createdAt ?? raw.created_at ?? '',
  }
}

function normalizeSummary(raw: Record<string, any>): AISummary {
  return {
    url: raw.url,
    title: raw.title,
    summary: raw.summary ?? '',
    keyInsights: raw.keyInsights ?? raw.keyPoints ?? [],
    metadata: {
      wordCount: raw.metadata?.wordCount ?? 0,
      readingTime: raw.metadata?.readingTime ?? 0,
      processedAt: raw.metadata?.processedAt ?? new Date().toISOString(),
      sentiment: raw.metadata?.sentiment,
      source: raw.metadata?.source,
      publishedAt: raw.metadata?.publishedAt,
    },
  }
}

function normalizeHistory(raw: Record<string, any>): SearchHistory {
  return {
    id: Number(raw.id ?? 0),
    userId: raw.userId ?? raw.user_id,
    actionType: mapActionType(raw.actionType ?? raw.action_type),
    query: raw.query ?? '',
    details: raw.details ?? '',
    resultSummary: raw.resultSummary ?? raw.result_summary ?? '',
    createdAt: raw.createdAt ?? raw.created_at ?? new Date().toISOString(),
  }
}

function normalizeSettings(raw: Record<string, any>): AppSettings {
  return {
    newsapiStatus: raw.newsapiStatus ?? raw.newsapi_status ?? 'unknown',
    translationProvider: raw.translationProvider ?? raw.translation_provider ?? 'unknown',
    appVersion: raw.appVersion ?? raw.app_version ?? 'unknown',
    theme: raw.theme ?? 'dark',
  }
}

// Dashboard
export async function fetchDashboardStats(): Promise<DashboardStats> {
  const response = await fetch(`${API_BASE}/dashboard/stats`)
  const payload = await readJson<Record<string, any>>(response)
  return {
    totalArticles: payload.totalArticles ?? 0,
    analysesDone: payload.analysesDone ?? payload.analyzesDone ?? payload.analysesPerformed ?? 0,
    feedHealth: payload.feedHealth ?? 0,
    sourceDiversity: payload.sourceDiversity ?? 0,
    duplicatePressure: payload.duplicatePressure ?? 0,
    urgencyLevel: payload.urgencyLevel ?? 0,
    recentActivityCount: payload.recentActivityCount ?? payload.recentActionsCount ?? 0,
    aiPulseSummary: payload.aiPulseSummary ?? '',
    latestNarrativeShifts: payload.latestNarrativeShifts ?? '',
    concentrationScore: payload.concentrationScore ?? 0,
    sourceBalanceSnapshot: payload.sourceBalanceSnapshot,
  }
}

// News
export async function fetchNews(country: CountryCode, category: CategoryValue): Promise<NewsArticle[]> {
  const response = await fetch(`${API_BASE}/news?country=${country}&category=${category}&count=60`)
  const payload = await readJson<Record<string, any> | Record<string, any>[]>(response)
  const items = Array.isArray(payload) ? payload : payload.articles ?? []
  return items.map(normalizeArticle)
}

export async function fetchAllArticles(): Promise<NewsArticle[]> {
  const response = await fetch(`${API_BASE}/articles`)
  const payload = await readJson<Record<string, any> | Record<string, any>[]>(response)
  const items = Array.isArray(payload) ? payload : payload.articles ?? []
  return items.map(normalizeArticle)
}

export async function deleteArticle(id: number): Promise<void> {
  const response = await fetch(`${API_BASE}/articles/${id}`, { method: 'DELETE' })
  await readJson<Record<string, any>>(response)
}

export async function clearAllArticles(): Promise<void> {
  const response = await fetch(`${API_BASE}/articles`, { method: 'DELETE' })
  await readJson<Record<string, any>>(response)
}

// Analytics
export async function fetchAnalytics(): Promise<{
  sentimentDistribution: { positive: number; neutral: number; negative: number }
  topSources: { name: string; count: number }[]
  sourceDiversity: number
  dominantCluster: string
  coverageConcentration: number
}> {
  const response = await fetch(`${API_BASE}/analytics`)
  const payload = await readJson<Record<string, any>>(response)
  return {
    sentimentDistribution: payload.sentimentDistribution ?? { positive: 0, neutral: 0, negative: 0 },
    topSources: payload.topSources ?? [],
    sourceDiversity: payload.sourceDiversity ?? 0,
    dominantCluster: payload.dominantCluster ?? 'No dominant cluster yet',
    coverageConcentration: payload.coverageConcentration ?? 0,
  }
}

// Story Radar
export async function fetchStoryClusters(): Promise<StoryCluster[]> {
  const response = await fetch(`${API_BASE}/radar/clusters`)
  return readJson<StoryCluster[]>(response)
}

// Source Monitor
export async function fetchSources(): Promise<SourceData[]> {
  const response = await fetch(`${API_BASE}/sources`)
  return readJson<SourceData[]>(response)
}

// Breaking Watch
export async function fetchBreakingNews(): Promise<BreakingNews[]> {
  const response = await fetch(`${API_BASE}/breaking`)
  return readJson<BreakingNews[]>(response)
}

// Duplicate Stories
export async function fetchDuplicates(): Promise<DuplicateCluster[]> {
  const response = await fetch(`${API_BASE}/duplicates`)
  return readJson<DuplicateCluster[]>(response)
}

// Source Balance
export async function fetchSourceBalance(): Promise<{
  score: number
  coverageRisk: string
  dominantSource: string
  dominantShare: number
  toneSpread: string
  sources: { name: string; share: number; tone: string }[]
}> {
  const response = await fetch(`${API_BASE}/balance`)
  return readJson(response)
}

// Story Timeline
export async function fetchTimelines(): Promise<StoryTimeline[]> {
  const response = await fetch(`${API_BASE}/timelines`)
  return readJson<StoryTimeline[]>(response)
}

// AI Summary
export async function generateSummary(url: string): Promise<AISummary> {
  const response = await fetch(`${API_BASE}/summarize`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ url }),
  })
  return normalizeSummary(await readJson<Record<string, any>>(response))
}

export async function summarizeArticle(articleId: number): Promise<AISummary> {
  const response = await fetch(`${API_BASE}/summarize/article/${articleId}`, { method: 'POST' })
  return normalizeSummary(await readJson<Record<string, any>>(response))
}

export async function generateFeedBriefing(): Promise<AISummary> {
  const response = await fetch(`${API_BASE}/summarize/feed`, { method: 'POST' })
  return normalizeSummary(await readJson<Record<string, any>>(response))
}

// Translation
export async function translateText(
  text: string,
  targetLang: string,
  mode: 'translate' | 'define' | 'explain' | 'detect'
): Promise<TranslationResult> {
  const response = await fetch(`${API_BASE}/translate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text, targetLang, toLanguage: targetLang, mode }),
  })
  const payload = await readJson<Record<string, any>>(response)
  return {
    originalText: payload.originalText ?? text,
    translatedText: payload.translatedText ?? payload.result ?? '',
    detectedLanguage: payload.detectedLanguage,
    targetLanguage: payload.targetLanguage ?? payload.toLanguage ?? targetLang,
  }
}

// Search History
export async function fetchSearchHistory(): Promise<SearchHistory[]> {
  const response = await fetch(`${API_BASE}/history`)
  const payload = await readJson<Record<string, any> | Record<string, any>[]>(response)
  const items = Array.isArray(payload) ? payload : payload.history ?? []
  return items.map(normalizeHistory)
}

export async function clearSearchHistory(): Promise<void> {
  const response = await fetch(`${API_BASE}/history`, { method: 'DELETE' })
  if (!response.ok) throw new Error('Failed to clear history')
}

// Settings
export async function fetchSettings(): Promise<AppSettings> {
  const response = await fetch(`${API_BASE}/settings`)
  return normalizeSettings(await readJson<Record<string, any>>(response))
}

export async function updateSetting(key: string, value: string): Promise<void> {
  const response = await fetch(`${API_BASE}/settings`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ key, value }),
  })
  await readJson<Record<string, any>>(response)
}
