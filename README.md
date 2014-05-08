![Appboy Logo](http://www.appboy.com/assets/logo-cee4c217ff207f321cc7250f86a1037d.gif)

# Android SDK

Successful marketing automation is essential to the future of your mobile app. Appboy helps you engage your users beyond the download. Visit the following links for details and we'll have you up and running in no time!

- [Appboy Academy](http://www.appboy.com/academy "Appboy Academy")
- [Technical Documentation](http://documentation.appboy.com "Appboy Technical Documentation")
- [Full Class Documentation](http://appboy.github.io/appboy-android-sdk/javadocs/ "Appboy Android SDK Class Documentation")

## Components

- `android-sdk-ui` - the Appboy SDK user interface containing the Appboy jar.
- `droidboy` - a sample application which demonstrates how to use Appboy.

## Building and Running the Sample Application

1. Install the Android Support Library and Android Support Repository with the `android` tool. Documentation on the
   support library is available in Google's
   [developer documentation](https://developer.android.com/tools/support-library/setup.html). Once installed, you should
   be able to see the support library in the `extras/android/m2repository/com/android/support/` subdirectory of the
   location of your SDK installation.
2. Install Android Build Tools v19.0.3 with the `android` tool (or update the buildToolsVersion in build.gradle to a
   version you already have installed and want to use).
3. Make sure the ANDROID_HOME environment variable is set to the location of your installed SDK or you have a
   local.properties file which defines the sdk.dir property.
4. To assemble the UI library and the Droidboy APK, run `./gradlew assemble` (use gradlew.bat on Windows). You can find
   assembled files in the `build/` subdirectories of the `android-sdk-ui` and `droidboy` projects. You can also
   use the `installDebug` task if you have a device or emulator connected to adb and want to run Droidboy. You can see
   other available tasks by running `./gradlew tasks`.

## Installing the android-sdk-ui to your local maven repository and using it in builds.
To install the UI library as an AAR file to your local maven repository, run the `install` task with
`./gradlew install`.

## Version Support

The Android SDK supports Android 2.2+ (Froyo and up).

## Questions?

If you have questions, please contact [support@appboy.com](mailto:support@appboy.com).