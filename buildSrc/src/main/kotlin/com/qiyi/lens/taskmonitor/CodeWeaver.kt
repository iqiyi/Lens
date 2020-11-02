package com.qiyi.lens.taskmonitor

import org.objectweb.asm.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

private const val ANNOTATION = "Lcom/qiyi/lens/dump/LensMonitor;"
private const val METHOD_CLASS = "com/qiyi/lens/utils/TimeStampUtil"
private const val METHOD_START_NAME = "startStamp"
private const val METHOD_STOP_NAME = "stopStamp"
private const val METHOD_DESC = "(Ljava/lang/String;)V"

private const val SYSTRACE_ANNOTATION = "Lcom/iqiyi/monitor/LensSysTrace;"
private const val METHOD_TRACE_ANNOTATION = "Lcom/iqiyi/monitor/LensMethodTrace;"
private const val METHOD_TRACE_TIMEGAP_ANNOTAIION = "Lcom/qiyi/lens/dump/LensTimeGap;"

private fun InputStream.transform(): ByteArray {
    val classReader = ClassReader(this)
    val classWriter = ClassWriter(classReader, 0)
    val classVisitor = object : ClassVisitor(Opcodes.ASM5, classWriter) {
        var className: String? = null
        var simpleClassName: String? = null

        override fun visit(
                version: Int,
                access: Int,
                name: String?,
                signature: String?,
                superName: String?,
                interfaces: Array<out String>?
        ) {
            className = name
            simpleClassName = name
            val index: Int? = simpleClassName?.lastIndexOf('/')
            if (index != null) {
                simpleClassName = simpleClassName?.substring(index + 1)
            }
            super.visit(version, access, name, signature, superName, interfaces)
        }

        override fun visitMethod(
                access: Int,
                name: String?,
                desc: String?,
                signature: String?,
                exceptions: Array<out String>?
        ): MethodVisitor {
            val fullMethodName = "$className#$name"
            val result = super.visitMethod(access, name, desc, signature, exceptions)
            return object : MethodVisitor(Opcodes.ASM5, result) {
                var monitor = false
                var sysTrace = false
                var methodTrace = false
                var timeGap = false
                var gapTag = "LensTime"
                var gapMsg = ""
                override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {
                    monitor = monitor || desc == ANNOTATION
                    sysTrace = sysTrace || desc == SYSTRACE_ANNOTATION
                    methodTrace = methodTrace || desc == METHOD_TRACE_ANNOTATION
                    timeGap = timeGap || desc == METHOD_TRACE_TIMEGAP_ANNOTAIION
                    if(className.equals("com.qiyi.lens.demo.MainActivity")) {
                        print("on find time gap!!!")
                    }

                    if (timeGap) {
                        return object : AnnotationVisitor(Opcodes.ASM5, super.visitAnnotation(desc, visible)) {

                            override fun visit(name: String?, value: Any?) {

                                av?.visit(name,value)

                                if ("tag" == name) {
                                    gapTag = value as String? ?: "LensTime"
                                } else if("msg"== name){
                                    gapMsg = value as String? ?: ""
                                }
                            }
                        };
                    }
                    return super.visitAnnotation(desc, visible)
                }

                override fun visitCode() {
                    if (monitor) {
                        visitLdcInsn(fullMethodName)
                        visitMethodInsn(Opcodes.INVOKESTATIC, METHOD_CLASS, METHOD_START_NAME, METHOD_DESC, false)
                    }

                    if (methodTrace) {
                        visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
                        visitInsn(Opcodes.DUP)
                        visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
                        visitLdcInsn("$simpleClassName-$name-trace-")
                        visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
                        visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
                        visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false)
                        visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
                        visitMethodInsn(Opcodes.INVOKESTATIC, "android/os/Debug", "startMethodTracing", "(Ljava/lang/String;)V", false)
                    } else if (sysTrace) {
                        visitLdcInsn("$simpleClassName#$name")
                        visitMethodInsn(Opcodes.INVOKESTATIC, "android/os/Trace", "beginSection", "(Ljava/lang/String;)V", false)
                    }

                    if (timeGap) {
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/qiyi/lens/utils/LensTimeGapUtil", "onMethodIn", "()V", false);
                    }
                    super.visitCode()
                }

                override fun visitInsn(opcode: Int) {
                    when (opcode) {
                        Opcodes.IRETURN, Opcodes.FRETURN, Opcodes.ARETURN, Opcodes.LRETURN, Opcodes.DRETURN, Opcodes.RETURN -> {
                            if (monitor) {
                                visitLdcInsn(fullMethodName)
                                visitMethodInsn(Opcodes.INVOKESTATIC, METHOD_CLASS, METHOD_STOP_NAME, METHOD_DESC, false)
                            }

                            if (methodTrace) {
                                visitMethodInsn(Opcodes.INVOKESTATIC, "android/os/Debug", "stopMethodTracing", "()V", false)
                            } else if (sysTrace) {
                                visitMethodInsn(Opcodes.INVOKESTATIC, "android/os/Trace", "endSection", "()V", false)
                            }

                            if (timeGap) {
                                mv.visitLdcInsn(gapTag);
                                mv.visitLdcInsn(fullMethodName+" "+gapMsg);
                                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/qiyi/lens/utils/LensTimeGapUtil", "onMethodExit", "(Ljava/lang/String;Ljava/lang/String;)V", false);
                            }
                        }
                    }
                    super.visitInsn(opcode)
                }
            }

        }
    }
    classReader.accept(classVisitor, 0)
    return classWriter.toByteArray()
}

fun weaveCode(input: File, output: File = input) {
    if (input.isClass()) {
        output.writeBytes(input.inputStream().use { it.transform() })
    } else if (input.isJar()) {
        val inputZip = ZipFile(input)
        val tempFile = File("${input.absolutePath}.temp")
        ZipOutputStream(FileOutputStream(tempFile)).use { outStream ->
            val inputEntries = inputZip.entries()
            while (inputEntries.hasMoreElements()) {
                val inputEntry = inputEntries.nextElement()
                val bytes = inputEntry.name.endsWith(".class").select({
                    inputZip.getInputStream(inputEntry).use { it.transform() }
                }, {
                    inputZip.getInputStream(inputEntry).use { it.readBytes() }
                })
                val writeEntry = ZipEntry(inputEntry.name)
                outStream.putNextEntry(writeEntry)
                outStream.write(bytes)
                outStream.flush()
                outStream.closeEntry()
            }
        }
        tempFile.renameTo(output)
    }
}





