package com.github.johnsonmoon.queue;

/**
 * Create by johnsonmoon at 2018/11/23 14:41.
 */
public class TaskLatch {
    private int taskCount;

    TaskLatch(int taskCount) {
        this.taskCount = taskCount;
    }

    private int executingCount = 0;

    boolean acquire() {
        if (executingCount >= taskCount) {
            return false;
        }
        executingCount++;
        return true;
    }

    void release() {
        if (executingCount <= 0) {
            return;
        }
        executingCount--;
    }

    void printAvailableCount() {
        System.out.println(String.format("Available count: %s, taskLatch: %s", taskCount - executingCount, this.toString()));
    }

    @Override
    public String toString() {
        return "TaskLatch{" +
                "taskCount=" + taskCount +
                ", executingCount=" + executingCount +
                '}';
    }
}
