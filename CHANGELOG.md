## 18.0.1

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v18.0.1)

##### Fixed
- Fixed an issue introduced in 17.0.0 where some HTML In-App Message zip asset files containing hidden `__MACOSX` folders without a corresponding entry for that folder would cause the in-app message to fail to display.

## 18.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v18.0.0)
 
#### Breaking
- Removed `AppboyLruImageLoader` in favor of `DefaultBrazeImageLoader`
  - `com.appboy.lrucache.AppboyLruImageLoader` -> `com.braze.images.DefaultBrazeImageLoader`
  - `com.appboy.Appboy.getAppboyImageLoader` -> `com.appboy.Appboy.getImageLoader`
  - `com.appboy.Appboy.setAppboyImageLoader` -> `com.appboy.Appboy.setImageLoader`
- Removed `IAppboyEndpointProvider` in favor of `IBrazeEndpointProvider`.
    - If using `Braze.setAppboyEndpointProvider()` please use `Braze.setEndpointProvider()`
   
##### Fixed
- Fixed an issue introduced in 15.0.0 where Full in-app messages on tablets may have had an incorrect background color.

##### Added
- Added the ability to change SDK authentication signature with `Braze.changeUser()` when the current user id and a new signature is passed in.
    - Previously, `Braze.changeUser()` would not change the SDK authentication signature if the current user id was used.

##### Changed
- `InAppMessageCloser` is deprecated.
  - Use `BrazeInAppMessageManager.hideCurrentlyDisplayingInAppMessage()` to hide currently displayed in-app messages.
  - Use `IInAppMessage.setAnimateOut()` to set whether your in-app message should animate on close.
  - New version of `IInAppMessageManagerListener.onInAppMessageClicked()` and `IInAppMessageManagerListener.onInAppMessageButtonClicked()` that don't use `InAppMessageCloser` have been added.
    - If you override the deprecated functions that use `InAppMessageCloser`, those will be called
    - If you override the new functions and don't override the deprecated functions, the new functions will be called
- Deprecated `ContentCardsUpdatedEvent.getLastUpdatedInSecondsFromEpoch`.
    - Use `getTimestampSeconds()` (Java) or `timestampSeconds` (Kotlin)

## 17.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v17.0.0)

#### Breaking
- `BrazeLogger.setLogLevel()` replaced with direct property setter `BrazeLogger.logLevel` for Kotlin.
- Removed `AppboyLogger, com.appboy.IntentUtils, com.appboy.StringUtils` class. The Braze namespaced classes remain.
- Removed `com_braze_locale_api_key_map` as a configuration option and `BrazeConfig.setLocaleToApiMapping()`. If you need to change your API key based on locale, please use `BrazeConfig` at runtime instead.

##### Added
- Added `Braze.isDisabled()` to determine whether the SDK is disabled.
- Added `Braze.addSdkMetadata()` to allow self reporting of SDK Metadata fields via the `BrazeSdkMetadata` enum.
  - Fields may also be added via a `string-array` to your `braze.xml` with the key `com_braze_sdk_metadata`. The allowed items are the same as the keys found in the `BrazeSdkMetadata` enum. For example when using Branch:
  ```xml
    <string-array name="com_braze_sdk_metadata">
     <item>BRANCH</item>
    </string-array>
  ```
  - Fields are additive across all reporting methods.

## 16.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v16.0.0)

#### Breaking
- Removed `AppboyConfigurationProvider` in favor of `BrazeConfigurationProvider`.
  - Any deprecated usages, such as in the `IBrazeNotificationFactory` have also been removed.

##### Fixed
- Fixed an issue introduced in 13.1.0 where session start location updates would fail to update on pre API 30 devices.
- Fixed an issue introduced in 13.1.0 where geofence update events would fail to update properly.

##### Added
- Added the ability to namespace all `braze.xml` configurations to be able to use `braze` in place of `appboy`. The Braze namespaced configuration keys will take precedence over the `appboy` keys.
  - For example, `com_appboy_api_key` can be replaced with `com_braze_api_key`.
  - Be sure to look for and update any API keys in your build variants as the `com_braze_api_key` from your default variant might take precedence unexpectedly.
  - All `com_appboy_*` configuration keys in XML will be removed in a future release so it is advised to migrate these configuration keys to their `com_braze_*` counterparts.

##### Changed
- Changed target API for the SDK to 31.

## 15.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v15.0.0)

##### Important
- It is highly recommended to do extensive QA after updating to this release, especially for clients doing any amount of Content Card or In-App Message customizations.

##### Breaking
- All Content Cards layout/drawables/colors/dimens identifiers containing `com_appboy_content_cards`/`com_appboy_content_card` were replaced with `com_braze_content_cards`/`com_braze_content_card` respectively.
  - Content Card drawables `icon_pinned, icon_read, icon_unread` are now `com_braze_content_card_icon_pinned, com_braze_content_card_icon_read, com_braze_content_card_icon_unread`.
- All In-App Message layout/drawables/colors/dimens identifiers containing `com_appboy_inappmessage`/`com_appboy_in_app_message` replaced with `com_braze_inappmessage`.
- All styles under namespace `Appboy.*` moved to `Braze.*`.
  - Any `Appboy.*` style overrides must be migrated to `Braze.*` as there is no backwards compatibility.
  - For example, a style override for `Appboy.Cards.ImageSwitcher` must be renamed to `Braze.Cards.ImageSwitcher`.
- Several classes/interfaces have been moved to a Braze namespace/package.
  - In-App Messages
    - In-App Message classes under `com.appboy.models.*` moved to `com.braze.models.inappmessage`
    - Class `com.appboy.ui.inappmessage.InAppMessageCloser` -> `com.braze.ui.inappmessage.InAppMessageCloser`
    - Enum `com.appboy.ui.inappmessage.InAppMessageOperation` -> `com.braze.ui.inappmessage.InAppMessageOperation`
    - Enums in package `com.appboy.enums.inappmessage.*` moved to `com.braze.enums.inappmessage`
  - Content Cards
    - Interface `IContentCardsUpdateHandler` moved to `com.braze.ui.contentcards.handlers.IContentCardsUpdateHandler`
    - Interface `IContentCardsViewBindingHandler` moved to `com.braze.ui.contentcards.handlers.IContentCardsViewBindingHandler`
    - Interface `AppboyContentCardsActionListener` moved to `com.braze.ui.contentcards.listeners.DefaultContentCardsActionListener`
    - Classes in package `com.appboy.ui.contentcards.view.*` moved to `com.braze.ui.contentcards.view.*`
      - This is the package containing all Content Card default views.
    - Class `com.appboy.events.ContentCardsUpdatedEvent` -> `com.braze.events.ContentCardsUpdatedEvent`
  - Miscellaneous
    - Class `AppboyBaseFragmentActivity` moved to `com.braze.ui.activities.BrazeBaseFragmentActivity`
- Removed deprecated `IInAppMessageManagerListener#onInAppMessageReceived` from `IInAppMessageManagerListener`.
- Removed `AppboyUser` in favor of `BrazeUser`.
  - Note that for Kotlin consumers, `Appboy.currentUser?` and `Braze.currentUser?` are valid due to the removal of generics on the `Braze.getCurrentUser()` method.

##### Added
- Added support for Conversational Push.
- Added the ability for custom broadcast receivers to not require the host package name as a prefix when declaring intent filters in your app manifest.
  - `<action android:name="${applicationId}.intent.APPBOY_PUSH_RECEIVED" />` should be replaced with `<action android:name="com.braze.push.intent.NOTIFICATION_RECEIVED" />`
  - `<action android:name="${applicationId}.intent.APPBOY_NOTIFICATION_OPENED" />` should be replaced with `<action android:name="com.braze.push.intent.NOTIFICATION_OPENED" />`
  - `<action android:name="${applicationId}.intent.APPBOY_PUSH_DELETED" />` should be replaced with `<action android:name="com.braze.push.intent.NOTIFICATION_DELETED" />`
  - The `appboy` intents have been deprecated but are still available. They will be removed in a future release so migrating early is highly recommended.
  - Both the `appboy` and `braze` intents are sent for backwards compatibility so only one set should be registered at a time.
- Added `BrazeUser.addToSubscriptionGroup()` and `BrazeUser.removeFromSubscriptionGroup()` to add or remove a user from an email or SMS subscription group.
  - Added `brazeBridge.getUser().addToSubscriptionGroup()` and `brazeBridge.getUser().removeFromSubscriptionGroup()` to the javascript interface for HTML In-App Messages.

##### Changed
- Several classes in the android-sdk-ui artifact have been renamed to the Braze namespace/package. Whenever possible, the original classes are still available. However, they will be removed in a future release so migrating early is highly recommended.
  - Classes in package `com.appboy.push.*` moved to `com.braze.push.*`
  - Classes in package `com.appboy.ui.inappmessage.views` moved to `com.braze.ui.inappmessage.views`
  - Classes in package `com.appboy.ui.inappmessage.listeners` moved to `com.braze.ui.inappmessage.listeners`
  - Interfaces in `com.appboy.ui.inappmessage.*` moved to `com.braze.ui.inappmessage.*`
  - Class `com.appboy.AppboyFirebaseMessagingService` -> `com.braze.push.BrazeFirebaseMessagingService`
  - Class `com.appboy.AppboyAdmReceiver` -> `com.braze.push.BrazeAmazonDeviceMessagingReceiver`
  - Class `com.appboy.ui.AppboyContentCardsFragment` -> `com.braze.ui.contentcards.ContentCardsFragment`
  - Class `com.appboy.ui.activities.AppboyContentCardsActivity` -> `com.braze.ui.activities.ContentCardsActivity`
  - Class `com.appboy.ui.AppboyWebViewActivity` -> `com.braze.ui.BrazeWebViewActivity`
  - Class `com.appboy.ui.inappmessage.AppboyInAppMessageManager` -> `com.braze.ui.inappmessage.BrazeInAppMessageManager`
  - Class `com.appboy.ui.inappmessage.DefaultInAppMessageViewWrapper` -> `com.braze.ui.inappmessage.DefaultInAppMessageViewWrapper`
  - Class `com.appboy.AppboyLifecycleCallbackListener` -> `com.braze.BrazeActivityLifecycleCallbackListener`
- Changed the `ContentCardsFragment` and `BrazeInAppMessageManager` to clear their respective caches of messages after `wipeData()` is called.

## 14.0.1

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v14.0.1)

##### Fixed
- Fixed an issue with `BrazeProperties` not being kept via proguard rules.
- Fixed an issue on TV integrations where in app messages wouldn't properly be given focus when visible.

##### Added
- Added close icon highlighting for TV integrations when selecting the close button in In App Messages.

## 14.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v14.0.0)

##### Breaking
- Interface `IInAppMessageViewWrapperFactory` changed to use `BrazeConfigurationProvider`.
- Interface `IAppboyImageLoader/IBrazeImageLoader` changed to use `com.braze.enums.BrazeViewBounds`.
- Class `com.appboy.configuration.AppboyConfig` is now `com.braze.configuration.BrazeConfig`. The original class has been removed and old usages should be updated.
- Class `com.appboy.enums.AppboyViewBounds` is now `com.braze.enums.BrazeViewBounds`. The original class has been removed and old usages should be updated.
- Removed `com.appboy.push.AppboyNotificationUtils#bundleOptString`.
- `Braze.logPurchase()` and `Braze.logEvent()` now impose a 50KB limit on event properties. If the supplied properties are too large, the event is not logged.
  - See `BrazeProperties.isInvalid()`.
- HTML In-App Messages rendered via the default `AppboyHtmlViewFactory` now require the device to be in touch mode to display.
  - See `getIsTouchModeRequiredForHtmlInAppMessages()` in the #added section for configuration on disabling this behavior.
- For Kotlin consumers, `Appboy.currentUser?` calls must be migrated to `Braze.getCurrentUser<BrazeUser>()` due to updated generics resolution.

##### Changed
- Several classes in the base artifact have been renamed to the Braze namespace/packages. Whenever possible, the original classes are still available. However, they will be removed in a future release so migrating early is highly recommended.
  - `com.appboy.Appboy` -> `com.braze.Braze`
  - `com.appboy.configuration.AppboyConfig` -> `com.braze.configuration.BrazeConfig`
  - `com.braze.AppboyUser` -> `com.braze.BrazeUser`
  - `com.appboy.lrucache.AppboyLruImageLoader` -> `com.braze.images.DefaultBrazeImageLoader`
  - `com.appboy.configuration.AppboyConfigurationProvider` -> `com.braze.configuration.BrazeConfigurationProvider`
  - `com.appboy.models.outgoing.AppboyProperties` -> `com.braze.models.outgoing.BrazeProperties`
  - `com.appboy.support.AppboyImageUtils` -> `com.braze.support.BrazeImageUtils`
  - `com.appboy.support.AppboyFileUtils` -> `com.braze.support.BrazeFileUtils`
- Changed the behavior of In-App Message Accessibility Exclusive mode to save and reset the accessibility flags of views after display.
- Changed the `AppboyInAppMessageWebViewClientListener` to use an Activity context when following a deeplink in `IInAppMessageWebViewClientListener.onOtherUrlAction`.
- Deprecated `AppboyInAppMessageHtmlBaseView.APPBOY_BRIDGE_PREFIX`.

##### Added
- Added `Braze.registerPushToken()` and `Braze.getRegisteredPushToken()`.
  - Note that these methods are the functional equivalents of `Appboy.registerAppboyPushMessages()` and `Appboy.getAppboyPushMessageRegistrationId()`.
- Exposed `brazeBridge` which replaces `appboyBridge` to be used as the javascript interface for HTML In-App Messages. `appboyBridge` is deprecated and will be removed in a future version of the SDK.
- Added `AppboyInAppMessageHtmlBaseView.BRAZE_BRIDGE_PREFIX`.
- Added the ability to configure whether `View#isInTouchMode()` is required to show HTML In-App Messages via `BrazeConfig.setIsTouchModeRequiredForHtmlInAppMessages()`.
  - Can also be configured via boolean `com_braze_require_touch_mode_for_html_in_app_messages` in your `braze.xml`.
  - Defaults to true.
- Added support for new SDK Authentication feature.

##### Fixed
- Fixed an issue with `setIsInAppMessageAccessibilityExclusiveModeEnabled()` not being respected if set via runtime configuration. Setting this value via XML was unaffected.
- Fixed an issue with the SDK repeatedly failing to initialize when not properly setting a Braze API key.

## 13.1.2

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v13.1.2)

##### Changed
- Changed the `NotificationTrampolineActivity` to always call `finish()` regardless of any eventual deeplink handling by the host app or SDK.

## 13.1.1

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v13.1.1)

##### Fixed
- Fixed an issue with the `NotificationTrampolineActivity` being opened on notification delete intents.

## 13.1.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v13.1.0)

##### Changed
- All notifications now route through `NotificationTrampolineActivity` to comply with Android 12 notification trampoline restrictions.
- Inline Image push is now compatible with the Android 12 notification area changes.
- Automatic Firebase Messaging registration will now use `FirebaseMessaging.getInstance().getToken()` directly if available.
- Removed usage of `Intent.ACTION_CLOSE_SYSTEM_DIALOGS` with push notifications.

##### Added
- Added `getInAppMessageStack()`, `getCarryoverInAppMessage()`, and `getUnregisteredInAppMessage()` to `AppboyInAppMessageManager`.

## 13.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v13.0.0)

##### ⚠ Breaking
- Moved all In-App Message buttons from `Button` to `com.appboy.ui.inappmessage.views.InAppMessageButton`.
  - This ensures that the `MaterialComponentsViewInflater` does not interfere with standard In-App Message display when using a `MaterialComponents` theme.
  - Apps extending a `Material` theme should test to ensure their In-App Messages appear as expected.
- Moved `com.appboy.ui.inappmessage.AppboyInAppMessageImageView` to `com.appboy.ui.inappmessage.views.InAppMessageImageView`.
- Removed all getter methods from `AppboyConfig`. Access to the underlying data is now directly possible via the variables of the object, e.g. `appboyConfig.getApiKey()` is now `appboyConfig.mApiKey`.

##### Added
- Added `getEmptyCardsAdapter(), getContentCardUpdateRunnable(), getNetworkUnavailableRunnable()` to protected methods in `AppboyContentCardsFragment` for easier customizability.
- Changed the max content line length to 2 lines for Inline Image Push.
  - This style can be found via `"Appboy.Push.InlineImage.TextArea.TitleContent.ContentText"`

##### Fixed
- Changed the `AppboyContentCardsFragment.ContentCardsUpdateRunnable` to determine network unavailability and feed emptiness based on the filtered list of cards and not the original input list of cards.
- Fixed an issue with IAM display where a deleted local image would result in a failed image display.

## 12.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v12.0.0)

##### ⚠ Breaking
- Added `getIntentFlags` to the `IAppboyNavigator` interface to more easily allow for customizing Activity launch behavior.
  - A default implementation is available below:
  ```
    @Override
    public int getIntentFlags(IntentFlagPurpose intentFlagPurpose) {
      return new AppboyNavigator().getIntentFlags(intentFlagPurpose);
    }
  ```
- Renamed `firebase_messaging_service_automatically_register_on_new_token` to `com_appboy_firebase_messaging_service_automatically_register_on_new_token` in `appboy.xml` configuration.

##### Fixed
- Fixed an issue with the default image loader not properly setting image bitmaps on API 23 and below devices.
- Fixed an issue where the `AppboyInAppMessageManager.ensureSubscribedToInAppMessageEvents()` method wouldn't properly resubscribe after disabling and re-enabling the SDK.

##### Changed
- Changed Push Stories in `AppboyNotificationStyleFactory` to use `BrazeNotificationPayload`.

## 11.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v11.0.0)

##### ⚠ Breaking
- Changed the behavior of new beta HTML In-App Messages with dashboard preview support (i.e. those with `MessageType.HTML` and not `MessageType.HTML_FULL`) to not automatically log analytics clicks on url follows in `IInAppMessageWebViewClientListener`.
  - Body click analytics will no longer automatically be collected. To continue to receive body click analytics, you must log body clicks explicitly from your message via Javascript using `appboyBridge.logClick()`.
- `IContentCardsUpdateHandler` and `IContentCardsViewBindingHandler` interfaces now extend `android.os.Parcelable`.
  - This ensures that these handlers properly transition across instance state saves and reads.
  - Examples on how to extend `Parcelable` can be found in `DefaultContentCardsUpdateHandler` and `DefaultContentCardsViewBindingHandler`.
- Renamed `AppboyFcmReceiver` to `BrazePushReceiver`.

##### Added
- Added `AppboyInAppMessageManager.getIsCurrentlyDisplayingInAppMessage()`.
- Added ability to configure whether the `AppboyFirebaseMessagingService` will automatically register tokens in its `onNewToken` method.
  - Defaults to whether FCM automatic registration is enabled. Note that FCM automatic registration is a separate configuration option and is not enabled by default.
  - Configured by changing the boolean value for `firebase_messaging_service_automatically_register_on_new_token` in your `appboy.xml`, or at runtime by setting `AppboyConfig.setIsFirebaseMessagingServiceOnNewTokenRegistrationEnabled()`.
  - Note that the Sender ID used to configure tokens received in `onNewToken()` is based on the app's default Firebase Project rather than the explicitly configured Sender ID on the Braze SDK. These should generally be the same value.

##### Changed
- Deprecated `AppboyLifecycleCallbackListener.setInAppMessagingRegistrationBlacklist()` in favor of `AppboyLifecycleCallbackListener.setInAppMessagingRegistrationBlocklist()`.
- Deprecated `AppboyConfig.Builder.setDeviceObjectWhitelist()` in favor of `AppboyConfig.Builder.setDeviceObjectAllowlist()`.
- Deprecated `AppboyConfig.Builder.setDeviceObjectWhitelistEnabled()` in favor of `AppboyConfig.Builder.setDeviceObjectAllowlistEnabled()`.

##### Fixed
- Fixed an issue where the `AppboyContentCardsFragment` would not transition a custom `IContentCardsUpdateHandler` or `IContentCardsViewBindingHandler` implementation in `onSaveInstanceState()`, which caused the defaults for both to be used instead.
- Fixed an issue with deeplink handling where push action button deeplinks would only work once throughout the lifetime of the application.

## 10.1.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v10.1.0)

##### Changed
- Changed `AppboyWebViewActivity` to extend `FragmentActivity` for better fragment management.
  - Note that `AppboyWebViewActivity` now no longer performs session and in-app message registration on its own.
  - Clients using `AppboyLifecycleCallbackListener` will see no effect.
  - Clients performing manual session integration should override `AppboyWebViewActivity` to add back this registration and set the new Activity via `AppboyConfig.Builder#setCustomWebViewActivityClass()` or `com_appboy_custom_html_webview_activity_class_name` in the `appboy.xml` file.

##### Added
- Added support for receiving messages via the Huawei Messaging Service.

##### Fixed
- Fixed minor display issues with Inline Image Push.

## 10.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v10.0.0)

##### ⚠ Breaking
- The Android SDK has now fully migrated to AndroidX dependencies. No backwards compatibility is possible with the no longer maintained Android Support Library.
  - See https://developer.android.com/jetpack/androidx for more information on AndroidX, including migration steps.
  - Braze Android 9.0.0 is the last SDK version compatible with the Android Support Library.
- Added a new interface method, `IAppboyNotificationFactory.createNotification(BrazeNotificationPayload)`.
  - The `BrazeNotificationPayload` is a data object that performs the task of extracting and surfacing values from the Braze push payload in a far more convenient way.
  - Integrations without a custom `IAppboyNotificationFactory` will have no breaking changes.
  - Integrations with a custom `IAppboyNotificationFactory` are recommended to switchover to their non-deprecated counterparts in `AppboyNotificationUtils.java`.

##### Added
- Added support for `com_appboy_inapp_show_inapp_messages_automatically` boolean configuration for Unity.

##### Fixed
- Fixed support for dark mode in HTML in-app messages and remote urls opened in `AppboyWebViewActivity` for deeplinks via the `prefers-color-scheme: dark` css style.
  - The decision to display content in dark mode will still be determined at display time based on the device's state.
- Fixed an issue where the card parameter in `com.appboy.IAppboyImageLoader.renderUrlIntoCardView()` was null for Content Cards.

##### Removed
- Removed `com.appboy.push.AppboyNotificationUtils.handleContentCardsSerializedCardIfPresent()`.

## 9.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v9.0.0)

##### ⚠ Breaking
- The Android SDK now has a source and target build compatibility set to Java 8.

##### Changed
- Simplified the email regex used in the SDK to centralize most validation on the server.
  - The original email validation used is reproduced below:
  ```
  (?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])
  ```

##### Fixed
- Fixed an issue where in-app message icon TextViews could throw a `ClassCastException` on certain devices and prevent display.

##### Removed
- Removed `com.appboy.support.AppboyImageUtils.getBitmap(android.net.Uri)` in favor of `com.appboy.support.AppboyImageUtils.getBitmap(android.content.Context, android.net.Uri, com.appboy.enums.AppboyViewBounds)`.
- Removed `com.appboy.AppboyAdmReceiver.CAMPAIGN_ID_KEY`.
  - Use `Constants.APPBOY_PUSH_CAMPAIGN_ID_KEY` instead.
- Removed `com.appboy.push.AppboyNotificationUtils.isValidNotificationPriority()`.

## 8.1.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v8.1.0)

##### Added support for Android 11 R (API 30).
- Note that apps targeting API 30 should update to this SDK version.

##### Changed
- Changed Content Card subscriptions to automatically re-fire when silent push syncs or test send cards are received via push.
- Improved several accessibility features of In-App Messages and Content Cards as per [Principles for improving app accessibility](https://developer.android.com/guide/topics/ui/accessibility/principles).
  - Changed non-informative accessibility content descriptions for in-app message and Content Card images to `@null`.
  - Content Cards now have content descriptions on their views that incorporate the title and description.
- Changed the `AppboyFirebaseMessagingService` to override the `onNewToken()` method to register a Firebase push token when automatic Firebase registration enabled.

##### Added
- Added `appboyBridge.getUser().addAlias()` to the javascript interface for HTML In-App Messages.
- Added `Appboy.getConfiguredApiKey()` to aid in determining if the SDK has an API key properly configured.
- Added an overload for `IAppboy.getCurrentUser()` that adds an asynchronous callback for when the current user is available instead of blocking on the caller thread.
  - The following is an example of the full interface:
  - ```java
    Appboy.getInstance(mContext).getCurrentUser(new IValueCallback<AppboyUser>() {
      @Override
      public void onSuccess(@NonNull AppboyUser currentUser) {
        currentUser.setFirstName("Jared");
      }

      @Override
      public void onError() {}
    });
  ```
  - A convenience class is also provided with `SimpleValueCallback`:
  - ```java
    Appboy.getInstance(mContext).getCurrentUser(new SimpleValueCallback<AppboyUser>() {
      @Override
      public void onSuccess(@NonNull AppboyUser currentUser) {
        currentUser.setFirstName("Julian");
      }
    });
  ```
- Added `AppboyInAppMessageManager.setClickOutsideModalViewDismissInAppMessageView()` allow for the dismissal of a Modal In-App Message when tapping on the frame behind the message itself.
  - The default (and historical) value is false, meaning that clicks outside the modal do not close the modal.
  - To toggle the feature on, call: `AppboyInAppMessageManager.getInstance().setClickOutsideModalViewDismissInAppMessageView(true)`

##### Fixed
- Fixed behavior of the `com.appboy.ui.AppboyContentCardsFragment` to not assign margin of the first card in the feed from the top of the feed.
- Fixed an issue with Content Card test sends where the test send wouldn't be visible in some conditions.
- Fixed an issue with regex based event property triggers not working as expected. Previously they had to match the entire string, now they will search for matches as expected. The regex is now also case-insensitive.
- Fixed an issue with `resolveActivity()` in the default `UriAction` logic not returning a valid `Activity` to handle external deeplinks on Android 11 devices without the `QUERY_ALL_PACKAGES` permission.
- Fixed an issue introduced in 4.0.1 where upgrading the SDK could result in server configuration values getting removed until the next session start.

##### Removed
- Removed `AppboyConfig.Builder.setNotificationsEnabledTrackingOn()`.
- Removed `AppboyImageUtils.getPixelsFromDp()`.
- Removed `ViewUtils.getDisplayHeight()`.

## 8.0.1

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v8.0.1)

##### Fixed
- Fixed an Activity resolution issue in `com.appboy.ui.AppboyWebViewActivity` by removing a call to `setDownloadListener()`.
- Fixed an implementation issue in 8.0.0 related to setting runtime configuration after stopping the SDK.

## 8.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v8.0.0)

##### ⚠ Breaking
* Integrators note: most of the changes listed below are on lightly used interfaces that do no affect most clients.
- Moved `InAppMessageHtmlBase.getAssetsZipRemoteUrl(), InAppMessageHtmlBase.setAssetsZipRemoteUrl()` to `InAppMessageZippedAssetHtmlBase.java`.
- Moved `AppboyInAppMessageHtmlFullView.APPBOY_BRIDGE_PREFIX` to `AppboyInAppMessageHtmlBaseView.APPBOY_BRIDGE_PREFIX`
- Renamed `IInAppMessage.getRemoteAssetPathForPrefetch` to `IInAppMessage.getRemoteAssetPathsForPrefetch` and changed signature to List<String>.
- Renamed `IInAppMessage.setLocalAssetPathForPrefetch` to `IInAppMessage.setLocalAssetPathsForPrefetch` and changed signature to Map<String, String>.
- Created In-App Message interface `IInAppMessageWithImage` for slideup, modal, and fulls to hold image based methods. These methods have been refactored out of the `IInAppMessage` interface.
  - These methods are `getImageUrl(), getRemoteImageUrl(), getLocalImageUrl(), getBitmap(), getImageDownloadSuccessful(), setImageUrl(), setLocalImageUrl(), setImageDownloadSuccessful(), setRemoteImageUrl()`, and `setBitmap()`.
- Content Card backgrounds (in the default UI), now have their colors set via `/android-sdk-ui/src/main/res/drawable-nodpi/com_appboy_content_card_background.xml`.
- Several Content Cards related style values are now fully decoupled from News Feed values and are enumerated below.
 - The color `@color/com_appboy_card_background_border` is now `@color/com_appboy_content_card_background_border` for Content Cards.
 - The color `@color/com_appboy_card_background_shadow` is now `@color/com_appboy_content_card_background_shadow` for Content Cards.
 - The color `@color/com_appboy_card_background` is now `@color/com_appboy_content_card_background` for Content Cards.
 - The color used for the text in the empty `AppboyContentCardsFragment`, `@color/com_appboy_title` is now `@color/com_appboy_content_card_empty_text_color`.
- Several News Feed dimensions values also used in Content Card styles now have Content Card specific values, enumerated below. Note that if these values were overridden in your styles for use in Content Cards, they will have to be updated to the new keys.
 - The dimension `@dimen/com_appboy_card_background_border_left` is now `@dimen/com_appboy_content_card_background_border_left`.
 - The dimension `@dimen/com_appboy_card_background_border_right` is now `@dimen/com_appboy_content_card_background_border_right`.
 - The dimension `@dimen/com_appboy_card_background_border_top` is now `@dimen/com_appboy_content_card_background_border_top`.
 - The dimension `@dimen/com_appboy_card_background_border_bottom` is now `@dimen/com_appboy_content_card_background_border_bottom`.
 - The dimension `@dimen/com_appboy_card_background_shadow_bottom` is now `@dimen/com_appboy_content_card_background_shadow_bottom`.
 - The dimension `@dimen/com_appboy_card_background_corner_radius` is now `@dimen/com_appboy_content_card_background_corner_radius`.
 - The dimension `@dimen/com_appboy_card_background_shadow_radius` is now `@dimen/com_appboy_content_card_background_shadow_radius`.
- Removed `AppboyInAppMessageHtmlJavascriptInterface(Context)` in favor of `AppboyInAppMessageHtmlJavascriptInterface(Context, IInAppMessageHtml)`.
- Removed `IAppboy.logPushDeliveryEvent()` and `AppboyNotificationUtils.logPushDeliveryEvent()`.

##### Added
- Added support for upcoming HTML In-App Message templates.
- Added `appboyBridge.logClick(String), appboyBridge.logClick()` and `appboyBridge.getUser().setLanguage()` to the javascript interface for HTML In-App Messages.
- Added support for dark mode in HTML in-app messages and remote urls opened in `AppboyWebViewActivity` for deeplinks via the `prefers-color-scheme: dark` css style.
  - The decision to display content in dark mode will be determined at display time based on the device's state.
- Added support for dark mode in the default Content Cards UI.
  - This feature is enabled by default. To disable or change, override the values present in `android-sdk-ui/src/main/res/values-night/colors.xml` and `android-sdk-ui/src/main/res/values-night/dimens.xml`.
- Added `IAppboy.subscribeToSessionUpdates()` which allows for the host app to be notified when a session is started or ended.
- Added the ability to optionally set a custom list of location providers when obtaining a single location, such as on session start. See `AppboyConfig.Builder.setCustomLocationProviderNames()` for more information.
  - The following example showcases instructing the SDK to use `LocationManager.GPS_PROVIDER` and `LocationManager.NETWORK_PROVIDER`.
    ```
      new AppboyConfig.Builder()
          .setCustomLocationProviderNames(EnumSet.of(LocationProviderName.GPS, LocationProviderName.NETWORK));
    ```
  - In xml:
    ```
      <string-array translatable="false" name="com_appboy_custom_location_providers_list">
        <item>GPS</item>
        <item>NETWORK</item>
      </string-array>
    ```
  - By default, only the passive and network providers are used when obtaining a single location from the system.
  - This change does not affect Braze Geofences.

##### Fixed
- Fixed an issue where the pending intent flags on a push story only allowed for the main deeplink to be fired once.
- Fixed behavior of the `com.appboy.ui.AppboyContentCardsFragment` to not double the margin of the first card in the feed from the top of the feed.
- Fixed an issue where calling `wipeData()` or `disableSdk()` could result in not being able to set runtime configuration afterwards.

##### Changed
- Deprecated `com.appboy.models.IInAppMessageWithImage#setImageUrl()` in favor of `com.appboy.models.IInAppMessageWithImage#setRemoteImageUrl(String)`.

## 7.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v7.0.0)

##### ⚠ Breaking
- Made several changes to the default Content Card views to more easily customize and apply ImageView styling.
  - Changed `Appboy.ContentCards.BannerImage.ImageContainer.Image` to `Appboy.ContentCards.BannerImage.Image`.
- Removed `com.appboy.ui.contentcards.view.ContentCardViewHolder.createCardImageWithStyle()`.

##### Added
- Added Czech and Ukrainian language translations for Braze UI elements.
- Added `android-sdk-base-jetified` and `android-sdk-ui-jetified` to reference jetified SDK AAR artifacts from the artifact repository.
  - This is a direct replacement for `android-sdk-ui-x` and is a more complete integration path for using the Braze SDK with AndroidX.
  - Usage as follows:
  ```
  dependencies {
    implementation "com.appboy:android-sdk-ui-jetified:${BRAZE_SDK_VERSION}"
  }
  ```
  - If previously using the `android-sdk-ui-x` module, you must replace any imports under the `com.appboy.uix.push` package to be under `com.appboy.ui.push`.
  - The gradle properties `android.enableJetifier=true` and `android.useAndroidX=true` are no longer required when using androidX libraries with the Braze SDK.
- Added Material Design Button class names to exported consumer proguard rules.
  ```
  -keepnames class android.support.design.button.MaterialButton
  -keepnames class com.google.android.material.button.MaterialButton
  ```

##### Fixed
- Fixed issue in `AppboyCardAdapter` where a card index could be out of bounds when marking a card as seen.

##### Changed
- In-App Message "test sends" from the dashboard now display automatically if your app is in the foreground.
  - Backgrounded apps will continue to receive a push notification to display the message.
  - You can disable this feature by changing the boolean value for `com_appboy_in_app_message_push_test_eager_display_enabled` in your `appboy.xml`, or at runtime by setting `AppboyConfig.setInAppMessageTestPushEagerDisplayEnabled()` to false.
- Changed `UriAction` to be more easily customizable.

##### Removed
- Removed the `android-sdk-ui-x` module. See the `Added` section for more information.
- Removed the China Push Sample app.

## 6.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v6.0.0)

##### ⚠ Breaking
- Slideup and HTML Full In-App Messages now require the device to be in touch mode at the time of display. This is enforced in their respective `IInAppMessageViewFactory` default implementations.
  - See https://developer.android.com/reference/android/view/View.html#isInTouchMode().
- Removed `ViewUtils.setFocusableInTouchModeAndRequestFocus()`.
- `AppboyUnityPlayerNativeActivity`, `AppboyOverlayActivity`, `AppboyUnityNativeInAppMessageManagerListener`, `AppboyUnityPlayerNativeActivity`, `AppboyUnityPlayerNativeActivity`, and `IAppboyUnityInAppMessageListener` have been removed from the `android-sdk-unity` project.
  - `UnityPlayerNativeActivity` was deprecated in 2015. See https://unity3d.com/unity/beta/unity5.4.0b1.

##### Added
- Added proper support for navigating and closing Braze In-App Messages with directional-pads/TV remote input devices.
- Added the ability to customize the in-app message button border radius via `@dimen/com_appboy_in_app_message_button_corner_radius`.
- Added the ability to customize the in-app message button border color stroke width via `@dimen/com_appboy_in_app_message_button_border_stroke`.
  - The stroke width used when an in-app message button border is focused is set via `@dimen/com_appboy_in_app_message_button_border_stroke_focused`.

##### Fixed
- Fixed an issue where Content Cards syncs were suppressed too often.
- Fixed an issue where in-app messages could not be closed on TVs or other devices without touch interactions.

##### Changed
- Changed in-app messages to return focus back to the view that previously held focus before a message is displayed as given via `Activity#getCurrentFocus()`.

## 5.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v5.0.0)

##### ⚠ Breaking
- Added `IInAppMessageView.hasAppliedWindowInsets()`.

##### Added
- Added `appboyBridge.logClick()` and `appboyBridge.getUser().setLanguage()` to the javascript interface for HTML In-App Messages.
- Added `Appboy.requestGeofences()` to request a Braze Geofences update for a manually provided GPS coordinate. Automatic Braze Geofence requests must be disabled to properly use this method.
  - Braze Geofences can only be requested once per session, either automatically by the SDK or manually with the above method.
- Added the ability to disable Braze Geofences from being requested automatically at session start.
  - You can do this by configuring the boolean value for `com_appboy_automatic_geofence_requests_enabled` in your `appboy.xml`.
  - You can also configure this at runtime by setting `AppboyConfig.setAutomaticGeofenceRequestEnabled()`.

##### Fixed
- Fixed an issue where multiple calls to `ViewCompat.setOnApplyWindowInsetsListener()` could result in in-app messages margins getting applied multiple times instead of exactly once.
- Fixed an issue where pure white `#ffffffff` in a dark theme in-app message would not be used when the device was in dark mode.
  - In this case, the original non-dark theme color would be used by the in-app message instead.

## 4.0.2

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v4.0.2)

##### Fixed
- Fixed an issue introduced in 4.0.0 where Content Card clicks wouldn't get forwarded to the parent RecyclerView based on its View's `clickable` status.
  - This would result in clicks not being handled or logged for Content Cards.

## 4.0.1

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v4.0.1)

##### Fixed
- Fixed an issue where in-app messages could display behind translucent status and navigation bars.

## 4.0.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v4.0.0)

##### Known Issues with version 4.0.0
- Content Card clicks are not handled or logged for Content Cards due to the `"Appboy.ContentCards"` style containing the `"clickable=true"` style. This is fixed in SDK version 4.0.2.

##### ⚠ Breaking
- Added `beforeInAppMessageViewOpened(), afterInAppMessageViewOpened(), beforeInAppMessageViewClosed(), afterInAppMessageViewClosed()` to the `IInAppMessageManagerListener` interface.
  - These methods are intended to help instrument each stage of the In-App Message View gaining and losing visibility status.
- Renamed `Card.getIsDismissible()` to `Card.getIsDismissibleByUser()`.

##### Added
- Added the ability to more easily test In-App Messages from the dashboard when sending a test push by bypassing the need to click the test push notification and instead directly display the test In-App Message when the app is in the foreground.
  - A push notification will still display if a test In-App Message push is received and the app is in the background.
  - You can enable this feature by configuring the boolean value for `com_appboy_in_app_message_push_test_eager_display_enabled` in your `appboy.xml`. The default value is false.
  - You can also enable this feature at runtime by setting `AppboyConfig.setInAppMessageTestPushEagerDisplayEnabled()` to true. The default value is false.
- Added the ability to customize how In-App Messages views are added to the view hierarchy with a custom `IInAppMessageViewWrapperFactory`.
  - See `AppboyInAppMessageManager.setCustomInAppMessageViewWrapperFactory()`.
  - For lightweight customizations, consider extending `DefaultInAppMessageViewWrapper` and overriding `getParentViewGroup()`, `getLayoutParams()`, and `addInAppMessageViewToViewGroup()`.
  - Addresses https://github.com/Appboy/appboy-android-sdk/issues/138.
- Added `Card.setIsDismissibleByUser()` to allow for integrators to disable the default swipe-to-dismiss behavior on a per-card basis.
- Added the ability to set the initial `AppboyLogger` log level via `appboy.xml`.
  - In your `appboy.xml`, set an integer value for `com_appboy_logger_initial_log_level`. The integer should correspond to a constant in `Log`, such as `Log.VERBOSE` which is 2.
  - Values set via `AppboyLogger.setLogLevel()` take precedence over values set in `appboy.xml`.
- Added the ability to use a custom Activity when opening deeplinks inside the app via a WebView. This Activity will be used in place of the default `AppboyWebViewActivity`.
  - You can do this by configuring the string value for `com_appboy_custom_html_webview_activity_class_name` in your `appboy.xml`. Note that the class name used `appboy.xml` must be the exact class name string as returned from `YourClass.class.getName()`.
  - You can also configure this at runtime by setting `AppboyConfig.setCustomWebViewActivityClass()`.
  - To retrieve the url in your custom WebView:
  ```
  final Bundle extras = getIntent().getExtras();
  if (extras.containsKey(Constants.APPBOY_WEBVIEW_URL_EXTRA)) {
    String url = extras.getString(Constants.APPBOY_WEBVIEW_URL_EXTRA);
  }
  ```

##### Fixed
- Fixed the inability to scroll through Content Cards when not using standard input mechanisms, aiding accessibility.
  - All Content Card views now have `selectable` and `focusable` attributes set to true.
  - Amazon Fire TV integrators should update to this version.
- Changed `AppboyInAppMessageHtmlUserJavascriptInterface.setCustomAttribute()` in the HTML javascript bridge to not coerce `Double` into `Float`.
- Fixed default Content Card rendering on low screen density devices. Previously, Content Cards could render without a margin and overflow off screen.
  - `@dimens/com_appboy_content_cards_max_width` now accurately sets the maximum possible width of a Content Card.
  - `@dimens/com_appboy_content_cards_divider_left_margin` and `@dimens/com_appboy_content_cards_divider_right_margin` are now used to provide a margin for Content Cards when the width of the Content Card does not exceed the max width of `@dimens/com_appboy_content_cards_max_width`.
- Fixed an issue where images in Content Cards could be resized before they had finished a layout, resulting in an 0 width/height ImageView.

##### Changed
- `InAppMessageImmersiveBase.getMessageButtons()` is now guaranteed to be non-null. When buttons are not set on the message, this list will be non-null and empty.
  - Calling `InAppMessageImmersiveBase.setMessageButtons()` with null will instead clear the `MessageButton` list
- Changed the SDK to compile against the 18.0.0 version of the Firebase Cloud Messaging dependency.
- Updated the exported `android-sdk-ui` consumer proguard rules to keep javascript interface methods.
- Changed the WebView used in HTML In-App Messages to have DOM storage enabled via `setDomStorageEnabled(true)`.
- Changed Content Cards to allow for blank or empty values for the title or description. In these situations, the `TextView`'s visibility is changed to `GONE` in the view hierarchy.

##### Removed
- Removed `Constants.APPBOY_WEBVIEW_URL_KEY`.

## 3.8.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v3.8.0)

##### ⚠ Breaking
- Added `renderUrlIntoInAppMessageView()`, `renderUrlIntoCardView()`, `getPushBitmapFromUrl()`, and `getInAppMessageBitmapFromUrl()` to the `IAppboyImageLoader` interface. These methods provide more information about the rendered object. For example, `renderUrlIntoCardView()` provides the `Card` object being rendered in the feed.
  - `IAppboyImageLoader.renderUrlIntoView()` and `IAppboyImageLoader.getBitmapFromUrl()` have been removed.
  - For maintaining behavioral parity, `renderUrlIntoInAppMessageView()` and `renderUrlIntoCardView()` can reuse your previous `IAppboyImageLoader.renderUrlIntoView()` implementation while `getPushBitmapFromUrl()` and `getInAppMessageBitmapFromUrl()` can reuse your previous `IAppboyImageLoader.getBitmapFromUrl()` implementation.
  - The Glide `IAppboyImageLoader` implementation has been updated and can be found [here](https://github.com/Appboy/appboy-android-sdk/blob/master/samples/glide-image-integration/src/main/java/com/appboy/glideimageintegration/GlideAppboyImageLoader.java).
- Removed `MessageButton#getIsSecondaryButton()` and `MessageButton#setIsSecondaryButton()`.

##### Added
- Added support for the upcoming feature, In-App Messages in Dark Mode.
  - Dark Mode enabled messages must be created from the dashboard. Braze does not dynamically theme In-App Messages for Dark Mode.
  - Added `IInAppMessageThemeable` interface to In-App Messages, which adds `enableDarkTheme()` to In-App Messages.
  - To configure/disable Braze from automatically applying a Dark Theme (when available from Braze's servers), use a custom `IInAppMessageManagerListener`.
    - ```
        if (inAppMessage instanceof IInAppMessageThemeable && ViewUtils.isDeviceInNightMode(AppboyInAppMessageManager.getInstance().getApplicationContext())) {
          ((IInAppMessageThemeable) inAppMessage).enableDarkTheme();
        }
      ```
- Added `Card.isContentCard()`.
- Added the ability to use an existing color resource for `com_appboy_default_notification_accent_color` in your `appboy.xml`.
  - For example: `<color name="com_appboy_default_notification_accent_color">@color/my_color_here</color>`.

##### Fixed
- Fixed an edge case where the `AppboyInAppMessageManager` could throw an `NullPointerException` if an in-app message was in the process of animating out while `AppboyInAppMessageManager.unregisterInAppMessageManager()` was called.
- Fixed an issue where multiple subscribers to Content Cards updates could cause a `ConcurrentModificationException` if they simultaneously attempted to mutate the list returned in `ContentCardsUpdatedEvent.getAllCards()`.
  - `ContentCardsUpdatedEvent.getAllCards()` now returns a shallow copy of the list of Content Cards model objects.
- Fixed an issue (introduced in 3.7.0) where the background color for fullscreen in-app messages was not set.
- Fixed an issue (introduced in 3.7.0) were images for fullscreen in-app messages would not appear on API 21 and below devices.

## 3.7.1

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v3.7.1)

##### Added
- Added `IInAppMessage.setExtras()` to set extras on In-App Messages.

##### Fixed
- Fixed an issue where a slow loading HTML In-App Message could throw an exception if the Activity changed before `onPageFinished()` was called.
- Removed `FEATURE_INDETERMINATE_PROGRESS` and `FEATURE_PROGRESS` from `AppboyWebViewActivity`.

## 3.7.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v3.7.0)

##### Known Issues
- This release introduced issues with in-app message unregistration (`AppboyInAppMessageManager.unregisterInAppMessageManager()`) and fullscreen in-app messages. These issues have been fixed in version 3.8.0 of the SDK.

##### Breaking
- Added the `applyWindowInsets()` method to `IInAppMessageView` interface. This allows for granular customization at the in-app message view level with respect to device notches.
- The old configuration key used in `appboy.xml` for disabling location collection `com_appboy_disable_location_collection` is now deleted. This key is replaced by `com_appboy_enable_location_collection`. The default value of `com_appboy_disable_location_collection` is false. Braze location collection is disabled by default starting with Braze SDK version 3.6.0.
- Removes the Feedback feature from the SDK. All Feedback methods on the SDK, including `Appboy.submitFeedback()` and `Appboy.logFeedbackDisplayed()`, are removed.

##### Fixed
- Changed the behavior of In-App Messages to allow analytics to be logged again when the same In-App Message is displaying a new time.

##### Changed
- Improves support for in-app messages on “notched” devices (for example, iPhone X, Pixel 3XL). Full-screen messages now expand to fill the entire screen of any phone, while covering the status bar.
- Changed the behavior of HTML In-App Messages to not display until the content has finished loading as determined via `WebViewClient#onPageFinished()` on the in-app message's `WebView`.

## 3.6.0

[Release Date](https://github.com/Appboy/appboy-android-sdk/releases/tag/v3.6.0)

##### Breaking
- External user ids (provided via `Appboy.changeUser()`), are now limited to 997 bytes in UTF-8 encoding.
  - Existing user IDs will be truncated to 997 bytes in UTF-8 encoding.
  - New user IDs (via `Appboy.changeUser()`) will be rejected if too long.
  - This byte limit can be read in code via `Constants#USER_ID_MAX_LENGTH_BYTES`.
- Added `IInAppMessage.getMessageType()` to return the `MessageType` enum for easier in-app message type checking.
- Braze location collection is disabled by default. If you choose to use our location services, you must explicitly enable location services.
  - You can do this by configuring the boolean value for `com_appboy_enable_location_collection` in your `appboy.xml`. The default value is false.
  - You can also enable location collection at runtime by setting `AppboyConfig.setIsLocationCollectionEnabled()` to true.
  - The old configuration value `com_appboy_disable_location_collection` in appboy.xml is deprecated. It should be replaced with new configuration value of `com_appboy_enable_location_collection`.

##### Added
- Added `AppboyContentCardsFragment.getContentCardsRecyclerView()` to obtain the RecyclerView associated with the Content Cards fragment.
- Added `AppboyInAppMessageManager.getDefaultInAppMessageViewFactory()` to simplify most custom implementations of `IInAppMessageViewFactory`.

##### Changed
- Changed the click target area of in-app message close buttons to 48dp. The close button drawable was increased to `20dp` from `14dp`.
  - The width/height in dp of this click target can be configured with a `dimens` override for `com_appboy_in_app_message_close_button_click_area_width` and `com_appboy_in_app_message_close_button_click_area_height` respectively.
- Changed `UriUtils.getQueryParameters()` to handle the parsing of an opaque/non-hierarchical Uri such as `mailto:` or `tel:`.

## 3.5.0

##### Breaking
- Removed `IAppboyUnitySupport` interface from Appboy singleton object. Its methods have been added to the `IAppboy` interface.
- The `IAction` in `IContentCardsActionListener.onContentCardClicked()` is now annotated as `@Nullable`. Previously, this field was always non-null.
- Fixed an issue where `FLAG_ACTIVITY_NEW_TASK` was not added to configured back stack Activities when opening push. This resulted in push notifications failing to open deep links in that situation.
  - Custom push back stack Activities are set via `AppboyConfig.setPushDeepLinkBackStackActivityClass()`.

##### Added
- Added `Appboy.getCachedContentCards()` to provide an easier way to obtain the cached/offline list of Content Cards on the device.
- Added `Appboy.deserializeContentCard()` to allow for the deserialization of a Content Card. Useful for custom integrations that store the Content Cards data models in their own storage and recreate the Content Card afterwards.

##### Changed
- Deprecated `Card.isEqualTo()` in favor of using `Card.equals()`.

##### Fixed
- Fixed behavior in Content Cards and News Feed where cards without a click action wouldn't have their client click listeners called.

## 3.4.0

##### Added
- Added support for Android 10 Q (API 29).
  - With the addition of the `android.permission.ACCESS_BACKGROUND_LOCATION` permission in Android Q, this permission is now required for Braze Geofences to work on Android Q+ devices. Please see the documentation for more information.
  - The `AppboyNotificationRoutingActivity` class is now sent with the `Intent.FLAG_ACTIVITY_NO_HISTORY` Intent flag. This is not expected to be a user visible change nor will require any integration changes.
- Added the ability to enable Braze Geofences without enabling Braze location collection. Set `AppboyConfig.setGeofencesEnabled()` or `com_appboy_geofences_enabled` in your `appboy.xml` to enable Braze Geofences.
  - Note that Braze Geofences will continue to work on existing integrations if location collection is enabled and this new configuration is not present. This new configuration is intended for integrations that want Braze Geofences, but not location collection enabled as well.
- Added `Appboy.setGoogleAdvertisingId()` to pass a Google Advertising ID and Ad Tracking Limiting enabled flag back to Braze. Note that the SDK will not automatically collect either field.

##### Fixed
- Fixed in-app message buttons not properly respecting colors when using a Material Design style theme.

##### Breaking
- Geofences on Android Q+ devices will not work without the `android.permission.ACCESS_BACKGROUND_LOCATION` permission.
- Changed the signature of `IInAppMessageManagerListener.onInAppMessageButtonClicked()` to include the in-app message of the clicked button.
- Removed the deprecated `AppboyWebViewActivity.URL_EXTRA`. Please use `Constants.APPBOY_WEBVIEW_URL_EXTRA` instead.

## 3.3.0

##### Known Issues
- If using a defined back stack Activity (set via `AppboyConfig.setPushDeepLinkBackStackActivityClass()`), then push notifications containing deep links won't be opened. This behavior is fixed in 3.4.1.

##### Changed
- Changed the behavior of push deep links to not restart the launcher activity of the app when clicked.
- Changed the broadcast receiver responsible for sealing sessions after the session timeout to use `goAsync` to lower the occurrence of ANRs on certain devices.
  - This ANR would contain the constant `APPBOY_SESSION_SHOULD_SEAL` in the Google Play Console.
- Changed the default video poster (the large black & white play icon) used by default in HTML in-app messages to be transparent.

##### Added
- Added support for `long` type event properties.

##### Fixed
- Fixed fullscreen in-app messages on notched devices rendering with a gap at the top of the in-app message.
- Fixed behavior of in-app messages where modal display would take up the entire screen after successive rotations on older devices.

## 3.2.2

##### Changed
- Improved the reliability of the session start location logic when location collection is enabled.
- Changed the in-app message trigger behavior to not perform custom event triggering until any pending server trigger requests have finished.

##### Fixed
- Fixed a bug in `AppboyInAppMessageImageView` that made images loaded with Glide appear blurry or not appear when setting an aspect ratio.

## 3.2.1

##### Added
- Added `AppboyFirebaseMessagingService.handleBrazeRemoteMessage()` to facilitate forwarding a Firebase `RemoteMessage` from your `FirebaseMessagingService` to the `AppboyFirebaseMessagingService`.
  - `AppboyFirebaseMessagingService.handleBrazeRemoteMessage()` will return false if the argument `RemoteMessage` did not originate from Braze. In that case, the `AppboyFirebaseMessagingService` will do nothing.
  - A helper method `AppboyFirebaseMessagingService.isBrazePushNotification()` will also return true if the `RemoteMessage` originated from Braze.

#### Fixed
- Fixed an issue with `AppboyInAppMessageBoundedLayout` having a custom styleable attribute that collided with a preset Android attribute.

## 3.2.0

##### Important
- Please note the breaking push changes in release 3.1.1 regarding the `AppboyFirebaseMessagingService` before upgrading to this version.

##### Fixed
- Fixed an issue where a filename's canonical path was not validated during zip file extraction.
- Fixed an issue where the SDK setup verification would erroneously always log a warning that the `AppboyFcmReceiver` was registered using the old `com.google.android.c2dm.intent.RECEIVE` intent-filter.

##### Changed
- Improved the look and feel of in-app messages to adhere to the latest UX and UI best practices. Changes affect font sizes, padding, and responsiveness across all message types. Now supports button border styling.

##### Added
- Added collection of `ActivityManager.isBackgroundRestricted()` to device collection information.

## 3.1.1

##### Breaking
- Added `AppboyFirebaseMessagingService` to directly use the Firebase messaging event `com.google.firebase.MESSAGING_EVENT`. This is now the required way to integrate Firebase push with Braze. The `AppboyFcmReceiver` should be removed from your `AndroidManifest` and replaced with the following:
  - ```
    <service android:name="com.appboy.AppboyFirebaseMessagingService">
      <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
      </intent-filter>
    </service>
    ```
  - Also note that any `c2dm` related permissions should be removed from your manifest as Braze does not require any extra permissions for `AppboyFirebaseMessagingService` to work correctly.
- Changed signature of `Appboy.logPushNotificationActionClicked()`.

##### Added
- Added ability to render HTML elements in push notifications via `AppboyConfig.setPushHtmlRenderingEnabled()` and also `com_appboy_push_notification_html_rendering_enabled` in your `appboy.xml`.
  - This allows the ability to use "multicolor" text in your push notifications.
  - Note that html rendering be used on all push notification text fields when this feature is enabled.

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
- Added an appboyBridge ready event to know precisely when the appboyBridge has finished loading in the context of an HTML in-app message.
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
- Improved in-app message triggering logic to fall back to lower priority messages when the Braze server aborts templating (e.g. from a Connected Content abort in the message body, or because the user is no longer in the correct segment for the message)

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
- Removed `AppboyInAppMessageImmersiveBaseView.getMessageButtonsView()`.
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
- Introduced support for the Content Cards feature, which will eventually replace the existing News Feed feature and adds significant capability.

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
  - We recommend that session tracking and in-app messages registration be done via an `AppboyLifecycleCallbackListener` instance using [`Application.registerActivityLifecycleCallbacks()`](https://developer.android.com/reference/android/app/Application.html#registerActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks)).
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
- Fixed a bug where relative links in `href` tags in HTML in-app messages would get passed as file Uris to the `AppboyNavigator`.

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
- Added support for GIF images in the News Feed and in in-app messages via the Facebook Fresco image library (version 0.6.1) as a provided library. If found in the parent app (your app), images and GIFs will be loaded using views from the Fresco library. In order to display GIFs, Fresco must be added as a dependency in the parent app. If not found in the parent app, News Feed cards and in-app messages will not display GIFs. To disable use of the Fresco library in the UI project, set the value of `com_appboy_enable_fresco_library_use` to false (or omit it) in your `appboy.xml`; to enable Fresco use set `com_appboy_enable_fresco_library_use` to true in your `appboy.xml`. ImageView specific attributes for News Feed cards and in-app messages, such as `scaleType`, must now be applied programmatically instead of being applied from `styles.xml`. If using Fresco and proguarding your app, please include http://frescolib.org/docs/proguard.html with your proguard config. If you are not using Fresco, add the `dontwarn com.appboy.ui.**` directive. Note: to use Fresco with Braze it must be initialized when your application launches.
- Added explicit top and bottom padding values for in-app message buttons to improve button rendering on some phones.  See the `Appboy.InAppMessage.Button` style in `styles.xml`.
- Added HTML in-app message types. HTML in-app messages consist of html along with an included zipped assets file to locally reference images, css, etc. See `CustomHtmlInAppMessageActionListener` in our Droidboy sample app for an example listener for the callbacks on the actions inside the WebView hosting the HTML in-app message.
- Added a `setAttributionData()` method to AppboyUser that sets an AttributionData object for the user. Use this method with attribution provider SDKs when attribution events are fired.

##### Changed
- Removed the need for integrating client apps to log push notifications inside their activity code.  **Please remove all calls to `Appboy.logPushNotificationOpened()` from your app as they are now all handled automatically by Braze.  Otherwise, push opens will be incorrectly logged twice.**
- In-app message views are now found in the `com.appboy.ui.inappmessage.views` package and in-app message listeners are now found in the `com.appboy.ui.inappmessage.listeners` package.

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
- Added support for changing in-app message duration from the client app.  To do this, you can modify the slideup object passed to you in the `onReceive()` delegate using the new setter method `IInAppMessage.setDurationInMilliseconds()`.

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
