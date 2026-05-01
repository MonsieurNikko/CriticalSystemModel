# CriticalSystemModel - Sous-systeme critique M14 (canton + quai + portes palieres)
> Projet construit en deux temps : d'abord un socle simple **2 trains / 1 troncon partage**, puis une extension M14 plus complete le 29/04/2026 avec **canton + quai + portes palieres (PSD)**.

![Status](https://img.shields.io/badge/status-finalisation-yellow)
![Equipe](https://img.shields.io/badge/equipe-4%20personnes-blue)
![Deadline](https://img.shields.io/badge/deadline-4%20mai%202026-red)
![Extension](https://img.shields.io/badge/extension-PSD%20valid%C3%A9e-green)

---

## Resume du projet

Le projet est recentre sur un sous-systeme critique inspire de la M14: le controle d'acces concurrent de deux trains automatiques a un canton de signalisation, suivi d'un arret en station avec gestion des portes palieres (PSD - Platform Screen Doors).

Ce choix est une reduction du projet existant, pas une recreation. Le cas a 2 trains est retenu comme plus petit cas non trivial pour etudier:
- concurrence
- exclusion mutuelle canton et quai
- attente/arbitrage FIFO
- progression apres liberation
- surete des portes palieres

### Lecture chronologique pour le rendu

Le projet n'a pas commence directement avec les portes palieres. Il a ete construit par paliers :

| Etape | Modele | Objectif | Resultat |
|---|---|---|---|
| **1 - Socle initial** | 2 trains + 1 troncon partage | Prouver l'exclusion mutuelle et l'absence de deadlock sur le plus petit cas concurrent interessant | 3 acteurs, 7 places, 6 transitions, 8 marquages atteignables |
| **2 - Upgrade M14** | 2 trains + canton + quai + portes palieres | Rendre le sujet plus proche de la M14 et ajouter les proprietes critiques PSD | 5 acteurs, 12 places, 12 transitions, 20 marquages, 5 invariants, 49 tests |

Dans le rendu final, le mot `canton` correspond au troncon critique du socle initial, renomme avec un vocabulaire ferroviaire plus precis. Le modele final garde donc l'idee de depart (**2 trains qui se partagent une zone critique**) et l'etend avec le quai et les portes.

Le projet combine:
- Akka + Scala: simulation distribuee du protocole de messages (5 acteurs)
- Reseaux de Petri: abstraction formelle du comportement essentiel (12 places, 12 transitions)
- Analyseur Scala: exploration exhaustive (20 marquages, 40 arcs, 5 invariants, 5 proprietes LTL)
- Demo HTML: animation locale pilotee par des traces JSON generees depuis le modele Petri

Positionnement academique explicite:
- ce projet ne modelise pas la ligne M14 complete
- ce projet modelise un sous-systeme critique simplifie, defendable et analysable a la main

---

## Objectifs du projet (version recadree)

| # | Objectif | Description |
|---|----------|-------------|
| 1 | Etat de l'art | Etudier verification formelle et systemes de transport urbain critiques |
| 2 | Modelisation | Construire d'abord le socle 2 trains / 1 troncon, puis l'etendre a canton + quai + portes palieres |
| 3 | Traduction formelle | Construire un reseau de Petri compact, centre sur les ressources critiques |
| 4 | Verification | Prouver exclusion mutuelle, surete PSD, absence de deadlock et LTL bornee |
| 5 | Simulation & comparaison | Relier messages Akka, transitions Petri et demo sur les scenarios critiques |

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

Commandes de verification locale:

```bash
sbt compile
sbt test
sbt "runMain m14.Main"
sbt "runMain m14.petri.Analyseur"
sbt "runMain m14.demo.LancerDemo"
```

Resultat attendu :
- `sbt test` : **49 tests verts**.
- `runMain m14.petri.Analyseur` : **20 marquages**, **40 arcs**, **5 invariants PASSE**, **0 deadlock**, **5 proprietes LTL PASSE**.
- `runMain m14.demo.LancerDemo` : regenere les 5 traces JSON et ouvre la demo locale.

---

## Gouvernance (obligatoire)

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
          +-------------------+
Train1 -> | SectionController | <- Train2
          +-------------------+
                    |
                    v
          +-------------------+
          |  QuaiController   |
          +-------------------+
                    |
                    v
          +-------------------+
          | GestionnairePortes|
          +-------------------+
```

### Invariants metier critiques

- Un seul train peut occuper le canton critique a un instant donne.
- Un seul train peut etre a quai a un instant donne.
- **Les portes palieres ne s'ouvrent que si un train est a quai (PSD-Open).**
- **Un train ne peut quitter le quai que portes fermees (PSD-Departure).**
- Un train en attente peut progresser quand le canton est libere (sous hypothese d'arbitrage non pathologique).
- Le protocole de messages critique doit rester borne et interpretable.

### Proprietes formelles ciblees

- Safety 1: exclusion mutuelle sur le canton de signalisation.
- Safety 2: exclusion mutuelle sur le quai.
- Safety 3: surete d'ouverture des portes palieres (CRITIQUE M14).
- Safety 4: surete au demarrage train portes fermees (CRITIQUE M14).
- Liveness 1: progression eventuelle d'un train en attente apres liberation.
- Liveness 2: absence de blocage injustifie dans les scenarios retenus.

### Invariants principaux (3 invariants de ressource)

```
canton : T1_sur_canton + T2_sur_canton + Canton_libre = 1
quai   : T1_a_quai     + T2_a_quai     + Quai_libre   = 1
portes : Portes_fermees + Portes_ouvertes              = 1
```

### Invariants critiques de surete PSD

```
PSD-Open      : G ( Portes_ouvertes -> (T1_a_quai OR T2_a_quai) )
PSD-Departure : Ti_depart_quai tirable -> Portes_fermees = 1
```

---

## Perimetre du coeur du projet final (apres upgrade PSD du 29/04)

Le **socle initial** etait volontairement plus petit : 2 trains, 1 troncon partage, un controleur d'acces, 7 places Petri et 6 transitions. Une fois ce socle coherent et teste, il a ete garde comme base et **upgrade** vers le modele final ci-dessous.

Le coeur du projet inclut uniquement:
- 2 trains concurrents (4 etats chacun : `hors`, `enAttente`, `surCanton`, `aQuai`)
- 1 canton de signalisation partage
- 1 quai en station partage
- 1 paire de portes palieres (PSD)
- 3 controleurs : `SectionController` (canton) + `QuaiController` (quai) + `GestionnairePortes` (PSD)
- Protocole minimal : 6 messages vers controleurs + 4 messages vers trains
- Reseau de Petri : **12 places, 12 transitions effectives**

Hors coeur (extensions eventuelles uniquement):
- densite voyageurs
- gestion globale multi-incidents
- logique d'exploitation complete de ligne
- N trains, 2e canton, 2 quais opposes
- pannes, timeouts, fairness explicite, Petri temporise

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
- Code Akka/Scala fonctionnel du sous-systeme critique (`src/main/scala/m14/troncon/`)
- Reseau de Petri final du controle d'acces + quai + PSD (`petri/petri-troncon.md`)
- Analyseur Petri en Scala (`src/main/scala/m14/petri/`)
- Rapport de verification des proprietes (`documentation/livrables/rapport.md`)
- Comparaison simulation Akka vs modele formel (`documentation/livrables/comparaison.md`)
- Depot GitHub propre et trace

---

Projet realise dans le cadre du cours - 2026
