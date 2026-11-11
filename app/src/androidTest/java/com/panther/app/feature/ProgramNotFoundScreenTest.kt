package com.panther.app.feature

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.panther.app.R
import com.panther.app.core.designsystem.theme.PantherTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The terminal screen rendered when `editor/{id}` points at a program that does not exist. */
@RunWith(AndroidJUnit4::class)
class ProgramNotFoundScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun showsTheMessageAndNavigatesBack() {
        var backPressed = false
        composeRule.setContent {
            PantherTheme {
                ProgramNotFoundScreen(onBack = { backPressed = true })
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.program_not_found)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.program_not_found_hint)).assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.back)).performClick()

        assertTrue("the back action must be wired to the navigation callback", backPressed)
    }
}
