# ThreadPoolShrinkPlugin
## 线程治理
> Android项目经常引用大量的第三方框架和SDK，这些框架中创建的线程池是完全不受控制的，如果使用不当，将会积攒大量无法回收的线程。
> 大多数厂商会对线程数进行限制，线程数过多直接引起崩溃；
> 线程也会占用大量资源，超过内存限制，将会引起无法创建新的线程OOM；
> 线程治理变得尤为重要，`ThreadPoolShrinkPlugin`插件主要作用：排查线程池使用、管理线程池

## 实现方案
1. **排查线程池使用:** 将线程池的调用在编译期全部打印到日志中；
2. **管理线程池** 将要修改的线程池替换为自己创建的线程池；

## 使用步骤
1. 配置项目`build.gradle
```groovy
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
	}
	dependencies {
        classpath "com.github.wei120698598:threadpool-shrink-plugin:Tag"
    }
}
```

2. 配置Application `build.gradle`
```groovy
apply plugin: 'com.planb.threadpool.shrink'
```

3. 添加`ThreadPoolShrinkOptions`配置
```gradle
threadPoolShrinkOptions {
    // 是否启用插件，如果想禁用建议直接控制apply plugin，效率会更高些
    enabled true
    
    // 日志开关
    debug true

    // 自己定义的线程池，会使用这个线程池替换项目中的线程池；
    // 线程池类型为ThreadPoolExecutor，签名可以是两种：
    // 1. 静态线程池对象；格式为：{className}.{staticField}
    // 2. 静态方法，返回线程池；格式为：{className}.{staticMethod}()
    defaultExecutorSignature 'com.planb.thread.DefaultThreadPoolExecutor.defaultExecutor'

    // 同defaultExecutorSignature，线程池类型为ScheduledThreadPoolExecutor;
    defaultScheduledExecutorSignature 'com.planb.thread.DefaultThreadPoolExecutor.defaultScheduledExecutor'

    // 不处理的类或包名
    classSignaturePrefixFilter =  [
        'com.planb.thread.ExcludeThreadPoolActivity'
    ]
    
    // 配置不打印的方法调用，默认打印线程池除了以外所有的方法调用；
    executorMethodPrintFilter = [
        'execute',
        'submit'
    ]
}

```
