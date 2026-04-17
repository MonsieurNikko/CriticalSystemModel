# Guide Complet - CriticalSystemModel (Projet 2026)
> Recadrage officiel du projet existant vers un sous-systeme critique M14

---

## Resume du projet

Ce guide met a jour le cadrage du projet existant.

Le sujet reste celui d'une application distribuee critique avec Akka/Scala et reseaux de Petri. En revanche, le coeur du projet est volontairement reduit pour rester defendable academiquement:
- 2 trains concurrents
- 1 troncon critique partage
- 1 mecanisme d'arbitrage d'acces

Positionnement assume:
- ce n'est pas une modelisation complete de la ligne M14
- c'est une abstraction d'un sous-systeme critique inspire de la M14
- le cas a 2 trains est le plus petit cas non trivial pour etudier concurrence, exclusion mutuelle, attente et progression

---

## Objectifs academiques (inchanges sur le fond)

| # | Objectif | Cadrage applique |
|---|----------|------------------|
| 1 | Etat de l'art | Verification formelle et systemes critiques ferroviaires |
| 2 | Modelisation | Architecture d'acteurs minimale centree sur le troncon partage |
| 3 | Traduction formelle | Reseau de Petri compact, analysable a la main |
| 4 | Verification | Exclusion mutuelle, absence de collision, progression apres liberation |
| 5 | Simulation & comparaison | Correspondance scenarios Akka <-> transitions Petri |

---

## Perimetre du coeur de projet

Inclus dans le coeur:
- protocole d'acces au troncon (demande, autorisation/attente, occupation, sortie, liberation)
- interactions distribuees minimales entre trains et controleur
- preuves formelles ciblees sur surete et vivacite du mecanisme retenu

Hors coeur (extensions futures seulement):
- supervision complete de station
- densite voyageurs
- gestion d'alertes complexes
- gestion globale multi-incidents
- logique d'exploitation complete de ligne

---

## Architecture cible minimale (niveau conceptuel)

Acteurs cibles:
- Train1Actor
- Train2Actor
- SectionControllerActor (ou SignalisationActor, un seul composant d'arbitrage)
- TronconActor optionnel (a garder seulement si utile pedagogiquement)

Responsabilites:
- Trains: demander, attendre, entrer, sortir
- Controleur: arbitrer les demandes et garantir l'occupation exclusive
- Troncon optionnel: expliciter la ressource partagee

Regle de simplification:
- tout acteur sans contribution directe a l'exclusion mutuelle est supprime ou absorbe

---

## Flux de messages critiques a conserver

Messages necessaires:
- demande d'entree
- autorisation d'entree
- attente ou refus temporaire
- notification de sortie
- liberation du troncon

Messages a eliminer du coeur:
- messages decoratifs
- messages de monitoring non utilises dans les preuves
- messages lies a des sous-systemes hors perimetre

---

## Scenarios critiques retenus

Scenario 1 - Nominal:
- un train obtient l'acces, occupe, sort, libere

Scenario 2 - Concurrence:
- les deux trains demandent presque en meme temps
- un seul accede, l'autre attend

Scenario 3 - Sortie / liberation / progression:
- le train occupant libere le troncon
- le train en attente progresse ensuite

Ces trois scenarios servent a la fois a la simulation et a la verification formelle.

---

## Reseau de Petri cible (abstraction minimale)

Places recommandees:
- Troncon_libre
- T1_hors
- T1_attente
- T1_sur_troncon
- T2_hors
- T2_attente
- T2_sur_troncon

Transitions recommandees:
- T1_demande
- T2_demande
- T1_entree_autorisee
- T2_entree_autorisee
- T1_sortie_liberation
- T2_sortie_liberation

Marquage initial recommande:
- Troncon_libre = 1
- T1_hors = 1
- T2_hors = 1

Principe:
- ne modeliser que les etats/transitions utiles a la preuve des proprietes critiques

---

## Proprietes a verifier

Proprietes structurelles:
- conservation de la ressource troncon
- bornitude des places d'occupation

Invariants metier:
- occupation exclusive du troncon
- coherence des etats de chaque train

Surete:
- exclusion mutuelle sur le troncon
- absence de collision

Vivacite:
- progression eventuelle d'un train en attente apres liberation
- absence de blocage injustifie dans les scenarios retenus

Exemples LTL (avec hypothese de fairness explicite):
- G !(T1_sur_troncon && T2_sur_troncon)
- G (T1_attente -> F T1_sur_troncon)
- G (T2_attente -> F T2_sur_troncon)

Limite assumee:
- l'equite forte ne peut pas etre affirmee sans hypotheses supplementaires

---

## Invariant principal

Invariant central:

T1_sur_troncon + T2_sur_troncon + Troncon_libre = 1

Usage:
- preuve de preservation transition par transition
- deduction de l'exclusion mutuelle

Limite:
- cet invariant ne suffit pas a prouver la vivacite

---

## Methode de comparaison Akka / Petri (future)

La comparaison attendue doit rester concrete et prudente:
- associer les messages cles de la simulation a des transitions Petri
- comparer les 3 scenarios critiques retenus
- eviter de revendiquer une equivalence totale non defendable

---

## Plan de travail recommande (phase suivante)

Semaine 1:
- figer le perimetre de coeur et la checklist anti-derive
- valider le protocole minimal de messages
- stabiliser la structure du reseau de Petri

Semaine 2:
- consolider les preuves de surete
- formaliser la vivacite sous hypotheses explicites
- preparer la matrice de correspondance scenario/message/transition

---

## Livrables attendus

- bibliographie commentee
- simulation Akka/Scala du sous-systeme recadre
- reseau de Petri compact et justifie
- rapport de verification (structurelles, invariants, surete, vivacite)
- comparaison scenarios simulation vs abstraction formelle
- depot GitHub propre, trace et coherent

---

## Bibliographie de depart

- Murata, T. (1989), Petri nets: Properties, analysis and applications, Proceedings of the IEEE.
- Documentation officielle Akka: https://akka.io/docs/
- Tour of Scala: https://docs.scala-lang.org/tour/tour-of-scala.html
- Introduction LTL (TU Munich): https://www7.in.tum.de/um/courses/verification/SS05/LTL.pdf
- Clarke, Grumberg, Peled (1999), Model Checking, MIT Press.

---

Projet realise dans le cadre du cours - deadline fin mai 2026.
