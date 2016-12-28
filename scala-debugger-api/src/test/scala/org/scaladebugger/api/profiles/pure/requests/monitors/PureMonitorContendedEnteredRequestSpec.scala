package org.scaladebugger.api.profiles.pure.requests.monitors
import com.sun.jdi.event.MonitorContendedEnteredEvent
import org.scaladebugger.api.lowlevel.events.EventType.MonitorContendedEnteredEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.monitors.{MonitorContendedEnteredManager, MonitorContendedEnteredRequestInfo, PendingMonitorContendedEnteredSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{EventInfoProducer, MonitorContendedEnteredEventInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.Success

class PureMonitorContendedEnteredRequestSpec extends ParallelMockFunSpec with JDIMockHelpers {
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMonitorContendedEnteredManager =
    mock[MonitorContendedEnteredManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = MonitorContendedEnteredEvent
  private type EI = MonitorContendedEnteredEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = Seq[JDIRequestArgument]
  private type CounterKey = Seq[JDIRequestArgument]
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = MonitorContendedEnteredEventType
  )

  private class TestPureMonitorContendedEnteredRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureMonitorContendedEnteredRequest {
    override def newMonitorContendedEnteredRequestHelper() = {
      val originalRequestHelper = super.newMonitorContendedEnteredRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val monitorContendedEnteredManager = mockMonitorContendedEnteredManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureMonitorContendedEnteredProfile =
    new TestPureMonitorContendedEnteredRequest(Some(mockRequestHelper))

  describe("PureMonitorContendedEnteredRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val pureMonitorContendedEnteredProfile = new TestPureMonitorContendedEnteredRequest()
          val requestHelper = pureMonitorContendedEnteredProfile.newMonitorContendedEnteredRequestHelper()

          val requestId1 = requestHelper._newRequestId()
          val requestId2 = requestHelper._newRequestId()

          requestId1 shouldBe a[String]
          requestId2 shouldBe a[String]
          requestId1 should not be (requestId2)
        }
      }

      describe("#_newRequest") {
        it("should create a new request with the provided args and id") {
          val expected = Success("some id")

          val pureMonitorContendedEnteredProfile = new TestPureMonitorContendedEnteredRequest()
          val requestHelper = pureMonitorContendedEnteredProfile.newMonitorContendedEnteredRequestHelper()

          val requestId = expected.get
          val requestArgs = Seq(mock[JDIRequestArgument])
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
            .expects(requestId, jdiRequestArgs)
            .returning(expected)
            .once()

          val actual = requestHelper._newRequest(requestId, requestArgs, jdiRequestArgs)

          actual should be(expected)
        }
      }

      describe("#_hasRequest") {
        it("should return true if a request exists with matching request arguments") {
          val expected = true

          val pureMonitorContendedEnteredProfile = new TestPureMonitorContendedEnteredRequest()
          val requestHelper = pureMonitorContendedEnteredProfile.newMonitorContendedEnteredRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = MonitorContendedEnteredRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = requestArgs
          )

          // Get a list of request ids
          (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that has arguments
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be(expected)
        }

        it("should return false if no request exists with matching request arguments") {
          val expected = false

          val pureMonitorContendedEnteredProfile = new TestPureMonitorContendedEnteredRequest()
          val requestHelper = pureMonitorContendedEnteredProfile.newMonitorContendedEnteredRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = MonitorContendedEnteredRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = Seq(mock[JDIRequestArgument])
          )

          // Get a list of request ids
          (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that does not have same arguments
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be(expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val pureMonitorContendedEnteredProfile = new TestPureMonitorContendedEnteredRequest()
          val requestHelper = pureMonitorContendedEnteredProfile.newMonitorContendedEnteredRequestHelper()

          val requestId = "some id"

          (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(MonitorContendedEnteredRequestInfo(
            requestId = "some id",
            isPending = true,
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val pureMonitorContendedEnteredProfile = new TestPureMonitorContendedEnteredRequest()
          val requestHelper = pureMonitorContendedEnteredProfile.newMonitorContendedEnteredRequestHelper()

          val requestId = "some id"

          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be(expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[MonitorContendedEnteredEventInfo]

          val pureMonitorContendedEnteredProfile = new TestPureMonitorContendedEnteredRequest()
          val requestHelper = pureMonitorContendedEnteredProfile.newMonitorContendedEnteredRequestHelper()

          val mockEventProducer = mock[EventInfoProducer]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[MonitorContendedEnteredEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultMonitorContendedEnteredEventInfoProfile _)
            .expects(mockScalaVirtualMachine, mockEvent, mockJdiArgs)
            .returning(expected).once()

          val actual = requestHelper._newEventInfo(
            mockScalaVirtualMachine,
            mockEvent,
            mockJdiArgs
          )

          actual should be(expected)
        }
      }
    }

    describe("#tryGetOrCreateMonitorContendedEnteredRequestWithData") {
      it("should use the request helper's request and event pipeline methods") {
        val requestId = java.util.UUID.randomUUID().toString
        val mockJdiRequestArgs = Seq(mock[JDIRequestArgument])
        val mockJdiEventArgs = Seq(mock[JDIEventArgument])
        val requestArgs = mockJdiRequestArgs

        (mockRequestHelper.newRequest _)
          .expects(requestArgs, mockJdiRequestArgs)
          .returning(Success(requestId)).once()
        (mockRequestHelper.newEventPipeline _)
          .expects(requestId, mockJdiEventArgs, requestArgs)
          .returning(Success(Pipeline.newPipeline(classOf[EIData]))).once()

        val actual = pureMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequest(
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an[IdentityPipeline[EIData]]
      }
    }

    describe("#monitorContendedEnteredRequests") {
      it("should include all active requests") {
        val expected = Seq(
          MonitorContendedEnteredRequestInfo(TestRequestId, false)
        )

        val mockMonitorContendedEnteredManager = mock[PendingMonitorContendedEnteredSupportLike]
        val pureMonitorContendedEnteredProfile = new Object with PureMonitorContendedEnteredRequest {
          override protected val monitorContendedEnteredManager = mockMonitorContendedEnteredManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        (mockMonitorContendedEnteredManager.pendingMonitorContendedEnteredRequests _).expects()
          .returning(Nil).once()

        val actual = pureMonitorContendedEnteredProfile.monitorContendedEnteredRequests

        actual should be(expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          MonitorContendedEnteredRequestInfo(TestRequestId, true)
        )

        val mockMonitorContendedEnteredManager = mock[PendingMonitorContendedEnteredSupportLike]
        val pureMonitorContendedEnteredProfile = new Object with PureMonitorContendedEnteredRequest {
          override protected val monitorContendedEnteredManager = mockMonitorContendedEnteredManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(Nil).once()

        (mockMonitorContendedEnteredManager.pendingMonitorContendedEnteredRequests _).expects()
          .returning(expected).once()

        val actual = pureMonitorContendedEnteredProfile.monitorContendedEnteredRequests

        actual should be(expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          MonitorContendedEnteredRequestInfo(TestRequestId, false)
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        val actual = pureMonitorContendedEnteredProfile.monitorContendedEnteredRequests

        actual should be(expected)
      }
    }

    describe("#removeMonitorContendedEnteredRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(Nil).once()

        val actual = pureMonitorContendedEnteredProfile.removeMonitorContendedEnteredRequestWithArgs()

        actual should be(expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorContendedEnteredProfile.removeMonitorContendedEnteredRequestWithArgs()

        actual should be(expected)
      }

      it("should return remove and return matching pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = true,

            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorContendedEnteredProfile.removeMonitorContendedEnteredRequestWithArgs(
          extraArguments: _*
        )

        actual should be(expected)
      }

      it("should remove and return matching non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = false,

            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorContendedEnteredProfile.removeMonitorContendedEnteredRequestWithArgs(
          extraArguments: _*
        )

        actual should be(expected)
      }
    }

    describe("#removeAllMonitorContendedEnteredRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(Nil).once()

        val actual = pureMonitorContendedEnteredProfile.removeAllMonitorContendedEnteredRequests()

        actual should be(expected)
      }

      it("should remove and return all pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorContendedEnteredProfile.removeAllMonitorContendedEnteredRequests()

        actual should be(expected)
      }

      it("should remove and return all non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorContendedEnteredProfile.removeAllMonitorContendedEnteredRequests()

        actual should be(expected)
      }
    }

    describe("#isMonitorContendedEnteredRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMonitorContendedEnteredProfile.isMonitorContendedEnteredRequestWithArgsPending()

        actual should be(expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorContendedEnteredProfile.isMonitorContendedEnteredRequestWithArgsPending()

        actual should be(expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorContendedEnteredProfile.isMonitorContendedEnteredRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be(expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorContendedEnteredProfile.isMonitorContendedEnteredRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be(expected)
      }
    }
  }
}
