package com.appboy.push.support;

import android.os.Build;
import android.text.Html;

import com.appboy.support.StringUtils;
import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.support.BrazeLogger;

public class HtmlUtils {
  private static final String TAG = BrazeLogger.getBrazeLogTag(HtmlUtils.class);

  /**
   * Returns displayable styled text from the provided HTML
   * string, if {@link com.braze.configuration.BrazeConfigurationProvider#getIsPushNotificationHtmlRenderingEnabled()} is enabled.
   * <br>
   * When disabled, returns the input text.
   */
  @SuppressWarnings("deprecation") // fromHtml(String)
  public static CharSequence getHtmlSpannedTextIfEnabled(BrazeConfigurationProvider configurationProvider, String text) {
    if (StringUtils.isNullOrBlank(text)) {
      BrazeLogger.d(TAG, "Cannot create html spanned text on null or empty text. Returning blank string.");
      return text;
    }
    if (configurationProvider.getIsPushNotificationHtmlRenderingEnabled()) {
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
