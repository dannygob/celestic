# ProGuard / R8 Rules for Celestic Quality Inspection System

# ── General Configuration ───────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ── Celestic Domain and Data Models (JSON / GSON Serialization) ─────────────
-keepclassmembers enum * { *; }
-keep class com.example.celestic.models.** { *; }
-keepclassmembers class com.example.celestic.models.** { <fields>; }

# ── Room Database ───────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ── OpenCV Native Bindings ──────────────────────────────────────────────────
-keep class org.opencv.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# ── LiteRT / TensorFlow Lite ────────────────────────────────────────────────
-keep class com.google.ai.edge.litert.** { *; }
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**
-dontwarn com.google.ai.edge.litert.**

# ── Document & Report Generation (iText & POI) ──────────────────────────────
-dontwarn com.itextpdf.**
-keep class com.itextpdf.** { *; }
-dontwarn org.apache.poi.**
-keep class org.apache.poi.** { *; }

# ── MPAndroidChart ──────────────────────────────────────────────────────────
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# ── Hilt / Dagger ───────────────────────────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
