package com.github.johnsonmoon.queue;


/**
 * Create by johnsonmoon at 2018/11/21 10:20.
 */
public interface SimpleTask<T> {
    /**
     * Execute something. Main function of a task.
     */
    T execute();
}
