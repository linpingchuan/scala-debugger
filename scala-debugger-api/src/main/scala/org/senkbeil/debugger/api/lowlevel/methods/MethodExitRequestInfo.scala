package org.senkbeil.debugger.api.lowlevel.methods

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a method exit request.
 *
 * @param className The full name of the class containing the method
 * @param methodName The name of the method
 * @param extraArguments The additional arguments provided to the
 *                       method exit request
 */
case class MethodExitRequestInfo(
  className: String,
  methodName: String,
  extraArguments: Seq[JDIRequestArgument] = Nil
)

