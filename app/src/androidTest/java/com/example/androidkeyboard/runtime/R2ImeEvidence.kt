package com.example.androidkeyboard.runtime

import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject

/** TEST_ONLY. Opt-in snapshots on a disposable emulator, before ActivityScenario.close. */
internal object R2ImeEvidence {
    private const val TAG = "KUYIN_R2_TEST_ONLY"
    val enabled: Boolean
        get() = InstrumentationRegistry.getArguments().getString("r2Diagnostics") == "true"

    fun recordVisibility(stage: String, visible: Boolean) {
        if (!enabled) return
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val observedAt = SystemClock.elapsedRealtime()
        Log.i(TAG, JSONObject().put("event", "VISIBILITY_RESULT")
            .put("stage", stage).put("visible", visible).put("elapsed_ms", observedAt).toString())
        if (visible) return
        // No IMM.isActive/isAcceptingText query: those call checkFocus and change the experiment.
        if (shell("getprop ro.kernel.qemu").trim() != "1") {
            Log.i(TAG, "{\"event\":\"SNAPSHOT_REFUSED_NOT_EMULATOR\"}")
            return
        }
        shell("mkdir -p /data/local/tmp/kuyin-r2")
        val commands = listOf(
            "input-method" to "dumpsys -t 2 input_method",
            "window" to "dumpsys -t 2 window windows",
            "service" to "dumpsys -t 2 activity service io.github.juhjuhx.kuyin/com.example.ime.KuYinInputMethodService",
            "activity" to "dumpsys -t 2 activity activities",
            "user" to "am get-current-user",
        )
        for ((kind, command) in commands) {
            try {
                shell("$command > /data/local/tmp/kuyin-r2/$stage-$kind.txt 2>&1")
                Log.i(TAG, JSONObject().put("event", "LIVE_FAILURE_DUMP")
                    .put("stage", stage).put("kind", kind)
                    .put("observed_false_at_ms", observedAt)
                    .put("dump_completed_at_ms", SystemClock.elapsedRealtime()).toString())
            } catch (error: Exception) {
                Log.e(TAG, JSONObject().put("event", "DUMP_ERROR")
                    .put("stage", stage).put("kind", kind)
                    .put("exception_class", error.javaClass.name).toString())
            }
        }
        shell("screencap -p /data/local/tmp/kuyin-r2/$stage-screen.png")
        // Logs and shell snapshots survive Gradle cleanup; leave JUnit status events unchanged.
    }

    private fun shell(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText() }
    }
}
