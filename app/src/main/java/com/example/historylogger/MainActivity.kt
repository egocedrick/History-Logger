package com.example.historylogger

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Simple layout with buttons
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val viewLogsButton = Button(this).apply {
            text = "View History Logs"
            setOnClickListener {
                val intent = Intent(this@MainActivity, LogViewerActivity::class.java)
                startActivity(intent)
            }
        }

        val forceLogButton = Button(this).apply {
            text = "Force Log Now"
            setOnClickListener {
                val oneTimeRequest = OneTimeWorkRequestBuilder<UsageLoggerWorker>().build()
                WorkManager.getInstance(this@MainActivity).enqueue(oneTimeRequest)
                logToFile("Manual trigger: Background worker executed immediately.")
            }
        }

        layout.addView(viewLogsButton)
        layout.addView(forceLogButton)
        setContentView(layout)

        // ✅ Check Usage Access permission
        if (!hasUsageAccess()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            logToFile("Usage Access permission not granted. Prompting user to enable.")
            return
        }

        // ✅ Load last active timestamp
        val lastActiveFile = File(filesDir, "last_active.txt")
        val now = System.currentTimeMillis()
        var lastActive: Long? = null
        if (lastActiveFile.exists()) {
            lastActive = lastActiveFile.readText().toLongOrNull()
        }

        // ✅ Compare gap
        if (lastActive != null) {
            val gapMinutes = (now - lastActive) / 1000 / 60
            if (gapMinutes > 30) { // arbitrary threshold
                logToFile("⚠ Possible Safe Mode or downtime detected. Gap = $gapMinutes minutes")
            } else {
                logToFile("Normal restart/resume. Gap = $gapMinutes minutes")
            }
        } else {
            logToFile("First run, no previous timestamp recorded.")
        }

        // ✅ Save current timestamp
        lastActiveFile.writeText(now.toString())

        // ✅ Log app usage for last hour
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 60 // last 1 hour
        val stats: List<UsageStats> =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        if (stats.isEmpty()) {
            logToFile("No usage stats available. Conductor may not have granted access.")
        } else {
            for (usage in stats) {
                val message = "App: ${usage.packageName}, " +
                        "Last used: ${Date(usage.lastTimeUsed)}, " +
                        "Total time: ${usage.totalTimeInForeground / 1000}s"
                logToFile(message)
            }
        }

        // ✅ Schedule periodic background logging every 15 minutes
        val workRequest = PeriodicWorkRequestBuilder<UsageLoggerWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueue(workRequest)
        logToFile("Background worker scheduled to log every 15 minutes.")
    }

    // ✅ Helper: Check if Usage Access is granted
    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // ✅ Helper: Append logs to file
    private fun logToFile(message: String) {
        Log.d("HistoryLogger", message)
        val logFile = File(filesDir, "activity_log.txt")
        FileOutputStream(logFile, true).bufferedWriter().use { writer ->
            writer.appendLine("${Date()}: $message")
        }
    }
}