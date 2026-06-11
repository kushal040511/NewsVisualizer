"""Vercel Python function wrapping the NewsVisualizer backend.

All /api/* requests are rewritten here (see vercel.json). The original
request path is preserved, so the RequestHandler's own routing works
unchanged. SQLite lives in /tmp — ephemeral per serverless instance,
which is fine for the simulated feed (re-fetch repopulates it).
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from _server import (
    RequestHandler,
    DatabaseService,
    NewsGenerator,
    SentimentAnalyzer,
    KeywordExtractor,
    Summarizer,
    DB_PATH,
)

# Initialize services once per function instance (warm invocations reuse them)
RequestHandler.db_service = DatabaseService(str(DB_PATH))
RequestHandler.news_generator = NewsGenerator()
RequestHandler.sentiment = SentimentAnalyzer()
RequestHandler.keyword_extractor = KeywordExtractor()
RequestHandler.summarizer = Summarizer()


class handler(RequestHandler):  # noqa: N801 — Vercel requires this exact name
    pass
