# queue-task
A task queue for executing task by plan.  

If your machine can not stand too much thread executing at the same time, you can use this task queue end execute by sequences.

## Usage

### Simple usage
```
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
```

And the result is: 

```

[2018-12-12 16:24:31:589] taskId: 2afd1912e76c400987804e83ab6fc422, result: -DONE-
[2018-12-12 16:24:34:631] taskId: d48f1df1c4364d28b30d4b14535cdd12, result: -DONE-
[2018-12-12 16:24:37:667] taskId: b5a08e9d105b49b38acd9d544a72145f, result: -DONE-
```

### Example No.1:
```
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
```

And the result is: 
```

---------------------
[2018-12-12 16:25:12:396] taskId: 28248cdd933641fcac24415c3a5632b9, status: WAITING
[2018-12-12 16:25:12:397] taskId: 58c9e34b53014ef091812eb7ee7f06a6, status: WAITING
[2018-12-12 16:25:12:397] taskId: 0318d0a048d643839e5ca4bc4e2748d2, status: WAITING
[2018-12-12 16:25:12:397] taskId: a4ee0322de974c61b1c409eb11b9580e, status: WAITING
[2018-12-12 16:25:12:398] taskId: 406aa6d336f948cb81e641c1ed6b59bb, status: WAITING

---------------------
[2018-12-12 16:25:15:402] taskId: 28248cdd933641fcac24415c3a5632b9, status: COMPLETED
[2018-12-12 16:25:15:402] taskId: 58c9e34b53014ef091812eb7ee7f06a6, status: EXECUTING
[2018-12-12 16:25:15:403] taskId: 0318d0a048d643839e5ca4bc4e2748d2, status: WAITING
[2018-12-12 16:25:15:403] taskId: a4ee0322de974c61b1c409eb11b9580e, status: WAITING
[2018-12-12 16:25:15:403] taskId: 406aa6d336f948cb81e641c1ed6b59bb, status: WAITING

---------------------
[2018-12-12 16:25:18:408] taskId: 28248cdd933641fcac24415c3a5632b9, status: COMPLETED
[2018-12-12 16:25:18:408] taskId: 58c9e34b53014ef091812eb7ee7f06a6, status: COMPLETED
[2018-12-12 16:25:18:409] taskId: 0318d0a048d643839e5ca4bc4e2748d2, status: EXECUTING
[2018-12-12 16:25:18:409] taskId: a4ee0322de974c61b1c409eb11b9580e, status: EXECUTING
[2018-12-12 16:25:18:409] taskId: 406aa6d336f948cb81e641c1ed6b59bb, status: WAITING

---------------------
[2018-12-12 16:25:21:413] taskId: 28248cdd933641fcac24415c3a5632b9, status: COMPLETED
[2018-12-12 16:25:21:413] taskId: 58c9e34b53014ef091812eb7ee7f06a6, status: COMPLETED
[2018-12-12 16:25:21:414] taskId: 0318d0a048d643839e5ca4bc4e2748d2, status: COMPLETED
[2018-12-12 16:25:21:414] taskId: a4ee0322de974c61b1c409eb11b9580e, status: COMPLETED
[2018-12-12 16:25:21:414] taskId: 406aa6d336f948cb81e641c1ed6b59bb, status: EXECUTING

---------------------
[2018-12-12 16:25:24:417] taskId: 28248cdd933641fcac24415c3a5632b9, status: COMPLETED
[2018-12-12 16:25:24:417] taskId: 58c9e34b53014ef091812eb7ee7f06a6, status: COMPLETED
[2018-12-12 16:25:24:418] taskId: 0318d0a048d643839e5ca4bc4e2748d2, status: COMPLETED
[2018-12-12 16:25:24:418] taskId: a4ee0322de974c61b1c409eb11b9580e, status: COMPLETED
[2018-12-12 16:25:24:418] taskId: 406aa6d336f948cb81e641c1ed6b59bb, status: COMPLETED

---------------------
-DONE-
-DONE-
-DONE-
-DONE-
-DONE-
```

### Example No.2:
```
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
```

And the result is: 
```

---------------------
[2018-12-12 16:25:37:302] taskId: 48766295a2584ef49ddc374e1f3ba0ed, status: WAITING
[2018-12-12 16:25:37:303] taskId: e63440f37ffa4532b0a663519d687c4c, status: WAITING
[2018-12-12 16:25:37:303] taskId: 0c9c34813171454eab4a2de4b21ba282, status: WAITING
[2018-12-12 16:25:37:304] taskId: ff7f55a6fe4f47009e8418601430e9d3, status: WAITING
[2018-12-12 16:25:37:304] taskId: ae711ef97cb14a908552f030601f7ff9, status: WAITING

---------------------
[2018-12-12 16:25:40:307] taskId: 48766295a2584ef49ddc374e1f3ba0ed, status: COMPLETED
[2018-12-12 16:25:40:308] taskId: e63440f37ffa4532b0a663519d687c4c, status: EXECUTING
[2018-12-12 16:25:40:308] taskId: 0c9c34813171454eab4a2de4b21ba282, status: WAITING
[2018-12-12 16:25:40:308] taskId: ff7f55a6fe4f47009e8418601430e9d3, status: WAITING
[2018-12-12 16:25:40:308] taskId: ae711ef97cb14a908552f030601f7ff9, status: WAITING

---------------------
[2018-12-12 16:25:43:311] taskId: 48766295a2584ef49ddc374e1f3ba0ed, status: COMPLETED
[2018-12-12 16:25:43:312] taskId: e63440f37ffa4532b0a663519d687c4c, status: COMPLETED
[2018-12-12 16:25:43:312] taskId: 0c9c34813171454eab4a2de4b21ba282, status: EXECUTING
[2018-12-12 16:25:43:312] taskId: ff7f55a6fe4f47009e8418601430e9d3, status: EXECUTING
[2018-12-12 16:25:43:312] taskId: ae711ef97cb14a908552f030601f7ff9, status: WAITING

---------------------
[2018-12-12 16:25:46:318] taskId: 48766295a2584ef49ddc374e1f3ba0ed, status: COMPLETED
[2018-12-12 16:25:46:319] taskId: e63440f37ffa4532b0a663519d687c4c, status: COMPLETED
[2018-12-12 16:25:46:319] taskId: 0c9c34813171454eab4a2de4b21ba282, status: COMPLETED
[2018-12-12 16:25:46:319] taskId: ff7f55a6fe4f47009e8418601430e9d3, status: COMPLETED
[2018-12-12 16:25:46:319] taskId: ae711ef97cb14a908552f030601f7ff9, status: EXECUTING

---------------------
[2018-12-12 16:25:49:320] taskId: 48766295a2584ef49ddc374e1f3ba0ed, status: COMPLETED
[2018-12-12 16:25:49:320] taskId: e63440f37ffa4532b0a663519d687c4c, status: COMPLETED
[2018-12-12 16:25:49:321] taskId: 0c9c34813171454eab4a2de4b21ba282, status: COMPLETED
[2018-12-12 16:25:49:321] taskId: ff7f55a6fe4f47009e8418601430e9d3, status: COMPLETED
[2018-12-12 16:25:49:321] taskId: ae711ef97cb14a908552f030601f7ff9, status: COMPLETED

---------------------
-DONE-
-DONE-
-DONE-
-DONE-
-DONE-
```