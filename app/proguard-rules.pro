# Panther release shrinking rules.
#
# Room generates implementations reflectively referenced by the generated database class.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Hilt/Dagger generated components are accessed reflectively.
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep,allowobfuscation @interface dagger.hilt.**

# Kotlin metadata is required for kotlinx.coroutines and reflection-based helpers.
-keepclassmembers class kotlin.Metadata { public <methods>; }

# Keep the domain model names used as JSON keys and enum values.
-keepclassmembers enum com.panther.app.domain.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve line numbers so crash reports stay actionable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
