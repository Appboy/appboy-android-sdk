package com.appboy.ui.inappmessage;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.appboy.Constants;
import com.appboy.ui.R;

public class AppboyInAppMessageTextView extends TextView {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyInAppMessageTextView.class.getName());

  public AppboyInAppMessageTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    try {
      TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.com_appboy_ui_inappmessage_AppboyInAppMessageTextView);
      for (int i = 0; i < typedArray.getIndexCount(); i++) {
        if (typedArray.getIndex(i) == R.styleable.com_appboy_ui_inappmessage_AppboyInAppMessageTextView_appboyInAppMessageCustomFontFile) {
          String fontFile = typedArray.getString(i);
          try {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontFile);
            this.setTypeface(typeface);
          } catch (Exception e) {
            Log.w(TAG, "Error loading custom typeface from: " + fontFile, e);
          }
        }
      }
      typedArray.recycle();
    } catch (Exception e) {
      Log.w(TAG, "Error while checking for custom typeface.", e);
    }
  }
}