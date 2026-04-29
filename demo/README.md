# Demo M14 - Simulation animee (canton + quai + portes palieres)

Cette demo rejoue les scenarios canoniques du sous-systeme critique M14
(extension PSD : 2 trains + canton + quai + portes palieres) sous forme
d'une animation HTML/JS autonome.

## Principe

Une seule source de verite : le code Scala.

```
[Scala] m14.demo.GenererTraces (utilise m14.petri.PetriNet.tirer)
    |
    |  produit
    v
trace-nominal.json | trace-concurrence.json | trace-violation.json
    |
    |  fetch() au chargement
    v
[Browser] index.html  -> animations + popups + invariants en direct
```

Le navigateur ne simule rien : il **rejoue** une trace pre-calculee a partir
du vrai reseau de Petri verifie par les tests Scala (39/39 verts).

## Comment lancer

```powershell
# 1) Regenerer les traces (a refaire si on modifie GenererTraces.scala ou PetriNet.scala)
sbt "runMain m14.demo.GenererTraces"

# 2) Servir le dossier demo via HTTP (fetch ne marche pas en file://)
cd demo ; python -m http.server 8000
# puis ouvrir http://localhost:8000 dans un navigateur
```

> Alternative VS Code : extension "Live Server", clic droit sur `index.html` -> Open with Live Server.

## Scenarios disponibles

| Scenario | Trace | Etapes | Demontre |
|----------|-------|-------:|----------|
| **A - Cycle nominal complet** | `trace-nominal.json` | 7 | Train1 enchaine demande -> canton -> quai -> ouverture -> fermeture -> depart |
| **B - Concurrence canton + quai** | `trace-concurrence.json` | 11 | Train2 arrive en parallele, attend canton puis quai, FIFO respecte |
| **C - Tentative PSD invalide (CRITIQUE)** | `trace-violation.json` | 3 | Acteur tente d'ouvrir les portes sans train a quai. Refus Petri (transition non tirable) + refus Akka (garde de surete `GestionnairePortes`). Overlay rouge anime. |

## Elements visuels

- **Trains animes** : T1 (bleu) et T2 (vert) glissent entre les 4 zones (hors / attente / canton / quai).
- **Portes palieres** : 2 panneaux qui s'ecartent quand `Portes_ouvertes=1`, changent de couleur (gris -> vert).
- **Reseau de Petri** : 12 places affichees a droite, jetons synchronises avec les transitions.
- **Popups Akka** : chaque message envoye genere un toast violet (`Train1 -> SectionController : Demande`) en bas a droite. Toasts ROUGES pour les messages refuses par les gardes.
- **Bandeau invariants** : 5 pastilles vertes (canton, quai, portes, PSD-Open, PSD-Departure). Calcul JS qui MIROIRE `m14.petri.Analyseur`.
- **Overlay violation** : badge rouge "TENTATIVE BLOQUEE par garde de surete" + clignotement quand `violation: true`.

## Controles

- **Lecture / Pause** : auto-play pas par pas.
- **Pas suivant** : avance manuellement.
- **Reset** : revient a M0.
- **Vitesse** : lente (2s) / normale (1.2s) / rapide (0.6s) / pas-a-pas (manuel).

## Format des traces JSON

Voir `src/main/scala/m14/demo/TraceWriter.scala` pour la specification.

```json
{
  "id": "nominal",
  "titre": "...",
  "description": "...",
  "etapes": [
    {
      "label": "Train1 demande l'acces au canton",
      "akka": {"de": "Train1", "vers": "SectionController", "libelle": "Demande"},
      "transition": "T1_demande",
      "marquage": {"Canton_libre": 1, "T1_attente": 1, ...},
      "violation": false
    }
  ]
}
```

## Pour la soutenance

Ordre suggere :
1. **Scenario A** (nominal) en vitesse normale -> "voici le cycle de vie d'un train".
2. **Scenario B** (concurrence) -> "voici comment SectionController et QuaiController arbitrent en FIFO".
3. **Scenario C** (violation) en vitesse lente -> "voici la propriete CRITIQUE PSD-Open : la garde refuse l'ouverture, l'overlay rouge le rend visible".

L'ancien prototype standalone (sans Scala) reste en `index-old.html.bak`.
