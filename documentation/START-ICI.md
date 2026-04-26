# START ICI - Guide d'arrivee sur le projet

> Tu viens d'arriver sur le repo ? Lis ce fichier en 5 minutes et tu sauras exactement quoi faire.

---

## Le projet en une phrase

On prouve qu'un **systeme de metro automatique (M14)** ne peut pas avoir 2 trains sur le meme troncon en meme temps. On le fait avec du code Scala/Akka ET un modele formel (reseau de Petri).

## Structure du depot

```
CriticalSystemModel/
├── src/main/scala/m14/
│   ├── troncon/               <- LE COEUR DU PROJET
│   │   ├── Protocol.scala       Messages entre trains et controleur
│   │   ├── SectionController.scala  Arbitre du troncon (FIFO)
│   │   └── Train.scala          Machine a etats du train
│   ├── petri/                 <- ANALYSEUR DE PREUVES
│   │   ├── PetriNet.scala       Reseau de Petri encode en dur
│   │   └── Analyseur.scala      BFS + verification invariants
│   ├── StationControl.scala   (hors scope, ne pas toucher)
│   └── Main.scala             Point d'entree
│
├── src/test/scala/m14/        <- TESTS (22 tests, tous verts)
│   ├── troncon/
│   │   ├── SectionControllerSpec.scala  3 scenarios critiques
│   │   └── TrainSpec.scala              Tests du train
│   └── petri/
│       └── AnalyseurSpec.scala          Tests de l'analyseur
│
├── petri/
│   └── petri-troncon.md       SOURCE DE VERITE du modele formel
│
├── documentation/             <- TU ES ICI
│   ├── START-ICI.md             Ce fichier
│   ├── suivi/                   Ou en est-on ?
│   │   ├── PLAN.md                Sprint et phases
│   │   ├── HANDOVER.md            Guide technique de reprise
│   │   └── historique.md          Journal de tous les changements
│   ├── gouvernance/             Comment on travaille ?
│   │   ├── REGLES_PROJET.md       Regles de code et de commit
│   │   ├── repartition-equipe.md  Qui fait quoi
│   │   ├── protocole-coordination.md  Contrat code/preuves
│   │   └── lexique.md            Vocabulaire partage
│   ├── livrables/               Ce qu'on rend le 4 mai
│   │   ├── rapport.md            L4 - Rapport de verification
│   │   ├── biblio.md             L1 - Bibliographie commentee
│   │   ├── comparaison.md        L6 - Akka vs Petri
│   │   └── preuves-manuelles.md  Carnet de preuves a la main
│   └── contexte/                Pourquoi ce projet
│       └── recadrage-m14-troncon-critique.md
│
├── README.md                  Presentation generale

└── build.sbt                  Config Scala/Akka
```

## Par ou commencer (Poles d'activites)

Nous avons 3 grands poles restants pour terminer le projet. Choisissez un pole selon vos affinites et coordonnez-vous :

### Pole 1 : Preuves Formelles & LTL (Charge : Moyenne)
- **Objectif** : Blinder la theorie et les preuves mathematiques.
- **Actions** :
  1. Lire `petri/petri-troncon.md`.
  2. Completer le carnet de preuves : `documentation/livrables/preuves-manuelles.md` (taches 2 a 7).
  3. Rediger la formalisation LTL (Safety/Liveness).

### Pole 2 : Comparaison & Tests (Charge : Faible)
- **Objectif** : Prouver que notre code Scala correspond au reseau de Petri.
- **Actions** :
  1. Lancer l'analyseur : `sbt "runMain m14.petri.Analyseur"`.
  2. Inserer les resultats dans `documentation/livrables/comparaison.md`.
  3. Mettre a jour le mapping explicite entre Messages Akka et Transitions Petri.

### Pole 3 : Redaction du Rapport & Biblio (Charge : Forte)
- **Objectif** : Assembler le livrable final (8-15 pages).
- **Actions** :
  1. Relire `documentation/suivi/PLAN.md` (Phases 7 et 8).
  2. Completer la bibliographie dans `documentation/livrables/biblio.md`.
  3. Rediger le rapport final `documentation/livrables/rapport.md` en integrant le travail des poles 1 et 2.

## Commandes essentielles

```bash
# Verifier que tout compile
sbt compile

# Lancer tous les tests (22 tests)
sbt test

# Lancer l'analyseur Petri (voir la preuve formelle)
sbt "runMain m14.petri.Analyseur"
```

## Etat actuel (mis a jour le 26 avril 2026)

| Quoi | Etat |
|------|------|
| Code Akka (Train + SectionController) | FAIT |
| Tests des 3 scenarios critiques | FAIT (3/3 verts) |
| Analyseur Petri (BFS + invariants) | FAIT (8 etats, 0 deadlocks) |
| Rapport de verification | A REDIGER |
| Preuves manuelles (tableau T2, Liveness) | FAIT |
| Comparaison Akka vs Petri | A FINALISER |

## Regles d'or (lire avant de coder)

1. **Jamais de push sans `sbt test` vert.**
2. **Jamais de push sans mise a jour de `documentation/suivi/historique.md`.**
3. **Jamais de nouveau message Akka** (les 4 sont verrouilles : Demande, Sortie, Autorisation, Attente).
4. **En cas de doute** : lis `documentation/gouvernance/protocole-coordination.md` section 4 (FAQ).

## Besoin d'aide ?

- Comprendre un terme ? → `documentation/gouvernance/lexique.md`
- Comprendre le planning ? → `documentation/suivi/PLAN.md`
- Comprendre le code ? → `documentation/suivi/HANDOVER.md`
- Comprendre le modele Petri ? → `petri/petri-troncon.md`
