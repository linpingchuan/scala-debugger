package org.senkbeil.debugger.api.lowlevel.methods

import com.sun.jdi.request.MethodEntryRequest
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Represents the manager for method entry requests.
 */
trait MethodEntryManager {
  /**
   * Retrieves the list of method entry requests contained by this manager.
   *
   * @return The collection of method entry request information
   */
  def methodEntryRequestList: Seq[MethodEntryRequestInfo]

  /**
   * Retrieves the list of method entry requests contained by this manager.
   *
   * @return The collection of method entry requests by id
   */
  def methodEntryRequestListById: Seq[String]

  /**
   * Creates a new method entry request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method entry event.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class whose method entry events to watch
   * @param methodName The name of the method whose entry to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMethodEntryRequestWithId(
    requestId: String,
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new method entry request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method entry event.
   *
   * @param className The name of the class whose method entry events to watch
   * @param methodName The name of the method whose entry to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMethodEntryRequest(
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Determines if a method entry request for the specific class and method
   * exists.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   *
   * @return True if a method entry request exists, otherwise false
   */
  def hasMethodEntryRequest(className: String, methodName: String): Boolean

  /**
   * Determines if a method entry request exists with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if a method entry request exists, otherwise false
   */
  def hasMethodEntryRequestWithId(requestId: String): Boolean

  /**
   * Retrieves the method entry requests for the specific class and method.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   *
   * @return Some collection of method entry requests if they exist,
   *         otherwise None
   */
  def getMethodEntryRequest(
    className: String,
    methodName: String
  ): Option[Seq[MethodEntryRequest]]

  /**
   * Retrieves the method entry request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some method entry request if it exists, otherwise None
   */
  def getMethodEntryRequestWithId(
    requestId: String
  ): Option[MethodEntryRequest]

  /**
   * Removes the specified method entry request.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   *
   * @return True if the method entry request was removed (if it existed),
   *         otherwise false
   */
  def removeMethodEntryRequest(
    className: String,
    methodName: String
  ): Boolean

  /**
   * Removes the specified method entry request.
   *
   * @param requestId The id of the request
   *
   * @return True if the method entry request was removed (if it existed),
   *         otherwise false
   */
  def removeMethodEntryRequestWithId(
    requestId: String
  ): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
