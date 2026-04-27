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
Total number of tests run: 19
```

---

## 2) Demonstration console

Lancer la demo principale Akka + Petri :

```bash
sbt "runMain m14.Main"
```

Cette commande affiche :

- les 3 scenarios critiques ;
- les messages Akka ;
- les transitions Petri associees ;
- le bilan automatique : 8 etats, invariant OK, 0 deadlock, collision NON.

Important : eviter `sbt run` seul, car le projet contient plusieurs points d'entree et sbt peut demander de choisir lequel lancer.

---

## 3) Analyseur Petri

Lancer uniquement l'analyseur Petri :

```bash
sbt "runMain m14.petri.Analyseur"
```

Cette commande sert a verifier :

- les marquages atteignables ;
- l'invariant principal ;
- les invariants par train ;
- l'absence de deadlock ;
- l'exclusion mutuelle.

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

La page permet de tester :

- scenario nominal ;
- scenario concurrence ;
- scenario liberation / progression ;
- ordre Train1 ou Train2 en premier ;
- lecture automatique ou pas-a-pas ;
- affichage des places, transitions et invariants.

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
