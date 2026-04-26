# Recadrage du projet existant - Sous-systeme critique M14 (2 trains, 1 troncon partage)

## 1) Diagnostic du projet actuel

Le projet existant est pertinent sur le fond, mais trop large pour une verification formelle manuelle robuste.

Sources de complexite observees:
- trop de sous-systemes en parallele (densite, alertes, incidents, supervision globale)
- trop d'acteurs avec responsabilites heterogenes
- trop de messages dont certains n'ont pas d'impact direct sur les proprietes critiques
- melange entre objectif de simulation riche et objectif de preuve formelle concise

Complexite utile:
- distribution par acteurs
- concurrence sur ressource critique
- protocole asynchrone de coordination

Complexite inutile dans le coeur actuel:
- modelisation complete de station
- gestion multi-zones
- alertes et incidents complexes non necessaires a l'exclusion mutuelle

Risque principal:
- reseau de Petri trop gros pour etre analyse serieusement a la main.

## 2) Nouveau cadrage propose

Le projet est recadre, pas remplace:
- meme theme M14 (metro automatique)
- perimetre reduit a un sous-systeme critique
- conservation de l'approche Akka + Reseaux de Petri

Sous-systeme cible:
- controle d'acces concurrent de deux trains automatiques a un troncon partage
- arbitrage centralise
- occupation exclusive
- sortie et liberation

Positionnement academique assume:
- ce n'est pas une simulation complete de la M14
- c'est une abstraction raisonnable d'un mecanisme critique
- le cas 2 trains est le plus petit cas non trivial pour etudier concurrence et progression

## 3) Ce qu'il faut conserver dans l'existant

A conserver dans le coeur:
- architecture distribuee par acteurs
- logique d'echanges asynchrones
- orientation verification formelle (surete/vivacite)

A conserver comme contexte:
- ancrage M14 dans l'introduction
- justification de criticite du sous-systeme
- historique des decisions et gouvernance

A conserver comme extension future:
- densite voyageurs
- supervision station
- gestion enrichie d'incidents
- scenarios multi-zones

## 4) Ce qu'il faut sortir du coeur (et pourquoi)

A retirer du coeur immediat:
- densite voyageurs
- portes de quai et acces station
- alertes operationnelles non critiques pour l'exclusion mutuelle
- gestion globale d'incidents
- logique complete d'exploitation de ligne

Justification:
- n'apporte pas de preuve plus forte sur la propriete centrale
- augmente fortement la complexite du modele de Petri
- reduit la defendabilite academique en temps limite

Renommage recommande:
- toute notion de controle global de station vers controle de section critique

## 5) Architecture minimale cible

Acteurs minimaux recommandes:
- Train1Actor
- Train2Actor
- SectionControllerActor (ou SignalisationActor, un seul)
- TronconActor (optionnel uniquement si utile pedagogiquement)

Responsabilites minimales:
- chaque train: demander, attendre, entrer, sortir
- controleur: arbitrer, garantir l'occupation exclusive, liberer la ressource
- troncon optionnel: representer explicitement la ressource partagee

Regle de simplification:
- tout acteur sans role critique explicite doit etre fusionne ou supprime.

## 6) Flux de messages critiques a garder

Messages centraux:
- demande d'entree
- autorisation d'entree
- attente/refus temporaire
- notification de sortie
- liberation du troncon

A eliminer:
- messages decoratifs de monitoring
- messages metier secondaires sans impact sur la preuve

## 7) Scenarios critiques retenus (maximum 3)

Scenario 1 - Nominal:
- un train demande, obtient, occupe, sort, libere

Scenario 2 - Concurrence:
- les deux trains demandent presque en meme temps
- un seul entre, l'autre attend

Scenario 3 - Sortie/liberation/progression:
- le train occupant sort
- le troncon est libere
- le train en attente progresse

Ces 3 scenarios servent a la fois la simulation future et la verification formelle.

## 8) Reseau de Petri simplifie cible

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

Objectif de simplification:
- ne modeliser que ce qui est necessaire pour prouver exclusion mutuelle et progression.

## 9) Proprietes a verifier

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
- un train en attente peut avancer apres liberation
- pas de blocage injustifie dans les scenarios retenus

Exemples LTL (avec hypothese de fairness explicite):
- G !(T1_sur_troncon && T2_sur_troncon)
- G (T1_attente -> F T1_sur_troncon)
- G (T2_attente -> F T2_sur_troncon)

Limite assumee:
- les garanties d'equite forte ne sont pas prouvables sans hypotheses supplementaires.

## 10) Invariant principal

Invariant principal cible:

T1_sur_troncon + T2_sur_troncon + Troncon_libre = 1

Interpretation:
- la ressource critique est soit libre, soit occupee par T1, soit occupee par T2.

Usage en preuve:
- verification au marquage initial
- preservation sur chaque transition
- deduction de l'exclusion mutuelle

Limites:
- ne prouve pas a lui seul la vivacite
- ne garantit pas l'absence de famine sans hypothese d'arbitrage

## 11) Plan de mise a jour documentaire

README:
- reformuler l'objectif vers le sous-systeme critique
- supprimer les promesses de modelisation station complete
- clarifier la portee de comparaison Akka/Petri

Presentation/rapport:
- annoncer explicitement le choix d'abstraction
- justifier pedagogiquement le cas minimal non trivial a 2 trains

Historique et gouvernance:
- tracer explicitement les decisions de reduction de scope
- maintenir la distinction coeur versus extensions futures

## 12) Analyse critique honnete

Ce qui devient meilleur:
- perimetre faisable
- preuves plus rigoureuses
- meilleure lisibilite des flux critiques

Ce qui reste fragile:
- hypothese de fairness a expliciter clairement
- risque de re-derivation vers un scope trop large

Critiques possibles d'un correcteur exigeant:
- confusion entre abstraction et realite operationnelle
- vivacite sur-affirmee sans cadre d'hypothese
- comparaison Akka/Petri trop ambitieuse

Verdict:
- avec ce recadrage, le projet devient defendable et realisable dans un cadre academique.
