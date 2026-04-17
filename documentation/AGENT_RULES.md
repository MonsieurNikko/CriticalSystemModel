# AGENT_RULES - CriticalSystemModel

## FR - Reglement officiel pour agents IA et contributeurs

### 1. Statut et portee
- Ce document est obligatoire pour tout agent IA qui intervient sur ce depot.
- Les humains l'utilisent comme reference de controle et d'audit.
- En cas de conflit entre habitudes personnelles et ce document, ce document prevaut.

### 2. Objectifs
- Garantir la qualite technique des modifications.
- Garantir la tracabilite complete des changements.
- Preserver les invariants critiques du projet:
  - pas de double-reservation
  - stock jamais negatif
  - pas de deadlock

### 3. Pre-travail obligatoire (avant toute modification)
- Lire tous les fichiers pertinents du scope de la tache pour comprendre le codebase en profondeur.
- Lire obligatoirement: README.md, documentation/historique.md, build.sbt, et les fichiers directement impactes.
- Identifier les impacts potentiels sur les invariants metier, le build et les tests.
- Reformuler l'objectif de la tache et les risques avant implementation.

### 4. Regles d'execution
- Interdiction de modifier des zones non demandees, sauf correction strictement necessaire a la coherence.
- Interdiction d'executer des actions destructives sans validation explicite de l'utilisateur.
- Interdiction de committer des artefacts generes (ex: target/) sauf demande explicite.
- Toujours travailler sur une branche dediee (jamais directement sur main).
- Commits atomiques, messages clairs (feat/fix/docs/test/refactor/chore).

### 5. Regles de push (obligatoires)
- Apres modifications et verifications, push obligatoire sur la branche de travail.
- Interdiction de push direct sur main.
- Le push doit respecter les conventions de commit et la branche nommee selon la tache.
- Le push n'est autorise qu'apres mise a jour de documentation/historique.md.
- Sequence minimale attendue:
  - git add .
  - git commit -m "type: description claire"
  - git push origin feature/nom-de-la-tache
- Si un agent IA ne peut pas pousser (permissions/outils), il doit le declarer explicitement.

### 6. Regles de verification
- Avant proposition de merge, verifier au minimum:
  - compilation: sbt compile
  - tests: sbt test (si des tests existent)
- Si une verification echoue:
  - ne pas merger
  - documenter l'echec, la cause probable, et le plan de correction

### 7. Mise a jour obligatoire de documentation/historique.md
- Chaque changement doit ajouter une nouvelle entree dans documentation/historique.md.
- Entree obligatoire a chaque intervention, IA ou humaine.
- Champs obligatoires:
  - Modele (ou N/A pour contribution humaine)
  - GitHub (@username)
  - Type (IA/Humain)
  - Date
  - Heure locale
  - Branche
  - Fichiers modifies
  - Resume detaille des changements
  - Commandes executees
  - Resultat des verifications
  - Risques/notes
- Le format doit rester chronologique inverse (plus recent en haut).

  ### 8. Transparence IA
- L'agent doit expliciter:
  - ce qu'il a fait
  - ce qu'il n'a pas fait
  - les hypotheses prises
  - les limites connues
- L'agent ne doit jamais presenter une action non executee comme executee.

  ### 9. Merge readiness checklist (obligatoire)
- Objectif respecte sans derive de perimetre.
- Invariants metier preserves.
- Build/tests verifies ou echec documente.
- documentation/historique.md mis a jour correctement.
  - Push effectue sur la branche de travail.
- Diff revue: pas de secrets, pas d'artefacts inutiles, pas de bruit.

  ### 10. Non-conformite
- Une intervention est non conforme si au moins une obligation critique n'est pas respectee.
- En cas de non-conformite:
  - bloquer le merge
  - corriger la tracabilite et/ou la verification

  ### 11. Entree en vigueur
- Version: 1.0
- Date: 2026-03-27
- Portee: depot CriticalSystemModel

---

## EN - Official policy for AI agents and contributors

### 1. Status and scope
- This document is mandatory for any AI agent acting on this repository.
- Humans use it as an audit and control reference.
- If personal habits conflict with this policy, this policy takes precedence.

### 2. Goals
- Ensure technical quality of all changes.
- Ensure full change traceability.
- Preserve critical project invariants:
  - no double booking
  - stock never negative
  - no deadlock

### 3. Mandatory pre-work (before any edit)
- Read all files relevant to the task scope to deeply understand the codebase.
- Mandatory reads: README.md, documentation/historique.md, build.sbt, and directly impacted files.
- Identify possible impacts on business invariants, build, and tests.
- Restate task objective and risks before implementation.

### 4. Execution rules
- Do not edit out-of-scope areas unless strictly required for consistency.
- Do not run destructive actions without explicit user approval.
- Do not commit generated artifacts (for example target/) unless explicitly requested.
- Always use a dedicated branch (never work directly on main).
- Use atomic commits with clear messages (feat/fix/docs/test/refactor/chore).

### 5. Push rules (mandatory)
- After changes and verification, push is mandatory on the working branch.
- Direct push to main is forbidden.
- Push must follow commit conventions and task-based branch naming.
- Push is only allowed after updating documentation/historique.md.
- Expected minimum sequence:
  - git add .
  - git commit -m "type: clear description"
  - git push origin feature/task-name
- If an AI agent cannot push (permissions/tooling), it must state that explicitly.

### 6. Verification rules
- Before proposing a merge, verify at minimum:
  - compile: sbt compile
  - tests: sbt test (if tests exist)
- If verification fails:
  - do not merge
  - document failure, likely root cause, and fix plan

### 7. Mandatory documentation/historique.md update
- Every change must add a new entry in documentation/historique.md.
- Mandatory for every intervention, AI or human.
- Required fields:
  - Model (or N/A for human contribution)
  - GitHub (@username)
  - Type (AI/Human)
  - Date
  - Local time
  - Branch
  - Modified files
  - Detailed change summary
  - Commands executed
  - Verification results
  - Risks/notes
- Keep reverse chronological order (most recent first).

  ### 8. AI transparency
- The agent must explicitly state:
  - what was done
  - what was not done
  - assumptions made
  - known limitations
- The agent must never claim an unexecuted action as executed.

  ### 9. Merge readiness checklist (mandatory)
- Objective met without scope drift.
- Business invariants preserved.
- Build/tests verified or failure documented.
- documentation/historique.md correctly updated.
  - Push completed on the working branch.
- Diff reviewed: no secrets, no unnecessary artifacts, no noise.

  ### 10. Non-compliance
- An intervention is non-compliant if any critical obligation is missing.
- In case of non-compliance:
  - block merge
  - fix traceability and/or verification

  ### 11. Effective date
- Version: 1.0
- Date: 2026-03-27
- Scope: CriticalSystemModel repository
