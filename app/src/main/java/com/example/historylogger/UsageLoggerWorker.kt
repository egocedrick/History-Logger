package com.example.historylogger

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class UsageLoggerWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext

        // ✅ Cleanup old logs before writing new ones
        cleanupOldLogs(context)

        // ✅ Check Usage Access permission
        if (!hasUsageAccess(context)) {
            logToFile(context, "⚠ Usage Access permission not granted. Cannot log usage stats.")
            return Result.failure()
        }

        // ✅ Log app usage for last 15 minutes
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 15 // last 15 minutes
        val stats: List<UsageStats> =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        if (stats.isEmpty()) {
            logToFile(context, "ℹ No usage stats available in last 15 minutes.")
        } else {
            val pm = context.packageManager
            val sessionHeader = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
            logToFile(context, "===== Session Log ($sessionHeader) =====")

            for (usage in stats) {
                try {
                    val appName = pm.getApplicationLabel(pm.getApplicationInfo(usage.packageName, 0))
                    val lastUsed = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(usage.lastTimeUsed))
                    val duration = formatDuration(usage.totalTimeInForeground / 1000)

                    val message = """
📱 App: $appName
⏰ Last used: $lastUsed
⌛ Total time: $duration
-----------------------------
""".trimIndent()

                    logToFile(context, message)
                } catch (e: Exception) {
                    logToFile(context, "App: ${usage.packageName}, (unresolved name)")
                }
            }
        }

        // ✅ Update last_active timestamp for BootReceiver gap analysis
        val lastActiveFile = File(context.filesDir, "last_active.txt")
        lastActiveFile.writeText(endTime.toString())

        logToFile(context, "✅ Background worker executed successfully.")

        return Result.success()
    }

    // ✅ Helper: Check if Usage Access is granted
    private fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // ✅ Helper: Append logs to file
    private fun logToFile(context: Context, message: String) {
        val logFile = File(context.filesDir, "activity_log.txt")
        FileOutputStream(logFile, true).bufferedWriter().use { writer ->
            writer.appendLine("${Date()}: $message")
        }
    }

    // ✅ Helper: Cleanup logs older than 24 hours
    private fun cleanupOldLogs(context: Context) {
        val logFile = File(context.filesDir, "activity_log.txt")
        if (logFile.exists()) {
            val cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 hours in ms
            val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())

            val filteredLogs = logFile.readLines().filter { line ->
                val timestampPart = line.substringBefore(":")
                try {
                    val parsedDate = sdf.parse(timestampPart)?.time ?: 0L
                    parsedDate >= cutoff
                } catch (e: Exception) {
                    true // keep line if parsing fails
                }
            }
            logFile.writeText(filteredLogs.joinToString("\n"))
        }
    }

    // ✅ Helper: Format duration into hr/min/sec
    private fun formatDuration(seconds: Long): String {
        val minutes = seconds / 60
        val hours = minutes / 60
        return when {
            hours > 0 -> "$hours hr ${minutes % 60} min"
            minutes > 0 -> "$minutes min"
            else -> "$seconds sec"
        }
    }
}