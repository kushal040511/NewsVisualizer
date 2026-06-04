# Structure Roadmap

This project should be restructured in small, behavior-preserving phases. The
current Java source should keep compiling and running after each phase.

## Current State

- Maven is already in place, and the main application entry point is
  `com.newsvisualizer.NewsVisualizerApp`.
- Source code is grouped by broad technical type: `gui`, `service`, `model`,
  `utils`, `visualization`, and `adapter`.
- Several generated or local runtime files are currently tracked, including
  compiled Maven output, local H2 database files, and generated HTML reports.
- Some classes have grown very large and mix UI construction, event handling,
  service calls, state updates, and rendering logic.
- There are multiple launcher-style classes, including older/alternate app
  entry points.

## Phase 1: Repository Hygiene

Goal: clean project boundaries without changing application behavior.

- Keep build output out of version control.
- Keep local H2 database files out of version control.
- Keep generated HTML reports and scratch files out of version control.
- Keep source, tests, configuration, scripts, and intentional documentation
  visible at the top level.

## Phase 2: Non-Behavioral Source Organization

Goal: clarify intent without changing runtime behavior.

- Move manual launcher/test classes out of `src/main/java` when they are not
  production entry points.
- Pick one production entry point and mark older launchers as legacy before
  removing them.
- Keep Swing UI classes under `gui`, but split oversized windows into panels,
  actions, table models, and renderers.
- Keep package moves mechanical and verify with Maven after every move.

## Phase 3: Configuration Cleanup

Goal: remove environment-specific values from Java source.

- Move API keys and local settings to environment variables or a config file.
- Keep `.env.example` as the documented template.
- Avoid committing secrets, local database state, or user-generated runtime
  data.

## Phase 4: Architecture Cleanup

Goal: reduce coupling between UI, services, persistence, and analysis.

- Add small service interfaces where the UI needs data but should not know the
  implementation details.
- Separate API/RSS fetching, fallback/mock data, and response mapping.
- Move database schema initialization and repository-style operations into a
  persistence-focused package.
- Increase unit tests around analyzer, summarizer, authentication, and service
  behavior before deeper refactors.

## Suggested Future Package Shape

```text
com.newsvisualizer
  app
  config
  domain
    model
    service
  infrastructure
    database
    newsapi
    rss
  ui
    windows
    panels
    components
    theme
  analytics
  visualization
```

This is a target direction, not a one-step migration plan.
