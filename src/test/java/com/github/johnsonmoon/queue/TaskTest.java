package com.github.johnsonmoon.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Create by johnsonmoon at 2018/11/26 14:20.
 */
public class TaskTest {
    private static Logger logger = LoggerFactory.getLogger(TaskTest.class);

    private static int testCount = 20;

    private static void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    public static void main(String... args) {
        normalTest();
        normalTest2();
        executionTimeoutTest();
        waitTimeoutTest();
        cancelTest();
        simpleTaskTest();
        simpleTaskTestTimeout();
    }

    private static void normalTest() {
        QueueTaskExecutor<String> executor = new QueueTaskExecutor<String>()
                .taskMaxCount(1000)
                .taskConcurrenceCount(2)
                .defaultTaskTimeout(30_000)
                .start();
        //--------
        ExecutorService service = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(testCount);

        for (int i = 0; i < testCount; i++) {
            final Integer num = i;
            service.submit(() -> {
                try {
                    TaskFuture<String> taskFuture = executor.submit(new Task<String>() {
                        @Override
                        public void before() {
                        }

                        @Override
                        public String execute() {
                            try {
                                Thread.sleep(2000);
                            } catch (Exception e) {
                                logger.debug(e.getMessage());
                            }
                            return "execution done: " + System.currentTimeMillis();
                        }

                        @Override
                        public void after(String s) {
                        }
                    });
                    String result = taskFuture.waitFor();
                    logger.info(String.format("Task num: %s taskId: %s, status: %s, result: %s", num, taskFuture.getTaskId(), taskFuture.getTaskStatus(), result));
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        //--------
        executor.stop();

        try {
            Thread.sleep(5_000);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        System.out.println("\r\n\r\n - DONE -");
        System.exit(0);
    }

    private static void normalTest2() {
        QueueTaskExecutor<String> executor = new QueueTaskExecutor<String>()
                .taskMaxCount(1000)
                .taskConcurrenceCount(2)
                .defaultTaskTimeout(30_000)
                .start();
        //--------
        ExecutorService service = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(testCount);

        for (int i = 0; i < testCount; i++) {
            final Integer num = i;
            service.submit(() -> {
                try {
                    TaskFuture<String> taskFuture = executor.submit(new Task<String>() {
                        @Override
                        public void before() {
                        }

                        @Override
                        public String execute() {
                            try {
                                Thread.sleep(3000);
                            } catch (Exception e) {
                                logger.debug(e.getMessage());
                            }
                            return "execution done: " + System.currentTimeMillis();
                        }

                        @Override
                        public void after(String s) {
                        }
                    });
                    boolean resultCode = taskFuture.waitFor(10000);
                    logger.info(String.format("Task num: %s taskId: %s, status: %s, result code: %s, result: %s", num, taskFuture.getTaskId(), taskFuture.getTaskStatus(), resultCode, taskFuture.exitValue()));
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        //--------
        executor.stop();

        try {
            Thread.sleep(5_000);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        System.out.println("\r\n\r\n - DONE -");
        System.exit(0);
    }

    private static void executionTimeoutTest() {
        QueueTaskExecutor<String> executor = new QueueTaskExecutor<String>()
                .taskMaxCount(1000)
                .taskConcurrenceCount(2)
                .defaultTaskTimeout(30_000)
                .start();
        //--------
        ExecutorService service = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(testCount);

        for (int i = 0; i < testCount; i++) {
            final Integer num = i;
            service.submit(() -> {
                try {
                    TaskFuture<String> taskFuture = executor.submit(
                            new Task<String>() {
                                @Override
                                public void before() {
                                }

                                @Override
                                public String execute() {
                                    try {
                                        Thread.sleep(10_000);
                                    } catch (Exception e) {
                                        logger.debug(e.getMessage());
                                    }
                                    return "execution done: " + System.currentTimeMillis();
                                }

                                @Override
                                public void after(String s) {
                                }
                            },
                            5_000L);
                    String result = taskFuture.waitFor();
                    logger.info(String.format("Task num: %s taskId: %s, status: %s, result: %s", num, taskFuture.getTaskId(), taskFuture.getTaskStatus(), result));
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        //--------
        executor.stop();

        try {
            Thread.sleep(5_000);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        System.out.println("\r\n\r\n - DONE -");
        System.exit(0);
    }

    private static void waitTimeoutTest() {
        QueueTaskExecutor<String> executor = new QueueTaskExecutor<String>()
                .taskMaxCount(1000)
                .taskConcurrenceCount(2)
                .defaultTaskTimeout(30_000)
                .start();
        //--------
        ExecutorService service = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(testCount);

        for (int i = 0; i < testCount; i++) {
            final Integer num = i;
            service.submit(() -> {
                try {
                    TaskFuture<String> taskFuture = executor.submit(new Task<String>() {
                        @Override
                        public void before() {
                        }

                        @Override
                        public String execute() {
                            try {
                                Thread.sleep(10_000);
                            } catch (Exception e) {
                                logger.debug(e.getMessage());
                            }
                            return "execution done: " + System.currentTimeMillis();
                        }

                        @Override
                        public void after(String s) {
                        }
                    });
                    boolean resultCode = taskFuture.waitFor(5_000);
                    logger.info(String.format("Task num: %s taskId: %s, status: %s, result code: %s, result: %s", num, taskFuture.getTaskId(), taskFuture.getTaskStatus(), resultCode, taskFuture.exitValue()));
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        //--------
        executor.stop();

        try {
            Thread.sleep(5_000);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        System.out.println("\r\n\r\n - DONE -");
        System.exit(0);
    }

    private static void cancelTest() {
        QueueTaskExecutor<String> executor = new QueueTaskExecutor<String>()
                .taskMaxCount(100)
                .taskConcurrenceCount(5)
                .defaultTaskTimeout(60_000)
                .start();
        //--------
        ExecutorService service = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(testCount);

        for (int i = 0; i < testCount; i++) {
            final Integer num = i;
            service.submit(() -> {
                try {
                    TaskFuture<String> taskFuture = executor.submit(new Task<String>() {
                        @Override
                        public void before() {
                        }

                        @Override
                        public String execute() {
                            try {
                                Thread.sleep(10_000);
                            } catch (Exception e) {
                                logger.debug(e.getMessage());
                            }
                            return "Done: " + System.currentTimeMillis();
                        }

                        @Override
                        public void after(String s) {
                        }
                    });
                    sleep(2_000);
                    boolean cancelResult = taskFuture.cancel(true);
                    String result = taskFuture.waitFor();
                    logger.info(String.format("Task num: %s taskId: %s, status: %s, cancel result: %s, result: %s", num, taskFuture.getTaskId(), taskFuture.getTaskStatus(), cancelResult, result));
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        //--------
        executor.stop();

        try {
            Thread.sleep(5_000);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        System.out.println("\r\n\r\n - DONE -");
        System.exit(0);
    }

    private static void simpleTaskTest() {
        QueueTaskExecutor<String> executor = new QueueTaskExecutor<String>()
                .taskMaxCount(100)
                .taskConcurrenceCount(5)
                .defaultTaskTimeout(60_000)
                .start();
        //--------
        ExecutorService service = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(testCount);

        for (int i = 0; i < testCount; i++) {
            final Integer num = i;
            service.submit(() -> {
                try {
                    TaskFuture<String> taskFuture = executor.submit(
                            () -> {
                                try {
                                    Thread.sleep(2_000);
                                } catch (Exception e) {
                                    logger.debug(e.getMessage());
                                }
                                return "Done: " + System.currentTimeMillis();
                            }
                    );
                    String result = taskFuture.waitFor();
                    logger.info(String.format("Task num: %s taskId: %s, status: %s, result: %s", num, taskFuture.getTaskId(), taskFuture.getTaskStatus(), result));
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        //--------
        executor.stop();

        try {
            Thread.sleep(5_000);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        System.out.println("\r\n\r\n - DONE -");
        System.exit(0);
    }

    private static void simpleTaskTestTimeout() {
        QueueTaskExecutor<String> executor = new QueueTaskExecutor<String>()
                .taskMaxCount(100)
                .taskConcurrenceCount(5)
                .defaultTaskTimeout(60_000)
                .start();
        //--------
        ExecutorService service = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(testCount);

        for (int i = 0; i < testCount; i++) {
            final Integer num = i;
            service.submit(() -> {
                try {
                    TaskFuture<String> taskFuture = executor.submit(
                            () -> {
                                try {
                                    Thread.sleep(10_000);
                                } catch (Exception e) {
                                    logger.debug(e.getMessage());
                                }
                                return "Done: " + System.currentTimeMillis();
                            },
                            3_000
                    );
                    String result = taskFuture.waitFor();
                    logger.info(String.format("Task num: %s taskId: %s, status: %s, result: %s", num, taskFuture.getTaskId(), taskFuture.getTaskStatus(), result));
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        //--------
        executor.stop();

        try {
            Thread.sleep(5_000);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        System.out.println("\r\n\r\n - DONE -");
        System.exit(0);
    }
}
