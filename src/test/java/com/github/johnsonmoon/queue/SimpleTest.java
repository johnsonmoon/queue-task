package com.github.johnsonmoon.queue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Create by johnsonmoon at 2018/12/12 15:22.
 */
public class SimpleTest {
    private static Logger logger = LoggerFactory.getLogger(SimpleTest.class);

    @Test
    public void test0() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

        //--- configure the task executor and start it
        QueueTaskExecutor<String> executor = new QueueTaskExecutor<String>()
                .defaultTaskTimeout(30_000)//Set default timeout for single task execution
                .taskConcurrenceCount(2)//Set max count of task executing at the same time
                .taskMaxCount(100)//Set max block waiting task count
                .start();


        {
            //--- submit task
            TaskFuture<String> taskFuture = executor.submit(() -> {
                // TODO task codes
                try {
                    Thread.sleep(3_000);
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                }
                return "-DONE-";
            });

            //--- wait task done
            String result = taskFuture.waitFor();
            System.out.println(String.format("[%s] taskId: %s, result: %s", simpleDateFormat.format(new Date()), taskFuture.getTaskId(), result));
        }


        {
            //--- submit task
            TaskFuture<String> taskFuture = executor.submit(() -> {
                // TODO task codes
                try {
                    Thread.sleep(3_000);
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                }
                return "-DONE-";
            });

            //--- wait task done
            if (taskFuture.waitFor(10_000)) {
                String result = taskFuture.exitValue();
                System.out.println(String.format("[%s] taskId: %s, result: %s", simpleDateFormat.format(new Date()), taskFuture.getTaskId(), result));
            }
        }

        {
            //--- submit task
            TaskFuture<String> taskFuture = executor.submit(new Task<String>() {
                @Override
                public void before() {
                    //TODO code before this task executing
                }

                @Override
                public void after(String s) {
                    //TODO code after this task executed
                }

                @Override
                public String execute() {
                    //TODO task codes
                    try {
                        Thread.sleep(3_000);
                    } catch (Exception e) {
                        logger.debug(e.getMessage());
                    }
                    return "-DONE-";
                }
            });

            //--- wait task done
            String result = taskFuture.waitFor();
            System.out.println(String.format("[%s] taskId: %s, result: %s", simpleDateFormat.format(new Date()), taskFuture.getTaskId(), result));
        }

        //--- stop the executor
        executor.stop();
    }

    @Test
    public void test1() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

        //--- configure the task executor and start it
        QueueTaskExecutor<String> executor = new QueueTaskExecutor<String>()
                .defaultTaskTimeout(30_000)//Set default timeout for single task execution
                .taskConcurrenceCount(2)//Set max count of task executing at the same time
                .taskMaxCount(100)//Set max block waiting task count
                .start();

        //--- submit tasks
        List<TaskFuture<String>> taskFutures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TaskFuture<String> taskFuture = executor.submit(() -> {
                // TODO task codes
                try {
                    Thread.sleep(3_000);
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                }
                return "-DONE-";
            });
            taskFutures.add(taskFuture);
        }

        //--- wait all task done
        while (true) {
            boolean alldone = true;
            System.out.println("\r\n---------------------");
            for (TaskFuture<String> taskFuture : taskFutures) {
                System.out.println(String.format("[%s] taskId: %s, status: %s", simpleDateFormat.format(new Date()), taskFuture.getTaskId(), taskFuture.getTaskStatus()));

                alldone = alldone && taskFuture.isDone();
            }
            if (alldone) {
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }

        //--- get all task exit values
        System.out.println("\r\n---------------------");
        for (TaskFuture<String> taskFuture : taskFutures) {
            System.out.println(taskFuture.exitValue());
        }

        //--- stop the executor
        executor.stop();
    }

    @Test
    public void test2() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

        //--- configure the task executor and start it
        QueueTaskExecutor<String> executor = new QueueTaskExecutor<String>()
                .defaultTaskTimeout(30_000)//Set default timeout for single task execution
                .taskConcurrenceCount(2)//Set max count of task executing at the same time
                .taskMaxCount(100)//Set max block waiting task count
                .start();

        //--- submit tasks
        List<TaskFuture<String>> taskFutures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TaskFuture<String> taskFuture = executor.submit(new Task<String>() {
                @Override
                public void before() {
                    //TODO code before this task executing
                }

                @Override
                public void after(String s) {
                    //TODO code after this task executed
                }

                @Override
                public String execute() {
                    //TODO task codes
                    try {
                        Thread.sleep(3_000);
                    } catch (Exception e) {
                        logger.debug(e.getMessage());
                    }
                    return "-DONE-";
                }
            });
            taskFutures.add(taskFuture);
        }

        //--- wait all task done
        while (true) {
            boolean alldone = true;
            System.out.println("\r\n---------------------");
            for (TaskFuture<String> taskFuture : taskFutures) {
                System.out.println(String.format("[%s] taskId: %s, status: %s", simpleDateFormat.format(new Date()), taskFuture.getTaskId(), taskFuture.getTaskStatus()));

                alldone = alldone && (taskFuture.getTaskStatus().getStatus() >= TaskStatus.COMPLETED.getStatus());
            }
            if (alldone) {
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }

        //--- get all task exit values
        System.out.println("\r\n---------------------");
        for (TaskFuture<String> taskFuture : taskFutures) {
            System.out.println(taskFuture.exitValue());
        }

        //--- stop the executor
        executor.stop();
    }
}
