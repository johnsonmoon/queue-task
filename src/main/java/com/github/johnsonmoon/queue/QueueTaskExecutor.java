package com.github.johnsonmoon.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Create by johnsonmoon at 2018/11/20 17:32.
 */
public class QueueTaskExecutor<T> {
    private static Logger logger = LoggerFactory.getLogger(QueueTaskExecutor.class);
    private Integer taskMaxCount = 1000;
    private Integer taskConcurrenceCount = 4;
    private Integer defaultTaskTimeout = 60_000;

    /**
     * Set Max size for task blocking queue.
     *
     * @param taskMaxCount Max size for task blocking queue
     * @return {@link QueueTaskExecutor}
     */
    public QueueTaskExecutor<T> taskMaxCount(Integer taskMaxCount) {
        this.taskMaxCount = taskMaxCount;
        return this;
    }

    /**
     * Set Concurrence size for executing tasks.
     * <pre>
     *  Default 4, means there will be 4 tasks at most executing at the same time.
     * </pre>
     *
     * @param taskConcurrenceCount Concurrence size for executing tasks.
     * @return {@link QueueTaskExecutor}
     */
    public QueueTaskExecutor<T> taskConcurrenceCount(Integer taskConcurrenceCount) {
        this.taskConcurrenceCount = taskConcurrenceCount;
        return this;
    }

    /**
     * Set default global task execution timeout after task has been submitted.
     *
     * <pre>
     *  Default timeout unit: ms, means that after task has been submitted, block wait before the task was completed.
     * </pre>
     *
     * @param defaultTaskTimeout Global task execution timeout after task has been submitted.
     * @return {@link QueueTaskExecutor}
     */
    public QueueTaskExecutor<T> defaultTaskTimeout(Integer defaultTaskTimeout) {
        this.defaultTaskTimeout = defaultTaskTimeout;
        return this;
    }

    /**
     * Shutdown flag
     */
    private AtomicBoolean shutdown = new AtomicBoolean(false);
    /**
     * Blocking queue for task {@link SimpleTask}
     */
    private BlockingQueue<TaskFuture<T>> taskFutureBlockingQueue;
    /**
     * Executor loop thread
     */
    private Thread executorThread;
    /**
     * Thread pool for task dispatcher.
     */
    private ExecutorService taskDispatcherThreadPool;
    /**
     * Thread pool for task executing.
     */
    private ExecutorService taskExecutionThreadPool;
    /**
     * Available count of tasks for executing. {@link TaskStatus#EXECUTING}
     */
    private TaskLatch taskLatch;

    /**
     * Submit task.
     *
     * @param task {@link SimpleTask}
     * @return {@link TaskFuture}
     */
    public TaskFuture<T> submit(SimpleTask<T> task) {
        TaskFuture<T> taskFuture = new TaskFuture<>(task);
        try {
            taskFutureBlockingQueue.put(taskFuture);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            taskFuture.setTaskStatus(TaskStatus.INTERRUPTED);
        }
        return taskFuture;
    }

    /**
     * Submit task.
     *
     * @param task             {@link SimpleTask}
     * @param executionTimeout timeout after task executing.
     * @return {@link TaskFuture}
     */
    public TaskFuture<T> submit(SimpleTask<T> task, long executionTimeout) {
        TaskFuture<T> taskFuture = new TaskFuture<>(task);
        taskFuture.setExecutionTimeout(executionTimeout);
        try {
            taskFutureBlockingQueue.put(taskFuture);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            taskFuture.setTaskStatus(TaskStatus.INTERRUPTED);
        }
        return taskFuture;
    }

    /**
     * Start the task executor.
     *
     * @return {@link QueueTaskExecutor}
     */
    public QueueTaskExecutor<T> start() {
        if (shutdown.get()) {
            return null;
        }
        taskFutureBlockingQueue = new LinkedBlockingDeque<>(taskMaxCount);
        taskExecutionThreadPool = Executors.newFixedThreadPool(taskConcurrenceCount);
        taskDispatcherThreadPool = Executors.newCachedThreadPool();
        taskLatch = new TaskLatch(taskConcurrenceCount);
        shutdown.set(false);
        executorThread = new Thread(() -> {
            while (!shutdown.get()) {
                loopSleep();
                if (taskLatch.acquire()) {
                    taskDispatcherThreadPool.submit(() -> {
                        try {
                            dispatcherTask();
                        } catch (Exception e) {
                            logger.warn(e.getMessage(), e);
                        } finally {
                            taskLatch.release();
                        }
                    });
                }
            }

        });
        executorThread.start();
        return this;
    }

    private void dispatcherTask() {
        final TaskFuture<T> taskFuture = taskFutureBlockingQueue.poll();
        if (taskFuture != null) {
            final String taskId = taskFuture.getTaskId();
            final SimpleTask<T> task = taskFuture.getTask();
            if (taskId != null && task != null) {
                taskFuture.setTaskStatus(TaskStatus.EXECUTING);
                Future<T> future = taskExecutionThreadPool.submit(() -> {
                    T result = null;
                    int step = 0;
                    try {
                        if (task instanceof Task) {
                            Task<T> tTask = (Task<T>) task;
                            step = 1;
                            tTask.before();
                            step = 2;
                            result = task.execute();
                            step = 3;
                            tTask.after(result);
                        } else {
                            result = task.execute();
                        }
                    } catch (Exception e) {
                        switch (step) {
                            case 1:
                                logger.warn(String.format("Exception happened while task before operation, message: %s", e.getMessage()), e);
                                break;
                            case 2:
                                logger.warn(String.format("Exception happened while task execution, message: %s", e.getMessage()), e);
                                break;
                            case 3:
                                logger.warn(String.format("Exception happened while task after operation, message: %s", e.getMessage()), e);
                                break;
                            default:
                                logger.warn(e.getMessage(), e);
                                break;
                        }
                    }
                    return result;
                });
                taskFuture.setFuture(future);
                Long executionTimeout = taskFuture.getExecutionTimeout() == null ? defaultTaskTimeout : taskFuture.getExecutionTimeout();
                try {
                    taskFuture.setResult(future.get(executionTimeout, TimeUnit.MILLISECONDS));
                    taskFuture.setTaskStatus(TaskStatus.COMPLETED);
                } catch (CancellationException cancelE) {
                    taskFuture.setTaskStatus(TaskStatus.CANCELED);
                } catch (InterruptedException interruptE) {
                    taskFuture.setTaskStatus(TaskStatus.INTERRUPTED);
                } catch (ExecutionException executionE) {
                    taskFuture.setTaskStatus(TaskStatus.ERROR);
                } catch (TimeoutException timeoutE) {
                    taskFuture.setTaskStatus(TaskStatus.TIMEOUT);
                }
            }
        }
    }

    /**
     * Stop the task executor.
     */
    public void stop() {
        if (!shutdown.get()) {
            return;
        }
        shutdown.set(true);
        executorThread.interrupt();
    }

    private static void loopSleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }
}
