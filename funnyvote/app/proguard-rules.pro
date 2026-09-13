# ====================================================================
# FunnyVote OWASP MASVS Production ProGuard / R8 Hardening Rules
# ====================================================================

# 1. [MASVS-STORAGE-2 / MASVS-CODE-2] 徹底在 Release 中剝離所有 Log 輸出，防止除錯輸出敏感資訊
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int println(...);
}

# 2. [MASVS-CODE-2] 移除源碼檔案名與行號資訊（防逆向分析）
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# 3. Kotlinx Serialization & Room Data Models Keep Rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# 4. Hilt / Dagger DI 保留規則
-keep class * extends androidx.lifecycle.ViewModel
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}
