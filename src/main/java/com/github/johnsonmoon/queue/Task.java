package com.github.johnsonmoon.queue;

/**
 * Create by johnsonmoon at 2018/11/27 11:41.
 */
public interface Task<T> extends SimpleTask<T> {
    /**
     * Method before task execution.
     */
    void before();

    /**
     * Method after task executed (was done).
     *
     * @param t returned value by task executed.
     */
    void after(T t);
}
