package com.example.historylogger

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogViewerActivityTest {

    @Test
    fun testRefreshButton_isVisibleAndClickable() {
        ActivityScenario.launch(LogViewerActivity::class.java)
        onView(withText("Refresh Logs")).check(matches(isDisplayed()))
        onView(withText("Refresh Logs")).perform(click())
    }

    @Test
    fun testShareButton_isVisibleAndClickable() {
        ActivityScenario.launch(LogViewerActivity::class.java)
        onView(withText("Share Logs")).check(matches(isDisplayed()))
        onView(withText("Share Logs")).perform(click())
    }

    @Test
    fun testExportButton_isVisibleAndClickable() {
        ActivityScenario.launch(LogViewerActivity::class.java)
        onView(withText("Export Logs")).check(matches(isDisplayed()))
        onView(withText("Export Logs")).perform(click())
    }

    @Test
    fun testToggleDarkMode_isVisibleAndClickable() {
        ActivityScenario.launch(LogViewerActivity::class.java)
        onView(withText("Toggle Dark Mode")).check(matches(isDisplayed()))
        onView(withText("Toggle Dark Mode")).perform(click())
    }
}