# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Mandatory Reading

Before making any changes, read these documents:
- `documentation/START-ICI.md` — start here (project overview, team roles, what to do)
- `documentation/gouvernance/REGLES_PROJET.md` — coding rules, Git workflow, commit conventions, and AI agent constraints
- `documentation/suivi/PLAN.md` — sprint phases and deliverables (deadline May 4, 2026)
- `documentation/suivi/historique.md` — mandatory change log (update it after every significant change)

## Build & Test Commands

```bash
sbt compile        # Compile the project
sbt test           # Run all tests (required before any push)
sbt run            # Run Main (StationControl demo)
sbt testOnly m14.* # Run tests matching a package pattern
sbt testQuick      # Run only tests affected by recent changes
```

No linter or formatter is configured. The project uses manual code review based on the vibe-code style rules in `REGLES_PROJET.md`.

## Project Purpose

Formal verification and distributed simulation of the critical shared-section (tronçon critique) on Paris Metro Line M14, focusing on **mutual exclusion** between two autonomous trains.

**Core invariant** (must never be violated):
```
T1_sur_troncon + T2_sur_troncon + Troncon_libre = 1
```
Exactly one of the three states is active at all times — two trains on the section simultaneously is a safety failure.

## Architecture

```
src/main/scala/
  m14/
    Main.scala                  # Entry point (runs StationControl demo for now)
    StationControl.scala        # Station-wide safety actor (occupancy, gate closures)
    troncon/
      Protocol.scala            # All message types (Demande, Autorisation, Attente, Sortie)
      SectionController.scala   # Centralized arbiter for section access (Phase 2)
      Train.scala               # Train actor state machine: hors→attente→sur_troncon→hors (Phase 3)
    petri/
      PetriNet.scala            # Petri net data structures (Phase 5 — not yet created)
      Analyseur.scala           # BFS state-space generator and invariant verifier (Phase 6 — not yet created)
src/test/scala/
  m14/
    StationControlSpec.scala    # Akka TestKit tests for StationControl
    troncon/                    # Tests for SectionController + Train (Phase 2-3)
```

**Akka Typed** (`akka-actor-typed 2.8.5`) is the concurrency model. All actors use `Behaviors.receive` with explicit state threading — no mutable state.

**Message flow (target design):**
```
[Train1Actor] --Demande--> [SectionController] <--Demande-- [Train2Actor]
     ^                           |
     +--- Autorisation/Attente --+
     +--- release on Sortie -----+
```

`SectionController` maintains a FIFO queue. When the section is free it grants `Autorisation`; when occupied it replies `Attente` and queues the train. On `Sortie` it dequeues and notifies the next waiter.

## Development Phases

| Phase | Status | Deliverable |
|-------|--------|-------------|
| 1 — Skeletons | Done | Protocol, actor stubs, passing empty tests |
| 2 — SectionController | Pending | Full state machine with FIFO queue |
| 3 — Train actor | Pending | State machine: hors→attente→sur_troncon→hors |
| 4 — Integration tests | Pending | 3 critical scenarios (nominal, concurrency, release) |
| 5-6 — Petri + Analyzer | Pending | Formal model + BFS verifier |
| 7-9 — LTL, report, polish | Pending | Proofs, final deliverable |

## Code Style (Vibe-code)

From `REGLES_PROJET.md`:
- **No advanced Scala idioms** — no implicits, macros, type classes, tagless final
- Functions ≤ 30 lines, nesting ≤ 3 levels
- Long, descriptive variable names; short functions
- One-line role comment at the top of each file: `// FileName : role in one sentence.`
- Comments explain *why*, not *what*

## Git Workflow

- Work on `feature/<name>` branches, never commit directly to `main`
- Commit format: `type(scope): message` (e.g., `feat(troncon): implement SectionController FIFO queue`)
- Run `sbt compile && sbt test` before any push
- Update `documentation/suivi/historique.md` with every significant change (reverse-chronological, use the template in that file)
- Merge to `main` via PR only after verification passes

## Three Critical Test Scenarios

Any implementation of `SectionController` + `Train` must pass:
1. **Nominal** — single train requests, enters, exits; section returns to libre
2. **Concurrency** — two trains request simultaneously; exactly one gets `Autorisation`, the other gets `Attente`; after exit, waiting train advances
3. **Release/Progress** — occupying train sends `Sortie`; queued train receives `Autorisation` without deadlock
