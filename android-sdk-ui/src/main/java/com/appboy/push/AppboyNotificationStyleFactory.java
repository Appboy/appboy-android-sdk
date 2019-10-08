package com.appboy.push;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.enums.AppboyViewBounds;
import com.appboy.push.support.HtmlUtils;
import com.appboy.support.AppboyImageUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.IntentUtils;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;

import java.util.HashMap;
import java.util.Map;

public class AppboyNotificationStyleFactory {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyNotificationStyleFactory.class);
  /**
   * BigPictureHeight is set in
   * https://android.googlesource.com/platform/frameworks/base/+/6387d2f6dae27ba6e8481883325adad96d3010f4/core/res/res/layout/notification_template_big_picture.xml.
   */
  public static final int BIG_PICTURE_STYLE_IMAGE_HEIGHT = 192;
  private static final String STORY_SET_GRAVITY = "setGravity";
  private static final String STORY_SET_VISIBILITY = "setVisibility";
  private static final String START = "start";
  private static final String CENTER = "center";
  private static final String END = "end";
  private static final Integer[] STORY_FULL_VIEW_XML_IDS;

  static {
    Integer[] idArray = new Integer[6];
    idArray[0] = R.id.com_appboy_story_text_view;
    idArray[1] = R.id.com_appboy_story_text_view_container;
    idArray[2] = R.id.com_appboy_story_text_view_small;
    idArray[3] = R.id.com_appboy_story_text_view_small_container;
    idArray[4] = R.id.com_appboy_story_image_view;
    idArray[5] = R.id.com_appboy_story_relative_layout;
    STORY_FULL_VIEW_XML_IDS = idArray;
  }

  private static final Map<String, Integer> GRAVITY_MAP;

  static {
    Map<String, Integer> stringToGravityInt = new HashMap<String, Integer>();
    stringToGravityInt.put(START, Gravity.START);
    stringToGravityInt.put(CENTER, Gravity.CENTER);
    stringToGravityInt.put(END, Gravity.END);
    GRAVITY_MAP = stringToGravityInt;
  }

  /**
   * Returns a big style NotificationCompat.Style. If an image is present, this will be a BigPictureStyle,
   * otherwise it will be a BigTextStyle.
   */
  public static NotificationCompat.Style getBigNotificationStyle(Context context, Bundle notificationExtras, Bundle appboyExtras, NotificationCompat.Builder notificationBuilder) {
    NotificationCompat.Style style = null;

    if (notificationExtras.containsKey(Constants.APPBOY_PUSH_STORY_KEY)) {
      AppboyLogger.d(TAG, "Rendering push notification with DecoratedCustomViewStyle (Story)");
      style = getStoryStyle(context, notificationExtras, appboyExtras, notificationBuilder);
    } else if (appboyExtras != null && appboyExtras.containsKey(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY)) {
      AppboyLogger.d(TAG, "Rendering push notification with BigPictureStyle");
      style = getBigPictureNotificationStyle(context, notificationExtras, appboyExtras);
    }

    // Default style is BigTextStyle.
    if (style == null) {
      AppboyLogger.d(TAG, "Rendering push notification with BigTextStyle");
      style = getBigTextNotificationStyle(new AppboyConfigurationProvider(context), notificationExtras);
    }

    return style;
  }

  /**
   * Returns a BigTextStyle notification style initialized with the content, big title, and big summary
   * specified in the notificationExtras and appboyExtras bundles.
   * <p/>
   * If summary text exists, it will be shown in the expanded notification view.
   * If a title exists, it will override the default in expanded notification view.
   */
  public static NotificationCompat.BigTextStyle getBigTextNotificationStyle(AppboyConfigurationProvider appboyConfigurationProvider, Bundle notificationExtras) {
    if (notificationExtras != null) {
      NotificationCompat.BigTextStyle bigTextNotificationStyle = new NotificationCompat.BigTextStyle();
      String pushContent = notificationExtras.getString(Constants.APPBOY_PUSH_CONTENT_KEY);
      bigTextNotificationStyle.bigText(HtmlUtils.getHtmlSpannedTextIfEnabled(appboyConfigurationProvider, pushContent));

      String bigSummary = null;
      String bigTitle = null;

      if (notificationExtras.containsKey(Constants.APPBOY_PUSH_BIG_SUMMARY_TEXT_KEY)) {
        bigSummary = notificationExtras.getString(Constants.APPBOY_PUSH_BIG_SUMMARY_TEXT_KEY);
      }
      if (notificationExtras.containsKey(Constants.APPBOY_PUSH_BIG_TITLE_TEXT_KEY)) {
        bigTitle = notificationExtras.getString(Constants.APPBOY_PUSH_BIG_TITLE_TEXT_KEY);
      }
      if (bigSummary != null) {
        bigTextNotificationStyle.setSummaryText(HtmlUtils.getHtmlSpannedTextIfEnabled(appboyConfigurationProvider, bigSummary));
      }
      if (bigTitle != null) {
        bigTextNotificationStyle.setBigContentTitle(HtmlUtils.getHtmlSpannedTextIfEnabled(appboyConfigurationProvider, bigTitle));
      }

      return bigTextNotificationStyle;
    } else {
      return null;
    }
  }

  /**
   * Returns a DecoratedCustomViewStyle for push story.
   *
   * @param context Current context.
   * @param notificationExtras Notification extras as provided by FCM/ADM.
   * @param notificationBuilder Must be an instance of the v7 builder.
   * @return a DecoratedCustomViewStyle that describes the appearance of the push story.
   */
  public static NotificationCompat.DecoratedCustomViewStyle getStoryStyle(Context context, Bundle notificationExtras,
                                                                          Bundle appboyExtras, NotificationCompat.Builder notificationBuilder) {
    int pageIndex = getPushStoryPageIndex(notificationExtras);
    RemoteViews storyView = new RemoteViews(context.getPackageName(), R.layout.com_appboy_notification_story_one_image);
    if (!populatePushStoryPage(storyView, context, notificationExtras, appboyExtras, pageIndex)) {
      AppboyLogger.w(TAG, "Push story page was not populated correctly. Not using DecoratedCustomViewStyle.");
      return null;
    }

    NotificationCompat.DecoratedCustomViewStyle style = new NotificationCompat.DecoratedCustomViewStyle();
    int storyPages = getPushStoryPageCount(notificationExtras);
    PendingIntent previousButtonPendingIntent = createStoryTraversedPendingIntent(context, notificationExtras, (pageIndex - 1 + storyPages) % storyPages);
    storyView.setOnClickPendingIntent(R.id.com_appboy_story_button_previous, previousButtonPendingIntent);
    PendingIntent nextButtonPendingIntent = createStoryTraversedPendingIntent(context, notificationExtras, (pageIndex + 1) % storyPages);
    storyView.setOnClickPendingIntent(R.id.com_appboy_story_button_next, nextButtonPendingIntent);
    notificationBuilder.setCustomBigContentView(storyView);

    // Ensure clicks on the story don't vibrate or make noise after the story first appears
    notificationBuilder.setOnlyAlertOnce(true);
    return style;
  }

  /**
   * Returns a BigPictureStyle notification style initialized with the bitmap, big title, and big summary
   * specified in the notificationExtras and appboyExtras bundles.
   * <p/>
   * If summary text exists, it will be shown in the expanded notification view.
   * If a title exists, it will override the default in expanded notification view.
   */
  public static NotificationCompat.BigPictureStyle getBigPictureNotificationStyle(Context context, Bundle notificationExtras, Bundle appboyExtras) {
    if (appboyExtras == null || !appboyExtras.containsKey(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY)) {
      return null;
    }

    String imageUrl = appboyExtras.getString(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY);
    if (StringUtils.isNullOrBlank(imageUrl)) {
      return null;
    }

    Bitmap imageBitmap = Appboy.getInstance(context).getAppboyImageLoader()
        .getPushBitmapFromUrl(context, appboyExtras, imageUrl, AppboyViewBounds.NOTIFICATION_EXPANDED_IMAGE);
    if (imageBitmap == null) {
      AppboyLogger.d(TAG, "Failed to download image bitmap for big picture notification style. Url: " + imageUrl);
      return null;
    }

    try {
      // Images get cropped differently across different screen sizes
      // Here we grab the current screen size and scale the image to fit correctly
      // Note: if the height is greater than the width it's going to look poor, so we might
      // as well let the system modify it and not complicate things by trying to smoosh it here.
      if (imageBitmap.getWidth() > imageBitmap.getHeight()) {
        DisplayMetrics displayMetrics = AppboyImageUtils.getDefaultScreenDisplayMetrics(context);
        int bigPictureHeightPixels = AppboyImageUtils.getPixelsFromDensityAndDp(displayMetrics.densityDpi, BIG_PICTURE_STYLE_IMAGE_HEIGHT);
        // 2:1 aspect ratio
        int bigPictureWidthPixels = 2 * bigPictureHeightPixels;
        if (bigPictureWidthPixels > displayMetrics.widthPixels) {
          bigPictureWidthPixels = displayMetrics.widthPixels;
        }

        try {
          imageBitmap = Bitmap.createScaledBitmap(imageBitmap, bigPictureWidthPixels, bigPictureHeightPixels, true);
        } catch (Exception e) {
          AppboyLogger.e(TAG, "Failed to scale image bitmap, using original.", e);
        }
      }
      if (imageBitmap == null) {
        AppboyLogger.i(TAG, "Bitmap download failed for push notification. No image will be included with the notification.");
        return null;
      }

      NotificationCompat.BigPictureStyle bigPictureNotificationStyle = new NotificationCompat.BigPictureStyle();
      bigPictureNotificationStyle.bigPicture(imageBitmap);
      setBigPictureSummaryAndTitle(new AppboyConfigurationProvider(context), bigPictureNotificationStyle, notificationExtras);

      return bigPictureNotificationStyle;
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Failed to create Big Picture Style.", e);
      return null;
    }
  }

  private static PendingIntent createStoryPageClickedPendingIntent(Context context, String uriString, String useWebView, String storyPageId, String campaignId) {
    Intent storyClickedIntent = new Intent(Constants.APPBOY_STORY_CLICKED_ACTION)
        .setClass(context, AppboyNotificationRoutingActivity.class);
    storyClickedIntent
        .setFlags(storyClickedIntent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
    storyClickedIntent.putExtra(Constants.APPBOY_ACTION_URI_KEY, uriString);
    storyClickedIntent.putExtra(Constants.APPBOY_ACTION_USE_WEBVIEW_KEY, useWebView);
    storyClickedIntent.putExtra(Constants.APPBOY_STORY_PAGE_ID, storyPageId);
    storyClickedIntent.putExtra(Constants.APPBOY_CAMPAIGN_ID, campaignId);
    return PendingIntent.getActivity(context, IntentUtils.getRequestCode(), storyClickedIntent, PendingIntent.FLAG_ONE_SHOT);
  }

  private static PendingIntent createStoryTraversedPendingIntent(Context context, Bundle notificationExtras, int pageIndex) {
    Intent storyNextClickedIntent = new Intent(Constants.APPBOY_STORY_TRAVERSE_CLICKED_ACTION).setClass(context, AppboyNotificationUtils.getNotificationReceiverClass());
    if (notificationExtras != null) {
      notificationExtras.putInt(Constants.APPBOY_STORY_INDEX_KEY, pageIndex);
      storyNextClickedIntent.putExtras(notificationExtras);
    }
    return PendingIntent.getBroadcast(context, IntentUtils.getRequestCode(), storyNextClickedIntent, PendingIntent.FLAG_ONE_SHOT);
  }

  /**
   * Given a notificationExtras with properly formatted image keys (consecutively numbered beginning
   * at 0), returns the number of images in the push story.
   *
   * @param notificationExtras Notification extras as provided by FCM/ADM.
   * @return The number of images keyed in the given notificationExtras
   */
  @VisibleForTesting
  static int getPushStoryPageCount(Bundle notificationExtras) {
    int index = 0;
    while (pushStoryPageExistsForIndex(notificationExtras, index)) {
      index++;
    }
    return index;
  }

  @VisibleForTesting
  static boolean pushStoryPageExistsForIndex(Bundle notificationExtras, int index) {
    return AppboyNotificationActionUtils.getActionFieldAtIndex(index, notificationExtras,
        Constants.APPBOY_PUSH_STORY_IMAGE_KEY_TEMPLATE, null) != null;
  }

  /**
   * Returns the current page index of the push story.
   *
   * @param notificationExtras Notification extras as provided by FCM/ADM.
   * @return The current page index.
   */
  @VisibleForTesting
  static int getPushStoryPageIndex(Bundle notificationExtras) {
    if (!notificationExtras.containsKey(Constants.APPBOY_STORY_INDEX_KEY)) {
      return 0;
    }
    return notificationExtras.getInt(Constants.APPBOY_STORY_INDEX_KEY);
  }

  /**
   * Adds the appropriate image, title/subtitle, and PendingIntents to the story page.
   *
   * @param view The push story remoteView, as instantiated in the getStoryStyle method.
   * @param context Current context.
   * @param notificationExtras Notification extras as provided by FCM/ADM.
   * @param index The index of the story page.
   * @return True if the push story page was populated correctly.
   */
  private static boolean populatePushStoryPage(RemoteViews view, Context context, Bundle notificationExtras, Bundle appboyExtras, int index) {
    AppboyConfigurationProvider configurationProvider = new AppboyConfigurationProvider(context);

    // Set up title
    String pageTitle = AppboyNotificationActionUtils.getActionFieldAtIndex(index,
        notificationExtras, Constants.APPBOY_PUSH_STORY_TITLE_KEY_TEMPLATE);

    // If the title is null or blank, the visibility of the container becomes GONE.
    if (!StringUtils.isNullOrBlank(pageTitle)) {
      view.setTextViewText(STORY_FULL_VIEW_XML_IDS[0], HtmlUtils.getHtmlSpannedTextIfEnabled(configurationProvider, pageTitle));
      String titleGravityKey = AppboyNotificationActionUtils.getActionFieldAtIndex(index,
          notificationExtras, Constants.APPBOY_PUSH_STORY_TITLE_JUSTIFICATION_KEY_TEMPLATE, CENTER);
      int titleGravity = GRAVITY_MAP.get(titleGravityKey);
      view.setInt(STORY_FULL_VIEW_XML_IDS[1], STORY_SET_GRAVITY, titleGravity);
    } else {
      view.setInt(STORY_FULL_VIEW_XML_IDS[1], STORY_SET_VISIBILITY, View.GONE);
    }

    // Set up subtitle
    String pageSubtitle = AppboyNotificationActionUtils.getActionFieldAtIndex(index,
        notificationExtras, Constants.APPBOY_PUSH_STORY_SUBTITLE_KEY_TEMPLATE);

    //If the subtitle is null or blank, the visibility of the container becomes GONE.
    if (!StringUtils.isNullOrBlank(pageSubtitle)) {
      view.setTextViewText(STORY_FULL_VIEW_XML_IDS[2], HtmlUtils.getHtmlSpannedTextIfEnabled(configurationProvider, pageSubtitle));
      String subtitleGravityKey = AppboyNotificationActionUtils.getActionFieldAtIndex(index,
          notificationExtras, Constants.APPBOY_PUSH_STORY_SUBTITLE_JUSTIFICATION_KEY_TEMPLATE,
          CENTER);
      int subtitleGravity = GRAVITY_MAP.get(subtitleGravityKey);
      view.setInt(STORY_FULL_VIEW_XML_IDS[3], STORY_SET_GRAVITY, subtitleGravity);
    } else {
      view.setInt(STORY_FULL_VIEW_XML_IDS[3], STORY_SET_VISIBILITY, View.GONE);
    }

    // Set up bitmap url
    String bitmapUrl = AppboyNotificationActionUtils.getActionFieldAtIndex(index,
        notificationExtras, Constants.APPBOY_PUSH_STORY_IMAGE_KEY_TEMPLATE);
    Bitmap largeNotificationBitmap = Appboy.getInstance(context).getAppboyImageLoader()
        .getPushBitmapFromUrl(context, appboyExtras, bitmapUrl, AppboyViewBounds.NOTIFICATION_ONE_IMAGE_STORY);
    if (largeNotificationBitmap == null) {
      return false;
    }
    view.setImageViewBitmap(STORY_FULL_VIEW_XML_IDS[4], largeNotificationBitmap);

    // Set up story clicked intent
    String campaignId = notificationExtras.getString(Constants.APPBOY_PUSH_CAMPAIGN_ID_KEY);
    String storyPageId = AppboyNotificationActionUtils.getActionFieldAtIndex(index,
        notificationExtras, Constants.APPBOY_PUSH_STORY_ID_KEY_TEMPLATE, "");
    String deepLink = AppboyNotificationActionUtils.getActionFieldAtIndex(index,
        notificationExtras, Constants.APPBOY_PUSH_STORY_DEEP_LINK_KEY_TEMPLATE);
    String useWebView = AppboyNotificationActionUtils.getActionFieldAtIndex(index,
        notificationExtras, Constants.APPBOY_PUSH_STORY_USE_WEBVIEW_KEY_TEMPLATE);
    PendingIntent storyClickedPendingIntent = createStoryPageClickedPendingIntent(context,
        deepLink, useWebView, storyPageId, campaignId);
    view.setOnClickPendingIntent(STORY_FULL_VIEW_XML_IDS[5], storyClickedPendingIntent);
    return true;
  }

  @VisibleForTesting
  static void setBigPictureSummaryAndTitle(AppboyConfigurationProvider appboyConfigurationProvider,
                                           NotificationCompat.BigPictureStyle bigPictureNotificationStyle, Bundle notificationExtras) {
    String bigSummary = null;
    String bigTitle = null;

    if (notificationExtras.containsKey(Constants.APPBOY_PUSH_BIG_SUMMARY_TEXT_KEY)) {
      bigSummary = notificationExtras.getString(Constants.APPBOY_PUSH_BIG_SUMMARY_TEXT_KEY);
    }
    if (notificationExtras.containsKey(Constants.APPBOY_PUSH_BIG_TITLE_TEXT_KEY)) {
      bigTitle = notificationExtras.getString(Constants.APPBOY_PUSH_BIG_TITLE_TEXT_KEY);
    }

    if (bigSummary != null) {
      bigPictureNotificationStyle.setSummaryText(HtmlUtils.getHtmlSpannedTextIfEnabled(appboyConfigurationProvider, bigSummary));
    }
    if (bigTitle != null) {
      bigPictureNotificationStyle.setBigContentTitle(HtmlUtils.getHtmlSpannedTextIfEnabled(appboyConfigurationProvider, bigTitle));
    }

    // If summary is null (which we set to the subtext in setSummaryTextIfPresentAndSupported in AppboyNotificationUtils)
    // and bigSummary is null, set the summary to the message. Without this, the message would be blank in expanded mode.
    String summaryText = notificationExtras.getString(Constants.APPBOY_PUSH_SUMMARY_TEXT_KEY);
    if (summaryText == null && bigSummary == null) {
      String contentText = notificationExtras.getString(Constants.APPBOY_PUSH_CONTENT_KEY);
      bigPictureNotificationStyle.setSummaryText(HtmlUtils.getHtmlSpannedTextIfEnabled(appboyConfigurationProvider, contentText));
    }
  }
}
