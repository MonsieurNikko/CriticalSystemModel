# CriticalSystemModel - Gestion Critique du Trafic M14 a Chatelet
> Modelisation et verification formelle avec Akka, Scala et Reseaux de Petri

![Status](https://img.shields.io/badge/status-en%20cours-yellow)
![Equipe](https://img.shields.io/badge/equipe-4%20personnes-blue)
![Deadline](https://img.shields.io/badge/deadline-fin%20mai%202026-red)

---

## Resume du projet

Ce projet consiste a construire et verifier formellement un systeme distribue de gestion de trafic passagers sur la ligne 14 (zone Chatelet), avec focalisation sur la securite de quai et la gestion d'incidents.

Le systeme doit coordonner:
- la supervision de densite passagers (quais et couloirs)
- le pilotage des acces (ouverture/fermeture)
- la gestion d'incidents critiques
- la diffusion d'alertes operationnelles

Deux approches sont menees en parallele:
- Akka + Scala: implementation executable et simulation
- Reseaux de Petri: preuve formelle de surete et vivacite

---

## Objectifs du projet

| # | Objectif | Description |
|---|----------|-------------|
| 1 | Etat de l'art | Etudier verification formelle et systemes de transport urbain critiques |
| 2 | Modelisation | Definir l'architecture d'acteurs Akka pour M14 Chatelet |
| 3 | Traduction formelle | Construire le reseau de Petri du controle de trafic |
| 4 | Verification | Prouver absence de deadlock, surete capacitaire, traitement des incidents |
| 5 | Simulation & comparaison | Confronter comportement Akka et modele formel |

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
| Akka Typed | Definir acteurs de supervision et securite |
| Messages asynchrones | Echanger des evenements de trafic/incidents |
| Reseaux de Petri | Capturer transitions de mode normal/surete |
| Proprietes structurelles | Deadlock-freedom, bornitude, vivacite |

### Niveau 3 - Semaine 4-6

| Notion | Pourquoi c'est utile |
|--------|----------------------|
| LTL | Exprimer les obligations temporelles de securite |
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
├── documentation/                <- gouvernance et suivi
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

- documentation/AGENT_RULES.md
- documentation/historique.md

Exigences minimales avant merge:

- Perimetre respecte
- Invariants metier preserves
- Verification technique documentee (compile/test)
- Entree complete ajoutee dans documentation/historique.md

### Workflow Git

```bash
git checkout -b feature/nom-de-ma-tache
git add .
git commit -m "feat: description claire"
git push origin feature/nom-de-ma-tache
```

Exemples de branches:
- feature/m14-station-control
- feature/m14-petri-model
- feature/m14-safety-tests

---

## Vue d'ensemble du systeme M14 Chatelet

Architecture fonctionnelle de reference:

```
[FlowSensorActor] --densite--> [StationControlActor] --commandes--> [GateControlActor]
                                      |
                                      +--> [IncidentManagerActor]
                                      |
                                      +--> [AlertDispatcherActor]
```

### Invariants metier critiques

- Le seuil de densite d'une zone ne doit jamais rester depasse sans action de controle.
- En incident critique, le systeme bascule en mode Safety et ferme les acces.
- Aucune alerte critique n'est perdue ni traitee deux fois.
- Le systeme ne doit jamais entrer en deadlock.
- Apres resolution d'incident, un retour controle au mode Normal doit etre possible.

### Proprietes formelles ciblees

- Safety 1: pas d'ouverture d'acces en zone fermee pour incident critique.
- Safety 2: porte fermee si densite > seuil.
- Safety 3: unicite de traitement d'une alerte critique.
- Liveness 1: toute alerte critique est traitee en temps borne.
- Liveness 2: en absence d'incident actif et sous seuil, les acces reouvrent.

---

## Bibliographie de depart

- Documentation officielle Akka: https://akka.io/docs/
- Tour of Scala: https://docs.scala-lang.org/tour/tour-of-scala.html
- Murata, T. (1989), Petri nets: Properties, analysis and applications, Proceedings of the IEEE.
- Introduction a la logique LTL (TU Munich): https://www7.in.tum.de/um/courses/verification/SS05/LTL.pdf

---

## Livrables

- Bibliographie commentee
- Code Akka/Scala fonctionnel (M14 Chatelet)
- Reseau de Petri du controle de trafic
- Rapport de verification des proprietes
- Comparaison simulation Akka vs modele formel
- Depot GitHub propre et trace

---

Projet realise dans le cadre du cours - 2026
