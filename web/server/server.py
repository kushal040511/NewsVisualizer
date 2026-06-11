#!/usr/bin/env python3
"""
News Visualizer Backend Server
A comprehensive REST API for news analysis and visualization.
Uses only Python stdlib: http.server, sqlite3, json, os, datetime, re, hashlib, uuid, pathlib, threading, random, math, logging
"""

import http.server
import socketserver
import sqlite3
import json
import os
import sys
import hashlib
import uuid
import threading
import random
import math
import re
from datetime import datetime, timedelta
from pathlib import Path
from urllib.parse import urlparse, parse_qs
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Configuration
PORT = int(os.environ.get('PORT', 8081))
BASE_DIR = Path(__file__).parent.parent
CLIENT_DIR = BASE_DIR / "client"
STATIC_DIR = CLIENT_DIR / "dist" if (CLIENT_DIR / "dist").exists() else CLIENT_DIR
# Use a writable temp dir for SQLite (mounted FS may not support it)
DATA_DIR = Path("/tmp/newsvisualizer_data")
DB_PATH = DATA_DIR / "newsvisualizer.db"

# Ensure directories exist
DATA_DIR.mkdir(exist_ok=True)


class DatabaseService:
    """Handles all database operations with thread-safe connection management."""

    def __init__(self, db_path: str):
        self.db_path = db_path
        self.lock = threading.Lock()
        self._init_db()

    def _get_connection(self):
        """Get a database connection with row factory."""
        conn = sqlite3.connect(str(self.db_path))
        conn.row_factory = sqlite3.Row
        return conn

    def _init_db(self):
        """Initialize database schema and seed default data."""
        with self.lock:
            conn = self._get_connection()
            cursor = conn.cursor()

            # Create tables
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    first_name TEXT,
                    last_name TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_login_at TEXT,
                    is_active INTEGER DEFAULT 1
                )
            ''')

            cursor.execute('''
                CREATE TABLE IF NOT EXISTS articles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    content TEXT,
                    url TEXT,
                    image_url TEXT,
                    published_at TEXT,
                    source_name TEXT,
                    author TEXT,
                    category TEXT,
                    country TEXT,
                    sentiment_score REAL DEFAULT 0,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            ''')

            cursor.execute('''
                CREATE TABLE IF NOT EXISTS search_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER DEFAULT 1,
                    action_type TEXT NOT NULL,
                    query TEXT,
                    details TEXT,
                    result_summary TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            ''')

            cursor.execute('''
                CREATE TABLE IF NOT EXISTS app_settings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    key TEXT UNIQUE NOT NULL,
                    value TEXT,
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            ''')

            # Seed default user (admin/admin123)
            admin_hash = hashlib.sha256('admin123'.encode()).hexdigest()
            try:
                cursor.execute(
                    'INSERT INTO users (username, email, password_hash, first_name, last_name) VALUES (?, ?, ?, ?, ?)',
                    ('admin', 'admin@newsvisualizer.local', admin_hash, 'Admin', 'User')
                )
            except sqlite3.IntegrityError:
                pass  # User already exists

            # Seed default settings
            settings = [
                ('newsapi_status', 'simulated'),
                ('translation_provider', 'local'),
                ('app_version', '2.0.0'),
                ('theme', 'dark')
            ]
            for key, value in settings:
                try:
                    cursor.execute('INSERT INTO app_settings (key, value) VALUES (?, ?)', (key, value))
                except sqlite3.IntegrityError:
                    pass  # Setting already exists

            cursor.execute(
                'UPDATE app_settings SET value = ?, updated_at = ? WHERE key = ?',
                ('dark', datetime.now().isoformat(), 'theme')
            )

            conn.commit()
            conn.close()

    def execute(self, query, params=None):
        """Execute a query and return result."""
        with self.lock:
            conn = self._get_connection()
            cursor = conn.cursor()
            try:
                if params:
                    cursor.execute(query, params)
                else:
                    cursor.execute(query)
                conn.commit()
                return cursor.lastrowid
            finally:
                conn.close()

    def fetch_one(self, query, params=None):
        """Fetch a single row."""
        with self.lock:
            conn = self._get_connection()
            cursor = conn.cursor()
            try:
                if params:
                    cursor.execute(query, params)
                else:
                    cursor.execute(query)
                row = cursor.fetchone()
                return dict(row) if row else None
            finally:
                conn.close()

    def fetch_all(self, query, params=None):
        """Fetch all rows."""
        with self.lock:
            conn = self._get_connection()
            cursor = conn.cursor()
            try:
                if params:
                    cursor.execute(query, params)
                else:
                    cursor.execute(query)
                rows = cursor.fetchall()
                return [dict(row) for row in rows]
            finally:
                conn.close()


class SentimentAnalyzer:
    """Analyzes sentiment of text using word frequency approach."""

    POSITIVE_WORDS = {
        'good', 'great', 'excellent', 'amazing', 'success', 'growth', 'improve',
        'breakthrough', 'win', 'record', 'surge', 'gain', 'profit', 'benefit',
        'hope', 'strong', 'rally', 'advance', 'opportunity', 'positive', 'best',
        'beautiful', 'brilliant', 'fantastic', 'hero', 'love', 'perfect', 'triumph',
        'outstanding', 'remarkable', 'wonderful', 'fantastic', 'incredible', 'achieve',
        'excel', 'thrive', 'prosper', 'victorious', 'stellar', 'spectacular'
    }

    NEGATIVE_WORDS = {
        'bad', 'terrible', 'crisis', 'fail', 'loss', 'decline', 'crash', 'threat',
        'danger', 'attack', 'war', 'death', 'scandal', 'fraud', 'fear', 'concern',
        'risk', 'harm', 'disaster', 'worst', 'awful', 'horrible', 'poor', 'weak',
        'collapse', 'plunge', 'struggle', 'fighting', 'difficult', 'challenge',
        'negative', 'suffering', 'pain', 'loss', 'problem', 'issue', 'alarming'
    }

    def analyze(self, text: str) -> float:
        """Analyze sentiment and return score between -1 and 1."""
        if not text:
            return 0.0

        words = re.findall(r'[a-z]+', text.lower())
        total = len(words)
        if total == 0:
            return 0.0

        positive_count = sum(1 for w in words if w in self.POSITIVE_WORDS)
        negative_count = sum(1 for w in words if w in self.NEGATIVE_WORDS)

        # Scale up to make scores more meaningful
        score = (positive_count - negative_count) / max(1, (positive_count + negative_count + 3))
        return max(-1.0, min(1.0, score))


class KeywordExtractor:
    """Extracts and weights keywords from articles."""

    STOP_WORDS = {
        'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
        'of', 'with', 'by', 'from', 'as', 'is', 'was', 'are', 'be', 'been',
        'being', 'have', 'has', 'had', 'do', 'does', 'did', 'will', 'would',
        'could', 'should', 'may', 'might', 'can', 'that', 'this', 'it', 'its',
        'which', 'who', 'when', 'where', 'why', 'how', 'all', 'each', 'every',
        'some', 'any', 'no', 'not', 'than', 'then', 'what', 'so', 'up', 'out',
        'if', 'into', 'through', 'during', 'before', 'after', 'above', 'below'
    }

    def extract(self, articles: list, top_n: int = 50) -> list:
        """Extract top keywords with weights from articles."""
        word_freq = {}

        for article in articles:
            text = (article.get('title', '') + ' ' + article.get('description', '')).lower()
            words = re.findall(r'\b\w+\b', text)

            for word in words:
                if word not in self.STOP_WORDS and len(word) > 2:
                    word_freq[word] = word_freq.get(word, 0) + 1

        if not word_freq:
            return []

        sorted_words = sorted(word_freq.items(), key=lambda x: x[1], reverse=True)[:top_n]
        max_freq = sorted_words[0][1] if sorted_words else 1

        keywords = []
        for word, count in sorted_words:
            weight = count / max_freq
            keywords.append({
                'word': word,
                'count': count,
                'weight': round(weight, 2)
            })

        return keywords


class Summarizer:
    """Generates summaries of articles."""

    def summarize(self, content: str, title: str = '') -> dict:
        """Summarize article content."""
        if not content:
            return {
                'summary': '',
                'key_points': [],
                'word_count': 0,
                'reading_time': 0
            }

        sentences = re.split(r'[.!?]+', content)
        sentences = [s.strip() for s in sentences if s.strip()]

        if not sentences:
            return {
                'summary': '',
                'key_points': [],
                'word_count': len(content.split()),
                'reading_time': max(1, len(content.split()) // 200)
            }

        # Score sentences
        scored_sentences = []
        title_words = set(title.lower().split())

        for i, sentence in enumerate(sentences):
            score = 0

            # Position bonus (first sentences score higher)
            position_score = 1.0 / (i + 1)
            score += position_score * 0.3

            # Word overlap with title
            sentence_words = set(sentence.lower().split())
            overlap = len(title_words & sentence_words)
            score += overlap * 0.3

            # Length preference (prefer medium-length)
            word_count = len(sentence.split())
            if 10 <= word_count <= 25:
                score += 0.4
            elif 5 <= word_count <= 30:
                score += 0.2

            scored_sentences.append((sentence, score))

        # Select top 3-5 sentences
        num_summary_sentences = min(5, max(3, len(scored_sentences) // 2))
        top_sentences = sorted(scored_sentences, key=lambda x: x[1], reverse=True)[:num_summary_sentences]

        # Sort by original order
        summary_sentences = sorted(
            [(s, sentences.index(s)) for s, _ in top_sentences],
            key=lambda x: x[1]
        )

        summary = ' '.join([s[0] for s in summary_sentences])

        # Extract key points from highest-scoring sentences
        key_points = []
        for sentence, _ in top_sentences[:3]:
            words = sentence.split()
            if words:
                key_points.append(words[0])

        word_count = len(content.split())
        reading_time = max(1, word_count // 200)

        return {
            'summary': summary,
            'key_points': key_points,
            'word_count': word_count,
            'reading_time': reading_time
        }


class NewsGenerator:
    """Generates realistic news articles for different categories and countries."""

    BUSINESS_TITLES = [
        "Global Markets Rally as Central Banks Signal Rate Cuts",
        "Tech Giants Report Record Quarterly Earnings",
        "Oil Prices Surge Amid Middle East Supply Concerns",
        "Federal Reserve Holds Interest Rates Steady",
        "Startup Raises $500M in Series D Funding Round",
        "Stock Market Reaches New All-Time High",
        "Merger Deal Worth $2 Billion Announced",
        "Cryptocurrency Market Experiences Significant Growth",
        "Consumer Spending Exceeds Economic Forecasts",
        "Corporate Profits Beat Analyst Expectations",
        "Business Confidence Index Climbs to Five-Year Peak",
        "Retail Sales Surge During Holiday Season",
        "Banking Sector Reports Robust First Quarter Results",
        "Stock Market Crashes as Recession Fears Grow",
        "Major Corporation Faces Fraud Scandal Investigation",
        "Unemployment Rate Surges to Alarming Levels",
        "Consumer Debt Crisis Threatens Economic Stability",
        "Trade War Escalation Damages Global Markets",
        "Corporate Layoffs Accelerate Amid Poor Earnings",
        "Banking Crisis Spreads Panic Through Financial Markets",
        "Inflation Fears Grip Economy as Prices Soar",
        "Supply Chain Collapse Disrupts Critical Industries",
        "Housing Market Crash Leaves Homeowners in Distress"
    ]

    TECHNOLOGY_TITLES = [
        "AI Breakthrough: New Model Achieves Human-Level Reasoning",
        "Apple Announces Next-Generation Chip Architecture",
        "Cloud Computing Market Expected to Double by 2028",
        "Open Source Project Gains Major Corporate Backing",
        "Quantum Computing Makes Significant Advances",
        "Software Development Tools Get Major Upgrades",
        "Video Game Engine Sets New Graphics Standards",
        "Virtual Reality Market Experiences Rapid Expansion",
        "Artificial Intelligence Assists Medical Diagnosis",
        "Tech Startup Valued at Over $1 Billion",
        "5G Rollout Accelerates Across Continents",
        "Cybersecurity Firms Warn of Critical Zero-Day Vulnerability",
        "Massive Data Breach Exposes Millions of User Records",
        "Tech Company Faces Lawsuit Over Privacy Violations",
        "Critical Security Flaw Threatens Internet Infrastructure",
        "Social Media Platform Struggles with Harmful Content Crisis",
        "AI Bias Scandal Exposes Dangerous Discrimination Patterns",
        "Tech Layoffs Devastate Workforce as Spending Collapses",
        "Ransomware Attack Cripples Major Hospital Network",
        "Internet Outage Causes Widespread Disruption and Panic"
    ]

    HEALTH_TITLES = [
        "Breakthrough Treatment Shows Promise for Disease",
        "Medical Research Reveals New Health Insights",
        "Vaccine Distribution Reaches Milestone",
        "New Drug Approval Offers Hope for Patients",
        "Cancer Research Discovers Promising Therapy",
        "Medical Device Innovation Improves Patient Outcomes",
        "Exercise Linked to Increased Longevity",
        "Telemedicine Services Expand Access to Care",
        "Healthcare Workers Receive Special Recognition",
        "Disease Outbreak Threatens Communities Across Region",
        "Hospital Crisis Worsens as Staff Shortages Escalate",
        "Drug Recall Sparks Fear Over Patient Safety",
        "Mental Health Crisis Deepens Among Young Adults",
        "Healthcare System Collapse Feared in Rural Areas",
        "Deadly Virus Strain Causes Alarm Among Scientists",
        "Antibiotic Resistance Reaches Dangerous Levels",
        "Pollution Linked to Alarming Rise in Cancer Rates",
        "Pandemic Response Failures Under Investigation",
        "Opioid Crisis Claims Record Number of Lives"
    ]

    SCIENCE_TITLES = [
        "Telescope Reveals Distant Galaxies",
        "Significant Scientific Discovery Made",
        "Space Exploration Mission Returns Successfully",
        "Researchers Uncover Ancient Species",
        "Quantum Physics Experiment Yields Unexpected Results",
        "Climate Research Shows Important Trends",
        "Biology Research Advances Disease Understanding",
        "Physics Experiment Challenges Current Theory",
        "Deep Sea Exploration Finds Unknown Creatures",
        "Atmospheric Research Monitors Environmental Change",
        "Chemical Breakthrough Enables New Applications",
        "Genetic Research Reveals Evolutionary Connections",
        "Marine Biology Study Protects Ocean Species",
        "Paleontology Discovery Rewrites History",
        "Solar Energy Research Achieves New Efficiency",
        "Microbiome Study Changes Health Perspectives",
        "Oceanography Expedition Gathers Critical Data",
        "Astronomy Observation Challenges Previous Models",
        "Environmental Science Guides Conservation Efforts",
        "Geological Discovery Provides Geological Insights"
    ]

    SPORTS_TITLES = [
        "Team Secures Championship Victory",
        "Athlete Sets New World Record",
        "League Announces Expansion Plans",
        "Player Signs Record-Breaking Contract",
        "Tournament Draws Record Attendance",
        "Coach Leads Team to Historic Win",
        "Championship Game Delivers Thrilling Finish",
        "Young Athlete Shows Exceptional Promise",
        "Team Defeats Rival in Overtime",
        "Sports Event Breaks Viewership Records",
        "Player Wins Multiple Awards",
        "Team Completes Undefeated Season",
        "Trade Reshapes Team Roster",
        "Olympic Games Begin in City",
        "Athlete Returns After Injury",
        "Sports League Adopts New Rules",
        "Team Building Period Shows Progress",
        "Stadium Opens to Enthusiastic Crowds",
        "Championship Final Approaches Excitement",
        "Hall of Fame Inducts New Members"
    ]

    ENTERTAINMENT_TITLES = [
        "Movie Breaks Box Office Records",
        "Celebrity Announces Exciting Project",
        "Television Series Wins Major Awards",
        "Music Album Tops Charts Worldwide",
        "Film Festival Showcases Innovation",
        "Entertainment Industry Event Draws Crowds",
        "Actor Signs Major Studio Deal",
        "Musician Announces World Tour",
        "Streaming Platform Releases Blockbuster",
        "Theater Production Opens to Critical Acclaim",
        "Celebrity Charity Event Raises Millions",
        "Video Game Launch Exceeds Expectations",
        "Music Awards Celebrate Industry Talent",
        "Documentary Film Receives Praise",
        "Entertainment News Agency Reports Merger",
        "Concert Series Features Global Artists",
        "Television Network Announces Fresh Content",
        "Celebrity Guest Stars in Special Episode",
        "Entertainment Platform Expands Global Reach",
        "Award Show Celebrates Creative Excellence"
    ]

    SOURCES = {
        'technology': ['TechNews Daily', 'InnovateTech', 'Digital Tribune', 'Code Gazette', 'Silicon Review'],
        'business': ['Business Weekly', 'Market Insider', 'Finance Times', 'Corporate Chronicle', 'Trade Journal'],
        'health': ['Health Times', 'Medical News', 'Wellness Daily', 'Doctor\'s Review', 'Life Science'],
        'science': ['Science Today', 'Research Weekly', 'Discovery Journal', 'Academic Press', 'Nature Review'],
        'sports': ['Sports Times', 'Athletic Daily', 'Game Report', 'Sports Chronicle', 'Competition News'],
        'entertainment': ['Entertainment Weekly', 'Celebrity News', 'Show Times', 'Media Report', 'Entertainment Tribune'],
        'general': ['News Daily', 'Current Affairs', 'General Tribune', 'World News', 'Public News']
    }

    def __init__(self):
        self.sentiment = SentimentAnalyzer()

    def generate(self, country: str, category: str, count: int = 20) -> list:
        """Generate realistic news articles."""
        articles = []
        category = category.lower()

        titles = self._get_titles_for_category(category)
        sources = self.SOURCES.get(category, self.SOURCES['general'])

        for _ in range(count):
            title = random.choice(titles)
            source = random.choice(sources)
            author_names = ['John Smith', 'Sarah Johnson', 'Michael Chen', 'Emma Wilson', 'David Brown']
            author = random.choice(author_names)

            # Generate description
            description = self._generate_description(title, category)

            # Generate content
            content = self._generate_content(title, description, category)

            # Generate URL
            url = self._generate_url(title)

            # Generate image URL
            image_url = self._generate_image_url(category)

            # Calculate published date (random within last 7 days)
            days_ago = random.randint(0, 7)
            hours_ago = random.randint(0, 23)
            published_at = (datetime.now() - timedelta(days=days_ago, hours=hours_ago)).isoformat()

            # Calculate sentiment
            sentiment_score = self.sentiment.analyze(title + ' ' + description)

            article = {
                'title': title,
                'description': description,
                'content': content,
                'url': url,
                'image_url': image_url,
                'published_at': published_at,
                'source_name': source,
                'author': author,
                'category': category,
                'country': country,
                'sentiment_score': round(sentiment_score, 2)
            }
            articles.append(article)

        return articles

    def _get_titles_for_category(self, category: str) -> list:
        """Get title templates for a category."""
        titles_map = {
            'technology': self.TECHNOLOGY_TITLES,
            'business': self.BUSINESS_TITLES,
            'health': self.HEALTH_TITLES,
            'science': self.SCIENCE_TITLES,
            'sports': self.SPORTS_TITLES,
            'entertainment': self.ENTERTAINMENT_TITLES,
            'general': self.BUSINESS_TITLES + self.TECHNOLOGY_TITLES
        }
        return titles_map.get(category, titles_map['general'])

    def _generate_description(self, title: str, category: str) -> str:
        """Generate a realistic description."""
        templates = [
            "Recent developments in {category} have shown promising results. Industry experts suggest that {title_lower} could have significant implications.",
            "{title} according to latest reports. This development comes as {category} continues to evolve rapidly in the current market.",
            "Latest news indicates {title_lower}. Analysts believe this trend will continue shaping the {category} landscape.",
            "Significant progress has been made as {title_lower}. Stakeholders in the {category} sector are closely monitoring this evolution.",
            "{title} with potential for widespread impact. Experts forecast continued growth in this {category} area."
        ]

        template = random.choice(templates)
        title_lower = title.lower()
        description = template.format(title_lower=title_lower, title=title, category=category)

        return description

    def _generate_content(self, title: str, description: str, category: str) -> str:
        """Generate article content."""
        sections = [
            description,
            "The recent announcement has attracted significant attention from industry observers and stakeholders. Many experts predict that this development will have lasting effects on the sector.",
            "According to sources within the industry, implementation of these changes is expected to begin in the coming weeks. The transition is anticipated to proceed smoothly, with minimal disruption to ongoing operations.",
            "This milestone represents a significant achievement in the field. Industry leaders have expressed optimism about the potential benefits and opportunities that may emerge.",
            "Looking ahead, analysts expect continued momentum in this area. The coming months will be crucial for assessing the full impact of these developments on the broader market landscape.",
            "For more information and updates on this developing story, stakeholders are encouraged to monitor official announcements and industry publications."
        ]

        content = ' '.join(sections)
        return content

    def _generate_url(self, title: str) -> str:
        """Generate a realistic URL."""
        slug = re.sub(r'[^\w\s-]', '', title.lower())
        slug = re.sub(r'[-\s]+', '-', slug)
        return 'https://news.example.com/articles/' + slug + '/' + str(uuid.uuid4())[:8]

    def _generate_image_url(self, category: str) -> str:
        """Generate an image URL."""
        image_ids = {
            'technology': ['tech-1', 'tech-2', 'tech-3'],
            'business': ['business-1', 'business-2', 'business-3'],
            'health': ['health-1', 'health-2', 'health-3'],
            'science': ['science-1', 'science-2', 'science-3'],
            'sports': ['sports-1', 'sports-2', 'sports-3'],
            'entertainment': ['entertainment-1', 'entertainment-2', 'entertainment-3'],
            'general': ['general-1', 'general-2', 'general-3']
        }

        image_id = random.choice(image_ids.get(category, image_ids['general']))
        return 'https://images.example.com/' + image_id + '.jpg'


class RequestHandler(http.server.SimpleHTTPRequestHandler):
    """Handles HTTP requests with custom routing and CORS."""

    db_service = None
    news_generator = None
    sentiment = None
    keyword_extractor = None
    summarizer = None

    def do_GET(self):
        """Handle GET requests."""
        parsed_path = urlparse(self.path)
        path = parsed_path.path
        query_string = parsed_path.query

        # Handle CORS preflight
        if self.command == 'OPTIONS':
            self.send_response(200)
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Access-Control-Allow-Methods', 'GET, POST, DELETE, OPTIONS')
            self.send_header('Access-Control-Allow-Headers', 'Content-Type')
            self.end_headers()
            return

        # Parse query parameters
        params = parse_qs(query_string)
        for key in params:
            params[key] = params[key][0] if params[key] else None

        logger.info('GET %s', path)

        # Route to appropriate handler
        if path.startswith('/api/'):
            self._handle_api_get(path, params)
        elif path == '/' or path == '':
            self._serve_static_file('index.html')
        else:
            self._serve_static_file(path)

    def do_POST(self):
        """Handle POST requests."""
        parsed_path = urlparse(self.path)
        path = parsed_path.path

        logger.info('POST %s', path)

        # Parse JSON body
        content_length = int(self.headers.get('Content-Length', 0))
        body = ''
        if content_length > 0:
            body = self.rfile.read(content_length).decode('utf-8')

        try:
            data = json.loads(body) if body else {}
        except json.JSONDecodeError:
            self._send_json_response({'error': 'Invalid JSON'}, 400)
            return

        # Route to appropriate handler
        if path.startswith('/api/'):
            self._handle_api_post(path, data)
        else:
            self._send_json_response({'error': 'Not Found'}, 404)

    def do_PUT(self):
        """Handle PUT requests."""
        parsed_path = urlparse(self.path)
        path = parsed_path.path

        logger.info('PUT %s', path)

        content_length = int(self.headers.get('Content-Length', 0))
        body = ''
        if content_length > 0:
            body = self.rfile.read(content_length).decode('utf-8')

        try:
            data = json.loads(body) if body else {}
        except json.JSONDecodeError:
            self._send_json_response({'error': 'Invalid JSON'}, 400)
            return

        if path.startswith('/api/'):
            self._handle_api_post(path, data)
        else:
            self._send_json_response({'error': 'Not Found'}, 404)

    def do_DELETE(self):
        """Handle DELETE requests."""
        parsed_path = urlparse(self.path)
        path = parsed_path.path

        logger.info('DELETE %s', path)

        if path == '/api/history':
            self.db_service.execute('DELETE FROM search_history')
            self._send_json_response({'success': True})
        elif path == '/api/articles':
            self.db_service.execute('DELETE FROM articles')
            self._send_json_response({'success': True})
        elif path.startswith('/api/articles/'):
            article_id = path.rsplit('/', 1)[-1]
            if not article_id.isdigit():
                self._send_json_response({'error': 'Invalid article id'}, 400)
                return
            self.db_service.execute('DELETE FROM articles WHERE id = ?', (int(article_id),))
            self._send_json_response({'success': True})
        else:
            self._send_json_response({'error': 'Not Found'}, 404)

    def do_OPTIONS(self):
        """Handle OPTIONS preflight requests."""
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()

    def _set_cors_headers(self):
        """Set CORS headers."""
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')

    def _handle_api_get(self, path: str, params: dict):
        """Route GET API requests."""
        if path in ('/api/dashboard', '/api/dashboard/stats'):
            self._api_dashboard()
        elif path in ('/api/news/fetch', '/api/news'):
            country = params.get('country', 'us')
            category = params.get('category', 'general')
            count = int(params.get('count', 50))
            self._api_news_fetch(country, category, count)
        elif path == '/api/articles':
            page = int(params.get('page', 1))
            limit = int(params.get('limit', 200))
            sort = params.get('sort', 'published_at')
            order = params.get('order', 'desc')
            self._api_articles(page, limit, sort, order)
        elif path == '/api/analytics':
            self._api_analytics()
        elif path == '/api/analytics/sentiment':
            self._api_analytics_sentiment()
        elif path == '/api/analytics/keywords':
            self._api_analytics_keywords()
        elif path == '/api/history':
            page = int(params.get('page', 1))
            limit = int(params.get('limit', 50))
            self._api_history(page, limit)
        elif path == '/api/settings':
            self._api_settings()
        elif path == '/api/radar/clusters':
            self._send_json_response(self._build_story_clusters(self._get_all_articles()))
        elif path == '/api/sources':
            self._send_json_response(self._build_source_monitor(self._get_all_articles()))
        elif path == '/api/breaking':
            self._send_json_response(self._build_breaking_signals(self._get_all_articles()))
        elif path == '/api/duplicates':
            self._send_json_response(self._build_duplicate_clusters(self._get_all_articles()))
        elif path == '/api/balance':
            self._send_json_response(self._build_source_balance(self._get_all_articles()))
        elif path == '/api/timelines':
            self._send_json_response(self._build_story_timelines(self._get_all_articles()))
        elif path == '/api/auth/session':
            self._api_auth_session()
        else:
            self._send_json_response({'error': 'Not Found'}, 404)

    def _handle_api_post(self, path: str, data: dict):
        """Route POST API requests."""
        if path in ('/api/news/fetch', '/api/news'):
            country = data.get('country', 'us')
            category = data.get('category', 'general')
            count = int(data.get('count', 50))
            self._api_news_fetch(country, category, count)
        elif path in ('/api/summary', '/api/summarize'):
            self._api_summary(data)
        elif path == '/api/summarize/feed':
            self._api_feed_summary()
        elif path.startswith('/api/summarize/article/'):
            article_id = path.rsplit('/', 1)[-1]
            if not article_id.isdigit():
                self._send_json_response({'error': 'Invalid article id'}, 400)
                return
            self._api_summary({'articleId': int(article_id)})
        elif path == '/api/translate':
            self._api_translate(data)
        elif path == '/api/settings':
            self._api_settings_update(data)
        elif path == '/api/auth/login':
            self._api_auth_login(data)
        elif path == '/api/auth/register':
            self._api_auth_register(data)
        else:
            self._send_json_response({'error': 'Not Found'}, 404)

    def _serve_static_file(self, filepath: str):
        """Serve static files from client directory."""
        if filepath.startswith('/'):
            filepath = filepath[1:]

        file_path = STATIC_DIR / filepath

        # Prevent directory traversal
        try:
            file_path = file_path.resolve()
            if not str(file_path).startswith(str(STATIC_DIR.resolve())):
                self._send_json_response({'error': 'Forbidden'}, 403)
                return
        except (ValueError, OSError):
            self._send_json_response({'error': 'Bad Request'}, 400)
            return

        # Serve file
        if file_path.exists() and file_path.is_file():
            self.send_response(200)
            content_type = self._get_content_type(file_path.suffix)
            self.send_header('Content-type', content_type)
            self.end_headers()
            with open(file_path, 'rb') as f:
                self.wfile.write(f.read())
        elif filepath == '' or filepath == 'index.html' or '.' not in Path(filepath).name:
            # Fallback to index.html
            index_path = STATIC_DIR / 'index.html'
            if index_path.exists():
                self.send_response(200)
                self.send_header('Content-type', 'text/html')
                self.end_headers()
                with open(index_path, 'rb') as f:
                    self.wfile.write(f.read())
            else:
                self._send_json_response({'error': 'Not Found'}, 404)
        else:
            self._send_json_response({'error': 'Not Found'}, 404)

    def _get_content_type(self, extension: str) -> str:
        """Determine content type from file extension."""
        types = {
            '.html': 'text/html',
            '.css': 'text/css',
            '.js': 'application/javascript',
            '.json': 'application/json',
            '.png': 'image/png',
            '.jpg': 'image/jpeg',
            '.jpeg': 'image/jpeg',
            '.gif': 'image/gif',
            '.svg': 'image/svg+xml',
            '.txt': 'text/plain'
        }
        return types.get(extension, 'application/octet-stream')

    def _parse_datetime(self, value: str):
        """Parse ISO timestamps safely."""
        if not value:
            return None

        candidates = [value]
        if value.endswith('Z'):
            candidates.append(value.replace('Z', '+00:00'))

        for candidate in candidates:
            try:
                parsed = datetime.fromisoformat(candidate)
                if parsed.tzinfo is not None:
                    parsed = parsed.astimezone().replace(tzinfo=None)
                return parsed
            except ValueError:
                continue
        return None

    def _relative_time(self, value: str) -> str:
        """Format timestamps for compact UI display."""
        parsed = self._parse_datetime(value)
        if not parsed:
            return 'Unknown'

        delta = datetime.now() - parsed
        minutes = max(0, int(delta.total_seconds() // 60))
        if minutes < 1:
            return 'Just now'
        if minutes < 60:
            return f'{minutes}m ago'
        hours = minutes // 60
        if hours < 24:
            return f'{hours}h ago'
        days = hours // 24
        if days < 7:
            return f'{days}d ago'
        return parsed.strftime('%b %d')

    def _get_all_articles(self, limit: int = 200) -> list:
        """Return the latest stored articles."""
        return self.db_service.fetch_all(
            'SELECT * FROM articles ORDER BY COALESCE(published_at, created_at) DESC LIMIT ?',
            (limit,)
        )

    def _tokenize_story_terms(self, text: str) -> list:
        """Tokenize article text for clustering heuristics."""
        stop_words = getattr(self.keyword_extractor, 'STOP_WORDS', set())
        return [
            token for token in re.findall(r'[a-z0-9]+', (text or '').lower())
            if len(token) > 2 and token not in stop_words
        ]

    def _cluster_signature(self, article: dict, token_count: int = 2) -> str:
        """Create a simple clustering signature from article copy."""
        tokens = self._tokenize_story_terms(
            f"{article.get('title', '')} {article.get('description', '')}"
        )
        signature_tokens = tokens[:token_count] or [article.get('category', 'general')]
        return ' '.join(signature_tokens)

    def _cluster_label(self, signature: str, fallback: str = 'Top Story') -> str:
        """Create a readable label from a cluster signature."""
        if not signature:
            return fallback
        return ' '.join(word.capitalize() for word in signature.split())

    def _tone_label(self, sentiment_score: float) -> str:
        """Convert numeric sentiment to a UI label."""
        if sentiment_score is None:
            return 'Neutral'
        if sentiment_score > 0.15:
            return 'Positive'
        if sentiment_score < -0.15:
            return 'Negative'
        return 'Neutral'

    def _urgency_score(self, article: dict) -> tuple[int, list]:
        """Score urgency from recency, title, and tone."""
        title = (article.get('title') or '').lower()
        score = 25
        reasons = []

        keyword_weights = {
            'breaking': 28,
            'emergency': 24,
            'crisis': 22,
            'threat': 18,
            'attack': 18,
            'breach': 18,
            'crash': 20,
            'collapse': 20,
            'outbreak': 18,
            'warning': 14,
            'lawsuit': 12,
            'layoffs': 16,
            'recall': 16,
            'urgent': 18,
            'investigation': 12,
            'alarm': 14,
            'danger': 18,
        }

        for keyword, weight in keyword_weights.items():
            if keyword in title:
                score += weight
                reasons.append(keyword.replace('_', ' '))

        published_at = article.get('published_at') or article.get('created_at')
        published_dt = self._parse_datetime(published_at)
        if published_dt:
            hours_old = max(0, (datetime.now() - published_dt).total_seconds() / 3600)
            freshness_bonus = max(0, 24 - int(hours_old))
            score += min(24, freshness_bonus)
            if hours_old <= 3:
                reasons.append('very recent coverage')

        sentiment_score = abs(article.get('sentiment_score') or 0)
        score += min(12, int(sentiment_score * 40))
        if sentiment_score >= 0.35:
            reasons.append('strong tone signal')

        return min(100, score), reasons[:3]

    def _build_story_clusters(self, articles: list, limit: int = 6) -> list:
        """Build story clusters for the frontend."""
        clusters = {}

        for article in articles:
            signature = self._cluster_signature(article, 2)
            entry = clusters.setdefault(signature, [])
            entry.append(article)

        ranked = sorted(clusters.items(), key=lambda item: len(item[1]), reverse=True)[:limit]
        result = []
        for signature, grouped_articles in ranked:
            sources = {article.get('source_name') for article in grouped_articles if article.get('source_name')}
            article_count = len(grouped_articles)
            source_count = len(sources)
            confidence = min(99, 55 + article_count * 6 + source_count * 3)
            result.append({
                'id': signature.replace(' ', '-') or str(uuid.uuid4())[:8],
                'label': self._cluster_label(signature, grouped_articles[0].get('category', 'Top Story').title()),
                'articleCount': article_count,
                'sourceCount': source_count,
                'leadHeadlines': [article.get('title') for article in grouped_articles[:3] if article.get('title')],
                'confidence': confidence,
            })
        return result

    def _build_source_monitor(self, articles: list) -> list:
        """Build source monitor rows."""
        if not articles:
            return []

        grouped = {}
        total = len(articles)
        for article in articles:
            source_name = article.get('source_name') or 'Unknown Source'
            entry = grouped.setdefault(source_name, {'articles': [], 'sentiments': []})
            entry['articles'].append(article)
            entry['sentiments'].append(article.get('sentiment_score') or 0)

        result = []
        for source_name, data in grouped.items():
            source_articles = sorted(
                data['articles'],
                key=lambda article: self._parse_datetime(article.get('published_at') or article.get('created_at')) or datetime.min,
                reverse=True
            )
            average_sentiment = sum(data['sentiments']) / max(1, len(data['sentiments']))
            result.append({
                'name': source_name,
                'articleCount': len(source_articles),
                'averageTone': self._tone_label(average_sentiment),
                'latestHeadline': source_articles[0].get('title', 'No headline available'),
                'freshness': self._relative_time(source_articles[0].get('published_at') or source_articles[0].get('created_at')),
                'coverageShare': round((len(source_articles) / total) * 100, 1),
            })

        return sorted(result, key=lambda source: source['articleCount'], reverse=True)

    def _build_breaking_signals(self, articles: list, limit: int = 10) -> list:
        """Build breaking-watch items."""
        signals = []
        for article in articles:
            urgency_score, reasons = self._urgency_score(article)
            if urgency_score < 45:
                continue
            reason = ', '.join(reasons) if reasons else 'developing coverage pattern'
            signals.append({
                'id': str(article.get('id') or uuid.uuid4()) + '-breaking',
                'headline': article.get('title', 'Untitled headline'),
                'source': article.get('source_name', 'Unknown Source'),
                'reason': reason,
                'urgencyScore': urgency_score,
                'publishTime': article.get('published_at') or article.get('created_at'),
            })

        signals.sort(key=lambda item: item['urgencyScore'], reverse=True)
        return signals[:limit]

    def _build_duplicate_clusters(self, articles: list, limit: int = 8) -> list:
        """Detect repeated stories from normalized title signatures."""
        clusters = {}
        for article in articles:
            signature = self._cluster_signature(article, 4)
            if not signature:
                continue
            clusters.setdefault(signature, []).append(article)

        result = []
        for signature, grouped_articles in clusters.items():
            if len(grouped_articles) < 2:
                continue
            unique_sources = {article.get('source_name') for article in grouped_articles if article.get('source_name')}
            duplicate_score = min(100, 50 + len(grouped_articles) * 8 + len(unique_sources) * 4)
            syndication_signal = 'High Syndication' if len(unique_sources) >= 4 else 'Shared Story'
            if len(grouped_articles) >= 6:
                syndication_signal = 'Major Coverage'
            result.append({
                'id': signature.replace(' ', '-') or str(uuid.uuid4())[:8],
                'representativeHeadline': grouped_articles[0].get('title', 'Repeated story'),
                'duplicateScore': duplicate_score,
                'sourceCount': len(unique_sources),
                'articleCount': len(grouped_articles),
                'repeatedHeadlines': [article.get('title') for article in grouped_articles[:5] if article.get('title')],
                'syndicationSignal': syndication_signal,
            })

        result.sort(key=lambda item: item['duplicateScore'], reverse=True)
        return result[:limit]

    def _build_source_balance(self, articles: list) -> dict:
        """Compute source balance summary for the frontend."""
        sources = self._build_source_monitor(articles)
        if not sources:
            return {
                'score': 0,
                'coverageRisk': 'No data',
                'dominantSource': 'N/A',
                'dominantShare': 0,
                'toneSpread': 'No data',
                'sources': [],
            }

        shares = [source['coverageShare'] / 100 for source in sources]
        concentration = sum(share * share for share in shares)
        dominant_source = sources[0]
        dominant_share = dominant_source['coverageShare']
        score = max(0, min(100, round(100 - (dominant_share * 1.2) - (concentration * 120))))
        coverage_risk = 'Low'
        if dominant_share >= 35 or concentration >= 0.22:
            coverage_risk = 'Moderate'
        if dominant_share >= 50 or concentration >= 0.35:
            coverage_risk = 'High'

        distinct_tones = {source['averageTone'] for source in sources if source.get('averageTone')}
        if len(distinct_tones) >= 3:
            tone_spread = 'Balanced'
        elif len(distinct_tones) == 2:
            tone_spread = 'Mixed'
        else:
            tone_spread = next(iter(distinct_tones)) if distinct_tones else 'Neutral'

        return {
            'score': score,
            'coverageRisk': coverage_risk,
            'dominantSource': dominant_source['name'],
            'dominantShare': round(dominant_share, 1),
            'toneSpread': tone_spread,
            'sources': [
                {
                    'name': source['name'],
                    'share': source['coverageShare'],
                    'tone': source['averageTone'],
                }
                for source in sources[:8]
            ],
        }

    def _build_story_timelines(self, articles: list, limit: int = 6) -> list:
        """Build story timelines from cluster groups."""
        clusters = {}
        for article in articles:
            signature = self._cluster_signature(article, 2)
            clusters.setdefault(signature, []).append(article)

        ranked = sorted(clusters.items(), key=lambda item: len(item[1]), reverse=True)[:limit]
        timelines = []

        for signature, grouped_articles in ranked:
            ordered_articles = sorted(
                grouped_articles,
                key=lambda article: self._parse_datetime(article.get('published_at') or article.get('created_at')) or datetime.min
            )

            if not ordered_articles:
                continue

            seen_sources = set()
            last_tone = None
            events = []
            for index, article in enumerate(ordered_articles[:8]):
                urgency_score, _ = self._urgency_score(article)
                article_tone = self._tone_label(article.get('sentiment_score') or 0)
                event_type = 'followup'
                if index == 0:
                    event_type = 'initial_report'
                elif article.get('source_name') not in seen_sources:
                    event_type = 'new_source'
                elif urgency_score >= 80:
                    event_type = 'escalation'
                elif last_tone and article_tone != last_tone:
                    event_type = 'tone_shift'
                elif index <= 2:
                    event_type = 'rapid_followup'

                seen_sources.add(article.get('source_name'))
                last_tone = article_tone
                events.append({
                    'id': str(article.get('id') or uuid.uuid4()) + f'-{index}',
                    'type': event_type,
                    'headline': article.get('title', 'Untitled headline'),
                    'source': article.get('source_name', 'Unknown Source'),
                    'timestamp': article.get('published_at') or article.get('created_at'),
                })

            start_dt = self._parse_datetime(ordered_articles[0].get('published_at') or ordered_articles[0].get('created_at'))
            end_dt = self._parse_datetime(ordered_articles[-1].get('published_at') or ordered_articles[-1].get('created_at'))
            if start_dt and end_dt:
                coverage_window = f'{start_dt.strftime("%b %d %H:%M")} - {end_dt.strftime("%b %d %H:%M")}'
            else:
                coverage_window = 'Recent coverage'

            timelines.append({
                'id': signature.replace(' ', '-') or str(uuid.uuid4())[:8],
                'label': self._cluster_label(signature, ordered_articles[0].get('category', 'Story').title()),
                'coverageWindow': coverage_window,
                'sourceCount': len({article.get("source_name") for article in grouped_articles if article.get("source_name")}),
                'events': events,
            })

        return timelines

    def _build_feed_briefing(self, articles: list) -> dict:
        """Build a feed-wide AI briefing."""
        clusters = self._build_story_clusters(articles, limit=3)
        balance = self._build_source_balance(articles)
        breaking = self._build_breaking_signals(articles, limit=3)
        duplicate_clusters = self._build_duplicate_clusters(articles, limit=3)

        lead_cluster = clusters[0] if clusters else None
        lead_story = lead_cluster['label'] if lead_cluster else 'No dominant story yet'
        breaking_line = breaking[0]['headline'] if breaking else 'No urgent headlines detected'

        summary_parts = [
            f'Lead story: {lead_story}.',
            f'The current feed includes {len(articles)} stored articles across {len(balance["sources"])} active sources.',
            f'Primary urgent signal: {breaking_line}.',
        ]

        if duplicate_clusters:
            summary_parts.append(
                f'Duplicate pressure is led by "{duplicate_clusters[0]["representativeHeadline"]}" across {duplicate_clusters[0]["sourceCount"]} sources.'
            )

        key_insights = [
            f'Source balance: {balance["coverageRisk"]} risk, score {balance["score"]}/100.',
            f'Dominant source: {balance["dominantSource"]} at {balance["dominantShare"]}%.',
        ]
        if lead_cluster:
            key_insights.append(
                f'Top cluster "{lead_cluster["label"]}" spans {lead_cluster["articleCount"]} articles.'
            )
        if breaking:
            key_insights.append(f'Highest urgency item scored {breaking[0]["urgencyScore"]}.')

        return {
            'summary': ' '.join(summary_parts),
            'keyInsights': key_insights,
            'metadata': {
                'wordCount': sum(len((article.get('content') or '').split()) for article in articles),
                'readingTime': max(1, len(articles) // 5),
                'processedAt': datetime.now().isoformat(),
            },
        }

    def _api_dashboard(self):
        """GET /api/dashboard"""
        total_articles = self.db_service.fetch_one('SELECT COUNT(*) as count FROM articles')['count']
        total_analyses = self.db_service.fetch_one('SELECT COUNT(*) as count FROM search_history')['count']

        articles = self._get_all_articles()
        sentiments = [article.get('sentiment_score') or 0 for article in articles if article.get('sentiment_score') is not None]
        avg_sentiment = sum(sentiments) / len(sentiments) if sentiments else 0

        recent = self.db_service.fetch_all(
            'SELECT action_type, query, created_at FROM search_history ORDER BY created_at DESC LIMIT 10'
        )
        recent_activity = [
            {
                'type': item['action_type'],
                'description': item['query'] or item['action_type'],
                'timestamp': item['created_at']
            }
            for item in recent
        ]

        source_balance = self._build_source_balance(articles)
        story_clusters = self._build_story_clusters(articles, limit=3)
        breaking_items = self._build_breaking_signals(articles, limit=5)
        duplicate_clusters = self._build_duplicate_clusters(articles, limit=5)
        unique_sources = len({article.get('source_name') for article in articles if article.get('source_name')})
        source_diversity = round(min(100, (unique_sources / max(1, min(total_articles, 12))) * 100)) if total_articles else 0
        feed_health = max(0, min(100, round((source_balance['score'] * 0.55) + (source_diversity * 0.45))))
        duplicate_pressure = duplicate_clusters[0]['duplicateScore'] if duplicate_clusters else 0
        urgency_level = round(sum(item['urgencyScore'] for item in breaking_items) / len(breaking_items)) if breaking_items else 0
        concentration_score = round(max((source.get('share') or 0) for source in source_balance['sources']), 1) if source_balance['sources'] else 0
        latest_narrative_shift = story_clusters[0]['leadHeadlines'][0] if story_clusters and story_clusters[0]['leadHeadlines'] else 'No story shifts detected yet'
        ai_pulse = (
            f'{len(story_clusters)} live narrative clusters, {len(breaking_items)} urgent signals, '
            f'and {len(source_balance["sources"])} active sources in the current feed.'
        )

        # Generate trends data (7 days)
        article_trends = []
        sentiment_trends = []
        for i in range(7, 0, -1):
            date = (datetime.now() - timedelta(days=i)).strftime('%Y-%m-%d')
            count = self.db_service.fetch_one(
                'SELECT COUNT(*) as count FROM articles WHERE DATE(published_at) = ?',
                (date,)
            )['count']
            article_trends.append(count)

            sentiment_avg = self.db_service.fetch_one(
                'SELECT AVG(sentiment_score) as avg FROM articles WHERE DATE(published_at) = ?',
                (date,)
            )['avg'] or 0
            sentiment_trends.append(round(sentiment_avg, 2))

        response = {
            'totalArticles': total_articles,
            'analyzesDone': total_analyses,
            'analysesDone': total_analyses,
            'analysesPerformed': total_analyses,
            'avgSentiment': round(avg_sentiment, 2),
            'averageSentiment': round(avg_sentiment, 2),
            'recentActions': len(recent_activity),
            'recentActionsCount': len(recent_activity),
            'recentActivityCount': len(recent_activity),
            'feedHealth': feed_health,
            'sourceDiversity': source_diversity,
            'duplicatePressure': duplicate_pressure,
            'urgencyLevel': urgency_level,
            'aiPulseSummary': ai_pulse,
            'latestNarrativeShifts': latest_narrative_shift,
            'concentrationScore': concentration_score,
            'sourceBalanceSnapshot': source_balance,
            'lastFetchTimestamp': recent_activity[0]['timestamp'] if recent_activity else None,
            'lastAnalysisTimestamp': recent_activity[0]['timestamp'] if recent_activity else None,
            'recentActivity': recent_activity,
            'trends': {
                'articles': article_trends,
                'sentiment': sentiment_trends
            }
        }

        self._send_json_response(response)

    def _api_news_fetch(self, country: str, category: str, count: int):
        """GET /api/news/fetch"""
        articles = self.news_generator.generate(country, category, min(count, 60))

        # Store in database
        for article in articles:
            existing = self.db_service.fetch_one('SELECT id FROM articles WHERE url = ?', (article['url'],))
            if existing:
                continue
            self.db_service.execute(
                '''INSERT INTO articles (title, description, content, url, image_url, published_at,
                   source_name, author, category, country, sentiment_score)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)''',
                (article['title'], article['description'], article['content'],
                 article['url'], article['image_url'], article['published_at'],
                 article['source_name'], article['author'], article['category'],
                 article['country'], article['sentiment_score'])
            )

        # Log to search history
        self.db_service.execute(
            'INSERT INTO search_history (action_type, query, result_summary) VALUES (?, ?, ?)',
            ('fetch', country + '/' + category, str(len(articles)) + ' articles fetched')
        )

        stored_articles = self.db_service.fetch_all(
            'SELECT * FROM articles WHERE country = ? AND category = ? ORDER BY published_at DESC LIMIT ?',
            (country, category, min(count, 60))
        )

        response = {
            'success': True,
            'articles': stored_articles,
            'total': len(stored_articles)
        }

        self._send_json_response(response)

    def _api_articles(self, page: int, limit: int, sort: str, order: str):
        """GET /api/articles"""
        # Validate sort column
        allowed_sorts = ['id', 'title', 'published_at', 'sentiment_score', 'created_at']
        if sort not in allowed_sorts:
            sort = 'published_at'

        order = 'DESC' if order.upper() == 'DESC' else 'ASC'

        total = self.db_service.fetch_one('SELECT COUNT(*) as count FROM articles')['count']
        offset = (page - 1) * limit

        articles = self.db_service.fetch_all(
            'SELECT * FROM articles ORDER BY {} {} LIMIT ? OFFSET ?'.format(sort, order),
            (limit, offset)
        )

        pages = (total + limit - 1) // limit

        response = {
            'articles': articles,
            'total': total,
            'page': page,
            'limit': limit,
            'pages': pages
        }

        self._send_json_response(response)

    def _api_analytics(self):
        """GET /api/analytics"""
        total_articles = self.db_service.fetch_one('SELECT COUNT(*) as count FROM articles')['count']
        total_analyses = self.db_service.fetch_one('SELECT COUNT(*) as count FROM search_history')['count']

        articles = self._get_all_articles()
        avg_sentiment = sum((article.get('sentiment_score') or 0) for article in articles) / len(articles) if articles else 0

        # Sentiment distribution
        positive = sum(1 for a in articles if (a.get('sentiment_score') or 0) > 0.1)
        negative = sum(1 for a in articles if (a.get('sentiment_score') or 0) < -0.1)
        neutral = len(articles) - positive - negative

        sentiment_dist = {
            'positive': positive,
            'neutral': neutral,
            'negative': negative
        }

        # Top sources
        sources = self.db_service.fetch_all(
            'SELECT source_name, COUNT(*) as count FROM articles GROUP BY source_name ORDER BY count DESC LIMIT 10'
        )
        top_sources = [{'name': s['source_name'], 'count': s['count']} for s in sources]

        # Category counts
        categories = self.db_service.fetch_all(
            'SELECT category, COUNT(*) as count FROM articles GROUP BY category'
        )
        category_counts = [{'category': c['category'], 'count': c['count']} for c in categories]

        # Articles by day
        articles_by_day = self.db_service.fetch_all(
            'SELECT DATE(published_at) as date, COUNT(*) as count FROM articles GROUP BY DATE(published_at) ORDER BY date DESC LIMIT 30'
        )
        article_days = [{'date': a['date'], 'count': a['count']} for a in articles_by_day]

        recent = self.db_service.fetch_all(
            'SELECT action_type, query, created_at FROM search_history ORDER BY created_at DESC LIMIT 20'
        )
        recent_activity = [
            {
                'type': item['action_type'],
                'description': item['query'] or item['action_type'],
                'timestamp': item['created_at']
            }
            for item in recent
        ]

        story_clusters = self._build_story_clusters(articles, limit=3)
        source_balance = self._build_source_balance(articles)

        response = {
            'totalArticles': total_articles,
            'totalAnalyses': total_analyses,
            'avgSentiment': round(avg_sentiment, 2),
            'averageSentiment': round(avg_sentiment, 2),
            'sentimentDistribution': sentiment_dist,
            'sentimentBreakdown': sentiment_dist,
            'topSources': top_sources,
            'topSource': top_sources[0]['name'] if top_sources else 'N/A',
            'categoryCounts': category_counts,
            'categoryBreakdown': category_counts,
            'articlesByDay': article_days,
            'recentActivity': recent_activity,
            'sourceDiversity': round(min(100, len(top_sources) / max(1, min(total_articles, 10)) * 100)) if total_articles else 0,
            'dominantCluster': story_clusters[0]['label'] if story_clusters else 'No dominant cluster yet',
            'coverageConcentration': source_balance['dominantShare'],
            'feedHealth': source_balance['score'],
        }

        self._send_json_response(response)

    def _api_analytics_sentiment(self):
        """GET /api/analytics/sentiment"""
        articles = self.db_service.fetch_all('SELECT sentiment_score, category, source_name, published_at FROM articles')

        # Distribution
        positive = sum(1 for a in articles if a['sentiment_score'] > 0.1)
        negative = sum(1 for a in articles if a['sentiment_score'] < -0.1)
        neutral = len(articles) - positive - negative

        distribution = {
            'positive': positive,
            'neutral': neutral,
            'negative': negative
        }

        # Over time (7 days)
        over_time = []
        for i in range(7, 0, -1):
            date = (datetime.now() - timedelta(days=i)).strftime('%Y-%m-%d')
            day_articles = [a for a in articles if a['published_at'].startswith(date)]

            pos = sum(1 for a in day_articles if a['sentiment_score'] > 0.1)
            neg = sum(1 for a in day_articles if a['sentiment_score'] < -0.1)
            neut = len(day_articles) - pos - neg

            over_time.append({
                'date': date,
                'positive': pos,
                'neutral': neut,
                'negative': neg
            })

        # By category
        by_category = {}
        for article in articles:
            cat = article['category']
            if cat not in by_category:
                by_category[cat] = {'total': 0, 'sum': 0, 'count': 0}
            by_category[cat]['sum'] += article['sentiment_score']
            by_category[cat]['count'] += 1

        by_cat_list = []
        for cat, data in by_category.items():
            avg_sentiment = data['sum'] / data['count'] if data['count'] > 0 else 0
            by_cat_list.append({
                'category': cat,
                'positive': sum(1 for a in articles if a['category'] == cat and a['sentiment_score'] > 0.1),
                'neutral': sum(1 for a in articles if a['category'] == cat and -0.1 <= a['sentiment_score'] <= 0.1),
                'negative': sum(1 for a in articles if a['category'] == cat and a['sentiment_score'] < -0.1)
            })

        # By source
        by_source = {}
        for article in articles:
            src = article['source_name']
            if src not in by_source:
                by_source[src] = {'sum': 0, 'count': 0}
            by_source[src]['sum'] += article['sentiment_score']
            by_source[src]['count'] += 1

        by_src_list = []
        for src, data in by_source.items():
            avg_sentiment = data['sum'] / data['count'] if data['count'] > 0 else 0
            by_src_list.append({
                'source': src,
                'avgSentiment': round(avg_sentiment, 2),
                'count': data['count']
            })

        response = {
            'distribution': distribution,
            'overTime': over_time,
            'byCategory': by_cat_list,
            'bySource': by_src_list
        }

        self._send_json_response(response)

    def _api_analytics_keywords(self):
        """GET /api/analytics/keywords"""
        articles = self.db_service.fetch_all('SELECT title, description FROM articles')
        keywords = self.keyword_extractor.extract(articles, 50)

        response = {
            'keywords': keywords
        }

        self._send_json_response(response)

    def _api_feed_summary(self):
        """POST /api/summarize/feed"""
        articles = self._get_all_articles()
        if not articles:
            self._send_json_response({'error': 'No articles available to summarize'}, 404)
            return

        response = self._build_feed_briefing(articles)
        self.db_service.execute(
            'INSERT INTO search_history (action_type, query, result_summary) VALUES (?, ?, ?)',
            ('AI Summary', 'Feed briefing', response['summary'][:200])
        )
        self._send_json_response(response)

    def _api_summary(self, data: dict):
        """POST /api/summary"""
        article_id = data.get('articleId')
        url = data.get('url')

        if article_id:
            article = self.db_service.fetch_one('SELECT * FROM articles WHERE id = ?', (article_id,))
        elif url:
            article = self.db_service.fetch_one('SELECT * FROM articles WHERE url = ?', (url,))
        else:
            self._send_json_response({'error': 'Missing articleId or url'}, 400)
            return

        if not article:
            if url:
                fallback_title = Path(urlparse(url).path).stem.replace('-', ' ').replace('_', ' ').strip().title()
                fallback_title = fallback_title or 'External Article'
                synthetic_content = (
                    f'This external article reference points to {url}. '
                    f'The page was not found in the local article store, so NewsVisualizer generated a best-effort preview '
                    f'based on the URL and current feed context.'
                )
                article = {
                    'id': None,
                    'title': fallback_title,
                    'content': synthetic_content,
                    'url': url,
                    'source_name': urlparse(url).netloc or 'External Source',
                    'published_at': datetime.now().isoformat(),
                    'sentiment_score': self.sentiment.analyze(fallback_title),
                }
            else:
                self._send_json_response({'error': 'Article not found'}, 404)
                return

        summary_data = self.summarizer.summarize(article['content'], article['title'])

        self.db_service.execute(
            'INSERT INTO search_history (action_type, query, result_summary) VALUES (?, ?, ?)',
            ('AI Summary', article.get('url') or article.get('title', 'Article summary'), summary_data['summary'][:200])
        )

        response = {
            'title': article['title'],
            'summary': summary_data['summary'],
            'keyPoints': summary_data['key_points'],
            'keyInsights': summary_data['key_points'],
            'metadata': {
                'wordCount': summary_data['word_count'],
                'readingTime': summary_data['reading_time'],
                'sentiment': article['sentiment_score'],
                'source': article['source_name'],
                'publishedAt': article['published_at'],
                'processedAt': datetime.now().isoformat(),
            }
        }

        self._send_json_response(response)

    def _api_translate(self, data: dict):
        """POST /api/translate"""
        text = data.get('text', '')
        mode = data.get('mode', 'translate')
        from_lang = data.get('fromLanguage', 'en')
        to_lang = data.get('toLanguage') or data.get('targetLang', 'es')
        detected_language = None

        if mode == 'translate':
            result = f'Translation ({from_lang} → {to_lang}):\n\n"{text}"\n\nThe text has been processed for translation from {from_lang} to {to_lang}. In a production environment, this would use an AI translation service (OpenAI, Google Translate, or DeepL) for accurate results.\n\n[Processed using local translation engine]'
        elif mode == 'define':
            word = text.strip().split()[0] if text.strip() else text
            result = f'Definition of "{word}":\n\n'
            result += f'Word: {word}\n'
            result += f'Part of Speech: noun / verb / adjective (context-dependent)\n\n'
            result += f'1. A term commonly used in English language with multiple contextual meanings.\n'
            result += f'2. Can function in various grammatical roles depending on sentence structure.\n\n'
            result += f'Example: "The concept of {word} is fundamental to understanding the topic."\n\n'
            result += f'Etymology: Derived from common English usage.\n'
            result += f'Synonyms: Related terms vary by context.\n\n'
            result += f'[Processed using local dictionary engine]'
        elif mode == 'explain':
            word_count = len(text.split())
            result = f'Explanation of: "{text}"\n\n'
            result += f'This is a {word_count}-word expression in English.\n\n'
            if word_count == 1:
                result += f'"{text}" is a single word that can have various meanings depending on context. '
                result += f'It is commonly used in everyday communication and may function as different parts of speech.\n\n'
            elif word_count <= 5:
                result += f'This short phrase expresses a concise idea. '
                result += f'The combination of these words creates a specific meaning that depends on the broader context in which it appears.\n\n'
            else:
                result += f'This sentence conveys a complete thought. '
                result += f'The grammatical structure follows standard English patterns. '
                result += f'Key terms in this expression include: {", ".join(text.split()[:3])}.\n\n'
            result += f'[Processed using local language analysis engine]'
        elif mode == 'detect':
            # Simple language detection heuristic
            has_accents = any(ord(c) > 127 for c in text)
            common_en = {'the', 'is', 'a', 'an', 'and', 'or', 'to', 'in', 'of', 'for'}
            words = set(text.lower().split())
            en_overlap = len(words & common_en)
            if en_overlap >= 2:
                detected = 'English'
                confidence = '95%'
            elif has_accents:
                detected = 'Non-English (contains special characters)'
                confidence = '60%'
            else:
                detected = 'English (probable)'
                confidence = '75%'
            detected_language = detected
            result = f'Language Detection Result:\n\n'
            result += f'Input: "{text[:100]}{"..." if len(text) > 100 else ""}"\n\n'
            result += f'Detected Language: {detected}\n'
            result += f'Confidence: {confidence}\n'
            result += f'Script: Latin\n'
            result += f'Word Count: {len(text.split())}\n\n'
            result += f'[Processed using local language detection engine]'
        else:
            result = text

        # Log to search history
        action_type = 'Translation' if mode == 'translate' else mode.capitalize()
        self.db_service.execute(
            'INSERT INTO search_history (action_type, query, details, result_summary) VALUES (?, ?, ?, ?)',
            (action_type, text[:200], f'Mode: {mode}, From: {from_lang}, To: {to_lang}', result[:200])
        )

        response = {
            'result': result,
            'mode': mode,
            'fromLanguage': from_lang,
            'toLanguage': to_lang,
            'provider': 'local',
            'originalText': text,
            'translatedText': result,
            'detectedLanguage': detected_language,
            'targetLanguage': to_lang,
        }

        self._send_json_response(response)

    def _api_history(self, page: int, limit: int):
        """GET /api/history"""
        total = self.db_service.fetch_one('SELECT COUNT(*) as count FROM search_history')['count']
        offset = (page - 1) * limit

        history = self.db_service.fetch_all(
            'SELECT * FROM search_history ORDER BY created_at DESC LIMIT ? OFFSET ?',
            (limit, offset)
        )

        response = {
            'history': history,
            'total': total,
            'page': page,
            'limit': limit
        }

        self._send_json_response(response)

    def _api_settings(self):
        """GET /api/settings"""
        settings = self.db_service.fetch_all('SELECT key, value FROM app_settings')
        response = {item['key']: item['value'] for item in settings}
        self._send_json_response(response)

    def _api_settings_update(self, data: dict):
        """POST /api/settings"""
        key = data.get('key')
        value = data.get('value')

        if not key:
            self._send_json_response({'error': 'Missing key'}, 400)
            return

        try:
            self.db_service.execute(
                'INSERT OR REPLACE INTO app_settings (key, value, updated_at) VALUES (?, ?, ?)',
                (key, value, datetime.now().isoformat())
            )
            self._send_json_response({'success': True})
        except Exception as e:
            self._send_json_response({'error': str(e)}, 500)

    def _api_auth_login(self, data: dict):
        """POST /api/auth/login"""
        username = data.get('username')
        password = data.get('password')

        if not username or not password:
            self._send_json_response({'error': 'Missing username or password'}, 400)
            return

        password_hash = hashlib.sha256(password.encode()).hexdigest()
        user = self.db_service.fetch_one(
            'SELECT id, username, email, first_name, last_name FROM users WHERE username = ? AND password_hash = ?',
            (username, password_hash)
        )

        if user:
            self.db_service.execute(
                'UPDATE users SET last_login_at = ? WHERE id = ?',
                (datetime.now().isoformat(), user['id'])
            )

            self._send_json_response({
                'success': True,
                'user': dict(user),
                'token': str(uuid.uuid4())
            })
        else:
            self._send_json_response({'error': 'Invalid credentials'}, 401)

    def _api_auth_register(self, data: dict):
        """POST /api/auth/register"""
        username = data.get('username')
        email = data.get('email')
        password = data.get('password')
        first_name = data.get('firstName', '')
        last_name = data.get('lastName', '')

        if not username or not email or not password:
            self._send_json_response({'error': 'Missing required fields'}, 400)
            return

        password_hash = hashlib.sha256(password.encode()).hexdigest()

        try:
            user_id = self.db_service.execute(
                'INSERT INTO users (username, email, password_hash, first_name, last_name) VALUES (?, ?, ?, ?, ?)',
                (username, email, password_hash, first_name, last_name)
            )

            self._send_json_response({
                'success': True,
                'userId': user_id,
                'token': str(uuid.uuid4())
            }, 201)
        except sqlite3.IntegrityError:
            self._send_json_response({'error': 'Username or email already exists'}, 409)

    def _api_auth_session(self):
        """GET /api/auth/session"""
        # For now, return default admin session
        response = {
            'authenticated': True,
            'user': {
                'id': 1,
                'username': 'admin',
                'email': 'admin@newsvisualizer.local',
                'first_name': 'Admin',
                'last_name': 'User'
            }
        }

        self._send_json_response(response)

    def _send_json_response(self, data: dict, status: int = 200):
        """Send JSON response with proper headers."""
        self.send_response(status)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()
        self.wfile.write(json.dumps(data, default=str).encode('utf-8'))

    def log_message(self, format, *args):
        """Override to use logging."""
        logger.info(format % args)


class ThreadingHTTPServer(socketserver.ThreadingMixIn, http.server.HTTPServer):
    """Threaded HTTP server."""
    daemon_threads = True


def main():
    """Start the server."""
    global RequestHandler

    # Initialize services
    db_service = DatabaseService(str(DB_PATH))
    news_generator = NewsGenerator()
    sentiment = SentimentAnalyzer()
    keyword_extractor = KeywordExtractor()
    summarizer = Summarizer()

    # Attach services to request handler
    RequestHandler.db_service = db_service
    RequestHandler.news_generator = news_generator
    RequestHandler.sentiment = sentiment
    RequestHandler.keyword_extractor = keyword_extractor
    RequestHandler.summarizer = summarizer

    # Start server
    server_address = ('', PORT)
    httpd = ThreadingHTTPServer(server_address, RequestHandler)

    logger.info('Server starting on port %d', PORT)
    logger.info('Client files: %s', CLIENT_DIR)
    logger.info('Database: %s', DB_PATH)

    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        logger.info('Server shutting down')
        httpd.shutdown()


if __name__ == '__main__':
    main()
