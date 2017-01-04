# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in android-sdk/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keeping classes in com.appboy.ui and com.appboy.services because not keeping
# them can cause build failures for users using Google Play Services with
# Appboy. Alternative fix for:
# https://github.com/Appboy/appboy-android-sdk/issues/49
-keepnames class com.appboy.ui.** { *; }
-keep class com.appboy.services.** { *; }

-dontwarn com.amazon.device.messaging.**
-dontwarn bo.app.**
-dontwarn com.appboy.ui.**
-dontwarn com.google.android.gms.**
