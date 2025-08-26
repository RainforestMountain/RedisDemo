// package com.example.redisdemo_1.timedTask;

// import jakarta.annotation.PostConstruct;
// import jakarta.annotation.Resource;
// import org.redisson.api.CronSchedule;
// import org.redisson.api.RScheduledExecutorService;
// import org.redisson.api.RedissonClient;
// import org.redisson.executor.TasksService;
// import org.springframework.stereotype.Component;

// import java.util.concurrent.TimeUnit;

// @Component
// public class TimedTask {
//     @Resource
//     private RedissonClient redissonClient;
//     @Resource
//     private TasksService tasksService;

//     /**
//      * 用于存储任务id,便于后续可能的取消操作
//      */
//     private String dailyStatisticsTaskId;
//     private String cleanupTaskId;
//     private String dataSyncTaskId;

//     @PostConstruct
//     public void initScheduledTasks() {
//         //获取Redisson的调度器
//         RScheduledExecutorService scheduler = redissonClient.getExecutorService("scheduledTaskExecutor");

//         //1.基于Cron（计时程序）表达式 --每天凌晨两点执行统计任务
//         dailyStatisticsTaskId = scheduler.schedule(
//                 new Runnable() {
//                     @Override
//                     public void run() {
//                         //执行统计任务
//                         System.out.println("执行统计任务");
//                     }
//                 }, CronSchedule.of("0 0 2 * * ?")
//         ).getTaskId();

//         //2.固定延迟任务 - 初始延迟10秒， 之后每次任务完成后延迟30秒再执行
//         cleanupTaskId = scheduler.scheduleWithFixedDelay(
//                 () -> System.out.println("延迟清除任务"),
//                 10,
//                 30,
//                 TimeUnit.SECONDS
//         ).getTaskId();

//         //固定速率的任务 - 初始延迟5秒，之后每15秒执行一次（以上次开始时间计算）
//         dataSyncTaskId = String.valueOf(scheduler.scheduleAtFixedRate(
//                 () -> System.out.println("执行固定速率的任务"),
//                 5,
//                 15,
//                 TimeUnit.SECONDS
//         ));
//     }

//     private void executeDailyStatistics() {

//     }

// }
