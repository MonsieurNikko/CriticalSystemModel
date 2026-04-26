# Historique des changements - CriticalSystemModel

Ce fichier est obligatoire pour la tracabilite des interventions sur le projet.
Ordre obligatoire: chronologique inverse (la plus recente entree en premier).

## Template d'entree (copier/coller)

### [YYYY-MM-DD HH:MM] - Titre court de l'intervention
- GitHub: @username
- Branche: feature/nom-branche
- Contexte/tache: description courte de l'objectif
- Fichiers modifies:
	- chemin/fichier1
	- chemin/fichier2
- Changements detailles:
	- Point 1 (ce qui a ete change et pourquoi)
	- Point 2 (impact attendu)
- Commandes executees:
	- commande 1
	- commande 2
- Resultats de verification:
	- Build: PASS | FAIL | N/A
	- Tests: PASS | FAIL | N/A
	- Notes: details utiles (erreurs, warnings, limitations)
- Risques/impacts:
	- Risque 1
	- Risque 2
- Prochaines actions recommandees:
	- Action 1
	- Action 2

---

## Entrees

### [2026-04-26 15:55] - Verification Phase 2 : sbt compile + sbt test verts
- GitHub: @MonsieurNikko (assistance IA Claude Sonnet 4.6)
- Branche: feature/m14-doc-anticipation-phase2
- Contexte/tache: installer sbt sur la machine de l'agent IA, puis valider que le commit precedent (5826b9b) compile et que les tests existants continuent de passer.
- Fichiers modifies:
	- documentation/historique.md (cette entree)
- Commandes executees:
	- brew install sbt (sbt 1.12.9 installe avec succes via Homebrew, Java 21 Temurin)
	- sbt compile : 5 fichiers Scala compiles, 8 secondes
	- sbt test : 3 tests StationControlSpec passent, 0 echec
- Resultats de verification:
	- Build: PASS
	- Tests: PASS (3/3 StationControlSpec)
	- Notes: warning cosmetique SLF4J sur l'absence de logger backend, sans impact fonctionnel.
- Risques/impacts:
	- L'absence de tests pour SectionController reste une lacune qui sera comblee en Phase 4 (3 ScenarioXxxSpec).
	- StationControlSpec teste un module hors scope du recadrage, mais on le garde vert pour ne pas casser l'historique du build.
- Prochaines actions recommandees:
	- Push de la branche feature/m14-doc-anticipation-phase2 vers origin (necessite auth GitHub).
	- Apres push : merge --no-ff vers main et push origin main.
	- Phase 3 : implementer Train.scala (3 Behaviors : comportementHors, comportementEnAttente, comportementSurTroncon).

---

### [2026-04-26 15:45] - Phase 2 implementee : SectionController complet avec FIFO
- GitHub: @MonsieurNikko (assistance IA Claude Sonnet 4.6)
- Branche: feature/m14-doc-anticipation-phase2
- Contexte/tache: implementation de la logique d'arbitrage du troncon partage conformement au protocole de coordination (sections 4 Q1-Q10) et au lexique d'equipe.
- Fichiers modifies:
	- src/main/scala/m14/troncon/SectionController.scala (squelette vide -> implementation complete 70 lignes)
- Changements detailles:
	- Deux Behaviors distincts (un par etat, conformement a Q5) : `etatLibre()` et `etatOccupe(occupant, fileAttente)`. Pas de `var`, pas de match geant.
	- File d'attente : `scala.collection.immutable.Queue[EnAttente]` (conformement a Q1). Le type alias `EnAttente = (IdTrain, ActorRef[MessagePourTrain])` memorise l'adresse de reponse pour pouvoir notifier le train quand son tour viendra.
	- Robustesse defensive (Q2, Q3) : un Demande recu de l'occupant ou un Sortie recu hors-occupation sont ignores avec commentaire explicite. Pas de modification du modele Petri pour ces cas (cf protocole cas B).
	- Helper `promouvoirOuLiberer(fileAttente)` extrait pour garder `etatOccupe` sous 30 lignes (regle vibe-code section 12 du REGLES_PROJET).
	- Commentaires referencent explicitement les transitions Petri concernees (Ti_entree_autorisee, Ti_sortie_liberation) et les sections de doc (lexique.md section 2, comparaison.md scenario 2, etc).
- Verification mentale sur les 3 scenarios (cf documentation/comparaison.md) :
	- Scenario 1 nominal : etatLibre -> etatOccupe(Train1, vide) -> etatLibre. OK.
	- Scenario 2 concurrence : etatLibre -> etatOccupe(Train1, vide) -> etatOccupe(Train1, [(Train2, ref2)]). OK.
	- Scenario 3 liberation : etatOccupe(Train1, [(Train2, ref2)]) -> etatOccupe(Train2, vide) avec Autorisation envoyee a ref2. OK.
- Commandes executees:
	- ecriture du fichier
- Resultats de verification:
	- Build: NON LANCE (sbt non disponible sur la machine de l'agent IA, cf entree precedente)
	- Tests: N/A (les tests scenario sont l'objet de la Phase 4, pas encore ecrits)
	- Notes: l'utilisateur humain doit lancer `sbt compile` localement avant de pousser.
- Risques/impacts:
	- Le code reference `Q1` `Q2` etc dans les commentaires : si protocole-coordination.md est renumerote, ces references se desynchronisent. Risque faible.
	- La file FIFO Akka est plus deterministe que le modele Petri (qui est non-deterministe sur le choix entre T1_entree_autorisee et T2_entree_autorisee depuis M4). Cette dissymetrie est documentee dans petri-troncon.md section 9 et lexique.md section 2.
	- Pas de tests ecrits a cette etape : la validation se fait par lecture humaine du code et verification mentale. Phase 4 ajoutera les 3 ScenarioXxxSpec.scala.
- Prochaines actions recommandees:
	- Lancer `sbt compile` en local pour valider la syntaxe Scala (en particulier l'usage de `Queue.dequeueOption`, disponible en Scala 2.13).
	- Phase 3 : implementer `Train.scala` avec la meme structure (un Behavior par etat : `comportementHors`, `comportementEnAttente`, `comportementSurTroncon`).
	- Phase 4 : ecrire les 3 fichiers de tests (`ScenarioNominalSpec`, `ScenarioConcurrenceSpec`, `ScenarioLiberationProgressionSpec`) en utilisant Akka TestKit Probes.

---

### [2026-04-26 15:15] - Ajout carnet preuves manuelles + protocole de coordination equipe
- GitHub: @MonsieurNikko (assistance IA Claude Sonnet 4.6)
- Branche: feature/m14-doc-anticipation-phase2
- Contexte/tache: standardiser le travail parallele entre la piste codage Akka/analyseur (Piste B, Nikko) et la piste preuves manuelles (Piste A, reste de l'equipe), en anticipant les conflits qui surgissent typiquement pendant le codage. Reponse a la question "comment mes equipiers peuvent occuper la verification a la main pendant que je code".
- Fichiers crees:
	- documentation/preuves-manuelles.md (carnet de travail manuel structure en 7 taches : enumeration espace d'etats, verification invariant principal, invariants par train, absence de deadlock, formalisation LTL, argumentation Liveness sous fairness, diagramme final ; chaque tache contient un template + un exemple pre-rempli pour servir de modele)
	- documentation/protocole-coordination.md (contrat d'equipe : sources de verite par artefact, liste gelee des decisions verrouillees, procedures de changement par cas (A renommage, B detail protocole, C bug Petri, D bug code, E nouveau scenario), 10 questions anticipees pour Phase 2-3 avec reponses de reference, points de synchronisation, template RFC, checklist de cloture, anti-patterns)
- Changements detailles:
	- preuves-manuelles.md : permet a 3 contributeurs (Axelobistro, Alicette, Ostreann) de travailler sur les preuves formelles immediatement, sans attendre que le code soit ecrit. Contient deja les 8 marquages attendus pre-remplis dans la tache 1, l'invariant principal verifie en tache 2, les transitions tirables par marquage en tache 4. Les zones a remplir par l'equipe sont marquees `_` ou `?`.
	- protocole-coordination.md : repond au besoin de "standardisation". Section 1 indique qui edite quoi et qui relit. Section 2 verrouille 7 decisions critiques (7 places, 6 transitions, 4 messages, 2 trains, marquage initial, invariant principal, 3 scenarios). Section 4 anticipe 10 questions concretes du codage Phase 2-3 et donne des reponses de reference (FIFO, robustesse defensive, etat interne du controleur, etc).
- Commandes executees:
	- ecriture des 2 fichiers
- Resultats de verification:
	- Build: N/A (changements 100% documentaires)
	- Tests: N/A
- Risques/impacts:
	- Le protocole-coordination.md depend du fait que tous les contributeurs le lisent vraiment. Risque humain, pas technique.
	- Les "reponses de reference" des Q1-Q10 figent certains choix d'implementation (Queue immutable, Behavior par etat, pas de var, etc) qui contraignent la Piste B. C'est volontaire pour eviter qu'on se reinterroge sur ces points pendant le codage.
- Prochaines actions recommandees:
	- Distribuer ce protocole a l'equipe (envoyer le lien de la branche `feature/m14-doc-anticipation-phase2`).
	- Avant Phase 2 : que chaque contributeur confirme avoir lu protocole-coordination.md (par une entree historique.md ou un emoji sur le PR).
	- Phase 2 : Nikko code SectionController en respectant strictement les Q1-Q10 du protocole.
	- En parallele : Axelobistro commence tache 1 du carnet, Alicette commence taches 2-3.

---

### [2026-04-26 14:30] - Correction docs + anticipation livrables L1/L3/L4/L6 + lexique d'equipe
- GitHub: @MonsieurNikko (assistance IA Claude Sonnet 4.6)
- Branche: feature/m14-doc-anticipation-phase2
- Contexte/tache: apres lecture complete du cahier des charges (`projet_2026.pdf`) et de toute la documentation interne, corriger les incoherences detectees dans README.md et PLAN.md, et anticiper la creation de plusieurs livrables sous forme de squelette pour cadrer le travail des phases 2 a 9.
- Fichiers modifies:
	- README.md (3 corrections : badge deadline, reference fichier renomme, libelle livrable obsolete)
	- documentation/PLAN.md (Phase 1 cochee comme terminee, table des livrables enrichie d'une colonne "Etat actuel")
	- documentation/historique.md (cette entree)
- Fichiers crees:
	- petri/petri-troncon.md (livrable L3 complet : 7 places, 6 transitions, marquage initial, schema ASCII, preuve a la main de l'invariant principal, espace d'etats attendu, table de correspondance messages Akka <-> transitions Petri, limites assumees)
	- documentation/biblio.md (livrable L1 : squelette + 9 sources commentees couvrant Petri, Akka, LTL, concurrence, transport critique)
	- documentation/comparaison.md (livrable L6 : squelette + matrice complete des 3 scenarios avec marquages Petri pas-a-pas)
	- documentation/rapport.md (livrable L4 : squelette structure 8 sections, sections vides a remplir Phases 7-8)
	- documentation/lexique.md (document pivot d'equipe : aligne le vocabulaire entre metier, code Akka, places Petri, tests pour garantir la comprehension partagee dans une equipe de 4 contributeurs)
	- CLAUDE.md (a la racine, genere par /init pour orienter les futures sessions IA)
- Changements detailles:
	- README.md : `badge deadline-fin%20mai%202026` -> `4%20mai%202026` (cf PLAN.md Phase 9 qui mentionne ce bug). Reference cassee `documentation/AGENT_RULES.md` -> `documentation/REGLES_PROJET.md` + ajout PLAN.md et HANDOVER.md dans la liste obligatoire. Libelle "Code Akka/Scala fonctionnel (M14 Chatelet)" remplace par "Code Akka/Scala fonctionnel du troncon partage" + chemins de chaque livrable. Exemples de branches mis a jour vers le scope troncon partage.
	- PLAN.md : cases Phase 1 cochees (squelettes presents, branche merge sur main, compile passe). Table des livrables : ajout d'une colonne "Etat actuel" reflechissant ce qui existe vraiment apres cette intervention.
	- petri/petri-troncon.md : pose le formalisme complet du reseau, fait office de **source de verite du vocabulaire** pour le code a venir (les noms `T1_hors`, `T1_attente`, `T1_sur_troncon` etc. doivent etre repris a l'identique dans les Behaviors Akka).
	- documentation/lexique.md : table de correspondance metier <-> Petri <-> Akka <-> tests, avec regle dure de coherence : tout nouveau terme passe d'abord par Petri, puis par le lexique, puis par le code.
- Commandes executees:
	- git checkout -b feature/m14-doc-anticipation-phase2
	- lecture exhaustive de documentation/{PLAN,REGLES_PROJET,HANDOVER,historique,recadrage-m14-troncon-critique,repartition-equipe}.md
	- extraction et lecture du cahier des charges projet_2026.pdf via decodage zlib des streams
	- ecriture des 6 fichiers ci-dessus
- Resultats de verification:
	- Build: N/A (sbt n'est pas disponible sur la machine de l'agent IA - changements 100% documentaires, aucun fichier .scala touche, donc risque nul sur le build)
	- Tests: N/A (aucun changement de code Scala)
	- Notes: l'utilisateur humain doit faire tourner `sbt compile && sbt test` localement avant push final, conforme aux regles projet section 6.
- Risques/impacts:
	- Risque cosmetique : la table des livrables PLAN.md a une nouvelle colonne, peut casser un parser markdown strict (peu probable).
	- Le document `lexique.md` mentionne des noms de Behaviors (`comportementHors`, `comportementEnAttente`, `comportementSurTroncon`, `EtatLibre`, `EtatOccupe`) qui n'existent pas encore dans le code : c'est volontaire, c'est le contrat que devront respecter les Phases 2 et 3.
	- La biblio liste 9 sources alors que la cible cahier est 5-8 : a relire en Phase 8, possibilite d'en retirer 1 ou 2.
- Prochaines actions recommandees:
	- Phase 2 (samedi 26 avril, en cours) : implementer `SectionController` en respectant strictement les noms de places Petri (cf lexique.md section 2). Logique d'arbitrage avec file FIFO. Un comportement Akka (Behavior) par etat, pas un grand match.
	- Phase 3 (dimanche 27 avril) : implementer `Train` avec un Behavior par etat (`comportementHors`, `comportementEnAttente`, `comportementSurTroncon`).
	- Phase 4 : ecrire les 3 fichiers de tests `ScenarioNominalSpec`, `ScenarioConcurrenceSpec`, `ScenarioLiberationProgressionSpec` en suivant la matrice de comparaison.md section 2-3-4.
	- Avant tout push : refaire passer `sbt compile && sbt test`, completer cette entree historique.

---

### [2026-04-26 00:10] - Preparation merge vers main + adaptation historique
- GitHub: @MonsieurNikko
- Branche: feature/m14-coeur-troncon
- Contexte/tache: preparer l'integration de la branche de travail sur `main`, en nettoyant la tracabilite documentaire avant merge.
- Fichiers modifies:
	- documentation/historique.md
	- documentation/REGLES_PROJET.md
	- documentation/PLAN.md
	- documentation/HANDOVER.md
	- src/main/scala/m14/troncon/Protocol.scala
	- src/main/scala/m14/troncon/SectionController.scala
	- src/main/scala/m14/troncon/Train.scala
- Changements detailles:
	- Ajout d'une entree de preparation merge pour expliciter le passage feature -> main.
	- Correction de la reference historique `documentation/AGENT_RULES.md` vers `documentation/REGLES_PROJET.md` suite au renommage.
	- Consolidation des artefacts de Phase 1 (plan sprint, handover, squelette troncon) avant integration.
- Commandes executees:
	- git status -sb
	- lecture de documentation/historique.md
	- git add (fichiers projet uniquement)
	- git commit
	- git checkout main
	- git pull origin main
	- git merge feature/m14-coeur-troncon
	- git push origin main
- Resultats de verification:
	- Build: N/A (pas de relance compile/test dans cette etape de merge)
	- Tests: N/A
	- Notes: les artefacts `target/` et `.metals/` restent exclus du commit.
- Risques/impacts:
	- Sans hygiene Git, des artefacts de build pourraient polluer la revue (evite ici par selection explicite des fichiers).
	- Le merge peut necessiter une resolution si `main` a evolue entre-temps.
- Prochaines actions recommandees:
	- Verifier `sbt compile` et `sbt test` depuis `main` apres merge.
	- Ouvrir la Phase 2 (protocole + arbitrage) sur la branche de travail.

---

### [2026-04-25 23:16] - Sprint final : planification + squelette package troncon
- GitHub: @MonsieurNikko
- Branche: feature/m14-coeur-troncon
- Contexte/tache: demarrer le sprint final de 9 jours (deadline reelle 2026-05-04). Poser le plan de sprint, le guide de reprise pour l'equipe, et le squelette compilant des 3 acteurs du coeur troncon (Phase 1 sur 10).
- Fichiers modifies:
  - documentation/PLAN.md (nouveau)
  - documentation/HANDOVER.md (nouveau)
	- documentation/REGLES_PROJET.md (modifie: §2 invariants alignes sur le coeur M14)
  - src/main/scala/m14/troncon/Protocol.scala (nouveau)
  - src/main/scala/m14/troncon/SectionController.scala (nouveau)
  - src/main/scala/m14/troncon/Train.scala (nouveau)
- Changements detailles:
  - PLAN.md : sprint 25 avril -> 4 mai decoupe en 10 phases, avec tableau des livrables, definition of done, risques, hors scope explicite et format de rendu.
  - HANDOVER.md : guide de reprise (lecture en 10 minutes), glossaire Akka/Petri/verif, conventions de lecture du code, checklist push, tableau d'anti-patterns.
	- REGLES_PROJET.md : §2 invariants critiques mis a jour (T1+T2+libre=1, pas de collision, pas de deadlock).
  - Package troncon : 3 fichiers squelette qui compilent. Protocol.scala definit IdTrain (Train1/Train2), MessagePourControleur (Demande, Sortie), MessagePourTrain (Autorisation, Attente). SectionController et Train sont des Behaviors vides avec TODO Phase 2/3.
- Commandes executees:
  - git checkout -b feature/m14-coeur-troncon
  - mkdir -p src/main/scala/m14/troncon
  - sbt compile
- Resultats de verification:
  - Build: PASS (5 sources Scala compiles en 8s)
  - Tests: N/A (Phase 1 ne touche pas les tests existants)
  - Notes: warning JLine "dumb terminal" attendu en mode batch sbt, sans impact fonctionnel.
- Risques/impacts:
  - Le squelette troncon coexiste avec StationControl.scala (ancien scope station). Non-blocant : StationControl reste hors-coeur, mentionne comme extension future dans PLAN.md.
  - Le protocole de messages dans Protocol.scala est une premiere passe ; il pourra etre affine en Phase 2 sans casser le compile.
- Prochaines actions recommandees:
  - Push de la branche feature/m14-coeur-troncon apres validation humaine.
  - Lancer Phase 2 (samedi 26 avril) : finaliser Protocol.scala, implementer la logique d'arbitrage SectionController, ecrire le test scenario nominal.
  - Demarrer en parallele la biblio sur une branche dediee.

---

### [2026-04-17 12:10] - Recadrage du guide complet et alignement final de la documentation
- GitHub: @MonsieurNikko
- Branche: feature/m14-docs-recentrage-guide
- Contexte/tache: modifier aussi le guide global pour supprimer l'ancien cadrage billetterie et l'aligner sur le sous-systeme critique M14 retenu
- Fichiers modifies:
	- projet2026_guide_complet.md
	- documentation/historique.md
- Changements detailles:
	- Remplacement du contenu billetterie par un guide recadre sur 2 trains, 1 troncon partage, 1 controleur d'acces.
	- Suppression des sections de code detaillees dans le guide au profit d'un cadrage academique de planification.
	- Alignement explicite des objectifs, scenarios, proprietes et limites avec le noyau de preuve exclusion mutuelle + progression.
- Commandes executees:
	- Get-Date -Format "yyyy-MM-dd HH:mm"
	- edition documentaire via apply_patch
	- sbt compile
	- sbt test
- Resultats de verification:
	- Build: PASS
	- Tests: PASS
	- Notes: warning SLF4J (binder absent) observe en tests, sans impact fonctionnel
- Risques/impacts:
	- Baisse de confusion entre ancien sujet billetterie et recadrage M14.
	- Le guide devient plus sobre mais plus defendable sur les exigences formelles.
- Prochaines actions recommandees:
	- Pousser sur branche dediee et ouvrir la revue.
	- Fusionner vers main apres validation finale de coherence documentaire.

### [2026-04-17 12:05] - Recadrage documentaire vers le sous-systeme troncon critique
- GitHub: @MonsieurNikko
- Branche: main
- Contexte/tache: appliquer un recadrage strict du projet existant vers un perimetre minimal defendable (2 trains, 1 troncon partage), sans implementation
- Fichiers modifies:
	- README.md
	- documentation/repartition-equipe.md
	- documentation/recadrage-m14-troncon-critique.md
	- documentation/historique.md
- Changements detailles:
	- Reorientation du README vers une abstraction explicite de sous-systeme critique M14, en supprimant le cadrage station complete du coeur du projet.
	- Mise a jour de la repartition equipe pour aligner les roles, livrables et risques sur le mecanisme d'exclusion mutuelle du troncon partage.
	- Ajout d'un document de recadrage complet (diagnostic, architecture minimale, flux critiques, scenarios, reseau de Petri cible, proprietes, invariant principal, limites).
	- Clarification des hypotheses academiques pour eviter toute sur-promesse de realisme ou d'equivalence totale Akka/Petri.
- Commandes executees:
	- Get-Date -Format "yyyy-MM-dd HH:mm"
	- edition documentaire via apply_patch/create_file
- Resultats de verification:
	- Build: N/A
	- Tests: N/A
	- Notes: phase de planification uniquement, aucun code source ni tests modifies
- Risques/impacts:
	- Le recadrage peut paraitre moins "complet" mais augmente fortement la defendabilite formelle.
	- Un risque de re-derivation du scope subsiste si les extensions futures reviennent trop tot dans le coeur.
- Prochaines actions recommandees:
	- Valider en equipe le perimetre final et geler la checklist de scope.
	- Preparer la matrice de correspondance messages Akka <-> transitions Petri sur 3 scenarios.

### [2026-04-17 11:45] - Push et Merge vers la branche principale (main)
- GitHub: @MonsieurNikko
- Branche: main
- Contexte/tache: Finaliser la premiere phase d'implementation critique en fusionnant la branche de fonctionnalite vers main
- Fichiers modifies:
	- Toutes les ressources de la branche feature/documentation-agent-rules
- Changements detailles:
	- Validation et commit de l'historique complet avant fusion.
	- Synchronisation locale de main avec le depot distant (git pull).
	- Fusion (merge) de feature/documentation-agent-rules vers main.
	- Publication (push) des changements fusionnes vers le depot principal.
- Commandes executees:
	- git add documentation/historique.md
	- git commit -m "docs: mise a jour de l'historique avant merge"
	- git push origin feature/documentation-agent-rules
	- git checkout main
	- git pull origin main
	- git merge feature/documentation-agent-rules
	- git push origin main
- Resultats de verification:
	- Build: PASS (pre-merge branch)
	- Tests: PASS (pre-merge branch)
	- Notes: Fusion reussie via strategie 'ort' apres resolution de retard local sur main.
- Risques/impacts:
	- Branche main desormais a jour avec le pivot M14 Chatelet.
	- Ecrasement potentiel de modifications conflictuelles sur main (resolu par pull preventif).
- Prochaines actions recommandees:
	- Supprimer la branche feature locale si elle n'est plus necessaire.
	- Demarrer la phase de modelisation formelle (Reseaux de Petri).

### [2026-04-15 09:25] - Demarrage implementation M14 Chatelet
- GitHub: @MonsieurNikko
- Branche: feature/documentation-agent-rules
- Contexte/tache: demarrer l'implementation d'un systeme distribue critique pour la gestion du trafic M14 a Chatelet et aligner la documentation
- Fichiers modifies:
	- README.md
	- documentation/repartition-equipe.md
	- documentation/historique.md
	- src/main/scala/m14/StationControl.scala
	- src/main/scala/m14/Main.scala
	- src/test/scala/m14/StationControlSpec.scala
- Changements detailles:
	- Creation d'un socle Akka Typed pour StationControl avec supervision de densite, gestion d'incidents et mode Safety.
	- Ajout d'un point d'entree minimal de simulation M14 (Main).
	- Ajout de tests ScalaTest/Akka TestKit couvrant 3 scenarios critiques (incident critique, surcharge zone, retour mode normal).
	- Pivot documentaire complet du README et de la repartition equipe vers le domaine M14 Chatelet.
- Commandes executees:
	- rg --files -g "!target/**" -g "!project/target/**"
	- sbt compile
	- sbt test
	- Get-Date -Format "yyyy-MM-dd HH:mm"
- Resultats de verification:
	- Build: PASS
	- Tests: PASS
	- Notes: warning SLF4J (binder absent) sans impact fonctionnel sur les tests
- Risques/impacts:
	- Le scope peut deriver si la modelisation inclut toute la ligne au lieu de la zone Chatelet.
	- Les artefacts de build (target/) ne doivent pas etre commits.
- Prochaines actions recommandees:
	- Completer la modelisation Petri des proprietes Safety/Liveness definies dans le README.
	- Ajouter des tests de concurrence supplementaires pour des rafales d'evenements simultanes.

### [2026-04-14 09:09] - Affectation des noms reels dans la repartition
- GitHub: @MonsieurNikko
- Branche: feature/documentation-agent-rules
- Contexte/tache: remplacer les placeholders Membre A/B/C/D par les noms reels de l'equipe
- Fichiers modifies:
	- documentation/repartition-equipe.md
	- documentation/historique.md
- Changements detailles:
	- Affectation des roles a Axelobistro, Alicette, Nikko et Ostreann selon l'ordre fourni.
	- Mise a jour des binomes de relecture et des sections collaboratives avec les noms.
	- Alignement des exemples de branches et du planning hebdomadaire avec les noms de l'equipe.
- Commandes executees:
	- Get-Date -Format "yyyy-MM-dd HH:mm"
	- edition documentaire via apply_patch
- Resultats de verification:
	- Build: N/A
	- Tests: N/A
	- Notes: modifications documentaires uniquement
- Risques/impacts:
	- La repartition reste ajustable selon les disponibilites reelles de chacun.
	- Aucun impact sur le code applicatif.
- Prochaines actions recommandees:
	- Confirmer collectivement la repartition en reunion courte.
	- Ouvrir les 4 issues de demarrage et associer chaque branche a son owner.

### [2026-04-14 09:04] - Proposition de repartition equipe (4 membres)
- GitHub: @MonsieurNikko
- Branche: feature/documentation-agent-rules
- Contexte/tache: relire les fichiers projet et proposer une repartition logique permettant le travail parallele et collaboratif
- Fichiers modifies:
	- documentation/repartition-equipe.md
	- documentation/historique.md
- Changements detailles:
	- Ajout d'un plan de repartition a 4 membres avec owners, binomes, zones communes et flux de collaboration.
	- Ajout d'un planning iteratif sur 2 semaines avec points d'integration.
	- Definition d'un workflow Git collaboratif, d'une Definition of Done commune et des parades aux risques principaux.
- Commandes executees:
	- rg --files -g "!target/**" -g "!project/target/**"
	- lecture README.md, build.sbt, documentation/AGENT_RULES.md, documentation/historique.md, project/build.properties
	- Get-Date -Format "yyyy-MM-dd HH:mm"
- Resultats de verification:
	- Build: N/A
	- Tests: N/A
	- Notes: modifications documentaires uniquement
- Risques/impacts:
	- Le plan suppose une execution disciplinee des revues croisees et points d'integration.
	- Le PDF projet n'a pas ete parse automatiquement dans cet environnement.
- Prochaines actions recommandees:
	- Valider collectivement les owners A/B/C/D avec les noms reels.
	- Creer les issues GitHub et branches associees avant demarrage implementation.

### [2026-03-27 10:05] - Mise a jour regles push et champ modele
- GitHub: @MonsieurNikko
- Branche: feature/documentation-agent-rules
- Contexte/tache: aligner la gouvernance documentaire avec les nouvelles consignes (push obligatoire, champ modele)
- Fichiers modifies:
	- documentation/AGENT_RULES.md
	- documentation/historique.md
	- README.md
- Changements detailles:
	- Ajout des regles de push obligatoires dans la charte FR/EN.
	- Remplacement du champ "Nom complet" par "Modele" dans l'historique et la documentation associee.
	- Mise a jour des liens et checklists README vers le dossier documentation.
- Commandes executees:
	- git checkout -b feature/documentation-agent-rules
	- git add README.md documentation/AGENT_RULES.md documentation/historique.md
	- git commit -m "docs: align agent rules and history template"
	- git push origin feature/documentation-agent-rules
- Resultats de verification:
	- Build: N/A
	- Tests: N/A
	- Notes: modifications documentaires uniquement
- Risques/impacts:
	- Faible risque, impact limite a la gouvernance documentaire.
- Prochaines actions recommandees:
	- Ouvrir une Pull Request pour relecture equipe.

### [2026-03-27 09:40] - Initialisation du template historique
- GitHub: @MonsieurNikko
- Branche: main
- Contexte/tache: mise en place du format officiel de tracabilite pour le projet
- Fichiers modifies:
	- documentation/historique.md
- Changements detailles:
	- Ajout d'un template standard obligatoire pour toutes les futures interventions.
	- Definition des champs minimaux (auteur, type, date/heure, fichiers, commandes, resultats).
- Commandes executees:
	- N/A
- Resultats de verification:
	- Build: N/A
	- Tests: N/A
	- Notes: fichier de documentation uniquement
- Risques/impacts:
	- Faible risque, modification documentaire seulement.
- Prochaines actions recommandees:
	- Appliquer ce format a chaque nouveau changement.
