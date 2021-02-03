package com.appboy.push.support;

import android.os.Build;
import android.text.Html;

import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;

public class HtmlUtils {
  private static final String TAG = AppboyLogger.getBrazeLogTag(HtmlUtils.class);

  /**
   * Returns displayable styled text from the provided HTML
   * string, if {@link AppboyConfigurationProvider#getIsPushNotificationHtmlRenderingEnabled()} is enabled.
   * <br>
   * When disabled, returns the input text.
   */
  @SuppressWarnings("deprecation") // fromHtml(String)
  public static CharSequence getHtmlSpannedTextIfEnabled(AppboyConfigurationProvider appboyConfigurationProvider, String text) {
    if (StringUtils.isNullOrBlank(text)) {
      AppboyLogger.d(TAG, "Cannot create html spanned text on null or empty text. Returning blank string.");
      return text;
    }
    if (appboyConfigurationProvider.getIsPushNotificationHtmlRenderingEnabled()) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
      } else {
        return Html.fromHtml(text);
      }
    } else {
      return text;
    }
  }
}
