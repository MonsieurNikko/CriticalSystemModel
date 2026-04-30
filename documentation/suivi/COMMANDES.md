# Commandes utiles - CriticalSystemModel

Ce fichier regroupe les commandes a connaitre pour travailler, verifier et presenter le projet.

---

## 1) Verification Scala

Compiler le projet :

```bash
sbt compile
```

Lancer tous les tests :

```bash
sbt test
```

Lancer compilation + tests en une seule commande :

```bash
sbt "compile; test"
```

Resultat attendu avant rendu :

```text
All tests passed.
Total number of tests run: 49
```

(Repartition : TrainSpec 6 + SectionControllerSpec 3 + QuaiControllerSpec 5 + GestionnairePortesSpec 5 + AnalyseurSpec 30 = 49.)

---

## 2) Demonstration console

Lancer la demo principale Akka + Petri :

```bash
sbt "runMain m14.Main"
```

Cette commande affiche :

- les 3 scenarios critiques (cycle nominal, concurrence canton+quai, tentative PSD invalide) ;
- les messages Akka ;
- les transitions Petri associees ;
- le bilan automatique : **20 marquages**, **5 invariants PASSE**, **0 deadlock**, exclusions mutuelles canton/quai PASSE.

Important : eviter `sbt run` seul, car le projet contient plusieurs points d'entree et sbt peut demander de choisir lequel lancer.

---

## 3) Analyseur Petri

Lancer uniquement l'analyseur Petri :

```bash
sbt "runMain m14.petri.Analyseur"
```

Cette commande sert a verifier :

- **20 marquages atteignables** (M0..M19) ;
- les **3 invariants de ressource** (canton, quai, portes) ;
- les **2 invariants critiques de surete PSD** (PSD-Open, PSD-Departure) ;
- les invariants par train sur 4 etats ;
- l'absence de deadlock ;
- l'exclusion mutuelle canton et quai ;
- le **graphe d'accessibilite** : 40 arcs etiquetes M_i --transition--> M_j ;
- la verification **LTL programmatique** (Phase 7) : G safety canton/quai/PSD-Open + G F liveness canton T1/T2.

---

## 4) Demo visuelle HTML

### 4.a) Lancement automatique (recommande pour la soutenance)

```bash
sbt "runMain m14.demo.LancerDemo"
```

Cette commande effectue automatiquement :

1. la **regeneration des 5 fichiers JSON** depuis le modele Petri verifie ;
2. le **demarrage d'un mini serveur HTTP local** (port 8000, ou premier port libre dans 8000-8010) servant le dossier `demo/` ;
3. l'**ouverture automatique du navigateur** par defaut sur `http://localhost:<port>/index.html`.

Pour arreter : revenir dans le terminal sbt et appuyer sur **Entree**.

Avantages : pas de probleme `file://` / CORS, traces toujours a jour, parcours soutenance en une commande.

### 4.b) Lancement manuel

Si le port 8000-8010 est indisponible ou si le navigateur ne s'ouvre pas automatiquement :

- regenerer les traces explicitement : `sbt "runMain m14.demo.GenererTraces"` ;
- ouvrir directement `demo/index.html` dans un navigateur ; cela peut bloquer le `fetch()` des JSON selon le navigateur (file://). Privilegier `LancerDemo` ou un serveur statique tiers (`python3 -m http.server` depuis `demo/`).

### 4.c) Scenarios disponibles

La page permet de tester (extension PSD complete, 5 scenarios) :

- **A** cycle nominal (1 train, 7 etapes) ;
- **B** concurrence canton + quai (2 trains, 11 etapes) ;
- **C** cycle complet sequentiel des 2 trains - liveness (13 etapes) ;
- **D** tentative PSD-Open invalide (overlay rouge, 3 etapes - CRITIQUE) ;
- **E** tentative PSD-Departure portes ouvertes (overlay rouge puis sequence corrigee, 9 etapes - CRITIQUE).

Toutes les traces sont generees depuis `m14.petri.PetriNet.tirer` (source de verite).

---

## 5) Commandes Git de base

Voir les fichiers modifies :

```bash
git status --short
```

Voir le detail des changements :

```bash
git diff
```

Voir un resume du diff :

```bash
git diff --stat
```

Ajouter les fichiers utiles :

```bash
git add README.md documentation/ petri/ src/ demo/ build.sbt project/build.properties .gitignore
```

Creer un commit :

```bash
git commit -m "docs: ajouter les commandes utiles du projet"
```

Pousser la branche courante :

```bash
git push
```

---

## 6) Avant rendu

Checklist de commandes :

```bash
git status --short
sbt compile
sbt test
sbt "runMain m14.Main"
sbt "runMain m14.petri.Analyseur"
git diff --stat
```

Points a verifier :

- aucun fichier important non commite ;
- pas de dossier `target/`, `.bloop/`, `.metals/` ajoute au commit ;
- `demo/index.html` present si la demo visuelle est gardee ;
- `documentation/livrables/rapport.md` ne contient plus de sections "A rediger" ;
- `documentation/livrables/comparaison.md` contient les resultats de l'analyseur ;
- le lien GitHub pointe vers la branche finale a jour.

---

## 7) Commandes a eviter

Ne pas utiliser sans validation explicite :

```bash
git reset --hard
git checkout -- .
git clean -fd
```

Ces commandes peuvent supprimer du travail local non commite.
