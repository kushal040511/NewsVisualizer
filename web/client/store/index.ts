import { create } from 'zustand'
import type { CountryCode, CategoryValue, NewsArticle } from '@/types'

interface AppState {
  // Sidebar
  sidebarCollapsed: boolean
  setSidebarCollapsed: (collapsed: boolean) => void

  // News Fetch
  selectedCountry: CountryCode
  setSelectedCountry: (country: CountryCode) => void
  selectedCategory: CategoryValue
  setSelectedCategory: (category: CategoryValue) => void

  // Articles
  articles: NewsArticle[]
  setArticles: (articles: NewsArticle[]) => void
  selectedArticleId: number | null
  setSelectedArticleId: (id: number | null) => void

  // Loading states
  isFetchingNews: boolean
  setIsFetchingNews: (fetching: boolean) => void
}

export const useAppStore = create<AppState>((set) => ({
  // Sidebar
  sidebarCollapsed: false,
  setSidebarCollapsed: (collapsed) => set({ sidebarCollapsed: collapsed }),

  // News Fetch
  selectedCountry: 'us',
  setSelectedCountry: (country) => set({ selectedCountry: country }),
  selectedCategory: 'general',
  setSelectedCategory: (category) => set({ selectedCategory: category }),

  // Articles
  articles: [],
  setArticles: (articles) => set({ articles }),
  selectedArticleId: null,
  setSelectedArticleId: (id) => set({ selectedArticleId: id }),

  // Loading states
  isFetchingNews: false,
  setIsFetchingNews: (fetching) => set({ isFetchingNews: fetching }),
}))
