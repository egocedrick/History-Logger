package com.example.historylogger

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val now = System.currentTimeMillis()
            val lastActiveFile = context.filesDir.resolve("last_active.txt")
            val lastActive = if (lastActiveFile.exists()) lastActiveFile.readText().toLongOrNull() else null

            // Safe Mode detection using ActivityManager
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val safeModeMethod = ActivityManager::class.java.getMethod("isRunningInSafeMode")
            val isSafeMode = safeModeMethod.invoke(am) as Boolean

            if (isSafeMode) {
                logToFile(context, "SAFE_MODE", "⚠ Device booted into Safe Mode")
            } else {
                logToFile(context, "INFO", "Device booted normally (not Safe Mode)")
            }

            // Gap analysis
            if (lastActive != null) {
                val gapMinutes = (now - lastActive) / 1000 / 60
                if (gapMinutes > 30) {
                    logToFile(context, "SAFE_MODE", "⚠ Downtime detected at boot. Gap = $gapMinutes minutes")
                } else {
                    logToFile(context, "INFO", "Normal boot. Gap = $gapMinutes minutes")
                }
            } else {
                logToFile(context, "INFO", "Boot completed. No previous timestamp recorded.")
            }

            // Save current timestamp
            lastActiveFile.writeText(now.toString())
        }
    }
}