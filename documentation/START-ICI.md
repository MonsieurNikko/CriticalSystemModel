# START ICI - Guide rapide du projet

> Nouveau sur le repo ? Lis cette page avant de coder. Elle dit quoi lancer, quoi lire, quoi modifier, et ce qu'il ne faut pas casser.

> Derniere mise a jour : **1 mai 2026** (rapport finalise, README/docs alignes sur 49 tests / 20 marquages / 40 arcs, clarification PSD-Open Akka/Petri).

---

## 1) Le projet en une phrase

On modelise un sous-systeme critique inspire de la M14 : **deux trains automatiques veulent acceder a un canton de signalisation puis a un quai equipe de portes palieres (PSD), et on prouve formellement les invariants de surete** (exclusion mutuelle canton + exclusion mutuelle quai + portes palieres jamais ouvertes sans train a quai + train ne demarre jamais portes ouvertes).

### 1.1 Histoire courte a raconter au rendu

Le projet s'est fait en **deux etapes**, ce qui explique pourquoi certains anciens fichiers parlent encore de "troncon" :

| Etape | Ce qu'on a fait | Pourquoi |
|---|---|---|
| **1 - Socle initial** | 2 trains + 1 troncon partage, sans quai ni portes. Modele Akka minimal, reseau Petri a 7 places / 6 transitions, 8 marquages. | Valider d'abord la concurrence simple : un seul train dans la zone critique, pas de deadlock, progression apres liberation. |
| **2 - Upgrade final** | Le troncon est precise en **canton**, puis on ajoute le **quai** et les **portes palieres PSD**. Modele final : 12 places / 12 transitions, 20 marquages, 5 invariants. | Rendre le sujet plus proche de la M14 et ajouter deux vraies proprietes critiques : PSD-Open et PSD-Departure. |

Donc au rendu, la lecture attendue est : **on a d'abord prouve un coeur simple 2 trains / 1 troncon, puis on l'a etendu proprement sans abandonner la preuve initiale**.

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

## 3) Structure du depot (etat final apres upgrade PSD)

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
| Rapport (sections 1-8 + annexes) | **FAIT** | Section 5 remplie depuis `preuves-manuelles.md`, annexes A1-A4 alignees sur les 40 arcs et les noeuds PSD |
| Bibliographie | **FAIT** | 11 sources commentees |
| Comparaison Akka vs Petri | **FAIT** | 3 scenarios + sortie analyseur verbatim |
| Merge `extension` -> `main` | A FAIRE | D.7 du PLAN.md |
| Tag `v1.0-rendu` | A FAIRE | Phase 9 |

---

## 5) Qui fait quoi maintenant (sprint final)

### Piste A - Rapport et livrables

Objectif : relire le rapport finalise en reprenant le carnet de preuves.

Fichiers a editer :

- [`documentation/livrables/rapport.md`](livrables/rapport.md)
- [`documentation/livrables/preuves-manuelles.md`](livrables/preuves-manuelles.md) (source pour les tableaux)

Actions restantes :

1. Relire les tableaux de la section 5 du rapport contre `preuves-manuelles.md`.
2. Verifier que l'annexe A1 contient les 40 arcs dans le meme ordre que l'analyseur.
3. Faire une relecture croisee equipe avant merge.

> **Mode d'emploi detaille pour la redaction : voir section 9 plus bas.**

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
   - **D** violation PSD-Open : overlay rouge sur tentative d'ouverture sans `Ti_a_quai` (transition Petri non tirable, message Akka hors protocole)
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
- la **sequence de surete PSD-Open** ne doit jamais etre affaiblie : le `Train` ne doit envoyer `OuverturePortes` qu'en etat `a_quai`, et le `GestionnairePortes` doit refuser les demandes concurrentes d'un autre train quand les portes sont deja ouvertes.

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

---

## 9) Guide pratique - Rediger sa partie du rapport

**Cible** : un membre de l'equipe qui doit completer une section de [`documentation/livrables/rapport.md`](livrables/rapport.md) et ne sait pas par ou commencer.

### 9.1 Principe directeur

Le rapport est un **assemblage** de contenus deja produits. On ne reinvente rien. Pour chaque section a remplir, il existe une **source de verite** (carnet de preuves, modele Petri, comparaison, sortie analyseur). La regle : **citer + reformuler court**, ne jamais paraphraser sans relire la source.

### 9.2 Tableau "section du rapport -> source -> action"

| Section rapport | Source de verite a copier | Action exacte |
|---|---|---|
| 1) Contexte et motivation | [`contexte/recadrage-m14-troncon-critique.md`](contexte/recadrage-m14-troncon-critique.md) + [`gouvernance/REGLES_PROJET.md`](gouvernance/REGLES_PROJET.md) | Reformuler le perimetre en 1/2 page (2 trains, 1 canton, 1 quai, PSD). Citer la M14. |
| 2) Bibliographie | [`livrables/biblio.md`](livrables/biblio.md) | Recopier les 11 sources commentees telles quelles. Aucune a chercher. |
| 3) Architecture Akka | Code `src/main/scala/m14/troncon/` + [`gouvernance/lexique.md`](gouvernance/lexique.md) | Donner schema 5 acteurs + 6+4 messages. Source : commentaires en tete de chaque fichier `.scala` du dossier `troncon/`. |
| 4) Modele Petri formel | [`petri/petri-troncon.md`](../petri/petri-troncon.md) sections 2-5 | Recopier la liste des 12 places, 12 transitions, M0. **Verifier les chiffres (12P/12T/20 marquages/40 arcs).** |
| **5) Verification des proprietes** **(section principale)** | [`livrables/preuves-manuelles.md`](livrables/preuves-manuelles.md) taches 2.1 a 4 | Une sous-section par invariant (5.1 a 5.5). Pour chacun : enonce + preuve manuelle (source carnet) + sortie analyseur (recopier verbatim). |
| 6) Comparaison Akka vs Petri | [`livrables/comparaison.md`](livrables/comparaison.md) | Recopier les 3 tableaux de scenarios + la sortie analyseur verbatim. Deja fait, juste a relire. |
| 7) Limites assumees | [`gouvernance/protocole-coordination.md`](gouvernance/protocole-coordination.md) section 2 (perimetre) | Lister explicitement : pas de pannes, pas de timing, 2 trains max, fairness FIFO supposee. |
| 8) Conclusion | Tableau "Etat actuel" section 4 ci-dessus | 1/2 page : on a montre quoi (5 invariants, 5 LTL, 0 deadlock), pour combien d'effort, et 3 extensions possibles. |
| Annexe A1 (40 arcs) | [`livrables/preuves-manuelles.md`](livrables/preuves-manuelles.md) tache 7 | Copier-coller le bloc des 40 arcs (sortie `runMain m14.petri.Analyseur`). |
| Annexe A2 (sortie analyseur complete) | Lancer `sbt "runMain m14.petri.Analyseur" > /tmp/sortie.txt` | Coller la sortie integrale (20 marquages + invariants + LTL). |

### 9.3 Methode pas-a-pas pour une section

Exemple : tu dois rediger la section **5.5 Surete PSD**.

1. **Ouvrir la source** : [`livrables/preuves-manuelles.md`](livrables/preuves-manuelles.md), chercher "tache 2bis.1" et "tache 2bis.2".
2. **Lire l'enonce** dans [`petri/petri-troncon.md`](../petri/petri-troncon.md) section 5.5 (PSD-Open et PSD-Departure).
3. **Lancer la verification** :
   ```bash
   sbt "runMain m14.petri.Analyseur" 2>&1 | grep -A 1 "PSD"
   ```
   Recopier la sortie dans le rapport.
4. **Rediger** dans cet ordre fixe :
   - Enonce formel (LTL ou predicat)
   - Preuve structurelle (2-3 lignes : arc Petri qui empeche la violation)
   - Preuve programmatique (sortie analyseur)
   - Consequence M14 (1 phrase metier)
5. **Verifier la coherence** : si tu ecris "20 marquages", verifier que la sortie analyseur dit aussi 20. Si divergence : tu as un bug, **corriger le code en priorite** (regle d'or 7).
6. **Tracer** : ajouter une ligne dans [`suivi/historique.md`](suivi/historique.md) avec ton GitHub, la branche, les fichiers touches.

### 9.4 Erreurs frequentes a eviter

- **Inventer des chiffres**. Toujours relancer l'analyseur si tu n'es pas sur. Les chiffres officiels du sprint final sont : **12 places, 12 transitions, 20 marquages, 40 arcs, 5 invariants, 5 LTL, 49 tests, 0 deadlock**.
- **Reformuler la preuve sans la relire**. Le carnet de preuves est tres precis ; le paraphraser introduit des erreurs.
- **Oublier de citer la source M14**. Toute affirmation reglementaire doit pointer vers [`livrables/biblio.md`](livrables/biblio.md).
- **Modifier le code juste pour la redaction**. Si tu trouves une faute dans le code, ouvrir une issue ou prevenir l'equipe ; ne pas faire un commit "drive-by" non teste.
- **Pousser sans tests**. Avant tout `git push` : `sbt compile && sbt test` (regle d'or 1).

### 9.5 Definition of Done d'une section de rapport

Une section est consideree finie quand :

- [ ] tous les chiffres correspondent a la sortie de `sbt "runMain m14.petri.Analyseur"` du jour ;
- [ ] toutes les references vers d'autres documents sont cliquables (chemins relatifs valides) ;
- [ ] aucune phrase ne commence par "TODO", "a completer", "voir plus tard" ;
- [ ] la section a ete relue par un binome (cf [`gouvernance/repartition-equipe.md`](gouvernance/repartition-equipe.md)) ;
- [ ] une entree datee a ete ajoutee dans [`suivi/historique.md`](suivi/historique.md).

---

## 10) Parcours de lecture - Comment naviguer dans la doc

**Cible** : tu rejoins le projet aujourd'hui, ou tu reprends le travail apres une pause. Voici l'ordre conseille selon ton role.

### 10.1 Profil "Je decouvre" (30 min)

1. [`documentation/START-ICI.md`](START-ICI.md) **(cette page)** - vue d'ensemble.
2. [`documentation/contexte/recadrage-m14-troncon-critique.md`](contexte/recadrage-m14-troncon-critique.md) - le pourquoi M14, le perimetre.
3. [`petri/petri-troncon.md`](../petri/petri-troncon.md) sections 1-3 - le modele formel en gros.
4. [`demo/README.md`](../demo/README.md) puis lancer `sbt "runMain m14.demo.LancerDemo"` - voir le systeme tourner.
5. [`documentation/gouvernance/lexique.md`](gouvernance/lexique.md) - vocabulaire commun (canton, quai, PSD, marquage, transition...).

### 10.2 Profil "Je vais coder" (45 min de plus)

6. [`documentation/gouvernance/REGLES_PROJET.md`](gouvernance/REGLES_PROJET.md) - conventions de code, branches, PR.
7. Lire les 5 fichiers de `src/main/scala/m14/troncon/` (commentaires d'entete + signatures).
8. Lire `src/main/scala/m14/petri/PetriNet.scala` (12 places, 12 transitions, marquage initial).
9. Lire `src/main/scala/m14/petri/Analyseur.scala` (BFS + invariants + LTL).
10. Lancer `sbt test` et **lire** les noms de tests (49 tests = 49 specifications executables).

### 10.3 Profil "Je vais rediger le rapport" (1h)

6. [`documentation/livrables/rapport.md`](livrables/rapport.md) en entier - ce qui existe deja.
7. [`documentation/livrables/preuves-manuelles.md`](livrables/preuves-manuelles.md) - **la mine d'or**, lire les taches 1 a 7.
8. [`documentation/livrables/comparaison.md`](livrables/comparaison.md) - les 3 scenarios canoniques + sortie analyseur.
9. [`documentation/livrables/biblio.md`](livrables/biblio.md) - les 11 sources, savoir laquelle citer pour quel argument.
10. Section **9** ci-dessus - le tableau "section -> source -> action".

### 10.4 Profil "Je presente la soutenance" (30 min)

6. [`documentation/suivi/COMMANDES.md`](suivi/COMMANDES.md) section 4 - lancement de la demo.
7. Lancer `sbt "runMain m14.demo.LancerDemo"` et derouler les 5 scenarios A/B/C/D/E.
8. Repeter le storytelling : section 5.B de cette page (Piste B - Soutenance).
9. Preparer 3 questions defensives :
   - "Pourquoi pas TLA+ / Spin / mCRL2 ?" -> [`livrables/biblio.md`](livrables/biblio.md) + [`gouvernance/protocole-coordination.md`](gouvernance/protocole-coordination.md) Q3.
   - "Comment savez-vous que vos 5 invariants suffisent ?" -> [`livrables/preuves-manuelles.md`](livrables/preuves-manuelles.md) tache 6 (LTL).
   - "Que se passe-t-il avec 3 trains ?" -> section 6 ci-dessus + section 7 du rapport (limites assumees).

### 10.5 Profil "Je reprends apres une pause" (15 min)

1. [`documentation/suivi/historique.md`](suivi/historique.md) - les 3 dernieres entrees.
2. [`documentation/suivi/HANDOVER.md`](suivi/HANDOVER.md) - etat de transmission entre membres.
3. [`documentation/suivi/PLAN.md`](suivi/PLAN.md) - phase courante et prochaines etapes.
4. `git log --oneline -10` - les derniers commits.
5. `sbt compile && sbt test` - verifier que rien n'est casse.

### 10.6 Carte de la documentation (qui sert a quoi)

| Fichier | Role | Tu le lis quand... |
|---|---|---|
| [`START-ICI.md`](START-ICI.md) | Page d'accueil, point d'entree unique | toujours en premier |
| [`contexte/recadrage-m14-troncon-critique.md`](contexte/recadrage-m14-troncon-critique.md) | Justification du perimetre | tu doutes du scope |
| [`gouvernance/REGLES_PROJET.md`](gouvernance/REGLES_PROJET.md) | Conventions code/git/PR | avant ton premier commit |
| [`gouvernance/repartition-equipe.md`](gouvernance/repartition-equipe.md) | Qui fait quoi (poles A/B/C/D) | tu cherches ton binome de relecture |
| [`gouvernance/protocole-coordination.md`](gouvernance/protocole-coordination.md) | FAQ Q1-Q15 sur les decisions de design | tu veux justifier un choix |
| [`gouvernance/lexique.md`](gouvernance/lexique.md) | Vocabulaire Akka/Petri/M14 | un terme te semble flou |
| [`livrables/rapport.md`](livrables/rapport.md) | Le rapport final a rendre | tu rediges |
| [`livrables/preuves-manuelles.md`](livrables/preuves-manuelles.md) | Carnet detaille des preuves (taches 1-7) | tu redige la section 5 du rapport |
| [`livrables/comparaison.md`](livrables/comparaison.md) | Tableaux Akka vs Petri par scenario | tu rediges la section 6 |
| [`livrables/biblio.md`](livrables/biblio.md) | 11 sources commentees | tu cites une reference |
| [`suivi/PLAN.md`](suivi/PLAN.md) | Phases et avancement global | tu planifies |
| [`suivi/COMMANDES.md`](suivi/COMMANDES.md) | Toutes les commandes (sbt, git, demo) | tu cherches "la commande pour..." |
| [`suivi/historique.md`](suivi/historique.md) | Journal chronologique inverse | tu reprends ou tu finis ta journee |
| [`suivi/HANDOVER.md`](suivi/HANDOVER.md) | Etat de transmission entre membres | passage de relais |
| [`petri/petri-troncon.md`](../petri/petri-troncon.md) | Modele Petri formel (source de verite) | tu doutes d'un chiffre, d'une transition |
| [`demo/README.md`](../demo/README.md) | Mode d'emploi de la demo HTML | tu prepares la soutenance |

### 10.7 Regle de priorite en cas de conflit entre documents

1. Le **code Scala** est l'autorite finale (`PetriNet.scala`, `Analyseur.scala`, tests).
2. [`petri/petri-troncon.md`](../petri/petri-troncon.md) est la **specification** ; si elle diverge du code, on aligne le code OU le doc selon discussion d'equipe.
3. [`livrables/preuves-manuelles.md`](livrables/preuves-manuelles.md) doit refleter le code. Si divergence : on relance l'analyseur, on met a jour le carnet.
4. [`livrables/rapport.md`](livrables/rapport.md) est en bout de chaine : il copie depuis le carnet. Jamais l'inverse.
5. [`START-ICI.md`](START-ICI.md) (cette page) est un resume ; en cas de doute, la **source detaillee** prime.
