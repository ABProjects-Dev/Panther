package com.panther.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchShowsPantherBrandAndFoundationMessage() {
        val appName = composeRule.activity.getString(R.string.app_name)
        val message = composeRule.activity.getString(R.string.foundation_message)
        composeRule.onNodeWithText(appName).assertIsDisplayed()
        composeRule.onNodeWithText(message).assertIsDisplayed()
    }

    @Test
    fun bottomNavigationSwitchesTabsAndReturnsToRootFromNestedSettings() {
        val programsTab = composeRule.activity.getString(R.string.programs)
        val newProgram = composeRule.activity.getString(R.string.new_program)
        val profileTab = composeRule.activity.getString(R.string.profile)
        val settingsLabel = composeRule.activity.getString(R.string.settings)
        val backLabel = composeRule.activity.getString(R.string.back)
        val themeSection = composeRule.activity.getString(R.string.theme)
        val nameLabel = composeRule.activity.getString(R.string.name)

        composeRule.onNodeWithText(programsTab).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(newProgram).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(newProgram).assertIsDisplayed()

        composeRule.onNodeWithText(profileTab).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(settingsLabel).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(settingsLabel).performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(themeSection).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(themeSection).performScrollTo().assertIsDisplayed()

        // Tapping the active Profile tab on the bottom bar pops Settings back to ProfileScreen.
        composeRule.onNodeWithText(profileTab).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(nameLabel).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(backLabel).assertDoesNotExist()
        composeRule.onNodeWithText(nameLabel).performScrollTo().assertIsDisplayed()
    }
}
