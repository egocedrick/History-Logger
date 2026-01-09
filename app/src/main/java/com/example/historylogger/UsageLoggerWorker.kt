package com.example.historylogger

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.*

class UsageLoggerWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext

        cleanupOldLogs(context)

        if (!hasUsageAccess(context)) {
            logToFile(context, "WARNING", "Usage Access permission not granted. Cannot log usage stats.")
            return Result.failure()
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 15
        val stats: List<UsageStats> =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        if (stats.isEmpty()) {
            logToFile(context, "INFO", "No usage stats available in last 15 minutes.")
        } else {
            val pm = context.packageManager
            val sessionHeader = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
            logToFile(context, "INFO", "===== Session Log ($sessionHeader) =====")

            for (usage in stats) {
                try {
                    val appName = pm.getApplicationLabel(pm.getApplicationInfo(usage.packageName, 0))
                    val lastUsed = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(usage.lastTimeUsed))
                    val duration = formatDuration(usage.totalTimeInForeground / 1000)

                    val message = "📱 App: $appName | ⏰ Last used: $lastUsed | ⌛ Total time: $duration"
                    logToFile(context, "USAGE", message)
                } catch (e: Exception) {
                    logToFile(context, "USAGE", "App: ${usage.packageName}, (unresolved name)")
                }
            }
        }

        context.filesDir.resolve("last_active.txt").writeText(endTime.toString())
        logToFile(context, "INFO", "Background worker executed successfully.")

        return Result.success()
    }

    private fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun cleanupOldLogs(context: Context) {
        val logFile = context.filesDir.resolve("activity_log.txt")
        if (logFile.exists()) {
            val cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val filteredLogs = logFile.readLines().filter { line ->
                val timestampPart = line.substringBefore("|").trim()
                try {
                    val parsedDate = sdf.parse(timestampPart)?.time ?: 0L
                    parsedDate >= cutoff
                } catch (e: Exception) {
                    true
                }
            }
            logFile.writeText(filteredLogs.joinToString("\n"))
        }
    }

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