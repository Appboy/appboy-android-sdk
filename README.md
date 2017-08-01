![Appboy Logo](https://github.com/Appboy/appboy-android-sdk/blob/master/Appboy_Logo_400x100.png)

# Android SDK

Successful marketing automation is essential to the future of your mobile app. Appboy helps you engage your users beyond the download. Visit the following links for details and we'll have you up and running in no time!

- [Appboy Academy](http://www.appboy.com/academy "Appboy Academy")
- [Technical Documentation](http://documentation.appboy.com "Appboy Technical Documentation")
- [JavaDocs](http://appboy.github.io/appboy-android-sdk/javadocs/ "Appboy Android SDK Class Documentation")

## Components

- `android-sdk-ui` - the Appboy SDK user interface containing the Appboy jar.
- `droidboy` - a sample application which demonstrates how to use Appboy.
- `hello-appboy` - a sample gradle application demonstrating a simple use case of Appboy.
- `android-sdk-unity` - a library that enables Appboy SDK integrations on Unity.

## Building and Running the Sample Applications

1. Make sure the ANDROID_HOME environment variable is set to the location of your installed SDK or you have a
   local.properties file which defines the sdk.dir property. You can check this by running `echo $ANDROID_HOME`, or
   `echo %ANDROID_HOME%` in Windows.
2. To assemble the UI library and the Droidboy APK, run `./gradlew assemble` (use gradlew.bat on Windows). You can find
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
   compile 'com.appboy:android-sdk-ui:2.1.+'
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
  compile 'com.appboy:android-sdk-ui:2.1.+'
}
```

## Version Support

The Android SDK supports Android 4.0+ (Ice Cream Sandwich and up).

Appboy uses [Font Awesome](http://fortawesome.github.io/Font-Awesome/) 4.3.0 for in-app message icons.  Check out the [cheat sheet](http://fortawesome.github.io/Font-Awesome/cheatsheet/) to browse available icons.

Appboy requires Facebook's [Fresco image library](https://github.com/facebook/fresco) to display animated `gif` images.  Fresco is not included nor enabled by default in the Appboy library, and must be included and enabled explicitly by the parent app. Appboy supports Fresco 0.6.0 through 1.3.0.

## Questions?

If you have questions, please contact [support@appboy.com](mailto:support@appboy.com).
