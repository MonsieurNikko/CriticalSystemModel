# Plan d'execution - Sprint final M14 (25 avril -> 4 mai 2026)

> Document de pilotage du projet. A lire en priorite avant toute contribution sur la periode.
> Source de verite pour le sequencement par phase, le scope figé, et les criteres de rendu.

---

## 1) Cadre

- **Deadline reelle** : 4 mai 2026 (le badge README "fin mai" est obsolete, sera corrige en Phase 9)
- **Duree restante** : 9 jours (25 avril -> 3 mai au soir, rendu le 4 mai)
- **Equipe** : 4 contributeurs (voir `documentation/repartition-equipe.md` pour les specialites)
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
- StationControl.scala existant (densite voyageurs, multi-zones, incidents)
- Plus de 2 trains
- Tolerance aux pannes complete
- Outil graphique de Petri

---

## 3) Livrables exiges par le cahier des charges (projet_2026.pdf)

| # | Livrable | Ou | Etat cible | Etat actuel |
|---|----------|-----|-----------|-------------|
| L1 | Bibliographie commentee (etat de l'art) | `documentation/biblio.md` | 5-8 sources, 2-4 lignes par source | **Squelette + 9 sources commentees** (a relire/finaliser Phase 8) |
| L2 | Modele Akka/Scala fonctionnel | `src/main/scala/m14/troncon/` | Compile + 3 scenarios passent | Squelettes vides, logique a coder Phases 2-3 |
| L3 | Reseau de Petri | `petri/petri-troncon.md` (texte + ASCII) | 7 places, 6 transitions, marquage initial | **Complet** : 7 places, 6 transitions, ASCII, invariants prouves a la main |
| L4 | Rapport de verification (proprietes structurelles + invariants + LTL) | `documentation/rapport.md` | Invariant principal prouve, deadlocks ecartes, LTL formalise | **Squelette structure** (sections vides a remplir Phases 7-8) |
| L5 | Analyseur Petri en code | `src/main/scala/m14/petri/` | Espace d'etats genere, invariants verifies par programme | A coder Phases 5-6 |
| L6 | Simulation comparee Akka vs Petri (3 scenarios) | `documentation/comparaison.md` | Tableau de correspondance messages <-> transitions | **Squelette + matrice 3 scenarios** (sortie analyseur a inserer Phase 6) |
| L7 | Lien GitHub final | README.md | Depot propre, branche main a jour | A finaliser Phase 9 |

**Documents pivots d'equipe** (a lire avant toute contribution) :
- `documentation/lexique.md` : coherence de nommage metier <-> code Akka <-> Petri <-> tests
- `documentation/protocole-coordination.md` : contrat de travail parallele code/preuves, decisions verrouillees, procedures de changement, questions anticipees Phase 2-3
- `documentation/preuves-manuelles.md` : carnet de travail manuel pour la piste verification formelle (independante du code)

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
- [ ] Bibliographie : structure creee dans `documentation/biblio.md`, references a etoffer en Phase 8
- **Verification** : compile OK, squelettes presents sur main, structure documentaire en place

### Phase 2 - samedi 26 avril : Protocole + arbitrage
- [ ] Definir le **protocole de messages** dans `src/main/scala/m14/troncon/Protocol.scala` : `Demande(trainId)`, `Autorisation`, `Attente`, `Sortie(trainId)`, `Liberation`
- [ ] Implementer la **logique d'arbitrage** de `SectionController` : etat libre / occupe T1 / occupe T2 + file d'attente FIFO
- [ ] Test scenario nominal (1 train demande, obtient, sort, libere)
- **Verification** : test nominal passe, `sbt compile` propre

### Phase 3 - dimanche 27 avril : Train + scenarios concurrence
- [ ] Implementer `Train.scala` (etats : hors / attente / sur_troncon)
- [ ] Test scenario **concurrence** : 2 trains demandent quasi-simultanement, un seul entre, l'autre attend
- [ ] Test scenario **liberation/progression** : le train sort, le second progresse
- [ ] Premiere version de `documentation/biblio.md` poussee pour relecture
- **Verification** : `sbt test` vert sur 3 scenarios

### Phase 4 - lundi 28 avril : Reseau de Petri (papier)
- [ ] Dessin ASCII du reseau (7 places, 6 transitions) dans `petri/petri-troncon.md`
- [ ] Definition formelle du marquage initial
- [ ] Verification **a la main** de l'invariant principal sur les 3 scenarios
- [ ] Tableau de **correspondance message Akka <-> transition Petri** (premiere ebauche dans `documentation/comparaison.md`)
- **Verification** : reseau coherent avec architecture Akka

### Phase 5 - mardi 29 avril : Analyseur Petri (1) - structure de donnees
- [ ] Creer `src/main/scala/m14/petri/PetriNet.scala` : types `Place`, `Transition`, `Marking`, `Net`
- [ ] Encoder le reseau du troncon en dur dans le code
- [ ] Fonction `fire(transition, marking)` qui calcule le marquage suivant
- [ ] Tests unitaires sur les transitions (un test par transition)
- [ ] Documenter l'usage de l'analyseur dans `src/main/scala/m14/petri/README.md`
- **Verification** : tirer T1_demande depuis l'etat initial donne le bon marquage

### Phase 6 - mercredi 30 avril : Analyseur Petri (2) - espace d'etats
- [ ] Generation de l'espace d'etats accessible (BFS depuis marquage initial)
- [ ] Verification de l'invariant principal sur tous les marquages atteignables
- [ ] Detection de **deadlock** (etat sans transition tirable)
- [ ] Test d'integration end-to-end : Akka simule scenario X, Analyseur explore espace d'etats X, on compare
- [ ] Mise a jour de `documentation/comparaison.md` avec les sorties de l'analyseur
- **Verification** : ~6-8 etats atteignables, aucun deadlock, invariant tient partout

### Phase 7 - jeudi 1 mai : LTL + finalisation comparaison
- [ ] Formalisation LTL des proprietes :
  - Safety : `G !(T1_sur_troncon ∧ T2_sur_troncon)`
  - Liveness : `G (T1_attente -> F T1_sur_troncon)` (sous fairness)
- [ ] Justification informelle des proprietes LTL sur l'espace d'etats fini
- [ ] `documentation/comparaison.md` finalise : matrice 3 scenarios x (message Akka, transition Petri, etat resultant)
- [ ] Demarrer la **structure du rapport** (`documentation/rapport.md` avec sections vides + plan)
- **Verification** : tableau coherent, scenarios reproductibles, structure rapport prete

### Phase 8 - vendredi 2 mai : Bibliographie + rapport
- [ ] `documentation/biblio.md` finalise : 5-8 references commentees
  - Murata 1989 (Petri nets), doc Akka officielle, LTL TUM, livre Magee/Kramer ou similaire, Lamport (concurrent reasoning), 1-2 articles transport critique
- [ ] `documentation/rapport.md` complet :
  - Section **Contexte** + **Bibliographie** + **Limites/Conclusion**
  - Section **Architecture Akka** (acteurs, protocole, scenarios)
  - Section **Verification** (proprietes Safety/Liveness, deadlock, justification)
  - Section **Reseau de Petri + Analyseur** + section **Comparaison Akka/Petri**
- [ ] Relecture croisee en fin de phase
- **Verification** : rapport complet, longueur cible 8-15 pages

### Phase 9 - samedi 3 mai : Polish, README, fusion main
- [ ] Corriger badge README ("4 mai 2026" au lieu de "fin mai")
- [ ] Section "Comment lancer" du README claire et testee : `sbt compile`, `sbt test`, `sbt "runMain m14.petri.Analyseur"`
- [ ] Verifier `documentation/historique.md` a jour avec toutes les phases
- [ ] Checklist finale livrables (L1 a L7) cochee une derniere fois
- [ ] Merger `feature/m14-coeur-troncon` -> `main` apres validation collective
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
- [ ] `documentation/historique.md` contient une entree decrivant le changement
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
- Refondre `StationControl.scala` (le mentionner comme "extension future" et c'est tout)
- Generaliser l'analyseur a N trains
- Implementer un parseur de format Petri (.pnml ou autre)
- Ajouter une UI / un site web
- Tolerance aux pannes Akka avancee (supervision strategy au-dela du defaut)
- LTL model checking complet (on formalise + on argumente, on ne code pas un model checker)

Si une de ces idees revient, repondre : "extension hors sprint, voir section Limites du rapport".

---

## 8) Format de rendu final

- **Pas de site web**, pas de slides obligatoires
- Tout sur GitHub : code dans `src/`, Petri dans `petri/`, docs en Markdown dans `documentation/`
- Le lien GitHub est le livrable principal
- Le rapport final est `documentation/rapport.md` (exportable PDF si l'enseignant le demande)

---

## 9) Validation de ce plan

Une fois ce plan valide en equipe, il devient la reference jusqu'au 4 mai. Toute deviation passe par une mise a jour de ce fichier et une entree dans `documentation/historique.md`.
