package com.qiyi.lens.taskmonitor

import java.io.File
import java.security.MessageDigest

fun String.md5() = MessageDigest.getInstance("MD5").digest(toByteArray()).toHex()

fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }

fun File.isClass() = name.endsWith(".class")

fun File.isJar() = name.endsWith(".jar")

fun <T> Boolean.select(onTrue: () -> T, onFalse: () -> T): T =
        if (this) onTrue.invoke() else onFalse.invoke()
