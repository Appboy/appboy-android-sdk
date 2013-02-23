package com.appboy.ui;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.*;
import com.appboy.IAppboy;
import com.appboy.ui.support.StringUtils;
import com.appboy.ui.support.ValidationUtils;

/**
 * Utility class that inflates and wires the feedback form.
 *
 * When integrating the feedback form into an existing activity, set the content view to the view returned by
 * inflateFeedbackUI and call wire to add functionality to the widgets.
 *
 * TODO(martin) - should the widget styles be customizable or just allow the widget font color to be changed?
 * TODO(martin) - switch from static methods to instance methods
 *
 * Activities vs Fragments
 * Fragments were introduced in the Honeycomb release allowing for reusable UI code (although you can create a
 * Fragment without a UI) in Android API version 3.0+. They were backported in the v4 support library. Although
 * not a drop in replacement for the Honeycomb Fragments, the support fragments offer similar functionality.
 * Advantages of fragments are documented in this blog post (http://android-developers.blogspot.com/2011/02/android-30-fragments-api.html).
 *
 * Developers have 4 options when creating a UI for their app (Activities, Fragments, Support Fragments, Dialog
 * (possibly)). Providing all four versions would allow any app to use our UI but doesnt use the navigation or
 * settings options put in place by the apps developer. A better solution is to have the app inflate our layout
 * inside their activity/fragment and wire together the functionality. This would preserve the navigation and settings options.
 *
 * Theming/Styling
 * - Shipping with a styled layout means that our users are stuck with the look and feel we define (or must edit our styles.xml).
 * - Theming allows users to set the styles of widgets which we can inherit. This limits us to basic widgets (unless the users decide to style ours)
 * - Using attributes in our styles allows us to place hooks on what we think should be customizable. The set of default
 *   attributes is limited, but we can introduce our own. If we do, the user MUST specify a value for the attribute.
 * - Provide setters that can be called to override our default style
 *
 * Notes
 * - The feedback form cannot be injected into a layout that already has a ScrollView. Remove your ScrollView and
 *   let the feedback form handle the scrolling.
 */
public class FeedbackHelper {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, FeedbackHelper.class.getName());

  public static View inflateFeedbackUI(Activity activity) {
    return View.inflate(activity, R.layout.appboy_feedback, null);
  }

  public static void attachFeedbackUI(ViewGroup container, View feedbackView) {
    container.addView(feedbackView);
  }

  public static boolean wire(final View view, final IAppboy appboy, final FinishAction finishAction) {
    return wire(view, appboy, new FeedbackCustomStyle.Builder().build(), finishAction);
  }

  /**
   * Makes the widgets in this form functional
   *
   * Use FinishAction to add code that should be executed post send/cancel
   */
  public static boolean wire(final View view, final IAppboy appboy, final FeedbackCustomStyle feedbackCustomStyle, final FinishAction finishAction) {
    checkParentViewsForScrollView(view);

    final Button cancelButton = (Button) view.findViewById(R.id.com_appboy_ui_feedback_cancel);
    final Button sendButton = (Button) view.findViewById(R.id.com_appboy_ui_feedback_send);
    final CheckBox isBugCheckBox = (CheckBox) view.findViewById(R.id.com_appboy_ui_feedback_is_bug);
    final EditText messageEditText = (EditText) view.findViewById(R.id.com_appboy_ui_feedback_message);
    final EditText emailEditText = (EditText) view.findViewById(R.id.com_appboy_ui_feedback_email);

    if (cancelButton == null || sendButton == null || isBugCheckBox == null || messageEditText == null || emailEditText == null) {
      Log.e(TAG, "Unable to find feedback views");
      return false;
    }

    TextWatcher messageTextWatcher = new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence sequence, int start, int count, int after) { }
      @Override
      public void onTextChanged(CharSequence sequence, int start, int before, int count) { }
      @Override
      public void afterTextChanged(Editable sequence) {
        ensureSendButton(view, sendButton, feedbackCustomStyle);
      }
    };
    TextWatcher emailTextWatcher = new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence sequence, int start, int count, int after) { }
      @Override
      public void onTextChanged(CharSequence sequence, int start, int before, int count) { }
      @Override
      public void afterTextChanged(Editable sequence) {
        ensureSendButton(view, sendButton, feedbackCustomStyle);
      }
    };

    View.OnClickListener cancelListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (finishAction != null) {
          finishAction.onFinish();
        }
      }
    };
    View.OnClickListener sendListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        boolean isBug = isBugCheckBox.isChecked();
        String message = messageEditText.getText().toString();
        String email = emailEditText.getText().toString();

        boolean result = appboy.submitFeedback(email, message, isBug);
        // TODO(martin) - Look into error handling options
        if (!result) {
          Log.e(TAG, "Could not post feedback");
        }

        if (finishAction != null) {
          finishAction.onFinish();
        }
      }
    };

    wireMessageEditText(messageEditText, messageTextWatcher);
    wireEmailEditText(emailEditText, emailTextWatcher);
    wireCancelButton(cancelButton, cancelListener);
    wireSendButton(sendButton, sendListener);

    setStyle(view, feedbackCustomStyle);

    ensureSendButton(view, sendButton, feedbackCustomStyle);

    return true;
  }

  private static void checkParentViewsForScrollView(View view) {
    for (ViewParent parentView = view.getParent(); parentView != null; parentView = parentView.getParent()) {
      if (parentView instanceof ScrollView) {
        Log.w(TAG, "Multiple ScrollViews found in view hierarchy. Layout may not inflate properly!");
        break;
      }
    }
  }

  private static void setStyle(View view, FeedbackCustomStyle feedbackCustomStyle) {
    if (feedbackCustomStyle.getHintColor() != null) {
      setHintColor(view, feedbackCustomStyle.getHintColor());
    }
    if (feedbackCustomStyle.getFontColor() != null) {
      setFontColor(view, feedbackCustomStyle.getFontColor());
    }
    if (feedbackCustomStyle.getNavigationBarColor() != null) {
      setNavigationBarColor(view, feedbackCustomStyle.getNavigationBarColor());
    }
    if (feedbackCustomStyle.getNavigationButtonsBackgroundEnabledColor() != null) {
      setNavigationButtonsBackgroundColor(view, feedbackCustomStyle.getNavigationButtonsBackgroundEnabledColor());
    }
    if (feedbackCustomStyle.getNavigationButtonsFontColor() != null) {
      setNavigationButtonsFontColor(view, feedbackCustomStyle.getNavigationButtonsFontColor());
    }
  }

  private static void setHintColor(View view, int color) {
    EditText messageEditText = (EditText) view.findViewById(R.id.com_appboy_ui_feedback_message);
    EditText emailEditText = (EditText) view.findViewById(R.id.com_appboy_ui_feedback_email);

    messageEditText.setHintTextColor(color);
    emailEditText.setHintTextColor(color);
  }

  private static void setFontColor(View view, int color) {
    CheckBox isBugCheckBox = (CheckBox) view.findViewById(R.id.com_appboy_ui_feedback_is_bug);
    EditText messageEditText = (EditText) view.findViewById(R.id.com_appboy_ui_feedback_message);
    EditText emailEditText = (EditText) view.findViewById(R.id.com_appboy_ui_feedback_email);

    isBugCheckBox.setTextColor(color);
    messageEditText.setTextColor(color);
    emailEditText.setTextColor(color);
  }

  private static void setNavigationBarColor(View view, int color) {
    LinearLayout navigationBarLayout = (LinearLayout) view.findViewById(R.id.com_appboy_ui_feedback_navigation_bar);
    navigationBarLayout.setBackgroundColor(color);
  }

  private static void setNavigationButtonsBackgroundColor(View view, int color) {
    Button cancelButton = (Button) view.findViewById(R.id.com_appboy_ui_feedback_cancel);
    Button sendButton = (Button) view.findViewById(R.id.com_appboy_ui_feedback_send);

    Drawable cancelButtonDrawable = cancelButton.getBackground();
    cancelButtonDrawable.setColorFilter(color, PorterDuff.Mode.SRC_OVER);
    Drawable sendButtonDrawable = sendButton.getBackground();
    sendButtonDrawable.setColorFilter(color, PorterDuff.Mode.SRC_OVER);
  }

  private static void setNavigationButtonsFontColor(View view, int color) {
    Button cancelButton = (Button) view.findViewById(R.id.com_appboy_ui_feedback_cancel);
    Button sendButton = (Button) view.findViewById(R.id.com_appboy_ui_feedback_send);

    cancelButton.setTextColor(color);
    sendButton.setTextColor(color);
  }

  private static void wireMessageEditText(EditText messageEditText, TextWatcher watcher) {
    messageEditText.addTextChangedListener(watcher);
  }

  private static void wireEmailEditText(EditText emailEditText, TextWatcher watcher) {
    emailEditText.addTextChangedListener(watcher);
  }

  private static void wireCancelButton(Button cancelButton, View.OnClickListener listener) {
    cancelButton.setOnClickListener(listener);
  }

  private static void wireSendButton(Button sendButton, View.OnClickListener listener) {
    sendButton.setOnClickListener(listener);
  }

  private static boolean validatedMessage(String message) {
    return !StringUtils.isNullOrBlank(message);
  }

  private static boolean validatedEmail(String email) {
    return !StringUtils.isNullOrBlank(email) && ValidationUtils.isValidEmailAddress(email);
  }

  private static boolean validatedRequiredFields(String message, String email) {
    return validatedMessage(message) && validatedEmail(email);
  }

  private static void ensureSendButton(View view, Button sendButton, FeedbackCustomStyle feedbackCustomStyle) {
    EditText message = (EditText) view.findViewById(R.id.com_appboy_ui_feedback_message);
    EditText email = (EditText) view.findViewById(R.id.com_appboy_ui_feedback_email);
    Drawable sendButtonDrawable = sendButton.getBackground();

    if (validatedRequiredFields(message.getText().toString(), email.getText().toString())) {
      if (feedbackCustomStyle.getNavigationButtonsBackgroundEnabledColor() != null) {
        sendButtonDrawable.setColorFilter(feedbackCustomStyle.getNavigationButtonsBackgroundEnabledColor(), PorterDuff.Mode.SRC_OVER);
      }
      sendButton.setEnabled(true);
    } else {
      if (feedbackCustomStyle.getNavigationButtonsBackgroundDisabledColor() != null) {
        sendButtonDrawable.setColorFilter(feedbackCustomStyle.getNavigationButtonsBackgroundDisabledColor(), PorterDuff.Mode.SRC_OVER);
      }
      sendButton.setEnabled(false);
    }
  }
}
