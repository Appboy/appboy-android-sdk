# Welcome to Appboy's sample project!

## Getting Started

Droidboy is a sample application that shows Appboy in action.

Below are some sections that will explain how to:

* Set your API Key
* Change your User
* Log custom event data and in-app purchases to Appboy
* Set user attributes for the user on the device
* Activate an in-app message
* Display push
* Display an Appboy Feedback form
* Display the News Feed
* Create Custom Styles

## Set your API Key

Droidboy has an example of setting your Appboy API key by setting `com_appboy_api_key` in its [`appboy.xml`][2] file.

The Appboy API key and various other Appboy configuration settings are set in [`appboy.xml`][2]. Your will need to create one in the `res/values` folder of your application as well.

## Change your User

Droidboy's main page is [`MainFragment.java`][24]. This page is in the `EVENTS` tab of Droidboy's main Activity.

Droidboy users can change their user Id here.

## Custom Events, In-App Purchases, and Attributes

[`MainFragment.java`][24] also allows logging custom event data and in-app purchases to Appboy via the [`Appboy`][12] interface. Furthermore, It contains examples of setting user attributes via the [`AppboyUser`][13] interface.

To log events and purchases with more control over custom properties, currency code, etc., click on the hamburger on the top right of the app to go to [`Settings`][14].  In the `Custom Logging` section, you will find dialogs for logging [default user attributes][25], [custom user attributes][26], [purchases][27], and [events][28].

Appboy automatically flushes event data to its servers at an interval that can be configured via [`appboy.xml`][2]. You may request an immediate data flush any time by calling [`Appboy.requestImmediateDataFlush()`][38]. [`MainFragment.java`][24] has a button for requesting an immediate data flush.

## [In-app Messages][47]

Droidboy has a [Fragment][23] where you can create and display customized in-app messages.

To navigate there, click on the `IAMS` tab in Droidboy's main Activity.

#### Using Custom In-app Message Views

If you would like to change the appearance of your in-app message to a completely custom UI, create a custom [in-app message view factory][29] and your own custom views. Custom views factories can be registered with Appboy by being passed into [`AppboyInAppMessageManager.setCustomInAppMessageViewFactory()`][42]. Please see our [custom view factory example][31] and [custom view example][30].

#### Customizing In-app Message Display

To listen to and optionally action on in-app message lifecycle events (such as when a message is about to be displayed, or when it is clicked), create a custom [in-app message manager listener][33]. Custom manager listeners can be registered with Appboy by being passed into [`AppboyInAppMessageManager.setCustomInAppMessageManagerListener()`][43]. Please see our [custom manager listener example][34].

#### Customizing In-app Message Animations

If you would like to customize the animations of your in-app messages, create a custom [in-app message animation factory][32]. Custom in-app message animation factories can be registered with Appboy by being passed into [`AppboyInAppMessageManager.setCustomInAppMessageAnimationFactory()`][44]. Please see our [custom animation factory example][35].

#### Customizing HTML In-app Message Display

To listen to and optionally action on HTML in-app message lifecycle events (such as when a message is about to be displayed, or when it is clicked), create a custom [HTML in-app message manager listener][36]. Custom HTML manager listeners can be registered with Appboy by being passed into [`AppboyInAppMessageManager.setCustomHtmlInAppMessageActionListener()`][45]. Please see our [custom HTML manager listener example][37].

Note that custom factories and listeners should be set before the in-app message is displayed, typically in the [`Application`][40] class of your application.

For more information, visit the javadoc for [`AppboyInAppMessageManager`][22].

## [Push][46]

Droidboy is fully push integrated and also has a [Fragment][21] where you can create and display customized push notifications.

To navigate there, click on the `PUSH` tab in Droidboy's main Activity.

#### Displaying Custom Notifications

If you would like to control the display of push, create a custom [notification factory][20]. Custom notification factories can be registered with Appboy by being passed into [`Appboy.setCustomAppboyNotificationFactory()`][39]. Please see our [custom notification factory example][18].

Note that custom notification factories should be set in the [`Application`][40] class of your application. For an example, see [Droidboy's application class][17].

Droidboy also contains an example of declaring the [`AppboyGcmReceiver`][16] as well as setting a [deep link][4] in its [Android Manifest][3].

## [Feedback][49]

Droidboy has an [Activity][19] that shows an integration of our [Feedback Fragment][15].

To navigate there, swipe the left navigation tray into view and click on the `Feedback` button.

#### Modifying Feedback before Sending

Droidboy's Feedback [Activity][19] also shows an example of setting a custom [`FeedbackFinishedListener`][41] to modify feedback before it's sent to Appboy.

## [News Feed][48]

Droidboy integrates the Appboy [News Feed][9].

To navigate there, click on the `FEED` tab in Droidboy's main Activity.

#### Custom Handling News Feed Clicks

If you would like to custom handle News Feed card clicks, create a custom [feed click listener][10]. Custom feed click listeners can be registered with Appboy by being passed into `AppboyFeedManager.setFeedCardClickActionListener()`. Please see our [custom feed click listener example][11].

#### Customizing News Feed Colors and Styles

To customize colors and styles for the News Feed, override Appboy's [colors][8] and [styles][1] in a local `colors.xml` or `styles.xml` file. For more on overriding styles, see the section on `Custom Styles` and our [documentation][50].

Note that custom feed click listeners should be set before the News Feed is displayed, typically in the `Application` class of your application.

## Custom Styles

Droidboy has examples of creating custom styles for Appboy UI elements. For more, see Droidboy's [styles file][7].

To override a style, all elements of the style must be copied to the new style. For all of Appboy's available styles, see the UI [styles file][1].

Note that there are also styles files for larger devices [here][5] and [here][6].

## Next Steps

Finally, look around for other examples of how to use the methods employed throughout Droidboy.

Don't hesitate to contact us if you have questions at [support@appboy.com](mailto:support@appboy.com)!

[1]: https://github.com/Appboy/appboy-android-sdk/blob/master/android-sdk-ui/res/values/styles.xml
[2]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/res/values/appboy.xml
[3]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/AndroidManifest.xml
[4]: https://developer.android.com/training/app-indexing/deep-linking.html
[5]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/res/values-xlarge/styles.xml
[6]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/res/values-sw600dp/styles.xml
[7]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/res/values/styles.xml
[8]: https://github.com/Appboy/appboy-android-sdk/blob/master/android-sdk-ui/res/values/colors.xml
[9]: https://github.com/Appboy/appboy-android-sdk/blob/master/android-sdk-ui/src/com/appboy/ui/AppboyFeedFragment.java
[10]: https://github.com/Appboy/appboy-android-sdk/blob/master/android-sdk-ui/src/com/appboy/ui/feed/listeners/IFeedClickActionListener.java
[11]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/CustomFeedClickActionListener.java
[12]: https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/Appboy.html
[13]: https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/AppboyUser.html
[14]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/PreferencesActivity.java
[15]: https://github.com/Appboy/appboy-android-sdk/blob/master/android-sdk-ui/src/com/appboy/ui/AppboyFeedbackFragment.java
[16]: https://github.com/Appboy/appboy-android-sdk/blob/master/android-sdk-ui/src/com/appboy/AppboyGcmReceiver.java
[17]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/DroidboyApplication.java
[18]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/DroidboyNotificationFactory.java
[19]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/FeedbackFragmentActivity.java
[20]: https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/IAppboyNotificationFactory.html
[21]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/PushTesterFragment.java
[22]: https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/ui/inappmessage/AppboyInAppMessageManager.html
[23]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/InAppMessageTesterFragment.java
[24]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/MainFragment.java
[25]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/UserProfileDialog.java
[26]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/logging/CustomUserAttributeDialog.java
[27]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/logging/CustomPurchaseDialog.java
[28]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/logging/CustomEventDialog.java
[29]: https://github.com/Appboy/appboy-android-sdk/blob/master/android-sdk-ui/src/com/appboy/ui/inappmessage/IInAppMessageViewFactory.java
[30]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/CustomInAppMessageView.java
[31]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/CustomInAppMessageViewFactory.java
[32]: https://github.com/Appboy/appboy-android-sdk/blob/master/android-sdk-ui/src/com/appboy/ui/inappmessage/IInAppMessageAnimationFactory.java
[33]: https://github.com/Appboy/appboy-android-sdk/blob/master/android-sdk-ui/src/com/appboy/ui/inappmessage/listeners/IInAppMessageManagerListener.java
[34]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/CustomInAppMessageManagerListener.java
[35]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/CustomInAppMessageAnimationFactory.java
[36]: https://github.com/Appboy/appboy-android-sdk/blob/master/android-sdk-ui/src/com/appboy/ui/inappmessage/listeners/IHtmlInAppMessageActionListener.java
[37]: https://github.com/Appboy/appboy-android-sdk/blob/master/droidboy/src/com/appboy/sample/CustomHtmlInAppMessageActionListener.java
[38]: https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/Appboy.html#requestImmediateDataFlush--
[39]: https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/Appboy.html#setCustomAppboyNotificationFactory-com.appboy.IAppboyNotificationFactory-
[40]: https://developer.android.com/reference/android/app/Application.html
[41]: https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/ui/AppboyFeedbackFragment.FeedbackFinishedListener.html
[42]: https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/ui/inappmessage/AppboyInAppMessageManager.html#setCustomInAppMessageViewFactory-com.appboy.ui.inappmessage.IInAppMessageViewFactory-
[43]: https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/ui/inappmessage/AppboyInAppMessageManager.html#setCustomInAppMessageManagerListener-com.appboy.ui.inappmessage.listeners.IInAppMessageManagerListener-
[44]: https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/ui/inappmessage/AppboyInAppMessageManager.html#setCustomInAppMessageAnimationFactory-com.appboy.ui.inappmessage.IInAppMessageAnimationFactory-
[45]: https://appboy.github.io/appboy-android-sdk/javadocs/com/appboy/ui/inappmessage/AppboyInAppMessageManager.html#setCustomHtmlInAppMessageActionListener-com.appboy.ui.inappmessage.listeners.IHtmlInAppMessageActionListener-
[46]: https://www.appboy.com/documentation/Android/#push-notifications
[47]: https://www.appboy.com/documentation/Android/#in-app-messaging
[48]: https://www.appboy.com/documentation/Android/#news-feed
[49]: https://www.appboy.com/documentation/Android/#customer-feedback
[50]: https://www.appboy.com/documentation/Android/#news-feed-customization
