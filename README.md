# NewsVisualizer

**🌐 Live: [newsvisualizer.vercel.app](https://newsvisualizer.vercel.app)**

A news intelligence platform that fetches, analyzes, and visualizes news data — narrative clusters, breaking signals, sentiment, source balance, and AI summaries, rendered as one instrument.

The project ships in two forms:

| | Stack | Where |
|---|---|---|
| **Web platform** (primary) | Next.js 16 · React Three Fiber · Tailwind · Python API | [`web/`](web/) — deployed on Vercel |
| **Desktop app** (legacy) | Java 11 · Swing · JFreeChart · Maven | [`src/`](src/) |

---

## Web Platform

A futuristic dark-mode web app: a cinematic landing page with an interactive 3D signal globe, and a 14-module newsroom console.

### Modules

Dashboard · News Fetch · Analytics · Story Radar · Source Monitor · Breaking Watch · Duplicate Stories · Source Balance · Story Timeline · AI Summary · Translation · NewsApp Reader · Search History · Settings

### Architecture

```
web/
├── client/        # Next.js 16 frontend (App Router, Tailwind v4,
│                  #   Framer Motion, React Three Fiber, React Query, Zustand)
├── server/        # Python stdlib API server (SQLite, no dependencies)
├── api-service/   # Vercel wrapper that deploys server.py as a Python function
└── start.sh       # Local dev: starts backend (8081) + frontend (3000)
```

- In **development**, the frontend proxies `/api/*` to the local Python server on port 8081.
- In **production**, the frontend ([newsvisualizer.vercel.app](https://newsvisualizer.vercel.app)) rewrites `/api/*` to the deployed Python API ([newsvisualizer-api.vercel.app](https://newsvisualizer-api.vercel.app)).
- The database lives in `/tmp` (SQLite). On the serverless deployment it is ephemeral — click **Fetch News** to repopulate the feed.

### Run locally

Requirements: Node.js 18+, Python 3.

```bash
cd web
./start.sh
# Frontend: http://localhost:3000
# API:      http://localhost:8081
```

Or run the pieces separately:

```bash
cd web/server && PORT=8081 python3 server.py   # backend
cd web/client && npm install && npm run dev    # frontend
```

### Deploy

Both projects deploy to Vercel with the CLI:

```bash
cd web/client      && vercel deploy --prod   # frontend
cd web/api-service && vercel deploy --prod   # Python API
```

---

## Desktop App (Java)

The original Swing desktop application with interactive charts and sentiment analysis.

### Features

- News fetching from external APIs (NewsAPI.org)
- Sentiment analysis of article tone
- Interactive charts: sentiment distribution, source distribution, keyword frequency, publication timeline
- Search and filter by keywords, country, or category

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Setup & Run

```bash
# Quick setup (macOS/Linux)
./setup.sh
# Windows
setup.bat

# Run
./run.sh                # macOS/Linux
run.bat                 # Windows
python3 launch.py       # Universal

# Or via Maven
mvn clean compile
mvn exec:java -Dexec.mainClass="com.newsvisualizer.NewsVisualizerApp"
```

**API key (optional):** register at [newsapi.org](https://newsapi.org), then set your key in `src/main/java/com/newsvisualizer/service/NewsApiService.java`.

### Key dependencies

Apache HttpClient · Jackson · JFreeChart · Apache Commons Lang · SLF4J + Logback · JUnit

### Troubleshooting

- **No articles found** — check internet connection, API key, and quota limits
- **Build issues** — ensure Java 11+ and Maven are installed; try `mvn clean`
- **Port 8080 conflict** — the desktop app's server and the web backend both like this port; the web backend defaults to 8081 (override with `PORT=...`)

---

## License

This project is created for educational purposes.

## Contributing

Feel free to submit issues and enhancement requests!
