package com.planb.threadpool.shrink

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import javassist.ClassPool
import javassist.expr.Expr
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import javassist.expr.NewExpr
import org.gradle.api.Project
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadPoolExecutor

val ExecutorsClassName: String = java.util.concurrent.Executors::class.java.name

val ThreadPoolExecutorClassName: String = ThreadPoolExecutor::class.java.name

val ScheduledThreadPoolExecutorClassName: String =
        java.util.concurrent.ScheduledThreadPoolExecutor::class.java.name

private val classPool: ClassPool = ClassPool.getDefault()
var isLoaded = false
var defaultExecutorClass: String? = null
var defaultExecutor: String? = null
var defaultScheduledExecutorClass: String? = null
var defaultScheduledExecutor: String? = null
lateinit var threadPoolShrinkOptions: ThreadPoolShrinkOptions

val executorServiceMethods = arrayOf(
        "newFixedThreadPool",
        "newFixedThreadPool",
        "newWorkStealingPool",
        "newWorkStealingPool",
        "newSingleThreadExecutor",
        "newSingleThreadExecutor",
        "newCachedThreadPool",
        "newCachedThreadPool"
)
val scheduledExecutorServiceMethods = arrayOf(
        "newSingleThreadScheduledExecutor",
        "newSingleThreadScheduledExecutor",
        "newScheduledThreadPool",
        "newScheduledThreadPool",
        "unconfigurableExecutorService",
        "unconfigurableScheduledExecutorService"
)

val defaultFilterExecutorMethodPrint = HashSet<String>()

val defaultFilterClassSignaturePrefix = hashSetOf(
        "android.support.v4.content",
        "androidx.core.content",
        "com.sankuai.movie.luacher.sdks.RxJavaInit"
)

val executorClassNames = arrayOf(
        ExecutorsClassName,
        ThreadPoolExecutorClassName,
        ScheduledThreadPoolExecutorClassName,
        ExecutorService::class.java.name,
        ScheduledExecutorService::class.java.name
)

fun loadClassPath(project: Project, options: ThreadPoolShrinkOptions) {
    if (isLoaded) return
    threadPoolShrinkOptions = options

    defaultExecutorClass = threadPoolShrinkOptions.defaultExecutorSignature!!.substring(
            0,
            threadPoolShrinkOptions.defaultExecutorSignature!!.lastIndexOf(".")
    )
    defaultExecutor = threadPoolShrinkOptions.defaultExecutorSignature!!.substring(
            threadPoolShrinkOptions.defaultExecutorSignature!!.lastIndexOf(".") + 1,
            threadPoolShrinkOptions.defaultExecutorSignature!!.length
    )
    defaultScheduledExecutorClass =
            threadPoolShrinkOptions.defaultScheduledExecutorSignature!!.substring(
                    0,
                    threadPoolShrinkOptions.defaultScheduledExecutorSignature!!.lastIndexOf(".")
            )
    defaultScheduledExecutor =
            threadPoolShrinkOptions.defaultScheduledExecutorSignature!!.substring(
                    threadPoolShrinkOptions.defaultScheduledExecutorSignature!!.lastIndexOf(".") + 1,
                    threadPoolShrinkOptions.defaultScheduledExecutorSignature!!.length
            )

    defaultFilterExecutorMethodPrint.addAll(threadPoolShrinkOptions.executorMethodPrintFilter)
    defaultFilterClassSignaturePrefix.addAll(threadPoolShrinkOptions.classSignaturePrefixFilter)

    isLoaded = true
    if (project.plugins.hasPlugin(AppPlugin::class.java)) {
        project.plugins.getPlugin(AppPlugin::class.java).extension.bootClasspath
    } else if (project.plugins.hasPlugin(LibraryPlugin::class.java)) {
        project.plugins.getPlugin(LibraryPlugin::class.java).extension.bootClasspath
    } else {
        return
    }.forEach {
        classPool.appendClassPath(it.absolutePath)
    }

//    classPool.appendClassPath(project.android.bootClasspath[0].toString())
}

fun loadClassPath(file: File) {
    classPool.appendClassPath(file.absolutePath)
}

fun replaceDir(dir: File) {
    dir.walk()
            .maxDepth(Int.MAX_VALUE)
            .filter { it.isFile }
            .filter { it.extension == "class" }
            .forEach {
                processClass(it, dir)
            }
}

fun replaceJar(jar: File) {
    val zipDir = File(jar.parent, jar.nameWithoutExtension)
    zipDir.deleteRecursively()
    jar.unZipTo(zipDir.absolutePath)
    replaceDir(zipDir)
    jar.delete()
    zipDir.zipTo(jar.absolutePath)
    zipDir.deleteRecursively()
}

fun processClass(classFile: File, dir: File) {
    val ctClass = classPool.makeClass(classFile.inputStream())
    if (ctClass.isFrozen) ctClass.defrost()
    ctClass.instrument(ThreadPoolExprEditor)
    ctClass.writeFile(dir.absolutePath)
    ctClass.freeze()
    ctClass.detach()
}

object ThreadPoolExprEditor : ExprEditor() {
    override fun edit(methodCall: MethodCall) {
        if (!checkEnclosingClass(methodCall)) return
        if (methodCall.className == ExecutorsClassName) {
            "Executors: ${methodCall.enclosingClass.name}:${methodCall.lineNumber} => ${methodCall.className}.${methodCall.methodName}".log(
                    when {
                        executorServiceMethods.contains(methodCall.methodName) -> {
                            methodCall.replace("{ \$_ = $defaultExecutorClass.$defaultExecutor; }")
                            true
                        }
                        scheduledExecutorServiceMethods.contains(methodCall.methodName) -> {
                            methodCall.replace("{ \$_ = $defaultScheduledExecutorClass.$defaultScheduledExecutor; }")
                            true
                        }
                        else -> {
                            false
                        }
                    }
            )
        } else if (
                executorClassNames.contains(methodCall.className)
                && !defaultFilterExecutorMethodPrint.contains(methodCall.methodName)
        ) {
            System.err.println("Method: ${methodCall.enclosingClass.name}:${methodCall.lineNumber} => ${methodCall.className}.${methodCall.methodName}")
        }
    }


    override fun edit(expr: NewExpr) {
        if (!checkEnclosingClass(expr)) return
        if (expr.className == ThreadPoolExecutorClassName) {
            "NewInstance: ${expr.enclosingClass.name}:${expr.lineNumber} => ${expr.className}".log()
            expr.replace("{ \$_ = $defaultExecutorClass.$defaultExecutor; }")
        } else if (expr.className == ScheduledThreadPoolExecutorClassName) {
            "NewInstance: ${expr.enclosingClass.name}:${expr.lineNumber} => ${expr.className}".log()
            expr.replace("{ \$_ = $defaultScheduledExecutorClass.$defaultScheduledExecutor; }")
        }
    }

    private fun checkEnclosingClass(methodCall: Expr?): Boolean {
        return methodCall != null
                && methodCall.enclosingClass.name != defaultExecutorClass
                && methodCall.enclosingClass.name != ExecutorsClassName
                && methodCall.enclosingClass.name != ThreadPoolExecutorClassName
                && methodCall.enclosingClass.name != ScheduledThreadPoolExecutorClassName
                && defaultFilterClassSignaturePrefix.firstOrNull { methodCall.enclosingClass.name.startsWith(it) } == null
    }
}

fun Any.log(debug: Boolean = true) {
    if (threadPoolShrinkOptions.debug && debug) {
        println(toString())
    }
}
