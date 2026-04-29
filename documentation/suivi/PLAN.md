# Plan d'execution - Sprint final M14 (25 avril -> 4 mai 2026)

> Document de pilotage du projet. A lire en priorite avant toute contribution sur la periode.
> Source de verite pour le sequencement par phase, le scope figé, et les criteres de rendu.

---

## 1) Cadre

- **Deadline reelle** : 4 mai 2026 (le badge README "fin mai" est obsolete, sera corrige en Phase 9)
- **Duree restante** : 9 jours (25 avril -> 3 mai au soir, rendu le 4 mai)
- **Equipe** : 4 contributeurs (voir `documentation/gouvernance/repartition-equipe.md` pour les specialites)
- **Mode de travail** : code lisible et defendable - chaque membre doit pouvoir expliquer le code qu'il signe

---

## 2) Cadrage du sujet (rappel court)

Sous-systeme critique M14 : controle d'acces concurrent de **2 trains automatiques** a **1 canton de signalisation** suivi de **1 quai equipe de portes palieres (PSD)**, via **3 controleurs centralises**.

Invariants principaux a prouver :

```
canton : T1_sur_canton + T2_sur_canton + Canton_libre = 1
quai   : T1_a_quai     + T2_a_quai     + Quai_libre   = 1
portes : Portes_fermees + Portes_ouvertes              = 1
```

Invariants critiques de surete PSD :

```
PSD-Open      : Portes_ouvertes=1 => un train a quai (CRITIQUE)
PSD-Departure : Ti_depart_quai tirable => Portes_fermees=1 (CRITIQUE)
```

Ce qui est dans le coeur (apres extension PSD du 29/04) :
- 2 acteurs Train (4 etats chacun) + 1 SectionController + 1 QuaiController + 1 GestionnairePortes
- Protocole : 6 messages vers controleurs + 4 messages vers trains
- Reseau de Petri : 12 places, 12 transitions effectives
- Analyseur Scala : generation espace d'etats + verification 5 invariants + absence de deadlock
- Proprietes LTL : exclusion mutuelle (canton + quai), surete PSD (Open + Departure), Liveness sous fairness

Hors coeur (mentionnes "extensions futures" dans le rapport, pas implementes) :
- Plus de 2 trains, 2e canton, 2 quais opposes
- Tolerance aux pannes complete
- Outil graphique de Petri
- Petri temporise (duree d'arret a quai)
- Synchronisation portes train + portes palieres

---

## 3) Livrables exiges par le cahier des charges (projet_2026.pdf)

| # | Livrable | Ou | Etat cible | Etat actuel |
|---|----------|-----|-----------|-------------|
| L1 | Bibliographie commentee (etat de l'art) | `documentation/livrables/biblio.md` | 5-8 sources, 2-4 lignes par source | **11 sources commentees** (9 initiales + 2 PSD : IEEE 1474 CBTC, UITP/PSD) |
| L2 | Modele Akka/Scala fonctionnel | `src/main/scala/m14/troncon/` | Compile + 3 scenarios passent | **Modele initial complet** (Train + SectionController + tests + demo). **Extension PSD : code Phase B en attente** (5 acteurs cibles). |
| L3 | Reseau de Petri | `petri/petri-troncon.md` (texte + ASCII) | 12 places, 12 transitions, marquage initial | **Complet (etendu PSD)** : 12 places, 12 transitions, ASCII, 5 invariants prouves a la main |
| L4 | Rapport de verification (proprietes structurelles + invariants + LTL) | `documentation/livrables/rapport.md` | 5 invariants prouves (canton, quai, portes, PSD-Open, PSD-Departure), deadlocks ecartes, LTL formalise | **Squelette etendu PSD** (sections 1-4 et 7-8 a remplir Phase D) |
| L5 | Analyseur Petri en code | `src/main/scala/m14/petri/` | Espace d'etats genere, 5 invariants verifies par programme | **Modele initial complet** : BFS, 8 marquages, invariants, deadlocks. **Extension PSD : Phase B.7** (cible 15-18 marquages, +invariantQuai/Portes, +verifierSurteOuverturePortes/DepartQuai). |
| L6 | Simulation comparee Akka vs Petri (3 scenarios) | `documentation/livrables/comparaison.md` | Tableau de correspondance messages <-> transitions | **Etendu PSD** : 3 scenarios reecrits (cycle nominal complet, concurrence canton+quai, surete PSD invalide). Sortie analyseur a inserer en Phase D. |
| L7 | Lien GitHub final | README.md | Depot propre, branche main a jour | A finaliser apres commit/push |
| Bonus | Demo visuelle locale | `demo/index.html` | Aide de soutenance, pas preuve principale | **Prototype fait** : pas-a-pas Akka/Petri |

**Documents pivots d'equipe** (a lire avant toute contribution) :
- `documentation/gouvernance/lexique.md` : coherence de nommage metier <-> code Akka <-> Petri <-> tests
- `documentation/gouvernance/protocole-coordination.md` : contrat de travail parallele code/preuves, decisions verrouillees, procedures de changement, questions anticipees Phase 2-3
- `documentation/livrables/preuves-manuelles.md` : carnet de travail manuel pour la piste verification formelle (independante du code)

**Contrainte cle du cahier** : interdiction d'utiliser un outil logiciel de Petri externe. L'analyseur **doit** etre code par nous.

---

## 4) Calendrier par phase

---

### Phase 1 : Plan + squelette (TERMINEE)

- [x] Lire cahier des charges + recadrage
- [x] Valider ce PLAN.md
- [x] Creer la branche `feature/m14-coeur-troncon`
- [x] Squelette des 3 acteurs (`Protocol.scala`, `Train.scala`, `SectionController.scala`) — signatures de messages, pas de logique
- [x] `sbt compile` passe
- [x] Branche `feature/m14-coeur-troncon` mergee sur `main` (voir historique.md)
- [x] Bibliographie : structure creee dans `documentation/livrables/biblio.md`
- **Verification** : compile OK, squelettes presents sur main, structure documentaire en place

---

### Phase 2 : Protocole + arbitrage (TERMINEE)

- [x] Definir le **protocole de messages** dans `Protocol.scala` : `Demande`, `Autorisation`, `Attente`, `Sortie`
- [x] Implementer la **logique d'arbitrage** de `SectionController` : etat libre / occupe + file d'attente FIFO
- [x] Test scenario nominal (1 train demande, obtient, sort, libere)
- **Verification** : `sbt compile` et `sbt test` passent

---

### Phase 3 : Train + scenarios concurrence (TERMINEE)

- [x] Implementer `Train.scala` (etats : hors / attente / sur_troncon)
- [x] Test scenario **concurrence** : 2 trains demandent quasi-simultanement, un seul entre, l'autre attend
- [x] Test scenario **liberation/progression** : le train sort, le second progresse
- [x] Premiere version de `documentation/livrables/biblio.md`
- **Verification** : `sbt test` vert (19 tests)

---

### Phase 4 : Reseau de Petri (papier) (TERMINEE)

- [x] Dessin ASCII du reseau (7 places, 6 transitions) dans `petri/petri-troncon.md`
- [x] Definition formelle du marquage initial
- [x] Verification **a la main** de l'invariant principal sur les transitions
- [x] Tableau de **correspondance message Akka <-> transition Petri** dans `documentation/livrables/comparaison.md`
- **Verification** : reseau coherent avec architecture Akka

---

### Phase 5 : Analyseur Petri (1) — structure de donnees (TERMINEE)

- [x] Creer `src/main/scala/m14/petri/PetriNet.scala` : types `Place`, `Transition`, `Marking`, `Net`
- [x] Encoder le reseau en dur dans le code
- [x] Fonction `tirer(transition, marking)` qui calcule le marquage suivant
- [x] Tests unitaires sur les transitions principales
- [x] Documenter l'usage via `documentation/suivi/COMMANDES.md`
- **Verification** : tests `AnalyseurSpec` verts

---

### Phase 6 : Analyseur Petri (2) — espace d'etats (TERMINEE sur modele initial)

- [x] Generation de l'espace d'etats accessible (BFS depuis marquage initial)
- [x] Verification de l'invariant principal sur tous les marquages atteignables
- [x] Detection de **deadlock** (etat sans transition tirable)
- [x] Refonte de `Main.scala` : 3 scenarios de demo console, suppression de `StationControl.scala`
- [x] Demo HTML prototype : `demo/index.html`
- [x] Memo commandes : `documentation/suivi/COMMANDES.md`
- [ ] Test d'integration end-to-end Akka/Petri (a faire en Phase Extension — sous-phase D)
- **Verification (modele initial)** : 8 etats atteignables, aucun deadlock, 1 invariant tient partout
- **Resultat apres Phase Extension (modele etendu)** : **20 etats**, 5 invariants PASSE, 0 deadlock

---

### Phase Extension PSD : canton + quai + portes palieres (EN COURS)

Extension decidee pour rendre le projet M14-realiste sans exploser la combinatoire. Justification complete dans `documentation/contexte/recadrage-m14-troncon-critique.md` section 13.

Branche de travail : `extension` — a merger sur `main` avec `--no-ff` en fin de sous-phase D.

#### Sous-phase A : Documentation — source de verite avant le code (TERMINEE)

- [x] A.1 — Reecrire `petri/petri-troncon.md` : 12 places, 12 transitions, 3 invariants ressource + 2 invariants PSD, read-arc emule sur `Portes_fermees`
- [x] A.2 — Etendre `documentation/gouvernance/lexique.md` (4 etats par train, `QuaiController`, `GestionnairePortes`) et `protocole-coordination.md` (Q11-Q15, 6+4 messages)
- [x] A.3 — Etendre `documentation/livrables/preuves-manuelles.md` : carnet etendu, tache 2bis = invariants PSD avec triple preuve
- [x] A.4 — Reecrire `documentation/livrables/comparaison.md` : 3 scenarios (cycle nominal complet, concurrence canton+quai, surete PSD invalide)
- [x] A.5 — Etendre `documentation/livrables/rapport.md` : architecture 5 acteurs, modele 12P/12T, section 5.5 triple preuve PSD
- [x] A.6 — Mettre a jour `README.md`, `START-ICI.md`, `PLAN.md`, `recadrage.md`, `historique.md`
- [x] A.7 — Etendre `documentation/livrables/biblio.md` : +IEEE 1474 CBTC, +UITP/PSD (11 sources au total)
- **Verification** : coherence documentaire validee. Aucun fichier Scala modifie. Tests 22/22 toujours verts.

#### Sous-phase B : Code Scala — 8 fichiers (TERMINEE)

Ordre d'implementation impose : Protocol -> QuaiController -> Train -> GestionnairePortes -> SectionController -> PetriNet -> Analyseur -> Main.

- [x] B.1 — Etendre `Protocol.scala` : +6 types de messages (`ArriveeQuai`, `DepartQuai`, `OuverturePortes`, `FermeturePortes`, `PortesOuvertes`, `PortesFermees`) + types `ActorRef` des nouveaux controleurs
- [x] B.2 — Creer `QuaiController.scala` : clone structurel de `SectionController`, etats `quaiLibre` / `quaiOccupe(occupant, file)`, arbitrage FIFO
- [x] B.3 — Etendre `Train.scala` : 4 etats `comportementHors`, `comportementEnAttente`, `comportementSurCanton`, `comportementAQuai`
- [x] B.4 — Creer `GestionnairePortes.scala` : etats `portesFermees` / `portesOuvertes(occupant)`, **garde de surete CRITIQUE** (refus silencieux OuverturePortes/FermeturePortes d'un train non-occupant)
- [x] B.5 — Adapter `SectionController.scala` : commentaires alignes (canton)
- [x] B.6 — Etendre `PetriNet.scala` : 12 places, 12 transitions, M0 = 5 jetons
- [x] B.7 — Etendre `Analyseur.scala` : 5 invariants (canton, quai, portes, PSD-Open CRITIQUE, PSD-Departure CRITIQUE) + invariants par train + detection deadlock
- [x] B.8 — Mettre a jour `Main.scala` : 3 scenarios (cycle complet, concurrence canton+quai, tentative PSD invalide bloquee)
- **Verification** : `sbt clean compile` PASS. `sbt compile` PASS. Aucune regression.

#### Sous-phase C : Tests — 5 fichiers (TERMINEE)

- [x] C.1 — Adapter `TrainSpec.scala` : 6 tests (4 etats + ack portes + cycle complet, nouveau constructeur a 4 args)
- [x] C.2 — `SectionControllerSpec.scala` : 3 tests inchanges, toujours verts
- [x] C.3 — Creer `QuaiControllerSpec.scala` : 5 tests (autorisation, attente, promotion, garde occupant, reutilisation)
- [x] C.4 — Creer `GestionnairePortesSpec.scala` : 5 tests dont 2 CRITIQUES verifient le refus silencieux PSD-Open (OuverturePortes et FermeturePortes d'un train non-occupant)
- [x] C.5 — Adapter `AnalyseurSpec.scala` : 20 tests (12P/12T, exactement 20 marquages, 5 invariants PASSE, 0 deadlock, exclusions mutuelles canton+quai, instantiation explicite PSD-Open)
- **Verification** : `sbt test` : **39/39 verts** (vs 22 avant extension)

#### Sous-phase D : Execution + finalisation (EN COURS)

- [x] D.1 — `sbt clean compile` PASS
- [x] D.2 — `sbt test` : **39 tests verts**
- [x] D.3 — `sbt "runMain m14.petri.Analyseur"` : **20 marquages** atteignables (M0..M19), 5 invariants PASS, 0 deadlock
- [x] D.4 — Sections narratives du `rapport.md` (1, 2, 3, 4, 6, 7, 8) remplies avec la sortie reelle de l'analyseur (29/04/2026)
- [x] D.5 — Section 6 de `comparaison.md` remplie : sortie console reproduite verbatim + tableau de correspondance scenarios <-> marquages M0..M19 (29/04/2026)
- [x] D.6 — Commit atomique Phase B+C + entree `historique.md`
- [ ] D.7 — Merge `extension` -> `main` avec `--no-ff` (apres relecture equipe)
- [x] D.8 — Push branche `extension` : 34dc087
- **Verification globale** : `sbt test` vert (39/39) + 5 invariants PASS + rapport.md et comparaison.md finalises avec sortie reelle. Reste D.7 (merge) et Phase 7 (LTL programmatique).

---

### Phase 7 : LTL + verification formelle (A FAIRE — apres Phase Extension D)

- [x] Formalisation LTL ecrite dans `preuves-manuelles.md` tache 5 et `rapport.md` section 5.8 :
  - Safety canton : `G !(T1_sur_canton AND T2_sur_canton)`
  - Safety quai : `G !(T1_a_quai AND T2_a_quai)`
  - Safety PSD-Open : `G ( Portes_ouvertes -> (T1_a_quai OR T2_a_quai) )`
  - Safety PSD-Departure : `G ( (Ti_a_quai AND X(Ti_hors)) -> Portes_fermees )`
  - Liveness canton : `G (Ti_attente -> F Ti_sur_canton)` (sous fairness FIFO)
  - Liveness PSD : `G (Ti_a_quai -> F Portes_ouvertes)`
- [ ] Verification programmatique LTL Safety sur graphe etendu (`verifierGSafety` dans `Analyseur.scala`)
- [ ] Verification programmatique LTL Liveness sur graphe etendu (`verifierGFLiveness`)
- [ ] Graphe d'accessibilite avec arcs etiquetes (`M_i --transition--> M_j`) — sortie inserable en annexe A1 du rapport
- **Verification** : Safety renvoie `true` sur 15-18 marquages. Liveness renvoie `true` sous hypothese FIFO documentee.

---

### Phase 8 : Bibliographie + rapport final (EN COURS)

- [x] `documentation/livrables/biblio.md` : **11 references commentees** (Murata 1989, Akka, LTL TUM, Magee/Kramer, Lamport, RATP, Hewitt, Baier/Katoen, Manna/Pnueli, IEEE 1474 CBTC, UITP/PSD)
- [ ] `documentation/livrables/rapport.md` complet — sections narratives a remplir (Phase Extension D.4) :
  - Section 1 : Contexte et motivation (M14, PSD, sous-systeme retenu)
  - Section 3 : Architecture Akka (5 acteurs, protocole 6+4 messages)
  - Section 4 : Modele Petri formel (12 places, 12 transitions, read-arc emule)
  - Section 6 : Comparaison Akka vs Petri (reprendre comparaison.md)
  - Section 7 : Limites assumees
  - Section 8 : Conclusion + extensions futures
  - Annexes A1-A4 : sortie analyseur, matrice, extraits code, graphe accessibilite
- [ ] Relecture croisee en equipe
- **Verification** : rapport complet, longueur cible 12-18 pages a l'export PDF

---

### Phase 9 : Polish, README, fusion main (A FAIRE)

- [ ] Section "Comment lancer" du README claire et testee : `sbt compile`, `sbt test`, `sbt "runMain m14.Main"`, `sbt "runMain m14.petri.Analyseur"`
- [ ] Aligner la demo `demo/index.html` sur le modele etendu (5 acteurs, nouveaux scenarios PSD)
- [ ] Verifier `documentation/suivi/historique.md` a jour avec toutes les phases
- [ ] Checklist finale livrables L1-L7 cochee une derniere fois
- [ ] Commit/push de la branche finale apres validation collective
- [ ] Tag git : `v1.0-rendu`
- **Verification** : checkout propre depuis main + `sbt compile` + `sbt test` verts

---

### Phase 10 : RENDU

- [ ] Soumettre le lien GitHub
- Aucun nouveau commit ce jour-la sauf urgence critique

---

## 5) Definition of Done par livrable

Un livrable est "fait" si et seulement si :

- [ ] Le code compile sans erreur (`sbt compile`)
- [ ] Les tests existants passent (`sbt test`)
- [ ] Le fichier est dans le repo, sur la branche de travail, pousse
- [ ] `documentation/suivi/historique.md` contient une entree decrivant le changement
- [ ] Relecture par au moins 1 autre membre de l'equipe

---

## 6) Risques et parades

| Risque | Probabilite | Parade |
|--------|-------------|--------|
| L'analyseur Petri prend plus de temps que prevu | Moyenne | Garder le reseau borne (12 places, 12 transitions verrouilles cf protocole-coordination.md section 2). Pas de generalisation N trains. |
| Explosion combinatoire en passant de 8 a 15-18 marquages | Faible | Cible analytique deja calculee a la main dans `preuves-manuelles.md` tache 1. Si depassement -> revoir read-arc emule sur Portes_fermees. |
| Bug de concurrence Akka non reproductible | Moyenne | Utiliser Akka TestKit avec probes deterministes. Pas de Thread.sleep. |
| Conflit git lors du merge feature -> main | Faible | Branche actuelle `feature/m14-extension-quai-psd` isolee. Merge `--no-ff` uniquement Phase 7-bis D.7 apres tests verts. |
| Desync code/doc pendant Phase 7-bis | Forte (assumee) | Phase A (doc) finie avant Phase B (code). La doc est source de verite, le code doit s'y conformer. |
| Disponibilite equipe variable | Moyenne | Phases independantes des que possible, relecture asynchrone par DM/commit. |

---

## 7) Hors scope explicite (pour eviter la derive)

Ne **pas** faire pendant ce sprint :
- Generaliser l'analyseur a N trains
- Implementer un parseur de format Petri (.pnml ou autre)
- Transformer la demo HTML en vraie application hors scope
- Tolerance aux pannes Akka avancee (supervision strategy au-dela du defaut)
- LTL model checking complet (on formalise + on argumente, on ne code pas un model checker)

Si une de ces idees revient, repondre : "extension hors sprint, voir section Limites du rapport".

---

## 8) Doctrine de fin de sprint - Profondeur > complexite

Principe : ne pas complexifier le systeme, mais renforcer la profondeur de la verification.

Le cahier des charges le dit explicitement deux fois (§1.4 Risque 1 et §10 Recommandation strategique) :

> "Il vaut mieux avoir un modele simple, propre et verifiable qu'un systeme tres realiste mais impossible a analyser. Le projet sera mieux evalue si le raisonnement est rigoureux, meme avec un systeme volontairement simplifie."

Consequence : le scope (2 trains, 1 canton, 1 quai, 1 paire de PSD, 15-18 marquages cibles, 6+4 messages, 12 places, 12 transitions) reste fige apres l'extension PSD du 29/04 (cf `documentation/gouvernance/protocole-coordination.md` section 2). A la place d'etendre encore le systeme, on etend la **verification** sur 4 axes a integrer dans les Phases 7-9 sans creer de nouvelle phase.

> **Note 29/04** : cette section a ete redigee avant l'extension PSD. Les sous-sections 8.2 a 8.5 conservent leur logique mais le vocabulaire (`T1_sur_troncon`, 8 marquages, 7P/6T) doit etre lu dans le contexte du modele initial. La logique reste valable pour le modele etendu (5 invariants au lieu de 1, 15-18 marquages au lieu de 8) et est portee par la Phase 7-bis.

### 8.1 Refonte de Main.scala pour demontrer les 3 scenarios [FAIT 2026-04-27]
- **Probleme** : `Main.scala` lancait l'ancien `StationControl` (heritage du scope Chatelet). Un correcteur qui clonait le repo et faisait `sbt run` voyait une simulation hors-scope. Les 3 scenarios n'etaient visibles que via les tests, ce qui paraissait moins fini.
- **Action realisee** : `Main.scala` reecrit. Spawn 1 `SectionController` par scenario + un acteur "loggeur" qui joue le role de faux train et affiche les reponses. La demo affiche aussi les transitions Petri et le bilan automatique de l'analyseur. `StationControl.scala` et `StationControlSpec.scala` ont ete supprimes du repo dans la foulee (cf entree historique 2026-04-27). La commande conseillee est `sbt "runMain m14.Main"`.
- **Cout reel** : ~45 min.
- **Verification** : `sbt "runMain m14.Main"` deroule les 3 scenarios sans erreur, `sbt test` reste vert (19/19 apres suppression des 3 StationControlSpec).

### 8.2 Verification LTL programmatique sur l'espace d'etats fini
- **Probleme** : les formules `G !(T1_sur ∧ T2_sur)` et `G (T1_attente → F T1_sur)` sont aujourd'hui en doc mais aucun code ne les evalue. Risque "LTL decorative" du cahier.
- **Action** : ajouter dans `Analyseur.scala` deux fonctions : `verifierGSafety(net, predicat)` (pour tout marquage atteignable, predicat est faux) et `verifierGFLiveness(net, p, q)` (depuis tout marquage ou p tient, q est-il atteignable). Evaluation directe sur le graphe deja construit, **pas** un model checker generique (coherent avec section 7 qui exclut un model checker complet avec automates de Buchi).
- **Cout** : ~1h, ~50-80 lignes. Phase 7 (jeu 1 mai).
- **Verification** : Safety renvoie true sur les 8 marquages. Liveness renvoie true sous hypothese FIFO du controleur (a documenter). Tests unitaires associes dans `AnalyseurSpec`.

### 8.3 Graphe d'accessibilite avec arcs etiquetes
- **Probleme** : l'analyseur produit la liste des 8 marquages mais pas les arcs `(M_i --transition_t--> M_j)` que le cahier appelle "graphe d'accessibilite". La tache 7 du carnet de preuves manuelles attend ce graphe.
- **Action** : ajouter dans `Analyseur.scala` un type `Arc(source: Marking, transition: Transition, cible: Marking)` et faire que `explorerEspaceEtats` retourne aussi la liste des arcs decouverts pendant le BFS. La sortie texte de l'analyseur affiche les arcs (ex : `M0 --T1_demande--> M1`).
- **Cout** : ~30 min, ~20 lignes. Phase 7 (jeu 1 mai).
- **Verification** : la sortie reproduit le diagramme attendu en tache 7 des preuves manuelles, inserable directement en annexe A1 du rapport.

### 8.4 Test d'integration Akka ↔ Petri programmatique
- **Probleme** : la comparaison Akka/Petri (livrable L6) n'existe qu'en Markdown dans `documentation/livrables/comparaison.md`. Le cahier (etape 6 du plan de travail conseille) demande de comparer "etats atteints, transitions observees, messages echanges" entre simulation et modele.
- **Action** : ecrire un nouveau spec `IntegrationAkkaPetriSpec` qui pour chaque scenario : (1) execute la simulation Akka via probes, (2) recolte la trace de messages, (3) traduit chaque message en transition Petri (mapping de `petri/petri-troncon.md` section 8), (4) tire la suite de transitions sur le reseau, (5) verifie que les marquages obtenus correspondent aux marquages attendus de `comparaison.md` sections 2-4.
- **Cout** : 1-2h. Phase 8 (ven 2 mai).
- **Verification** : le spec passe sur les 3 scenarios. La sortie est inserable telle quelle dans le rapport L6.

### 8.5 (non retenu) Second canton en serie
- **Pourquoi serait ambitieux** : introduirait la possibilite reelle de deadlock (deux trains qui se croisent et se bloquent), rendant la preuve d'absence de deadlock plus impressionnante.
- **Pourquoi on refuse** : viole le verrou `[LOCKED] 2 trains, 1 canton, 1 quai` du protocole-coordination.md (mis a jour 29/04), oblige a refaire entierement le carnet de preuves manuelles deja etendu PSD, et a 5 jours du rendu le risque de derive l'emporte sur le gain.
- **Statut** : a mentionner en "Extensions futures" du rapport (L4 section 8), pas implemente. L'extension PSD du 29/04 est la seule extension de scope autorisee sur ce sprint.

### 8.6 Demo HTML locale + memo commandes [FAIT 2026-04-27]
- **Probleme** : le projet peut sembler court si le correcteur ne voit que le nombre de fichiers Scala. Une visualisation simple aide a comprendre la simulation sans changer le modele prouve.
- **Action realisee** : ajout de `demo/index.html` avec scenario nominal, concurrence, liberation/progression, choix du premier train, vitesse, pas-a-pas, places Petri, transitions tirables, trace messages/transitions et invariants. Ajout de `documentation/suivi/COMMANDES.md`.
- **Verification** : la demo HTML est autonome. Les verifications code restent `sbt compile`, `sbt test`, `sbt "runMain m14.Main"`.
- **Limite** : cette demo est une aide de presentation, pas une preuve formelle.

---

## 9) Format de rendu final

- **Pas de site web obligatoire**, pas de slides obligatoires
- Demo HTML locale autorisee comme aide de presentation : `demo/index.html`
- Tout sur GitHub : code dans `src/`, Petri dans `petri/`, docs en Markdown dans `documentation/`
- Le lien GitHub est le livrable principal
- Le rapport final est `documentation/livrables/rapport.md` (exportable PDF si l'enseignant le demande)

---

## 10) Validation de ce plan

Une fois ce plan valide en equipe, il devient la reference jusqu'au 4 mai. Toute deviation passe par une mise a jour de ce fichier et une entree dans `documentation/suivi/historique.md`.
