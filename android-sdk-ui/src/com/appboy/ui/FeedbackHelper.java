package com.appboy.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import com.appboy.IAppboy;
import com.appboy.support.ValidationUtils;
import com.appboy.ui.support.StringUtils;

/**
 * Utility class that inflates and wires the feedback form.
 *
 * When integrating the feedback form into an existing activity, set the content view to the view returned by
 * inflateFeedbackUI and call wire to add functionality to the widgets.
 *
 * Notes
 * - The feedback form cannot be injected into a layout that already has a ScrollView. Remove your ScrollView and
 *   let the feedback form handle the scrolling.
 */
public class FeedbackHelper {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, FeedbackHelper.class.getName());

  public static void attachFeedbackUI(ViewGroup container, View feedbackView) {
    container.addView(feedbackView);
  }

  /**
   * Makes the widgets in this form functional
   *
   * Use FinishAction to add code that should be executed post send/cancel
   */
  public static boolean wire(final View feedbackView, final IAppboy appboy, final FinishAction finishAction) {
    checkParentViewsForScrollView(feedbackView);

    final Button cancelButton = (Button) feedbackView.findViewById(R.id.com_appboy_feedback_cancel);
    final Button sendButton = (Button) feedbackView.findViewById(R.id.com_appboy_feedback_send);
    final CheckBox isBugCheckBox = (CheckBox) feedbackView.findViewById(R.id.com_appboy_feedback_is_bug);
    final EditText messageEditText = (EditText) feedbackView.findViewById(R.id.com_appboy_feedback_message);
    final EditText emailEditText = (EditText) feedbackView.findViewById(R.id.com_appboy_feedback_email);

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
        ensureSendButton(feedbackView, sendButton);
      }
    };
    TextWatcher emailTextWatcher = new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence sequence, int start, int count, int after) { }
      @Override
      public void onTextChanged(CharSequence sequence, int start, int before, int count) { }
      @Override
      public void afterTextChanged(Editable sequence) {
        ensureSendButton(feedbackView, sendButton);
      }
    };

    final View.OnClickListener cancelListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (finishAction != null) {
          finishAction.onFinish();
        }
        clearData(feedbackView);
      }
    };
    View.OnClickListener sendListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        boolean isBug = isBugCheckBox.isChecked();
        String message = messageEditText.getText().toString();
        String email = emailEditText.getText().toString();

        boolean result = appboy.submitFeedback(email, message, isBug);
        if (!result) {
          Log.e(TAG, "Could not post feedback");
        }

        if (finishAction != null) {
          finishAction.onFinish();
        }
        clearData(feedbackView);
      }
    };

    wireMessageEditText(messageEditText, messageTextWatcher);
    wireEmailEditText(emailEditText, emailTextWatcher);
    wireCancelButton(cancelButton, cancelListener);
    wireSendButton(sendButton, sendListener);

    ensureSendButton(feedbackView, sendButton);
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

  private static void ensureSendButton(View view, Button sendButton) {
    EditText message = (EditText) view.findViewById(R.id.com_appboy_feedback_message);
    EditText email = (EditText) view.findViewById(R.id.com_appboy_feedback_email);

    if (validatedRequiredFields(message.getText().toString(), email.getText().toString())) {
      sendButton.setEnabled(true);
    } else {
      sendButton.setEnabled(false);
    }
  }

  private static void clearData(View view) {
    EditText emailEditText = (EditText) view.findViewById(R.id.com_appboy_feedback_email);
    EditText messageEditText = (EditText) view.findViewById(R.id.com_appboy_feedback_message);
    CheckBox isBugCheckBox = (CheckBox) view.findViewById(R.id.com_appboy_feedback_is_bug);
    emailEditText.setText("");
    messageEditText.setText("");
    isBugCheckBox.setChecked(false);
  }
}
