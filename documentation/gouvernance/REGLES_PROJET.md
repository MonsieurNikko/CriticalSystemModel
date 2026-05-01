# REGLES_PROJET - CriticalSystemModel

## FR - Reglement officiel pour les contributeurs

### 1. Statut et portee
- Ce document fixe les regles de qualite et de tracabilite pour tout contributeur sur ce depot.
- En cas de conflit entre habitudes personnelles et ce document, ce document prevaut.

### 2. Objectifs
- Garantir la qualite technique des modifications.
- Garantir la tracabilite complete des changements.
- Preserver les invariants critiques du sous-systeme M14 (canton + quai + portes palieres):
  - exclusion mutuelle canton: T1_sur_canton + T2_sur_canton + Canton_libre = 1
  - exclusion mutuelle quai:   T1_a_quai + T2_a_quai + Quai_libre = 1
  - coherence portes:          Portes_fermees + Portes_ouvertes = 1
  - PSD-Open Safety:           Portes_ouvertes=1 => un train est a quai (CRITIQUE)
  - PSD-Departure Safety:      Ti_depart_quai tirable => Portes_fermees=1 (CRITIQUE)
  - absence de deadlock dans le protocole d'acces

### 3. Pre-travail obligatoire (avant toute modification)
- Lire tous les fichiers pertinents du scope de la tache pour comprendre le codebase en profondeur.
- Lire obligatoirement: README.md, documentation/suivi/historique.md, build.sbt, et les fichiers directement impactes.
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
- Le push n'est autorise qu'apres mise a jour de documentation/suivi/historique.md.
- Sequence minimale attendue:
  - git add .
  - git commit -m "type: description claire"
  - git push origin feature/nom-de-la-tache

### 6. Regles de verification
- Avant proposition de merge, verifier au minimum:
  - compilation: sbt compile
  - tests: sbt test (si des tests existent)
- Si une verification echoue:
  - ne pas merger
  - documenter l'echec, la cause probable, et le plan de correction

### 7. Mise a jour obligatoire de documentation/suivi/historique.md
- Chaque changement doit ajouter une nouvelle entree dans documentation/suivi/historique.md.
- Entree obligatoire a chaque intervention.
- Champs obligatoires:
  - GitHub (@username)
  - Date
  - Heure locale
  - Branche
  - Fichiers modifies
  - Resume detaille des changements
  - Commandes executees
  - Resultat des verifications
  - Risques/notes
- Le format doit rester chronologique inverse (plus recent en haut).



  ### 9. Merge readiness checklist (obligatoire)
- Objectif respecte sans derive de perimetre.
- Invariants metier preserves.
- Build/tests verifies ou echec documente.
- documentation/suivi/historique.md mis a jour correctement.
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

  ### 11. Regles de code (style etudiant)
- Le code produit doit etre simple, naif, lisible.
- Interdictions cote style:
  - pas d'implicits, pas de macros, pas de point-free style, pas d'operateurs custom
  - pas de chaines de flatMap/for-yield complexes; preferer if/else et boucles simples
  - pas de hierarchies de types avancees (tagless final, type classes maison, etc.)
  - pas de "patterns Scala idiomatiques" si une version naive marche
- Obligations cote style:
  - noms de variables longs et explicites (en francais ou anglais, mais coherents dans un fichier)
  - pas d'en-tete obligatoire au sommet de chaque fichier source ; les noms de classes/packages doivent deja parler
  - fonctions courtes (<30 lignes), nesting <=3
  - duplication legere (2-3 lignes similaires) toleree si elle ameliore la lisibilite
  - commentaires rares mais utiles, en francais simple, plutot pour expliquer POURQUOI
- Obligations cote tracabilite:
  - le message de commit doit suivre la structure: 1) Quoi, 2) Pourquoi, 3) Comment relire (quel fichier ouvrir en premier)
- Test de defendabilite: si un equipier ouvre un fichier, il doit pouvoir l'expliquer a l'oral en 2 minutes sans aide. Si non, le code est trop complexe et doit etre simplifie.

---

## EN - Official policy for contributors

### 1. Status and scope
- This document sets the quality and traceability rules for any contributor on this repository.
- If personal habits conflict with this policy, this policy takes precedence.

### 2. Goals
- Ensure technical quality of all changes.
- Ensure full change traceability.
- Preserve critical M14 subsystem invariants (final canton + platform + PSD model):
  - canton mutual exclusion: T1_on_canton + T2_on_canton + canton_free = 1
  - platform mutual exclusion: T1_at_platform + T2_at_platform + platform_free = 1
  - door coherence: doors_closed + doors_open = 1
  - PSD-Open Safety: open doors imply one train is at platform
  - PSD-Departure Safety: a train can leave the platform only with closed doors
  - no deadlock in the access protocol

### 3. Mandatory pre-work (before any edit)
- Read all files relevant to the task scope to deeply understand the codebase.
- Mandatory reads: README.md, documentation/suivi/historique.md, build.sbt, and directly impacted files.
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
- Push is only allowed after updating documentation/suivi/historique.md.
- Expected minimum sequence:
  - git add .
  - git commit -m "type: clear description"
  - git push origin feature/task-name

### 6. Verification rules
- Before proposing a merge, verify at minimum:
  - compile: sbt compile
  - tests: sbt test (if tests exist)
- If verification fails:
  - do not merge
  - document failure, likely root cause, and fix plan

### 7. Mandatory documentation/suivi/historique.md update
- Every change must add a new entry in documentation/suivi/historique.md.
- Mandatory for every intervention.
- Required fields:
  - GitHub (@username)
  - Date
  - Local time
  - Branch
  - Modified files
  - Detailed change summary
  - Commands executed
  - Verification results
  - Risks/notes
- Keep reverse chronological order (most recent first).



  ### 9. Merge readiness checklist (mandatory)
- Objective met without scope drift.
- Business invariants preserved.
- Build/tests verified or failure documented.
- documentation/suivi/historique.md correctly updated.
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

  ### 11. Code rules (student style)
- Code produced must be simple, naive, readable.
- Style prohibitions:
  - no implicits, no macros, no point-free style, no custom operators
  - no complex flatMap / for-yield chains; prefer if/else and plain loops
  - no advanced type hierarchies (tagless final, custom type classes, etc.)
  - no "idiomatic Scala patterns" if a naive version works
- Style obligations:
  - long, descriptive variable names (in French or English, but consistent within a file)
  - no mandatory one-line header at the top of every source file; class/package names should already be clear
  - short functions (<30 lines), nesting <=3
  - mild duplication (2-3 similar lines) acceptable when it improves readability
  - sparse but useful comments in plain French, mostly explaining WHY
- Traceability obligations:
  - the commit message must follow the structure: 1) What, 2) Why, 3) How to re-read (which file to open first)
- Defensibility test: if a teammate opens a file, they must be able to explain it orally in 2 minutes without help. If not, the code is too complex and must be simplified.

  ### 13. Effective date (EN section)
- Version: 1.1
- Date: 2026-04-25
- Scope: CriticalSystemModel repository
