package m14

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import m14.StationControl._
import org.scalatest.wordspec.AnyWordSpecLike

class StationControlSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "StationControl" should {

    "switch to safety mode and close every gate on critical incident" in {
      val stationControl = spawn(StationControl())
      val snapshotProbe = createTestProbe[Snapshot]()

      stationControl ! ReportIncident(
        id = "INC-CRIT-1",
        zone = PlatformNorth,
        severity = Critical,
        description = "Incident critique sur quai"
      )
      stationControl ! GetSnapshot(snapshotProbe.ref)

      val snapshot = snapshotProbe.receiveMessage()

      assert(snapshot.mode == Safety)
      assert(snapshot.openIncidents.contains("INC-CRIT-1"))
      assert(snapshot.zones.values.forall(zoneState => !zoneState.gateOpen))
    }

    "close only the impacted zone gate when occupancy exceeds threshold" in {
      val stationControl = spawn(StationControl())
      val snapshotProbe = createTestProbe[Snapshot]()

      stationControl ! PassengerFlowUpdate(PlatformNorth, occupancy = 980)
      stationControl ! GetSnapshot(snapshotProbe.ref)

      val snapshot = snapshotProbe.receiveMessage()

      assert(snapshot.mode == Normal)
      assert(snapshot.zones(PlatformNorth).gateOpen == false)
      assert(snapshot.zones(PlatformSouth).gateOpen)
      assert(snapshot.zones(TransferCorridor).gateOpen)
    }

    "return to normal mode after incident resolution when all zones are safe" in {
      val stationControl = spawn(StationControl())
      val snapshotProbe = createTestProbe[Snapshot]()

      stationControl ! ReportIncident(
        id = "INC-CRIT-2",
        zone = TransferCorridor,
        severity = Critical,
        description = "Forte perturbation en couloir"
      )
      stationControl ! PassengerFlowUpdate(TransferCorridor, occupancy = 500)
      stationControl ! ResolveIncident("INC-CRIT-2")
      stationControl ! GetSnapshot(snapshotProbe.ref)

      val snapshot = snapshotProbe.receiveMessage()

      assert(snapshot.mode == Normal)
      assert(snapshot.openIncidents.isEmpty)
      assert(snapshot.zones.values.forall(zoneState => zoneState.gateOpen))
    }
  }
}
