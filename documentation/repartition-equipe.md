# Repartition equipe (4 personnes) - Travail en parallele

Objectif: avancer vite sans se bloquer, tout en garantissant une integration continue entre code Akka, modele de Petri et verification sur le systeme M14 Chatelet.

## 1) Principe de decoupage

On decoupe par flux metier, pas par silos rigides. Chaque flux a:
- 1 responsable principal (owner)
- 1 binome de relecture (backup)

Rotation recommandee chaque semaine pour que tout le monde comprenne tout le projet.

## 2) Repartition proposee

### Axelobistro - StationControl et protocole de messages (Akka)
- Scope:
  - definition des messages metier (densite, incident, resolution)
  - orchestration StationControl (modes Normal/Safety)
  - gestion des snapshots d'etat
- Livrables:
  - code acteur central + protocoles
  - diagramme de sequence de bascule Safety
- Binome relecture: Ostreann

### Alicette - Controle des acces et regles de surete (Akka)
- Scope:
  - logique de fermeture/reouverture des acces par zone
  - surveillance des seuils de densite
  - prevention des etats incoherents en mode incident
- Livrables:
  - composants de controle d'acces robustes
  - scenarios de securite zone surchargee
- Binome relecture: Axelobistro

### Nikko - Modele formel (Reseau de Petri + preuves)
- Scope:
  - modelisation des etats station (normal/safety)
  - invariants de place/transition sur seuils et incidents
  - formalisation absence deadlock + traitement des alertes
- Livrables:
  - modele Petri versionne
  - notes de preuve (structurelles/LTL)
- Binome relecture: Alicette

### Ostreann - Integration, tests et qualite
- Scope:
  - architecture des tests (ScalaTest + akka-testkit)
  - tests de non-regression et de concurrence
  - CI locale (sbt compile, sbt test) et support integration
- Livrables:
  - suite de tests reproductible
  - rapport ecarts simulation Akka vs modele Petri
- Binome relecture: Nikko

## 3) Travail ensemble (important)

Pour eviter le travail en silo, vous partagez 3 zones communes:
- Contrats de messages (co-rediges par Axelobistro + Alicette + Ostreann)
- Invariants formels M14 (co-rediges par Alicette + Nikko)
- Scenarios de validation station (co-rediges par Axelobistro + Nikko + Ostreann)

Regle pratique: toute PR doit toucher au moins 1 artefact metier ET 1 artefact de validation (test, modele, ou doc de preuve).

## 4) Planning type sur 2 semaines (iteratif)

### Semaine 1
- Jour 1: cadrage du protocole M14 + checklist invariants
- Jours 2-3: implementation parallele StationControl/Access, modelisation Petri, squelette tests
- Jour 4: integration intermediaire (merge des branches vers integration)
- Jour 5: revue croisee + ajustements

### Semaine 2
- Jours 1-2: consolidation des cas limites (surcharges, incidents critiques, retour normal)
- Jour 3: campagne de verification complete (compile, tests, coherence avec Petri)
- Jour 4: freeze fonctionnel + documentation finale
- Jour 5: repetition demo + buffer correction

## 5) Workflow Git conseille (collaboratif)

- Branches de travail:
  - feature/m14-station-control (Axelobistro)
  - feature/m14-access-safety (Alicette)
  - feature/m14-petri-verification (Nikko)
  - feature/m14-tests-integration (Ostreann)
- Branche d'integration hebdomadaire:
  - integration/week-XX
- Regles:
  - PR courtes (< 300 lignes idealement)
  - relecture obligatoire par le binome
  - merge seulement si compile + tests passent

## 6) Definition of Done commune

Une tache est terminee seulement si:
- comportement code valide (Akka)
- impact sur invariants explicite
- test associe present (ou justification N/A)
- trace dans documentation/historique.md

## 7) Risques et parades

- Risque: derive entre code Akka et modele Petri
  - Parade: point de synchronisation fixe 2 fois/semaine (Axelobistro + Alicette avec Nikko)
- Risque: mode Safety mal gere en concurrence
  - Parade: tests de scenario critiques rediges par Ostreann et relus par l'equipe
- Risque: surcharge d'un membre
  - Parade: rotation owner/backup chaque semaine