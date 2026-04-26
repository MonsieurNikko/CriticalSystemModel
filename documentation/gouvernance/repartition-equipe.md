# Repartition equipe (4 personnes) - Version recadree

Objectif: livrer un noyau academique defendable sur un sous-systeme critique M14 simplifie (2 trains + 1 troncon partage), sans derive vers une simulation complete de station.

## 1) Principe de decoupage

On decoupe par mecanisme critique, pas par sous-systemes larges. Chaque flux a:
- 1 responsable principal (owner)
- 1 binome de relecture (backup)

Rotation recommandee chaque semaine pour que tout le monde comprenne tout le projet.

## 2) Repartition proposee

### Axelobistro - Protocole de messages critiques (Akka)
- Scope:
  - definition stricte des messages indispensables (demande, autorisation, attente, sortie, liberation)
  - alignement du vocabulaire Akka/Petri
  - reduction des messages decoratifs
- Livrables:
  - protocole documente et borne
  - diagramme de sequence du cycle d'acces au troncon
- Binome relecture: Ostreann

### Alicette - Arbitrage de section critique et surete
- Scope:
  - logique d'autorisation/refus temporaire d'entree
  - coherence de l'occupation exclusive du troncon
  - clarification des hypotheses d'arbitrage (fairness minimale)
- Livrables:
  - specification d'arbitrage claire
  - scenarios de non-collision et d'attente
- Binome relecture: Axelobistro

### Nikko - Modele formel (Reseau de Petri minimal + preuves)
- Scope:
  - modelisation des places/transitions strictement necessaires au mecanisme d'acces
  - preuve de l'invariant principal de ressource
  - formulation des proprietes de surete/vivacite realistes
- Livrables:
  - reseau Petri compact analysable a la main
  - notes de preuve structurelles et LTL ciblees
- Binome relecture: Alicette

### Ostreann - Integration, tests et qualite
- Scope:
  - alignement scenario simulation <-> scenario formel
  - campagne de tests concentree sur 3 scenarios critiques
  - CI locale (sbt compile, sbt test) et support integration
- Livrables:
  - rapport de comparaison Akka/Petri limite au perimetre retenu
  - suite de tests reproductible sur le protocole critique
- Binome relecture: Nikko

## 3) Travail ensemble (important)

Pour eviter le travail en silo, vous partagez 3 zones communes:
- Contrats de messages critiques (co-rediges par Axelobistro + Alicette + Ostreann)
- Invariant principal et hypotheses de vivacite (co-rediges par Alicette + Nikko)
- Scenarios de validation du troncon partage (co-rediges par Axelobistro + Nikko + Ostreann)

Regle pratique: toute PR doit toucher au moins 1 artefact metier ET 1 artefact de validation (test, modele, ou doc de preuve).

## 4) Planning type sur 2 semaines (iteratif)

### Semaine 1
- Jour 1: freeze du perimetre (2 trains, 1 troncon, 1 arbitre)
- Jours 2-3: mise au propre du protocole critique + premier reseau Petri compact
- Jour 4: integration intermediaire et revue des hypotheses
- Jour 5: validation de l'invariant principal et des 3 scenarios

### Semaine 2
- Jours 1-2: consolidation concurrence/attente/progression
- Jour 3: verification complete (compile, tests, coherence avec Petri)
- Jour 4: freeze fonctionnel + documentation finale
- Jour 5: repetition demo + buffer correction

## 5) Workflow Git conseille (collaboratif)

- Branches de travail:
  - feature/m14-critical-messaging (Axelobistro)
  - feature/m14-section-arbitration (Alicette)
  - feature/m14-petri-troncon (Nikko)
  - feature/m14-validation-scenarios (Ostreann)
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
- trace dans `documentation/suivi/historique.md`

## 7) Risques et parades

- Risque: retour a un perimetre trop large (station complete)
  - Parade: checklist de scope obligatoire a chaque PR
- Risque: formule de vivacite trop ambitieuse par rapport au modele
  - Parade: expliciter les hypotheses de fairness et limiter les proprietes a celles defendables
- Risque: derive entre simulation Akka et abstraction Petri
  - Parade: mapping explicite message -> transition sur les 3 scenarios retenus