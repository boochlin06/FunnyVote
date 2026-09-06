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
-keep class com.heaton.funnyvote.data.local.entity.** { *; }
-keep class com.heaton.funnyvote.data.local.dao.** { *; }
-dontwarn androidx.room.paging.**

# --- Navigation Compose Type-Safe Routes & ViewModels ---
-keep class com.heaton.funnyvote.ui.navigation.** { *; }
-keep class com.heaton.funnyvote.ui.**UiState { *; }
-keep class com.heaton.funnyvote.ui.**UiEffect { *; }
-keep class com.heaton.funnyvote.ui.**Intent { *; }
-keep class com.heaton.funnyvote.ui.**Contract* { *; }

# --- Remote Models, Repository & Utility Keep ---
-keep class com.heaton.funnyvote.data.remote.** { *; }
-keep class com.heaton.funnyvote.data.repository.** { *; }
-keep class com.heaton.funnyvote.util.** { *; }

# --- Coil ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- WorkManager ---
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
