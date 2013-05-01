appboy-android-sdk
==================

This is the public repo for the Appboy Android SDK.

## Android Testing Environment
Full support for Android is currently running off Appboy's staging environment, which can be accessed http://sweeney.appboy.com. 
We're making daily changes to this branch but will have a stable environment the week starting 6 May 13. We will migrate accounts to our production environment.

**GETTING STARTED**:

1. Go to http://sweeney.appboy.com and create an account
2. Add a new App Group by clicking the + sign in the left column
3. If you're app is released, use the Play Store search to add it to the Group. If not, just type the name and add your app.
4. Follow integration instructions available at http://appboy.github.com/appboy-android-sdk/
5. Target our staging environment in your app's manifest as a child of the application element.

```java
<meta-data android:name="com.appboy.SERVER_TARGET" android:value="staging" />
``` 

Please *DO NOT* submit apps including the beta SDK to the Google Play Store (or other app stores).

### Supported OS Versions
Android 2.2+ (Froyo and up).

### Features
We currently have full feature parity with our iOS V2 SDK.

### Feedback & Support
For feedback and any support questions, please email android-beta@appboy.com
