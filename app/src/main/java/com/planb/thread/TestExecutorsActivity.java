package com.planb.thread;

import android.app.Activity;
import android.os.Bundle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class TestExecutorsActivity extends Activity {
    static final ScheduledExecutorService defaultExecutor = Executors.newScheduledThreadPool(20);
    ExecutorService executorService1 = Executors.newSingleThreadScheduledExecutor();
    static ExecutorService executorService2 = Executors.newScheduledThreadPool(120);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        executorService1.submit(new Runnable() {
            @Override
            public void run() {

            }
        });

    }


    public void test() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("Test For Executors.newCachedThreadPool");
            }
        });
    }

    public void test2() {
        Executors.newFixedThreadPool(10).submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("Test For Executors.newFixedThreadPool");
            }
        });
    }

    public void test3() {
        executorService2.submit(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
}
