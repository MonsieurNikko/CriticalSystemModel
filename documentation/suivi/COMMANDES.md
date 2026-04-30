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

Ouvrir le fichier suivant dans un navigateur :

```text
demo/index.html
```

Depuis PowerShell, on peut aussi lancer :

```powershell
Start-Process .\demo\index.html
```

La page permet de tester (extension PSD) :

- scenario nominal (cycle complet canton + quai + portes palieres) ;
- scenario concurrence canton + quai (T1 et T2 partagent les ressources) ;
- scenario tentative PSD invalide (overlay rouge : la garde de surete bloque) ;
- timeline cliquable, lecture / pause / pas-a-pas ;
- visualisation des 5 acteurs, des 12 places Petri, des popups Akka et du bandeau d'invariants en temps reel.

Pour regenerer les traces JSON depuis le modele Scala :

```bash
sbt "runMain m14.demo.GenererTraces"
```

Cette commande ecrit `demo/trace-nominal.json`, `demo/trace-concurrence.json`, `demo/trace-violation.json` (sources de verite : `m14.petri.PetriNet.tirer`).

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
