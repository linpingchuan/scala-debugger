package org.scaladebugger.api.profiles.swappable.monitors
import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.monitors.MonitorWaitRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.monitors.MonitorWaitProfile

import scala.util.Try

/**
 * Represents a swappable profile for monitor wait events that
 * redirects the invocation to another profile.
 */
trait SwappableMonitorWaitProfile extends MonitorWaitProfile {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateMonitorWaitRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitEventAndData]] = {
    withCurrentProfile.tryGetOrCreateMonitorWaitRequestWithData(extraArguments: _*)
  }

  override def isMonitorWaitRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isMonitorWaitRequestWithArgsPending(extraArguments: _*)
  }

  override def monitorWaitRequests: Seq[MonitorWaitRequestInfo] = {
    withCurrentProfile.monitorWaitRequests
  }
}
