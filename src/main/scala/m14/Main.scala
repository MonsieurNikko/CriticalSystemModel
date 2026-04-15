package m14

import akka.actor.typed.ActorSystem

object Main extends App {
  import StationControl._

  val system: ActorSystem[Command] = ActorSystem(StationControl(), "line14-chatelet-control")

  system ! PassengerFlowUpdate(PlatformNorth, occupancy = 650)
  system ! PassengerFlowUpdate(TransferCorridor, occupancy = 1250)
  system ! ReportIncident(
    id = "INC-CHATELET-001",
    zone = PlatformSouth,
    severity = Critical,
    description = "Obstacle detecte sur quai sud"
  )

  println("StationControl M14 demarre. Arreter avec Ctrl+C.")
}
