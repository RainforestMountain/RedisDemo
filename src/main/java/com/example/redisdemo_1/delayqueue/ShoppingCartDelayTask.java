package com.example.redisdemo_1.delayqueue;

import com.example.redisdemo_1.entity.CartTask;
import jakarta.annotation.PostConstruct;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 购物车延时任务管理器
 * 当用户将商品加入购物车并生成订单时，调用 addDelayTask() 方法添加延时任务
 * 任务会在 Redis 中存储 30 分钟，到期后自动进入目标队列
 * 消费者线程通过 take() 方法（阻塞式）获取到期任务并处理,这是还没有支付的订单
 * 处理逻辑包括取消订单、恢复库存等操作
 */
@Service
public class ShoppingCartDelayTask {
    private static RedissonClient redissonClient;
    private static RDelayedQueue<CartTask> delayedQueue;
    private static RBlockingQueue<CartTask> targetQueue;

    @Value("${CLOUD_SERVER_HOST}")
    private String CLOUD_SERVER_HOST;
    @Value("${CLOUD_SERVER_PWD}")
    private String CLOUD_SERVER_PWD;
    @Value("${CLOUD_SERVER_USERNAME}")
    private String CLOUD_SERVER_USERNAME;


    /**
     * @PostConstruct 是 Java EE 规范中的一个注解（位于 javax.annotation 包下，在 Java 9+ 中需要额外引入依赖），
     * 用于标记对象初始化完成后执行的方法。
     * 在 Spring 框架中，它的核心作用是：在依赖注入（DI）完成后，执行初始化逻辑。
     */
    @PostConstruct
    public void init() {
        try {
            // 1. 校验配置参数
            if (CLOUD_SERVER_HOST == null || CLOUD_SERVER_HOST.trim().isEmpty()) {
                throw new IllegalArgumentException("Redis主机地址(CLOUD_SERVER_HOST)未配置");
            }

            // 2. 初始化Redisson客户端
            Config config = new Config();
            config.useSingleServer()
                    .setAddress("redis://" + CLOUD_SERVER_HOST + ":6379")
                    .setPassword(CLOUD_SERVER_PWD)  // 若密码为空会自动忽略，无需特殊处理
                    .setDatabase(4)
                    .setConnectTimeout(5000)
                    .setRetryAttempts(2);

            redissonClient = Redisson.create(config);
            System.out.println("Redisson客户端初始化成功");

            // 3. 初始化队列
            targetQueue = redissonClient.getBlockingQueue("cart_order_queue");
            delayedQueue = redissonClient.getDelayedQueue(targetQueue);
            System.out.println("队列初始化成功");

            // 4. 启动消费者
            startConsumer();

        } catch (Exception e) {
            System.err.println("初始化失败：" + e.getMessage());
            // 释放资源
            if (redissonClient != null) {
                redissonClient.shutdown();
            }
            throw new RuntimeException("初始化Redisson失败", e);
        }
    }

    /**
     * 生产者：添加延时任务： 30分钟后执行
     *
     * @param task
     */
    public void addDelayTask(CartTask task) {
        //将任务添加到延时队列， 30分钟后自动转换到目标队列
        delayedQueue.offer(task, 20, TimeUnit.SECONDS);
        System.out.println("已经添加延时任务 - 用户:" + task.getUserId() +
                ", 商品：" + task.getProductId() +
                ", 订单：" + task.getOrderId());
    }

    /**
     * 启动消费者线程
     */
    private void startConsumer() {
        new Thread(() -> {
            System.out.println("购买车超时任务消费者已启动");
            while (true) {
                try {
                    //从目标队列获取到期任务（阻塞等待）
                    CartTask task = targetQueue.take();
                    if (task != null) {
                        //处理超时任务：取消订单，释放库存
                        processExpiredTask(task);
                    }
                } catch (InterruptedException e) {
                    System.err.println("获取任务异常： " + e.getMessage());
                    //短暂休眠后重试，避免cpu空转
                    try {
                        TimeUnit.MICROSECONDS.sleep(300);
                    } catch (InterruptedException ex) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void processExpiredTask(CartTask task) {
        System.out.println("执行超时取消用户:" + task.getUserId() +
                "，商品：" + task.getProductId() +
                "，订单：" + task.getOrderId() +
                "-因30分钟没有支付已经自动取消");
        System.err.println("已经取消订单");
        System.err.println("恢复库存");

    }
}
