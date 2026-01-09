package com.example.historylogger

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LogAdapter(private val logs: List<LogEntry>, private val isDarkMode: Boolean) :
    RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    class LogViewHolder(val textView: android.widget.TextView) :
        RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val textView = android.widget.TextView(parent.context).apply {
            textSize = 14f
            setPadding(24, 16, 24, 16)
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return LogViewHolder(textView)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        holder.textView.text = "${log.timestamp} | [${log.type}] ${log.message}"

        // Background by theme
        holder.textView.setBackgroundColor(if (isDarkMode) Color.BLACK else Color.WHITE)

        // Color coding by type
        when (log.type.uppercase()) {
            "SAFE_MODE" -> holder.textView.setTextColor(Color.RED)
            "SECURITY" -> holder.textView.setTextColor(Color.parseColor("#FF4500")) // orange-red
            else -> holder.textView.setTextColor(if (isDarkMode) Color.GREEN else Color.BLACK)
        }
    }

    override fun getItemCount() = logs.size
}

class LogViewerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var isDarkMode = false
    private var logs: MutableList<LogEntry> = mutableListOf()
    private lateinit var adapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root layout
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Buttons
        val refreshButton = Button(this).apply {
            text = "Refresh Logs"
            setOnClickListener { loadLogsAndRefreshUI() }
        }

        val toggleDarkButton = Button(this).apply {
            text = "Toggle Dark Mode"
            setOnClickListener {
                isDarkMode = !isDarkMode
                applyThemeColors(layout)
                adapter = LogAdapter(logs, isDarkMode)
                recyclerView.adapter = adapter
            }
        }

        // RecyclerView
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@LogViewerActivity)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        // Assemble layout
        layout.addView(refreshButton)
        layout.addView(toggleDarkButton)
        layout.addView(recyclerView)
        setContentView(layout)

        // Initial
        applyThemeColors(layout)
        loadLogsAndRefreshUI()
    }

    private fun applyThemeColors(root: LinearLayout) {
        root.setBackgroundColor(if (isDarkMode) Color.BLACK else Color.WHITE)
    }

    private fun loadLogsAndRefreshUI() {
        val allowed = setOf("SAFE_MODE", "SECURITY")
        logs = readStructuredLogs(this, allowed).toMutableList()
        adapter = LogAdapter(logs, isDarkMode)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition((logs.size - 1).coerceAtLeast(0))
    }
}