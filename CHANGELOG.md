## 1.5.0
- Adds Kindle Fire support and ADM support.
- Adds read/unread visual indicators to newsfeed cards. Use the configuration boolean com_appboy_newsfeed_unread_visual_indicator_on in appboy.xml
  to enabled the indicators.  Additionally, moved the logFeedCardImpression() and logFeedCardClick() methods to the 
  card objects themselves.
- Adds support to image loading in CaptionedImage and Banner cards for dynamic resizing after loading the image url; supports any
  aspect ratio.
- Adds Hello Appboy sample project that shows a minimal use case of the Appboy SDK.
- Adds wake lock to AppboyGcmReceiver in the UI project. When the WAKE_LOCK permission is set, the screen
  will be turned on when a notification is received.
- Restricted productId to 255 characters for logPurchase.

## 1.4.3
- Removes org.json classes from appboy.jar.

## 1.4.2
- Adds summary text for push image notifications.
- Adds a new constant, APPBOY_LOG_TAG_PREFIX, for logging which includes the sdk version number.

## 1.4.1
- Adds automatic tests to verify that the sdk has integrated correctly.
- Added an optional quantity amount to in-app-purchases.
- Removed default max length and ellipsize properties in the styles.xml. The old defaults were set to 5 for maxLines for 
  newsfeed cards and ellipsize 'end'.
- Changed the device identifier from the device persistent ANDROID_ID to a non device persistent identifier for compliance with
  the new Google Play Terms of Service.

## 1.4.0
- Adds categories.
- Added swipe to refresh functionality to the newsfeed. The swipe to refresh colors are configurable in
  the colors xml file.
- Added configurable session timeout to the appboy xml.
- Added images to gcm push notifications.
- Removed click logging on slideups when action is None.
- Added email and push notification subscription types for a user. Subscription types are explicitly opted in, subscribed, and unsubscribed. The old email boolean subscribe method has been deprecated.
- The feedback form now displays error popups to the user on invalid fields.

## 1.3.4
- Minor changes to address some Lint issues in the UI project.
- Updates the open source AppboyGcmReceiver to use references to R.java for resource identifiers. This became possible
  when we moved AppboyGcmReceiver.java into the android-sdk-ui project (from the base library JAR).

## 1.3.3
- Minor bug fix for a crash that occurred in certain conditions where the News Feed cards were replaced with a smaller set of cards.

## 1.3.2
- Moves com.appboy.AppboyGcmReceiver to the open source android-sdk-ui project. Also moves some of the constants
  previously available as AppboyGcmReceiver.* to com.appboy.constants.APPBOY_GCM_*. The CAMPAIGN_ID_KEY previously used
  in our sample app is still available in com.appboy.AppboyGcmReceiver, but if you were using other constants, you'll
  have to move the references.
- Fixes a few minor style issues to be closer in line with Eclipse's preferences.
- Fixes a potential synchronization issue with the AppboyListAdapter.
- Minor update to Chinese language translation.
- Adds the ability to set the avatar image URL for your users.
- Fixes support for protocol URLs and adds an ActivityAction overload that streamlines the use of deep link and web link actions.
- Removes input validation on custom attribute key names so that you can use foreign characters and spaces to your heart's desire. Just don't go over the max character limit.

## 1.3.1
- Updating to version 1.9.1 of Android-Universal-Image-Loader.
- Adds Chinese language translations.
- Minor cleanup to imports.

## 1.3
Appboy version 1.3 provides a substantial upgrade to the slideup code and reorganization for better flexibility moving forward, but at the expense of a number of breaking changes. We've detailed the changes in this changelog and hope that you'll love the added power, increased flexibility, and improved UI that the new Appboy slideup provides. If you have any trouble with these changes, feel free to reach out to success@appboy.com for help, but most migrations to the new code structure should be relatively painless.

New AppboySlideupManager
- The AppboySlideupManager has moved to ```com.appboy.ui.slideups.AppboySlideupManager.java```.
- An ```ISlideupManagerListener``` has been provided to allow the developer to control which slideups are displayed, when they are displayed, as well as what action(s) to perform when a slideup is clicked or dismissed.
  - The slideup ```YOUR-APPLICATION-PACKAGE-NAME.intent.APPBOY_SLIDEUP_CLICKED``` event has been replaced by the ```ISlideupManagerListener.onSlideupClicked(Slideup slideup, SlideupCloser slideupCloser)``` method.
- Added the ability to use a custom ```android.view.View``` class to display slideups by providing an ```ISlideupViewFactory```.
- Default handling of actions assigned to the slideup from the Appboy dashboard.
- Slideups can be dismissed by swiping away the view to either the left or the right. (Only on devices running Honeycomb Android 3.1 or higher).
  - Any slideups that are created to be dismissed by a swipe will automatically be converted to auto dismiss slideups on devices that are not running Android 3.1 or higher.

Slideup model
- A key value ```extras``` java.util.Map has been added to provide additional data to the slideup. ```Extras``` can be on defined on a per slideup basis via the dashboard.
- The ```SlideFrom``` field defines whether the slideup originates from the top or the bottom of the screen.
- The ```DismissType``` property controls whether the slideup will dismiss automatically after a period of time has lapsed, or if it will wait for interaction with the user before disappearing. 
  - The slideup will be dismissed automatically after the number of milliseconds defined by the duration field have elapsed if the slideup's DismissType is set to AUTO_DISMISS.
- The ClickAction field defines the behavior after the slideup is clicked: display a news feed, redirect to a uri, or nothing but dismissing the slideup. This can be changed by calling any of the following methods: ```setClickActionToNewsFeed()```, ```setClickActionToUri(Uri uri)```, or ```setClickActionToNone()```.
- The uri field defines the uri string that the slide up will open when the ClickAction is set to URI. To change this value, use the ```setClickActionToUri(Uri uri)``` method.
- Convenience methods to track slideup impression and click events have been added to the ```com.appboy.models.Slideup``` class.
  - Impression and click tracking methods have been removed from ```IAppboy.java```.
- A static ```createSlideup``` method has been added to create custom slideups.

IAppboyNavigator
- A custom ```IAppboyNavigator``` can be set via ```IAppboy.setAppboyNavigator(IAppboyNavigator appboyNavigator)``` which can be used to direct your users to your integrated Appboy news feed when certain slideups are clicked. This provides a more seamless experience for your users. Alternatively, you can choose not to provide an IAppboyNavigator, but instead register the new ```AppboyFeedActivity``` class in your ```AndroidManifest.xml``` which will open a new Appboy news feed Activity when certain slideups are clicked.

Other
- A new base class, ```AppboyBaseActivity```, has been added that extends ```android.app.Activity``` and integrates Appboy session and slideup management.
- A drop in ```AppboyFeedActivity``` class has been added which can be used to display the Appboy news feed.

## 1.2.1
- Fixing a ProGuard issue.

## 1.2
- Introducing two new card types (Banner card and Captioned Image card).
- Adding support for sending down key/value pairs as part of a GCM message.
- Minor bug fixes.

## 1.1
- Adds support for reporting purchases in multiple currencies. Deprecating IAppboy.logPurchase(String, int).
- Fixing a bug in caching custom events to a SQLite database.  
- Fixing a validation bug when logging custom events.

## 1.0
- Initial release
