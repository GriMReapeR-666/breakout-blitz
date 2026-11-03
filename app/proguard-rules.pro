# ProGuard & R8 Optimization Rules for Setup Candy Google Play Release

# 1. Jetpack Compose Rules
-keepattributes *Annotation*
-keepclassmembers class androidx.compose.** { *; }

# 2. Native Bridge JavaScriptInterface preservation
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.setuprush.candy.NativeBridge { *; }

# 3. Preserve Engine Data Models, Enums & Catalogs for reflection and serialization
-keep class com.setuprush.candy.engine.** { *; }
-keepclassmembers enum com.setuprush.candy.engine.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 4. Preserve UI Theme and State Classes
-keep class com.setuprush.candy.theme.** { *; }
-keep class com.setuprush.candy.ui.** { *; }

# 5. Coroutines & Kotlin Metadata
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# 6. Optimization Flags
-repackageclasses
-allowaccessmodification
