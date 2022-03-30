-dontwarn org.p2p.wallet.**

-keepclassmembers class ** implements androidx.viewbinding.ViewBinding {
   public static *** inflate(...);
   public static *** bind(***);
}

# GSON
# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keep class org.p2p.solanaj.** { *; }
-keep class org.p2p.wallet.utils.NavigationExtensionsKt

# Saving class name to detect the problem source in Crashlytics
-keepnames class  * extends org.p2p.wallet.common.mvp.BaseFragment

# Remove logging
-assumenosideeffects class android.util.Log {
    public static *** e(...);
    public static *** w(...);
    public static *** wtf(...);
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
-assumenosideeffects class timber.log.Timber {
    public static *** w(...);
    public static *** wtf(...);
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

-keep class com.google.android.gms.** { *; }
-keep class com.google.android.apps.** { *; }

# GeeTest SDK has already been obfuscated, please do not obfuscate it again
-dontwarn com.geetest.sdk.**
-keep class com.geetest.sdk.**{*;}

-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.
-keep public class org.p2p.wallet.common.crashlytics.TimberCrashTree
-keep public class org.p2p.wallet.common.crashlytics.CrashHttpLoggingInterceptor