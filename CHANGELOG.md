## 1.15.0
- Adds the ability for session tracking and `InAppMessageManager` registration to be handled automatically via `Activity` lifecycle methods on API level 14 and above. See the Hello Appboy sample for an example.
- Adds support for upgraded in-app messages including image-only messages, improved image sizing/cropping, text scrolling, text alignment, configurable orientation, and configurable frame color.
- Adds support for in-app messages triggered on custom event properties, purchase properties, and in-app message clicks.
- Adds support for templating event properties within in-app messages.
- Adds the ability to optionally open deep links and the main activity of the app automatically when a user clicks a push notification, eliminating the need to write a custom `BroadcastReceiver` for Appboy push. To activate, set the boolean property `com_appboy_handle_push_deep_links_automatically` to `true` in your `appboy.xml`. Note that even when automatic deep link opening is enabled, Appboy push opened and received intents will still be sent. To avoid double opening, remove your custom `BroadcastReceiver` or modify it to not open deep links.
- Deprecates `AppboyInAppMessageManager.hideCurrentInAppMessage()`. Please use `AppboyInAppMessageManager.hideCurrentlyDisplayingInAppMessage()` instead.

## 1.14.1
- Fixes a bug where images in short news and cross promotion News Feed cards would appear too small on high resolution devices. This bug did not affect Fresco users.
- Updates Baidu push service jar from v4.6.2.38 to v5.1.0.48.

## 1.14.0
- Removes `keep` rules from `consumerProguardFiles` automatic Proguard configuration for potentially improved optimization for client apps. Note that client apps that Proguard Appboy code must now store release mapping files for Appboy to interpret stack traces. If you would like to continue to `keep` all Appboy code, add `-keep class bo.app.** { *; }` and `-keep class com.appboy.** { *; }` to your Proguard configuration.
- Removes `onRetainInstance()` from the Appboy News Feed fragment. As a result, the News Feed may be used in nested fragments.
- Adds the ability to log negatively-priced purchases.
- Allows the inclusion of file separators in User Ids.
- Adds the option to sort News Feed cards based on read/unread status.
- Adds a custom News Feed click delegate. To handle News Feed clicks manually, implement `IFeedClickActionListener` and register an instance using `AppboyFeedManager.getInstance().setFeedCardClickActionListener()`.  This enables use-cases such as selectively using the native browser to open web links.
- Changes Appboy's default Log Level from VERBOSE to INFO. Previously disabled debug log statements are enabled and available for debugging. To change Appboy's Log Level, update the value of `AppboyLogger.LogLevel`, e.g. `AppboyLogger.LogLevel = Log.VERBOSE`.
- Renames `disableAllAppboyNetworkRequests()` to `enableMockAppboyNetworkRequestsAndDropEventsMode()` and fixes a bug where calling `Appboy.changeUser()` would cause a network request even in disabled/mocked mode. Note that `enableMockAppboyNetworkRequestsAndDropEventsMode` should only be used in testing environments.

## 1.13.5
- Defines `com_appboy_card_background` to provide simpler control of news feed card background color.
- Removes optimizations from the private library's Proguard configuration to allow dexing Appboy with Jack and Android Gradle Plugin 2.2.0+.
- Adds a convenience method to `Month` to allow direct instantiation from a month integer.
- Fixes a database access race condition in changeUser code.

## 1.13.4
- Adds ability to set push and email subscription state from Droidboy.
- Open sources Appboy's Unity plugin library code.

## 1.13.3
- Updates Baidu push service jar from v4.3.0.4 to v4.6.2.38.
- Analytics are now logged for in-app messages and in-app message buttons with 'NONE' click actions.
- Adds the ability to set the large notification icon from within the GCM payload.
- Fixes a bug where triggered HTML in-app messages would not always send button analytics.
- Updates the Droidboy sample app to use material design.
- Updates the Hello Appboy sample app to use Proguard.
- Introduces `consumerProguardFiles` automatic Proguard configuration.

## 1.13.2
- Fixes bug where passing a `JSONObject` with multiple invalid keys or values to the `AppboyProperties` constructor would cause a `ConcurrentModificationException`.

## 1.13.1
- Adds handling to a case where certain devices were returning null Resources for GCM BroadcastReceiver onReceive contexts.

## 1.13.0
- Adds support for action-based, locally triggered in-app messages. In-app messages are now sent to the device at session start with associated trigger events. The SDK will display in-app messages in near real-time when the trigger event associated with a message occurs. Trigger events can be app opens, push opens, purchases, and custom events. 
- Deprecates the old system of requesting in-app message display, now collectively known as 'original' in-app messaging, where messages were limited to displaying at app start. 
- Removes Iab billing example code from Droidboy.

## 1.12.0
- Removes the deprecated method `Appboy.requestSlideupRefresh()`.  Please use `Appboy.requestInAppMessageRefresh()` instead.
- Removes the deprecated class AppboySlideupManager.  Please use AppboyInAppMessageManager instead.
- HTML in-app message WebViews now use wide viewport mode and load pages in overview mode.
- Removes the partial duplicate of the private library's StringUtils from the ui project.
- Moves `AppboyImageUtils` to the private library with an updated api.
- Moves `WebContentUtils` to the private library.
- Renames `IInAppMessageHtmlBase` to `InAppMessageHtmlBase`.
- Method count of the private Appboy library has decreased by over 600 since version 1.11.0.

## 1.11.2
- Fixes bug where large and small icons both rendered at full size in notification remoteviews for Honeycomb/ICS.  Now, if a large icon is available, only the large icon is shown.  Otherwise, the small icon is used.
- Fixes bug where push open logs were under-reported under certain device conditions.

## 1.11.1
- Placeholder for Unity release.

## 1.11.0
- Makes the WebView background for HTML in-app messages transparent.  Ensure your HTML in-app messages expect a transparent background.
- Updates Google Play Services from to 7.5.0 to 8.3.0 and Play Services Support from 1.2.0 to 1.3.0.
- Creates Activity based Unity in-app messages (fixing an issue where touches on in-app messages were hitting the game behind the in-app message) and removes redundant Unity permissions.
- Updates Appboy WebView to support redirects to deep links and enables DOM storage.
- Adds a method for setting modal frame color on in-app messages, no longer displays in-app messages on asset download failure and adds robustness.
- Adds deep link support to `AppboyUnityGcmReceiver`.

## 1.10.3
- Adds Android M Support.  Under the runtime permissions model introduced in Android M, location permission must be explicitly obtained from the end user by the integrating app.  Once location permission is granted, Appboy will resume location data collection on the subsequent session.

## 1.10.2
- Adds the ability to log a custom event from an HTML in-app message. To log a custom event from an HTML in-app message, navigate a user to a url of the form `appboy://customEvent?name=customEventName&p1=v2`, where the `name` URL parameter is the name of the event, and the remaining parameters are logged as String properties on the event.

## 1.10.1
- Enables javascript in HTML in-app messages.
- Deprecates `logShare()` and `setBio()` in the public interface as support in the Appboy dashboard has been removed.

## 1.10.0
- Fixes an issue where applications in extremely resource starved environments were seeing ANRs from the periodic dispatch `BroadcastReceiver`.  This was not a bug in the Appboy code, but a symptom of a failing application.  This updates our periodic dispatch mechanism so it won't have this symptomatic behavior, which in some cases should help developers track down the source of the actual issue (depending on the bug).  Apps that only use the Appboy jar file will now have to register `<service android:name="com.appboy.services.AppboyDataSyncService"/>` in their `AndroidManifest.xml` to enable Appboy to periodically flush data.
- Updates the News Feed to not show cards in the local cache that have expired.
- Fixes a very rare issue where calling `Context.checkCallingOrSelfPermission()` would cause an exception to be thrown on certain custom Android builds.

## 1.9.2
- Fixes bug triggered when `AppboyWearableListenerService` was not registered.

## 1.9.0
- Removes the need for integrating client apps to log push notifications inside their activity code.  **Please remove all calls to `Appboy.logPushNotificationOpened()` from your app as they are now all handled automatically by Appboy.  Otherwise, push opens will be incorrectly logged twice.**
- Adds support for analytics from Android Wear devices. 
- All users must add the line `-dontwarn com.google.android.gms.**` to their proguard config file if using proguard.
- Adds support for displaying notification action buttons sent from the Appboy dashboard.  To allow image sharing on social networks, add the `<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />` permission to your `AndroidManifest.xml`.
- Adds delegate to `FeedbackFinishedListener` enabling modification of feedback messages before they are sent to Appboy.  Also adds a disposition parameter to `onFeedbackFinished()`.
- Adds support for GIF images in the News Feed and in In-App Messages via the Facebook Fresco image library (version 0.6.1) as a provided library. If found in the parent app (your app), images and GIFs will be loaded using views from the Fresco library. In order to display GIFs, Fresco must be added as a dependency in the parent app. If not found in the parent app, News Feed cards and In-App Messages will not display GIFs. To disable use of the Fresco library in the UI project, set the value of `com_appboy_enable_fresco_library_use` to false (or omit it) in your `appboy.xml`; to enable Fresco use set `com_appboy_enable_fresco_library_use` to true in your `appboy.xml`. ImageView specific attributes for News Feed cards and In-App Messages, such as `scaleType`, must now be applied programmatically instead of being applied from `styles.xml`. If using Fresco and proguarding your app, please include http://frescolib.org/docs/proguard.html with your proguard config. If you are not using Fresco, add the `dontwarn com.appboy.ui.**` directive. Note: to use Fresco with Appboy it must be initialized when your application launches.
- In-App Message views are now found in the `com.appboy.ui.inappmessage.views` package and In-App Message listeners are now found in the `com.appboy.ui.inappmessage.listeners` package.
- Adds explicit top and bottom padding values for In-App Message buttons to improve button rendering on some phones.  See the `Appboy.InAppMessage.Button` style in `styles.xml`.
- Adds HTML In-App Message types. HTML In-App Messages consist of html along with an included zipped assets file to locally reference images, css, etc. See `CustomHtmlInAppMessageActionListener` in our Droidboy sample app for an example listener for the callbacks on the actions inside the WebView hosting the HTML In-App Message.
- Adds a `setAttributionData()` method to AppboyUser that sets an AttributionData object for the user. Use this method with attribution provider SDKs when attribution events are fired. 

## 1.8.2
- Adds the ability to specify custom fonts for in-app message ui elements via the `appboyInAppMessageCustomFontFile` custom xml attribute.
- Increases the number of supported currency codes from 22 to 171.  All common currency codes are now supported. The full list of supported codes is available at our <a href="https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/IAppboy.html#logPurchase(java.lang.String,%20java.lang.String,%20java.math.BigDecimal,%20int,%20com.appboy.models.outgoing.AppboyProperties)">Javadoc</a>.
- Adds the method `isUninstallTrackingPush()` to AppboyNotificationUtils to be able to detect background push sent for Appboy uninstall tracking.
- Updates `BigPictureStyle` to show message in expanded view if summary is not present (after 1.7.0 a summary was required in expanded view to have text appear).

## 1.8.1
- Internal release for Xamarin, adds `AppboyXamarinFormsFeedFragment`.

## 1.8.0
- Updates the minimum sdk version from 8 (froyo) to 9 (gingerbread).
- Adds an opt-in location service that logs background location events.
- Fixes an in-app message lifecycle listener bug where certain lifecycle events could be fired twice.

## 1.7.3
- Adds Appboy logging configurability by setting the AppboyLogger.LogLevel.  This is intended to be used in development environments and should not be set in a released application as logging statements are essential for debugging.
- Adds `getAppboyPushMessageRegistrationId()` to the Appboy interface to enable retrieval of the GCM/ADM/Baidu registration ID Appboy has set for the device.
- Updates our libraries to build against API level 22.
- Blacklisted custom attributes may no longer be incremented.

## 1.7.2
- Removes `DownloadUtils.java` from `com.appboy.ui.support`.  The `downloadImageBitmap()` function has been moved to `com.appboy.support.AppboyImageUtils`.
- Introduces `AppboyNotificationUtils.getAppboyExtrasWithoutPreprocessing()` to parse Appboy extras from GCM/ADM intent extras directly rather than requiring Appboy extras to be parsed into a Bundle before being passed into `AppboyNotificationUtils.getAppboyExtras()`.
- Adds the ability to send and retrieve extra key-value pairs via a News Feed card.
- Adds the ability to define custom key-value properties on a custom event or purchase.  Property keys are strings and values may be strings, doubles, ints, booleans, or `java.util.Date` objects.

## 1.7.1
- Removes requirement to manually integrate Font Awesome into the client app's /assets folder for in-app messages with icons.
- Upgrades Droidboy's custom user attributes and purchases capability and refactors the settings page.

## 1.7.0
- Refactors AppboyNotificationUtils into multiple classes in the com.appboy.push package and the AppboyImageUtils class in com.appboy.
- Adds the ability to set a custom `IAppboyNotificationFactory` to customize push using `Appboy.setCustomAppboyNotificationFactory()`.
- Adds the ability to override title and summary in `BigView` push notifications.
- Adds summary subtext in `BigView` style notifications.  This is a breaking change in `BigView` style notification display.  Previously the summary text in `BigView` style notifications was set to the bundle/dashboard summary text if it was present, or the alert message otherwise.  Now the bundle/dashboard summary text is used to set the message subtext, which results in the bundle/dashboard summary text being shown in both the collapsed and expanded views.  See our updated push previews for a visualization of this change.
- Adds the ability to set a default large icon for push messages by adding the `com_appboy_push_large_notification_icon` drawable resource to your `appboy.xml`.
- Adds support for modal and full screen style in-app messages.  Also adds support for including fontawesome icons and images with in-app messages, changing colors on in-app message UI elements, expanded customization options, and message resizing for tablets.  Please visit our documentation for more information.
- Adds a sample application (China Sample App) which integrates Baidu Cloud Push and Appboy for sending push messages through Appboy to devices without Google Services installed.
- Adds `AppboyNotificationUtils.logBaiduNotificationClick()`, a utility method for logging push notification opens from push messages sent via Baidu Cloud Push by Appboy.

## 1.6.2
- Updates our UI library to build against API level 21. 
- Adds a major performance upgrade that reduces CPU usage, memory footprint, and network traffic. 
- Adds 26 additional languages to localization support for Appboy UI elements.
- Adds local blocking for blacklisted custom attributes, events, and purchases.  However, blacklisted attributes may still be incremented (removed in release 1.7.3).
- Adds the ability to set the accent color for notification in Android Lollipop and above.  This can be done by setting the `com_appboy_default_notification_accent_color` integer in your `appboy.xml`.
- Updates the News Feed to render wider on tablet screens.
- Adds swipe handling for in-app messages on APIs <= 11.

## 1.6.1
- Fixes a timezone bug where short names were used for lookup, causing the default timezone (GMT) to be set in cases where the short name was not equal to the time zone Id.
- Fixes a bug where multiple pending push intents could override each other in the notification center.

## 1.6.0
- Updates the android-L preview support from version 1.5.2 to support the public release of Android 5.0.  Updates the v4 support library dependency to version 21.0.0.
- `android.permission.GET_ACCOUNTS` is no longer required during initial GCM registration for devices running Jelly Bean and higher.  However, use of this permissions is recommended so that pre-Jelly Bean devices can register with GCM.
- `android.permission.WAKE_LOCK` is no longer required during initial GCM registration.  However, use of this permissions is recommended to allow notifications to wake the screen and engage users when the notification arrives.
- No longer overwrite messages in the notification center based on collapse key (GCM) or consolidation key (ADM).  Instead, overwrite based on message title and message alert, or, if specified, a custom notification id.
- Fixes News Feed swipe-refresh `CalledFromWrongThreadException`.
- Updates Droidboy to use the most recent Google IAB helper classes.

## 1.5.5
- Adds support for displaying Kindle notifications with images.
- Notifications with a minimum priority specified no longer trigger the device wakelock because Android does not display them in the status bar (they appear silently in the drawer).
- Removes styleable elements from the UI project. This should have no impact on consuming projects.

## 1.5.4
- Incubates a feature to allow for runtime changes to be made to the API key. Please contact android@appboy.com if you want to test this feature.
- Adds support for Big View text summaries, allowing summary text to be displayed under the main text in a notification.
- Adds support for custom URIs to open when a notification is clicked.
- Adds support for notification duration control.  When specified, sets an alarm to remove a notification from the notification center after the specified duration.
- Adds support for notification sounds.  Users can specify a notification sound URI to play with the notification.
- Adds support for changing In-App Message duration from the client app.  To do this, you can modify the slideup object passed to you in the `onReceive()` delegate using the new setter method `IInAppMessage.setDurationInMilliseconds()`.
- Updates `AppboyWebViewActivity` to always fill the parent view.  This forces some previously problematic websites to render at the correct size.

## 1.5.3
- Adds the ability to turn off Appboy's automatic location collection using the `com_appboy_disable_location_collection` boolean in `appboy.xml`.
- Adds the ability to send location tracking events to Appboy manually using setLastKnownLocation on the AppboyUser.  This is intended to be used with `com_appboy_disable_location_collection` set to true so that locations are only being recorded from a single source.

## 1.5.2
- Adds support for GCM and ADM messages without collapse keys.
- Adds support for GCM and ADM messages with notification priorities.
- Removes extraneous features from Droidboy so it's more easily digestible as a sample application.
- Enables setting a registration ID without a full push setup; `registerAppboyGcmMessages()` and `registerAppboyPushMessages()` no longer throw null pointer exceptions if Appboy isn't correctly configured to display push messages.
- Enables `AppboyWebViewActivity` to download items.
- Adds support for apps built targeting android-L. Appboy's process for registering push notifications had previously used an implicit service intent which caused a runtime error. Any apps built against android-L will need to upgrade to this version. However, apps with Appboy that are/were built against any other versions of Android will run without issue on android-L. Thus, this is not an urgent upgrade unless you're working with android-L.

## 1.5.1
- Removes obfuscation from parameter names on public models.

## 1.5.0
- Adds Kindle Fire support and ADM support.
- Adds read/unread visual indicators to newsfeed cards. Use the configuration boolean com_appboy_newsfeed_unread_visual_indicator_on in appboy.xml to enabled the indicators.  Additionally, moved the `logFeedCardImpression()` and `logFeedCardClick()` methods to the card objects themselves.
- Adds support to image loading in CaptionedImage and Banner cards for dynamic resizing after loading the image url; supports any aspect ratio.
- Adds Hello Appboy sample project that shows a minimal use case of the Appboy SDK.
- Adds wake lock to `AppboyGcmReceiver` in the UI project. When the `WAKE_LOCK` permission is set, the screen will be turned on when a notification is received.
- Moved constants from `AppboyGcmReceiver` (ie: `APPBOY_GCM_NOTIFICATION_TITLE_ID`, etc.) into new `AppboyNotificationUtils` class.
- Restricted productId to 255 characters for `Appboy.logPurchase()`.

## 1.4.3
- Removes org.json classes from appboy.jar.

## 1.4.2
- Adds summary text for push image notifications.
- Adds a new constant, `APPBOY_LOG_TAG_PREFIX`, for logging which includes the sdk version number.

## 1.4.1
- Adds automatic tests to verify that the sdk has integrated correctly.
- Added an optional quantity amount to in-app-purchases.
- Removed default max length and ellipsize properties in the `styles.xml`. The old defaults were set to 5 for maxLines for  newsfeed cards and ellipsize 'end'.
- Changed the device identifier from the device persistent `ANDROID_ID` to a non device persistent identifier for compliance with the new Google Play Terms of Service.

## 1.4.0
- Adds categories.
- Adds swipe to refresh functionality to the newsfeed. The swipe to refresh colors are configurable in the colors xml file.
- Adds configurable session timeout to the `appboy xml`.
- Adds images to GCM push notifications.
- Removes click logging on slideups when action is `None`.
- Adds email and push notification subscription types for a user. Subscription types are explicitly opted in, subscribed, and unsubscribed. The old email boolean subscribe method has been deprecated.
- The feedback form now displays error popups to the user on invalid fields.

## 1.3.4
- Minor changes to address some Lint issues in the UI project.
- Updates the open source AppboyGcmReceiver to use references to R.java for resource identifiers. This became possible when we moved AppboyGcmReceiver.java into the android-sdk-ui project (from the base library JAR).

## 1.3.3
- Minor bug fix for a crash that occurred in certain conditions where the News Feed cards were replaced with a smaller set of cards.

## 1.3.2
- Moves com.appboy.AppboyGcmReceiver to the open source android-sdk-ui project. Also moves some of the constants previously available as AppboyGcmReceiver.* to com.appboy.constants.APPBOY_GCM_*. The CAMPAIGN_ID_KEY previously used in our sample app is still available in com.appboy.AppboyGcmReceiver, but if you were using other constants, you'll have to move the references.
- Fixes a few minor style issues to be closer in line with Eclipse's preferences.
- Fixes a potential synchronization issue with the AppboyListAdapter.
- Minor update to Chinese language translation.
- Adds the ability to set the avatar image URL for your users.
- Fixes support for protocol URLs and adds an ActivityAction overload that streamlines the use of deep link and web link actions.
- Removes input validation on custom attribute key names so that you can use foreign characters and spaces to your heart's desire. Just don't go over the max character limit.

## 1.3.1
- Updates to version 1.9.1 of Android-Universal-Image-Loader.
- Adds Chinese language translations.
- Minor cleanup to imports.

## 1.3
Appboy version 1.3 provides a substantial upgrade to the slideup code and reorganization for better flexibility moving forward, but at the expense of a number of breaking changes. We've detailed the changes in this changelog and hope that you'll love the added power, increased flexibility, and improved UI that the new Appboy slideup provides. If you have any trouble with these changes, feel free to reach out to success@appboy.com for help, but most migrations to the new code structure should be relatively painless.

New AppboySlideupManager
- The AppboySlideupManager has moved to `com.appboy.ui.slideups.AppboySlideupManager.java`.
- An `ISlideupManagerListener` has been provided to allow the developer to control which slideups are displayed, when they are displayed, as well as what action(s) to perform when a slideup is clicked or dismissed.
  - The slideup `YOUR-APPLICATION-PACKAGE-NAME.intent.APPBOY_SLIDEUP_CLICKED` event has been replaced by the `ISlideupManagerListener.onSlideupClicked(Slideup slideup, SlideupCloser slideupCloser)` method.
- Added the ability to use a custom `android.view.View` class to display slideups by providing an `ISlideupViewFactory`.
- Default handling of actions assigned to the slideup from the Appboy dashboard.
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
- A custom `IAppboyNavigator` can be set via `IAppboy.setAppboyNavigator(IAppboyNavigator appboyNavigator)` which can be used to direct your users to your integrated Appboy news feed when certain slideups are clicked. This provides a more seamless experience for your users. Alternatively, you can choose not to provide an IAppboyNavigator, but instead register the new `AppboyFeedActivity` class in your `AndroidManifest.xml` which will open a new Appboy news feed Activity when certain slideups are clicked.

Other
- A new base class, `AppboyBaseActivity`, has been added that extends `android.app.Activity` and integrates Appboy session and slideup management.
- A drop in `AppboyFeedActivity` class has been added which can be used to display the Appboy news feed.

## 1.2.1
- Fixes a ProGuard issue.

## 1.2
- Introduces two new card types (Banner card and Captioned Image card).
- Adds support for sending down key/value pairs as part of a GCM message.
- Minor bug fixes.

## 1.1
- Adds support for reporting purchases in multiple currencies.
- Deprecates `IAppboy.logPurchase(String, int)`.
- Fixes a bug in caching custom events to a SQLite database.  
- Fixes a validation bug when logging custom events.

## 1.0
- Initial release
