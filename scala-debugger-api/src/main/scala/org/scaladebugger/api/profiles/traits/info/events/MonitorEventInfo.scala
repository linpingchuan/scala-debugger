package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.LocatableEvent
import org.scaladebugger.api.profiles.traits.info.{ObjectInfo, ThreadInfo}

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI monitor events.
 */
trait MonitorEventInfo extends LocatableEventInfo {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance as a locatable event since there is
   *         no abstraction between monitor events
   */
  override def toJdiInstance: LocatableEvent

  /**
   * Returns the monitor associated with the event.
   *
   * @return The information profile about the monitor object
   */
  def monitor: ObjectInfo

  /**
   * Returns the thread tied to the monitor object (typically where
   * the event occurred).
   *
   * @return The information profile about the thread
   */
  override def thread: ThreadInfo
}
