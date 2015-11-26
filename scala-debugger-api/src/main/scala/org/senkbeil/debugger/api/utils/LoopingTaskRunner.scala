package org.senkbeil.debugger.api.utils

import java.util
import java.util.concurrent._

import scala.util.Try

import LoopingTaskRunner._

/**
 * Contains defaults for the looping task runner.
 */
object LoopingTaskRunner {
  /** Default maximum workers is equal to number of available processors */
  val DefaultMaxWorkers: Int = Runtime.getRuntime.availableProcessors()

  /** Default maximum wait time is 100 milliseconds */
  val DefaultMaxTaskWaitTime: (Long, TimeUnit) = (100L, TimeUnit.MILLISECONDS)
}

/**
 * Represents a queue of tasks that will be executed infinitely in order
 * until removed.
 *
 * @param maxWorkers The total number of works to use for this runner,
 *                   defaulting to the total number of available processors
 * @param maxTaskWaitTime The maximum time to wait for a task to be pulled off
 *                        of the queue before allowing other tasks to be run
 */
class LoopingTaskRunner(
  private val maxWorkers: Int = DefaultMaxWorkers,
  private val maxTaskWaitTime: (Long, TimeUnit) = DefaultMaxTaskWaitTime
) {
  type TaskId = String

  /**
   * Represents a task that will execute the next task on the provided queue
   * and add it back to the end of the queue when finished.
   *
   * @param taskQueue The queue containing the ids of the tasks to run
   * @param taskMap The mapping of task ids to associated runnable tasks
   */
  private class LoopingTask(
    private val taskQueue: util.concurrent.BlockingQueue[TaskId],
    private val taskMap: util.Map[TaskId, Runnable]
  ) extends Runnable {
    override def run(): Unit = {
      // Determine the next task to execute (wait for a maximum time duration)
      val retrievedTaskId = Option(taskQueue.poll(
        maxTaskWaitTime._1,
        maxTaskWaitTime._2
      ))

      // If there is a new task, perform the operation
      retrievedTaskId.foreach { taskId =>
        // Retrieve and execute the next task
        val tryTask = Try(taskMap.get(taskId))
        tryTask.foreach(task => Try(task.run()))

        // Task finished, so add back to end of our queue
        // NOTE: Only do so if the map knows about our task (allows removal)
        if (tryTask.isSuccess) taskQueue.put(taskId)
      }

      // Start next task once this is free (suppress exceptions in the
      // situation that this runner has been stopped)
      Try(runNextTask())
    }
  }

  /** Contains the ids of tasks to be executed (in order). */
  private val taskQueue = new LinkedBlockingQueue[TaskId]()

  /** Contains mapping of task ids to task implementations. */
  private val taskMap = new ConcurrentHashMap[TaskId, Runnable]()

  /** Represents the executors used to execute the tasks. */
  @volatile private var executorService: Option[ExecutorService] = None

  /**
   * Indicates whether or not the task runner is processing tasks.
   *
   * @return True if it is running, otherwise false
   */
  def isRunning: Boolean = executorService.nonEmpty

  /**
   * Executing begins the process of executing queued up tasks.
   */
  def start(): Unit = {
    assert(!isRunning, "Runner already started!")

    // Create our thread pool with X workers to process tasks
    executorService = Some(Executors.newFixedThreadPool(maxWorkers))

    // Start X tasks to be run
    (1 to maxWorkers).foreach(_ => runNextTask())
  }

  /**
   * Prevents the runner from executing any more tasks.
   *
   * @param removeAllTasks If true, removes all tasks after being stopped
   */
  def stop(removeAllTasks: Boolean = true): Unit = {
    assert(isRunning, "Runner not started!")

    executorService.get.shutdown()
    executorService = None

    if (removeAllTasks) {
      taskQueue.clear()
      taskMap.clear()
    }
  }

  /**
   * Adds a task to be executed repeatedly (in a queue with other tasks).
   *
   * @param task The task to add
   * @tparam T The return type of the task
   *
   * @return The id of the queued task
   */
  def addTask[T](task: => T): TaskId = {
    val taskId = java.util.UUID.randomUUID().toString

    // Add the task to our lookup table, and then queue it up for processing
    taskMap.put(taskId, new Runnable {
      override def run(): Unit = task
    })
    taskQueue.put(taskId)

    taskId
  }

  /**
   * Removes a task from the repeated execution.
   *
   * @param taskId The id of the task to remove
   *
   * @return Task implementation that was removed
   */
  def removeTask(taskId: TaskId): Runnable = {
    taskQueue.remove(taskId)
    taskMap.remove(taskId)
  }

  /**
   * Executes next available task.
   */
  protected def runNextTask(): Unit =
    executorService.foreach(_.execute(newLoopingTask()))

  /**
   * Creates a new looping task to be executed.
   */
  protected def newLoopingTask(): Runnable = new LoopingTask(taskQueue, taskMap)
}
