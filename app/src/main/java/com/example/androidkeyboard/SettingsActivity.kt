package com.example.androidkeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.androidkeyboard.engines.core.IMEConfig

/** Settings surface for features that are implemented in the current IME build. */
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val ctx = requireContext()
            val cfg = IMEConfig(ctx)
            val screen = preferenceManager.createPreferenceScreen(ctx)

            screen.addPreference(Preference(ctx).apply {
                title = "簡繁轉換"
                summary = "尚未提供：目前版本未包含 OpenCC 後端，文字會原樣輸出"
                isSelectable = false
            })

            screen.addPreference(SwitchPreferenceCompat(ctx).apply {
                key = IMEConfig.KEY_HAPTIC_ENABLED
                title = "按鍵震動回饋"
                setDefaultValue(true)
            })

            screen.addPreference(ListPreference(ctx).apply {
                key = IMEConfig.KEY_PROXIMITY_TOLERANCE
                title = "鄰鍵容錯"
                entries = arrayOf("嚴格 0.05", "預設 0.15", "寬鬆 0.30")
                entryValues = arrayOf("0.05", "0.15", "0.30")
                value = cfg.proximityTolerance.toString()
                setDefaultValue("0.15")
                summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            })

            screen.addPreference(Preference(ctx).apply {
                title = "啟用輸入法"
                summary = "前往系統設置，勾選 KU-Yin (Chewing)"
                setOnPreferenceClickListener {
                    runCatching { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
                    true
                }
            })

            screen.addPreference(Preference(ctx).apply {
                title = "關於"
                summary = buildString {
                    append("KU-Yin 注音輸入法")
                    runCatching {
                        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
                    }.getOrNull()?.let { append(" v").append(it) }
                }
                isSelectable = false
            })

            preferenceScreen = screen
        }
    }
}
