# 🎟️ CriticalSystemModel — Système de Billetterie Distribué
> Modélisation et Vérification Formelle avec Akka, Scala et Réseaux de Pétri

![Status](https://img.shields.io/badge/status-en%20cours-yellow)
![Équipe](https://img.shields.io/badge/équipe-4%20personnes-blue)
![Deadline](https://img.shields.io/badge/deadline-fin%20mai%202026-red)

---

## 📌 Résumé du projet

Ce projet consiste à **construire et vérifier formellement** un système de billetterie distribué.

L'idée : plusieurs composants (acteurs) communiquent pour gérer des réservations de billets. On doit s'assurer qu'il est **impossible** de vendre deux fois le même billet, qu'il n'y a **jamais de blocage**, et que les règles métier sont **toujours respectées**.

On utilise deux approches en parallèle :
- **Akka + Scala** → le vrai code qui simule le système
- **Réseaux de Pétri** → le modèle mathématique qui prouve que le système est correct

---

## 🎯 Objectifs du projet

| # | Objectif | Description |
|---|----------|-------------|
| 1 | **État de l'art** | Comprendre la vérification formelle et les réseaux de Pétri |
| 2 | **Modélisation** | Définir l'architecture en acteurs Akka pour la billetterie |
| 3 | **Traduction formelle** | Construire le réseau de Pétri correspondant |
| 4 | **Vérification** | Prouver : pas de deadlock, pas de double-réservation, stock jamais négatif |
| 5 | **Simulation & comparaison** | Tester le code Scala et comparer avec le modèle formel |

---

## 🧠 Savoirs requis (de zéro)

> Pas de panique. Voici exactement ce que chaque membre doit apprendre, dans l'ordre.

### 🔵 Niveau 1 — Bases à acquérir en semaine 1-2

| Notion | Pourquoi c'est utile | Ressource suggérée |
|--------|----------------------|--------------------|
| **Scala** (syntaxe de base) | Langage utilisé pour coder le projet | [Tour of Scala](https://docs.scala-lang.org/tour/tour-of-scala.html) |
| **Programmation orientée objet** | Classes, objets, méthodes | Vos cours Java/Python s'appliquent ici |
| **Modèle Acteur** | Comprendre comment Akka fonctionne | [Akka documentation intro](https://akka.io/docs/) |
| **Git & GitHub** | Travailler en équipe sans se marcher dessus | Voir section collaboration ci-dessous |

### 🟡 Niveau 2 — À apprendre en semaine 2-4

| Notion | Pourquoi c'est utile |
|--------|----------------------|
| **Akka Actors** | Définir les composants du système (Client, Billetterie, Paiement...) |
| **Messages Akka** | Faire communiquer les acteurs entre eux |
| **Réseaux de Pétri** | Modéliser les états et transitions du système |
| **Propriétés structurelles** | Absence de deadlock, bornitude, vivacité |

### 🔴 Niveau 3 — À apprendre en semaine 4-6

| Notion | Pourquoi c'est utile |
|--------|----------------------|
| **LTL (Linear Temporal Logic)** | Formaliser des propriétés comme "un billet ne peut jamais être vendu deux fois" |
| **Invariants de place/transition** | Prouver mathématiquement les propriétés du réseau de Pétri |
| **Tests unitaires Scala** | Valider le comportement du code |

---

## 🗓️ Plan de travail — Jour 1 (26 mars 2026)

### ✅ À faire AUJOURD'HUI (26 mars 2026)

#### 1. Installer l'environnement de développement (tout le monde)

```bash
# Étape 1 — Installer Java 11+ (requis pour Scala)
# Télécharger ici : https://adoptium.net/
# Vérifier l'installation :
java -version

# Étape 2 — Installer Scala via Coursier (gestionnaire officiel)
# Télécharger Coursier ici : https://get-coursier.io/docs/cli-installation
# Puis lancer :
cs setup
# Vérifier :
scala -version

# Étape 3 — Installer sbt (outil de build Scala)
# Inclus avec Coursier, sinon : https://www.scala-sbt.org/download.html
sbt -version

# Étape 4 — Extension VSCode
# Ouvrir VSCode → Extensions → chercher "Scala (Metals)" → Installer
```

#### 2. Cloner le repo et créer la structure du projet

```bash
git clone https://github.com/MonsieurNikko/CriticalSystemModel
cd CriticalSystemModel
```

Structure de dossiers à créer dès aujourd'hui :
```
CriticalSystemModel/
├── src/
│   ├── main/
│   │   └── scala/
│   │       └── billetterie/       ← votre code Akka/Scala ici
│   └── test/
│       └── scala/
│           └── billetterie/       ← vos tests ici
├── petri/                         ← vos modèles de réseaux de Pétri (schémas, analyses)
├── docs/                          ← votre rapport, bibliographie
├── README.md                      ← ce fichier
└── build.sbt                      ← configuration du projet
```

#### 3. Créer le fichier build.sbt

Créer un fichier `build.sbt` à la racine avec ce contenu :

```scala
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version      := "0.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "CriticalSystemModel",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.8.5" % Test,
      "org.scalatest"     %% "scalatest" % "3.2.17" % Test
    )
  )
```

#### 4. Tester que tout fonctionne

```bash
cd CriticalSystemModel
sbt compile
# Si vous voyez "success" → votre environnement est prêt ✅
```

---

## 👥 Collaboration en équipe — Les règles d'or

### 🌿 Workflow Git (à suivre sans exception)

```bash
# Ne JAMAIS travailler directement sur main
# Toujours créer une branche pour votre tâche :
git checkout -b feature/nom-de-ma-tache

# Exemple :
git checkout -b feature/acteur-billetterie
git checkout -b feature/reseau-petri-reservation
git checkout -b feature/rapport-etat-de-lart

# Quand votre tâche est finie :
git add .
git commit -m "feat: description claire de ce que vous avez fait"
git push origin feature/nom-de-ma-tache
# Puis ouvrir une Pull Request sur GitHub → un autre membre relit → merge
```

### 💬 Conventions de commit (à utiliser dès le début)

| Préfixe | Usage |
|---------|-------|
| `feat:` | Nouvelle fonctionnalité |
| `fix:` | Correction d'un bug |
| `docs:` | Modification de documentation |
| `test:` | Ajout ou modification de tests |
| `refactor:` | Restructuration du code sans changer le comportement |

Exemple : `feat: ajout de l'acteur ClientActor avec message ReservationRequest`

### 📋 Répartition suggérée des rôles

| Rôle | Responsabilité principale |
|------|---------------------------|
| **Lead Dev Scala/Akka** | Architecture des acteurs, code principal |
| **Modélisateur Pétri** | Construction et analyse du réseau de Pétri |
| **Rédacteur Rapport** | État de l'art, documentation, bibliographie |
| **Intégration & Tests** | Tests unitaires, comparaison simulation/modèle |

> ⚠️ Ces rôles ne sont pas des silos ! Tout le monde doit comprendre l'ensemble du projet. On tourne et on s'entraide.

### 📅 Rituels d'équipe recommandés

- **Réunion hebdomadaire courte** (30 min) : qu'est-ce que j'ai fait ? qu'est-ce que je vais faire ? est-ce que j'ai un blocage ?
- **Issues GitHub** : chaque tâche = une issue = une branche = une Pull Request
- **Ne jamais bloquer en silence** : si vous êtes bloqués plus de 2h → vous en parlez à l'équipe

---

## 🗺️ Vue d'ensemble du système de billetterie

Le système qu'on va construire aura ces composants (acteurs Akka) :

```
[Client] ──demande billet──► [ActeurBilletterie] ──vérifie──► [ActeurStock]
                                      │
                                      └──► [ActeurPaiement] ──► [ActeurConfirmation]
```

**Règles critiques (invariants métier) :**
- Un billet ne peut jamais être vendu deux fois (pas de double-réservation)
- Le stock de billets ne peut jamais passer en dessous de 0
- Un paiement refusé annule toujours la réservation
- Le système ne peut jamais se bloquer indéfiniment (pas de deadlock)

---

## 📚 Bibliographie de départ

- [Documentation officielle Akka](https://akka.io/docs/)
- [Tour of Scala](https://docs.scala-lang.org/tour/tour-of-scala.html)
- Murata, T. (1989). *Petri nets: Properties, analysis and applications*. Proceedings of the IEEE — **référence incontournable sur les réseaux de Pétri**
- [Introduction à la logique LTL — TU Munich](https://www7.in.tum.de/um/courses/verification/SS05/LTL.pdf)

---

## 📦 Livrables (rappel)

- [ ] Bibliographie commentée
- [ ] Code Akka/Scala fonctionnel
- [ ] Réseau de Pétri du système
- [ ] Rapport de vérification des propriétés
- [ ] Comparaison simulation vs modèle formel
- [ ] Ce dépôt GitHub propre et documenté

---

*Projet réalisé dans le cadre du cours — 2026*
