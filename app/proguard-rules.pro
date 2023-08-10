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

-keepnames class kotlinx.coroutines.JobCancellationException {*;}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.Job {*;}

# Saving class name to detect the problem source in Crashlytics
-keepnames class * extends org.p2p.wallet.common.mvp.BaseFragment
-keepnames class * extends org.p2p.wallet.common.mvp.BaseMvpActivity
-keepnames class * extends org.p2p.wallet.common.ui.bottomsheet.BaseBottomSheet
-keepnames class * extends org.p2p.wallet.common.mvp.BaseMvpBottomSheet
-keepnames class * extends com.google.android.material.bottomsheet.BottomSheetDialogFragment
-keepnames class * extends org.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
-keepnames class androidx.biometric.BiometricFragment
-keepnames class androidx.biometric.FingerprintDialogFragment

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
-keep public class ** extends java.lang.Throwable  # Optional: Keep custom exceptions.
-keep class ** extends kotlin.Throwable  # Optional: Keep custom exceptions.

# Our own classes
-keep public class kotlin.coroutines.cancellation.CancellationExceptionKt
-keep public class org.p2p.core.crashlytics.helpers.TimberCrashTree
-keep public class org.p2p.core.crashlytics.helpers.CrashHttpLoggingInterceptor
-keep class io.sentry.SentryEvent { *; }
-keep public class * implements org.p2p.wallet.auth.model.OnboardingFlow
-keep public class org.p2p.wallet.auth.model.RestoreError
-keep public class * implements org.p2p.wallet.auth.model.RestoreUserResult
-keep public class * implements org.p2p.wallet.jupiter.statemanager.SwapStateAction
-keep public class * extends org.p2p.wallet.moonpay.model.MoonpayBuyResult
-keep public class * implements org.p2p.core.network.data.transactionerrors.RpcTransactionError
-keep public class * implements org.p2p.core.network.data.transactionerrors.TransactionInstructionError
-keep public class * implements org.p2p.wallet.send.model.FeeRelayerStateError
-keep public class * implements org.p2p.wallet.send.model.FeeRelayerState
-keep public class * extends org.p2p.wallet.transaction.model.progressstate.TransactionState
-keep public class * implements org.p2p.wallet.feerelayer.model.FeeCalculationState
-keep public class * implements org.p2p.wallet.bridge.model.BridgeResult
-keep public class * extends org.p2p.wallet.striga.common.model.StrigaDataLayerError
-keep enum org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

-keep class io.intercom.android.** { *; }
-keep class com.intercom.** { *; }
-keep class com.appsflyer.** { *; }
-keep public class com.android.installreferrer.** { *; }
-keep public class com.miui.referrer.** {*;}
-dontwarn com.appsflyer.**
-keep public class com.google.firebase.messaging.FirebaseMessagingService {
    public *;
}
# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

-keep class com.lokalise.** { *; }
-dontwarn com.lokalise.*
-keep interface io.realm.annotations.RealmModule { *; }
-keep class io.realm.annotations.RealmModule { *; }
-keep class org.bouncycastle.jcajce.provider.** { *; }
-keep class org.bouncycastle.jce.provider.** { *; }

-dontwarn javax.naming.**