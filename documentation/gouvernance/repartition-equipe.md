# Repartition equipe (4 personnes) - Version recadree

> Note : ce fichier a servi au decoupage initial de l'equipe. Le projet a d'abord vise un socle **2 trains / 1 troncon partage**, puis ce socle a ete upgrade le 29/04 vers le modele final **canton + quai + portes palieres**.

Objectif final: livrer un noyau academique defendable sur un sous-systeme critique M14 simplifie (2 trains + canton + quai + PSD), sans derive vers une simulation complete de station.

## 1) Principe de decoupage

On decoupe par mecanisme critique, pas par sous-systemes larges. Chaque flux a:
- 1 responsable principal (owner)
- 1 binome de relecture (backup)

Rotation recommandee chaque semaine pour que tout le monde comprenne tout le projet.

### Pole A - Implémentation & Code Akka (Terminé)
- Scope:
  - Architecture et implémentation Akka (`SectionController`, `QuaiController`, `GestionnairePortes`, `Train`, `Protocol`)
  - Développement de l'analyseur Petri en Scala (`PetriNet`, `Analyseur`)
  - Rédaction des tests unitaires et d'intégration (Akka TestKit)
- Livrables:
  - Code fonctionnel et testé (49/49 verts)
  - Preuves programmatiques d'absence de deadlock, d'exclusion mutuelle et de surete PSD
- Statut: Fait

### Pole B - Preuves d'Invariants & Modèle Formel (Charge: Moyenne)
- Scope:
  - Preuves mathématiques/manuelles de l'exclusion mutuelle
  - Clarification des hypothèses d'arbitrage (fairness minimale)
  - Formalisation LTL (Safety / Liveness)
- Livrables:
  - Carnet de preuves manuelles (`preuves-manuelles.md`)
  - Validation formelle des 3 scénarios critiques

### Pole C - Comparaison & Tests (Charge: Faible)
- Scope:
  - Alignement du vocabulaire Akka/Petri (`lexique.md`)
  - Exécution de l'analyseur et intégration des résultats
- Livrables:
  - Mapping explicite message -> transition
  - Tableau de comparaison Akka vs Petri (`comparaison.md`)

### Pole D - Rédaction du Rapport Final (Charge: Forte)
- Scope:
  - Synthèse globale et bibliographie
  - Assemblage de tous les travaux des pôles A, B et C
- Livrables:
  - Rapport final (`rapport.md`) complet et structuré

## 3) Travail ensemble (important)

Pour eviter le travail en silo, vous partagez 3 zones communes:
- Contrats de messages critiques (validation croisée de l'équipe)
- Invariant principal et hypotheses de vivacite (validation croisée de l'équipe)
- Scenarios de validation du canton/quai/PSD (validation croisée de l'équipe)

Regle pratique: toute PR doit toucher au moins 1 artefact metier ET 1 artefact de validation (test, modele, ou doc de preuve).

## 4) Planning type sur 2 semaines (iteratif)

### Semaine 1
- Jour 1: freeze du perimetre initial (2 trains, 1 troncon, 1 arbitre)
- Jours 2-3: mise au propre du protocole critique + premier reseau Petri compact
- Jour 4: integration intermediaire et revue des hypotheses
- Jour 5: validation de l'invariant principal et des 3 scenarios

### Semaine 2
- Jours 1-2: consolidation concurrence/attente/progression
- Jour 3: verification complete (compile, tests, coherence avec Petri)
- Jour 4: freeze fonctionnel + upgrade PSD documente
- Jour 5: repetition demo + buffer correction

## 5) Workflow Git conseille (collaboratif)

- Branches de travail:
  - `feature/m14-preuves-manuelles` (Pour les tâches du Pôle B)
  - `feature/m14-rapport-final` (Pour les tâches des Pôles C et D)
- Branche principale:
  - `main`
- Regles:
  - PR courtes (< 300 lignes idealement)
  - relecture obligatoire par le binome
  - merge seulement si compile + tests passent

## 6) Definition of Done commune

Une tache est terminee seulement si:
- comportement code valide (Akka)
- impact sur invariants explicite
- test associe present (ou justification N/A)
- trace dans `documentation/suivi/historique.md`

## 7) Risques et parades

- Risque: retour a un perimetre trop large (station complete)
  - Parade: checklist de scope obligatoire a chaque PR
- Risque: formule de vivacite trop ambitieuse par rapport au modele
  - Parade: expliciter les hypotheses de fairness et limiter les proprietes a celles defendables
- Risque: derive entre simulation Akka et abstraction Petri
  - Parade: mapping explicite message -> transition sur les 3 scenarios retenus
