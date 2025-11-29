---
version: 2.0
date: 2025-11-29
title: The Human–AI Collaboration Pact
project: GuicedEE / GuicedVertxWeb (guiced-vertx-web)
authors: [GuicedEE Team, Contributors, AI Assistants]
---

# 🤝 Pact.md (v2)
### The Human–AI Collaboration Pact
*(Human × AI Assistant — “The Pact” Developer Edition)*

## 1. Purpose

This pact records how we collaborate on GuicedVertxWeb — a Java 25, Maven-based Vert.x Web bootstrapper for GuicedEE. It aligns tone, traceability, and forward-only delivery so docs, diagrams, and code stay coherent across Rules → Guides → Implementation.

## 2. Principles

- **Continuity:** Carry repo-scoped rules from `rules/RULES.md` (sections 4, 5, Document Modularity, 6) and project docs forward; do not regress to legacy anchors.
- **Finesse:** Prefer succinct, reviewable Markdown and Mermaid diagrams; keep SPI names and module labels consistent with the code (`com.guicedee.vertx.web`).
- **Non-Transactional Flow:** Treat each stage (Architecture → Guides → Implementation Plan → Code) as a closed loop with back-links to PACT, RULES, GUIDES, IMPLEMENTATION, and GLOSSARY.
- **Closing Loops:** Every artifact points to its parent and next layer (e.g., `docs/architecture/README.md` ↔ `IMPLEMENTATION.md`).

## 3. Structure of Work

| Layer | Description | Artifact |
|-------|--------------|----------|
| **Pact** | Collaboration culture, stack selection, stage gates (blanket approval noted). | `PACT.md` |
| **Rules** | Conventions mapped to selected stacks (Java 25 LTS, Maven, Vert.x 5, GuicedEE Core/Client, CRTP, JSpecify, Logging, GitHub Actions). | `RULES.md` |
| **Guides** | How-to application of rules for router/server configurators, Vert.x Web wiring, CI/env. | `GUIDES.md` |
| **Implementation** | Current modules, SPI surfaces, and code layout with links back to Guides. | `IMPLEMENTATION.md` |

## 4. Behavioral Agreements

- Language: precise, conversational technical English; avoid speculation when repo evidence is missing.
- Context: Treat existing repo docs as outdated; rely on observed code/config and the rules submodule.
- Transparency: State assumptions (e.g., configurator discovery, TLS/keystore) when code is absent.
- Iteration: Stage-gated (blanket approval granted for this run) — proceed through all stages without pauses but keep stage markers in responses.
- Attribution: Dual authorship; credit both maintainers and AI assistants.

## 5. Developer Culture: *Vibe Engineering*

- **Tool literacy:** Java 25 + Maven + Vert.x 5 + GuicedEE SPI hooks; GitHub Actions for CI; .env alignment per rules/generative/platform/secrets-config.
- **Meta-awareness:** Fluent API strategy = CRTP; avoid Lombok builders; JSpecify defaults apply.
- **Traceability:** Diagrams live under `docs/architecture/`; prompts tracked in `docs/PROMPT_REFERENCE.md`.
- **Clarity:** Use topic-first glossaries; prefer links to modular rules over duplicating prose.

## 6. Technical Commitments

- Markdown-first with Mermaid/PlantUML diagrams.
- JPMS-friendly naming consistent with `module-info.java`.
- Forward-only: replace monoliths with modular docs; update all references in the same change set.
- No project docs inside the `rules` submodule; host docs live at the root or under `docs/`.

## 7. Shared Goals

1. Document current Vert.x Web bootstrap behavior and SPI extension points.
2. Align RULES/GUIDES/IMPLEMENTATION with GuicedEE + Vert.x 5 + CRTP conventions.
3. Keep glossary precedence explicit (topic-first) and applied to naming in code/docs.
4. Maintain CI/env readiness (GitHub Actions, `.env.example`) consistent with rules references.

## 8. Closing Note

Blanket approval is recorded for this run; stages proceed without waiting for manual checkpoints. Future runs should re-confirm approval preferences. Every update should cite this PACT when evolving RULES, GUIDES, IMPLEMENTATION, and diagrams.
