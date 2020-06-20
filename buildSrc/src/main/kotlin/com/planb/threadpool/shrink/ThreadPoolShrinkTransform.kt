package com.planb.threadpool.shrink

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import javassist.ClassPool
import org.gradle.api.Project
import java.io.File


/**
 * v1.0 of the file created on 2020/6/20 by shuxin.wei
 *
 */
class ThreadPoolShrinkTransform(
    private var project: Project,
    private var options: ThreadPoolShrinkOptions
) : Transform() {
    override fun getName(): String = "threadPoolShrinkTransform"
    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun isIncremental() = true

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        if (transformInvocation == null) {
            return
        }
        val outputProvider = transformInvocation.outputProvider

        if (!options.enabled) {
            transformInvocation.inputs.forEach {
                it.directoryInputs.forEach { directoryInput ->
                    FileUtils.copyDirectory(
                        directoryInput.file, outputProvider.getContentLocation(
                            directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY
                        )
                    )
                }

                it.jarInputs.forEach { jarInput ->
                    FileUtils.copyFile(
                        jarInput.file, outputProvider.getContentLocation(
                            jarInput.file.absolutePath,
                            jarInput.contentTypes,
                            jarInput.scopes,
                            Format.JAR
                        )
                    )
                }
            }
            return
        }

        if (options.debug) {
            println("---------------${ThreadPoolShrinkTransform::class.java.simpleName}${if (transformInvocation.isIncremental) "incremental" else ""} Begin---------------")
        }

        loadClassPath(project, options)
        transformInvocation.inputs.forEach {
            it.directoryInputs.forEach {
                loadClassPath(it.file)
            }
            it.jarInputs.forEach {
                loadClassPath(it.file)
            }
        }
        transformInvocation.inputs.forEach {
            it.directoryInputs.forEach { directoryInput ->
                val dest: File = outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes, directoryInput.scopes,
                    Format.DIRECTORY
                )
                FileUtils.copyDirectory(directoryInput.file, dest)
                replaceDir(dest)
            }

            it.jarInputs.forEach { jarInput ->
                val dest = outputProvider.getContentLocation(
                    jarInput.file.absolutePath,
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )
                FileUtils.copyFile(jarInput.file, dest)
                replaceJar(dest)
            }
        }

        ClassPool.getDefault().clearImportedPackages()
        if (options.debug) {
            println("---------------${ThreadPoolShrinkTransform::class.java.simpleName} End---------------")
        }
    }


}