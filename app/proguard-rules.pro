# =============================================================================
# Gemini Music Player - ProGuard Rules
# =============================================================================
# 此檔案定義 R8/ProGuard 混淆規則，確保 Release 版本正常運作。
# =============================================================================

# -----------------------------------------------------------------------------
# 通用設定
# -----------------------------------------------------------------------------
# 保留行號資訊以便於 Crash Report 分析
-keepattributes SourceFile,LineNumberTable

# 重命名 SourceFile 屬性以節省空間
-renamesourcefileattribute SourceFile

# 保留 Signature 資訊 (泛型支援)
-keepattributes Signature

# 保留 Annotation (Room, Hilt, Retrofit 等需要)
-keepattributes *Annotation*

# 保留 Exceptions 資訊
-keepattributes Exceptions

# -----------------------------------------------------------------------------
# Kotlin Serialization (如果使用)
# -----------------------------------------------------------------------------
-keepattributes InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# -----------------------------------------------------------------------------
# Retrofit + OkHttp
# -----------------------------------------------------------------------------
# Retrofit
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Gson (用於 Retrofit Converter)
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保留 API Response 模型
-keep class com.gemini.music.data.network.model.** { *; }

# -----------------------------------------------------------------------------
# Room Database
# -----------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# 保留 Room 實體類別
-keep class com.gemini.music.data.database.** { *; }

# -----------------------------------------------------------------------------
# Hilt / Dagger
# -----------------------------------------------------------------------------
-dontwarn dagger.hilt.internal.aggregatedroot.codegen.**
-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }

# -----------------------------------------------------------------------------
# Media3 / ExoPlayer
# -----------------------------------------------------------------------------
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# 保留 MediaSession 相關類別
-keep class * extends androidx.media3.session.MediaLibraryService { *; }
-keep class * extends androidx.media3.session.MediaSessionService { *; }

# -----------------------------------------------------------------------------
# Jetpack Compose
# -----------------------------------------------------------------------------
-dontwarn androidx.compose.**

# -----------------------------------------------------------------------------
# Coroutines
# -----------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# -----------------------------------------------------------------------------
# DataStore
# -----------------------------------------------------------------------------
-keep class androidx.datastore.*.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# -----------------------------------------------------------------------------
# Amplituda (Audio Waveform Library)
# -----------------------------------------------------------------------------
-keep class linc.com.amplituda.** { *; }
-dontwarn linc.com.amplituda.**

# -----------------------------------------------------------------------------
# Domain Layer Models (Pure Kotlin)
# -----------------------------------------------------------------------------
# 保留 Domain 模型，防止序列化問題
-keep class com.gemini.music.domain.model.** { *; }

# -----------------------------------------------------------------------------
# 避免警告
# -----------------------------------------------------------------------------
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
