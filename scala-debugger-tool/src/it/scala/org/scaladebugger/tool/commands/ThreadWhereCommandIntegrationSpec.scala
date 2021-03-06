package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class ThreadWhereCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("ThreadWhereCommand") {
    it("should print out the stack trace of the current active thread") {
      val testClass = "org.scaladebugger.test.misc.MainUsingMethod"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val className = testClass + "$"
      val threadName = "main"
      val methodName = "main"

      // Create a breakpoint before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 13")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:13\n")
          }

          // Set the active thread
          vt.newInputLine(s"thread $q$threadName$q")

          // Print the current stack trace
          vt.newInputLine("where")

          // Accumulate other text (delay to allow accumulation of all text)
          val waitTime = ToolConstants.AccumulationTimeout.millisPart
          val lines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).mkString("\n")

          // Verify current location is in stack trace
          lines should include (s"$className.$methodName ($testFileName:13)\n")
        })
      }
    }

    it("should print out the stack trace of the thread whose name is provided") {
      val testClass = "org.scaladebugger.test.misc.MainUsingMethod"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val className = testClass + "$"
      val threadName = "main"
      val methodName = "main"

      // Create a breakpoint before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 13")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:13\n")
          }

          // Print the current stack trace
          vt.newInputLine(s"where $q$threadName$q")

          // Accumulate other text (delay to allow accumulation of all text)
          val waitTime = ToolConstants.AccumulationTimeout.millisPart
          val lines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).mkString("\n")

          // Verify current location is in stack trace
          lines should include (s"$className.$methodName ($testFileName:13)\n")
        })
      }
    }
  }
}
