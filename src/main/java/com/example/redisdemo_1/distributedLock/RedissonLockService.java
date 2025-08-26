package com.example.redisdemo_1.distributedLock;


import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedissonLockService {
    @Resource
    private RedissonClient redissonClient;

    /**
     * 默认等待拿到锁的时间
     */
    private static final long defaultWaitTime = 60;
    /**
     * 默认锁的自动释放时间
     */
    private static final long defaultLeaseTime = 30;
    /**
     * 使用Redisson的Watch Dog 自动续期机制
     */
    private static final long WATCH_DOG_LEASE_TIME = -1;

    /**
     * 获取分布锁并执行任务
     *
     * @param lockKey   锁的唯一标识
     * @param waitTime  最多等待时间
     * @param leaseTime 锁的自动释放时间, Redisson内置的看门狗机制，当为-1的时候启用Watch Dog 自动续期
     * @param timeUnit  时间单位
     * @param task      需要执行的任务
     */
    public void executeWithLock(String lockKey, long waitTime, long leaseTime,
                                TimeUnit timeUnit, Runnable task) {
        //先拿到锁对象（Redisson的锁是可重入的）
        RLock lock = redissonClient.getLock(lockKey);

        try {
            //尝试获取锁
            //当leaseTime = -1时， Redisson使用默认的30秒过期时间并启用Watch Dog
            boolean isLocked = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (isLocked) {
                //获取锁成功，执行任务
                task.run();
            } else {
                //获取锁失败
                throw new RuntimeException("获取分布式锁失败， 锁键" + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); //恢复中断状态
            throw new RuntimeException("获取锁被中断，锁键：" + lockKey, e);
        } finally {
            //只有当前线程持有锁时，才能释放，避免释放其他线程的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 重载方法，使用默认的等待时间和释放时间60秒
     */
    public void executeWithLock(String lockKey, Runnable task) {
        executeWithLock(lockKey, defaultWaitTime, defaultLeaseTime, TimeUnit.SECONDS, task);
    }

    /**
     * 使用Watch Dog 自动续期机制
     * 初始锁过期时间为30秒，当时持有锁的线程时， 每隔30秒自动续期一次， 内部时是要过期的时候，执行Lua脚本
     *
     * @param lockKey
     * @param waitTime
     * @param timeUnit
     * @param task
     */
    public void executeWithLockAutoRenew(String lockKey, long waitTime, TimeUnit timeUnit, Runnable task) {
        executeWithLock(lockKey, waitTime, WATCH_DOG_LEASE_TIME, timeUnit, task);
    }

    /**
     * 重载方法，使用默认等待时间和Watch Dog 自动续期机制
     */
    public void executeWithLockAutoRenew(String lockKey, Runnable task) {
        executeWithLock(lockKey, defaultWaitTime, WATCH_DOG_LEASE_TIME, TimeUnit.SECONDS, task);
    }

    //定时任务
}
