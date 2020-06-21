package com.planb.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class ThreadTest {
    public static String printThread() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup finalTopGroup = group;
        // 遍历线程组树，获取根线程组
        while (group != null) {
            finalTopGroup = group;
            group = group.getParent();
        }
        // 激活的线程数再加一倍，防止枚举时有可能刚好有动态线程生成
        int slackSize = finalTopGroup.activeCount() * 2;
        Thread[] slackThreads = new Thread[slackSize];
        // 获取根线程组下的所有线程，返回的actualSize便是最终的线程数
        int actualSize = finalTopGroup.enumerate(slackThreads);
        int activeCount = finalTopGroup.activeCount();
        String log = "总共线程：" + actualSize + "\n活跃线程数：" + activeCount + "\nAllStack:" + Thread.getAllStackTraces().size();
        System.out.println(log);
        return log;
    }

    public static void runThread() {
        ExecutorService threadPool = new ThreadPoolExecutor(50, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
        for (int j = 0; j < 100; j++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.activeCount() + ":" + Thread.currentThread().getName());
                }
            });
        }

        ThreadPoolExecutor threadPool2 = new ThreadPoolExecutor(50, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }
}
