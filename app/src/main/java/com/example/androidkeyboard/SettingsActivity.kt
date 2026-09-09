package com.example.androidkeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.androidkeyboard.engines.core.ChewingEngine.Layout
import com.example.androidkeyboard.engines.core.ChineseConverter.Profile
import com.example.androidkeyboard.engines.core.IMEConfig

/**
 * Real settings UI. All keys match [IMEConfig] storage keys exactly so the
 * IME service picks up changes without any migration layer:
 * layout / full_half / s2t_profile / t2s_profile / conversion_enabled /
 * haptic_enabled / proximity_tolerance / user_dict_path.
 */
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

            screen.addPreference(ListPreference(ctx).apply {
                key = "layout"
                title = "注音鍵盤佈局"
                entries = arrayOf("大千 DaChen（預設）", "許氏 Hsu", "倚天26 Eten26")
                entryValues = arrayOf(Layout.DACHEN.name, Layout.HSU.name, Layout.Eten26.name)
                setDefaultValue(Layout.DACHEN.name)
                summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            })

            screen.addPreference(SwitchPreferenceCompat(ctx).apply {
                key = "conversion_enabled"
                title = "簡繁轉換"
                summary = "候選字上屏時自動轉換"
                setDefaultValue(true)
            })

            screen.addPreference(ListPreference(ctx).apply {
                key = "s2t_profile"
                title = "簡轉繁配置"
                entries = arrayOf("台灣正體 s2tw（預設）", "通用繁體 s2t")
                entryValues = arrayOf(Profile.S2TW.name, Profile.S2T.name)
                setDefaultValue(Profile.S2TW.name)
                summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                dependency = "conversion_enabled"
            })

            screen.addPreference(ListPreference(ctx).apply {
                key = "t2s_profile"
                title = "繁轉簡配置"
                entries = arrayOf("簡體 tw2s（預設）", "通用簡體 t2s")
                entryValues = arrayOf(Profile.TW2S.name, Profile.T2S.name)
                setDefaultValue(Profile.TW2S.name)
                summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                dependency = "conversion_enabled"
            })

            screen.addPreference(SwitchPreferenceCompat(ctx).apply {
                key = "haptic_enabled"
                title = "按鍵震動回饋"
                setDefaultValue(true)
            })

            screen.addPreference(SwitchPreferenceCompat(ctx).apply {
                key = "full_half"
                title = "全形標點"
                summary = "使用全形標點符號"
                setDefaultValue(false)
            })

            screen.addPreference(ListPreference(ctx).apply {
                key = "proximity_tolerance"
                title = "鄰鍵容錯"
                entries = arrayOf("嚴格 0.05", "預設 0.15", "寬鬆 0.30")
                entryValues = arrayOf("0.05", "0.15", "0.30")
                value = cfg.proximityTolerance.toString()
                summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                setOnPreferenceChangeListener { _, newValue ->
                    cfg.proximityTolerance =
                        (newValue as String).toFloatOrNull()?.coerceIn(0f, 0.5f)
                            ?: cfg.proximityTolerance
                    true
                }
            })

            screen.addPreference(EditTextPreference(ctx).apply {
                key = "user_dict_path"
                title = "使用者詞庫路徑"
                summary = "留空使用預設位置"
                setDefaultValue("")
            })

            screen.addPreference(Preference(ctx).apply {
                title = "啟用輸入法"
                summary = "前往系統設置，勾選 KU-Yin (Chewing)"
                setOnPreferenceClickListener {
                    try {
                        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    } catch (e: Exception) {
                        // No-op: device without the settings entry.
                    }
                    true
                }
            })

            screen.addPreference(Preference(ctx).apply {
                title = "關於"
                summary = buildString {
                    append("KU-Yin 注音輸入法")
                    try {
                        val info = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
                        append(" v").append(info.versionName)
                    } catch (e: Exception) {
                        // Keep static text when lookup fails.
                    }
                }
                isSelectable = false
            })

            preferenceScreen = screen
        }
    }
}
