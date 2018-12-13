package com.github.johnsonmoon.queue;

/**
 * Create by johnsonmoon at 2018/11/23 14:17.
 */
public enum TaskStatus {
    WAITING(0),
    EXECUTING(1),
    COMPLETED(2),
    ERROR(3),
    CANCELED(4),
    TIMEOUT(5),
    INTERRUPTED(6);

    private Integer status;

    TaskStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
