# Repartition equipe (4 personnes) - Travail en parallele

Objectif: avancer vite sans se bloquer, tout en garantissant une integration continue entre code Akka, modele de Petri et verification.

## 1) Principe de decoupage

On decoupe par flux metier, pas par silos rigides. Chaque flux a:
- 1 responsable principal (owner)
- 1 binome de relecture (backup)

Rotation recommandee chaque semaine pour que tout le monde comprenne tout le projet.

## 2) Repartition proposee

### Axelobistro - Flux Reservation (Akka)
- Scope:
  - definition des messages de reservation
  - acteur billetterie (orchestration)
  - scenarios reussite/annulation
- Livrables:
  - code acteur + protocoles
  - diagramme sequence reservation
- Binome relecture: Ostreann

### Alicette - Flux Stock & Cohesion des invariants (Akka)
- Scope:
  - acteur stock
  - prevention du stock negatif
  - gestion concurrence sur decrementation/incrementation
- Livrables:
  - logique de stock atomique
  - tests ciblant stock jamais negatif
- Binome relecture: Axelobistro

### Nikko - Modele formel (Reseau de Petri + preuves)
- Scope:
  - modelisation places/transitions du flux complet
  - invariants de place/transition
  - formalisation absence deadlock/double-reservation
- Livrables:
  - modele Petri versionne
  - notes de preuve (structurelles/LTL si prevu)
- Binome relecture: Alicette

### Ostreann - Integration, tests et qualite
- Scope:
  - architecture des tests (ScalaTest + akka-testkit)
  - tests de non-regression
  - CI locale (sbt compile, sbt test) et support integration
- Livrables:
  - suite de tests reproductible
  - rapport ecarts simulation Akka vs modele Petri
- Binome relecture: Nikko

## 3) Travail ensemble (important)

Pour eviter le travail en silo, vous partagez 3 zones communes:
- Contrats de messages (co-rediges par Axelobistro + Alicette + Ostreann)
- Invariants formels (co-rediges par Alicette + Nikko)
- Scenarios de validation (co-rediges par Axelobistro + Nikko + Ostreann)

Regle pratique: toute PR doit toucher au moins 1 artefact metier ET 1 artefact de validation (test, modele, ou doc de preuve).

## 4) Planning type sur 2 semaines (iteratif)

### Semaine 1
- Jour 1: cadrage des contrats/messages + checklist invariants
- Jours 2-3: implementation parallele Axelobistro/Alicette, modelisation Nikko, squelette tests Ostreann
- Jour 4: integration intermediaire (merge de toutes les branches vers une branche d'integration)
- Jour 5: revue croisee + ajustements

### Semaine 2
- Jours 1-2: consolidation des cas limites (erreurs paiement, annulations, concurrence)
- Jour 3: campagne de verification complete (compile, tests, coherence avec Petri)
- Jour 4: freeze fonctionnel + documentation finale
- Jour 5: repetition demo + buffer correction

## 5) Workflow Git conseille (collaboratif)

- Branches de travail:
  - feature/reservation-akka (Axelobistro)
  - feature/stock-invariants (Alicette)
  - feature/petri-verification (Nikko)
  - feature/tests-integration (Ostreann)
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
- Risque: blocage integration tardive
  - Parade: integration/week-XX des le milieu de semaine
- Risque: surcharge d'un membre
  - Parade: rotation owner/backup chaque semaine