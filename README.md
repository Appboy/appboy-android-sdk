![Braze Logo](https://github.com/Appboy/appboy-android-sdk/blob/master/braze-logo.png)

# Android SDK

Successful marketing automation is essential to the future of your mobile app. Braze helps you engage your users beyond the download. Visit the following links for details and we'll have you up and running in no time!

- [Braze User Guide](https://www.braze.com/docs/user_guide/introduction/ "Braze User Guide")
- [Braze Developer Guide](https://www.braze.com/docs/developer_guide/platform_integration_guides/android/initial_sdk_setup/android_sdk_integration/ "Braze Developer Guide")
- [KDoc](https://appboy.github.io/appboy-android-sdk/kdoc/ "Braze Android SDK Class Documentation")
- [Javadoc(old)](https://appboy.github.io/appboy-android-sdk/javadocs/ "Braze Android SDK Class Documentation"). This Javadoc is discontinued. For up-to-date documentation, please visit the Kotlin Doc (KDoc) instead.

## Version Information

- The Braze Android SDK supports Android 4.1+ / API 16+ (Jelly Bean and up).
- Last Target SDK Version: 33
- Kotlin version: `org.jetbrains.kotlin:kotlin-stdlib:1.6.0`
- Last Compiled Firebase Cloud Messaging Version: 23.0.0
- Braze uses [Font Awesome](https://fortawesome.github.io/Font-Awesome/) 4.3.0 for in-app message icons. Check out the [cheat sheet](http://fortawesome.github.io/Font-Awesome/cheatsheet/) to browse available icons.

## Components

- `android-sdk-base` - the Braze SDK base analytics library.
- `android-sdk-ui` - the Braze SDK user interface library for in-app messages, push, and the news feed.
- `droidboy` - a sample app demonstrating how to use Braze in-depth.
- `android-sdk-unity` - a library that enables Braze SDK integrations on Unity.
- `samples` - a folder containing several sample apps for various integration options.

## Remote repository for gradle
The version should match the git version tag, or the most recent version noted in the changelog. An example dependency declaration is:

```
allprojects {
  repositories {
    maven { url "https://appboy.github.io/appboy-android-sdk/sdk" }
    ...
  }
}
```

```
dependencies {
  implementation 'com.appboy:android-sdk-ui:23.3.+'
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
  implementation 'com.appboy:android-sdk-ui:23.3.+'
}
```

## Questions?

If you have questions, please contact [support@braze.com](mailto:support@braze.com).
