# Bibliographie commentee - CriticalSystemModel

> Livrable L1 du cahier des charges. Sources de reference cadrant theoriquement et methodologiquement le projet.
>
> Format : titre + auteur + annee + lien stable + 2-4 lignes expliquant l'usage dans **ce** projet (pas un resume general). Cible : 5-8 sources, etoffees en Phase 8.

---

## 1) Reseaux de Petri (modele formel)

### Murata, T. (1989) - "Petri Nets: Properties, Analysis and Applications"
*Proceedings of the IEEE, vol. 77, no. 4, pp. 541-580.*
Lien : https://doi.org/10.1109/5.24143

Reference canonique des reseaux de Petri. Sert de base pour les definitions formelles utilisees dans `petri/petri-troncon.md` : places, transitions, marquage, tirabilite, espace d'etats accessible, invariants de places (P-invariants). La notion de P-invariant est directement appliquee a notre invariant principal `T1_sur_troncon + T2_sur_troncon + Troncon_libre = 1`.

### David, R. & Alla, H. (2010) - "Discrete, Continuous, and Hybrid Petri Nets" (2e ed., chapitres 1-3)
*Springer.*

Source francophone de cours sur la theorie des reseaux de Petri ordinaires. Utilise pour la justification de la procedure d'analyse par exploration de l'espace d'etats accessible (BFS) qui sera implementee dans l'analyseur Scala (livrable L5).

---

## 2) Modele acteur et Akka

### Akka Documentation (Lightbend) - "Akka Typed Actors"
*Documentation officielle, version 2.8.x.*
Lien : https://doc.akka.io/docs/akka/current/typed/index.html

Reference d'usage du framework. Sert de base pour la definition des `Behavior[T]` typés, l'utilisation de `Behaviors.receive` / `Behaviors.receiveMessage`, et la mise en oeuvre des tests via Akka TestKit. La typisation des messages permet de borner statiquement le protocole, ce qui est important pour la coherence avec le reseau de Petri.

### Hewitt, C., Bishop, P., Steiger, R. (1973) - "A Universal Modular ACTOR Formalism for Artificial Intelligence"
*Proceedings of the 3rd International Joint Conference on Artificial Intelligence.*

Source historique du modele acteur. Citee pour ancrer theoriquement le choix Akka et justifier que le modele acteur est une abstraction adaptee aux systemes distribues critiques (isolation d'etat, communication par messages asynchrones).

---

## 3) Verification formelle et logique LTL

### Baier, C. & Katoen, J.-P. (2008) - "Principles of Model Checking" (chapitres 5 et 6)
*MIT Press.*

Reference moderne pour la logique LTL et le model checking. Utilisee pour la formalisation des proprietes de surete (Safety) et de vivacite (Liveness) du projet : `G !(T1_sur_troncon ∧ T2_sur_troncon)` et `G (T1_attente -> F T1_sur_troncon)`. Le projet ne code pas un model checker complet mais s'appuie sur les definitions et la justification informelle sur l'espace d'etats fini.

### Manna, Z. & Pnueli, A. (1995) - "Temporal Verification of Reactive Systems: Safety"
*Springer-Verlag.*

Reference theorique pour la distinction Safety/Liveness, utile pour structurer la section "Verification" du rapport (L4). On retient en particulier la formulation precise des proprietes de surete comme "rien de mal n'arrive jamais" et leur decidabilite sur des espaces d'etats finis.

### Linear Temporal Logic - Cours TU Munchen
*Notes de cours en ligne.*
Lien : https://www7.in.tum.de/um/courses/verification/SS05/LTL.pdf

Support pedagogique concis pour les operateurs LTL (`G`, `F`, `X`, `U`) et leur semantique sur les chemins. Sert de reference rapide pour les etudiants de l'equipe lors de la redaction du rapport.

---

## 4) Concurrence et systemes distribues

### Magee, J. & Kramer, J. (2006) - "Concurrency: State Models and Java Programs" (2e ed., chapitres 4-5)
*Wiley.*

Reference pedagogique sur la modelisation de la concurrence par machines a etats. Utilisee comme inspiration pour le decoupage des etats des acteurs `Train` (`hors` / `attente` / `sur_troncon`) et pour la justification du choix d'un controleur centralise plutot que d'un protocole peer-to-peer.

### Lamport, L. (1978) - "Time, Clocks, and the Ordering of Events in a Distributed System"
*Communications of the ACM, vol. 21, no. 7, pp. 558-565.*
Lien : https://doi.org/10.1145/359545.359563

Reference fondamentale sur la coordination dans les systemes distribues asynchrones. Citee pour cadrer la nature des hypotheses faites sur l'ordre des messages dans le projet et pour justifier la necessite d'un arbitre centralise dans le cas d'un acces a une ressource critique partagee.

---

## 5) Domaine d'application : transport ferroviaire critique

### RATP - Documentation publique de la ligne 14
*Site officiel RATP, dossiers de presse extension M14.*
Lien : https://www.ratp.fr/

Source de contexte applicatif. Sert a justifier le choix du domaine (metro automatique sans conducteur) comme exemple de systeme distribue **critique** au sens du cahier des charges (defaillance = consequences graves). Le projet ne pretend pas modeliser fidelement la M14, mais s'appuie sur ce contexte pour ancrer le sous-systeme abstrait etudie.

---

## Notes de gestion bibliographique

- Toute source ajoutee ici doit etre citee au moins une fois dans `documentation/rapport.md` (livrable L4).
- Eviter d'ajouter des sources non lues : preferer 5 sources comprises a 15 sources decoratives.
- Format des liens : URL stable (DOI, page institutionnelle, edition de reference). Pas de lien vers un PDF tiers periss able.
- Etat actuel : 9 sources. Cible cahier des charges : 5-8. Possibilite d'en retirer 1 ou 2 a la relecture si redondantes (par exemple Lamport 1978 si non utilisee dans le rapport).
