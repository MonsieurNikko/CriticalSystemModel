# CriticalSystemModel - Sous-systeme critique M14 (troncon partage)
> Recadrage du projet existant: abstraction formelle et simulation distribuee d'un mecanisme d'exclusion mutuelle

![Status](https://img.shields.io/badge/status-en%20cours-yellow)
![Equipe](https://img.shields.io/badge/equipe-4%20personnes-blue)
![Deadline](https://img.shields.io/badge/deadline-4%20mai%202026-red)

---

## Resume du projet

Le projet est recentre sur un sous-systeme critique inspire de la M14: le controle d'acces concurrent de deux trains automatiques a un troncon partage.

Ce choix est une reduction du projet existant, pas une recreation. Le cas a 2 trains est retenu comme plus petit cas non trivial pour etudier:
- concurrence
- exclusion mutuelle
- attente/arbitrage
- progression apres liberation

Le projet combine:
- Akka + Scala: simulation distribuee du protocole de messages
- Reseaux de Petri: abstraction formelle du comportement essentiel et verification des proprietes critiques

Positionnement academique explicite:
- ce projet ne modelise pas la ligne M14 complete
- ce projet modelise un sous-systeme critique simplifie, defendable et analysable a la main

---

## Objectifs du projet (version recadree)

| # | Objectif | Description |
|---|----------|-------------|
| 1 | Etat de l'art | Etudier verification formelle et systemes de transport urbain critiques |
| 2 | Modelisation | Definir une architecture d'acteurs minimale pour le controle d'acces au troncon partage |
| 3 | Traduction formelle | Construire un reseau de Petri compact, centre sur l'exclusion mutuelle |
| 4 | Verification | Prouver exclusion mutuelle, absence de collision, progression apres liberation |
| 5 | Simulation & comparaison | Relier messages Akka et transitions Petri sur 3 scenarios critiques |

---

## Savoirs requis

### Niveau 1 - Semaine 1-2

| Notion | Pourquoi c'est utile | Ressource suggeree |
|--------|----------------------|--------------------|
| Scala (bases) | Langage principal du projet | https://docs.scala-lang.org/tour/tour-of-scala.html |
| Modele Acteur | Structurer coordination concurrente | https://akka.io/docs/ |
| Git/GitHub | Travailler en equipe sans collision | Workflow ci-dessous |

### Niveau 2 - Semaine 2-4

| Notion | Pourquoi c'est utile |
|--------|----------------------|
| Akka Typed | Definir acteurs de train et arbitre de section critique |
| Messages asynchrones | Emettre demande/autorisation/attente/sortie/liberation |
| Reseaux de Petri | Capturer les transitions essentielles d'acces exclusif |
| Proprietes structurelles | Deadlock-freedom, bornitude, vivacite |

### Niveau 3 - Semaine 4-6

| Notion | Pourquoi c'est utile |
|--------|----------------------|
| LTL | Exprimer exclusion mutuelle et progression eventuelle |
| Invariants de place/transition | Prouver les contraintes critiques |
| ScalaTest + Akka TestKit | Valider scenarios de concurrence |

---

## Demarrage implementation

Structure cible du depot:

```
CriticalSystemModel/
├── src/
│   ├── main/
│   │   └── scala/
│   │       └── m14/              <- code Akka/Scala
│   └── test/
│       └── scala/
│           └── m14/              <- tests
├── petri/                        <- modeles formels
├── documentation/                <- toute la doc (lire START-ICI.md en premier)
│   ├── START-ICI.md              <- POINT D'ENTREE pour les nouveaux
│   ├── suivi/                    <- planning, historique, reprise
│   ├── gouvernance/              <- regles, equipe, coordination
│   ├── livrables/                <- rapport, biblio, comparaison, preuves
│   └── contexte/                 <- recadrage du projet
├── README.md
└── build.sbt
```

Commande de verification locale:

```bash
sbt compile
sbt test
```

---

## Gouvernance IA (obligatoire)

Lire et appliquer avant toute contribution:

- documentation/START-ICI.md (point d'entree)
- documentation/gouvernance/REGLES_PROJET.md
- documentation/suivi/PLAN.md
- documentation/suivi/HANDOVER.md
- documentation/suivi/historique.md

Exigences minimales avant merge:

- Perimetre respecte
- Invariants metier preserves
- Verification technique documentee (compile/test)
- Entree complete ajoutee dans documentation/suivi/historique.md

### Workflow Git

```bash
git checkout -b feature/nom-de-ma-tache
git add .
git commit -m "feat: description claire"
git push origin feature/nom-de-ma-tache
```

Exemples de branches (alignees sur le scope troncon partage) :
- feature/m14-coeur-troncon
- feature/m14-petri-troncon
- feature/m14-scenarios-validation

---

## Vue d'ensemble du sous-systeme critique

Architecture cible minimale:

```
[Train1Actor] --demande--> [SectionControllerActor] <--demande-- [Train2Actor]
    ^                          |       ^
    |                          |       |
    +---- autorisation/attente +-------+
            sortie/liberation
```

### Invariants metier critiques

- Un seul train peut occuper le troncon critique a un instant donne.
- L'occupation concurrente du troncon par deux trains est impossible.
- Un train en attente peut progresser quand le troncon est libere (sous hypothese d'arbitrage non pathologique).
- Le protocole de messages critique doit rester borne et interpretable.

### Proprietes formelles ciblees

- Safety 1: exclusion mutuelle sur le troncon partage.
- Safety 2: absence de collision sur le troncon critique.
- Liveness 1: progression eventuelle d'un train en attente apres liberation.
- Liveness 2: absence de blocage injustifie dans les scenarios retenus.

### Invariant principal

Invariant de ressource vise:

T1_sur_troncon + T2_sur_troncon + Troncon_libre = 1

Cet invariant sert de noyau de preuve de l'exclusion mutuelle.

---

## Perimetre du coeur du projet

Le coeur du projet inclut uniquement:
- 2 trains concurrents
- 1 troncon critique partage
- 1 mecanisme d'arbitrage d'acces
- 1 protocole minimal de messages

Hors coeur (extensions eventuelles uniquement):
- densite voyageurs
- supervision complete de station
- alertes complexes
- gestion globale multi-incidents
- logique d'exploitation complete de ligne

Voir le detail du recadrage dans documentation/contexte/recadrage-m14-troncon-critique.md.

---

## Bibliographie de depart

- Documentation officielle Akka: https://akka.io/docs/
- Tour of Scala: https://docs.scala-lang.org/tour/tour-of-scala.html
- Murata, T. (1989), Petri nets: Properties, analysis and applications, Proceedings of the IEEE.
- Introduction a la logique LTL (TU Munich): https://www7.in.tum.de/um/courses/verification/SS05/LTL.pdf

---

## Livrables

- Bibliographie commentee (`documentation/livrables/biblio.md`)
- Code Akka/Scala fonctionnel du troncon partage (`src/main/scala/m14/troncon/`)
- Reseau de Petri du controle d'acces concurrent (`petri/petri-troncon.md`)
- Analyseur Petri en Scala (`src/main/scala/m14/petri/`)
- Rapport de verification des proprietes (`documentation/livrables/rapport.md`)
- Comparaison simulation Akka vs modele formel (`documentation/livrables/comparaison.md`)
- Depot GitHub propre et trace

---

Projet realise dans le cadre du cours - 2026
