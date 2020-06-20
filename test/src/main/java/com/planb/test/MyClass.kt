package com.planb.test

import java.util.concurrent.Executors

class MyClass {
    fun test(){
        val newCachedThreadPool = Executors.newCachedThreadPool()
        newCachedThreadPool.submit(Runnable {  })
    }
}