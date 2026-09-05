# --- Kotlin Coroutines & Serialization ---
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-dontwarn kotlinx.serialization.**

# --- Google Services & Firebase ---
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# --- Dagger Hilt ---
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class * extends dagger.hilt.internal.ComponentManager
-keep class androidx.hilt.navigation.compose.** { *; }

# --- Room Database ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Coil ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- WorkManager ---
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
