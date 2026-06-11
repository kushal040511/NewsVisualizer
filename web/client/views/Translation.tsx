'use client'

import { useState } from 'react'
import { motion } from 'framer-motion'
import { Languages, BookOpen, MessageSquare, Ear } from 'lucide-react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button, Input, Card, Badge, EmptyState, Select } from '@/components/ui'
import { translateText } from '@/services/api'
import { cn } from '@/lib/utils'
import type { TranslationMode, TranslationResult } from '@/types'

const modes = [
  { value: 'translate', label: 'Translate', icon: Languages },
  { value: 'define', label: 'Define Word', icon: BookOpen },
  { value: 'explain', label: 'Explain Sentence', icon: MessageSquare },
  { value: 'detect', label: 'Detect Language', icon: Ear },
] as const

const languages = [
  { value: 'es', label: 'Spanish' },
  { value: 'fr', label: 'French' },
  { value: 'de', label: 'German' },
  { value: 'it', label: 'Italian' },
  { value: 'pt', label: 'Portuguese' },
  { value: 'ja', label: 'Japanese' },
  { value: 'hi', label: 'Hindi' },
  { value: 'zh', label: 'Chinese' },
]

export function Translation() {
  const [mode, setMode] = useState<TranslationMode>('translate')
  const [text, setText] = useState('')
  const [targetLang, setTargetLang] = useState('es')
  const [result, setResult] = useState<TranslationResult | null>(null)
  const queryClient = useQueryClient()

  const translateMutation = useMutation({
    mutationFn: (payload: { text: string; targetLang: string; mode: TranslationMode }) =>
      translateText(payload.text, payload.targetLang, payload.mode),
    onSuccess: (data) => {
      setResult(data)
      void queryClient.invalidateQueries({ queryKey: ['history'] })
    },
  })

  const handleProcess = async () => {
    if (!text.trim()) return
    await translateMutation.mutateAsync({ text: text.trim(), targetLang, mode })
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Translation"
        subtitle="Use the language assistant for translation, definitions, sentence explanations, and language detection."
      />

      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
      >
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <div className="flex flex-wrap gap-2">
            {modes.map((entry) => (
              <button
                key={entry.value}
                type="button"
                onClick={() => setMode(entry.value)}
                className={cn(
                  'inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 border',
                  mode === entry.value
                    ? 'bg-accent-primary/15 text-accent-light border-accent-primary/40 shadow-[0_0_12px_rgba(59,130,246,0.25)]'
                    : 'bg-surface-alt/40 text-text-muted border-border/30 hover:border-border-strong hover:text-text-secondary'
                )}
              >
                <entry.icon className="w-4 h-4" />
                {entry.label}
              </button>
            ))}
          </div>
        </Card>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.06 }}
        className="grid grid-cols-1 xl:grid-cols-[1.05fr_1fr] gap-4"
      >
        <Card className="bg-surface/80 backdrop-blur-sm border-border/50">
          <div className="space-y-4">
            <Input
              label="Input text"
              placeholder={
                mode === 'define'
                  ? 'Enter a word to define'
                  : mode === 'detect'
                    ? 'Enter text to detect its language'
                    : 'Enter text to process'
              }
              value={text}
              onChange={(event) => setText(event.target.value)}
              className="focus-within:shadow-[0_0_12px_rgba(59,130,246,0.15)] transition-shadow duration-300"
            />

            {mode === 'translate' ? (
              <Select
                label="Target language"
                value={targetLang}
                onChange={(event) => setTargetLang(event.target.value)}
                options={languages}
              />
            ) : null}

            <div className="flex items-center gap-2 text-xs text-text-muted">
              <Badge variant="info">{modes.find((entry) => entry.value === mode)?.label}</Badge>
              {mode === 'translate' ? <Badge variant="default">{languages.find((language) => language.value === targetLang)?.label}</Badge> : null}
            </div>

            <Button onClick={handleProcess} loading={translateMutation.isPending} className="w-full">
              Process
            </Button>

            {translateMutation.isError ? (
              <div className="rounded-lg border border-semantic-danger/30 bg-semantic-danger/5 backdrop-blur-sm px-4 py-3 text-sm text-semantic-danger">
                {translateMutation.error instanceof Error ? translateMutation.error.message : 'Unable to process this request right now.'}
              </div>
            ) : null}
          </div>
        </Card>

        <Card className="bg-surface/80 backdrop-blur-sm border-border/50 bg-gradient-to-br from-surface/90 via-surface-elevated/50 to-surface/90">
          <div className="flex items-center justify-between gap-3 mb-5">
            <div>
              <h3 className="text-sm font-medium text-text-primary">Result</h3>
              <p className="text-xs text-text-muted">Structured language assistance output.</p>
            </div>
            {translateMutation.isPending ? (
              <Badge variant="info" className="animate-pulse">Processing...</Badge>
            ) : result ? (
              <Badge variant="success" className="shadow-[0_0_8px_rgba(52,211,153,0.2)]">Ready</Badge>
            ) : null}
          </div>

          {!result ? (
            <EmptyState
              icon={<Languages className="w-6 h-6" />}
              title="No language result yet"
              description="Enter text and run one of the language modes to populate this panel."
            />
          ) : (
            <div className="space-y-5">
              <div className="flex flex-wrap gap-2">
                <Badge variant="default">Mode: {modes.find((entry) => entry.value === mode)?.label}</Badge>
                {result.detectedLanguage ? <Badge variant="info">Detected: {result.detectedLanguage}</Badge> : null}
                <Badge variant="success">Target: {result.targetLanguage}</Badge>
              </div>

              <div>
                <p className="text-[10px] uppercase tracking-widest text-text-muted mb-2">Original</p>
                <div className="rounded-lg border border-border/30 bg-surface-alt/40 backdrop-blur-sm px-4 py-3 text-sm text-text-secondary whitespace-pre-wrap">
                  {result.originalText}
                </div>
              </div>

              <div>
                <p className="text-[10px] uppercase tracking-widest text-text-muted mb-2">
                  {mode === 'translate' ? 'Translation' : mode === 'define' ? 'Definition' : mode === 'detect' ? 'Detection Result' : 'Explanation'}
                </p>
                <div className="rounded-lg border border-accent-primary/20 bg-surface-alt/40 backdrop-blur-sm px-4 py-3 text-sm leading-7 text-text-secondary whitespace-pre-wrap shadow-[0_0_8px_rgba(59,130,246,0.08)]">
                  {result.translatedText}
                </div>
              </div>
            </div>
          )}
        </Card>
      </motion.div>
    </div>
  )
}
