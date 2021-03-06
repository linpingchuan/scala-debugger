package org.scaladebugger.api.profiles.traits.requests.breakpoints
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.breakpoints.BreakpointRequestInfo
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.BreakpointEventInfo
import org.scaladebugger.test.helpers.ParallelMockFunSpec

import scala.util.{Failure, Success, Try}

class BreakpointRequestSpec extends ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[BreakpointRequest#BreakpointEventAndData]
  )

  private val successBreakpointProfile = new Object with BreakpointRequest {
    override def tryGetOrCreateBreakpointRequestWithData(
      fileName: String,
      lineNumber: Int,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[BreakpointEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def removeBreakpointRequests(fileName: String, lineNumber: Int): Seq[BreakpointRequestInfo] = ???
    override def removeBreakpointRequestWithArgs(fileName: String, lineNumber: Int, extraArguments: JDIArgument*): Option[BreakpointRequestInfo] = ???
    override def removeAllBreakpointRequests(): Seq[BreakpointRequestInfo] = ???
    override def isBreakpointRequestPending(fileName: String, lineNumber: Int): Boolean = ???
    override def isBreakpointRequestWithArgsPending(fileName: String, lineNumber: Int, extraArguments: JDIArgument*): Boolean = ???
    override def breakpointRequests: Seq[BreakpointRequestInfo] = ???
  }

  private val failBreakpointProfile = new Object with BreakpointRequest {
    override def tryGetOrCreateBreakpointRequestWithData(
      fileName: String,
      lineNumber: Int,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[BreakpointEventAndData]] = {
      Failure(TestThrowable)
    }

    override def removeBreakpointRequests(fileName: String, lineNumber: Int): Seq[BreakpointRequestInfo] = ???
    override def removeBreakpointRequestWithArgs(fileName: String, lineNumber: Int, extraArguments: JDIArgument*): Option[BreakpointRequestInfo] = ???
    override def removeAllBreakpointRequests(): Seq[BreakpointRequestInfo] = ???
    override def isBreakpointRequestPending(fileName: String, lineNumber: Int): Boolean = ???
    override def isBreakpointRequestWithArgsPending(fileName: String, lineNumber: Int, extraArguments: JDIArgument*): Boolean = ???
    override def breakpointRequests: Seq[BreakpointRequestInfo] = ???
  }

  describe("BreakpointRequest") {
    describe("#tryGetOrCreateBreakpointRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[BreakpointEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: BreakpointEventInfo = null
        successBreakpointProfile.tryGetOrCreateBreakpointRequest("", 0).get.foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        // Data to be run through pipeline
        val data = (mock[BreakpointEventInfo], Seq(mock[JDIEventDataResult]))

        var actual: Throwable = null
        failBreakpointProfile.tryGetOrCreateBreakpointRequest("", 0).failed.foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateBreakpointRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[BreakpointEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: BreakpointEventInfo = null
        successBreakpointProfile.getOrCreateBreakpointRequest("", 0).foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failBreakpointProfile.getOrCreateBreakpointRequest("", 0)
        }
      }
    }

    describe("#getOrCreateBreakpointRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[BreakpointEventInfo], Seq(mock[JDIEventDataResult]))

        var actual: (BreakpointEventInfo, Seq[JDIEventDataResult]) = null
        successBreakpointProfile
          .getOrCreateBreakpointRequestWithData("", 0)
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failBreakpointProfile.getOrCreateBreakpointRequestWithData("", 0)
        }
      }
    }
  }
}
