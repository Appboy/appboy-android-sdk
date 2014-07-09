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

1. Make sure the ANDROID_HOME environment variable is set to the location of your installed SDK or you have a
   local.properties file which defines the sdk.dir property. You can check this by running `echo $ANDROID_HOME`, or
   `echo %ANDROID_HOME%` in Windows.
2. When you run your first build, the android-sdk-manager plugin will automatically install the proper Android build
   tools, setup your Android Support Library repository, etc. If you don't have your ANDROID_HOME variable properly set
   or don't have a local.properties folder with a valid sdk.dir folder, this plugin will also install the base SDK for
   you. Other than a bit of wasted disk space, it isn't bad if the plugin re-installs your Android SDK (it'll put it in
   `~/.android-sdk` if you want to move it or delete it later), but either way, it's cleaner to have your ANDROID_HOME
   properly set before running the build. See the [plugin repo](https://github.com/JakeWharton/sdk-manager-plugin) for
   more information.
3. To assemble the UI library and the Droidboy APK, run `./gradlew assemble` (use gradlew.bat on Windows). You can find
   assembled files in the `build/` subdirectories of the `android-sdk-ui` and `droidboy` projects. You can also
   use the `installDebug` task if you have a device or emulator connected to adb and want to run Droidboy. You can see
   other available tasks by running `./gradlew tasks`.

## Installing android-sdk-ui to Your Local Maven Repository
To install the UI library as an AAR file to your local maven repository, run the `install` task with
`./gradlew install`. You can reference it with groupId `com.appboy` and artifactId `android-sdk-ui`. The version should
match the git version tag, or the most recent version noted in the changelog. An example dependency declaration is:

```
dependencies {
  compile 'com.appboy:android-sdk-ui:1.4.+'
}
```

## Version Support

The Android SDK supports Android 2.2+ (Froyo and up).

## Questions?

If you have questions, please contact [support@appboy.com](mailto:support@appboy.com).
