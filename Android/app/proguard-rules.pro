# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-dontwarn okio.**
-dontwarn javax.annotation.**

# Gson
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-dontwarn sun.misc.Unsafe

# Hilt/Dagger (Usually handled automatically, but good to ensure)
-keep class com.katchy.focuslive.BrishApplication { *; }

# --- CRITICAL FIX FOR ROOM & GSON ---
# Keep all data models to prevent R8 from renaming fields that map to DB columns or JSON
-keep class com.katchy.focuslive.data.model.** { *; }

# Room
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * implements androidx.room.TypeConverter { *; }
-dontwarn androidx.room.paging.**

# Keep generic types for Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# --- STRIP LOGS IN RELEASE ---
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}