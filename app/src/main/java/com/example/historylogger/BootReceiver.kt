package com.example.historylogger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.io.File
import java.io.FileOutputStream
import java.util.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val now = System.currentTimeMillis()

            // Load last active timestamp
            val lastActiveFile = File(context.filesDir, "last_active.txt")
            var lastActive: Long? = null
            if (lastActiveFile.exists()) {
                lastActive = lastActiveFile.readText().toLongOrNull()
            }

            // Compare gap
            if (lastActive != null) {
                val gapMinutes = (now - lastActive) / 1000 / 60
                val message = if (gapMinutes > 30) {
                    "⚠ Possible Safe Mode or downtime detected at boot. Gap = $gapMinutes minutes"
                } else {
                    "Device restarted normally at boot. Gap = $gapMinutes minutes"
                }
                logToFile(context, message)
            } else {
                logToFile(context, "Boot completed. No previous timestamp recorded.")
            }

            // Save current timestamp
            lastActiveFile.writeText(now.toString())
        }
    }

    private fun logToFile(context: Context, message: String) {
        val logFile = File(context.filesDir, "activity_log.txt")
        FileOutputStream(logFile, true).bufferedWriter().use { writer ->
            writer.appendLine("${Date()}: $message")
        }
    }
}