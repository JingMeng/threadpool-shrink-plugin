package com.planb.threadpool.shrink

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.concurrent.Executors

/**
 * v1.0 of the file created on 2020/6/20 by shuxin.wei
 *
 */
internal class ThreadPoolShrinkPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val appExtension: AppExtension? = project.extensions.findByType(AppExtension::class.java)
        val options =
                project.extensions.create(
                        "threadPoolShrinkOptions",
                        ThreadPoolShrinkOptions::class.java
                )
        appExtension?.registerTransform(ThreadPoolShrinkTransform(project, options))
    }
}


open class ThreadPoolShrinkOptions {
    //日志开关
    var debug = true

    var enabled = true

    var filterClassSignaturePrefix: MutableList<String> = ArrayList()

    var filterExecutorMethodPrint: MutableList<String> = ArrayList()

    var defaultExecutorSignature: String? = null

    var defaultScheduledExecutorSignature: String? = null
}


fun main() {
    Executors::class.java.declaredMethods.forEach {
        println(it.name)
    }
}