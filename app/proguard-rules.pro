-dontwarn org.p2p.wallet.**

# Glide specific rules #
# https://github.com/bumptech/glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# Retrofit 2.X
## https://square.github.io/retrofit/ ##
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keepclassmembers class ** implements androidx.viewbinding.ViewBinding {
   public static *** inflate(...);
   public static *** bind(***);
}

# GSON
# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers enum * {*;}
-keep class org.p2p.solanaj.** { *; }
-keep class org.p2p.wallet.utils.NavigationExtensionsKt

# Saving class name to detect the problem source in Crashlytics
-keepnames class  * extends org.p2p.wallet.common.mvp.BaseFragment
-keepnames class  * extends org.p2p.wallet.common.mvp.BaseMvpActivity
-keepnames class  * extends com.google.android.material.bottomsheet.BottomSheetDialogFragment

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
-keep public class org.p2p.wallet.common.crashlogging.helpers.TimberCrashTree
-keep public class org.p2p.wallet.common.crashlogging.helpers.CrashHttpLoggingInterceptor
-keep class io.sentry.SentryEvent { *; }

-keep class io.intercom.android.** { *; }
-keep class com.intercom.** { *; }
-keep class com.appsflyer.** { *; }
-keep public class com.miui.referrer.** {*;}
-dontwarn com.appsflyer.**
-keep public class com.google.firebase.messaging.FirebaseMessagingService {
    public *;
}
