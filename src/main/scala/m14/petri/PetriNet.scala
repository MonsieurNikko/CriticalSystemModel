// PetriNet.scala : structures de donnees et operations pour un reseau de Petri ordinaire.

package m14.petri

object PetriNet {

  // Une place est identifiee par son nom (coherent avec petri-troncon.md section 2).
  type Place = String

  // Un marquage associe a chaque place son nombre de jetons.
  type Marking = Map[Place, Int]

  // Une transition a un nom, des arcs entrants (pre) et des arcs sortants (post).
  // Pre : places consommees (1 jeton chacune). Post : places produites (1 jeton chacune).
  final case class Transition(nom: String, pre: Set[Place], post: Set[Place])

  // Le reseau complet : ses places, ses transitions, son marquage initial.
  final case class Net(places: Set[Place], transitions: List[Transition], marquageInitial: Marking)

  // --- Les 7 places du troncon (cf petri-troncon.md section 2) ---
  val TronconLibre  = "Troncon_libre"
  val T1Hors        = "T1_hors"
  val T1Attente     = "T1_attente"
  val T1SurTroncon  = "T1_sur_troncon"
  val T2Hors        = "T2_hors"
  val T2Attente     = "T2_attente"
  val T2SurTroncon  = "T2_sur_troncon"

  val toutesLesPlaces: Set[Place] = Set(
    TronconLibre, T1Hors, T1Attente, T1SurTroncon, T2Hors, T2Attente, T2SurTroncon
  )

  // --- Les 6 transitions (cf petri-troncon.md section 3) ---
  val t1Demande           = Transition("T1_demande",           Set(T1Hors),                    Set(T1Attente))
  val t2Demande           = Transition("T2_demande",           Set(T2Hors),                    Set(T2Attente))
  val t1EntreeAutorisee   = Transition("T1_entree_autorisee",  Set(T1Attente, TronconLibre),    Set(T1SurTroncon))
  val t2EntreeAutorisee   = Transition("T2_entree_autorisee",  Set(T2Attente, TronconLibre),    Set(T2SurTroncon))
  val t1SortiLiberation   = Transition("T1_sortie_liberation", Set(T1SurTroncon),               Set(T1Hors, TronconLibre))
  val t2SortiLiberation   = Transition("T2_sortie_liberation", Set(T2SurTroncon),               Set(T2Hors, TronconLibre))

  val toutesLesTransitions: List[Transition] = List(
    t1Demande, t2Demande, t1EntreeAutorisee, t2EntreeAutorisee, t1SortiLiberation, t2SortiLiberation
  )

  // --- Marquage initial M0 (cf petri-troncon.md section 2) ---
  val marquageInitial: Marking = Map(
    TronconLibre -> 1, T1Hors -> 1, T1Attente -> 0, T1SurTroncon -> 0,
    T2Hors -> 1, T2Attente -> 0, T2SurTroncon -> 0
  )

  // Le reseau complet du troncon partage.
  val reseauTroncon: Net = Net(toutesLesPlaces, toutesLesTransitions, marquageInitial)

  // Verifie si une transition est tirable depuis un marquage donne.
  // Une transition est tirable si chaque place de son pre a au moins 1 jeton.
  def estTirable(transition: Transition, marquage: Marking): Boolean =
    transition.pre.forall(place => marquage.getOrElse(place, 0) >= 1)

  // Tire une transition depuis un marquage et retourne le nouveau marquage.
  // Retourne None si la transition n'est pas tirable.
  def tirer(transition: Transition, marquage: Marking): Option[Marking] = {
    if (!estTirable(transition, marquage)) {
      None
    } else {
      // Consommer 1 jeton dans chaque place du pre.
      val apresConsommation = transition.pre.foldLeft(marquage) { (m, place) =>
        m.updated(place, m.getOrElse(place, 0) - 1)
      }
      // Produire 1 jeton dans chaque place du post.
      val apresProduction = transition.post.foldLeft(apresConsommation) { (m, place) =>
        m.updated(place, m.getOrElse(place, 0) + 1)
      }
      Some(apresProduction)
    }
  }

  // Liste les transitions tirables depuis un marquage donne.
  def transitionsTirables(net: Net, marquage: Marking): List[Transition] =
    net.transitions.filter(t => estTirable(t, marquage))
}
