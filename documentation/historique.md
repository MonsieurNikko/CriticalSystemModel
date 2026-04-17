# Historique des changements - CriticalSystemModel

Ce fichier est obligatoire pour la tracabilite des interventions IA et humaines.
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
