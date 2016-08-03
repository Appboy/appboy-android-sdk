# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in android-sdk/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# turning off optimization because it can cause build failures for users using Google Play Services
# with Appboy. See https://github.com/Appboy/appboy-android-sdk/issues/49
-dontoptimize

-dontwarn com.amazon.device.messaging.**
-dontwarn bo.app.**
-dontwarn com.appboy.ui.**
-dontwarn com.google.android.gms.**
