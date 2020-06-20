package com.planb.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class DefaultThreadPoolExecutor {
    public static final ThreadPoolExecutor defaultExecutor = new ThreadPoolExecutor(10, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());


    public static final ScheduledThreadPoolExecutor defaultScheduledExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(5);
}

