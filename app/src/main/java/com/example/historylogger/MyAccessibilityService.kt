package com.example.historylogger

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.text?.any { it.contains("Incorrect") || it.contains("PIN") || it.contains("Password") } == true) {
                val attempts = incrementSecurityAttempts(applicationContext)
                logToFile(applicationContext, "SECURITY", "Failed unlock attempt #$attempts")
            }
        }
    }

    override fun onInterrupt() {}
}