![Appboy Logo](https://github.com/Appboy/appboy-android-sdk/blob/master/Appboy_Logo_400x100.png)

# Android SDK

Successful marketing automation is essential to the future of your mobile app. Appboy helps you engage your users beyond the download. Visit the following links for details and we'll have you up and running in no time!

- [Appboy Academy](http://www.appboy.com/academy "Appboy Academy")
- [Technical Documentation](http://documentation.appboy.com "Appboy Technical Documentation")
- [JavaDocs](http://appboy.github.io/appboy-android-sdk/javadocs/ "Appboy Android SDK Class Documentation")

## Components

- `android-sdk-ui` - the Appboy SDK user interface containing the Appboy jar.
- `wear-library` - a small library to support the Appboy SDK on Android Wear devices.
- `droidboy` - a sample application which demonstrates how to use Appboy.
- `hello-appboy` - a sample gradle application demonstrating a simple use case of Appboy.
- `wearboy` - a sample Android Wear app demonstrating Appboy SDK support for Wear devices.

## Building and Running the Sample Applications

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
   use the `installDebug` task if you have a device or emulator connected to adb and want to run Droidboy or Hello-Appboy.
   You can see other available tasks by running `./gradlew tasks`.

## Remote repository for gradle
The version should match the git version tag, or the most recent version noted in the changelog. An example dependency declaration is:

```
repositories {
   maven { url "http://appboy.github.io/appboy-android-sdk/sdk" }
   ...
}
```

```
dependencies {
   compile 'com.appboy:android-sdk-ui:1.15.+'
   ...
}
```

## Installing android-sdk-ui to Your Local Maven Repository
To install the UI library as an AAR file to your local maven repository, run the `install` task with
`./gradlew install`. You can reference it with groupId `com.appboy` and artifactId `android-sdk-ui`. The version should
match the git version tag, or the most recent version noted in the changelog. An example dependency declaration is:


```
repositories {
   mavenLocal()
   ...
}
```

```
dependencies {
  compile 'com.appboy:android-sdk-ui:1.15.+'
}
```

## Using wear-library in Your Android Wear App
We have included a sample Wear app in this project. As a reference, the main app is Droidboy and the Wear App is wearboy.

1. Copy the wear-library module into your project.
2. Register the AppboyWearableListenerService in your main app's manifest (see Droidboy's manifest.xml).
3. Obtain an instance of the AppboyWearableAdapter in your Wear app to use a subset of methods of the Appboy singleton. See the Wearboy sample app.
4. The feature "android.hardware.type.watch" must be declared in your Wear app's manifest for sdk actions to be properly logged from the watch.


## Version Support

The Android SDK supports Android 2.3+ (Gingerbread and up).

Appboy uses [Font Awesome](http://fortawesome.github.io/Font-Awesome/) 4.3.0 for in-app message icons.  Check out the [cheat sheet](http://fortawesome.github.io/Font-Awesome/cheatsheet/) to browse available icons.

Appboy requires Facebook's [Fresco image library](https://github.com/facebook/fresco) to display animated `gif` images.  Fresco is not included nor enabled by default in the Appboy library, and must be included and enabled explicitly by the parent app. Appboy supports Fresco 0.6.0 through 0.9.0.

## Questions?

If you have questions, please contact [support@appboy.com](mailto:support@appboy.com).
