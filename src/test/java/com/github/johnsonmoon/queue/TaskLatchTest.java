package com.github.johnsonmoon.queue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Create by johnsonmoon at 2018/11/23 14:48.
 */
public class TaskLatchTest {
    private static Logger logger = LoggerFactory.getLogger(TaskLatchTest.class);

    @Test
    public void test() {
        TaskLatch taskLatch = new TaskLatch(2);

        ExecutorService service = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {
            sleep(1000);
            taskLatch.printAvailableCount();
            if (taskLatch.acquire()) {
                final Integer num = i;
                service.submit(() -> {
                    sleep(4000);
                    System.out.println(String.format("Num: %s complete.", num));
                    taskLatch.release();
                });
            }
        }

        sleep(10_000);
    }

    private void sleep(int t) {
        try {
            Thread.sleep(t);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }
}
