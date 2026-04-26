# Lexique de coherence - CriticalSystemModel

> Document de pivot entre les 4 representations du systeme : metier, code Akka, reseau de Petri, tests.
> A consulter avant d'ouvrir un fichier de code que tu n'as pas ecrit.
>
> **Regle dure** : si un nom dans une colonne ne correspond pas exactement aux autres colonnes, c'est un bug de coherence et il faut renommer.

---

## 1) Etats des trains

| Sens metier                          | Place Petri        | Nom du comportement Akka         | Etat dans les tests    |
|--------------------------------------|--------------------|----------------------------------|------------------------|
| Train est hors zone d'approche       | `T1_hors`, `T2_hors`           | `comportementHors`           | `hors`                 |
| Train a demande, attend la reponse   | `T1_attente`, `T2_attente`     | `comportementEnAttente`      | `enAttente`            |
| Train occupe le troncon partage      | `T1_sur_troncon`, `T2_sur_troncon` | `comportementSurTroncon` | `surTroncon`           |

**Regle** : aucun autre etat n'est introduit. Pas de "transition", pas de "demande_envoyee", pas de "en_route". Si le metier evolue, on amende le reseau de Petri en premier (livrable L3) puis on aligne le code et les tests.

---

## 2) Etats du controleur de section

| Sens metier                       | Encodage Petri (place)  | Etat interne du controleur Akka | Description                       |
|-----------------------------------|-------------------------|---------------------------------|-----------------------------------|
| Le troncon est libre              | `Troncon_libre`         | `EtatLibre` (file vide)         | Aucun train sur le troncon        |
| Train 1 occupe le troncon         | `T1_sur_troncon`        | `EtatOccupe(Train1, file)`      | Train 1 dedans, file FIFO eventuelle |
| Train 2 occupe le troncon         | `T2_sur_troncon`        | `EtatOccupe(Train2, file)`      | Train 2 dedans, file FIFO eventuelle |

**Note** : la file FIFO du controleur Akka est une finesse d'implementation pour garantir la fairness en pratique. Elle n'a pas d'equivalent direct dans le reseau de Petri (qui reste non deterministe). Cette dissymetrie est documentee dans `petri/petri-troncon.md` section 9 et reprise dans le rapport.

---

## 3) Messages du protocole Akka

| Message                                  | Type                                  | Sens metier                                         | Transition Petri associee |
|------------------------------------------|---------------------------------------|-----------------------------------------------------|---------------------------|
| `Demande(emetteur, repondreA)`           | `MessagePourControleur`               | Le train demande l'acces au troncon                 | `T1_demande` ou `T2_demande` |
| `Sortie(emetteur)`                       | `MessagePourControleur`               | Le train libere le troncon en sortant               | `T1_sortie_liberation` ou `T2_sortie_liberation` |
| `Autorisation`                           | `MessagePourTrain`                    | Le controleur autorise l'entree                     | `T1_entree_autorisee` ou `T2_entree_autorisee` |
| `Attente`                                | `MessagePourTrain`                    | Le controleur signale au train d'attendre           | (aucune - notification de protocole, pas un changement d'etat formel) |

**Regle** : aucun nouveau message ne peut etre introduit sans mise a jour de `petri/petri-troncon.md` section 8 ET de ce lexique. Le protocole est verrouille a 4 messages.

---

## 4) Glossaire des termes critiques

### Termes du metier
- **Troncon partage** : segment de voie unique qu'au plus un train peut occuper a la fois. Synonyme : "section critique" en theorie de la concurrence.
- **Exclusion mutuelle** : propriete de surete garantissant qu'au plus un acteur acces a une ressource a la fois.
- **Arbitrage** : mecanisme par lequel le controleur decide qui peut entrer et qui doit attendre.
- **Fairness** : hypothese qu'un train en attente finit toujours par etre servi (pas de famine indefinie).

### Termes Akka
- **Acteur** : entite isolee qui recoit et traite des messages sequentiellement.
- **`Behavior[T]`** : description du comportement courant d'un acteur, specifiant comment il reagit aux messages de type T.
- **TestKit / Probe** : faux acteur utilise dans les tests pour intercepter et verifier les messages.

### Termes Petri
- **Place** : etat possible (par exemple `T1_attente`).
- **Marquage** : nombre de jetons dans chaque place a un instant donne.
- **Transition** : evenement qui consomme des jetons en entree et en produit en sortie.
- **Tirable** : une transition est tirable si toutes ses places d'entree ont assez de jetons.
- **Espace d'etats accessible** : ensemble des marquages atteignables depuis le marquage initial.
- **P-invariant** : combinaison lineaire de places qui reste constante sur tous les marquages atteignables (notre invariant principal en est un).

### Termes verification
- **Safety** : "rien de mal n'arrive jamais" (par exemple : pas d'occupation simultanee).
- **Liveness** : "quelque chose de bien finit toujours par arriver" (par exemple : un train en attente finit par passer).
- **LTL** : Linear Temporal Logic, langage pour exprimer Safety et Liveness sur des chemins d'execution.

---

## 5) Convention de nommage Scala (rappel court)

- Variables : noms longs et explicites en francais (`trainEnAttente`, `controleurSection`). Pas de `t`, `c`, `tmp`.
- Fonctions : verbe ou nom d'etat (`comportementHors`, `traiterDemande`). Court (<30 lignes).
- Classes / objets : substantif (`SectionController`, `PetriNet`).
- Messages : substantif decrivant l'evenement (`Demande`, `Sortie`, `Autorisation`, `Attente`).
- Tests : nom de scenario (`ScenarioNominalSpec`, `ScenarioConcurrenceSpec`).

---

## 6) Maintenance de ce lexique

- Toute introduction de nouveau terme metier doit etre ajoutee ici **avant** d'apparaitre dans le code.
- En cas de divergence detectee entre code, Petri et lexique : on aligne sur ce qui est dans `petri/petri-troncon.md` (source de verite formelle), puis on corrige les autres.
- Une entree dans `documentation/suivi/historique.md` est obligatoire a chaque modification de ce fichier.
