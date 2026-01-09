package com.example.historylogger

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class LogEntry(val timestamp: String, val type: String, val message: String)

private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

fun logToFile(context: Context, type: String, message: String) {
    val logFile = File(context.filesDir, "activity_log.txt")
    val timestamp = dateFormat.format(Date())
    val entry = "$timestamp | [$type] $message"
    try {
        FileOutputStream(logFile, true).bufferedWriter().use { it.appendLine(entry) }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun readStructuredLogs(context: Context, allowedTypes: Set<String>? = null): List<LogEntry> {
    val logFile = File(context.filesDir, "activity_log.txt")
    if (!logFile.exists()) return emptyList()

    val list = mutableListOf<LogEntry>()
    logFile.forEachLine { line ->
        val parts = line.split("|")
        if (parts.size >= 2) {
            val timestamp = parts[0].trim()
            val typeAndMessage = parts[1].trim()
            val type = typeAndMessage.substringAfter("[", "").substringBefore("]", "").ifEmpty { "INFO" }
            val message = typeAndMessage.substringAfter("]", "").trim().ifEmpty { typeAndMessage }

            if (allowedTypes == null || allowedTypes.contains(type.uppercase())) {
                list.add(LogEntry(timestamp, type, message))
            }
        }
    }
    return list
}

fun incrementSecurityAttempts(context: Context): Int {
    val file = File(context.filesDir, "security_attempts.txt")
    val current = file.takeIf { it.exists() }?.readText()?.toIntOrNull() ?: 0
    val next = current + 1
    file.writeText(next.toString())
    return next
}