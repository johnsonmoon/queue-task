package com.github.johnsonmoon.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Create by johnsonmoon at 2018/11/22 17:54.
 */
public class TaskFuture<T> {
    private static Logger logger = LoggerFactory.getLogger(TaskFuture.class);
    private AtomicBoolean interrupt = new AtomicBoolean(false);
    private SimpleTask<T> task;
    private String taskId = UUID.randomUUID().toString().replaceAll("-", "");
    private Long executionTimeout;
    private TaskStatus taskStatus;
    private Future<T> future;
    private T result;

    TaskFuture(SimpleTask<T> task) {
        this.task = task;
        this.taskStatus = TaskStatus.WAITING;
    }

    void setFuture(Future<T> future) {
        this.future = future;
    }

    void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    void setResult(T result) {
        this.result = result;
    }

    SimpleTask<T> getTask() {
        return task;
    }

    Long getExecutionTimeout() {
        return executionTimeout;
    }

    void setExecutionTimeout(Long executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    /**
     * Get UUID of the task.
     *
     * @return UUID of the task.
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Get status of the task {@link TaskStatus}
     *
     * @return status of the task
     */
    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    /**
     * Cancel the task execution. Block the cancel operation util task future {@link Future} is not null.
     *
     * @param mayInterruptIfRunning true if the task executing should be interrupted; otherwise, in-progress tasks are allowed to complete.
     * @return if the task could not be cancelled, typically because it has already completed normally;
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        while (future == null) {
            sleep();
        }
        interrupt.set(true);
        taskStatus = TaskStatus.CANCELED;
        return future.cancel(mayInterruptIfRunning);
    }

    /**
     * Returns {@code true} if this task was cancelled.
     *
     * @return {@code true} if this task was cancelled
     */
    public boolean isCancelled() {
        return taskStatus == TaskStatus.CANCELED;
    }

    /**
     * Returns {@code true} if this task was not {@link TaskStatus#WAITING} or {@link TaskStatus#EXECUTING}.
     *
     * @return {@code true}/{@code false}.
     */
    public boolean isDone() {
        return taskStatus.getStatus() >= TaskStatus.COMPLETED.getStatus();
    }

    /**
     * Wait util the task has complete and return task exit value. {@link SimpleTask#execute()}
     *
     * @return the computed task exit value result.
     */
    public T waitFor() {
        while (!interrupt.get()) {
            sleep();
            if (taskStatus.getStatus() >= TaskStatus.COMPLETED.getStatus()) {
                break;
            }
        }
        sleep();
        if (future != null && !future.isDone() && !future.isCancelled()) {
            future.cancel(true);
        }
        return result;
    }

    /**
     * Wait the task execution util timeout. {@link SimpleTask#execute()}
     * <pre>
     *  The task may has been waiting to execute while timeout.
     *  It means when timeout occurred, the task may still
     *  blocking in the task queue waiting for executing.
     * </pre>
     *
     * @param timeout the maximum time to wait for the task execution, unit: ms
     * @return {@code true} if the task has executed and completed.
     */
    public boolean waitFor(int timeout) {
        boolean isDone = false;
        long start = System.currentTimeMillis();
        while (!interrupt.get()) {
            sleep();
            if ((System.currentTimeMillis() - start) <= timeout) {
                if (taskStatus.getStatus() >= TaskStatus.COMPLETED.getStatus()) {
                    isDone = true;
                    break;
                }
            } else {
                break;
            }
        }
        sleep();
        if (!isDone && future != null && !future.isDone() && !future.isCancelled()) {
            future.cancel(true);
        }
        return isDone;
    }

    /**
     * Returns the exit value for the task.
     *
     * @return the exit value for the task {@link SimpleTask#execute()}
     */
    public T exitValue() {
        sleep();
        return result;
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }
}
