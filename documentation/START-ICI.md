# START ICI - Guide rapide du projet

> Nouveau sur le repo ? Lis cette page avant de coder. Elle dit quoi lancer, quoi lire, quoi modifier, et ce qu'il ne faut pas casser.

---

## 1) Le projet en une phrase

On modelise un sous-systeme critique inspire de la M14 : **deux trains automatiques veulent acceder a un meme troncon partage, et on prouve qu'ils ne peuvent jamais etre dessus en meme temps**.

Le projet combine :

- **Akka / Scala** : simulation distribuee par acteurs ;
- **Reseau de Petri** : modele formel du protocole ;
- **Analyseur Scala maison** : exploration des marquages, invariants, deadlocks ;
- **Demo HTML** : visualisation pas-a-pas pour la soutenance.

Invariant central :

```text
T1_sur_troncon + T2_sur_troncon + Troncon_libre = 1
```

---

## 2) A lancer en premier

Depuis la racine du projet :

```bash
sbt compile
sbt test
sbt "runMain m14.Main"
```

Resultat attendu :

- `sbt compile` passe ;
- `sbt test` affiche 19 tests reussis ;
- `runMain m14.Main` affiche les 3 scenarios + le bilan Petri.

Toutes les commandes utiles sont centralisees ici :

```text
documentation/suivi/COMMANDES.md
```

---

## 3) Structure du depot

```text
CriticalSystemModel/
├── src/main/scala/m14/
│   ├── troncon/                  <- coeur Akka
│   │   ├── Protocol.scala          messages Demande / Sortie / Autorisation / Attente
│   │   ├── SectionController.scala arbitre FIFO du troncon
│   │   └── Train.scala             machine a etats d'un train
│   ├── petri/                    <- analyse formelle en Scala
│   │   ├── PetriNet.scala          7 places, 6 transitions
│   │   └── Analyseur.scala         BFS + invariants + deadlocks
│   └── Main.scala                <- demo console Akka + Petri
│
├── src/test/scala/m14/           <- tests ScalaTest / Akka TestKit
│   ├── troncon/
│   │   ├── SectionControllerSpec.scala
│   │   └── TrainSpec.scala
│   └── petri/
│       └── AnalyseurSpec.scala
│
├── petri/
│   └── petri-troncon.md          <- source de verite du modele Petri
│
├── demo/
│   ├── index.html                <- demo visuelle autonome
│   └── README.md
│
├── documentation/
│   ├── START-ICI.md              <- cette page
│   ├── suivi/
│   │   ├── COMMANDES.md           commandes utiles
│   │   ├── PLAN.md                plan de fin de projet
│   │   ├── HANDOVER.md            guide technique de reprise
│   │   └── historique.md          journal des changements
│   ├── gouvernance/
│   │   ├── REGLES_PROJET.md
│   │   ├── repartition-equipe.md
│   │   ├── protocole-coordination.md
│   │   └── lexique.md
│   ├── livrables/
│   │   ├── rapport.md             rapport final a terminer
│   │   ├── biblio.md              bibliographie commentee
│   │   ├── comparaison.md         Akka vs Petri
│   │   └── preuves-manuelles.md   preuves a la main
│   └── contexte/
│       └── recadrage-m14-troncon-critique.md
│
├── README.md
└── build.sbt
```

---

## 4) Etat actuel

Mis a jour le 27 avril 2026.

| Element | Etat |
|---|---|
| Code Akka `Train` + `SectionController` | FAIT |
| Protocole de messages | FAIT, verrouille a 4 messages |
| Reseau de Petri | FAIT, 7 places / 6 transitions |
| Analyseur Petri | FAIT, 8 marquages, 0 deadlock |
| Tests | FAIT, 19 tests verts |
| Demo console | FAIT avec `sbt "runMain m14.Main"` |
| Demo HTML | Prototype FAIT dans `demo/index.html` |
| Comparaison Akka vs Petri | A finaliser avec sortie analyseur |
| Rapport final | A rediger / completer en priorite |

---

## 5) Qui fait quoi maintenant

### Piste A - Preuves et LTL

Objectif : rendre les preuves defendables a l'oral.

Fichiers a lire :

- `petri/petri-troncon.md`
- `documentation/livrables/preuves-manuelles.md`
- `documentation/gouvernance/lexique.md`

Actions prioritaires :

1. Verifier que les 8 marquages sont bien expliques.
2. Completer les cases restantes du carnet de preuves.
3. Clarifier la limite : la liveness depend d'une hypothese de fairness / FIFO.

### Piste B - Comparaison et tests

Objectif : montrer que le code Akka correspond au modele Petri.

Fichiers a lire :

- `documentation/livrables/comparaison.md`
- `src/test/scala/m14/troncon/SectionControllerSpec.scala`
- `src/main/scala/m14/Main.scala`

Actions prioritaires :

1. Lancer `sbt "runMain m14.Main"`.
2. Lancer `sbt "runMain m14.petri.Analyseur"`.
3. Copier les resultats utiles dans `comparaison.md`.
4. Ajouter plus tard un test d'integration Akka/Petri si le temps le permet.

### Piste C - Rapport et biblio

Objectif : transformer le travail technique en livrable lisible par la prof.

Fichiers a lire :

- `documentation/livrables/rapport.md`
- `documentation/livrables/biblio.md`
- `documentation/contexte/recadrage-m14-troncon-critique.md`

Actions prioritaires :

1. Remplacer tous les blocs "A rediger".
2. Expliquer pourquoi le modele est volontairement petit.
3. Citer la bibliographie dans le corps du rapport.
4. Ajouter les limites : pas de pannes, pas de temps reel, pas de N trains.

### Piste D - Demo et soutenance

Objectif : rendre le projet facile a comprendre en 2 minutes.

Fichiers a lire :

- `demo/index.html`
- `src/main/scala/m14/Main.scala`
- `documentation/suivi/COMMANDES.md`

Actions prioritaires :

1. Tester la demo HTML en pas-a-pas.
2. Preparer une phrase simple pour chaque scenario.
3. Montrer l'invariant qui reste OK pendant la simulation.

---

## 6) Ce qu'il ne faut pas changer sans accord

Ces choix sont verrouilles pour eviter de casser les preuves :

- pas plus de 2 trains ;
- pas plus de 1 troncon partage ;
- pas de nouveau message Akka ;
- pas de nouveau modele Petri sans mettre a jour les preuves ;
- pas de generalisation a N trains ;
- pas de pannes ou timeouts dans le coeur du projet.

La demo HTML peut etre amelioree visuellement, mais elle doit rester alignee sur le modele actuel.

---

## 7) Regles d'or

1. Avant de push : `sbt compile`, `sbt test`.
2. Pour la demo console : `sbt "runMain m14.Main"`, pas `sbt run`.
3. Toute modification importante doit etre tracee dans `documentation/suivi/historique.md`.
4. Ne pas commiter `target/`, `.bloop/`, `.metals/`, `demo-out.txt` ou des fichiers temporaires.
5. Si le code et le modele Petri divergent, le fichier de reference est `petri/petri-troncon.md`.

---

## 8) Besoin d'aide ?

- Commandes : `documentation/suivi/COMMANDES.md`
- Planning : `documentation/suivi/PLAN.md`
- Reprise technique : `documentation/suivi/HANDOVER.md`
- Vocabulaire : `documentation/gouvernance/lexique.md`
- Regles de contribution : `documentation/gouvernance/REGLES_PROJET.md`
- Modele Petri : `petri/petri-troncon.md`
