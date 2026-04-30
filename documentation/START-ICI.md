# START ICI - Guide rapide du projet

> Nouveau sur le repo ? Lis cette page avant de coder. Elle dit quoi lancer, quoi lire, quoi modifier, et ce qu'il ne faut pas casser.

> Derniere mise a jour : **30 avril 2026** (Phase 7 LTL programmatique terminee + demo 5 scenarios + `LancerDemo`).

---

## 1) Le projet en une phrase

On modelise un sous-systeme critique inspire de la M14 : **deux trains automatiques veulent acceder a un canton de signalisation puis a un quai equipe de portes palieres (PSD), et on prouve formellement les invariants de surete** (exclusion mutuelle canton + exclusion mutuelle quai + portes palieres jamais ouvertes sans train a quai + train ne demarre jamais portes ouvertes).

Le projet combine :

- **Akka / Scala** : simulation distribuee par acteurs (5 acteurs : 2 `Train` + `SectionController` + `QuaiController` + `GestionnairePortes`) ;
- **Reseau de Petri** : modele formel etendu (12 places, 12 transitions, marquage initial unique) ;
- **Analyseur Scala maison** : exploration BFS des marquages, **40 arcs etiquetes**, 5 invariants verifies, 0 deadlock, 5 proprietes LTL (3 Safety + 2 Liveness) verifiees programmatiquement ;
- **Demo HTML animee** : 5 scenarios (cycle nominal, concurrence, cycle complet 2 trains, 2 violations PSD), pilotee par les memes traces que celles generees par le main Scala.

Invariants centraux (3 ressources + 2 surete PSD critiques) :

```text
Canton  : T1_sur_canton + T2_sur_canton + Canton_libre  = 1
Quai    : T1_a_quai     + T2_a_quai     + Quai_libre    = 1
Portes  : Portes_fermees + Portes_ouvertes               = 1
PSD-Open      : Portes_ouvertes=1 => un train est a quai (CRITIQUE)
PSD-Departure : Ti_depart_quai tirable => Portes_fermees=1 (CRITIQUE)
```

Proprietes LTL (toutes verifiees automatiquement par l'analyseur, cf section 4) :

```text
G !(T1_sur_canton AND T2_sur_canton)              -- exclusion canton
G !(T1_a_quai AND T2_a_quai)                      -- exclusion quai
G (Portes_ouvertes -> (T1_a_quai OR T2_a_quai))   -- PSD-Open
G (T1_attente -> F T1_sur_canton)  (sous fairness FIFO)
G (T2_attente -> F T2_sur_canton)  (sous fairness FIFO)
```

---

## 2) A lancer en premier

Depuis la racine du projet (`projet/CriticalSystemModel/`) :

```bash
sbt compile
sbt test
sbt "runMain m14.Main"
sbt "runMain m14.petri.Analyseur"
sbt "runMain m14.demo.LancerDemo"
```

Resultat attendu :

| Commande | Sortie attendue |
|---|---|
| `sbt compile` | Compile sans warning |
| `sbt test` | **49 tests verts** (TrainSpec 6 + SectionControllerSpec 3 + QuaiControllerSpec 5 + GestionnairePortesSpec 5 + AnalyseurSpec 30) |
| `sbt "runMain m14.Main"` | 3 scenarios console + bilan : 20 marquages, 5 invariants OK, 0 deadlock |
| `sbt "runMain m14.petri.Analyseur"` | 20 marquages, 40 arcs etiquetes, 5 invariants PASSE, 5 LTL PASSE |
| `sbt "runMain m14.demo.LancerDemo"` | Regenere les 5 traces JSON, demarre un serveur HTTP local et **ouvre automatiquement le navigateur** sur la demo animee |

Toutes les commandes utiles sont centralisees dans [`documentation/suivi/COMMANDES.md`](suivi/COMMANDES.md).

---

## 3) Structure du depot (etat 30/04/2026)

```text
CriticalSystemModel/
├── src/main/scala/m14/
│   ├── Main.scala                    <- demo console Akka + Petri (3 scenarios)
│   ├── troncon/                      <- coeur Akka (5 acteurs)
│   │   ├── Protocol.scala              messages 6+4
│   │   ├── Train.scala                 machine a etats 4 etats
│   │   ├── SectionController.scala     arbitre canton FIFO
│   │   ├── QuaiController.scala        arbitre quai FIFO
│   │   └── GestionnairePortes.scala    garde de surete PSD
│   ├── petri/                        <- analyse formelle
│   │   ├── PetriNet.scala              12 places, 12 transitions
│   │   └── Analyseur.scala             BFS + 5 invariants + 5 LTL + 40 arcs
│   └── demo/                         <- pont Scala -> demo HTML
│       ├── TraceWriter.scala           serialisation JSON manuelle
│       ├── GenererTraces.scala         5 scenarios canoniques -> JSON
│       └── LancerDemo.scala            serveur HTTP + auto-ouverture navigateur
│
├── src/test/scala/m14/                <- 49 tests ScalaTest / Akka TestKit
│   ├── troncon/                       (TrainSpec, SectionControllerSpec,
│   │                                   QuaiControllerSpec, GestionnairePortesSpec)
│   └── petri/AnalyseurSpec.scala      (30 tests : invariants + arcs + LTL)
│
├── petri/petri-troncon.md             <- source de verite du modele Petri
│
├── demo/                              <- demo HTML autonome
│   ├── index.html                       (5 scenarios selectables)
│   ├── trace-nominal.json
│   ├── trace-concurrence.json
│   ├── trace-cycle-deux-trains.json     (Phase 7)
│   ├── trace-violation.json
│   ├── trace-violation-depart.json      (Phase 7)
│   └── README.md
│
├── documentation/
│   ├── START-ICI.md                   <- cette page
│   ├── suivi/
│   │   ├── COMMANDES.md
│   │   ├── PLAN.md                      (Phase 7 TERMINEE)
│   │   ├── HANDOVER.md
│   │   └── historique.md
│   ├── gouvernance/
│   │   ├── REGLES_PROJET.md
│   │   ├── repartition-equipe.md
│   │   ├── protocole-coordination.md
│   │   └── lexique.md
│   ├── livrables/
│   │   ├── rapport.md
│   │   ├── biblio.md                    (11 sources)
│   │   ├── comparaison.md               (3 scenarios + sortie analyseur)
│   │   └── preuves-manuelles.md         (taches 1-7 remplies)
│   └── contexte/
│       └── recadrage-m14-troncon-critique.md
│
├── README.md
└── build.sbt
```

---

## 4) Etat actuel - Tableau de bord

| Element | Etat | Detail |
|---|---|---|
| Modele Petri etendu (canton + quai + PSD) | **FAIT** | 12 places, 12 transitions, 5 invariants prouves a la main et programmatiquement |
| Code Akka (5 acteurs) | **FAIT** | Train, SectionController, QuaiController, GestionnairePortes, Protocol |
| Tests unitaires | **FAIT** | 49/49 verts |
| Analyseur Scala | **FAIT** | 20 marquages atteignables, 40 arcs etiquetes, 5 invariants PASSE, 0 deadlock |
| LTL programmatique (Phase 7) | **FAIT** | `verifierGSafety` + `verifierGFLiveness`, 3 Safety + 2 Liveness PASSE |
| Carnet de preuves manuelles | **FAIT** | Taches 1-7 remplies avec valeurs reelles (20 marquages, 40 arcs) |
| Demo HTML animee | **FAIT** | 5 scenarios pilotes par traces JSON generees depuis le modele Scala |
| Pont Scala -> demo (`LancerDemo`) | **FAIT** | Serveur HTTP local + auto-ouverture navigateur |
| Rapport (sections 1-8 + annexes) | **EN COURS** | Sections 1-4, 6-8 remplies. Section 5 (proprietes) a finaliser depuis `preuves-manuelles.md` |
| Bibliographie | **FAIT** | 11 sources commentees |
| Comparaison Akka vs Petri | **FAIT** | 3 scenarios + sortie analyseur verbatim |
| Merge `extension` -> `main` | A FAIRE | D.7 du PLAN.md |
| Tag `v1.0-rendu` | A FAIRE | Phase 9 |

---

## 5) Qui fait quoi maintenant (sprint final)

### Piste A - Rapport et livrables

Objectif : finaliser les sections du rapport en reprenant le carnet de preuves.

Fichiers a editer :

- [`documentation/livrables/rapport.md`](livrables/rapport.md)
- [`documentation/livrables/preuves-manuelles.md`](livrables/preuves-manuelles.md) (source pour les tableaux)

Actions prioritaires :

1. Reprendre les tableaux des taches 2.1 a 4 du carnet et les copier dans la section 5 du rapport.
2. Inserer le bloc des 40 arcs (tache 7) en annexe A1.
3. Relecture croisee.

### Piste B - Soutenance et demo

Objectif : preparer un parcours de demonstration de 5-10 minutes.

Fichiers a maitriser :

- [`demo/index.html`](../demo/index.html) (5 scenarios)
- [`src/main/scala/m14/Main.scala`](../src/main/scala/m14/Main.scala)
- [`documentation/suivi/COMMANDES.md`](suivi/COMMANDES.md)

Sequence recommandee pour la demo :

1. `sbt test` : montrer les 49 tests verts.
2. `sbt "runMain m14.petri.Analyseur"` : montrer les 20 marquages, 40 arcs, 5 invariants + 5 LTL PASSE.
3. `sbt "runMain m14.demo.LancerDemo"` : ouvre la demo animee. Derouler les 5 scenarios :
   - **A** cycle nominal (1 train, 7 etapes)
   - **B** concurrence canton + quai (2 trains, 11 etapes)
   - **C** cycle complet 2 trains -> liveness (13 etapes)
   - **D** violation PSD-Open : overlay rouge sur tentative ouverture sans train (3 etapes)
   - **E** violation PSD-Departure : tentative depart portes ouvertes refusee, puis sequence corrigee (9 etapes)
4. `sbt "runMain m14.Main"` : montrer la coherence Akka + Petri en console.

### Piste C - Polissage final

Objectif : preparer le rendu (tag git, README, merge).

Actions :

1. Verifier README.md a jour.
2. Merge `extension` -> `main` avec `--no-ff` apres relecture.
3. Tag `v1.0-rendu`.
4. Push final.

---

## 6) Ce qu'il ne faut pas changer sans accord

Ces choix sont verrouilles pour eviter de casser les preuves (cf [`documentation/gouvernance/protocole-coordination.md`](gouvernance/protocole-coordination.md) section 2) :

- pas plus de **2 trains** ;
- pas plus de **1 canton + 1 quai** partages ;
- pas plus de **12 places, 12 transitions** dans le reseau de Petri ;
- pas plus de **6 messages vers controleurs + 4 messages vers trains** ;
- pas de pannes, timeouts ou Petri temporise dans le coeur du projet ;
- la **garde de surete du `GestionnairePortes`** (refus d'ouverture sans train a quai) ne doit jamais etre desactivee : c'est le coeur de la propriete PSD-Open.

La demo HTML peut etre amelioree visuellement, mais elle doit rester alignee sur le modele etendu actuel et utiliser exclusivement des traces generees par `GenererTraces` (source de verite : `PetriNet.tirer`).

---

## 7) Regles d'or

1. Avant de push : `sbt compile && sbt test`.
2. Pour une demo console : `sbt "runMain m14.Main"`, **pas** `sbt run` (plusieurs points d'entree).
3. Pour la demo animee : `sbt "runMain m14.demo.LancerDemo"` (regenere les traces + lance le serveur + ouvre le navigateur).
4. Toute modification importante doit etre tracee dans [`documentation/suivi/historique.md`](suivi/historique.md).
5. Ne pas commiter `target/`, `.bloop/`, `.metals/`, ni les fichiers temporaires.
6. Si le code et le modele Petri divergent, le fichier de reference est [`petri/petri-troncon.md`](../petri/petri-troncon.md).
7. Si l'analyseur et le carnet de preuves divergent : on debugge le code en priorite, le carnet est la reference humaine.

---

## 8) Besoin d'aide ?

- Commandes : [`documentation/suivi/COMMANDES.md`](suivi/COMMANDES.md)
- Planning : [`documentation/suivi/PLAN.md`](suivi/PLAN.md)
- Reprise technique : [`documentation/suivi/HANDOVER.md`](suivi/HANDOVER.md)
- Vocabulaire : [`documentation/gouvernance/lexique.md`](gouvernance/lexique.md)
- Regles de contribution : [`documentation/gouvernance/REGLES_PROJET.md`](gouvernance/REGLES_PROJET.md)
- Modele Petri : [`petri/petri-troncon.md`](../petri/petri-troncon.md)
- Carnet de preuves : [`documentation/livrables/preuves-manuelles.md`](livrables/preuves-manuelles.md)
