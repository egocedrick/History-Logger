package com.example.historylogger

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class LogViewerActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var scrollView: ScrollView
    private var isDarkMode = false  // ✅ Track current mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Layout container
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        // ✅ TextView for logs
        textView = TextView(this).apply {
            textSize = 14f
            setPadding(16, 16, 16, 16)
        }

        // ✅ ScrollView wrapper
        scrollView = ScrollView(this).apply {
            addView(textView)
        }

        // ✅ Refresh button
        val refreshButton = Button(this).apply {
            text = "Refresh Logs"
            setOnClickListener { loadLogs() }
        }

        // ✅ Share button
        val shareButton = Button(this).apply {
            text = "Share Logs"
            setOnClickListener { shareLogs() }
        }

        // ✅ Export button
        val exportButton = Button(this).apply {
            text = "Export Logs"
            setOnClickListener { exportLogs() }
        }

        // ✅ Dark Mode toggle button
        val toggleButton = Button(this).apply {
            text = "Toggle Dark Mode"
            setOnClickListener { toggleDarkMode() }
        }

        // ✅ Add views to layout
        layout.addView(refreshButton)
        layout.addView(shareButton)
        layout.addView(exportButton)
        layout.addView(toggleButton)
        layout.addView(scrollView)

        setContentView(layout)

        // ✅ Initial load
        loadLogs()
    }

    private fun loadLogs() {
        val logFile = File(filesDir, "activity_log.txt")
        if (logFile.exists()) {
            val logs = logFile.readText()
            textView.text = logs

            // ✅ Auto-scroll to bottom (latest log)
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        } else {
            textView.text = "No logs found yet."
        }
    }

    private fun shareLogs() {
        val logFile = File(filesDir, "activity_log.txt")
        if (logFile.exists()) {
            val logs = logFile.readText()
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, logs)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share logs via"))
        } else {
            Toast.makeText(this, "No logs to share.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportLogs() {
        val logFile = File(filesDir, "activity_log.txt")
        if (logFile.exists()) {
            val logs = logFile.readText()
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val exportFile = File(downloadsDir, "activity_log_export.txt")

            try {
                FileOutputStream(exportFile).bufferedWriter().use { writer ->
                    writer.write(logs)
                }
                Toast.makeText(this, "Logs exported to Downloads folder.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "No logs to export.", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Toggle Dark Mode
    private fun toggleDarkMode() {
        if (isDarkMode) {
            // Switch to Light Mode
            scrollView.setBackgroundColor(Color.WHITE)
            textView.setTextColor(Color.BLACK)
            isDarkMode = false
        } else {
            // Switch to Dark Mode
            scrollView.setBackgroundColor(Color.BLACK)
            textView.setTextColor(Color.GREEN) // neon green for readability
            isDarkMode = true
        }
    }
}