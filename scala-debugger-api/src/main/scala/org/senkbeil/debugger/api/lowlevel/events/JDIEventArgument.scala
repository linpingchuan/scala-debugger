package org.senkbeil.debugger.api.lowlevel.events

import org.senkbeil.debugger.api.lowlevel.JDIArgument

/**
 * Represents an argument for a JDI Event.
 */
trait JDIEventArgument extends JDIArgument with Serializable {
  /**
   * Creates a new JDI event processor based on this argument.
   *
   * @return The new JDI event processor instance
   */
  def toProcessor: JDIEventProcessor
}
