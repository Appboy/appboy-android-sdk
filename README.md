<img src="https://github.com/Appboy/appboy-android-sdk/blob/master/braze-logo.png" width="300" title="Braze Logo" />

# Android SDK

Successful marketing automation is essential to the future of your mobile app. Braze helps you engage your users beyond the download. Visit the following links for details and we'll have you up and running in no time!

- [Braze User Guide](https://www.braze.com/docs/user_guide/introduction/ "Braze User Guide")
- [Braze Developer Guide](https://www.braze.com/docs/developer_guide/platform_integration_guides/android/initial_sdk_setup/android_sdk_integration/ "Braze Developer Guide")
- [JavaDocs](http://appboy.github.io/appboy-android-sdk/javadocs/ "Braze Android SDK Class Documentation")

## Components

- `android-sdk-base` - the Braze SDK base analytics library.
- `android-sdk-ui` - the Braze SDK user interface library for in-app messages, push, and the news feed.
- `droidboy` - a sample app demonstrating how to use Braze in-depth.
- `hello-appboy` - a sample app demonstrating a basic Braze integration.
- `china-push-sample` - a sample app demonstrating the Braze + Baidu push integration.
- `android-sdk-unity` - a library that enables Braze SDK integrations on Unity.
- `samples` - a folder containing several sample apps for various integration options.

## Version Support

- The Braze Android SDK supports Android 4.1+ / API 16+ (Jelly Bean and up).
- Last Target SDK Version: 29
- Last Compiled Support Library Version: 28.0.0
- Braze uses [Font Awesome](http://fortawesome.github.io/Font-Awesome/) 4.3.0 for in-app message icons. Check out the [cheat sheet](http://fortawesome.github.io/Font-Awesome/cheatsheet/) to browse available icons.
- Braze requires either a custom [IAppboyImageLoader](https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/IAppboyImageLoader.html) to display animated `gif` images.

## Remote repository for gradle
The version should match the git version tag, or the most recent version noted in the changelog. An example dependency declaration is:

```
repositories {
   maven { url "https://appboy.github.io/appboy-android-sdk/sdk" }
   ...
}
```

```
dependencies {
   compile 'com.appboy:android-sdk-ui:3.7.+'
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
  compile 'com.appboy:android-sdk-ui:3.7.+'
}
```

## Building and Running the Sample Applications

1. Make sure the ANDROID_HOME environment variable is set to the location of your installed SDK or you have a
   local.properties file which defines the sdk.dir property. You can check this by running `echo $ANDROID_HOME`, or
   `echo %ANDROID_HOME%` in Windows.
2. To assemble the UI library and the Droidboy APK, run `./gradlew assemble` (use gradlew.bat on Windows). You can find
   assembled files in the `build/` subdirectories of the `android-sdk-ui` and `droidboy` projects. You can also
   use the `installDebug` task if you have a device or emulator connected to adb and want to run Droidboy or Hello-Appboy.
   You can see other available tasks by running `./gradlew tasks`.

## Questions?

If you have questions, please contact [support@braze.com](mailto:support@braze.com).
