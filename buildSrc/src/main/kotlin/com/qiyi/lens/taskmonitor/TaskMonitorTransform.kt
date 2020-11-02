package com.qiyi.lens.taskmonitor

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import java.io.File

class TaskMonitorTransform : Transform() {
    override fun getName(): String = "LensTaskMonitor"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun isIncremental(): Boolean = true

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = TransformManager.SCOPE_FULL_PROJECT

    private val executor: WaitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()

    override fun transform(ti: TransformInvocation?) {
        if (ti?.isIncremental == false) {
            ti.outputProvider.deleteAll()
        }
        ti?.inputs?.forEach { input ->
            input.directoryInputs.forEach { dirInput ->
                processDirInput(ti, dirInput)
            }
            input.jarInputs.forEach { jarInput ->
                processJarInput(ti, jarInput)
            }
        }
        executor.waitForTasksWithQuickFail<Any>(false)
    }

    private fun processDirInput(ti: TransformInvocation, dirInput: DirectoryInput) {
        val out = getOutputLocation(ti, dirInput, Format.DIRECTORY)
        if (ti.isIncremental) {
            val fromDir = dirInput.file.absolutePath
            val toDir = out.absolutePath
            dirInput.changedFiles.forEach { (file, status) ->
                val destFile = File(file.absolutePath.replace(fromDir, toDir))
                if (status == Status.ADDED || status == Status.CHANGED) {
                    println("[$name][DIR:$status] $file, $destFile")
                    weaveCode(file, destFile)
                } else if (status == Status.REMOVED) {
                    println("[$name][DIR:$status] $file, $destFile")
                    destFile.delete()
                }
            }
        } else {
            executor.execute {
                println("[$name][DIR] ${dirInput.file}, $out")
                dirInput.file.copyRecursively(out, true)
                out.walk().forEach { file ->
                    if (file.isClass()) {
                        weaveCode(file)
                    }
                }
            }
        }
    }

    private fun processJarInput(ti: TransformInvocation, jarInput: JarInput) {
        val out = getOutputLocation(ti, jarInput, Format.JAR)
        if (ti.isIncremental) {
            if (jarInput.status == Status.ADDED || jarInput.status == Status.CHANGED) {
                weaveJar(jarInput, out, true)
            } else if (jarInput.status == Status.REMOVED) {
                println("[$name][JAR:${jarInput.status}] ${jarInput.file}, $out")
                out.delete()
            }
        } else {
            weaveJar(jarInput, out, false)
        }
    }

    private fun weaveJar(jarInput: JarInput, out: File, isIncremental: Boolean) {
        executor.execute {
            val msg = if (isIncremental) ":${jarInput.status}" else ""
            println("[$name][JAR$msg] ${jarInput.file}, $out")
            jarInput.file.copyTo(out, true)
            weaveCode(out)
        }
    }

    private fun getOutputLocation(ti: TransformInvocation, input: QualifiedContent, format: Format): File =
            ti.outputProvider.getContentLocation(input.file.toString(), input.contentTypes, input.scopes, format)
}