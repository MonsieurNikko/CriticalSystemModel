# Lexique de coherence - CriticalSystemModel

> Document de pivot entre les 4 representations du systeme : metier, code Akka, reseau de Petri, tests.
> A consulter avant d'ouvrir un fichier de code que tu n'as pas ecrit.
>
> **Regle dure** : si un nom dans une colonne ne correspond pas exactement aux autres colonnes, c'est un bug de coherence et il faut renommer.
>
> **Mise a jour majeure le 29/04/2026** : extension du modele avec le quai (station) et les portes palieres (PSD). Le vocabulaire couvre desormais 4 etats par train et 4 ressources globales.

---

## 1) Etats des trains (4 etats)

| Sens metier                                          | Place Petri                          | Comportement Akka          | Etat dans les tests |
|------------------------------------------------------|--------------------------------------|----------------------------|---------------------|
| Train hors zone (amont du canton ou apres depart)    | `T1_hors`, `T2_hors`                 | `comportementHors`         | `hors`              |
| Train a demande l'acces, attend la reponse           | `T1_attente`, `T2_attente`           | `comportementEnAttente`    | `enAttente`         |
| Train circule sur le canton de signalisation         | `T1_sur_canton`, `T2_sur_canton`     | `comportementSurCanton`    | `surCanton`         |
| Train est a l'arret en station, sur le quai          | `T1_a_quai`, `T2_a_quai`             | `comportementAQuai`        | `aQuai`             |

**Regle** : aucun autre etat n'est introduit. Pas de "transition", pas de "demande_envoyee", pas de "en_route". Si le metier evolue, on amende le reseau de Petri en premier (livrable L3) puis on aligne le code et les tests.

**Renommage 29/04** : l'etat `surTroncon` du modele initial est renomme `surCanton` (vocabulaire ferroviaire reel). La place `Ti_sur_troncon` devient `Ti_sur_canton`.

---

## 2) Etats des controleurs

### 2.1 SectionController (canton de signalisation)

| Sens metier                       | Encodage Petri (place)   | Etat Akka du controleur          | Description                        |
|-----------------------------------|--------------------------|----------------------------------|------------------------------------|
| Le canton est libre               | `Canton_libre`           | `cantonLibre` (file vide)        | Aucun train sur le canton          |
| Train 1 occupe le canton          | `T1_sur_canton`          | `cantonOccupe(Train1, file)`     | Train 1 dedans, file FIFO eventuelle |
| Train 2 occupe le canton          | `T2_sur_canton`          | `cantonOccupe(Train2, file)`     | Train 2 dedans, file FIFO eventuelle |

### 2.2 QuaiController (quai en station) - NOUVEAU

| Sens metier                       | Encodage Petri (place)   | Etat Akka du controleur          | Description                        |
|-----------------------------------|--------------------------|----------------------------------|------------------------------------|
| Le quai est libre                 | `Quai_libre`             | `quaiLibre` (file vide)          | Aucun train a quai                 |
| Train 1 a quai                    | `T1_a_quai`              | `quaiOccupe(Train1, file)`       | Train 1 a quai, file FIFO eventuelle |
| Train 2 a quai                    | `T2_a_quai`              | `quaiOccupe(Train2, file)`       | Train 2 a quai, file FIFO eventuelle |

### 2.3 GestionnairePortes (portes palieres) - NOUVEAU

| Sens metier                       | Encodage Petri (place)   | Etat Akka                        | Description                                  |
|-----------------------------------|--------------------------|----------------------------------|----------------------------------------------|
| Portes palieres fermees           | `Portes_fermees=1`       | `portesFermees`                  | Etat initial. Securise.                      |
| Portes palieres ouvertes          | `Portes_ouvertes=1`      | `portesOuvertes(occupant)`       | Ne peut etre atteint que si un train a quai. |

**Garde de surete CRITIQUE (PSD)** : le `GestionnairePortes` REFUSE toute demande d'ouverture si aucun train n'est a quai. C'est l'invariant de surete d'ouverture (cf petri-troncon.md section 6.1).

**Note FIFO vs Petri** : les files FIFO Akka des controleurs sont des finesses d'implementation pour garantir la fairness en pratique. Elles n'ont pas d'equivalent direct dans le reseau de Petri (non deterministe). Cette dissymetrie est documentee.

---

## 3) Messages du protocole Akka (8 messages)

### Messages vers les controleurs

| Message                                  | Type                                | Sens metier                                              | Transition Petri associee                  |
|------------------------------------------|-------------------------------------|----------------------------------------------------------|--------------------------------------------|
| `Demande(emetteur, repondreA)`           | `MessagePourControleur`             | Le train demande l'acces au canton                       | `T1_demande` / `T2_demande`                |
| `Sortie(emetteur)`                       | `MessagePourControleur`             | Le train signale qu'il a quitte le canton (vers le quai) | `T1_arrivee_quai` / `T2_arrivee_quai` (libere canton) |
| `ArriveeQuai(emetteur, repondreA)`       | `MessagePourQuaiController` (NEW)   | Le train demande l'acces au quai                         | `T1_arrivee_quai` / `T2_arrivee_quai` (occupe quai) |
| `DepartQuai(emetteur)`                   | `MessagePourQuaiController` (NEW)   | Le train signale qu'il a quitte le quai                  | `T1_depart_quai` / `T2_depart_quai`        |
| `OuverturePortes(emetteur)`              | `MessagePourGestionnairePortes` (NEW) | Le train demande l'ouverture des portes palieres       | `Ouverture_portes_T1` / `Ouverture_portes_T2` |
| `FermeturePortes(emetteur)`              | `MessagePourGestionnairePortes` (NEW) | Le train demande la fermeture des portes              | `Fermeture_portes_T1` / `Fermeture_portes_T2` |

### Messages vers les trains

| Message                | Type                | Sens metier                                                |
|------------------------|---------------------|------------------------------------------------------------|
| `Autorisation`         | `MessagePourTrain`  | Un controleur autorise l'entree (canton ou quai)           |
| `Attente`              | `MessagePourTrain`  | Un controleur signale au train d'attendre                  |
| `PortesOuvertes`       | `MessagePourTrain` (NEW) | Le `GestionnairePortes` confirme que les portes sont ouvertes |
| `PortesFermees`        | `MessagePourTrain` (NEW) | Le `GestionnairePortes` confirme la fermeture          |

**Regle de verrouillage** : aucun nouveau message ne peut etre introduit sans mise a jour de `petri/petri-troncon.md` section 8 ET de ce lexique. Le protocole est verrouille a **8 messages cote controleurs + 4 messages cote trains** (12 au total).

**Note** : selon l'implementation, certains messages peuvent partager un type commun (`MessagePourTrain` pour `Autorisation`, `Attente`, `PortesOuvertes`, `PortesFermees`).

---

## 4) Glossaire des termes critiques

### Termes du metier ferroviaire

- **Canton de signalisation (block section)** : segment de voie ferree entre 2 signaux. Au plus un train a la fois pour eviter les collisions. Concept fondamental du block signaling.
- **PSD (Platform Screen Doors) / Portes palieres** : portes physiques sur le quai, alignees avec les portes du train. Empechent les chutes sur les voies. Signature de la M14, premiere ligne entierement equipee en France.
- **Quai** : zone d'embarquement/debarquement en station. Au plus un train a la fois (par sens) en pratique.
- **CBTC (Communication-Based Train Control)** : technologie de controle continu des trains automatiques M14.
- **Exclusion mutuelle** : propriete de surete garantissant qu'au plus un acteur acces a une ressource a la fois.
- **Arbitrage** : mecanisme par lequel un controleur decide qui peut entrer et qui doit attendre.
- **Fairness** : hypothese qu'un train en attente finit toujours par etre servi (pas de famine indefinie).

### Termes Akka

- **Acteur** : entite isolee qui recoit et traite des messages sequentiellement.
- **`Behavior[T]`** : description du comportement courant d'un acteur, specifiant comment il reagit aux messages de type T.
- **TestKit / Probe** : faux acteur utilise dans les tests pour intercepter et verifier les messages.

### Termes Petri

- **Place** : etat possible (par exemple `T1_attente`, `Portes_fermees`).
- **Marquage** : nombre de jetons dans chaque place a un instant donne.
- **Transition** : evenement qui consomme des jetons en entree et en produit en sortie.
- **Tirable** : une transition est tirable si toutes ses places d'entree ont au moins 1 jeton.
- **Read-arc emule** : technique pour modeliser une "lecture" en Petri ordinaire, en consommant et reproduisant le meme jeton (utilisee pour `Portes_fermees` dans `Ti_depart_quai`).
- **Espace d'etats accessible** : ensemble des marquages atteignables depuis le marquage initial.
- **P-invariant** : combinaison lineaire de places qui reste constante sur tous les marquages atteignables.

### Termes verification

- **Safety** : "rien de mal n'arrive jamais" (par exemple : pas d'occupation simultanee, portes ne s'ouvrent pas sans train).
- **Liveness** : "quelque chose de bien finit toujours par arriver" (par exemple : un train en attente finit par passer).
- **LTL** : Linear Temporal Logic, langage pour exprimer Safety et Liveness sur des chemins d'execution.
- **Surete PSD** : propriete de surete specifique aux portes palieres ; il en existe 2 (ouverture, demarrage).

---

## 5) Convention de nommage Scala (rappel court)

- Variables : noms longs et explicites en francais (`trainEnAttente`, `controleurCanton`, `gestionnairePortes`). Pas de `t`, `c`, `tmp`.
- Fonctions : verbe ou nom d'etat (`comportementHors`, `comportementAQuai`, `traiterDemande`). Court (<30 lignes).
- Classes / objets : substantif (`SectionController`, `QuaiController`, `GestionnairePortes`, `PetriNet`).
- Messages : substantif decrivant l'evenement (`Demande`, `Sortie`, `ArriveeQuai`, `DepartQuai`, `OuverturePortes`, `FermeturePortes`, `Autorisation`, `Attente`, `PortesOuvertes`, `PortesFermees`).
- Tests : nom de scenario (`ScenarioNominalSpec`, `ScenarioConcurrenceSpec`, `ScenarioPSDSpec`).

---

## 6) Maintenance de ce lexique

- Toute introduction de nouveau terme metier doit etre ajoutee ici **avant** d'apparaitre dans le code.
- En cas de divergence detectee entre code, Petri et lexique : on aligne sur ce qui est dans `petri/petri-troncon.md` (source de verite formelle), puis on corrige les autres.
- Une entree dans `documentation/suivi/historique.md` est obligatoire a chaque modification de ce fichier.
