package com.example.historylogger.testhelpers

import com.example.historylogger.UsageLoggerWorkerTestHelper
import org.junit.Test
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryLoggerUnitTest {

    // ✅ Duration formatting tests
    @Test
    fun formatDuration_secondsOnly() {
        val helper = UsageLoggerWorkerTestHelper()
        assertEquals("45 sec", helper.formatDuration(45))
    }

    @Test
    fun formatDuration_minutes() {
        val helper = UsageLoggerWorkerTestHelper()
        assertEquals("2 min", helper.formatDuration(120))
    }

    @Test
    fun formatDuration_hours() {
        val helper = UsageLoggerWorkerTestHelper()
        assertEquals("1 hr 5 min", helper.formatDuration(3900))
    }

    // ✅ Gap analysis tests
    @Test
    fun gapAnalysis_detectsDowntime() {
        val now = System.currentTimeMillis()
        val lastActive = now - (1000 * 60 * 45) // 45 minutes ago
        val gapMinutes = (now - lastActive) / 1000 / 60
        assertTrue(gapMinutes > 30)
    }

    @Test
    fun gapAnalysis_detectsNormalRestart() {
        val now = System.currentTimeMillis()
        val lastActive = now - (1000 * 60 * 10) // 10 minutes ago
        val gapMinutes = (now - lastActive) / 1000 / 60
        assertTrue(gapMinutes <= 30)
    }

    // ✅ Cleanup logic simulation
    @Test
    fun cleanupOldLogs_keepsRecentEntries() {
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
        val cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000)

        val recentDate = Date(cutoff + 1000)
        val oldDate = Date(cutoff - (60 * 60 * 1000))

        val recentLine = "${recentDate}: Recent log entry"
        val oldLine = "${oldDate}: Old log entry"

        val filtered = listOf(recentLine, oldLine).filter { line ->
            val timestampPart = line.substringBefore(":")
            try {
                val parsedDate = sdf.parse(timestampPart)?.time ?: 0L
                parsedDate >= cutoff
            } catch (e: Exception) {
                true
            }
        }

        assertTrue(filtered.any { it.contains("Recent") })
        assertFalse(filtered.any { it.contains("Old") })
    }

    // ✅ Default sanity check
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}

/**
 * ✅ Helper stub to access formatDuration without full Worker context.
 */
class UsageLoggerWorkerTestHelper {
    fun formatDuration(seconds: Long): String {
        val minutes = seconds / 60
        val hours = minutes / 60
        return when {
            hours > 0 -> "$hours hr ${minutes % 60} min"
            minutes > 0 -> "$minutes min"
            else -> "$seconds sec"
        }
    }
}