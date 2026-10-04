package com.example.ime

import android.os.Looper
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class IdleAInitializeTest {
    @get:Rule val rule = createComposeRule()
    @Test fun initialize() {
        rule.setContent { Text("初始化") }
        rule.onNodeWithText("初始化").assertExists()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class IdleBWriteTest {
    @Test fun writeWithoutComposeRule() {
        val state = mutableStateOf(false)
        state.value = true
        shadowOf(Looper.getMainLooper()).idle()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class IdleCDisplayTest {
    @get:Rule val rule = createComposeRule()
    @Test fun displayAfterWrite() {
        try {
            rule.setContent { Text("後續") }
            rule.onNodeWithText("後續").assertExists()
        } catch (failure: RuntimeException) {
            printIdleDiagnostic(rule)
            throw failure
        }
    }
}
