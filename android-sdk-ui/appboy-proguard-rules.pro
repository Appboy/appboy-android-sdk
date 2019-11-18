# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in android-sdk/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
#
# See https://github.com/Appboy/appboy-android-sdk/issues/49
-keepnames class com.appboy.ui.** { *; }

-dontwarn com.appboy.ui.**
-dontwarn com.google.firebase.messaging.**

-keepclassmembers class * {
   @android.webkit.JavascriptInterface <methods>;
}
