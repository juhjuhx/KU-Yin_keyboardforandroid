# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# ===================================================================
# KuYin Keyboard ProGuard & R8 Security and Obfuscation Rules
# ===================================================================

# 1. 程式碼混淆強化與最佳化 (Security & Aggressive Obfuscation)
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''
-overloadaggressively

# 2. 隱藏原始檔案名與保護行號 (Anti-Reverse Engineering)
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# 3. 移除除錯與敏感 Log 輸出 (Security & Data Leak Prevention)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# 4. 保留 Android 核心元件 (Activities, InputMethodService, Application)
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.inputmethodservice.InputMethodService

# 5. 保留開放架構介面 (KuYin Open Architecture API & Sync)
-keep public interface com.example.ime.api.** { *; }
-keep class com.example.ime.api.** { *; }
-keep class com.example.ime.sync.** { *; }

# 6. 保留 Jetpack Compose 與 Kotlin 運行時註解
-keep class androidx.compose.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# 7. 保留 Room 資料庫實體與 DAO
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# 8. 忽略非關鍵第三方反射警告
-dontwarn javax.annotation.**
-dontwarn kotlin.reflect.**
