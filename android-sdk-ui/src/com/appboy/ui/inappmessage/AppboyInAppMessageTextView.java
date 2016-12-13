package com.appboy.ui.inappmessage;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.appboy.Constants;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;

public class AppboyInAppMessageTextView extends TextView {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyInAppMessageTextView.class.getName());

  public AppboyInAppMessageTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    try {
      // Get the array of offset indices into the R value array defined for this view.
      // The R value array is at R.styleable.com_appboy_ui_inappmessage_AppboyInAppMessageTextView.
      TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.com_appboy_ui_inappmessage_AppboyInAppMessageTextView);

      // For all offsets defined on this view, if the offset is equal to the offset for the custom font file
      // defined at R.styleable.com_appboy_ui_inappmessage_AppboyInAppMessageTextView_appboyInAppMessageCustomFontFile,
      // instruct the typed array to retrieve the data at that offset.
      for (int i = 0; i < typedArray.getIndexCount(); i++) {
        int offset = typedArray.getIndex(i);
        if (offset == R.styleable.com_appboy_ui_inappmessage_AppboyInAppMessageTextView_appboyInAppMessageCustomFontFile) {
          String fontFile = typedArray.getString(offset);
          try {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontFile);
            this.setTypeface(typeface);
          } catch (Exception e) {
            AppboyLogger.w(TAG, "Error loading custom typeface from: " + fontFile, e);
          }
        }
      }
      typedArray.recycle();
    } catch (Exception e) {
      AppboyLogger.w(TAG, "Error while checking for custom typeface.", e);
    }
  }
}