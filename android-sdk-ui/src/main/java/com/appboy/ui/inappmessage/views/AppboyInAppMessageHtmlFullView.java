package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsCompat;

import com.appboy.ui.R;

public class AppboyInAppMessageHtmlFullView extends AppboyInAppMessageHtmlBaseView {
  public AppboyInAppMessageHtmlFullView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public int getWebViewViewId() {
    return R.id.com_appboy_inappmessage_html_full_webview;
  }

  @Override
  public void applyWindowInsets(@NonNull WindowInsetsCompat insets) {
    // HTML Full in-app messages don't have special behavior with respect to notched devices at the View level.
  }

  @Override
  public boolean hasAppliedWindowInsets() {
    // HTML in-app messages don't have special behavior with respect to notched devices at the View level.
    // Thus we return true here to short-circuit any extra inset handling behavior.
    return true;
  }
}
