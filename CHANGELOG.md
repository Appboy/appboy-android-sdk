## 3.1.1

##### Breaking
- Changed signature of `Appboy.logPushNotificationActionClicked()`.

##### Added
- Added ability to render HTML elements in push notifications via `AppboyConfig.setPushHtmlRenderingEnabled()` and also `com_appboy_push_notification_html_rendering_enabled` in your `appboy.xml`.
  - This allows the ability to use "multicolor" text in your push notifications.
  - Note that html rendering be used on all push notification text fields when this feature is enabled.
- Added `AppboyFirebaseMessagingService` to directly use the Firebase messaging event `com.google.firebase.MESSAGING_EVENT`. This is now the recommended way to integrate Firebase push with Braze. The `AppboyFcmReceiver` can be removed from your `AndroidManifest` and replaced with the following:
  - ```
    <service android:name="com.appboy.AppboyFirebaseMessagingService">
      <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
      </intent-filter>
    </service>
    ```
  - Also note that any `c2dm` related permissions should be removed from your manifest as Braze does not require any extra permissions for `AppboyFirebaseMessagingService` to work correctly.

##### Fixed
- Fixed behavior where the app would be reopened after clicking notification action buttons with a "close" button.
- Fixed behavior where in-app messages would not apply proper margins on devices with notched displays and would appear obscured by the notch.
- Fixed an issue that caused the enum `DeviceKey` to be unavailable in our public API.
- Fixed an issue in the `AppboyFcmReceiver` where the "is uninstall tracking push" method was looking for the extras bundle before its preprocessing into a bundle. This would result in uninstall tracking push being forwarded to your broadcast receiver as a silent push when it should not.
- Fixed an issue in the `AppboyLruImageLoader` where very large bitmaps stored in the cache could throw `OutOfMemoryError` when retrieving them from the cache.

##### Changed
- Changed behavior of the Feed and Content Cards image loader to always resize images to their true source aspect ratio after download.

## 3.1.0

##### Breaking
- Renamed `AppboyNotificationUtils.wakeScreenIfHasPermission()` to `AppboyNotificationUtils.wakeScreenIfAppropriate()`. Wakelocks can now be configured to not wake the device screen for push notifications.
  - This can be set via `AppboyConfig.setIsPushWakeScreenForNotificationEnabled()` and also `com_appboy_push_wake_screen_for_notification_enabled` in your `appboy.xml`.

##### Added
- A drop-in `AppboyContentCardsActivity` class has been added which can be used to display Braze Content Cards.
- Added HTML IAM `appboyBridge` ready event to know precisely when the `appboyBridge` has finished loading.
  - Example below:
    ```javascript
     <script type="text/javascript">
       function logMyCustomEvent() {
         appboyBridge.logCustomEvent('My Custom Event');
       }
       window.addEventListener('ab.BridgeReady', logMyCustomEvent, false);
     </script>
    ```
- Added `Constants.TRAFFIC_STATS_THREAD_TAG` to identify the Braze network traffic with the `TrafficStats` API.
- Added the ability to configure a blacklist of Activity classes to disable automatic session handling and in-app message registration in the AppboyLifecycleCallbackListener. See `AppboyLifecycleCallbackListener.setActivityClassInAppMessagingRegistrationBlacklist()`, `AppboyLifecycleCallbackListener.setActivityClassSessionHandlingBlacklist()`, and constructor `AppboyLifecycleCallbackListener(boolean, boolean, Set<Class>, Set<Class>)`.

##### Changed
- Deprecated the Feedback feature. This feature is disabled for new accounts, and will be removed in a future SDK release.
- Changed the deprecated status of the `AppboyNotificationUtils.isUninstallTrackingPush()` method. Note that uninstall tracking notifications will not be forwarded to registered receivers.

## 3.0.1

##### Changed
- Deprecated `Card.isRead()` and `Card.setIsRead()`. Please use `Card.isIndicatorHighlighted()` and `Card.setIndicatorHighlighted()` instead.

##### Added
- Added `Card.isClicked()`. Clicks made through `Card.logClick()` are now saved locally on the device for Content Cards.
- Added `AppboyConfig.setIsInAppMessageAccessibilityExclusiveModeEnabled()` which forces accessibility readers to only be able to read currently displaying in-app messages and no other screen contents.
  - This can also be set via `com_appboy_device_in_app_message_accessibility_exclusive_mode_enabled` in your `appboy.xml`.

## 3.0.0

##### Breaking
- From `AppboyConfig`, removed `getEnableBackgroundLocationCollection()`, `getLocationUpdateTimeIntervalSeconds()`, and `getLocationUpdateDistance()` and their respective setters in `AppboyConfig.Builder`.
- Removed the Fresco image library from the SDK. To displaying GIFs, you must use a custom image library. Please see `IAppboy#setAppboyImageLoader(IAppboyImageLoader)`.
  - We recommend the [Glide Image Library](https://bumptech.github.io/glide/) as a Fresco replacement.
  - `AppboyConfig.Builder.setFrescoLibraryEnabled()` has been removed.
  - `AppboyConfigurationProvider.getIsFrescoLibraryUseEnabled()` has been removed.

##### Fixed
- Fixed a NPE issue with the RecyclerView while saving the instance state in the `AppboyContentCardsFragment`.

##### Added
- Added the ability to set location custom attributes on the html in-app message javascript interface.
- Added compatibility with androidX dependencies.
    - This initial release adds direct compatibility for classes found under the `com.appboy.push` package. These classes are commonly used in conjunction with an `IAppboyNotificationFactory`. To use these compatible classes, add the following gradle import: `implementation 'com.appboy:android-sdk-ui-x:VERSION'` and replace your imports to fall under the `com.appboy.uix.push` package.
    - The gradle properties `android.enableJetifier=true` and `android.useAndroidX=true` are required when using androidX libraries with the Braze SDK.
- Added nullability annotation to `Appboy` and `AppboyUser` for better Kotlin interoperability.
- Added the ability to optionally whitelist keys in the device object. See `AppboyConfig.Builder.setDeviceObjectWhitelistEnabled()` and `AppboyConfig.Builder.setDeviceObjectWhitelist()` for more information.
  - The following example showcases whitelisting the device object to only include the Android OS version and device locale in the device object.
    ```
      new AppboyConfig.Builder()
          .setDeviceObjectWhitelistEnabled(true)
          .setDeviceObjectWhitelist(EnumSet.of(DeviceKey.ANDROID_VERSION, DeviceKey.LOCALE));
    ```

##### Removed
- Removed the ability to optionally track locations in the background.
- Removed Cross Promotion cards from the News Feed.
  - Cross Promotion cards have also been removed as a card model and will thus no longer be returned.

##### Changed
- Updated the Baidu China Push sample to use the version 2.9 Baidu JNI libraries and version 6.1.1.21 of the Baidu jar.

## 2.7.0

##### Breaking
- Renamed `AppboyGcmReceiver` to `AppboyFcmReceiver`. This receiver is intended to be used for Firebase integrations and thus the `com.google.android.c2dm.intent.REGISTRATION` intent-filter action in your `AndroidManifest` should be removed.
- Removed `AppboyConfigurationProvider.isGcmMessagingRegistrationEnabled()`, `AppboyConfigurationProvider.getGcmSenderId()`, `AppboyConfig.Builder.setGcmSenderId()`, and `AppboyConfig.Builder.setGcmMessagingRegistrationEnabled()`.

##### Changed
- Changed custom event property values validation to allow for empty strings.

## 2.6.0

##### Added
- Introduced support for the upcoming Content Cards feature, which will eventually replace the existing News Feed feature and adds significant capability. This feature is currently in closed beta testing; if you're interested in joining the beta, please reach out to your Customer Success Manager or Account Manager.

##### Breaking
- Updated the minimum SDK version from 14 (Ice Cream Sandwich) to 16 (Jelly Bean).

##### Added
- Added `AppboyUser.setLocationCustomAttribute()` and `AppboyUser.unsetLocationCustomAttribute()`.

## 2.5.1

##### Changed
- Changed the behavior of push stories to ensure that after the story initially appears in the notification tray, subsequent page traversal clicks don't alert the user again.

##### Added
- The Braze SDK now automatically records when the user has disabled notifications at the app level.
  - The `appboy.xml` `com_appboy_notifications_enabled_tracking_on` boolean attribute and `AppboyConfig.Builder.setNotificationsEnabledTrackingOn()` have been deprecated and are no longer used.
  - This allows users to more effectively opt-out of push and leads to a more accurate push notification reachable audience.

##### Fixed
- Fixed an issue where, when the lock screen was present, notification action button and push story body clicks would not open the application immediately. Added `AppboyNotificationRoutingActivity` for handling notification action button and push story body clicks. 
- Fixed an issue where, for non fullscreen activities targeting API 27, requesting an orientation on activities would throw an exception.

## 2.5.0

##### Breaking
- Added `isControl()` to the `IInAppMessage` interface.
- Added `logDisplayFailure()` to the `IInAppMessage` interface. In-app message display failures may affect campaign statistics so care should be taken when logging display failures.
- Added the `InAppMessageControl` class to represent control in-app messages. Control in-app messages should not be displayed to users and should only call `logImpression()` at render time.
  - Requesting in-app message display, even if the stack is non-empty, may potentially lead to no in-app message displaying if the in-app message is a control in-app message.
- Added `AppboyInAppMessageManager.setCustomControlInAppMessageManagerListener()` to modify the lifecycle behavior for control in-app messages.
- Removed `logInAppMessageClick`, `logInAppMessageButtonClick`, and `logInAppMessageImpression` from Appboy Unity player subclasses and `AppboyUnityActivityWrapper`.
- Removed `AppboyConfigurationProvider.getIsUilImageCacheDisabled()` and `AppboyConfig.Builder.setDisableUilImageCache()`.

##### Fixed
- Fixed the issue where in-app messages triggered on session start could potentially be templated with the old user's attributes.
- Fixed a bug where calling `Appboy.wipeData()` or `Appboy.disableSdk()` could potentially lead to null instances being returned from `Appboy.getInstance()`.
- Fixed the issue where push deep links did not respect the back stack behavior when instructed to open inside the app's WebView.
- Fixed a bug where the push received broadcast action contained the host package name twice.

## 2.4.0

##### Fixed
- Fixed a bug where calling `Appboy.wipeData()` would throw an uncaught exception when the Google Play location services library was not present.

##### Added
- Added the ability to listen for notification deleted intents from the `AppboyGcmReceiver` via the action suffix `AppboyNotificationUtils.APPBOY_NOTIFICATION_DELETED_SUFFIX`.
- Added a notification creation timestamp to notifications built from the `AppboyGcmReceiver`. This allows for calculating the duration of a notification. Intents will contain `Constants.APPBOY_PUSH_RECEIVED_TIMESTAMP_MILLIS` in the intent extras bundle.

##### Changed
- Deprecated `AppboyNotificationUtils.isUninstallTrackingPush()` to always return false. Uninstall tracking no longer requires sending a silent push notification to devices.

## 2.3.0

##### Known Issues with version 2.3.0
- If the Google Play location services library is not present, calls to `Appboy.wipeData()` will throw an uncaught exception.

##### Breaking
- Removed the `appboyInAppMessageCustomFontFile` custom xml attribute. Custom font typefaces must now be located in the `res/font` directory.
  - To override a Braze style, both `android:fontFamily` and `fontFamily` style attributes must be set to maintain compatibility across all SDK versions. Example below:
  ```
  <item name="android:fontFamily">@font/YOUR_CUSTOM_FONT_FAMILY</item>
  <item name="fontFamily">@font/YOUR_CUSTOM_FONT_FAMILY</item>
  ```
  - See https://developer.android.com/guide/topics/ui/look-and-feel/fonts-in-xml.html for more information.
- Removed `AppboyInAppMessageButtonView.java` and `AppboyInAppMessageTextView.java`.
- Removed the `AppboyGeofenceService`. Geofence integration no longer requires a manifest registration. Any reference to `AppboyGeofenceService` can safely be removed from your manifest.
- Renamed `AppboyUnityPlayerNativeActivityWrapper` to `AppboyUnityActivityWrapper`.

##### Fixed
- Fixed a bug where sessions could be opened and closed with a null activity.

##### Added
- Added the ability to have the Braze SDK automatically register for Firebase Cloud Messaging.
  - Enabled via `com_appboy_firebase_cloud_messaging_registration_enabled` boolean attribute in XML or via `AppboyConfig.Builder.setIsFirebaseCloudMessagingRegistrationEnabled()`.
  - The Firebase Cloud Messaging Sender ID is set via `com_appboy_firebase_cloud_messaging_sender_id` string attribute in XML or via `AppboyConfig.Builder.setFirebaseCloudMessagingSenderIdKey()`.
  - The Firebase Cloud Messaging dependencies must still be compiled into your project. The Braze SDK does not compile any Firebase Cloud Messaging dependencies as part of this release.
- Added UnityPlayerActivity support to AppboyUnityActivityWrapper. Previously only UnityPlayerNativeActivity was supported.
- Added a AppboyUnityPlayerActivity class for the UnityPlayerActivity for both prime31 and non-prime31 integrations.

## 2.2.5

##### Added
- Added support for wiping all customer data created by the Braze SDK via `Appboy.wipeData()`.
- Added `Appboy.disableSdk()` to disable the Braze SDK. 
- Added `Appboy.enableSdk()` to re-enable the SDK after a call to `Appboy.disableSdk()`.

##### Changed
- Changed `AppboyInAppMessageWebViewClientListener` to call `onDismissed()` when `onCloseAction()` gets called for HTML in-app messages.

##### Fixed
- Fixed an issue where internal thread pool executors could get blocked on a long running task and throw `RejectedExecutionException`.

## 2.2.4

##### Added
- Added `AppboyConfig.Builder.setIsSessionStartBasedTimeoutEnabled()` which optionally sets the session timeout behavior to be either session-start or session-end based. The default behavior is to be session-end based.
  - The use of this flag is recommended for long (30 minutes or longer) session timeout values.
  - This value can also be configured via `appboy.xml` with the boolean `com_appboy_session_start_based_timeout_enabled` set to true.

## 2.2.3

##### Added
- Added support for any custom image library to work with in-app messages and the news feed, including the [Glide Image Library](https://bumptech.github.io/glide/). 
  - Please see `IAppboy#setAppboyImageLoader(IAppboyImageLoader)` for how to set a custom image library.
- Added the `Glide Image Integration` sample app, showcasing how to use the Glide Library.

#### Changed
- Updated the proguard rules for Fresco and Notification Enabled Tracking.

## 2.2.2

##### Added
- The Braze SDK may now optionally record when the user has disabled notifications at the app level.
  - Enabled via `appboy.xml` using the `com_appboy_notifications_enabled_tracking_on` boolean attribute or via `AppboyConfig.Builder.setNotificationsEnabledTrackingOn()`.
  - If using proguard in your app and Braze SDK v2.2.2 or below, please add `-keep class android.support.v4.app.NotificationManagerCompat { *; }` to your proguard rules.
  - (Update) Note that starting with Braze Android SDK Version [`2.5.1`](https://github.com/Appboy/appboy-android-sdk/blob/master/CHANGELOG.md#251), this feature is now automatically enabled.

## 2.2.1

##### Added
- Added `Other`, `Unknown`, `Not Applicable`, and `Prefer not to Say` options for user gender.

## 2.2.0

##### Breaking
- Removed `Appboy.requestInAppMessageRefresh()` and removed support for Original in-app messages. Note that all customers on version 2.2.0 and newer should use triggered in-app messages.
- Changed the signature of most methods on the `IAppboy` interface. Methods that logged values now return void instead of boolean. 
  - `IAppboy.openSession()` now returns void.   
  - `IAppboy.closeSession` now returns void.
  - `IAppboy.changeUser()` now returns void. To get the current user, please call `IAppboy.getCurrentUser()`.
  - `IAppboy.logCustomEvent()` and all method overloads now return void.
  - `IAppboy.logPurchase()` and all method overloads now return void.
  - `IAppboy.submitFeedback()` now returns void.
  - `IAppboy.logPushNotificationOpened()` now returns void.
  - `IAppboy.logPushNotificationActionClicked()` now returns void.
  - `IAppboy.logFeedDisplayed()` now returns void.
  - `IAppboy.logFeedbackDisplayed()` now returns void.
- Removed `AppboyFeedbackFragment.FeedbackResult.ERROR`.
- Changed `AppboyFeedbackFragment.FeedbackFinishedListener` to `AppboyFeedbackFragment.IFeedbackFinishedListener`.
- Changed `AppboyFeedbackFragment.FeedbackResult.SENT` to `AppboyFeedbackFragment.FeedbackResult.SUBMITTED`. 
- Removed `Appboy.fetchAndRenderImage()`. Please use `getAppboyImageLoader().renderUrlIntoView()` instead.
- Removed `AppboyFileUtils.getExternalStorage()`.

##### Added
- Added Push Stories, a new push type that uses `DecoratedCustomViewStyle` to display multiple images in a single notification. We recommend posting push stories to a notification channel with vibration disabled to avoid repeated vibrations as the user navigates through the story.

##### Changed
- The Braze singleton now internally performs most actions on a background thread, giving a very substantial performance boost to all actions on the `Appboy` singleton.

#### Fixed
- Reduced the number of connections made when the Braze SDK downloads files and images. Note that the amount of data downloaded has not changed.

## 2.1.4

##### Added
- Added a check on Braze initialization for the "Calypso AppCrawler" indexing bot that disables all Braze network requests when found. This prevents erroneous Braze data from being sent for Firebase app indexing crawlers.
- Added the ability to disable adding an activity to the back stack when automatically following push deep links. Previously, the app's main activity would automatically be added to the back stack.
  - Enabled via `appboy.xml` using the `com_appboy_push_deep_link_back_stack_activity_enabled` boolean attribute or via `AppboyConfig.Builder.setPushDeepLinkBackStackActivityEnabled()`.
- Added the ability to specify a custom activity to open on the back stack when automatically following push deep links. Previously, only the app's main activity could be used.
  - The custom activity is set via `appboy.xml` using the `com_appboy_push_deep_link_back_stack_activity_class_name` string attribute or via `AppboyConfig.Builder.setPushDeepLinkBackStackActivityClass()`. Note that the class name used in the `appboy.xml` must be the exact class name string as returned from `YourClass.class.getName()`.
- Added the `setLanguage()` method to `AppboyUser` to allow explicit control over the language you use in the Braze dashboard to localize your messaging content.

##### Changed
- Added support for acquiring wake locks on Android O using the notification channel importance instead of the individual notification's priority.

## 2.1.3

##### Fixed
- Fixed a bug where implicit intents for custom push broadcast receivers would be suppressed in devices running Android O.
- Updated the Braze ProGuard configuration to ensure Google Play Services classes required by Geofencing aren't renamed.

## 2.1.2

##### Fixed
- Fixed a bug where sealed session flushes would not be sent on apps with long session timeouts due to Android O background service limitations.

## 2.1.1

##### Added
- Added the ability to set a custom API endpoint via `appboy.xml` using the `com_appboy_custom_endpoint` string attribute or via `AppboyConfig.Builder.setCustomEndpoint()`.

##### Fixed
- Fixed a bug where date custom attributes were formatted in the device's locale, which could result in incorrectly formatted dates. Date custom attributes are now always formatted in `Locale.US`.

## 2.1.0

##### Breaking
- Updated the minimum SDK version from 9 (Gingerbread) to 14 (Ice Cream Sandwich). 
  - We recommend that session tracking and In-App Messages registration be done via an `AppboyLifecycleCallbackListener` instance using [`Application.registerActivityLifecycleCallbacks()`](https://developer.android.com/reference/android/app/Application.html#registerActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks)).
- Removed the deprecated field: `AppboyLogger.LogLevel`. Please use `AppboyLogger.setLogLevel()` and `AppboyLogger.getLogLevel()` instead.
- Updated the v4 support library dependency to version 26.0.0. To download Android Support Libraries versions 26.0.0 and above, you must add the following line to your top-level `build.gradle` repositories block:
  ```
  maven {
    url "https://maven.google.com"
  }
  ```

##### Added
- Added support for Android O notification channels. In the case that a Braze notification does not contain the id for a notification channel, Braze will fallback to a default notification channel. Other than the default notification channel, Braze will not create any channels. All other channels must be programatically defined by the host app.
  - Note that default notification channel creation will occur even if your app does not target Android O. If you would like to avoid default channel creation until your app targets Android O, do not upgrade to this version.
  - To set the user facing name of the default Braze notification channel, please use `AppboyConfig.setDefaultNotificationChannelName()`.
  - To set the user facing description of the default Braze notification channel, please use `AppboyConfig.setDefaultNotificationChannelDescription()`.

##### Changed
- Updated the target SDK version to 26.

## 2.0.5

##### Fixed
- Fixed a bug where relative links in `href` tags in HTML In-App Messages would get passed as file Uris to the `AppboyNavigator`.

##### Added
- Added `Double` as a valid value type on `AppboyUser.setCustomUserAttribute()`.
- Added user aliasing capability. Aliases can be used in the API and dashboard to identify users in addition to their ID.  See the `addAlias` method on [`AppboyUser`](https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/AppboyUser.html) for more information.

## 2.0.4

##### Changed
- Made further improvements to Braze singleton initialization performance.

## 2.0.3

##### Changed
- Enabled TLS 1.2 for Braze HTTPS connections running on API 16+ devices. Previously, for devices running on API 16-20, only TLS 1.0 was enabled by default.
- Improved Braze singleton initialization performance.

## 2.0.2

##### Fixed
- Fixed a bug where identifying a user while a request was in flight could cause newly written attributes on the old user to be orphaned in local storage.

## 2.0.1

##### Added
- Added support for displaying Youtube videos inside of HTML in-app messages and the Braze Webview. For HTML in-app messages, this requires hardware acceleration to be enabled in the Activity where the in-app message is being displayed, please see https://developer.android.com/guide/topics/graphics/hardware-accel.html#controlling. Please note that hardware acceleration is only available on API versions 11 and above.
- Added the ability to access Braze's default notification builder instance from custom `IAppboyNotificationFactory` instances. This simplifies making small changes to Appboy's default notification handling.
- Improved `AppboyImageUtils.getBitmap()` by adding the ability to sample images using preset view bounds.

## 2.0.0

##### Breaking
- Removed the following deprecated methods and fields:
  - Removed the unsupported method `Appboy.logShare()`.
  - Removed `Appboy.logPurchase(String, int)`.
  - Removed `Appboy.logFeedCardImpression()` and `Appboy.logFeedCardClick()`. Please use `Card.logClick()` and `Card.logImpression()` instead.
  - Removed the unsupported method `Appboy.getAppboyResourceEndpoint()`.
  - Removed `IAppboyEndpointProvider.getResourceEndpoint()`. Please update your interface implementation if applicable.
  - Removed `Appboy.registerAppboyGcmMessages()`. Please use `Appboy.registerAppboyPushMessages()` instead.
  - Removed `AppboyInAppMessageBaseView.resetMessageMargins()`. Please use `AppboyInAppMessageBaseView.resetMessageMargins(boolean)` instead.
  - Removed `com.appboy.unity.AppboyUnityGcmReceiver`. To open Braze push deep links automatically in Unity, set the boolean configuration parameter `com_appboy_inapp_show_inapp_messages_automatically` to true in your `appboy.xml`.
  - Removed the unsupported method `AppboyUser.setBio()`.
  - Removed `AppboyUser.setIsSubscribedToEmails()`. Please use `AppboyUser.setEmailNotificationSubscriptionType()` instead.
  - Removed `Constants.APPBOY_PUSH_CUSTOM_URI_KEY`. Please use `Constants.APPBOY_PUSH_DEEP_LINK_KEY` instead.
  - Removed `Constants.APPBOY_CANCEL_NOTIFICATION_TAG`.
  - Removed `com.appboy.ui.actions.ViewAction` and `com.appboy.ui.actions.WebAction`.
  - Removed `CardCategory.ALL_CATEGORIES`. Please use `CardCategory.getAllCategories()` instead.
  - Removed `AppboyImageUtils.storePushBitmapInExternalStorage()`.
  - Removed `AppboyFileUtils.canStoreAssetsLocally()` and `AppboyFileUtils.getApplicationCacheDir()`.
  - Removed `InAppMessageModal.getModalFrameColor()` and `InAppMessageModal.setModalFrameColor()`. Please use `InAppMessageModal.getFrameColor()` and `InAppMessageModal.setFrameColor()` instead.
  - Removed `com.appboy.enums.SocialNetwork`.
  - Removed `AppboyNotificationUtils.getAppboyExtras()`. Please use `AppboyNotificationUtils.getAppboyExtrasWithoutPreprocessing()` instead.
  - Removed `AppboyNotificationUtils.setLargeIconIfPresentAndSupported(Context, AppboyConfigurationProvider, NotificationCompat.Builder)`. Please use `AppboyNotificationUtils.setLargeIconIfPresentAndSupported(Context, AppboyConfigurationProvider, NotificationCompat.Builder, Bundle)` instead.
  - Removed `AppboyInAppMessageManager.hideCurrentInAppMessage()`. Please use `AppboyInAppMessageManager.hideCurrentlyDisplayingInAppMessage()` instead.
- Changed method signatures for `gotoNewsFeed()` and `gotoURI()` in `IAppboyNavigator`. Please update your interface implementation if applicable.
- Removed `Appboy.unregisterAppboyPushMessages()`. Please use `AppboyUser.setPushNotificationSubscriptionType()` instead.
- Moved `getAppboyNavigator()` and `setAppboyNavigator()` from `Appboy.java` to `AppboyNavigator.java`.
- The Braze Baidu China Push integration now uses the Baidu `channelId` as the push token. Please update your push token registration code to pass `channelId` instead of `userId` into `Appboy.registerAppboyPushMessages()`. The China Push sample has been updated.
- Removed the `wearboy` and `wear-library` modules. Android Wear 1.0 is no longer supported. Please remove `AppboyWearableListenerService` from your `AndroidManifest.xml` if applicable.

##### Added
- Added a javascript interface to HTML in-app messages	with ability to	log custom events, purchases, user attributes, navigate users, and close the messaage.
- Added the ability to set a single delegate object to custom handle all Uris opened by Braze across in-app messages, push, and the news feed. Your delegate object should implement the `IAppboyNavigator` interface and be set using `AppboyNavigator.setAppboyNavigator()`.
  - See https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/main/java/com/appboy/sample/CustomAppboyNavigator.java for an example implementation.
  - You must also provide instructions for Braze to navigate to your app's (optional) news feed implementation. To use Braze's default handling, call `AppboyNavigator.executeNewsFeedAction(context, uriAction);`.
  - Note: Previously, `AppboyNavigator` was only used when opening in-app messages.

##### Changed
- Removed the need to manually add declarations for Braze's news feed and in-app message activities (`AppboyFeedActivity` and `AppboyWebViewActivity`) to the app `AndroidManifest.xml`. If you have these declarations in your manifest, they can be safely removed.
- Push notifications with web url click actions now open in an in-app webview instead of the external mobile web browser when clicked.

## 1.19.0

##### Added
- Added support for registering geofences with Google Play Services and messaging on geofence events. Please reach out to success@braze.com for more information about this feature.

##### Removed
- Support for share type notification action buttons and custom notification action buttons was removed.

##### Changed
- Push deep links that can be handled by the current app are automatically opened using the current app. Previously, if another app could handle the deep link as well, a chooser dialog would open.
  - Thanks to [catacom](https://github.com/catacom)
  - See https://github.com/Appboy/appboy-android-sdk/pull/71
- `AppboyImageUtils.storePushBitmapInExternalStorage()` has been deprecated.

## 1.18.0

##### Breaking
- Renamed the `android-sdk-jar` artifact in the `gh-pages` branch to `android-sdk-base` and changed its format from `jar` to `aar`. Most integrations depend on `android-sdk-ui` and won't need to take any action.
  - Note: If you were compiling `android-sdk-jar` in your `build.gradle`, you must now compile `android-sdk-base`.

##### Added
- Added the ability to set custom read and unread icons for News Feed cards. To do so, override the `Appboy.Cards.ImageSwitcher` style in your `styles.xml` and add `appboyFeedCustomReadIcon` and `appboyFeedCustomUnReadIcon` drawable attributes.
- Added a sample app showcasing the FCM + Braze push integration. See `/samples/firebase-push`.
- Added a sample app for manual session integration. See `/samples/manual-session-integration`.

##### Removed
- Removed the `-dontoptimize` flag from Braze's UI consumer proguard rules. See https://github.com/Appboy/appboy-android-sdk/blob/master/android-sdk-ui/appboy-proguard-rules.pro for the latest Proguard config.
  - Thanks to [mnonnenmacher](https://github.com/mnonnenmacher)
  - See https://github.com/Appboy/appboy-android-sdk/pull/69

##### Changed
- Updated the Droidboy project to use the conventional Android Build System folder structure.

## 1.17.0

##### Breaking
- Added the ability to configure Braze completely at runtime using `Appboy.configure()`. Values set at runtime take precedence over their counterparts in `appboy.xml`. A complete example of Braze runtime configuration is available in our Hello Appboy sample app's [application class](https://github.com/Appboy/appboy-android-sdk/blob/master/hello-appboy/src/main/java/com/appboy/helloworld/HelloAppboyApplication.java).
  - Renamed `com.appboy.configuration.XmlAppConfigurationProvider` to `com.appboy.configuration.AppboyConfigurationProvider`.
  - `Appboy.configure(String)` changed to `Appboy.configure(Context, AppboyConfig)`.  To maintain parity, replace your current usage with the following equivalent snippit:
  ```
  AppboyConfig appboyConfig = new AppboyConfig.Builder()
          .setApiKey("your-api-key")
          .build();
  Appboy.configure(this, appboyConfig);
  ```

##### Fixed
- Fixed an issue where in-app messages triggered off of push clicks wouldn't fire because the push click happened before the in-app message configuration was synced to the device.

##### Changed
- Updated `Appboy.registerAppboyPushMessages()` to flush the subscription to the server immediately.
- Improved the accessibility-mode behavior of in-app messages.

## 1.16.0

##### Added
- Added the ability to toggle outbound network requests from the Braze SDK online/offline. See `Appboy.setOutboundNetworkRequestsOffline()` for more details.

##### Fixed
- Fixed a bug that caused session sealed automatic data flushes to not occur.

##### Removed
- Removed Braze notification action button icons and icon constants.

## 1.15.3

##### Fixed
- Fixed a bug where in-app messages triggered while no activity was registered with `AppboyInAppMessageManager` would be dropped.

## 1.15.2

##### Fixed
- Fixed a bug where in-app messages triggered while no activity was registered with `AppboyInAppMessageManager` would be displayed without assets.

## 1.15.1

##### Added
- Added Hebrew localization strings.

##### Changed
- Improved the initialization time of the Braze SDK.

##### Removed
- Removed fetching of the device hardware serial number as part of device metadata collection.

## 1.15.0

##### Breaking
- Deprecated `AppboyInAppMessageManager.hideCurrentInAppMessage()`. Please use `AppboyInAppMessageManager.hideCurrentlyDisplayingInAppMessage()` instead.

##### Added
- Added the option to handle session tracking and `InAppMessageManager` registration automatically on apps with a minimum supported SDK of API level 14 or above. This is done by registering an `AppboyLifecycleCallbackListener` instance using [`Application.registerActivityLifecycleCallbacks()`](https://developer.android.com/reference/android/app/Application.html#registerActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks)). See the Hello Appboy sample app's [application class](https://github.com/Appboy/appboy-android-sdk/blob/master/hello-appboy/src/main/java/com/appboy/helloworld/HelloAppboyApplication.java) for an example.
- Added support for upgraded in-app messages including image-only messages, improved image sizing/cropping, text scrolling, text alignment, configurable orientation, and configurable frame color.
- Added support for in-app messages triggered on custom event properties, purchase properties, and in-app message clicks.
- Added support for templating event properties within in-app messages.
- Added the ability to optionally open deep links and the main activity of the app automatically when a user clicks a push notification, eliminating the need to write a custom `BroadcastReceiver` for Braze push. To activate, set the boolean property `com_appboy_handle_push_deep_links_automatically` to `true` in your `appboy.xml`. Note that even when automatic deep link opening is enabled, Braze push opened and received intents will still be sent. To avoid double opening, remove your custom `BroadcastReceiver` or modify it to not open deep links.

## 1.14.1

##### Fixed
- Fixed a bug where images in short news and cross promotion News Feed cards would appear too small on high resolution devices. This bug did not affect Fresco users.

##### Changed
- Updated Baidu push service jar from v4.6.2.38 to v5.1.0.48.

## 1.14.0

##### Breaking
- Renamed `disableAllAppboyNetworkRequests()` to `enableMockAppboyNetworkRequestsAndDropEventsMode()` and fixes a bug where calling `Appboy.changeUser()` would cause a network request even in disabled/mocked mode. Note that `enableMockAppboyNetworkRequestsAndDropEventsMode` should only be used in testing environments.

##### Added
- Added the ability to log negatively-priced purchases.
- Added the option to sort News Feed cards based on read/unread status.
- Added a custom News Feed click delegate. To handle News Feed clicks manually, implement `IFeedClickActionListener` and register an instance using `AppboyFeedManager.getInstance().setFeedCardClickActionListener()`.  This enables use-cases such as selectively using the native browser to open web links.

##### Changed
- Added the ability to include file separators in User Ids.
- Changes Braze's default Log Level from VERBOSE to INFO. Previously disabled debug log statements are enabled and available for debugging. To change Braze's Log Level, update the value of `AppboyLogger.LogLevel`, e.g. `AppboyLogger.LogLevel = Log.VERBOSE`.

##### Removed
- Removed `keep` rules from `consumerProguardFiles` automatic Proguard configuration for potentially improved optimization for client apps. Note that client apps that Proguard Braze code must now store release mapping files for Braze to interpret stack traces. If you would like to continue to `keep` all Braze code, add `-keep class bo.app.** { *; }` and `-keep class com.appboy.** { *; }` to your Proguard configuration.
  - See https://github.com/Appboy/appboy-android-sdk/issues/54
- Removed `onRetainInstance()` from the Braze News Feed fragment. As a result, the News Feed may be used in nested fragments.

## 1.13.5

##### Added
- Defined `com_appboy_card_background` to provide simpler control of news feed card background color.
- Added a convenience method to `Month` to allow direct instantiation from a month integer.

##### Fixed
- Fixed a database access race condition in changeUser code.
  - See https://github.com/Appboy/appboy-android-sdk/issues/52 and https://github.com/Appboy/appboy-android-sdk/issues/39

##### Removed
- Removed optimizations from the private library's Proguard configuration to allow dexing Braze with Jack and Android Gradle Plugin 2.2.0+.

## 1.13.4

##### Added
- Added ability to set push and email subscription state from Droidboy.

##### Changed
- Open sourced Braze's Unity plugin library code.

## 1.13.3

##### Added
- Added the ability to set the large notification icon from within the GCM payload.
- Added `consumerProguardFiles` automatic Proguard configuration.

##### Fixed
- Fixed a bug where triggered HTML in-app messages would not always send button analytics.

##### Changed
- Updated Baidu push service jar from v4.3.0.4 to v4.6.2.38.
- Updated to log analytics for in-app messages and in-app message buttons with 'NONE' click actions.
- Updated the Droidboy sample app to use material design.
- Updated the Hello Appboy sample app to use Proguard.

## 1.13.2

##### Fixed
- Fixed bug where passing a `JSONObject` with multiple invalid keys or values to the `AppboyProperties` constructor would cause a `ConcurrentModificationException`.

## 1.13.1

##### Fixed
- Added handling to a case where certain devices were returning null Resources for GCM BroadcastReceiver onReceive contexts.

## 1.13.0

##### Added
- Added support for action-based, locally triggered in-app messages. In-app messages are now sent to the device at session start with associated trigger events. The SDK will display in-app messages in near real-time when the trigger event associated with a message occurs. Trigger events can be app opens, push opens, purchases, and custom events.

##### Changed
- Deprecated the old system of requesting in-app message display, now collectively known as 'original' in-app messaging, where messages were limited to displaying at app start.

##### Removed
- Removed Iab billing example code from Droidboy.

## 1.12.0

##### Breaking
- Removed the deprecated method `Appboy.requestSlideupRefresh()`.  Please use `Appboy.requestInAppMessageRefresh()` instead.
- Removed the deprecated class AppboySlideupManager.  Please use AppboyInAppMessageManager instead.

##### Changed
- HTML in-app message WebViews now use wide viewport mode and load pages in overview mode.
- Moved `AppboyImageUtils` to the private library with an updated api.
- Moved `WebContentUtils` to the private library.
- Renamed `IInAppMessageHtmlBase` to `InAppMessageHtmlBase`.
- Method count of the private Braze library has decreased by over 600 since version 1.11.0.

##### Removed
- Removed the partial duplicate of the private library's StringUtils from the ui project.

## 1.11.2

##### Fixed
- Fixed bug where large and small icons both rendered at full size in notification remoteviews for Honeycomb/ICS.  Now, if a large icon is available, only the large icon is shown.  Otherwise, the small icon is used.
- Fixed bug where push open logs were under-reported under certain device conditions.

## 1.11.1
- Placeholder for Unity release.

## 1.11.0

##### Added
- Creates Activity based Unity in-app messages (fixing an issue where touches on in-app messages were hitting the game behind the in-app message) and removes redundant Unity permissions.
- Added a method for setting modal frame color on in-app messages, no longer displays in-app messages on asset download failure and adds robustness.
- Added deep link support to `AppboyUnityGcmReceiver`.

##### Changed
- Makes the WebView background for HTML in-app messages transparent.  Ensure your HTML in-app messages expect a transparent background.
- Updated Google Play Services from to 7.5.0 to 8.3.0 and Play Services Support from 1.2.0 to 1.3.0.
  - See https://github.com/Appboy/appboy-android-sdk/issues/45
- Updated Braze WebView to support redirects to deep links and enables DOM storage.

## 1.10.3

##### Added
- Added Android M Support.  Under the runtime permissions model introduced in Android M, location permission must be explicitly obtained from the end user by the integrating app.  Once location permission is granted, Braze will resume location data collection on the subsequent session.

## 1.10.2

##### Added
- Added the ability to log a custom event from an HTML in-app message. To log a custom event from an HTML in-app message, navigate a user to a url of the form `appboy://customEvent?name=customEventName&p1=v2`, where the `name` URL parameter is the name of the event, and the remaining parameters are logged as String properties on the event.

## 1.10.1

##### Changed
- Enabled javascript in HTML in-app messages.
- Deprecated `logShare()` and `setBio()` in the public interface as support in the Braze dashboard has been removed.

## 1.10.0

##### Fixed
- Fixed an issue where applications in extremely resource starved environments were seeing ANRs from the periodic dispatch `BroadcastReceiver`.  This was not a bug in the Braze code, but a symptom of a failing application.  This updates our periodic dispatch mechanism so it won't have this symptomatic behavior, which in some cases should help developers track down the source of the actual issue (depending on the bug).  Apps that only use the Braze jar file will now have to register `<service android:name="com.appboy.services.AppboyDataSyncService"/>` in their `AndroidManifest.xml` to enable Braze to periodically flush data.
- Fixed a very rare issue where calling `Context.checkCallingOrSelfPermission()` would cause an exception to be thrown on certain custom Android builds.

##### Changed
- Updated the News Feed to not show cards in the local cache that have expired.

## 1.9.2

##### Fixed
- Fixed bug triggered when `AppboyWearableListenerService` was not registered.

## 1.9.0

##### Breaking
- All users must add the line `-dontwarn com.google.android.gms.**` to their proguard config file if using proguard.
  - See https://github.com/Appboy/appboy-android-sdk/issues/43

##### Added
- Added support for analytics from Android Wear devices.
- Added support for displaying notification action buttons sent from the Braze dashboard.  To allow image sharing on social networks, add the `<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />` permission to your `AndroidManifest.xml`.
- Added delegate to `FeedbackFinishedListener` enabling modification of feedback messages before they are sent to Appboy.  Also adds a disposition parameter to `onFeedbackFinished()`.
- Added support for GIF images in the News Feed and in In-App Messages via the Facebook Fresco image library (version 0.6.1) as a provided library. If found in the parent app (your app), images and GIFs will be loaded using views from the Fresco library. In order to display GIFs, Fresco must be added as a dependency in the parent app. If not found in the parent app, News Feed cards and In-App Messages will not display GIFs. To disable use of the Fresco library in the UI project, set the value of `com_appboy_enable_fresco_library_use` to false (or omit it) in your `appboy.xml`; to enable Fresco use set `com_appboy_enable_fresco_library_use` to true in your `appboy.xml`. ImageView specific attributes for News Feed cards and In-App Messages, such as `scaleType`, must now be applied programmatically instead of being applied from `styles.xml`. If using Fresco and proguarding your app, please include http://frescolib.org/docs/proguard.html with your proguard config. If you are not using Fresco, add the `dontwarn com.appboy.ui.**` directive. Note: to use Fresco with Braze it must be initialized when your application launches.
- Added explicit top and bottom padding values for In-App Message buttons to improve button rendering on some phones.  See the `Appboy.InAppMessage.Button` style in `styles.xml`.
- Added HTML In-App Message types. HTML In-App Messages consist of html along with an included zipped assets file to locally reference images, css, etc. See `CustomHtmlInAppMessageActionListener` in our Droidboy sample app for an example listener for the callbacks on the actions inside the WebView hosting the HTML In-App Message.
- Added a `setAttributionData()` method to AppboyUser that sets an AttributionData object for the user. Use this method with attribution provider SDKs when attribution events are fired.

##### Changed
- Removed the need for integrating client apps to log push notifications inside their activity code.  **Please remove all calls to `Appboy.logPushNotificationOpened()` from your app as they are now all handled automatically by Braze.  Otherwise, push opens will be incorrectly logged twice.**
- In-App Message views are now found in the `com.appboy.ui.inappmessage.views` package and In-App Message listeners are now found in the `com.appboy.ui.inappmessage.listeners` package.

## 1.8.2

##### Added
- Added the ability to specify custom fonts for in-app message ui elements via the `appboyInAppMessageCustomFontFile` custom xml attribute.
- Increases the number of supported currency codes from 22 to 171.  All common currency codes are now supported. The full list of supported codes is available at our <a href="https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/IAppboy.html#logPurchase(java.lang.String,%20java.lang.String,%20java.math.BigDecimal,%20int,%20com.appboy.models.outgoing.AppboyProperties)">Javadoc</a>.
- Added the method `isUninstallTrackingPush()` to AppboyNotificationUtils to be able to detect background push sent for Braze uninstall tracking.

##### Changed
- Updated `BigPictureStyle` to show message in expanded view if summary is not present (after 1.7.0 a summary was required in expanded view to have text appear).

## 1.8.1
- Internal release for Xamarin, adds `AppboyXamarinFormsFeedFragment`.

## 1.8.0

##### Breaking
- Updated the minimum sdk version from 8 (froyo) to 9 (gingerbread).

##### Added
- Added an opt-in location service that logs background location events.

##### Fixed
- Fixed an in-app message lifecycle listener bug where certain lifecycle events could be fired twice.

## 1.7.3

##### Added
- Added Braze logging configurability by setting the AppboyLogger.LogLevel.  This is intended to be used in development environments and should not be set in a released application as logging statements are essential for debugging.
  - See https://github.com/Appboy/appboy-android-sdk/issues/38
- Added `getAppboyPushMessageRegistrationId()` to the Braze interface to enable retrieval of the GCM/ADM/Baidu registration ID Braze has set for the device.

##### Changed
- Updated our libraries to build against API level 22.
- Blacklisted custom attributes may no longer be incremented.

## 1.7.2

##### Added
- Introduced `AppboyNotificationUtils.getAppboyExtrasWithoutPreprocessing()` to parse Braze extras from GCM/ADM intent extras directly rather than requiring Braze extras to be parsed into a Bundle before being passed into `AppboyNotificationUtils.getAppboyExtras()`.
- Added the ability to send and retrieve extra key-value pairs via a News Feed card.
- Added the ability to define custom key-value properties on a custom event or purchase.  Property keys are strings and values may be strings, doubles, ints, booleans, or `java.util.Date` objects.

##### Removed
- Removed `DownloadUtils.java` from `com.appboy.ui.support`.  The `downloadImageBitmap()` function has been moved to `com.appboy.support.AppboyImageUtils`.

## 1.7.1

##### Added
- Upgrades Droidboy's custom user attributes and purchases capability and refactors the settings page.

##### Removed
- Removed requirement to manually integrate Font Awesome into the client app's /assets folder for in-app messages with icons.

## 1.7.0

##### Breaking
- Added summary subtext in `BigView` style notifications.  This is a breaking change in `BigView` style notification display.  Previously the summary text in `BigView` style notifications was set to the bundle/dashboard summary text if it was present, or the alert message otherwise.  Now the bundle/dashboard summary text is used to set the message subtext, which results in the bundle/dashboard summary text being shown in both the collapsed and expanded views.  See our updated push previews for a visualization of this change.

##### Added
- Added the ability to set a custom `IAppboyNotificationFactory` to customize push using `Appboy.setCustomAppboyNotificationFactory()`.
- Added the ability to override title and summary in `BigView` push notifications.
- Added the ability to set a default large icon for push messages by adding the `com_appboy_push_large_notification_icon` drawable resource to your `appboy.xml`.
- Added support for modal and full screen style in-app messages.  Also adds support for including fontawesome icons and images with in-app messages, changing colors on in-app message UI elements, expanded customization options, and message resizing for tablets.  Please visit our documentation for more information.
- Added a sample application (China Sample App) which integrates Baidu Cloud Push and Braze for sending push messages through Braze to devices without Google Services installed.
- Added `AppboyNotificationUtils.logBaiduNotificationClick()`, a utility method for logging push notification opens from push messages sent via Baidu Cloud Push by Braze.

##### Changed
- Refactors AppboyNotificationUtils into multiple classes in the com.appboy.push package and the AppboyImageUtils class in com.appboy.

## 1.6.2

##### Added
- Added a major performance upgrade that reduces CPU usage, memory footprint, and network traffic.
- Added 26 additional languages to localization support for Braze UI elements.
- Added local blocking for blacklisted custom attributes, events, and purchases.  However, blacklisted attributes may still be incremented (removed in release 1.7.3).
- Added the ability to set the accent color for notification in Android Lollipop and above.  This can be done by setting the `com_appboy_default_notification_accent_color` integer in your `appboy.xml`.
- Updated the News Feed to render wider on tablet screens.
- Added swipe handling for in-app messages on APIs <= 11.

##### Changed
- Updated our UI library to build against API level 21.

## 1.6.1

##### Fixed
- Fixed a timezone bug where short names were used for lookup, causing the default timezone (GMT) to be set in cases where the short name was not equal to the time zone Id.
- Fixed a bug where multiple pending push intents could override each other in the notification center.

## 1.6.0

##### Fixed
- Fixed News Feed swipe-refresh `CalledFromWrongThreadException`.

##### Changed
- Updated the android-L preview support from version 1.5.2 to support the public release of Android 5.0.  Updates the v4 support library dependency to version 21.0.0.
- `android.permission.GET_ACCOUNTS` is no longer required during initial GCM registration for devices running Jelly Bean and higher.  However, use of this permissions is recommended so that pre-Jelly Bean devices can register with GCM.
- `android.permission.WAKE_LOCK` is no longer required during initial GCM registration.  However, use of this permissions is recommended to allow notifications to wake the screen and engage users when the notification arrives.
- No longer overwrite messages in the notification center based on collapse key (GCM) or consolidation key (ADM).  Instead, overwrite based on message title and message alert, or, if specified, a custom notification id.
- Updated Droidboy to use the most recent Google IAB helper classes.

## 1.5.5

##### Added
- Added support for displaying Kindle notifications with images.

##### Changed
- Notifications with a minimum priority specified no longer trigger the device wakelock because Android does not display them in the status bar (they appear silently in the drawer).

##### Removed
- Removed styleable elements from the UI project. This should have no impact on consuming projects.

## 1.5.4

##### Added
- Incubates a feature to allow for runtime changes to be made to the API key. Please contact android@braze.com if you want to test this feature.
- Added support for Big View text summaries, allowing summary text to be displayed under the main text in a notification.
- Added support for custom URIs to open when a notification is clicked.
- Added support for notification duration control.  When specified, sets an alarm to remove a notification from the notification center after the specified duration.
- Added support for notification sounds.  Users can specify a notification sound URI to play with the notification.
- Added support for changing In-App Message duration from the client app.  To do this, you can modify the slideup object passed to you in the `onReceive()` delegate using the new setter method `IInAppMessage.setDurationInMilliseconds()`.

##### Changed
- Updated `AppboyWebViewActivity` to always fill the parent view.  This forces some previously problematic websites to render at the correct size.

## 1.5.3

##### Added
- Added the ability to turn off Braze's automatic location collection using the `com_appboy_disable_location_collection` boolean in `appboy.xml`.
- Added the ability to send location tracking events to Braze manually using setLastKnownLocation on the AppboyUser.  This is intended to be used with `com_appboy_disable_location_collection` set to true so that locations are only being recorded from a single source.

## 1.5.2

##### Added
- Added support for GCM and ADM messages without collapse keys.
- Added support for GCM and ADM messages with notification priorities.
- Enabled setting a registration ID without a full push setup; `registerAppboyGcmMessages()` and `registerAppboyPushMessages()` no longer throw null pointer exceptions if Braze isn't correctly configured to display push messages.
- Enabled `AppboyWebViewActivity` to download items.
- Added support for apps built targeting android-L. Braze's process for registering push notifications had previously used an implicit service intent which caused a runtime error. Any apps built against android-L will need to upgrade to this version. However, apps with Braze that are/were built against any other versions of Android will run without issue on android-L. Thus, this is not an urgent upgrade unless you're working with android-L.

##### Removed
- Removed extraneous features from Droidboy so it's more easily digestible as a sample application.

## 1.5.1

##### Removed
- Removed obfuscation from parameter names on public models.

## 1.5.0

##### Added
- Added Kindle Fire support and ADM support.
- Added read/unread visual indicators to newsfeed cards. Use the configuration boolean com_appboy_newsfeed_unread_visual_indicator_on in appboy.xml to enabled the indicators.  Additionally, moved the `logFeedCardImpression()` and `logFeedCardClick()` methods to the card objects themselves.
- Added support to image loading in CaptionedImage and Banner cards for dynamic resizing after loading the image url; supports any aspect ratio.
- Added Hello Appboy sample project that shows a minimal use case of the Braze SDK.
- Added wake lock to `AppboyGcmReceiver` in the UI project. When the `WAKE_LOCK` permission is set, the screen will be turned on when a notification is received.

##### Changed
- Moved constants from `AppboyGcmReceiver` (ie: `APPBOY_GCM_NOTIFICATION_TITLE_ID`, etc.) into new `AppboyNotificationUtils` class.
- Restricted productId to 255 characters for `Appboy.logPurchase()`.

## 1.4.3

##### Removed
- Removed org.json classes from appboy.jar.

## 1.4.2

##### Added
- Added summary text for push image notifications.
- Added a new constant, `APPBOY_LOG_TAG_PREFIX`, for logging which includes the sdk version number.

## 1.4.1

##### Added
- Added automatic tests to verify that the sdk has integrated correctly.
- Added an optional quantity amount to in-app-purchases.

##### Changed
- Changed the device identifier from the device persistent `ANDROID_ID` to a non device persistent identifier for compliance with the new Google Play Terms of Service.

##### Removed
- Removed default max length and ellipsize properties in the `styles.xml`. The old defaults were set to 5 for maxLines for  newsfeed cards and ellipsize 'end'.

## 1.4.0

##### Added
- Added categories.
- Added swipe to refresh functionality to the newsfeed. The swipe to refresh colors are configurable in the colors xml file.
- Added configurable session timeout to the `appboy xml`.
- Added images to GCM push notifications.
- Added email and push notification subscription types for a user. Subscription types are explicitly opted in, subscribed, and unsubscribed. The old email boolean subscribe method has been deprecated.

##### Changed
- The feedback form now displays error popups to the user on invalid fields.

##### Removed
- Removed click logging on slideups when action is `None`.

## 1.3.4

##### Changed
- Minor changes to address some Lint issues in the UI project.
- Updated the open source AppboyGcmReceiver to use references to R.java for resource identifiers. This became possible when we moved AppboyGcmReceiver.java into the android-sdk-ui project (from the base library JAR).

## 1.3.3

##### Fixed
- Minor bug fix for a crash that occurred in certain conditions where the News Feed cards were replaced with a smaller set of cards.

## 1.3.2

##### Fixed
- Fixed a few minor style issues to be closer in line with Eclipse's preferences.
- Fixed a potential synchronization issue with the AppboyListAdapter.
- Added the ability to set the avatar image URL for your users.
- Fixed support for protocol URLs and adds an ActivityAction overload that streamlines the use of deep link and web link actions.

##### Changed
- Minor update to Chinese language translation.
- Moved com.appboy.AppboyGcmReceiver to the open source android-sdk-ui project. Also moves some of the constants previously available as AppboyGcmReceiver.* to com.appboy.constants.APPBOY_GCM_*. The CAMPAIGN_ID_KEY previously used in our sample app is still available in com.appboy.AppboyGcmReceiver, but if you were using other constants, you'll have to move the references.
- Removed input validation on custom attribute key names so that you can use foreign characters and spaces to your heart's desire. Just don't go over the max character limit.

## 1.3.1

##### Changed
- Updated to version 1.9.1 of Android-Universal-Image-Loader.
- Added Chinese language translations.
- Minor cleanup to imports.

## 1.3

Braze version 1.3 provides a substantial upgrade to the slideup code and reorganization for better flexibility moving forward, but at the expense of a number of breaking changes. We've detailed the changes in this changelog and hope that you'll love the added power, increased flexibility, and improved UI that the new Braze slideup provides. If you have any trouble with these changes, feel free to reach out to success@braze.com for help, but most migrations to the new code structure should be relatively painless.

##### Breaking
New AppboySlideupManager
- The AppboySlideupManager has moved to `com.appboy.ui.slideups.AppboySlideupManager.java`.
- An `ISlideupManagerListener` has been provided to allow the developer to control which slideups are displayed, when they are displayed, as well as what action(s) to perform when a slideup is clicked or dismissed.
  - The slideup `YOUR-APPLICATION-PACKAGE-NAME.intent.APPBOY_SLIDEUP_CLICKED` event has been replaced by the `ISlideupManagerListener.onSlideupClicked(Slideup slideup, SlideupCloser slideupCloser)` method.
- Added the ability to use a custom `android.view.View` class to display slideups by providing an `ISlideupViewFactory`.
- Default handling of actions assigned to the slideup from the Braze dashboard.
- Slideups can be dismissed by swiping away the view to either the left or the right. (Only on devices running Honeycomb Android 3.1 or higher).
  - Any slideups that are created to be dismissed by a swipe will automatically be converted to auto dismiss slideups on devices that are not running Android 3.1 or higher.

Slideup model
- A key value `extras` java.util.Map has been added to provide additional data to the slideup. `Extras` can be on defined on a per slideup basis via the dashboard.
- The `SlideFrom` field defines whether the slideup originates from the top or the bottom of the screen.
- The `DismissType` property controls whether the slideup will dismiss automatically after a period of time has lapsed, or if it will wait for interaction with the user before disappearing.
  - The slideup will be dismissed automatically after the number of milliseconds defined by the duration field have elapsed if the slideup's DismissType is set to AUTO_DISMISS.
- The ClickAction field defines the behavior after the slideup is clicked: display a news feed, redirect to a uri, or nothing but dismissing the slideup. This can be changed by calling any of the following methods: `setClickActionToNewsFeed()`, `setClickActionToUri(Uri uri)`, or `setClickActionToNone()`.
- The uri field defines the uri string that the slide up will open when the ClickAction is set to URI. To change this value, use the `setClickActionToUri(Uri uri)` method.
- Convenience methods to track slideup impression and click events have been added to the `com.appboy.models.Slideup` class.
  - Impression and click tracking methods have been removed from `IAppboy.java`.
- A static `createSlideup` method has been added to create custom slideups.

IAppboyNavigator
- A custom `IAppboyNavigator` can be set via `IAppboy.setAppboyNavigator(IAppboyNavigator appboyNavigator)` which can be used to direct your users to your integrated Braze news feed when certain slideups are clicked. This provides a more seamless experience for your users. Alternatively, you can choose not to provide an IAppboyNavigator, but instead register the new `AppboyFeedActivity` class in your `AndroidManifest.xml` which will open a new Braze news feed Activity when certain slideups are clicked.

Other
- A new base class, `AppboyBaseActivity`, has been added that extends `android.app.Activity` and integrates Braze session and slideup management.
- A drop-in `AppboyFeedActivity` class has been added which can be used to display the Braze News Feed.

## 1.2.1

##### Fixed
- Fixed a ProGuard issue.

## 1.2

##### Added
- Introduced two new card types (Banner card and Captioned Image card).
- Added support for sending down key/value pairs as part of a GCM message.

##### Fixed
- Minor bug fixes.

## 1.1

##### Added
- Added support for reporting purchases in multiple currencies.

##### Fixed
- Fixed a bug in caching custom events to a SQLite database.
- Fixed a validation bug when logging custom events.

##### Changed
- Deprecated `IAppboy.logPurchase(String, int)`.

## 1.0
- Initial release
