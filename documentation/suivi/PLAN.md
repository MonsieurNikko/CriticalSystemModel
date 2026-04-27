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

Sous-systeme critique M14 : controle d'acces concurrent de **2 trains automatiques** a **1 troncon partage**, via **1 controleur centralise**.

Invariant principal a prouver :

```
T1_sur_troncon + T2_sur_troncon + Troncon_libre = 1
```

Ce qui est dans le coeur :
- 2 acteurs Train + 1 acteur SectionController
- Protocole minimal : demande / autorisation / attente / sortie / liberation
- Reseau de Petri (7 places, 6 transitions)
- Analyseur Scala : generation espace d'etats + verification invariants + absence de deadlock
- Proprietes LTL : exclusion mutuelle (Safety), progression (Liveness)

Hors coeur (mentionnes "extensions futures" dans le rapport, pas implementes) :
- Plus de 2 trains
- Tolerance aux pannes complete
- Outil graphique de Petri
- Densite voyageurs / multi-zones / incidents de station (ancien scope Chatelet, supprime du repo le 2026-04-27)

---

## 3) Livrables exiges par le cahier des charges (projet_2026.pdf)

| # | Livrable | Ou | Etat cible | Etat actuel |
|---|----------|-----|-----------|-------------|
| L1 | Bibliographie commentee (etat de l'art) | `documentation/livrables/biblio.md` | 5-8 sources, 2-4 lignes par source | **9 sources commentees** (a relire/finaliser Phase 8) |
| L2 | Modele Akka/Scala fonctionnel | `src/main/scala/m14/troncon/` | Compile + 3 scenarios passent | **Complet** : Train + SectionController + tests + demo console |
| L3 | Reseau de Petri | `petri/petri-troncon.md` (texte + ASCII) | 7 places, 6 transitions, marquage initial | **Complet** : 7 places, 6 transitions, ASCII, invariants prouves a la main |
| L4 | Rapport de verification (proprietes structurelles + invariants + LTL) | `documentation/livrables/rapport.md` | Invariant principal prouve, deadlocks ecartes, LTL formalise | **Squelette structure** (sections vides a remplir Phases 7-8) |
| L5 | Analyseur Petri en code | `src/main/scala/m14/petri/` | Espace d'etats genere, invariants verifies par programme | **Complet** : BFS, 8 marquages, invariants, deadlocks, tests |
| L6 | Simulation comparee Akka vs Petri (3 scenarios) | `documentation/livrables/comparaison.md` | Tableau de correspondance messages <-> transitions | **Partiel** : matrice faite, sortie analyseur a inserer |
| L7 | Lien GitHub final | README.md | Depot propre, branche main a jour | A finaliser apres commit/push |
| Bonus | Demo visuelle locale | `demo/index.html` | Aide de soutenance, pas preuve principale | **Prototype fait** : pas-a-pas Akka/Petri |

**Documents pivots d'equipe** (a lire avant toute contribution) :
- `documentation/gouvernance/lexique.md` : coherence de nommage metier <-> code Akka <-> Petri <-> tests
- `documentation/gouvernance/protocole-coordination.md` : contrat de travail parallele code/preuves, decisions verrouillees, procedures de changement, questions anticipees Phase 2-3
- `documentation/livrables/preuves-manuelles.md` : carnet de travail manuel pour la piste verification formelle (independante du code)

**Contrainte cle du cahier** : interdiction d'utiliser un outil logiciel de Petri externe. L'analyseur **doit** etre code par nous.

---

## 4) Calendrier par phase

### Phase 1 - vendredi 25 avril : Plan + squelette (TERMINEE)
- [x] Lire cahier des charges + recadrage
- [x] Valider ce PLAN.md
- [x] Creer la branche `feature/m14-coeur-troncon`
- [x] Squelette des 3 acteurs (`Protocol.scala`, `Train.scala`, `SectionController.scala`) - signatures de messages, pas de logique
- [x] `sbt compile` passe
- [x] Branche `feature/m14-coeur-troncon` mergee sur `main` le 26 avril (voir historique.md)
- [ ] Bibliographie : structure creee dans `documentation/livrables/biblio.md`, references a etoffer en Phase 8
- **Verification** : compile OK, squelettes presents sur main, structure documentaire en place

### Phase 2 - samedi 26 avril : Protocole + arbitrage (TERMINEE)
- [x] Definir le **protocole de messages** dans `src/main/scala/m14/troncon/Protocol.scala` : `Demande`, `Autorisation`, `Attente`, `Sortie`
- [x] Implementer la **logique d'arbitrage** de `SectionController` : etat libre / occupe + file d'attente FIFO
- [x] Test scenario nominal (1 train demande, obtient, sort, libere)
- **Verification** : `sbt compile` et `sbt test` passent

### Phase 3 - dimanche 27 avril : Train + scenarios concurrence (TERMINEE)
- [x] Implementer `Train.scala` (etats : hors / attente / sur_troncon)
- [x] Test scenario **concurrence** : 2 trains demandent quasi-simultanement, un seul entre, l'autre attend
- [x] Test scenario **liberation/progression** : le train sort, le second progresse
- [x] Premiere version de `documentation/livrables/biblio.md`
- **Verification** : `sbt test` vert (19 tests)

### Phase 4 - lundi 28 avril : Reseau de Petri (papier) (TERMINEE)
- [x] Dessin ASCII du reseau (7 places, 6 transitions) dans `petri/petri-troncon.md`
- [x] Definition formelle du marquage initial
- [x] Verification **a la main** de l'invariant principal sur les transitions
- [x] Tableau de **correspondance message Akka <-> transition Petri** dans `documentation/livrables/comparaison.md`
- **Verification** : reseau coherent avec architecture Akka

### Phase 5 - mardi 29 avril : Analyseur Petri (1) - structure de donnees (TERMINEE)
- [x] Creer `src/main/scala/m14/petri/PetriNet.scala` : types `Place`, `Transition`, `Marking`, `Net`
- [x] Encoder le reseau du troncon en dur dans le code
- [x] Fonction `tirer(transition, marking)` qui calcule le marquage suivant
- [x] Tests unitaires sur les transitions principales
- [x] Documenter l'usage via `documentation/suivi/COMMANDES.md`
- **Verification** : tests `AnalyseurSpec` verts

### Phase 6 - mercredi 30 avril : Analyseur Petri (2) - espace d'etats (TERMINEE pour le code)
- [x] Generation de l'espace d'etats accessible (BFS depuis marquage initial)
- [x] Verification de l'invariant principal sur tous les marquages atteignables
- [x] Detection de **deadlock** (etat sans transition tirable)
- [ ] Test d'integration end-to-end Akka/Petri (bonus si temps)
- [ ] Mise a jour de `documentation/livrables/comparaison.md` avec les sorties de l'analyseur
- **Verification** : 8 etats atteignables, aucun deadlock, invariant tient partout

### Phase 7 - jeudi 1 mai : LTL + finalisation comparaison
- [ ] Formalisation LTL des proprietes :
  - Safety : `G !(T1_sur_troncon ∧ T2_sur_troncon)`
  - Liveness : `G (T1_attente -> F T1_sur_troncon)` (sous fairness)
- [ ] Justification informelle des proprietes LTL sur l'espace d'etats fini
- [ ] `documentation/livrables/comparaison.md` finalise : matrice 3 scenarios x (message Akka, transition Petri, etat resultant)
- [x] Demo console enrichie : `sbt "runMain m14.Main"`
- [x] Demo HTML prototype : `demo/index.html`
- [x] Memo commandes : `documentation/suivi/COMMANDES.md`
- **Verification** : tableau coherent, scenarios reproductibles, structure rapport prete

### Phase 8 - vendredi 2 mai : Bibliographie + rapport
- [ ] `documentation/livrables/biblio.md` finalise : 5-8 references commentees
  - Murata 1989 (Petri nets), doc Akka officielle, LTL TUM, livre Magee/Kramer ou similaire, Lamport (concurrent reasoning), 1-2 articles transport critique
- [ ] `documentation/livrables/rapport.md` complet :
  - Section **Contexte** + **Bibliographie** + **Limites/Conclusion**
  - Section **Architecture Akka** (acteurs, protocole, scenarios)
  - Section **Verification** (proprietes Safety/Liveness, deadlock, justification)
  - Section **Reseau de Petri + Analyseur** + section **Comparaison Akka/Petri**
- [ ] Relecture croisee en fin de phase
- **Verification** : rapport complet, longueur cible 8-15 pages

### Phase 9 - samedi 3 mai : Polish, README, fusion main
- [ ] Corriger badge README ("4 mai 2026" au lieu de "fin mai")
- [ ] Section "Comment lancer" du README claire et testee : `sbt compile`, `sbt test`, `sbt "runMain m14.Main"`, `sbt "runMain m14.petri.Analyseur"`
- [ ] Mentionner `demo/index.html` comme aide visuelle optionnelle
- [ ] Verifier `documentation/suivi/historique.md` a jour avec toutes les phases
- [ ] Checklist finale livrables (L1 a L7) cochee une derniere fois
- [ ] Commit/push de la branche finale apres validation collective
- [ ] Tag git : `v1.0-rendu-2026-05-04`
- **Verification** : checkout propre depuis main + sbt compile/test verts

### Phase 10 - dimanche 4 mai : RENDU
- [ ] Soumettre le lien GitHub
- Aucun nouveau commit ce jour-la sauf urgence

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
| L'analyseur Petri prend plus de temps que prevu | Moyenne | Garder le reseau tres petit (7 places, 6 transitions). Pas plus. Pas de generalisation. |
| Bug de concurrence Akka non reproductible | Moyenne | Utiliser Akka TestKit avec probes deterministes. Pas de Thread.sleep. |
| Conflit git en fin de sprint | Faible | Tout sur la meme branche `feature/m14-coeur-troncon`, merge final Phase 9 uniquement. |
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

Consequence : le scope (2 trains, 1 troncon, 8 marquages, 4 messages, 7 places, 6 transitions) reste fige (cf `documentation/gouvernance/protocole-coordination.md` section 2). A la place d'etendre le systeme, on etend la **verification** sur 4 axes a integrer dans les Phases 7-9 sans creer de nouvelle phase.

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

### 8.5 (non retenu) Second troncon en serie
- **Pourquoi serait ambitieux** : introduirait la possibilite reelle de deadlock (deux trains qui se croisent et se bloquent), rendant la preuve d'absence de deadlock plus impressionnante.
- **Pourquoi on refuse** : viole le verrou `[LOCKED] 2 trains, 1 troncon` du protocole-coordination.md, oblige a refaire entierement le carnet de preuves manuelles, et a 7 jours du rendu le risque de derive l'emporte sur le gain.
- **Statut** : a mentionner en "Extensions futures" du rapport (L4 section 8), pas implemente.

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
