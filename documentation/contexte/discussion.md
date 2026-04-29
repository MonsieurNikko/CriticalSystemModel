 Ready to code?                                                                           
                                                                                                                                                                                                            
 Here is Claude's plan:                                                                                                                                                                                     
╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌
 Plan d'expansion M14 - Canton + Quai + Portes palières (PSD)                                                                                                                                               
                                                                                                                                                                                                          
 Contexte

 Le projet actuel modélise 2 trains qui se disputent 1 tronçon partagé. Le modèle Petri est compact (7 places, 6 transitions, 8 marquages atteignables) et défendable, mais l'utilisateur trouve le projet
 "court" pour un rendu académique. Il a explicitement demandé un modèle plus M14-réaliste, plus critique, en s'inspirant de la réalité d'un métro automatique sans conducteur.

 L'expansion proposée intègre les portes palières (PSD - Platform Screen Doors), qui sont la signature visuelle et la propriété de sécurité la plus critique de la M14 : elles empêchent les chutes des
 voyageurs sur les voies. Le modèle passe de "2 trains, 1 tronçon" à "2 trains, 1 canton de signalisation, 1 quai avec portes palières". Cela ajoute 2 invariants de sûreté critiques au sens du cahier des
  charges :

 1. Sûreté PSD ouverture : les portes palières ne s'ouvrent QUE si un train est à quai à l'arrêt (sinon = chute mortelle).
 2. Sûreté PSD démarrage : un train ne peut quitter le quai QUE si les portes sont fermées (sinon = voyageur écrasé/coincé).

 Le modèle Petri grossit modérément (12 places, 10 transitions, ~15-20 marquages) et reste énumérable manuellement avec aide de l'analyseur — explicable à l'oral pour des étudiants, conforme à la
 contrainte du sujet.

 L'utilisateur veut toute la doc rédigée AVANT le code, conforme à la stratégie déjà établie ("Petri = source de vérité, code suit").

 Deadline : 4 mai 2026 (5 jours restants depuis le 29 avril).

 ---
 Modèle Petri étendu — Spécification

 Places (12, dont 5 nouvelles)

 Ressources globales (4) :
 - Canton_libre (existant) — section de voie entre amont et station
 - Quai_libre (NEW) — quai d'arrêt en station
 - Portes_fermees (NEW) — état des portes palières fermées (initial=1)
 - Portes_ouvertes (NEW) — état des portes palières ouvertes (initial=0)

 Train 1 (4 places, dont 1 nouvelle) :
 - T1_hors (existant), T1_attente (existant)
 - T1_sur_canton (renommé depuis T1_sur_troncon)
 - T1_a_quai (NEW)

 Train 2 (4 places, dont 1 nouvelle) — symétrique de T1.

 Transitions (10, dont 6 nouvelles ou modifiées)

 ┌────────────────────────────┬───────────────────────────────────────┬─────────────────────────────────────┬─────────────────────────────────────────────────────────┐
 │         Transition         │                  Pré                  │                Post                 │                       Sens métier                       │
 ├────────────────────────────┼───────────────────────────────────────┼─────────────────────────────────────┼─────────────────────────────────────────────────────────┤
 │ T1_demande (existant)      │ T1_hors                               │ T1_attente                          │ Train 1 demande l'accès                                 │
 ├────────────────────────────┼───────────────────────────────────────┼─────────────────────────────────────┼─────────────────────────────────────────────────────────┤
 │ T2_demande (existant)      │ T2_hors                               │ T2_attente                          │ Train 2 demande l'accès                                 │
 ├────────────────────────────┼───────────────────────────────────────┼─────────────────────────────────────┼─────────────────────────────────────────────────────────┤
 │ T1_entree_canton (renommé) │ T1_attente, Canton_libre              │ T1_sur_canton                       │ Train 1 entre dans le canton                            │
 ├────────────────────────────┼───────────────────────────────────────┼─────────────────────────────────────┼─────────────────────────────────────────────────────────┤
 │ T2_entree_canton (renommé) │ T2_attente, Canton_libre              │ T2_sur_canton                       │ idem T2                                                 │
 ├────────────────────────────┼───────────────────────────────────────┼─────────────────────────────────────┼─────────────────────────────────────────────────────────┤
 │ T1_arrivee_quai (NEW)      │ T1_sur_canton, Quai_libre             │ T1_a_quai, Canton_libre             │ Train 1 sort du canton, arrive au quai                  │
 ├────────────────────────────┼───────────────────────────────────────┼─────────────────────────────────────┼─────────────────────────────────────────────────────────┤
 │ T2_arrivee_quai (NEW)      │ T2_sur_canton, Quai_libre             │ T2_a_quai, Canton_libre             │ idem T2                                                 │
 ├────────────────────────────┼───────────────────────────────────────┼─────────────────────────────────────┼─────────────────────────────────────────────────────────┤
 │ Ouverture_portes (NEW)     │ Portes_fermees, T1_a_quai ⊕ T2_a_quai │ Portes_ouvertes, train à quai       │ Portes palières s'ouvrent (uniquement si train présent) │
 ├────────────────────────────┼───────────────────────────────────────┼─────────────────────────────────────┼─────────────────────────────────────────────────────────┤
 │ Fermeture_portes (NEW)     │ Portes_ouvertes, train à quai         │ Portes_fermees, train à quai        │ Portes palières se ferment                              │
 ├────────────────────────────┼───────────────────────────────────────┼─────────────────────────────────────┼─────────────────────────────────────────────────────────┤
 │ T1_depart_quai (NEW)       │ T1_a_quai, Portes_fermees             │ T1_hors, Quai_libre, Portes_fermees │ Train 1 quitte le quai (portes fermées requis)          │
 ├────────────────────────────┼───────────────────────────────────────┼─────────────────────────────────────┼─────────────────────────────────────────────────────────┤
 │ T2_depart_quai (NEW)       │ T2_a_quai, Portes_fermees             │ T2_hors, Quai_libre, Portes_fermees │ idem T2                                                 │
 └────────────────────────────┴───────────────────────────────────────┴─────────────────────────────────────┴─────────────────────────────────────────────────────────┘

 Note : Ouverture_portes est dédoublée en deux transitions effectives (OuverturePortesT1, OuverturePortesT2) car en Petri ordinaire, on ne peut pas avoir une garde "OU" sur les places d'entrée. Idem pour
  Fermeture_portes. Cela porte le total à 12 transitions effectives.

 Marquage initial M0

 Canton_libre=1, Quai_libre=1, Portes_fermees=1, Portes_ouvertes=0,
 T1_hors=1, T1_attente=0, T1_sur_canton=0, T1_a_quai=0,
 T2_hors=1, T2_attente=0, T2_sur_canton=0, T2_a_quai=0
 Total : 5 jetons en circulation.

 Invariants à prouver

 1. Invariant canton (existant) : T1_sur_canton + T2_sur_canton + Canton_libre = 1
 2. Invariant quai (NEW) : T1_a_quai + T2_a_quai + Quai_libre = 1
 3. Invariant portes (NEW) : Portes_fermees + Portes_ouvertes = 1
 4. Sûreté PSD ouverture (NEW, CRITIQUE) : Portes_ouvertes = 1 ⟹ T1_a_quai + T2_a_quai = 1 (les portes palières ne s'ouvrent que si un train est à quai)
 5. Sûreté PSD démarrage (NEW, CRITIQUE) : tout marquage M tel qu'une transition Ti_depart_quai est tirable ⟹ M(Portes_fermees) = 1
 6. Cohérence par train (étendu) : Ti_hors + Ti_attente + Ti_sur_canton + Ti_a_quai = 1

 Espace d'états attendu : 15-20 marquages (à confirmer par l'analyseur).

 ---
 Phase A — Documentation (à faire AVANT le code)

 A.1 Source de vérité formelle

 Fichier : petri/petri-troncon.md (réécriture)

 - Renommer le titre : "Réseau de Petri — Canton + Quai + PSD M14"
 - Sections 1-12 : actualiser avec 12 places, 12 transitions effectives
 - Section 5 : prouver les 3 invariants de ressource (canton, quai, portes) par induction
 - Nouvelle section 6 : démonstration des 2 invariants critiques de sûreté PSD avec analyse case-par-case sur chaque transition
 - Section 7 : nouvel espace d'états attendu (~15-20 marquages, à pré-énumérer)
 - Section 8 : table de correspondance étendue avec les 8 messages Akka (4 nouveaux : ArriveeQuai, DepartQuai, OuverturePortes, FermeturePortes)

 A.2 Lexique et coordination

 Fichier : documentation/gouvernance/lexique.md

 - Section 1 : ajouter le 4ème état des trains (a_quai → comportementAQuai)
 - Section 2 : ajouter les états du QuaiController (quaiLibre, quaiOccupe)
 - Nouvelle section 2bis : états du GestionnairePortes (portesFermees, portesOuvertes)
 - Section 3 : protocole étendu à 8 messages
 - Section 4 : nouveau glossaire entrées (PSD, canton, quai, ATP)

 Fichier : documentation/gouvernance/protocole-coordination.md

 - Section 2 (verrouillage) : passer de "4 messages" à "8 messages", de "7 places" à "12 places"
 - Section 4 (Q&A anticipées) : ajouter Q11-Q15 sur le quai et les portes
 - Mettre à jour la table des sources de vérité

 A.3 Carnet de preuves manuelles

 Fichier : documentation/livrables/preuves-manuelles.md (mise à jour majeure)

 - Tâche 1 : réénumérer l'espace d'états (15-20 marquages au lieu de 8) avec template enrichi
 - Tâche 2 : invariants de ressource (3 à vérifier au lieu d'1)
 - Nouvelle tâche 2bis : invariants critiques PSD (cas par cas sur chaque transition)
 - Tâche 3 : cohérence par train sur 4 états
 - Tâche 4 : absence de deadlock (énumération étendue)
 - Tâche 5 : LTL — formalisation des nouvelles propriétés PSD :
   - Safety PSD : G (Portes_ouvertes ⟹ T1_a_quai ∨ T2_a_quai)
   - Safety démarrage : G (X(Ti_hors) ∧ Ti_a_quai ⟹ Portes_fermees)
 - Tâche 6 : argumentation Liveness sous fairness (étendue : 2 ressources critiques)
 - Tâche 7 : nouveau diagramme d'espace d'états (graphe à dessiner)

 A.4 Comparaison Akka vs Petri

 Fichier : documentation/livrables/comparaison.md

 - Réécriture des 3 scénarios pour le modèle étendu :
   - Scénario 1 (nominal) : 1 train fait le cycle complet hors → canton → quai → portes ouvertes → portes fermées → repart
   - Scénario 2 (concurrence) : 2 trains se disputent canton ET quai
   - Scénario 3 (sûreté PSD) : tentative invalide (portes qui essaient de s'ouvrir sans train) — démonstration que la transition n'est pas tirable
 - Table de correspondance étendue à 8 messages

 A.5 Rapport de vérification (squelette)

 Fichier : documentation/livrables/rapport.md

 - Section 3 : architecture Akka étendue (4 acteurs : 2 Trains + SectionController + QuaiController + GestionnairePortes)
 - Section 4 : modèle Petri étendu (12 places, 12 transitions)
 - Nouvelle section 5.5 : preuves de sûreté PSD (les 2 invariants critiques)
 - Section 7 : limites (notamment que le modèle PSD est simplifié — en vrai il y a synchro entre portes train + portes palières)

 A.6 Documentation de pilotage

 Fichiers :
 - README.md — actualiser le résumé et la liste des livrables
 - documentation/START-ICI.md — actualiser la structure du repo et l'état des livrables
 - documentation/suivi/PLAN.md — réécrire les phases restantes pour l'expansion
 - documentation/contexte/recadrage-m14-troncon-critique.md — ajouter une section "Extension Phase 7 : intégration PSD pour réalisme M14" justifiant pourquoi cette extension reste défendable
 - documentation/suivi/historique.md — entrée massive datée du démarrage de l'expansion

 A.7 Bibliographie

 Fichier : documentation/livrables/biblio.md

 - Ajouter 2 sources sur les PSD ferroviaires :
   - IEEE std 1474 (CBTC standard) — référence pour automatic train control
   - RATP / dossier presse M14 — référence d'origine pour les portes palières

 ---
 Phase B — Code (après validation des docs)

 B.1 Protocole étendu

 Fichier : src/main/scala/m14/troncon/Protocol.scala (modification)

 Ajouter 4 nouveaux messages :
 - ArriveeQuai(emetteur, repondreA) — train demande au QuaiController
 - DepartQuai(emetteur) — train quitte le quai
 - OuverturePortes(emetteur) — Train demande au GestionnairePortes
 - FermeturePortes(emetteur) — train demande la fermeture

 Total : 8 messages (4 anciens + 4 nouveaux).

 B.2 Nouveaux acteurs

 Fichier nouveau : src/main/scala/m14/troncon/QuaiController.scala

 Modèle identique à SectionController (FIFO, 2 états : quaiLibre, quaiOccupe). Arbitre du quai.

 Fichier nouveau : src/main/scala/m14/troncon/GestionnairePortes.scala

 Acteur qui gère les portes palières. 2 états : portesFermees, portesOuvertes. Garde de sûreté : refuse l'ouverture si aucun train n'est à quai.

 B.3 Train enrichi

 Fichier : src/main/scala/m14/troncon/Train.scala (modification majeure)

 Nouvelle machine à 4 états :
 - comportementHors (existant)
 - comportementEnAttente (existant)
 - comportementSurCanton (renommé)
 - comportementAQuai (NEW) — gère le cycle ouverture/fermeture portes puis départ

 B.4 SectionController renommé

 Fichier : src/main/scala/m14/troncon/SectionController.scala (mise à jour)

 Renommer en CantonController ou laisser le nom pour éviter de casser les imports — décision : garder le nom mais renommer les états internes (etatLibre → cantonLibre, etc.). Adapter pour que la sortie
 du canton ne libère pas immédiatement le train : il faut maintenant qu'il transite par le quai.

 B.5 Modèle Petri en code

 Fichier : src/main/scala/m14/petri/PetriNet.scala (extension)

 - Ajouter les 5 nouvelles places (Quai_libre, Portes_fermees, Portes_ouvertes, T1_a_quai, T2_a_quai)
 - Ajouter les 6 nouvelles transitions (T1_arrivee_quai, T2_arrivee_quai, OuverturePortesT1, OuverturePortesT2, FermeturePortesT1, FermeturePortesT2, T1_depart_quai, T2_depart_quai — soit 8 nouvelles
 transitions, certaines remplaçant des anciennes)
 - Mettre à jour le marquage initial

 B.6 Analyseur étendu

 Fichier : src/main/scala/m14/petri/Analyseur.scala (extension)

 - Ajouter verifierInvariantQuai (nouveau)
 - Ajouter verifierInvariantPortes (nouveau)
 - Ajouter verifierSurteOuverturePortes (CRITIQUE — vérifie que dans tout marquage, Portes_ouvertes=1 ⟹ ∃Ti_a_quai=1)
 - Adapter main() pour afficher les nouveaux invariants
 - Cible : 15-20 marquages atteignables, 0 deadlock, tous invariants OK

 B.7 Démo principale

 Fichier : src/main/scala/m14/Main.scala (mise à jour)

 - 3 nouveaux scénarios démonstrant le cycle complet canton → quai → portes
 - Ajout d'un scénario "tentative d'ouverture invalide" pour montrer que la safety PSD bloque

 ---
 Phase C — Tests

 C.1 Tests unitaires nouveaux

 Fichier nouveau : src/test/scala/m14/troncon/QuaiControllerSpec.scala

 3 scénarios miroir de SectionControllerSpec.

 Fichier nouveau : src/test/scala/m14/troncon/GestionnairePortesSpec.scala

 Tests critiques :
 - Refuse l'ouverture si aucun train à quai
 - Accepte l'ouverture si un train à quai
 - Train ne peut pas partir si portes ouvertes
 - Train peut partir si portes fermées

 C.2 Tests existants à adapter

 Fichier : src/test/scala/m14/troncon/TrainSpec.scala — adapter au cycle 4 états.

 Fichier : src/test/scala/m14/petri/AnalyseurSpec.scala — actualiser le nombre attendu de marquages, ajouter tests pour les nouveaux invariants.

 C.3 Tests d'intégration (NEW, optionnel mais conseillé)

 Fichier nouveau : src/test/scala/m14/integration/SimulationCompareeSpec.scala

 Test qui lance les acteurs Akka sur le scénario 2 et compare avec la sortie de l'analyseur Petri. Renforce le livrable L6.

 ---
 Phase D — Vérification et finition

 D.1 Lancement de l'analyseur

 sbt "runMain m14.petri.Analyseur"

 Sortie attendue : 15-20 marquages, tous invariants PASS, 0 deadlock, exclusion mutuelle PASS, sûreté PSD PASS.

 D.2 Insertion dans comparaison.md

 Coller la sortie de l'analyseur dans la section 6 et confirmer que les 15-20 marquages prédits manuellement (Phase A.3) correspondent.

 D.3 Compléter le rapport

 Remplir les 8 sections de rapport.md avec :
 - Sortie de l'analyseur
 - Diagramme d'espace d'états
 - Justifications LTL
 - Bibliographie référencée

 D.4 Push et merge

 Sur une branche feature/m14-extension-quai-psd, commits atomiques par phase :
 1. docs(petri+lexique): extension canton+quai+psd, nouvelle source de verite
 2. docs(carnet+comparaison+rapport): ajout invariants PSD et nouvelle enumeration
 3. feat(troncon): QuaiController + GestionnairePortes + Train 4 etats
 4. feat(petri): extension PetriNet + Analyseur avec invariants PSD
 5. test: QuaiController + GestionnairePortes + adaptations existantes
 6. feat(integration): SimulationCompareeSpec

 Puis merge --no-ff vers main.

 ---
 Fichiers critiques à modifier (récapitulatif)

 ┌─────────────────────────────────────────────────────────────┬───────────────────────────┬───────┐
 │                           Fichier                           │          Action           │ Phase │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ petri/petri-troncon.md                                      │ Réécriture majeure        │ A.1   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ documentation/gouvernance/lexique.md                        │ Extension                 │ A.2   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ documentation/gouvernance/protocole-coordination.md         │ Mise à jour verrouillages │ A.2   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ documentation/livrables/preuves-manuelles.md                │ Réécriture majeure        │ A.3   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ documentation/livrables/comparaison.md                      │ Réécriture                │ A.4   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ documentation/livrables/rapport.md                          │ Extension structure       │ A.5   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ README.md                                                   │ Actualisation             │ A.6   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ documentation/START-ICI.md                                  │ Actualisation             │ A.6   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ documentation/suivi/PLAN.md                                 │ Réécriture phases         │ A.6   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ documentation/contexte/recadrage-m14-troncon-critique.md    │ Section ajoutée           │ A.6   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ documentation/suivi/historique.md                           │ Entrée massive            │ A.6   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ documentation/livrables/biblio.md                           │ +2 sources                │ A.7   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/main/scala/m14/troncon/Protocol.scala                   │ +4 messages               │ B.1   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/main/scala/m14/troncon/QuaiController.scala             │ NEW                       │ B.2   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/main/scala/m14/troncon/GestionnairePortes.scala         │ NEW                       │ B.2   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/main/scala/m14/troncon/Train.scala                      │ Extension 4 états         │ B.3   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/main/scala/m14/troncon/SectionController.scala          │ Adaptation                │ B.4   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/main/scala/m14/petri/PetriNet.scala                     │ Extension                 │ B.5   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/main/scala/m14/petri/Analyseur.scala                    │ +invariants critiques     │ B.6   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/main/scala/m14/Main.scala                               │ Nouveaux scénarios        │ B.7   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/test/scala/m14/troncon/QuaiControllerSpec.scala         │ NEW                       │ C.1   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/test/scala/m14/troncon/GestionnairePortesSpec.scala     │ NEW                       │ C.1   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/test/scala/m14/troncon/TrainSpec.scala                  │ Adaptation                │ C.2   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/test/scala/m14/petri/AnalyseurSpec.scala                │ Adaptation                │ C.2   │
 ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
 │ src/test/scala/m14/integration/SimulationCompareeSpec.scala │ NEW (optionnel)           │ C.3   │
 └─────────────────────────────────────────────────────────────┴───────────────────────────┴───────┘

 Total : 12 fichiers de doc, 8 fichiers de code, 5 fichiers de test = 25 fichiers touchés.

 ---
 Estimation et planning

 ┌────────────────────────────────────┬──────────┬──────────────────┬────────────────────────────────────────────┐
 │               Phase                │  Charge  │       Jour       │                   Risque                   │
 ├────────────────────────────────────┼──────────┼──────────────────┼────────────────────────────────────────────┤
 │ A — Documentation (12 fichiers)    │ 1.5 jour │ J1-J2            │ Faible (rédaction structurée)              │
 ├────────────────────────────────────┼──────────┼──────────────────┼────────────────────────────────────────────┤
 │ B — Code Akka + Petri (8 fichiers) │ 1.5 jour │ J3-J4            │ Moyen (renommages, adaptations en cascade) │
 ├────────────────────────────────────┼──────────┼──────────────────┼────────────────────────────────────────────┤
 │ C — Tests (5 fichiers)             │ 0.5 jour │ J4               │ Faible (modèles existants à dupliquer)     │
 ├────────────────────────────────────┼──────────┼──────────────────┼────────────────────────────────────────────┤
 │ D — Rapport final + push           │ 1 jour   │ J5               │ Faible                                     │
 ├────────────────────────────────────┼──────────┼──────────────────┼────────────────────────────────────────────┤
 │ Buffer                             │ 0.5 jour │ J5               │ —                                          │
 ├────────────────────────────────────┼──────────┼──────────────────┼────────────────────────────────────────────┤
 │ Total                              │ 5 jours  │ 29 avril → 3 mai │ Rendu 4 mai                                │
 └────────────────────────────────────┴──────────┴──────────────────┴────────────────────────────────────────────┘

 Charge serrée mais cadrée. Risque principal : énumération manuelle de 15-20 marquages (long mais faisable avec aide de l'analyseur).

 ---
 Vérification end-to-end

 À la fin du sprint, les commandes suivantes doivent toutes passer :

 sbt clean compile        # Build sans erreur
 sbt test                 # Tests : 25-30 verts (vs 22 actuels)
 sbt "runMain m14.petri.Analyseur"  # Sortie : 15-20 marquages, 5 invariants PASS, 0 deadlock

 Et les tests manuels (oraux) :
 - L'utilisateur peut expliquer en 5 min : pourquoi PSD est critique sur M14, comment le Petri le modélise, pourquoi les 2 invariants de sûreté PSD tiennent.
 - L'utilisateur peut casser un test volontairement (par ex. permettre Ouverture_portes sans train à quai) et constater que l'analyseur détecte la violation de sûreté.

 ---
 Décisions verrouillées par ce plan

 - Pas de 3ème train (extension verticale rejetée — le Petri exploserait combinatoirement).
 - Pas de 2ème canton en série (extension horizontale rejetée — moins critique que PSD).
 - Pas de timeout / fairness explicite (déjà discuté et hors scope).
 - Pas de tolérance aux pannes (hors scope du recadrage).
 - Pas de modèle temporel Petri (hors scope).

 Le périmètre est cadré sur l'ajout de PSD comme propriété de sûreté critique M14, le reste reste tel quel.
╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌

 Claude has written up a plan and is ready to execute. Would you like to proceed?

 ❯ 1. Yes, auto-accept edits
   2. Yes, manually approve edits
   3. No, refine with Ultraplan on Claude Code on the web
   4. Tell Claude what to change
      shift+tab to approve with this feedback

 ctrl-g to edit in Vim · ~/.claude/plans/relis-tous-les-fichiers-jazzy-falcon.md

     ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
     │ src/test/scala/m14/troncon/TrainSpec.scala                  │ Adaptation                │ C.2   │
     ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
     │ src/test/scala/m14/petri/AnalyseurSpec.scala                │ Adaptation                │ C.2   │
     ├─────────────────────────────────────────────────────────────┼───────────────────────────┼───────┤
     │ src/test/scala/m14/integration/SimulationCompareeSpec.scala │ NEW (optionnel)           │ C.3   │
     └─────────────────────────────────────────────────────────────┴───────────────────────────┴───────┘

     Total : 12 fichiers de doc, 8 fichiers de code, 5 fichiers de test = 25 fichiers touchés.

     ---
     Estimation et planning

     ┌────────────────────────────────────┬──────────┬──────────────────┬────────────────────────────────────────────┐
     │               Phase                │  Charge  │       Jour       │                   Risque                   │
     ├────────────────────────────────────┼──────────┼──────────────────┼────────────────────────────────────────────┤
     │ A — Documentation (12 fichiers)    │ 1.5 jour │ J1-J2            │ Faible (rédaction structurée)              │
     ├────────────────────────────────────┼──────────┼──────────────────┼────────────────────────────────────────────┤
     │ B — Code Akka + Petri (8 fichiers) │ 1.5 jour │ J3-J4            │ Moyen (renommages, adaptations en cascade) │
     ├────────────────────────────────────┼──────────┼──────────────────┼────────────────────────────────────────────┤
     │ C — Tests (5 fichiers)             │ 0.5 jour │ J4               │ Faible (modèles existants à dupliquer)     │
     ├────────────────────────────────────┼──────────┼──────────────────┼────────────────────────────────────────────┤
     │ D — Rapport final + push           │ 1 jour   │ J5               │ Faible                                     │
     ├────────────────────────────────────┼──────────┼──────────────────┼────────────────────────────────────────────┤
     │ Buffer                             │ 0.5 jour │ J5               │ —                                          │
     ├────────────────────────────────────┼──────────┼──────────────────┼────────────────────────────────────────────┤
     │ Total                              │ 5 jours  │ 29 avril → 3 mai │ Rendu 4 mai                                │
     └────────────────────────────────────┴──────────┴──────────────────┴────────────────────────────────────────────┘

     Charge serrée mais cadrée. Risque principal : énumération manuelle de 15-20 marquages (long mais faisable avec aide de l'analyseur).

     ---
     Vérification end-to-end

     À la fin du sprint, les commandes suivantes doivent toutes passer :

     sbt clean compile        # Build sans erreur
     sbt test                 # Tests : 25-30 verts (vs 22 actuels)
     sbt "runMain m14.petri.Analyseur"  # Sortie : 15-20 marquages, 5 invariants PASS, 0 deadlock

     Et les tests manuels (oraux) :
     - L'utilisateur peut expliquer en 5 min : pourquoi PSD est critique sur M14, comment le Petri le modélise, pourquoi les 2 invariants de sûreté PSD tiennent.
     - L'utilisateur peut casser un test volontairement (par ex. permettre Ouverture_portes sans train à quai) et constater que l'analyseur détecte la violation de sûreté.

     ---
     Décisions verrouillées par ce plan

     - Pas de 3ème train (extension verticale rejetée — le Petri exploserait combinatoirement).
     - Pas de 2ème canton en série (extension horizontale rejetée — moins critique que PSD).
     - Pas de timeout / fairness explicite (déjà discuté et hors scope).
     - Pas de tolérance aux pannes (hors scope du recadrage).
     - Pas de modèle temporel Petri (hors scope).

     Le périmètre est cadré sur l'ajout de PSD comme propriété de sûreté critique M14, le reste reste tel quel.

⏺ Plan validé. J'attaque. Phase A d'abord (docs avant le code, comme tu l'as demandé). Je commence par créer une branche puis je réécris la source de vérité formelle (petri/petri-troncon.md).
                                                                                          
⏺ Bash(git checkout main && git pull origin main 2>&1 | tail -3 && git status -sb && git checkout -b feature/m14-extension-quai-psd)
  ⎿  Already on 'main'                                                                                                                                                                                      
     Your branch is up to date with 'origin/main'.
     From github.com:MonsieurNikko/CriticalSystemModel                                                                                                                                                      
     … +5 lines (ctrl+o to expand)                                                                                                                                                                        
                                                                                                                                                                                                            
  Read 1 file (ctrl+o to expand)                                                                                                                                                                            
                                                                                          
⏺ Write(petri/petri-troncon.md)                                                                                                                                                                             
  ⎿  Added 231 lines, removed 102 lines                                                                                                                                                                     
      1 -# Reseau de Petri - Troncon partage M14                                                                                                                                                            
      1 +# Reseau de Petri - Canton + Quai + Portes palieres (M14)                                                                                                                                          
      2                                                                                                                                                                                                     
      3 -> Livrable L3 du cahier des charges. Modele formel minimal du controle d'acces concurrent de 2 trains a 1 troncon partage.                                                                         
      3 +> Livrable L3 du cahier des charges. Modele formel d'un sous-systeme critique du metro M14 automatique : controle d'acces concurrent de 2 trains a un canton de signalisation, suivi d'un ar       
        +ret en station avec gestion des portes palieres (PSD - Platform Screen Doors).                                                                                                                   
      4  >
      5  > Ce fichier est la **source de verite du vocabulaire** : les noms des places et transitions doivent etre repris a l'identique dans le code Scala (acteurs Akka et analyseur Petri) et dans
         les tests. Toute divergence de nommage est un bug de coherence.
      6  
     ...
        8  
        9  ## 1) Vue d'ensemble
       10  
       11 -Le reseau modelise le mecanisme d'exclusion mutuelle decrit dans `documentation/contexte/recadrage-m14-troncon-critique.md`. Il est volontairement compact (7 places, 6 transitions) pour 
          -rester analysable a la main et par un analyseur Scala maison (livrable L5).                                                                                                               
       11 +Le reseau modelise deux mecanismes critiques imbriques d'un metro automatique sans conducteur :                                                                                           
       12  
       13 -**Scope strict :**                                                                                                                                                                        
       13 +1. **Block signaling** : un canton de signalisation que les 2 trains se disputent en exclusion mutuelle.                                                                                  
       14 +2. **PSD - Portes palieres** : a l'arrivee en station (sur le quai), des portes palieres synchronisees protegent les voyageurs des chutes sur les voies. **Les portes ne s'ouvrent que si 
          +un train est a l'arret a quai**, et **un train ne peut quitter le quai que portes fermees**.                                                                                              
       15 +                                                                                                                                                                                          
       16 +Cette extension par rapport au modele initial (1 troncon seul) ajoute deux **proprietes de surete CRITIQUES** au sens reglementaire : une violation peut entrainer une chute mortelle (por
          +te palieres ouverte sans train) ou un voyageur ecrase (depart avec portes ouvertes). C'est exactement ce que demande le cahier des charges sur les "systemes critiques".                  
       17 +                                                                                                                                                                                          
       18 +**Scope strict** :                                                                                                                                                                        
       19  - 2 trains : T1, T2
       15 -- 1 troncon partage                                                                                                                                                                       
       16 -- 1 controleur centralise (implicite : il est porte par les transitions, pas par une place dediee)                                                                                        
       20 +- 1 canton de signalisation (entre amont et station)                                                                                                                                      
       21 +- 1 quai en station                                                                                                                                                                       
       22 +- 1 paire de portes palieres (modelisee comme un etat global du quai)                                                                                                                     
       23 +- 1 controleur de canton + 1 controleur de quai + 1 gestionnaire de portes                                                                                                                
       24  - Aucune notion de panne, de famine prolongee, ni de priorite
       25  
       26 +Le reseau est analysable a la main (15-18 marquages atteignables prevus, a confirmer par l'analyseur Scala).                                                                              
       27 +                                                                                                                                                                                          
       28  ---
       29  
       21 -## 2) Places (7)                                                                                                                                                                          
       30 +## 2) Places (12)                                                                                                                                                                         
       31  
       23 -| Place | Signification | Marquage initial |                                                                                                                                              
       24 -|-------|---------------|-----------------:|                                                                                                                                              
       25 -| `Troncon_libre`     | Le troncon partage est libre, aucun train dessus | 1 |                                                                                                            
       26 -| `T1_hors`           | Train 1 est en dehors de la zone d'approche      | 1 |                                                                                                            
       27 -| `T1_attente`        | Train 1 a demande l'acces et attend une reponse  | 0 |                                                                                                            
       28 -| `T1_sur_troncon`    | Train 1 occupe le troncon partage                | 0 |                                                                                                            
       29 -| `T2_hors`           | Train 2 est en dehors de la zone d'approche      | 1 |                                                                                                            
       30 -| `T2_attente`        | Train 2 a demande l'acces et attend une reponse  | 0 |                                                                                                            
       31 -| `T2_sur_troncon`    | Train 2 occupe le troncon partage                | 0 |                                                                                                            
       32 +### Ressources globales (4)                                                                                                                                                               
       33  
       33 -Marquage initial **M0** :                                                                                                                                                                 
       34 +| Place             | Signification                                           | Marquage initial |                                                                                        
       35 +|-------------------|---------------------------------------------------------|-----------------:|                                                                                        
       36 +| `Canton_libre`    | Le canton de signalisation est libre                    | 1                |                                                                                        
       37 +| `Quai_libre`      | Le quai en station est libre                            | 1                |                                                                                        
       38 +| `Portes_fermees`  | Les portes palieres sont fermees                        | 1                |                                                                                        
       39 +| `Portes_ouvertes` | Les portes palieres sont ouvertes                       | 0                |                                                                                        
       40 +                                                                                                                                                                                          
       41 +### Etats du Train 1 (4)                                                                                                                                                                  
       42 +                                                                                                                                                                                          
       43 +| Place              | Signification                                                          | Marquage initial |                                                                        
       44 +|--------------------|------------------------------------------------------------------------|-----------------:|                                                                        
       45 +| `T1_hors`          | Train 1 hors zone (en amont du canton ou apres avoir quitte le quai)   | 1                |                                                                        
       46 +| `T1_attente`       | Train 1 a demande l'acces au canton et attend                          | 0                |                                                                        
       47 +| `T1_sur_canton`    | Train 1 occupe le canton de signalisation                              | 0                |                                                                        
       48 +| `T1_a_quai`        | Train 1 est a l'arret en station, sur le quai                          | 0                |                                                                        
       49 +                                                                                                                                                                                          
       50 +### Etats du Train 2 (4)                                                                                                                                                                  
       51 +                                                                                                                                                                                          
       52 +Symetrique du Train 1 : `T2_hors=1`, `T2_attente=0`, `T2_sur_canton=0`, `T2_a_quai=0`.                                                                                                    
       53 +                                                                                                                                                                                          
       54 +### Marquage initial M0 (5 jetons en circulation)                                                                                                                                         
       55 +                                                                                                                                                                                          
       56  ```
       35 -M0 = (Troncon_libre=1, T1_hors=1, T1_attente=0, T1_sur_troncon=0,                                                                                                                         
       36 -                       T2_hors=1, T2_attente=0, T2_sur_troncon=0)                                                                                                                         
       57 +M0 = (Canton_libre=1, Quai_libre=1, Portes_fermees=1, Portes_ouvertes=0,                                                                                                                  
       58 +      T1_hors=1, T1_attente=0, T1_sur_canton=0, T1_a_quai=0,                                                                                                                              
       59 +      T2_hors=1, T2_attente=0, T2_sur_canton=0, T2_a_quai=0)                                                                                                                              
       60  ```
       61  
       62  ---
       63  
       41 -## 3) Transitions (6)                                                                                                                                                                     
       64 +## 3) Transitions (12)                                                                                                                                                                    
       65  
       43 -Notation : `pre -> post` signifie consommation d'1 jeton dans chaque place de `pre`, production d'1 jeton dans chaque place de `post`.                                                    
       66 +Notation : `pre -> post` consomme 1 jeton dans chaque place de `pre`, produit 1 jeton dans chaque place de `post`.                                                                        
       67  
       45 -| Transition | Pre (consomme) | Post (produit) | Sens metier |                                                                                                                            
       46 -|------------|----------------|----------------|-------------|                                                                                                                            
       47 -| `T1_demande`              | `T1_hors`                          | `T1_attente`                        | Train 1 envoie une demande au controleur |                                       
       48 -| `T2_demande`              | `T2_hors`                          | `T2_attente`                        | Train 2 envoie une demande au controleur |                                       
       49 -| `T1_entree_autorisee`     | `T1_attente`, `Troncon_libre`      | `T1_sur_troncon`                    | Le controleur autorise Train 1 a entrer |                                        
       50 -| `T2_entree_autorisee`     | `T2_attente`, `Troncon_libre`      | `T2_sur_troncon`                    | Le controleur autorise Train 2 a entrer |                                        
       51 -| `T1_sortie_liberation`    | `T1_sur_troncon`                   | `T1_hors`, `Troncon_libre`          | Train 1 sort et libere la ressource |                                            
       52 -| `T2_sortie_liberation`    | `T2_sur_troncon`                   | `T2_hors`, `Troncon_libre`          | Train 2 sort et libere la ressource |                                            
       68 +### Cycle de mouvement Train 1 (5 transitions)                                                                                                                                            
       69  
       70 +| Transition                | Pre                              | Post                              | Sens metier                                          |                               
       71 +|---------------------------|----------------------------------|-----------------------------------|------------------------------------------------------|                               
       72 +| `T1_demande`              | `T1_hors`                        | `T1_attente`                      | Train 1 demande l'acces au canton                    |                               
       73 +| `T1_entree_canton`        | `T1_attente`, `Canton_libre`     | `T1_sur_canton`                   | Train 1 entre dans le canton (autorisation accordee) |                               
       74 +| `T1_arrivee_quai`         | `T1_sur_canton`, `Quai_libre`    | `T1_a_quai`, `Canton_libre`       | Train 1 sort du canton et arrive au quai             |                               
       75 +| `T1_depart_quai`          | `T1_a_quai`, `Portes_fermees`    | `T1_hors`, `Quai_libre`, `Portes_fermees` | Train 1 quitte le quai (portes fermees REQUIS)  |                            
       76 +                                                                                                                                                                                          
       77 +Note sur `T1_depart_quai` : la place `Portes_fermees` est **lue mais reproduite** (read-arc emule via consommation+production). La transition n'est tirable que si `Portes_fermees=1`.    
       78 +                                                                                                                                                                                          
       79 +### Cycle Train 2 (4 transitions)                                                                                                                                                         
       80 +                                                                                                                                                                                          
       81 +`T2_demande`, `T2_entree_canton`, `T2_arrivee_quai`, `T2_depart_quai` : symetriques de T1.                                                                                                
       82 +                                                                                                                                                                                          
       83 +### Gestion des portes palieres (4 transitions)                                                                                                                                           
       84 +                                                                                                                                                                                          
       85 +| Transition                | Pre                                              | Post                                              | Sens metier                                          
          +       |                                                                                                                                                                                  
       86 +|---------------------------|--------------------------------------------------|---------------------------------------------------|------------------------------------------------------
          +-------|                                                                                                                                                                                  
       87 +| `Ouverture_portes_T1`     | `Portes_fermees`, `T1_a_quai`                    | `Portes_ouvertes`, `T1_a_quai`                    | Portes ouvrent car T1 est arrive a quai (CRITIQUE PSD
          +)      |                                                                                                                                                                                  
       88 +| `Ouverture_portes_T2`     | `Portes_fermees`, `T2_a_quai`                    | `Portes_ouvertes`, `T2_a_quai`                    | idem T2                                              
          +       |                                                                                                                                                                                  
       89 +| `Fermeture_portes_T1`     | `Portes_ouvertes`, `T1_a_quai`                   | `Portes_fermees`, `T1_a_quai`                     | Portes ferment alors que T1 toujours a quai          
          +       |                                                                                                                                                                                  
       90 +| `Fermeture_portes_T2`     | `Portes_ouvertes`, `T2_a_quai`                   | `Portes_ouvertes`, `T2_a_quai` *(corrige -> fermees)* | idem T2 (cf rectification ci-dessous)            
          +      |                                                                                                                                                                                   
       91 +                                                                                                                                                                                          
       92 +**Correction** : `Fermeture_portes_T2` : `Portes_ouvertes`, `T2_a_quai` -> `Portes_fermees`, `T2_a_quai` (la place du train est lue+reproduite).                                          
       93 +                                                                                                                                                                                          
       94 +**Pourquoi dedoubler en T1/T2** : en Petri ordinaire, on n'a pas de "garde OU" sur les places de pre-condition. Pour exprimer "ouverture autorisee si UN train (T1 OU T2) est a quai", on 
          +instancie 2 transitions, chacune liee a un train. C'est equivalent semantiquement et reste analysable.                                                                                    
       95 +                                                                                                                                                                                          
       96 +### Total transitions                                                                                                                                                                     
       97 +                                                                                                                                                                                          
       98 +- 4 mouvement T1 (`T1_demande`, `T1_entree_canton`, `T1_arrivee_quai`, `T1_depart_quai`)                                                                                                  
       99 +- 4 mouvement T2 (symetriques)                                                                                                                                                            
      100 +- 2 ouvertures (`Ouverture_portes_T1`, `Ouverture_portes_T2`)                                                                                                                             
      101 +- 2 fermetures (`Fermeture_portes_T1`, `Fermeture_portes_T2`)                                                                                                                             
      102 +                                                                                                                                                                                          
      103 += **12 transitions effectives**.                                                                                                                                                          
      104 +                                                                                                                                                                                          
      105  ---
      106  
      107  ## 4) Schema ASCII
      108  
      109  ```
       59 -                 +--------------------+                                                                                                                                                   
       60 -                 |   Troncon_libre    |  <-- ressource partagee                                                                                                                           
       61 -                 +--------------------+                                                                                                                                                   
       62 -                    ^      |      ^                                                                                                                                                       
       63 -   T1_sortie_      /       |       \      T2_sortie_                                                                                                                                      
       64 -   liberation     /        |        \     liberation                                                                                                                                      
       65 -                 /         |         \                                                                                                                                                    
       66 -                /  T1_     |  T2_     \                                                                                                                                                   
       67 -               /  entree_  |  entree_  \                                                                                                                                                  
       68 -              /  autorisee | autorisee  \                                                                                                                                                 
       69 -             /             |             \                                                                                                                                                
       70 -   +------------+    +-----+-----+    +------------+                                                                                                                                      
       71 -   | T1_hors    |    |           |    | T2_hors    |                                                                                                                                      
       72 -   +------------+    |           |    +------------+                                                                                                                                      
       73 -        |            |           |          |                                                                                                                                             
       74 -   T1_demande        |           |     T2_demande                                                                                                                                         
       75 -        v            |           |          v                                                                                                                                             
       76 -   +------------+    |           |    +------------+                                                                                                                                      
       77 -   | T1_attente |--->|           |<---| T2_attente |                                                                                                                                      
       78 -   +------------+    |           |    +------------+                                                                                                                                      
       79 -                     v           v                                                                                                                                                        
       80 -              +----------------+  +----------------+                                                                                                                                      
       81 -              | T1_sur_troncon |  | T2_sur_troncon |                                                                                                                                      
       82 -              +----------------+  +----------------+                                                                                                                                      
      110 +                              Ressources globales                                                                                                                                         
      111 +                +---------+   +---------+   +-----------+   +-----------+                                                                                                                 
      112 +                | Canton_ |   |  Quai_  |   |  Portes_  |   |  Portes_  |                                                                                                                 
      113 +                |  libre  |   |  libre  |   |  fermees  |   |  ouvertes |                                                                                                                 
      114 +                +---------+   +---------+   +-----------+   +-----------+                                                                                                                 
      115 +                                                                                                                                                                                          
      116 +  Train 1                                                  Train 2                                                                                                                        
      117 ++----------+                                            +----------+                                                                                                                      
      118 +| T1_hors  |  <----+                            +---->  | T2_hors  |                                                                                                                      
      119 ++----------+       |                            |       +----------+                                                                                                                      
      120 +     |             | T1_depart_quai             | T2_depart_quai                                                                                                                          
      121 +T1_demande         | (consomme T1_a_quai +      |                                                                                                                                         
      122 +     v             |  Portes_fermees [read])    |       T2_demande                                                                                                                        
      123 ++--------------+   |                            |     +--------------+                                                                                                                    
      124 +| T1_attente   |   |                            |     | T2_attente   |                                                                                                                    
      125 ++--------------+   |                            |     +--------------+                                                                                                                    
      126 +     |             |                            |          |                                                                                                                              
      127 +T1_entree_canton   |                            |     T2_entree_canton                                                                                                                    
      128 +(consomme Canton_  |                            |                                                                                                                                         
      129 + libre)            |                            |                                                                                                                                         
      130 +     v             |                            |          v                                                                                                                              
      131 ++----------------+ |                            |   +----------------+                                                                                                                    
      132 +| T1_sur_canton  | |                            |   | T2_sur_canton  |                                                                                                                    
      133 ++----------------+ |                            |   +----------------+                                                                                                                    
      134 +     |             |                            |          |                                                                                                                              
      135 +T1_arrivee_quai    |                            |     T2_arrivee_quai                                                                                                                     
      136 +(consomme Quai_    |                            |                                                                                                                                         
      137 + libre, libere     |                            |                                                                                                                                         
      138 + Canton_libre)     |                            |                                                                                                                                         
      139 +     v             |                            |          v                                                                                                                              
      140 ++----------------+ |                            |   +----------------+                                                                                                                    
      141 +| T1_a_quai      |-+                            +-->| T2_a_quai      |                                                                                                                    
      142 ++----------------+                                  +----------------+                                                                                                                    
      143 +     |  ^                                                |  ^                                                                                                                             
      144 +     |  | (ouverture/fermeture portes synchro avec      |  |                                                                                                                              
      145 +     |  |  presence d'un train a quai)                  |  |                                                                                                                              
      146 +     v  |                                                v  |                                                                                                                             
      147 +   +-----------+                                       +-----------+                                                                                                                      
      148 +   |  cycle    |                                       |  cycle    |                                                                                                                      
      149 +   |  portes   |                                       |  portes   |                                                                                                                      
      150 +   +-----------+                                       +-----------+                                                                                                                      
      151  ```
      152  
       85 -Lecture : un train est toujours dans **exactement une** des trois places de sa colonne (`Ti_hors`, `Ti_attente`, `Ti_sur_troncon`). Le `Troncon_libre` est partage entre les deux colonnes
          -.                                                                                                                                                                                         
      153 +Lecture : un train est toujours dans **exactement une** des 4 places de sa colonne (`Ti_hors`, `Ti_attente`, `Ti_sur_canton`, `Ti_a_quai`). Les 4 ressources globales (canton, quai, porte
          +s ouvertes, portes fermees) sont partagees.                                                                                                                                               
      154  
      155  ---
      156  
       89 -## 5) Invariant principal (a prouver)                                                                                                                                                     
      157 +## 5) Invariants de ressource (P-invariants)                                                                                                                                              
      158  
      159 +Trois invariants structurels (preuves par induction).                                                                                                                                     
      160 +                                                                                                                                                                                          
      161 +### 5.1 Invariant canton                                                                                                                                                                  
      162 +                                                                                                                                                                                          
      163  ```
       92 -M(T1_sur_troncon) + M(T2_sur_troncon) + M(Troncon_libre) = 1                                                                                                                              
      164 +M(T1_sur_canton) + M(T2_sur_canton) + M(Canton_libre) = 1                                                                                                                                 
      165  ```
      166  
       95 -Pour tout marquage atteignable M depuis M0. Cet invariant est la pierre angulaire de la preuve d'exclusion mutuelle.                                                                      
      167 +**Preuve sur M0** : `0 + 0 + 1 = 1`. OK.                                                                                                                                                  
      168  
       97 -**Verification sur M0** : `0 + 0 + 1 = 1`. OK.                                                                                                                                            
      169 +**Preservation par transition** :                                                                                                                                                         
      170  
       99 -**Preservation par chaque transition** :                                                                                                                                                  
      171 +| Transition          | Effet sur la somme                                                        |                                                                                       
      172 +|---------------------|---------------------------------------------------------------------------|                                                                                       
      173 +| `T1_demande`        | aucun jeton sur les 3 places concernees : somme inchangee                 |                                                                                       
      174 +| `T2_demande`        | idem                                                                      |                                                                                       
      175 +| `T1_entree_canton`  | -1 sur `Canton_libre`, +1 sur `T1_sur_canton` : somme inchangee           |                                                                                       
      176 +| `T2_entree_canton`  | -1 sur `Canton_libre`, +1 sur `T2_sur_canton` : somme inchangee           |                                                                                       
      177 +| `T1_arrivee_quai`   | -1 sur `T1_sur_canton`, +1 sur `Canton_libre` : somme inchangee           |                                                                                       
      178 +| `T2_arrivee_quai`   | -1 sur `T2_sur_canton`, +1 sur `Canton_libre` : somme inchangee           |                                                                                       
      179 +| `T1_depart_quai`    | aucun jeton sur les 3 places concernees                                   |                                                                                       
      180 +| `T2_depart_quai`    | idem                                                                      |                                                                                       
      181 +| Transitions portes  | aucun jeton sur les 3 places concernees                                   |                                                                                       
      182  
      101 -| Transition | Effet sur la somme `T1_sur_troncon + T2_sur_troncon + Troncon_libre` |                                                                                                     
      102 -|------------|----------------------------------------------------------------------|                                                                                                     
      103 -| `T1_demande`           | aucun jeton sur les 3 places concernees : somme inchangee          |                                                                                           
      104 -| `T2_demande`           | aucun jeton sur les 3 places concernees : somme inchangee          |                                                                                           
      105 -| `T1_entree_autorisee`  | -1 sur `Troncon_libre`, +1 sur `T1_sur_troncon` : somme inchangee  |                                                                                           
      106 -| `T2_entree_autorisee`  | -1 sur `Troncon_libre`, +1 sur `T2_sur_troncon` : somme inchangee  |                                                                                           
      107 -| `T1_sortie_liberation` | -1 sur `T1_sur_troncon`, +1 sur `Troncon_libre` : somme inchangee  |                                                                                           
      108 -| `T2_sortie_liberation` | -1 sur `T2_sur_troncon`, +1 sur `Troncon_libre` : somme inchangee  |                                                                                           
      183 +L'invariant est preserve. **Consequence** : exclusion mutuelle sur le canton.                                                                                                             
      184  
      110 -L'invariant est preserve par toutes les transitions, donc il tient sur tout marquage atteignable.                                                                                         
      185 +### 5.2 Invariant quai                                                                                                                                                                    
      186  
      112 -**Consequence directe** : `T1_sur_troncon` et `T2_sur_troncon` ne peuvent jamais valoir 1 simultanement (sinon la somme depasserait 1). C'est l'exclusion mutuelle.                       
      187 +```                                                                                                                                                                                       
      188 +M(T1_a_quai) + M(T2_a_quai) + M(Quai_libre) = 1                                                                                                                                           
      189 +```                                                                                                                                                                                       
      190  
      191 +**Preuve sur M0** : `0 + 0 + 1 = 1`. OK.                                                                                                                                                  
      192 +                                                                                                                                                                                          
      193 +**Preservation** :                                                                                                                                                                        
      194 +- `T1_arrivee_quai` : -1 sur `Quai_libre`, +1 sur `T1_a_quai` : somme inchangee.                                                                                                          
      195 +- `T2_arrivee_quai` : symetrique.                                                                                                                                                         
      196 +- `T1_depart_quai` : -1 sur `T1_a_quai`, +1 sur `Quai_libre` : somme inchangee.                                                                                                           
      197 +- `T2_depart_quai` : symetrique.                                                                                                                                                          
      198 +- Toutes les autres transitions ne touchent pas ces 3 places : somme inchangee.                                                                                                           
      199 +                                                                                                                                                                                          
      200 +L'invariant est preserve. **Consequence** : exclusion mutuelle sur le quai.                                                                                                               
      201 +                                                                                                                                                                                          
      202 +### 5.3 Invariant portes (etat des portes)                                                                                                                                                
      203 +                                                                                                                                                                                          
      204 +```                                                                                                                                                                                       
      205 +M(Portes_fermees) + M(Portes_ouvertes) = 1                                                                                                                                                
      206 +```                                                                                                                                                                                       
      207 +                                                                                                                                                                                          
      208 +**Preuve sur M0** : `1 + 0 = 1`. OK.                                                                                                                                                      
      209 +                                                                                                                                                                                          
      210 +**Preservation** :                                                                                                                                                                        
      211 +- `Ouverture_portes_T1` / `Ouverture_portes_T2` : -1 sur `Portes_fermees`, +1 sur `Portes_ouvertes` : somme inchangee.                                                                    
      212 +- `Fermeture_portes_T1` / `Fermeture_portes_T2` : -1 sur `Portes_ouvertes`, +1 sur `Portes_fermees` : somme inchangee.                                                                    
      213 +- `T1_depart_quai` / `T2_depart_quai` : la place `Portes_fermees` est lue (consommee+reproduite), `Portes_ouvertes` n'est pas touchee : somme inchangee.                                  
      214 +- Toutes les autres transitions ne touchent pas ces 2 places.                                                                                                                             
      215 +                                                                                                                                                                                          
      216 +L'invariant est preserve. **Consequence** : les portes sont toujours dans exactement un etat (ouvertes ou fermees), jamais dans un etat indetermine.                                      
      217 +                                                                                                                                                                                          
      218  ---
      219  
      116 -## 6) Invariants secondaires (par train)                                                                                                                                                  
      220 +## 6) Invariants critiques de surete PSD (les NOUVEAUX)                                                                                                                                   
      221  
      118 -Pour chaque train i parmi {1, 2} :                                                                                                                                                        
      222 +Ces invariants ne sont pas des P-invariants au sens strict (ils ne sont pas des combinaisons lineaires constantes), mais des **proprietes de surete** que l'analyseur doit verifier sur to
          +ut marquage atteignable.                                                                                                                                                                  
      223  
      224 +### 6.1 Surete d'ouverture (PSD-Open Safety)                                                                                                                                              
      225 +                                                                                                                                                                                          
      226  ```
      121 -M(Ti_hors) + M(Ti_attente) + M(Ti_sur_troncon) = 1                                                                                                                                        
      227 +G ( Portes_ouvertes = 1  =>  T1_a_quai + T2_a_quai = 1 )                                                                                                                                  
      228  ```
      229  
      124 -Un train est toujours dans exactement un de ses trois etats. Verifie sur M0 et preserve par les 3 transitions du train i (chaque transition deplace un jeton entre deux places de la colon
          -ne).                                                                                                                                                                                      
      230 +**Lecture** : a tout instant, si les portes palieres sont ouvertes, alors un train (et un seul, par exclusion mutuelle quai) est a l'arret a quai.                                        
      231  
      232 +**Justification structurelle** : la place `Portes_ouvertes` ne peut etre marquee que par les transitions `Ouverture_portes_T1` ou `Ouverture_portes_T2`. Ces deux transitions ont `Ti_a_qu
          +ai` dans leur pre, donc elles ne sont tirables que si un train est a quai. De plus, ces transitions ne consomment pas `Ti_a_quai` (read-arc), donc le train reste a quai. Tant que le trai
          +n n'a pas applique `Fermeture_portes_Ti` (qui remet `Portes_fermees=1` et donc `Portes_ouvertes=0`), il ne peut pas tirer `Ti_depart_quai` (qui exige `Portes_fermees=1`). Donc tant que `
          +Portes_ouvertes=1`, le train reste a quai.                                                                                                                                                
      233 +                                                                                                                                                                                          
      234 +**Verification programmatique** : l'analyseur Scala enumere les marquages atteignables et verifie pour chaque marquage M : `M(Portes_ouvertes) = 1 => M(T1_a_quai) + M(T2_a_quai) = 1`.   
      235 +                                                                                                                                                                                          
      236 +**Consequence** : les voyageurs ne peuvent jamais tomber sur les voies via les portes palieres. **Surete CRITIQUE M14**.                                                                  
      237 +                                                                                                                                                                                          
      238 +### 6.2 Surete au demarrage (PSD-Departure Safety)                                                                                                                                        
      239 +                                                                                                                                                                                          
      240 +```                                                                                                                                                                                       
      241 +Pour tout marquage M et tout train Ti, si la transition Ti_depart_quai est tirable depuis M,                                                                                              
      242 +alors M(Portes_fermees) = 1.                                                                                                                                                              
      243 +```                                                                                                                                                                                       
      244 +                                                                                                                                                                                          
      245 +**Justification structurelle** : c'est inscrit dans le pre de `Ti_depart_quai` : `{Ti_a_quai, Portes_fermees}`. Si `Portes_fermees=0`, la transition n'est pas tirable. Donc un train ne p
          +eut pas demarrer alors que les portes sont ouvertes.                                                                                                                                      
      246 +                                                                                                                                                                                          
      247 +**Consequence** : aucun voyageur ne peut etre coince ou ecrase par un train qui demarre alors qu'il monte/descend. **Surete CRITIQUE M14**.                                               
      248 +                                                                                                                                                                                          
      249  ---
      250  
      251  ## 7) Espace d'etats attendu
      252  
      130 -Depuis M0, l'espace d'etats accessible est de **taille bornee** (6 a 9 marquages selon l'ordre d'exploration). Cette borne sera confirmee programmatiquement par l'analyseur (livrable L5,
          - Phase 6).                                                                                                                                                                                
      253 +Depuis M0, l'enumeration manuelle prevoit **15 a 18 marquages atteignables** (a confirmer par l'analyseur Scala). Cette borne est compatible avec une enumeration manuelle dans le carnet 
          +de preuves (`documentation/livrables/preuves-manuelles.md` tache 1 etendue).                                                                                                              
      254  
      132 -Etats attendus a la main :                                                                                                                                                                
      133 -1. M0 = `(libre, T1_hors, T2_hors)` (notation compacte : seules les places marquees a 1)                                                                                                  
      134 -2. `(libre, T1_attente, T2_hors)` apres `T1_demande`                                                                                                                                      
      135 -3. `(libre, T1_hors, T2_attente)` apres `T2_demande`                                                                                                                                      
      136 -4. `(libre, T1_attente, T2_attente)` les deux ont demande                                                                                                                                 
      137 -5. `(T1_sur_troncon, T2_hors)` T1 est entre depuis l'etat 2                                                                                                                               
      138 -6. `(T1_sur_troncon, T2_attente)` T1 est entre alors que T2 attendait                                                                                                                     
      139 -7. `(T2_sur_troncon, T1_hors)` symetrique                                                                                                                                                 
      140 -8. `(T2_sur_troncon, T1_attente)` symetrique                                                                                                                                              
      255 +**Estimations a la main** (raisonnement par projection) :                                                                                                                                 
      256 +- Le canton seul (avec 2 trains) : 8 etats (comme dans le modele initial).                                                                                                                
      257 +- Pour chaque etat de canton ou un train est `Ti_a_quai`, on multiplie par les 2 etats des portes (fermees/ouvertes).                                                                     
      258 +- Mais beaucoup de combinaisons sont inaccessibles (par exemple : un train sur le canton ET un autre a quai avec portes ouvertes).                                                        
      259  
      142 -Aucun de ces marquages ne contient simultanement un jeton dans `T1_sur_troncon` et `T2_sur_troncon` : l'exclusion mutuelle est respectee.                                                 
      260 +L'analyseur fournira la liste exacte. **Si elle excede 25 marquages, on simplifie le modele** (par exemple en fusionnant Ouverture/Fermeture en une seule transition, ou en retirant les p
          +ortes pour les ramener a un seul flag).                                                                                                                                                   
      261  
      262  ---
      263  
      264  ## 8) Correspondance avec les messages Akka
      265  
      148 -Cette table sera reprise et raffinee dans `documentation/livrables/comparaison.md` (livrable L6). Premiere ebauche :                                                                      
      266 +Etendu a 8 messages (4 anciens + 4 nouveaux). Cette table est la reference pour `documentation/livrables/comparaison.md` (livrable L6).                                                   
      267  
      150 -| Transition Petri          | Message Akka declencheur                       | Acteur emetteur -> recepteur     |                                                                         
      151 -|---------------------------|------------------------------------------------|----------------------------------|                                                                         
      152 -| `T1_demande`              | `Demande(Train1, replyTo)`                     | Train1 -> SectionController      |                                                                         
      153 -| `T2_demande`              | `Demande(Train2, replyTo)`                     | Train2 -> SectionController      |                                                                         
      154 -| `T1_entree_autorisee`     | `Autorisation` (en reponse a `Demande(Train1)`)| SectionController -> Train1      |                                                                         
      155 -| `T2_entree_autorisee`     | `Autorisation` (en reponse a `Demande(Train2)`)| SectionController -> Train2      |                                                                         
      156 -| `T1_sortie_liberation`    | `Sortie(Train1)`                               | Train1 -> SectionController      |                                                                         
      157 -| `T2_sortie_liberation`    | `Sortie(Train2)`                               | Train2 -> SectionController      |                                                                         
      268 +| Transition Petri          | Message Akka declencheur               | Acteur emetteur -> recepteur          |                                                                            
      269 +|---------------------------|----------------------------------------|---------------------------------------|                                                                            
      270 +| `T1_demande`              | `Demande(Train1, replyTo)`             | Train1 -> SectionController           |                                                                            
      271 +| `T2_demande`              | `Demande(Train2, replyTo)`             | Train2 -> SectionController           |                                                                            
      272 +| `T1_entree_canton`        | `Autorisation` (reponse a Demande T1)  | SectionController -> Train1           |                                                                            
      273 +| `T2_entree_canton`        | `Autorisation` (reponse a Demande T2)  | SectionController -> Train2           |                                                                            
      274 +| `T1_arrivee_quai`         | `ArriveeQuai(Train1, replyTo)` puis `Autorisation` retour | Train1 -> QuaiController -> Train1 |                                                            
      275 +| `T2_arrivee_quai`         | `ArriveeQuai(Train2, replyTo)` puis `Autorisation` retour | Train2 -> QuaiController -> Train2 |                                                            
      276 +| `Ouverture_portes_T1`     | `OuverturePortes(Train1)`              | Train1 -> GestionnairePortes          |                                                                            
      277 +| `Ouverture_portes_T2`     | `OuverturePortes(Train2)`              | Train2 -> GestionnairePortes          |                                                                            
      278 +| `Fermeture_portes_T1`     | `FermeturePortes(Train1)`              | Train1 -> GestionnairePortes          |                                                                            
      279 +| `Fermeture_portes_T2`     | `FermeturePortes(Train2)`              | Train2 -> GestionnairePortes          |                                                                            
      280 +| `T1_depart_quai`          | `DepartQuai(Train1)`                   | Train1 -> QuaiController              |                                                                            
      281 +| `T2_depart_quai`          | `DepartQuai(Train2)`                   | Train2 -> QuaiController              |                                                                            
      282  
      159 -Le message `Attente` (refus temporaire envoye par le controleur) **n'a pas de transition Petri associee** : c'est une notification de protocole, pas un changement d'etat formel. Le train
          - reste dans `Ti_attente` jusqu'a recevoir une `Autorisation`. Cette dissymetrie est documentee et assumee.                                                                                
      283 +Le message `Attente` (refus temporaire envoye par les controleurs) **n'a pas de transition Petri associee** (notification de protocole), conformement au modele initial. Il s'applique au 
          +SectionController et au QuaiController.                                                                                                                                                   
      284  
      285  ---
      286  
      287  ## 9) Limites assumees
      288  
      165 -- **Pas de fairness explicite** : si T1 demande en boucle juste apres avoir libere, T2 peut attendre indefiniment dans le modele Petri pur. La preuve de Liveness suppose une hypothese d'
          -arbitrage non pathologique du controleur (FIFO en pratique cote Akka). Cette hypothese sera formalisee dans le rapport (L4).                                                              
      166 -- **Pas de notion de temps** : le modele est purement combinatoire, aucune duree n'est attachee aux transitions.                                                                          
      167 -- **Pas de pannes** : aucune transition ne modelise la perte de message, le crash d'acteur, ou le timeout. C'est un choix de scope, pas un oubli.                                         
      289 +- **Pas de fairness explicite** : si T1 demande en boucle juste apres avoir libere, T2 peut attendre indefiniment dans le modele Petri pur. La preuve de Liveness suppose une hypothese d'
          +arbitrage non pathologique des controleurs (FIFO en pratique cote Akka).                                                                                                                  
      290 +- **Pas de notion de temps** : le modele est purement combinatoire, aucune duree n'est attachee aux transitions (en particulier, pas de duree minimale d'arret a quai).                   
      291 +- **Pas de pannes** : aucune transition ne modelise la perte de message, le crash d'acteur, ou le timeout.                                                                                
      292 +- **Modele PSD simplifie** : en realite, la M14 a aussi des **portes du train** (pas seulement palieres), avec une synchronisation electronique des deux. Ici on modelise un seul niveau d
          +e portes pour rester analysable.                                                                                                                                                          
      293 +- **Quai unique** : on modelise un seul quai au lieu de 2 (un par sens). Cela impose que les 2 trains se "partagent" un quai, ce qui est une abstraction. En realite, M14 a 2 quais oppose
          +s par station.                                                                                                                                                                            
      294  
      295  ---
      296  
      171 -## 10) Verification a faire avec l'analyseur (Phases 5-6)                                                                                                                                 
      297 +## 10) Verification a faire avec l'analyseur (Phases B-C-D)                                                                                                                               
      298  
      299  L'analyseur Scala doit confirmer programmatiquement :
      174 -- [ ] Tous les marquages atteignables verifient l'invariant principal.                                                                                                                    
      175 -- [ ] Tous les marquages atteignables verifient les invariants par train.                                                                                                                 
      176 -- [ ] Aucun marquage atteignable n'est un deadlock (au moins une transition tirable depuis chaque etat).                                                                                  
      177 -- [ ] Le nombre de marquages atteignables est borne et coherent avec la liste de la section 7.                                                                                            
      300 +- [ ] Tous les marquages atteignables verifient l'invariant canton (5.1).                                                                                                                 
      301 +- [ ] Tous les marquages atteignables verifient l'invariant quai (5.2).                                                                                                                   
      302 +- [ ] Tous les marquages atteignables verifient l'invariant portes (5.3).                                                                                                                 
      303 +- [ ] **Tous les marquages avec `Portes_ouvertes=1` ont `T1_a_quai + T2_a_quai = 1` (PSD safety, 6.1).**                                                                                  
      304 +- [ ] Tous les marquages atteignables verifient les invariants par train (`Ti_hors + Ti_attente + Ti_sur_canton + Ti_a_quai = 1`).                                                        
      305 +- [ ] Aucun marquage atteignable n'est un deadlock.                                                                                                                                       
      306 +- [ ] Le nombre de marquages atteignables est compatible avec l'enumeration manuelle (entre 12 et 20).                                                                                    

⏺ Source de vérité Petri réécrite. Maintenant le lexique.                          
                                                                                          
  Read 1 file (ctrl+o to expand)    
                                                                                                                                                                                                            
⏺ Write(documentation/gouvernance/lexique.md)                                      
  ⎿  Added 81 lines, removed 33 lines                                                                                                                                                                       
        4  > A consulter avant d'ouvrir un fichier de code que tu n'as pas ecrit.         
        5  >                                                                                                                                                                                                
        6  > **Regle dure** : si un nom dans une colonne ne correspond pas exactement aux autres colonnes, c'est un bug de coherence et il faut renommer.                                                   
        7 +>                                                                                                                                                                                                
        8 +> **Mise a jour majeure le 29/04/2026** : extension du modele avec le quai (station) et les portes palieres (PSD). Le vocabulaire couvre desormais 4 etats par train et 4 ressources globa       
          +les.                                                                                                                                                                                             
        9                                                   
       10  ---
       11  
       10 -## 1) Etats des trains                                                                                                                                                                    
       12 +## 1) Etats des trains (4 etats)                                                                                                                                                          
       13  
       12 -| Sens metier                          | Place Petri        | Nom du comportement Akka         | Etat dans les tests    |                                                                 
       13 -|--------------------------------------|--------------------|----------------------------------|------------------------|                                                                 
       14 -| Train est hors zone d'approche       | `T1_hors`, `T2_hors`           | `comportementHors`           | `hors`                 |                                                         
       15 -| Train a demande, attend la reponse   | `T1_attente`, `T2_attente`     | `comportementEnAttente`      | `enAttente`            |                                                         
       16 -| Train occupe le troncon partage      | `T1_sur_troncon`, `T2_sur_troncon` | `comportementSurTroncon` | `surTroncon`           |                                                         
       14 +| Sens metier                                          | Place Petri                          | Comportement Akka          | Etat dans les tests |                                        
       15 +|------------------------------------------------------|--------------------------------------|----------------------------|---------------------|                                        
       16 +| Train hors zone (amont du canton ou apres depart)    | `T1_hors`, `T2_hors`                 | `comportementHors`         | `hors`              |                                        
       17 +| Train a demande l'acces, attend la reponse           | `T1_attente`, `T2_attente`           | `comportementEnAttente`    | `enAttente`         |                                        
       18 +| Train circule sur le canton de signalisation         | `T1_sur_canton`, `T2_sur_canton`     | `comportementSurCanton`    | `surCanton`         |                                        
       19 +| Train est a l'arret en station, sur le quai          | `T1_a_quai`, `T2_a_quai`             | `comportementAQuai`        | `aQuai`             |                                        
       20  
       21  **Regle** : aucun autre etat n'est introduit. Pas de "transition", pas de "demande_envoyee", pas de "en_route". Si le metier evolue, on amende le reseau de Petri en premier (livrable L3)
            puis on aligne le code et les tests.
       22  
       23 +**Renommage 29/04** : l'etat `surTroncon` du modele initial est renomme `surCanton` (vocabulaire ferroviaire reel). La place `Ti_sur_troncon` devient `Ti_sur_canton`.                    
       24 +                                                                                                                                                                                          
       25  ---
       26  
       22 -## 2) Etats du controleur de section                                                                                                                                                      
       27 +## 2) Etats des controleurs                                                                                                                                                               
       28  
       24 -| Sens metier                       | Encodage Petri (place)  | Etat interne du controleur Akka | Description                       |                                                     
       25 -|-----------------------------------|-------------------------|---------------------------------|-----------------------------------|                                                     
       26 -| Le troncon est libre              | `Troncon_libre`         | `EtatLibre` (file vide)         | Aucun train sur le troncon        |                                                     
       27 -| Train 1 occupe le troncon         | `T1_sur_troncon`        | `EtatOccupe(Train1, file)`      | Train 1 dedans, file FIFO eventuelle |                                                  
       28 -| Train 2 occupe le troncon         | `T2_sur_troncon`        | `EtatOccupe(Train2, file)`      | Train 2 dedans, file FIFO eventuelle |                                                  
       29 +### 2.1 SectionController (canton de signalisation)                                                                                                                                       
       30  
       30 -**Note** : la file FIFO du controleur Akka est une finesse d'implementation pour garantir la fairness en pratique. Elle n'a pas d'equivalent direct dans le reseau de Petri (qui reste non
          - deterministe). Cette dissymetrie est documentee dans `petri/petri-troncon.md` section 9 et reprise dans le rapport.                                                                      
       31 +| Sens metier                       | Encodage Petri (place)   | Etat Akka du controleur          | Description                        |                                                  
       32 +|-----------------------------------|--------------------------|----------------------------------|------------------------------------|                                                  
       33 +| Le canton est libre               | `Canton_libre`           | `cantonLibre` (file vide)        | Aucun train sur le canton          |                                                  
       34 +| Train 1 occupe le canton          | `T1_sur_canton`          | `cantonOccupe(Train1, file)`     | Train 1 dedans, file FIFO eventuelle |                                                
       35 +| Train 2 occupe le canton          | `T2_sur_canton`          | `cantonOccupe(Train2, file)`     | Train 2 dedans, file FIFO eventuelle |                                                
       36  
       37 +### 2.2 QuaiController (quai en station) - NOUVEAU                                                                                                                                        
       38 +                                                                                                                                                                                          
       39 +| Sens metier                       | Encodage Petri (place)   | Etat Akka du controleur          | Description                        |                                                  
       40 +|-----------------------------------|--------------------------|----------------------------------|------------------------------------|                                                  
       41 +| Le quai est libre                 | `Quai_libre`             | `quaiLibre` (file vide)          | Aucun train a quai                 |                                                  
       42 +| Train 1 a quai                    | `T1_a_quai`              | `quaiOccupe(Train1, file)`       | Train 1 a quai, file FIFO eventuelle |                                                
       43 +| Train 2 a quai                    | `T2_a_quai`              | `quaiOccupe(Train2, file)`       | Train 2 a quai, file FIFO eventuelle |                                                
       44 +                                                                                                                                                                                          
       45 +### 2.3 GestionnairePortes (portes palieres) - NOUVEAU                                                                                                                                    
       46 +                                                                                                                                                                                          
       47 +| Sens metier                       | Encodage Petri (place)   | Etat Akka                        | Description                                  |                                        
       48 +|-----------------------------------|--------------------------|----------------------------------|----------------------------------------------|                                        
       49 +| Portes palieres fermees           | `Portes_fermees=1`       | `portesFermees`                  | Etat initial. Securise.                      |                                        
       50 +| Portes palieres ouvertes          | `Portes_ouvertes=1`      | `portesOuvertes(occupant)`       | Ne peut etre atteint que si un train a quai. |                                        
       51 +                                                                                                                                                                                          
       52 +**Garde de surete CRITIQUE (PSD)** : le `GestionnairePortes` REFUSE toute demande d'ouverture si aucun train n'est a quai. C'est l'invariant de surete d'ouverture (cf petri-troncon.md se
          +ction 6.1).                                                                                                                                                                               
       53 +                                                                                                                                                                                          
       54 +**Note FIFO vs Petri** : les files FIFO Akka des controleurs sont des finesses d'implementation pour garantir la fairness en pratique. Elles n'ont pas d'equivalent direct dans le reseau 
          +de Petri (non deterministe). Cette dissymetrie est documentee.                                                                                                                            
       55 +                                                                                                                                                                                          
       56  ---
       57  
       34 -## 3) Messages du protocole Akka                                                                                                                                                          
       58 +## 3) Messages du protocole Akka (8 messages)                                                                                                                                             
       59  
       36 -| Message                                  | Type                                  | Sens metier                                         | Transition Petri associee |                    
       37 -|------------------------------------------|---------------------------------------|-----------------------------------------------------|---------------------------|                    
       38 -| `Demande(emetteur, repondreA)`           | `MessagePourControleur`               | Le train demande l'acces au troncon                 | `T1_demande` ou `T2_demande` |                 
       39 -| `Sortie(emetteur)`                       | `MessagePourControleur`               | Le train libere le troncon en sortant               | `T1_sortie_liberation` ou `T2_sortie_liberation
          -` |                                                                                                                                                                                       
       40 -| `Autorisation`                           | `MessagePourTrain`                    | Le controleur autorise l'entree                     | `T1_entree_autorisee` ou `T2_entree_autorisee` 
          -|                                                                                                                                                                                         
       41 -| `Attente`                                | `MessagePourTrain`                    | Le controleur signale au train d'attendre           | (aucune - notification de protocole, pas un cha
          -ngement d'etat formel) |                                                                                                                                                                  
       60 +### Messages vers les controleurs                                                                                                                                                         
       61  
       43 -**Regle** : aucun nouveau message ne peut etre introduit sans mise a jour de `petri/petri-troncon.md` section 8 ET de ce lexique. Le protocole est verrouille a 4 messages.               
       62 +| Message                                  | Type                                | Sens metier                                              | Transition Petri associee                  |
       63 +|------------------------------------------|-------------------------------------|----------------------------------------------------------|--------------------------------------------|
       64 +| `Demande(emetteur, repondreA)`           | `MessagePourControleur`             | Le train demande l'acces au canton                       | `T1_demande` / `T2_demande`                |
       65 +| `Sortie(emetteur)`                       | `MessagePourControleur`             | Le train signale qu'il a quitte le canton (vers le quai) | `T1_arrivee_quai` / `T2_arrivee_quai` (liber
          +e canton) |                                                                                                                                                                               
       66 +| `ArriveeQuai(emetteur, repondreA)`       | `MessagePourQuaiController` (NEW)   | Le train demande l'acces au quai                         | `T1_arrivee_quai` / `T2_arrivee_quai` (occup
          +e quai) |                                                                                                                                                                                 
       67 +| `DepartQuai(emetteur)`                   | `MessagePourQuaiController` (NEW)   | Le train signale qu'il a quitte le quai                  | `T1_depart_quai` / `T2_depart_quai`        |
       68 +| `OuverturePortes(emetteur)`              | `MessagePourGestionnairePortes` (NEW) | Le train demande l'ouverture des portes palieres       | `Ouverture_portes_T1` / `Ouverture_portes_T2
          +` |                                                                                                                                                                                       
       69 +| `FermeturePortes(emetteur)`              | `MessagePourGestionnairePortes` (NEW) | Le train demande la fermeture des portes              | `Fermeture_portes_T1` / `Fermeture_portes_T2`
          + |                                                                                                                                                                                        
       70  
       71 +### Messages vers les trains                                                                                                                                                              
       72 +                                                                                                                                                                                          
       73 +| Message                | Type                | Sens metier                                                |                                                                             
       74 +|------------------------|---------------------|------------------------------------------------------------|                                                                             
       75 +| `Autorisation`         | `MessagePourTrain`  | Un controleur autorise l'entree (canton ou quai)           |                                                                             
       76 +| `Attente`              | `MessagePourTrain`  | Un controleur signale au train d'attendre                  |                                                                             
       77 +| `PortesOuvertes`       | `MessagePourTrain` (NEW) | Le `GestionnairePortes` confirme que les portes sont ouvertes |                                                                     
       78 +| `PortesFermees`        | `MessagePourTrain` (NEW) | Le `GestionnairePortes` confirme la fermeture          |                                                                            
       79 +                                                                                                                                                                                          
       80 +**Regle de verrouillage** : aucun nouveau message ne peut etre introduit sans mise a jour de `petri/petri-troncon.md` section 8 ET de ce lexique. Le protocole est verrouille a **8 messag
          +es cote controleurs + 4 messages cote trains** (12 au total).                                                                                                                             
       81 +                                                                                                                                                                                          
       82 +**Note** : selon l'implementation, certains messages peuvent partager un type commun (`MessagePourTrain` pour `Autorisation`, `Attente`, `PortesOuvertes`, `PortesFermees`).              
       83 +                                                                                                                                                                                          
       84  ---
       85  
       86  ## 4) Glossaire des termes critiques
       87  
       49 -### Termes du metier                                                                                                                                                                      
       50 -- **Troncon partage** : segment de voie unique qu'au plus un train peut occuper a la fois. Synonyme : "section critique" en theorie de la concurrence.                                    
       88 +### Termes du metier ferroviaire                                                                                                                                                          
       89 +                                                                                                                                                                                          
       90 +- **Canton de signalisation (block section)** : segment de voie ferree entre 2 signaux. Au plus un train a la fois pour eviter les collisions. Concept fondamental du block signaling.    
       91 +- **PSD (Platform Screen Doors) / Portes palieres** : portes physiques sur le quai, alignees avec les portes du train. Empechent les chutes sur les voies. Signature de la M14, premiere l
          +igne entierement equipee en France.                                                                                                                                                       
       92 +- **Quai** : zone d'embarquement/debarquement en station. Au plus un train a la fois (par sens) en pratique.                                                                              
       93 +- **CBTC (Communication-Based Train Control)** : technologie de controle continu des trains automatiques M14.                                                                             
       94  - **Exclusion mutuelle** : propriete de surete garantissant qu'au plus un acteur acces a une ressource a la fois.
       52 -- **Arbitrage** : mecanisme par lequel le controleur decide qui peut entrer et qui doit attendre.                                                                                         
       95 +- **Arbitrage** : mecanisme par lequel un controleur decide qui peut entrer et qui doit attendre.                                                                                         
       96  - **Fairness** : hypothese qu'un train en attente finit toujours par etre servi (pas de famine indefinie).
       97  
       98  ### Termes Akka
       99 +                                                                                                                                                                                          
      100  - **Acteur** : entite isolee qui recoit et traite des messages sequentiellement.
      101  - **`Behavior[T]`** : description du comportement courant d'un acteur, specifiant comment il reagit aux messages de type T.
      102  - **TestKit / Probe** : faux acteur utilise dans les tests pour intercepter et verifier les messages.
      103  
      104  ### Termes Petri
       61 -- **Place** : etat possible (par exemple `T1_attente`).                                                                                                                                   
      105 +                                                                                                                                                                                          
      106 +- **Place** : etat possible (par exemple `T1_attente`, `Portes_fermees`).                                                                                                                 
      107  - **Marquage** : nombre de jetons dans chaque place a un instant donne.
      108  - **Transition** : evenement qui consomme des jetons en entree et en produit en sortie.
       64 -- **Tirable** : une transition est tirable si toutes ses places d'entree ont assez de jetons.                                                                                             
      109 +- **Tirable** : une transition est tirable si toutes ses places d'entree ont au moins 1 jeton.                                                                                            
      110 +- **Read-arc emule** : technique pour modeliser une "lecture" en Petri ordinaire, en consommant et reproduisant le meme jeton (utilisee pour `Portes_fermees` dans `Ti_depart_quai`).     
      111  - **Espace d'etats accessible** : ensemble des marquages atteignables depuis le marquage initial.
       66 -- **P-invariant** : combinaison lineaire de places qui reste constante sur tous les marquages atteignables (notre invariant principal en est un).                                         
      112 +- **P-invariant** : combinaison lineaire de places qui reste constante sur tous les marquages atteignables.                                                                               
      113  
      114  ### Termes verification
       69 -- **Safety** : "rien de mal n'arrive jamais" (par exemple : pas d'occupation simultanee).                                                                                                 
      115 +                                                                                                                                                                                          
      116 +- **Safety** : "rien de mal n'arrive jamais" (par exemple : pas d'occupation simultanee, portes ne s'ouvrent pas sans train).                                                             
      117  - **Liveness** : "quelque chose de bien finit toujours par arriver" (par exemple : un train en attente finit par passer).
      118  - **LTL** : Linear Temporal Logic, langage pour exprimer Safety et Liveness sur des chemins d'execution.
      119 +- **Surete PSD** : propriete de surete specifique aux portes palieres ; il en existe 2 (ouverture, demarrage).                                                                            
      120  
      121  ---
      122  
      123  ## 5) Convention de nommage Scala (rappel court)
      124  
       77 -- Variables : noms longs et explicites en francais (`trainEnAttente`, `controleurSection`). Pas de `t`, `c`, `tmp`.                                                                       
       78 -- Fonctions : verbe ou nom d'etat (`comportementHors`, `traiterDemande`). Court (<30 lignes).                                                                                             
       79 -- Classes / objets : substantif (`SectionController`, `PetriNet`).                                                                                                                        
       80 -- Messages : substantif decrivant l'evenement (`Demande`, `Sortie`, `Autorisation`, `Attente`).                                                                                           
       81 -- Tests : nom de scenario (`ScenarioNominalSpec`, `ScenarioConcurrenceSpec`).                                                                                                             
      125 +- Variables : noms longs et explicites en francais (`trainEnAttente`, `controleurCanton`, `gestionnairePortes`). Pas de `t`, `c`, `tmp`.                                                  
      126 +- Fonctions : verbe ou nom d'etat (`comportementHors`, `comportementAQuai`, `traiterDemande`). Court (<30 lignes).                                                                        
      127 +- Classes / objets : substantif (`SectionController`, `QuaiController`, `GestionnairePortes`, `PetriNet`).                                                                                
      128 +- Messages : substantif decrivant l'evenement (`Demande`, `Sortie`, `ArriveeQuai`, `DepartQuai`, `OuverturePortes`, `FermeturePortes`, `Autorisation`, `Attente`, `PortesOuvertes`, `Porte
          +sFermees`).                                                                                                                                                                               
      129 +- Tests : nom de scenario (`ScenarioNominalSpec`, `ScenarioConcurrenceSpec`, `ScenarioPSDSpec`).                                                                                          
      130  
      131  ---
      132  

  Read 1 file (ctrl+o to expand)                                                   
                                                                                          
⏺ Update(documentation/gouvernance/protocole-coordination.md)                      
  ⎿  Added 14 lines, removed 6 lines                                                                                                                                                                        
      30                                                                                  
      31  ## 2) Liste gelee - Ce qui NE change PAS sans reunion d'equipe                                                                                                                                    
      32                                                                                                                                                                                                    
      33 -Les decisions suivantes sont verrouillees. Toute modification necessite un accord explicite des 4 contributeurs (a tracer dans historique.md).                                                    
      33 +Les decisions suivantes sont verrouillees apres l'extension PSD du 29 avril 2026. Toute modification necessite un accord explicite des 4 contributeurs (a tracer dans historique.md).             
      34                                                                                                                                                                                                    
      35 -- [LOCKED] **7 places, 6 transitions** dans le reseau de Petri. Pas plus, pas moins.                                                                                                       
      36 -- [LOCKED] **4 messages Akka** : `Demande`, `Sortie`, `Autorisation`, `Attente`. Aucun nouveau message.                                                                                    
      35 +- [LOCKED] **12 places, 12 transitions** dans le reseau de Petri etendu (canton + quai + portes). Pas plus, pas moins.                                                                     
      36 +- [LOCKED] **8 messages Akka cote controleurs** : `Demande`, `Sortie`, `ArriveeQuai`, `DepartQuai`, `OuverturePortes`, `FermeturePortes`, plus `Autorisation`, `Attente`, `PortesOuvertes`,
         + `PortesFermees` cote trains. Aucun nouveau message.                                                                                                                                       
      37  - [LOCKED] **2 trains** dans le scope. Pas de generalisation a N trains.
      38 -- [LOCKED] **Marquage initial** : `Troncon_libre=1, T1_hors=1, T2_hors=1`. Aucune autre place a 1.                                                                                         
      39 -- [LOCKED] **Invariant principal** : `T1_sur_troncon + T2_sur_troncon + Troncon_libre = 1`. C'est la pierre angulaire du projet.                                                           
      40 -- [LOCKED] **3 scenarios de validation** : Nominal, Concurrence, Liberation/Progression. Aucun 4e scenario sans accord.                                                                    
      38 +- [LOCKED] **Marquage initial M0** : `Canton_libre=1, Quai_libre=1, Portes_fermees=1, T1_hors=1, T2_hors=1`. Toute autre place a 0. 5 jetons en circulation.                               
      39 +- [LOCKED] **3 invariants de ressource** :                                                                                                                                                 
      40 +  - canton : `T1_sur_canton + T2_sur_canton + Canton_libre = 1`                                                                                                                            
      41 +  - quai : `T1_a_quai + T2_a_quai + Quai_libre = 1`                                                                                                                                        
      42 +  - portes : `Portes_fermees + Portes_ouvertes = 1`                                                                                                                                        
      43 +- [LOCKED] **2 invariants critiques de surete PSD** :                                                                                                                                      
      44 +  - PSD-Open : `Portes_ouvertes = 1 => T1_a_quai + T2_a_quai = 1`                                                                                                                          
      45 +  - PSD-Departure : `Ti_depart_quai tirable => Portes_fermees = 1`                                                                                                                         
      46 +- [LOCKED] **3 scenarios de validation** : Nominal cycle complet, Concurrence canton+quai, Surete PSD (tentative invalide). Aucun 4e scenario sans accord.                                 
      47  
      48 +**Decision archivee 26/04** : verrouillage initial a 7 places / 6 transitions / 4 messages, abandonne le 29/04 lors de l'extension PSD (cf historique.md). Cette extension a ete validee pa
         +r l'utilisateur comme moyen de rendre le projet plus M14-realiste.                                                                                                                         
      49 +                                                                                                                                                                                           
      50  Si pendant le codage une de ces verrous semble bloquer, **stop** : on en discute avant de toucher quoi que ce soit. Probablement le code peut s'adapter sans casser le verrou.
      51  
      52  ---

⏺ Update(documentation/gouvernance/protocole-coordination.md)                      
  ⎿  Added 27 lines                                                                       
      142                                                                          
      143  **Reponse** : non. Le reseau est encode **en dur** dans `src/main/scala/m14/petri/PetriNet.scala` (pas de parser de fichier). Conforme au scope verrou : pas de generalisation. La coheren       
           ce avec `petri/petri-troncon.md` est verifiee par relecture, pas par parsing.
      144                                                                                                                                                                                                   
      145 +### Q11 - Le `QuaiController` doit-il etre une copie conforme de `SectionController` ? [NEW Phase 7]                                                                                      
      146 +                                                                                                                                                                                                 
      147 +**Reponse** : **oui, deliberement**. Memes etats internes (`quaiLibre`, `quaiOccupe(occupant, file)`), meme logique FIFO, memes cas defensifs. La duplication est assumee : elle simplifie
          + la lecture et evite une abstraction prematuree (regles vibe-code section 12). Si plus tard on factorise, ce sera apres rendu.                                                            
      148 +                                                                                                                                                                                          
      149 +### Q12 - Comment le `GestionnairePortes` sait-il qu'un train est a quai ? [NEW Phase 7]                                                                                                  
      150 +                                                                                                                                                                                          
      151 +**Reponse** : c'est l'acteur lui-meme qui maintient l'etat. Quand le `Train` arrive a quai (a recu `Autorisation` du `QuaiController`), il envoie `OuverturePortes(idTrain)` au `Gestionna
          +irePortes`. Le gestionnaire passe alors de l'etat `portesFermees` a `portesOuvertes(idTrain)`. La garde de surete est : si on recoit `OuverturePortes` alors qu'on est deja en `portesOuve
          +rtes(autreTrain)`, on ignore (cas defensif, deux trains a quai en meme temps est exclu par l'invariant quai).                                                                             
      152 +                                                                                                                                                                                          
      153 +**Note importante** : le `GestionnairePortes` ne questionne pas le `QuaiController`, il fait confiance a la machine d'etat des trains. La coherence est garantie par le modele Petri (qui 
          +empeche `OuverturePortes` sans train a quai).                                                                                                                                             
      154 +                                                                                                                                                                                          
      155 +### Q13 - Le `Train` doit-il faire l'ouverture/fermeture des portes lui-meme ? [NEW Phase 7]                                                                                              
      156 +                                                                                                                                                                                          
      157 +**Reponse** : oui, le train **declenche** l'ouverture en envoyant `OuverturePortes` au gestionnaire des qu'il arrive a quai (recoit `Autorisation` du quai). Apres avoir recu la confirmat
          +ion `PortesOuvertes`, il attend un delai symbolique (montee/descente voyageurs - en realite quelques secondes), puis envoie `FermeturePortes`. Apres `PortesFermees`, il envoie `DepartQua
          +i` au `QuaiController` et passe a `comportementHors`.                                                                                                                                     
      158 +                                                                                                                                                                                          
      159 +Pour les tests deterministes : le delai d'embarquement est encode comme un message immediat (pas de `Thread.sleep`). En production reelle, ce serait un `context.scheduleOnce`.           
      160 +                                                                                                                                                                                          
      161 +### Q14 - Comment la transition Petri `T1_depart_quai` modelise-t-elle "Portes_fermees [read]" ? [NEW Phase 7]                                                                            
      162 +                                                                                                                                                                                          
      163 +**Reponse** : en Petri ordinaire, on n'a pas de read-arc natif. On emule en consommant `Portes_fermees` puis en le reproduisant (cf petri-troncon.md section 3 : `pre={T1_a_quai, Portes_f
          +ermees}, post={T1_hors, Quai_libre, Portes_fermees}`). Net effet : `Portes_fermees` reste a 1 (lecture), mais la transition n'est tirable que si `Portes_fermees=1`. C'est exactement la s
          +emantique recherchee.                                                                                                                                                                     
      164 +                                                                                                                                                                                          
      165 +### Q15 - Quelle preuve donne-t-on de la surete PSD-Open dans le rapport ? [NEW Phase 7]                                                                                                  
      166 +                                                                                                                                                                                          
      167 +**Reponse** : trois niveaux de preuve convergents (cf rapport.md section 5.5) :                                                                                                           
      168 +1. **Structurelle** : la place `Portes_ouvertes` ne peut etre marquee que par les transitions `Ouverture_portes_Ti`, dont le pre contient `Ti_a_quai`. Donc tant que `Portes_ouvertes=1`, 
          +un train est a quai.                                                                                                                                                                      
      169 +2. **Inductive** : par recurrence sur le marquage initial et chaque transition (analogue a la preuve de l'invariant principal).                                                           
      170 +3. **Programmatique** : l'analyseur Scala enumere les marquages atteignables et verifie pour chacun que `Portes_ouvertes=1 => T1_a_quai+T2_a_quai=1`. Si la verification echoue sur un seu
          +l marquage, l'analyseur signale une violation (cf `verifierSurteOuverturePortes` dans `Analyseur.scala`).                                                                                 
      171 +                                                                                                                                                                                          
      172  ---
      173  
      174  ## 5) Points de synchronisation

⏺ Update(documentation/gouvernance/protocole-coordination.md)                      
  ⎿  Added 13 lines, removed 11 lines                                                     
      12                                  
      13  Chaque information du projet a **une seule** source de verite. En cas de conflit, ce tableau dit qui gagne.                                                                                       
      14  
      15 -| Information                             | Source de verite                                  | Qui edite        | Qui doit relire   |                                                            
      16 -|-----------------------------------------|---------------------------------------------------|------------------|-------------------|                                                     
      17 -| Modele formel (places, transitions)     | `petri/petri-troncon.md`                          | Piste A (Nikko)  | Toute l'equipe    |                                                            
      18 -| Vocabulaire metier <-> code <-> Petri   | `documentation/gouvernance/lexique.md`                        | Piste A          | Piste B avant chaque commit |                               
      19 -| Protocole de messages Akka              | `src/main/scala/m14/troncon/Protocol.scala`       | Piste B          | Piste A           |                                                     
      20 -| Logique d'arbitrage du controleur       | `src/main/scala/m14/troncon/SectionController.scala` | Piste B       | Piste A pour validation FIFO |                                          
      21 -| Machine a etats du train                | `src/main/scala/m14/troncon/Train.scala`          | Piste B          | Piste A           |                                                     
      22 -| Preuves d'invariants a la main          | `documentation/livrables/preuves-manuelles.md`              | Piste A          | Piste B en Phase 6 |                                          
      23 -| Correspondance scenarios <-> transitions | `documentation/livrables/comparaison.md`                   | Piste A + Piste B (co-redige) | Toute l'equipe |                                 
      24 -| Sortie programmatique de l'analyseur    | `src/main/scala/m14/petri/` + log de sortie       | Piste B          | Piste A en Phase 6 |                                                    
      25 -| Rapport final                           | `documentation/livrables/rapport.md`                        | Toute l'equipe   | Toute l'equipe |                                              
      15 +| Information                                | Source de verite                                                | Qui edite                     | Qui doit relire             |             
      16 +|--------------------------------------------|-----------------------------------------------------------------|-------------------------------|-----------------------------|             
      17 +| Modele formel (places, transitions, PSD)   | `petri/petri-troncon.md`                                        | Piste A                       | Toute l'equipe              |             
      18 +| Vocabulaire metier <-> code <-> Petri      | `documentation/gouvernance/lexique.md`                          | Piste A                       | Piste B avant chaque commit |             
      19 +| Protocole de messages Akka (8+4 messages)  | `src/main/scala/m14/troncon/Protocol.scala`                     | Piste B                       | Piste A                     |             
      20 +| Arbitrage du canton                        | `src/main/scala/m14/troncon/SectionController.scala`            | Piste B                       | Piste A pour validation FIFO|             
      21 +| Arbitrage du quai (NEW)                    | `src/main/scala/m14/troncon/QuaiController.scala`               | Piste B                       | Piste A pour validation FIFO|             
      22 +| Garde de surete des portes (NEW, CRITIQUE) | `src/main/scala/m14/troncon/GestionnairePortes.scala`           | Piste B                       | Piste A obligatoire         |             
      23 +| Machine a etats du train (4 etats)         | `src/main/scala/m14/troncon/Train.scala`                        | Piste B                       | Piste A                     |             
      24 +| Preuves d'invariants a la main             | `documentation/livrables/preuves-manuelles.md`                  | Piste A                       | Piste B en Phase D          |             
      25 +| Correspondance scenarios <-> transitions   | `documentation/livrables/comparaison.md`                        | Piste A + Piste B (co-redige) | Toute l'equipe              |             
      26 +| Sortie programmatique de l'analyseur       | `src/main/scala/m14/petri/` + log                               | Piste B                       | Piste A en Phase D          |             
      27 +| Rapport final                              | `documentation/livrables/rapport.md`                            | Toute l'equipe                | Toute l'equipe              |             
      28  
      29  **Regle** : si tu modifies un fichier, tu es responsable de notifier les autres editeurs concernes via une entree dans `documentation/suivi/historique.md` **dans la meme session**.
      30  

⏺ Maintenant le carnet de preuves manuelles — réécriture majeure pour l'espace d'états étendu.
                                                                                          
  Read 1 file (ctrl+o to expand)    
                                                                                                                                                                                                            
⏺ Write(documentation/livrables/preuves-manuelles.md)
  ⎿  Added 238 lines, removed 166 lines                                                                                                                                                                     
       1 -# Carnet de preuves manuelles - CriticalSystemModel                                                                                                                                        
       1 +# Carnet de preuves manuelles - CriticalSystemModel (modele etendu PSD)                                                                                                                           
       2                                                                                                                                                                                                    
       3  > **A qui s'adresse ce document** : aux contributeurs de l'equipe qui ne touchent pas au code Scala mais qui prennent en charge la verification formelle a la main, conformement au cahier        
          des charges (interdiction d'utiliser un outil logiciel de Petri).                                                                                                                                 
       4  >                                                                                                                                                                                               
       5  > **Pourquoi ce travail est central** : le sujet exige une preuve manuelle des proprietes, l'analyseur Scala (livrable L5) ne fait que **confirmer programmatiquement** ce qui aura ete pro       
          uve ici. Sans ce carnet rempli, le rapport (L4) n'a pas de fondations.
       6  >
       7 -> **Quand commencer** : maintenant. Ce travail est independant du code. Il converge avec le code en Phase 6 quand on compare les sorties de l'analyseur a ce carnet.                       
       7 +> **Mise a jour majeure 29/04** : carnet etendu pour le modele Canton + Quai + Portes palieres (PSD). 12 places, 12 transitions, ~15-18 marquages atteignables attendus. Le carnet initial 
         +a 8 marquages est archive ci-dessous a titre indicatif.                                                                                                                                    
       8  
       9  ---
      10  
     ...
       12  
       13  Avant de remplir ce carnet, lire dans cet ordre :
       14  
       15 -1. **`petri/petri-troncon.md`** sections 1 a 5 : definition complete du reseau, invariant principal prouve sur chaque transition.                                                         
       16 -2. **`documentation/gouvernance/lexique.md`** : pour ne jamais s'eloigner du vocabulaire commun.                                                                                          
       17 -3. **`documentation/livrables/comparaison.md`** sections 2 a 4 : matrice scenarios x (message Akka, transition Petri, marquage).                                                          
       15 +1. **`petri/petri-troncon.md`** sections 1 a 8 : definition complete du nouveau reseau (Canton + Quai + PSD), invariants prouves par induction.                                           
       16 +2. **`documentation/gouvernance/lexique.md`** : 4 etats par train, 4 ressources globales, 8 messages Akka.                                                                                
       17 +3. **`documentation/gouvernance/protocole-coordination.md`** Q11-Q15 : decisions liees a l'extension PSD.                                                                                 
       18  
       19  Tout ce qui suit dans ce carnet **prolonge** ces documents avec les preuves manuelles complementaires.
       20  
       21  ---
       22  
       23 -## Tache 1 - Enumeration exhaustive de l'espace d'etats (par BFS a la main)                                                                                                               
       23 +## Tache 1 - Enumeration exhaustive de l'espace d'etats (BFS a la main)                                                                                                                   
       24  
       25  ### Objectif
       26  
       27 -Lister **tous** les marquages atteignables depuis M0 en explorant en largeur, et les indexer M0, M1, M2, ... pour pouvoir y referer.                                                      
       27 +Lister **tous** les marquages atteignables depuis M0 en explorant en largeur, et les indexer M0, M1, M2, ... Le nombre attendu est **15 a 18 marquages** (a confirmer par l'analyseur Scal
          +a).                                                                                                                                                                                       
       28  
       29  ### Methode
       30  
       31 -A chaque marquage, on liste les transitions tirables (celles dont toutes les places d'entree ont un jeton), on calcule les marquages successeurs, on ajoute ceux qui sont nouveaux a la fi
          -le d'exploration.                                                                                                                                                                         
       31 +A chaque marquage, on liste les transitions tirables (celles dont toutes les places de pre ont un jeton), on calcule les marquages successeurs, on ajoute ceux qui sont nouveaux a la file
          + d'exploration.                                                                                                                                                                           
       32  
       33 -Notation compacte : on liste seulement les places marquees a 1 (les autres sont a 0).                                                                                                     
       33 +**Notation compacte** : on liste seulement les places marquees a 1 (les autres sont a 0). Les ressources globales sont prefixees pour la lisibilite.                                      
       34  
       35 -### Tableau a remplir                                                                                                                                                                     
       35 +### Tableau partiellement pre-rempli (les 9 premiers marquages, le reste est a completer par l'equipe)                                                                                    
       36  
       37 -| Index | Marquage (places marquees a 1) | Atteint via      | Transitions tirables depuis ce marquage | Successeurs |                                                                     
       38 -|------:|--------------------------------|------------------|-----------------------------------------|-------------|                                                                     
       39 -| **M0** | `Troncon_libre, T1_hors, T2_hors`         | (initial)            | `T1_demande`, `T2_demande` | M1, M2 |                                                                       
       40 -| **M1** | `Troncon_libre, T1_attente, T2_hors`      | M0 + `T1_demande`    | `T1_entree_autorisee`, `T2_demande` | M3, M4 |                                                              
       41 -| **M2** | `Troncon_libre, T1_hors, T2_attente`      | M0 + `T2_demande`    | `T2_entree_autorisee`, `T1_demande` | M5, M4 |                                                              
       42 -| **M3** | `T1_sur_troncon, T2_hors`                  | M1 + `T1_entree_autorisee` | `T1_sortie_liberation`, `T2_demande` | M0 (via sortie), M6 |                                         
       43 -| **M4** | `Troncon_libre, T1_attente, T2_attente`   | M1 + `T2_demande` (ou M2 + `T1_demande`) | `T1_entree_autorisee`, `T2_entree_autorisee` | M6, M7 |                                 
       44 -| **M5** | `T2_sur_troncon, T1_hors`                  | M2 + `T2_entree_autorisee` | `T2_sortie_liberation`, `T1_demande` | M0 (via sortie), M7 |                                         
       45 -| **M6** | `T1_sur_troncon, T2_attente`               | M3 + `T2_demande` (ou M4 + `T1_entree_autorisee`) | `T1_sortie_liberation` | M2 (via sortie) |                                    
       46 -| **M7** | `T2_sur_troncon, T1_attente`               | M5 + `T1_demande` (ou M4 + `T2_entree_autorisee`) | `T2_sortie_liberation` | M1 (via sortie) |                                    
       37 +| Index | Marquage (places a 1)                                                                  | Atteint via                                | Transitions tirables                      
          +            | Successeurs |                                                                                                                                                               
       38 +|------:|----------------------------------------------------------------------------------------|--------------------------------------------|-------------------------------------------
          +------------|-------------|                                                                                                                                                               
       39 +| **M0** | `Canton_libre, Quai_libre, Portes_fermees, T1_hors, T2_hors`                           | (initial)                                  | `T1_demande`, `T2_demande`               
          +             | M1, M2      |                                                                                                                                                              
       40 +| **M1** | `Canton_libre, Quai_libre, Portes_fermees, T1_attente, T2_hors`                        | M0 + `T1_demande`                          | `T1_entree_canton`, `T2_demande`         
          +             | M3, M4      |                                                                                                                                                              
       41 +| **M2** | `Canton_libre, Quai_libre, Portes_fermees, T1_hors, T2_attente`                        | M0 + `T2_demande`                          | `T2_entree_canton`, `T1_demande`         
          +             | M5, M4      |                                                                                                                                                              
       42 +| **M3** | `Quai_libre, Portes_fermees, T1_sur_canton, T2_hors`                                   | M1 + `T1_entree_canton`                    | `T1_arrivee_quai`, `T2_demande`          
          +             | M6, M7      |                                                                                                                                                              
       43 +| **M4** | `Canton_libre, Quai_libre, Portes_fermees, T1_attente, T2_attente`                     | M1 + `T2_demande` (ou M2 + `T1_demande`)   | `T1_entree_canton`, `T2_entree_canton`   
          +             | M7, M8      |                                                                                                                                                              
       44 +| **M5** | `Quai_libre, Portes_fermees, T1_hors, T2_sur_canton`                                   | M2 + `T2_entree_canton`                    | `T2_arrivee_quai`, `T1_demande`          
          +             | M9, M8      |                                                                                                                                                              
       45 +| **M6** | `Canton_libre, Portes_fermees, T1_a_quai, T2_hors`                                     | M3 + `T1_arrivee_quai`                     | `Ouverture_portes_T1`, `T2_demande`      
          +             | M10, M11    |                                                                                                                                                              
       46 +| **M7** | `Quai_libre, Portes_fermees, T1_sur_canton, T2_attente`                                | M3 + `T2_demande` (ou M4 + `T1_entree_canton`) | `T1_arrivee_quai`                    
          +            | M11         |                                                                                                                                                               
       47 +| **M8** | `Quai_libre, Portes_fermees, T1_attente, T2_sur_canton`                                | M4 + `T2_entree_canton` (ou M5 + `T1_demande`) | `T2_arrivee_quai`                    
          +            | M12         |                                                                                                                                                               
       48 +| **M9** | `Canton_libre, Portes_fermees, T1_hors, T2_a_quai`                                     | M5 + `T2_arrivee_quai`                     | `Ouverture_portes_T2`, `T1_demande`      
          +             | M13, M14    |                                                                                                                                                              
       49  
       50 +### Marquages a completer par l'equipe (M10 a M17 estimes)                                                                                                                                
       51 +                                                                                                                                                                                          
       52 +A explorer en suivant la meme methode :                                                                                                                                                   
       53 +                                                                                                                                                                                          
       54 +| Index | Marquage (places a 1)                                                                | Atteint via                                | Transitions tirables                        
          +        | Successeurs |                                                                                                                                                                   
       55 +|------:|--------------------------------------------------------------------------------------|--------------------------------------------|---------------------------------------------
          +--------|-------------|                                                                                                                                                                   
       56 +| **M10** | _ (resultat de M6 + Ouverture_portes_T1)                                            | M6 + `Ouverture_portes_T1`                 | `Fermeture_portes_T1`, `T2_demande`        
          +         | _, _        |                                                                                                                                                                  
       57 +| **M11** | _ (resultat de M6 + T2_demande, ou M7 + T1_arrivee_quai)                            | M6 + `T2_demande` ou M7 + `T1_arrivee_quai` | _                                         
          +         | _           |                                                                                                                                                                  
       58 +| **M12** | _                                                                                    | M8 + `T2_arrivee_quai`                     | _                                         
          +          | _           |                                                                                                                                                                 
       59 +| **M13** | _                                                                                    | M9 + `Ouverture_portes_T2`                 | _                                         
          +          | _           |                                                                                                                                                                 
       60 +| **M14** | _                                                                                    | M9 + `T1_demande`                          | _                                         
          +          | _           |                                                                                                                                                                 
       61 +| **M15-M17** | _ (probablement marquages avec Portes_ouvertes + train a quai + autre train sur canton/attente) | _ | _ | _ |                                                             
       62 +                                                                                                                                                                                          
       63  **A verifier par l'equipe** :
       49 -- [ ] Chaque ligne est correcte : la transition citee est bien tirable depuis le marquage precedent.                                                                                      
       50 -- [ ] Chaque successeur est bien deja indexe (pas de marquage manquant).                                                                                                                  
       51 -- [ ] Aucun nouveau marquage n'apparait au-dela de M7 (l'exploration est close).                                                                                                          
       64 +- [ ] Tous les marquages avec `Portes_ouvertes=1` doivent **necessairement** contenir `T1_a_quai=1` ou `T2_a_quai=1` (surete PSD).                                                        
       65 +- [ ] Aucun marquage ne contient `T1_sur_canton=1` ET `T2_sur_canton=1` simultanement (exclusion canton).                                                                                 
       66 +- [ ] Aucun marquage ne contient `T1_a_quai=1` ET `T2_a_quai=1` simultanement (exclusion quai).                                                                                           
       67 +- [ ] Aucun marquage n'est un deadlock (au moins une transition tirable depuis chaque etat).                                                                                              
       68  
       53 -**Resultat attendu** : exactement **8 marquages atteignables**. C'est la valeur de reference que l'analyseur Scala devra reproduire (Phase 6).                                            
       69 +**Resultat attendu** : entre **15 et 18 marquages**. Si plus, l'equipe et l'analyseur doivent diverger : appliquer le cas D du protocole-coordination.                                    
       70  
       71  ---
       72  
       57 -## Tache 2 - Verification de l'invariant principal sur chaque marquage                                                                                                                    
       73 +## Tache 2 - Verification des invariants de ressource                                                                                                                                     
       74  
       59 -### Objectif                                                                                                                                                                              
       75 +Pour chaque marquage atteignable Mi, verifier les **3 invariants de ressource**.                                                                                                          
       76  
       61 -Confirmer que `M(T1_sur_troncon) + M(T2_sur_troncon) + M(Troncon_libre) = 1` sur **chacun** des 8 marquages enumeres.                                                                     
       77 +### 2.1 Invariant canton                                                                                                                                                                  
       78  
       63 -### Tableau a remplir                                                                                                                                                                     
       79 +Confirmer : `M(T1_sur_canton) + M(T2_sur_canton) + M(Canton_libre) = 1`                                                                                                                   
       80  
       65 -| Marquage | `T1_sur_troncon` | `T2_sur_troncon` | `Troncon_libre` | Somme | OK ? |                                                                                                       
       66 -|----------|------------------:|------------------:|-----------------:|------:|:----:|                                                                                                    
       81 +| Marquage | T1_sur_canton | T2_sur_canton | Canton_libre | Somme | OK ? |                                                                                                                
       82 +|----------|--------------:|--------------:|-------------:|------:|:----:|                                                                                                                
       83  | M0       | 0 | 0 | 1 | 1 | ✓ |
       84  | M1       | 0 | 0 | 1 | 1 | ✓ |
       85  | M2       | 0 | 0 | 1 | 1 | ✓ |
       86  | M3       | 1 | 0 | 0 | 1 | ✓ |
       87  | M4       | 0 | 0 | 1 | 1 | ✓ |
       88  | M5       | 0 | 1 | 0 | 1 | ✓ |
       89 +| M6       | 0 | 0 | 1 | 1 | ✓ |                                                                                                                                                          
       90 +| M7       | 1 | 0 | 0 | 1 | ✓ |                                                                                                                                                          
       91 +| M8       | 0 | 1 | 0 | 1 | ✓ |                                                                                                                                                          
       92 +| M9       | 0 | 0 | 1 | 1 | ✓ |                                                                                                                                                          
       93 +| M10-M17  | _ | _ | _ | ? | _ a remplir_ |                                                                                                                                               
       94 +                                                                                                                                                                                          
       95 +### 2.2 Invariant quai                                                                                                                                                                    
       96 +                                                                                                                                                                                          
       97 +Confirmer : `M(T1_a_quai) + M(T2_a_quai) + M(Quai_libre) = 1`                                                                                                                             
       98 +                                                                                                                                                                                          
       99 +| Marquage | T1_a_quai | T2_a_quai | Quai_libre | Somme | OK ? |                                                                                                                          
      100 +|----------|----------:|----------:|-----------:|------:|:----:|                                                                                                                          
      101 +| M0       | 0 | 0 | 1 | 1 | ✓ |                                                                                                                                                          
      102 +| M1       | 0 | 0 | 1 | 1 | ✓ |                                                                                                                                                          
      103 +| M2       | 0 | 0 | 1 | 1 | ✓ |                                                                                                                                                          
      104 +| M3       | 0 | 0 | 1 | 1 | ✓ |                                                                                                                                                          
      105 +| M4       | 0 | 0 | 1 | 1 | ✓ |                                                                                                                                                          
      106 +| M5       | 0 | 0 | 1 | 1 | ✓ |                                                                                                                                                          
      107  | M6       | 1 | 0 | 0 | 1 | ✓ |
       74 -| M7       | 0 | 1 | 0 | 1 | ✓ |                                                                                                                                                          
      108 +| M7       | 0 | 0 | 1 | 1 | ✓ |                                                                                                                                                          
      109 +| M8       | 0 | 0 | 1 | 1 | ✓ |                                                                                                                                                          
      110 +| M9       | 0 | 1 | 0 | 1 | ✓ |                                                                                                                                                          
      111 +| M10-M17  | _ | _ | _ | ? | _ a remplir_ |                                                                                                                                               
      112  
       76 -**Conclusion attendue** : l'invariant tient sur les 8 marquages. Aucun marquage ne contient simultanement `T1_sur_troncon` ET `T2_sur_troncon` a 1. Donc **l'exclusion mutuelle est garant
          -ie**.                                                                                                                                                                                     
      113 +### 2.3 Invariant portes                                                                                                                                                                  
      114  
       78 -### Verification croisee de la preuve par induction (deja faite)                                                                                                                          
      115 +Confirmer : `M(Portes_fermees) + M(Portes_ouvertes) = 1`                                                                                                                                  
      116  
       80 -La preuve par induction (chaque transition preserve la somme) est dans `petri/petri-troncon.md` section 5. La verification ci-dessus est la **preuve par enumeration**. Les deux concorden
          -t : c'est la convergence des deux methodes qui donne la confiance.                                                                                                                        
      117 +A remplir par l'equipe pour les 18 marquages. Tous doivent avoir somme = 1.                                                                                                               
      118  
      119  ---
      120  
       84 -## Tache 3 - Verification des invariants par train                                                                                                                                        
      121 +## Tache 2bis (NEW) - Verification des invariants critiques de surete PSD                                                                                                                 
      122  
       86 -### Objectif                                                                                                                                                                              
      123 +C'est **le coeur du nouveau livrable**. Sans cette tache, l'extension PSD n'a pas de valeur formelle.                                                                                     
      124  
       88 -Confirmer que pour chaque train i ∈ {1, 2}, sur chaque marquage : `M(Ti_hors) + M(Ti_attente) + M(Ti_sur_troncon) = 1`.                                                                   
      125 +### 2bis.1 Surete PSD-Open : portes ouvertes implique train a quai                                                                                                                        
      126  
       90 -### Tableau a remplir pour Train 1                                                                                                                                                        
      127 +Pour chaque marquage Mi tel que `M(Portes_ouvertes) = 1`, verifier que `M(T1_a_quai) + M(T2_a_quai) = 1`.                                                                                 
      128  
       92 -| Marquage | `T1_hors` | `T1_attente` | `T1_sur_troncon` | Somme |                                                                                                                        
       93 -|----------|----------:|-------------:|-----------------:|------:|                                                                                                                        
       94 -| M0       | 1 | 0 | 0 | 1 |                                                                                                                                                              
       95 -| M1       | 0 | 1 | 0 | 1 |                                                                                                                                                              
       96 -| M2       | 1 | 0 | 0 | 1 |                                                                                                                                                              
       97 -| M3       | 0 | 0 | 1 | 1 |                                                                                                                                                              
       98 -| M4       | 0 | 1 | 0 | 1 |                                                                                                                                                              
       99 -| M5       | 1 | 0 | 0 | 1 |                                                                                                                                                              
      100 -| M6       | 0 | 0 | 1 | 1 |                                                                                                                                                              
      101 -| M7       | 0 | 1 | 0 | 1 |                                                                                                                                                              
      129 +Liste prevue (a remplir) :                                                                                                                                                                
      130  
      103 -### Tableau a remplir pour Train 2 (a faire par l'equipe en miroir)                                                                                                                       
      131 +| Marquage avec Portes_ouvertes=1 | T1_a_quai | T2_a_quai | Train a quai ? | OK ? |                                                                                                       
      132 +|---------------------------------|----------:|----------:|---------------:|:----:|                                                                                                       
      133 +| M10 (= M6 + Ouverture_T1)       | 1 | 0 | OUI | ✓ |                                                                                                                                     
      134 +| M13 (= M9 + Ouverture_T2)       | 0 | 1 | OUI | ✓ |                                                                                                                                     
      135 +| _ Autres a identifier_           | _ | _ | _ | _ |                                                                                                                                      
      136  
      105 -| Marquage | `T2_hors` | `T2_attente` | `T2_sur_troncon` | Somme |                                                                                                                        
      106 -|----------|----------:|-------------:|-----------------:|------:|                                                                                                                        
      107 -| M0       | 1 | 0 | 0 | 1 |                                                                                                                                                              
      108 -| M1       | 1 | 0 | 0 | 1 |                                                                                                                                                              
      109 -| M2       | 0 | 1 | 0 | 1 |                                                                                                                                                              
      110 -| M3       | 1 | 0 | 0 | 1 |                                                                                                                                                              
      111 -| M4       | 0 | 1 | 0 | 1 |                                                                                                                                                              
      112 -| M5       | 0 | 0 | 1 | 1 |                                                                                                                                                              
      113 -| M6       | 0 | 1 | 0 | 1 |                                                                                                                                                              
      114 -| M7       | 0 | 0 | 1 | 1 |                                                                                                                                                              
      137 +**Conclusion attendue** : tout marquage avec portes ouvertes a strictement un train a quai. Aucune chute possible. **CRITIQUE M14**.                                                      
      138  
      116 -**A faire** : un membre de l'equipe remplit ce tableau et confirme que toutes les sommes valent 1.                                                                                        
      139 +### 2bis.2 Surete PSD-Departure : depart possible implique portes fermees                                                                                                                 
      140  
      141 +Pour chaque marquage Mi, identifier si la transition `Ti_depart_quai` est tirable. Si oui, verifier que `M(Portes_fermees) = 1`.                                                          
      142 +                                                                                                                                                                                          
      143 +| Marquage | Ti_depart_quai tirable ? | Lequel ? | Portes_fermees ? | OK ? |                                                                                                              
      144 +|----------|:------------------------:|----------|-----------------:|:----:|                                                                                                              
      145 +| M6       | OUI | T1_depart_quai (T1_a_quai=1, Portes_fermees=1) | 1 | ✓ |                                                                                                               
      146 +| M9       | OUI | T2_depart_quai (T2_a_quai=1, Portes_fermees=1) | 1 | ✓ |                                                                                                               
      147 +| M10      | NON | (Portes_ouvertes=1, donc Portes_fermees=0) | 0 | (transition non tirable, OK) |                                                                                        
      148 +| M13      | NON | (Portes_ouvertes=1) | 0 | (transition non tirable, OK) |                                                                                                               
      149 +| _ Autres_ | _ | _ | _ | _ |                                                                                                                                                             
      150 +                                                                                                                                                                                          
      151 +**Conclusion attendue** : un train ne peut quitter le quai que portes fermees. **CRITIQUE M14**.                                                                                          
      152 +                                                                                                                                                                                          
      153  ---
      154  
      120 -## Tache 4 - Preuve d'absence de deadlock par exhaustion                                                                                                                                  
      155 +## Tache 3 - Cohérence par train (sur 4 etats au lieu de 3)                                                                                                                               
      156  
      122 -### Objectif                                                                                                                                                                              
      157 +Pour chaque train i ∈ {1, 2}, confirmer : `M(Ti_hors) + M(Ti_attente) + M(Ti_sur_canton) + M(Ti_a_quai) = 1`.                                                                             
      158  
      124 -Demontrer qu'aucun marquage atteignable n'est un deadlock. Un deadlock serait un marquage **different de M0** dans lequel **aucune** transition n'est tirable.                            
      159 +### Train 1 (a remplir pour les 18 marquages)                                                                                                                                             
      160  
      126 -### Tableau a remplir                                                                                                                                                                     
      161 +| Marquage | T1_hors | T1_attente | T1_sur_canton | T1_a_quai | Somme |                                                                                                                   
      162 +|----------|--------:|-----------:|--------------:|----------:|------:|                                                                                                                   
      163 +| M0       | 1 | 0 | 0 | 0 | 1 |                                                                                                                                                          
      164 +| M1       | 0 | 1 | 0 | 0 | 1 |                                                                                                                                                          
      165 +| M2       | 1 | 0 | 0 | 0 | 1 |                                                                                                                                                          
      166 +| M3       | 0 | 0 | 1 | 0 | 1 |                                                                                                                                                          
      167 +| M4       | 0 | 1 | 0 | 0 | 1 |                                                                                                                                                          
      168 +| M5       | 1 | 0 | 0 | 0 | 1 |                                                                                                                                                          
      169 +| M6       | 0 | 0 | 0 | 1 | 1 |                                                                                                                                                          
      170 +| M7       | 0 | 0 | 1 | 0 | 1 |                                                                                                                                                          
      171 +| M8       | 0 | 1 | 0 | 0 | 1 |                                                                                                                                                          
      172 +| M9       | 1 | 0 | 0 | 0 | 1 |                                                                                                                                                          
      173 +| M10-M17  | _ | _ | _ | _ | _ |                                                                                                                                                          
      174  
      128 -Pour chaque marquage Mi, lister au moins une transition tirable :                                                                                                                         
      175 +### Train 2 (a remplir en miroir)                                                                                                                                                         
      176  
      177 +A remplir par l'equipe.                                                                                                                                                                   
      178 +                                                                                                                                                                                          
      179 +---                                                                                                                                                                                       
      180 +                                                                                                                                                                                          
      181 +## Tache 4 - Absence de deadlock par exhaustion                                                                                                                                           
      182 +                                                                                                                                                                                          
      183 +Pour chaque marquage Mi, lister au moins une transition tirable. Aucun marquage atteignable ne doit etre un deadlock (sauf eventuellement le marquage initial, mais ici M0 a deja 2 transi
          +tions tirables).                                                                                                                                                                          
      184 +                                                                                                                                                                                          
      185  | Marquage | Au moins une transition tirable ? | Laquelle ? |
      186  |----------|:---------------------------------:|------------|
      132 -| M0       | Oui | `T1_demande` (ou `T2_demande`) |                                                                                                                                       
      133 -| M1       | Oui | `T1_entree_autorisee` (ou `T2_demande`) |                                                                                                                              
      134 -| M2       | Oui | `T2_entree_autorisee` (ou `T1_demande`) |                                                                                                                              
      135 -| M3       | Oui | `T1_sortie_liberation` (ou `T2_demande`) |                                                                                                                             
      136 -| M4       | Oui | `T1_entree_autorisee` (ou `T2_entree_autorisee`) |                                                                                                                     
      137 -| M5       | Oui | `T2_sortie_liberation` (ou `T1_demande`) |                                                                                                                             
      138 -| M6       | Oui | `T1_sortie_liberation` |                                                                                                                                               
      139 -| M7       | Oui | `T2_sortie_liberation` |                                                                                                                                               
      187 +| M0       | OUI | `T1_demande` ou `T2_demande` |                                                                                                                                         
      188 +| M1       | OUI | `T1_entree_canton` ou `T2_demande` |                                                                                                                                   
      189 +| M2       | OUI | `T2_entree_canton` ou `T1_demande` |                                                                                                                                   
      190 +| M3       | OUI | `T1_arrivee_quai` ou `T2_demande` |                                                                                                                                    
      191 +| M4       | OUI | `T1_entree_canton` ou `T2_entree_canton` |                                                                                                                             
      192 +| M5       | OUI | `T2_arrivee_quai` ou `T1_demande` |                                                                                                                                    
      193 +| M6       | OUI | `Ouverture_portes_T1` ou `T2_demande` |                                                                                                                                
      194 +| M7       | OUI | `T1_arrivee_quai` |                                                                                                                                                    
      195 +| M8       | OUI | `T2_arrivee_quai` |                                                                                                                                                    
      196 +| M9       | OUI | `Ouverture_portes_T2` ou `T1_demande` |                                                                                                                                
      197 +| M10      | OUI (a confirmer) | `Fermeture_portes_T1` ou `T2_demande` |                                                                                                                  
      198 +| M11-M17  | _ | _ a remplir_ |                                                                                                                                                           
      199  
      141 -**Conclusion attendue** : aucun marquage atteignable n'est un deadlock. Le systeme peut toujours progresser.                                                                              
      200 +**Conclusion attendue** : aucun marquage n'est un deadlock. Le systeme peut toujours progresser.                                                                                          
      201  
      143 -**Note importante** : ceci ne prouve pas la **liveness individuelle** (un train peut-il toujours sortir de l'attente ?). C'est l'objet de la tache 6.                                     
      144 -                                                                                                                                                                                          
      202  ---
      203  
      204  ## Tache 5 - Formalisation LTL des proprietes
      205  
      149 -### Objectif                                                                                                                                                                              
      206 +### Proprietes existantes (a maintenir)                                                                                                                                                   
      207  
      151 -Exprimer en logique LTL les proprietes que le systeme doit verifier. Travail de papier, pas de code.                                                                                      
      208 +**Safety canton (existant)** :                                                                                                                                                            
      209 +```                                                                                                                                                                                       
      210 +G !(T1_sur_canton AND T2_sur_canton)                                                                                                                                                      
      211 +```                                                                                                                                                                                       
      212 +Lecture : "il n'arrive jamais que les deux trains soient simultanement sur le canton".                                                                                                    
      213  
      153 -### Rappel des operateurs LTL                                                                                                                                                             
      214 +**Liveness canton (existant, sous fairness)** :                                                                                                                                           
      215 +```                                                                                                                                                                                       
      216 +G (T1_attente -> F T1_sur_canton)                                                                                                                                                         
      217 +G (T2_attente -> F T2_sur_canton)                                                                                                                                                         
      218 +```                                                                                                                                                                                       
      219  
      155 -- `G p` : "globalement p" - p est vrai a tout moment.                                                                                                                                     
      156 -- `F p` : "finalement p" - p sera vrai a un moment.                                                                                                                                       
      157 -- `X p` : "au prochain pas p".                                                                                                                                                            
      158 -- `p U q` : "p jusqu'a q" - p est vrai jusqu'a ce que q devienne vrai.                                                                                                                    
      220 +### Proprietes NEW pour le quai et les portes                                                                                                                                             
      221  
      160 -### Proprietes a formaliser                                                                                                                                                               
      161 -                                                                                                                                                                                          
      162 -**Safety 1 - Exclusion mutuelle** :                                                                                                                                                       
      222 +**Safety quai (NEW)** :                                                                                                                                                                   
      223  ```
      164 -G ¬ (T1_sur_troncon ∧ T2_sur_troncon)                                                                                                                                                     
      224 +G !(T1_a_quai AND T2_a_quai)                                                                                                                                                              
      225  ```
      166 -Lecture : "il n'arrive jamais que les deux trains soient simultanement sur le troncon".                                                                                                   
      226  
      168 -**Safety 2 - Invariant principal (forme LTL)** :                                                                                                                                          
      227 +**Safety PSD-Open (NEW, CRITIQUE)** :                                                                                                                                                     
      228  ```
      170 -G (T1_sur_troncon + T2_sur_troncon + Troncon_libre = 1)                                                                                                                                   
      229 +G (Portes_ouvertes -> (T1_a_quai OR T2_a_quai))                                                                                                                                           
      230  ```
      172 -Lecture : "a tout instant, exactement une des trois places est marquee".                                                                                                                  
      231 +Lecture : "chaque fois que les portes sont ouvertes, un train est a quai". C'est la propriete qui empeche les chutes mortelles.                                                           
      232  
      174 -**Liveness 1 - Progression de Train 1** :                                                                                                                                                 
      233 +**Safety PSD-Departure (NEW, CRITIQUE)** :                                                                                                                                                
      234  ```
      176 -G (T1_attente → F T1_sur_troncon)                                                                                                                                                         
      235 +G ((T1_a_quai AND X(T1_hors)) -> Portes_fermees)                                                                                                                                          
      236 +G ((T2_a_quai AND X(T2_hors)) -> Portes_fermees)                                                                                                                                          
      237  ```
      178 -Lecture : "chaque fois que Train 1 est en attente, il finira par etre sur le troncon".                                                                                                    
      179 -**Hypothese** : fairness sur l'arbitrage du controleur (il finit toujours par servir Train 1).                                                                                            
      238 +Lecture : "si un train passe de a_quai a hors (donc execute Ti_depart_quai), alors les portes etaient fermees a l'instant du passage". C'est la propriete qui empeche un voyageur d'etre c
          +oince.                                                                                                                                                                                    
      239  
      181 -**Liveness 2 - Progression de Train 2** :                                                                                                                                                 
      240 +**Liveness PSD (NEW, sous fairness des portes)** :                                                                                                                                        
      241  ```
      183 -G (T2_attente → F T2_sur_troncon)                                                                                                                                                         
      242 +G (T1_a_quai -> F Portes_ouvertes)                                                                                                                                                        
      243 +G (T2_a_quai -> F Portes_ouvertes)                                                                                                                                                        
      244  ```
      245 +Lecture : un train a quai finit par avoir les portes ouvertes (les voyageurs peuvent monter/descendre).                                                                                   
      246  
      247  ### Justification informelle sur l'espace d'etats fini
      248  
      188 -Pour chaque propriete, expliquer en 3-5 phrases pourquoi elle tient ou non sur l'espace d'etats des taches 1-2.                                                                           
      249 +A remplir par l'equipe pour chaque propriete : l'analyseur Scala enumere les marquages, on verifie la condition. Si elle tient sur tous les marquages, la propriete LTL est satisfaite (ju
          +stification "model checking sur etat fini").                                                                                                                                              
      250  
      190 -A remplir :                                                                                                                                                                               
      191 -- Safety 1 : verifie par enumeration (tache 2). Aucun marquage ne contient les deux jetons. _OK._                                                                                         
      192 -- Safety 2 : equivalent a l'invariant principal. _OK._                                                                                                                                    
      193 -- Liveness 1 : Garanti par l'implementation Akka via file d'attente FIFO du `SectionController`. Le reseau Petri le permet, l'implementation le force. _OK._                              
      194 -- Liveness 2 : Identique a Liveness 1 par symetrie. _OK._                                                                                                                                 
      195 -                                                                                                                                                                                          
      251  ---
      252  
      198 -## Tache 6 - Argumentation Liveness sous fairness                                                                                                                                         
      253 +## Tache 6 - Argumentation Liveness sous fairness (etendue)                                                                                                                               
      254  
      200 -### Objectif                                                                                                                                                                              
      255 +### Le probleme (rappel)                                                                                                                                                                  
      256  
      202 -Discuter honnetement la propriete de liveness. C'est la partie la plus subtile et celle ou un correcteur exigeant cherchera des trous.                                                    
      257 +En Petri pur, depuis un marquage avec 2 trains en attente, l'execution adversaire peut toujours servir le meme train, affamant l'autre.                                                   
      258  
      204 -### Le probleme                                                                                                                                                                           
      259 +### La parade (etendue avec PSD)                                                                                                                                                          
      260  
      206 -Dans le modele Petri pur, depuis M4 = `(Troncon_libre, T1_attente, T2_attente)`, **les deux** transitions `T1_entree_autorisee` et `T2_entree_autorisee` sont tirables. Le modele Petri ne
          - dit pas laquelle est choisie. Si une execution adversaire choisit toujours `T1_...` quand T2 attend, T2 attend pour toujours. C'est de la **famine** (absence de Liveness).              
      261 +Cote Akka :                                                                                                                                                                               
      262 +- Le `SectionController` a une file FIFO (existant).                                                                                                                                      
      263 +- Le `QuaiController` a une file FIFO (NEW). Quand un train sort du canton et trouve le quai libre, il y entre directement. Sinon il attend dans la file du quai.                         
      264 +- Le `GestionnairePortes` n'a pas de file (un seul train a quai a la fois grace a l'invariant 2.2). Pas de famine sur les portes.                                                         
      265  
      208 -### La parade                                                                                                                                                                             
      266 +### Limites assumees                                                                                                                                                                      
      267  
      210 -Cote Akka, le `SectionController` maintient une **file FIFO**. Quand T1 et T2 demandent dans cet ordre, T1 est servi en premier. Quand T1 sort et que T2 est dans la file, T2 est obligato
          -irement servi avant qu'une nouvelle demande de T1 puisse passer.                                                                                                                          
      268 +A remplir par l'equipe (5-10 lignes) :                                                                                                                                                    
      269 +- Le modele Petri pur ne garantit pas la Liveness sans hypothese de fairness.                                                                                                             
      270 +- L'implementation Akka avec FIFO la garantit pour toute sequence finie.                                                                                                                  
      271 +- Sur les 3 scenarios retenus, la Liveness est verifiable par inspection.                                                                                                                 
      272 +- **Limite specifique PSD** : si les portes ne se ferment jamais (bug dans le `GestionnairePortes` ou le `Train`), le train est coince a quai et la fairness echoue. Cas defensif a prouve
          +r par tests d'integration.                                                                                                                                                                
      273  
      212 -### Ce qu'on peut prouver, ce qu'on ne peut pas                                                                                                                                           
      213 -                                                                                                                                                                                          
      214 -A remplir par l'equipe en redaction libre (5-10 lignes) :                                                                                                                                 
      215 -                                                                                                                                                                                          
      216 -**Ce qu'on peut affirmer** :                                                                                                                                                              
      217 -- Le modele Petri pur ne garantit pas la Liveness sans hypothese supplementaire.                                                                                                          
      218 -- L'implementation Akka avec file FIFO garantit la Liveness pour toute sequence finie de demandes.                                                                                        
      219 -- Sur les 3 scenarios retenus, la Liveness est verifiable directement par inspection.                                                                                                     
      220 -                                                                                                                                                                                          
      221 -**Ce qu'on ne peut pas affirmer (a assumer comme limite)** :                                                                                                                              
      222 -- On ne prouve pas formellement les temps d'attente maximum (modele intemporel).                                                                                                          
      223 -- On ne verifie pas la robustesse face a des crashs systemes majeurs dans cette modelisation (hors scope).                                                                                
      224 -- L'analyse du code pour prouver le FIFO n'est faite que par test (Akka TestKit) et non par theoreme prouve (par ex. TLA+).                                                               
      225 -                                                                                                                                                                                          
      226 -### Resultat attendu                                                                                                                                                                      
      227 -                                                                                                                                                                                          
      228 -Cette discussion doit alimenter directement la section "Limites assumees" du rapport (`documentation/livrables/rapport.md` section 7).                                                    
      229 -                                                                                                                                                                                          
      274  ---
      275  
      232 -## Tache 7 - Diagramme de l'espace d'etats (graphique a la main)                                                                                                                          
      276 +## Tache 7 - Diagramme d'espace d'etats (graphe a la main)                                                                                                                                
      277  
      278  ### Objectif
      279  
      236 -Dessiner un graphe ou les noeuds sont les 8 marquages (M0 a M7) et les arcs sont les transitions. C'est la representation visuelle de tout le travail des taches 1-4.                     
      280 +Dessiner un graphe ou les noeuds sont les 15-18 marquages (M0 a M17 estimes) et les arcs sont les transitions. Cette representation visuelle est centrale pour le rapport (livrable L4 sec
          +tion 5).                                                                                                                                                                                  
      281  
      282  ### Format suggere
      283  
      240 -Un dessin a la main scanne, ou un diagramme ASCII, ou un schema fait dans n'importe quel outil (sauf un outil de Petri formel - **interdit** par le cahier des charges).                  
      284 +Un dessin a la main scanne, ou un diagramme ASCII, ou un schema fait dans un outil de graphe generique (PAS un outil de Petri formel, **interdit** par le cahier des charges).            
      285  
      242 -Squelette ASCII a etoffer :                                                                                                                                                               
      286 +### Squelette ASCII a etoffer                                                                                                                                                             
      287  
      288  ```
      245 -                    M0                                                                                                                                                                    
      246 -                  /    \                                                                                                                                                                  
      247 -            T1_dem     T2_dem                                                                                                                                                             
      248 -              /            \                                                                                                                                                              
      249 -            M1              M2                                                                                                                                                            
      250 -           / \              / \                                                                                                                                                           
      251 -       T1_ea  T2_dem    T2_ea  T1_dem                                                                                                                                                     
      252 -         /      \        /       \                                                                                                                                                        
      253 -        M3       M4 ====        M5                                                                                                                                                        
      254 -         |      / \              |                                                                                                                                                        
      255 -       T1_sl  T1_ea T2_ea      T2_sl                                                                                                                                                      
      256 -         |    /     \            |                                                                                                                                                        
      257 -        ...  M6     M7          ...                                                                                                                                                       
      289 +                              M0 (etat initial)                                                                                                                                           
      290 +                            /                  \                                                                                                                                          
      291 +                       T1_dem                T2_dem                                                                                                                                       
      292 +                          /                      \                                                                                                                                        
      293 +                        M1                        M2                                                                                                                                      
      294 +                       / \                        / \                                                                                                                                     
      295 +                T1_ec    T2_dem            T2_ec     T1_dem                                                                                                                               
      296 +                  /        \                /            \                                                                                                                                
      297 +                M3         M4              M5            ...                                                                                                                              
      298 +                 |          |               |                                                                                                                                             
      299 +              T1_aq      T1_ec/T2_ec     T2_aq                                                                                                                                            
      300 +                 |          |               |                                                                                                                                             
      301 +                M6         M7/M8           M9                                                                                                                                             
      302 +              / \                          / \                                                                                                                                            
      303 +        Ouv_T1  T2_dem                  Ouv_T2  T1_dem                                                                                                                                    
      304 +            /     \                       /        \                                                                                                                                      
      305 +          M10     M11                   M13         M14                                                                                                                                   
      306 +           |                              |                                                                                                                                               
      307 +        Ferm_T1                       Ferm_T2                                                                                                                                             
      308 +           |                              |                                                                                                                                               
      309 +          ...                            ...                                                                                                                                              
      310  ```
      311  
      260 -(Legende : `T1_dem` = `T1_demande`, `T1_ea` = `T1_entree_autorisee`, `T1_sl` = `T1_sortie_liberation`, idem pour T2.)                                                                     
      312 +(Legende : `T1_dem` = `T1_demande`, `T1_ec` = `T1_entree_canton`, `T1_aq` = `T1_arrivee_quai`, `Ouv_T1` = `Ouverture_portes_T1`, `Ferm_T1` = `Fermeture_portes_T1`, idem T2.)             
      313  
      262 -A faire : completer ce graphe en y faisant figurer **toutes** les transitions tirables depuis chaque marquage, y compris les boucles de retour vers M0 via les transitions de sortie.     
      314 +A faire par l'equipe : completer ce graphe avec **toutes** les transitions tirables, y compris les boucles de retour vers M0 via `Ti_depart_quai`.                                        
      315  
      316  ### Insertion finale
      317  
      266 -Ce diagramme sera repris dans le rapport (`documentation/livrables/rapport.md` section 5) comme illustration centrale de la verification.                                                 
      318 +Ce diagramme sera repris dans le rapport (`documentation/livrables/rapport.md` section 5) comme illustration centrale de la verification, avec mise en evidence des marquages "Portes_ouve
          +rtes" pour visualiser la propriete de surete.                                                                                                                                             
      319  
      320  ---
      321  
      322  ## Synchronisation avec la piste codage
      323  
      272 -### Quand l'analyseur Scala (livrable L5) sera operationnel (Phase 6)                                                                                                                     
      324 +### Quand l'analyseur Scala etendu (livrable L5) sera operationnel (Phase D)                                                                                                              
      325  
      326  Il devra produire :
      275 -- [ ] Une liste de 8 marquages atteignables (correspondance avec la tache 1).                                                                                                             
      276 -- [ ] Pour chaque marquage, la verification de l'invariant principal (correspondance avec la tache 2).                                                                                    
      277 -- [ ] Pour chaque marquage, la verification des invariants par train (correspondance avec la tache 3).                                                                                    
      278 -- [ ] La confirmation qu'aucun marquage n'est un deadlock (correspondance avec la tache 4).                                                                                               
      327 +- [ ] Une liste de **15-18 marquages** atteignables (correspondance avec la tache 1).                                                                                                     
      328 +- [ ] Verification des **3 invariants de ressource** sur chaque marquage (taches 2.1, 2.2, 2.3).                                                                                          
      329 +- [ ] Verification des **2 invariants critiques PSD** (taches 2bis.1, 2bis.2).                                                                                                            
      330 +- [ ] Verification des invariants par train sur 4 etats (tache 3).                                                                                                                        
      331 +- [ ] Confirmation qu'aucun marquage n'est un deadlock (tache 4).                                                                                                                         
      332  
      333  Si le code et le carnet divergent, **on debugge le code en priorite** (le carnet est la reference). En cas d'erreur dans le carnet, on corrige et on re-explique dans `documentation/suivi
           /historique.md`.
      334  
      335  ### Ou inserer les sorties dans le rapport
      336  
      284 -| Section du rapport | Source carnet | Source code |                                                                                                                                      
      285 -|--------------------|---------------|-------------|                                                                                                                                      
      286 -| 5.1 Invariant principal | Tache 2 | Sortie analyseur (Phase 6) |                                                                                                                        
      287 -| 5.2 Invariants par train | Tache 3 | Sortie analyseur |                                                                                                                                 
      288 -| 5.3 Absence de deadlock | Tache 4 | Sortie analyseur |                                                                                                                                  
      289 -| 5.4 Proprietes LTL | Taches 5 et 6 | (informel, pas de model checker code) |                                                                                                            
      290 -| Annexe A1 | Tache 7 (diagramme) | Sortie brute analyseur |                                                                                                                              
      337 +| Section du rapport       | Source carnet | Source code |                                                                                                                                
      338 +|--------------------------|---------------|-------------|                                                                                                                                
      339 +| 5.1 Invariant canton     | Tache 2.1     | Sortie analyseur (`verifierInvariantCanton`) |                                                                                               
      340 +| 5.2 Invariant quai       | Tache 2.2     | Sortie analyseur (`verifierInvariantQuai`)   |                                                                                               
      341 +| 5.3 Invariant portes     | Tache 2.3     | Sortie analyseur (`verifierInvariantPortes`) |                                                                                               
      342 +| **5.4 Surete PSD-Open**  | Tache 2bis.1  | Sortie analyseur (`verifierSurteOuverturePortes`) **CRITIQUE** |                                                                             
      343 +| **5.5 Surete PSD-Departure** | Tache 2bis.2 | Sortie analyseur (`verifierSurteDepartQuai`) **CRITIQUE** |                                                                               
      344 +| 5.6 Cohérence par train  | Tache 3       | Sortie analyseur                             |                                                                                               
      345 +| 5.7 Absence de deadlock  | Tache 4       | Sortie analyseur                             |                                                                                               
      346 +| 5.8 LTL                  | Taches 5 + 6  | (informel, justification sur etat fini)      |                                                                                               
      347 +| Annexe A1 (graphe)       | Tache 7       | Sortie brute analyseur                       |                                                                                               
      348  
      349  ---
      350  
      294 -## Repartition des taches                                                                                                                                                                 
      351 +## Repartition possible dans l'equipe                                                                                                                                                     
      352  
      296 -Le travail sur ce carnet est typiquement realise par le **Pole 1 (Preuves Formelles & LTL)**.                                                                                             
      297 -Les tâches 1-2-3-4 sont séquentielles (l'exploration génère les marquages). Les tâches 5-6-7 sont indépendantes une fois l'espace d'états connu.                                          
      353 +Suggestion (modulable selon les disponibilites) :                                                                                                                                         
      354  
      355 +| Membre        | Taches recommandees |                                                                                                                                                   
      356 +|---------------|---------------------|                                                                                                                                                   
      357 +| Contributeur 1 | Taches 1 (enumeration M0-M9) + 5 (LTL) |                                                                                                                               
      358 +| Contributeur 2 | Taches 1 (enumeration M10-M17) + 4 (deadlock) |                                                                                                                        
      359 +| Contributeur 3 | Taches 2 (3 invariants ressources) + 3 (cohérence par train) |                                                                                                         
      360 +| Contributeur 4 (Nikko) | Code Scala + tache 7 (diagramme final) + integration des resultats dans le rapport |                                                                           
      361 +                                                                                                                                                                                          
      362 +Les taches 1, 2, 4 sont sequentielles (la 1 produit les marquages que les autres utilisent), les taches 5, 6, 7 sont independantes apres la tache 1. La **tache 2bis (PSD)** est la plus i
          +mportante academiquement et doit etre relue par 2 contributeurs.                                                                                                                          
      363 +                                                                                                                                                                                          
      364  ---
      365  
      366  ## Definition of Done de ce carnet
     ...
      369  - [ ] Tous les tableaux sont remplis (pas de `_` ou `?` restants).
      370  - [ ] Le diagramme de la tache 7 est joint (image ou ASCII).
      371  - [ ] Une entree `historique.md` a ete ajoutee a chaque tache terminee.
      307 -- [ ] Les resultats convergent avec ceux de l'analyseur Scala en Phase 6.                                                                                                                 
      308 -- [ ] Les sections du rapport (5.1 a 5.4) ont ete remplies a partir de ce carnet.                                                                                                         
      372 +- [ ] Les resultats convergent avec ceux de l'analyseur Scala en Phase D.                                                                                                                 
      373 +- [ ] **La tache 2bis (PSD) est integralement verifiee et chaque marquage avec `Portes_ouvertes=1` a son train a quai documente.**                                                        
      374 +- [ ] Les sections 5.1 a 5.8 du rapport ont ete remplies a partir de ce carnet.                                                                                                           
      375 +                                                                                                                                                                                          
      376 +---                                                                                                                                                                                       
      377 +                                                                                                                                                                                          
      378 +## Annexe : carnet initial (modele a 7 places, archive)                                                                                                                                   
      379 +                                                                                                                                                                                          
      380 +L'enumeration originale a 8 marquages pour le modele "1 troncon, 7 places, 6 transitions" est conservee a titre historique dans le commit `bf7fbc1` ainsi que dans l'historique git. Elle 
          +a servi de base pedagogique avant l'extension PSD.                                                                                                                                        

  Read 1 file (ctrl+o to expand)          
  ⎿  You've hit your limit · resets 5:10pm (Europe/Paris)                                 
     /upgrade to increase your usage limit.
                                                                                                                                                                                                            
✻ Baked for 15m 40s                                                                       
                                                                                                                                                                                                            
───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────── m14-expansion-psd-model ──
❯                                                                                                                                                                                                           
────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
  ⏵⏵ accept edits on (shift+tab to cycle)                                                                                                                                                                 

      290 -| Annexe A1 | Tache 7 (diagramme) | Sortie brute analyseur |                                                                                                                              
      337 +| Section du rapport       | Source carnet | Source code |                                                                                                                                
      338 +|--------------------------|---------------|-------------|                                                                                                                                
      339 +| 5.1 Invariant canton     | Tache 2.1     | Sortie analyseur (`verifierInvariantCanton`) |                                                                                               
      340 +| 5.2 Invariant quai       | Tache 2.2     | Sortie analyseur (`verifierInvariantQuai`)   |                                                                                               
      341 +| 5.3 Invariant portes     | Tache 2.3     | Sortie analyseur (`verifierInvariantPortes`) |                                                                                               
      342 +| **5.4 Surete PSD-Open**  | Tache 2bis.1  | Sortie analyseur (`verifierSurteOuverturePortes`) **CRITIQUE** |                                                                             
      343 +| **5.5 Surete PSD-Departure** | Tache 2bis.2 | Sortie analyseur (`verifierSurteDepartQuai`) **CRITIQUE** |                                                                               
      344 +| 5.6 Cohérence par train  | Tache 3       | Sortie analyseur                             |                                                                                               
      345 +| 5.7 Absence de deadlock  | Tache 4       | Sortie analyseur                             |                                                                                               
      346 +| 5.8 LTL                  | Taches 5 + 6  | (informel, justification sur etat fini)      |                                                                                               
      347 +| Annexe A1 (graphe)       | Tache 7       | Sortie brute analyseur                       |                                                                                               
      348  
      349  ---
      350  
      294 -## Repartition des taches                                                                                                                                                                 
      351 +## Repartition possible dans l'equipe                                                                                                                                                     
      352  
      296 -Le travail sur ce carnet est typiquement realise par le **Pole 1 (Preuves Formelles & LTL)**.                                                                                             
      297 -Les tâches 1-2-3-4 sont séquentielles (l'exploration génère les marquages). Les tâches 5-6-7 sont indépendantes une fois l'espace d'états connu.                                          
      353 +Suggestion (modulable selon les disponibilites) :                                                                                                                                         
      354  
      355 +| Membre        | Taches recommandees |                                                                                                                                                   
      356 +|---------------|---------------------|                                                                                                                                                   
      357 +| Contributeur 1 | Taches 1 (enumeration M0-M9) + 5 (LTL) |                                                                                                                               
      358 +| Contributeur 2 | Taches 1 (enumeration M10-M17) + 4 (deadlock) |                                                                                                                        
      359 +| Contributeur 3 | Taches 2 (3 invariants ressources) + 3 (cohérence par train) |                                                                                                         
      360 +| Contributeur 4 (Nikko) | Code Scala + tache 7 (diagramme final) + integration des resultats dans le rapport |                                                                           
      361 +                                                                                                                                                                                          
      362 +Les taches 1, 2, 4 sont sequentielles (la 1 produit les marquages que les autres utilisent), les taches 5, 6, 7 sont independantes apres la tache 1. La **tache 2bis (PSD)** est la plus i
          +mportante academiquement et doit etre relue par 2 contributeurs.                                                                                                                          
      363 +                                                                                                                                                                                          
      364  ---
      365  
      366  ## Definition of Done de ce carnet
     ...
      369  - [ ] Tous les tableaux sont remplis (pas de `_` ou `?` restants).
      370  - [ ] Le diagramme de la tache 7 est joint (image ou ASCII).
      371  - [ ] Une entree `historique.md` a ete ajoutee a chaque tache terminee.
      307 -- [ ] Les resultats convergent avec ceux de l'analyseur Scala en Phase 6.                                                                                                                 
      308 -- [ ] Les sections du rapport (5.1 a 5.4) ont ete remplies a partir de ce carnet.                                                                                                         
      372 +- [ ] Les resultats convergent avec ceux de l'analyseur Scala en Phase D.                                                                                                                 
      373 +- [ ] **La tache 2bis (PSD) est integralement verifiee et chaque marquage avec `Portes_ouvertes=1` a son train a quai documente.**                                                        
      374 +- [ ] Les sections 5.1 a 5.8 du rapport ont ete remplies a partir de ce carnet.                                                                                                           
      375 +                                                                                                                                                                                          
      376 +---                                                                                                                                                                                       
      377 +                                                                                                                                                                                          
      378 +## Annexe : carnet initial (modele a 7 places, archive)                                                                                                                                   
      379 +                                                                                                                                                                                          
      380 +L'enumeration originale a 8 marquages pour le modele "1 troncon, 7 places, 6 transitions" est conservee a titre historique dans le commit `bf7fbc1` ainsi que dans l'historique git. Elle 
          +a servi de base pedagogique avant l'extension PSD.                                                                                                                                        

  Read 1 file (ctrl+o to expand)
  ⎿  You've hit your limit · resets 5:10pm (Europe/Paris)
     /upgrade to increase your usage limit.

✻ Baked for 15m 40s

───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────── m14-expansion-psd-model ──
❯  
────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
  ⏵⏵ accept edits on (shift+tab to cycle)

