-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowobfuscation class * {
  @com.squareup.moshi.Json <fields>;
}

-keepclassmembers enum org.p2p.solanaj.** { *; }
-keep class org.p2p.solanaj.** { *; }

# Remove logging
-assumenosideeffects class android.util.Log {
    public static *** e(...);
    public static *** w(...);
    public static *** wtf(...);
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

-keep class com.google.android.gms.** { *; }