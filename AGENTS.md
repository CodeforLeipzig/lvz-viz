# Agents

## Navigation

- Backend: Spring Boot in `src/main/java/de/codefor/le/` (`crawler`, `ner`, `web`, `model`); Elasticsearch mapping in `src/main/resources/mapping.json`.
- Frontend: Angular in `frontend/src/app/features/`; run `pnpm test` and `pnpm lint` in `frontend/`.
- Backend tests: the `@SpringBootTest` classes need Elasticsearch on `localhost:9200` (`docker compose up -d elasticsearch`); all other tests run offline.

## Agent skills

### Issue tracker

Issues are tracked in GitHub Issues on the upstream repo `CodeforLeipzig/lvz-viz`.
See `docs/agents/issue-tracker.md`.

### Triage labels

The default five-label vocabulary (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`).
See `docs/agents/triage-labels.md`.
