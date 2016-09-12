package com.appboy.sample.logging;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.sample.R;
import com.appboy.sample.util.ButtonUtils;
import com.appboy.support.StringUtils;

public class CustomUserAttributeDialog extends DialogPreference {
  private static final String DEFAULT = "default";
  private EditText mCustomAttributeKey;
  private EditText mCustomAttributeValue;
  private EditText mCustomAttributeIncrementKey;
  private EditText mCustomAttributeIncrementValue;
  private EditText mCustomAttributeUnsetKey;
  private EditText mCustomAttributeArrayKey;
  private EditText mCustomAttributeArrayValue;
  private RadioGroup mCustomAttributeArrayChoices;
  private CheckBox mRequestFlush;
  private View mView;

  public CustomUserAttributeDialog(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    setDialogLayoutResource(R.layout.custom_attribute);
    setPersistent(false);
  }

  @Override
  protected View onCreateDialogView() {
    mView = super.onCreateDialogView();
    mCustomAttributeKey = (EditText) mView.findViewById(R.id.custom_attribute_key);
    mCustomAttributeValue = (EditText) mView.findViewById(R.id.custom_attribute_value);
    mCustomAttributeIncrementKey = (EditText) mView.findViewById(R.id.custom_attribute_increment_key);
    mCustomAttributeIncrementValue = (EditText) mView.findViewById(R.id.custom_attribute_increment_value);
    mCustomAttributeUnsetKey = (EditText) mView.findViewById(R.id.custom_attribute_unset_key);
    mCustomAttributeArrayKey = (EditText) mView.findViewById(R.id.custom_attribute_array_key);
    mCustomAttributeArrayValue = (EditText) mView.findViewById(R.id.custom_attribute_array_value);
    mCustomAttributeArrayChoices = (RadioGroup) mView.findViewById(R.id.custom_attribute_array_radio);
    mRequestFlush = (CheckBox) mView.findViewById(R.id.custom_logging_flush_checkbox);

    mCustomAttributeArrayChoices.check(R.id.custom_attribute_array_set);

    ButtonUtils.setUpPopulateButton(mView, R.id.custom_attribute_button, mCustomAttributeKey, "color", mCustomAttributeValue, "green");
    ButtonUtils.setUpPopulateButton(mView, R.id.custom_attribute_increment_button, mCustomAttributeIncrementKey, "height", mCustomAttributeIncrementValue, "10");
    ButtonUtils.setUpPopulateButton(mView, R.id.custom_attribute_unset_button, mCustomAttributeUnsetKey, "color");
    ButtonUtils.setUpPopulateButton(mView, R.id.custom_attribute_array_button, mCustomAttributeArrayKey, "toys", mCustomAttributeArrayValue, "doll");

    mRequestFlush.setChecked(false);
    return mView;
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);
    if (positiveResult) {
      logEvent();
    }
  }

  private void logEvent() {
    String customAttributeKeyName = mCustomAttributeKey.getText().toString();
    String customAttributeValueName = mCustomAttributeValue.getText().toString();
    String customAttributeIncrementKeyName = mCustomAttributeIncrementKey.getText().toString();
    String customAttributeIncrementValueName = mCustomAttributeIncrementValue.getText().toString();
    String customAttributeUnsetKeyName = mCustomAttributeUnsetKey.getText().toString();
    String customAttributeArrayKey = mCustomAttributeArrayKey.getText().toString();
    String customAttributeArrayValue = mCustomAttributeArrayValue.getText().toString();
    int attributeArrayResourceId = mCustomAttributeArrayChoices.getCheckedRadioButtonId();

    if (!StringUtils.isNullOrBlank(customAttributeKeyName)) {
      if (StringUtils.isNullOrBlank(customAttributeValueName)) {
        customAttributeValueName = DEFAULT;
      }
      AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
      notifyResult(appboyUser.setCustomUserAttribute(customAttributeKeyName, customAttributeValueName),
          "set user attribute! key=" + customAttributeKeyName + ", value=" + customAttributeValueName);
    }
    if (!StringUtils.isNullOrBlank(customAttributeIncrementKeyName)) {
      AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
      if (!StringUtils.isNullOrBlank(customAttributeIncrementValueName)) {
        int incrementValue = Integer.parseInt(customAttributeIncrementValueName);
        notifyResult(appboyUser.incrementCustomUserAttribute(customAttributeIncrementKeyName, incrementValue),
            "Increment user attribute! key=" + customAttributeIncrementKeyName + ", value=" + customAttributeIncrementValueName);
      } else {
        notifyResult(appboyUser.incrementCustomUserAttribute(customAttributeIncrementKeyName),
            "Increment user attribute! key=" + customAttributeIncrementKeyName + ", value=1");
      }
    }
    if (!StringUtils.isNullOrBlank(customAttributeUnsetKeyName)) {
      AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
      notifyResult(appboyUser.unsetCustomUserAttribute(customAttributeUnsetKeyName), "Unset user attribute! key=" + customAttributeUnsetKeyName);
    }
    if (!StringUtils.isNullOrBlank(customAttributeArrayKey)) {
      if (StringUtils.isNullOrBlank(customAttributeArrayValue)) {
        customAttributeArrayValue = DEFAULT;
      }
      AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
      switch (attributeArrayResourceId) {
        case R.id.custom_attribute_array_set:
          String[] attributeArray = new String[]{customAttributeArrayValue};
          notifyResult(appboyUser.setCustomAttributeArray(customAttributeArrayKey, attributeArray),
              "setCustomAttributeArray! Setting new array key=" + customAttributeArrayKey + ", values={" + customAttributeArrayValue + "}");
          break;
        case R.id.custom_attribute_array_add:
          notifyResult(appboyUser.addToCustomAttributeArray(customAttributeArrayKey, customAttributeArrayValue),
              "addToCustomAttributeArray! Adding value=" + customAttributeArrayValue + " to array with key=" + customAttributeArrayKey + ".");
          break;
        case R.id.custom_attribute_array_remove:
          notifyResult(appboyUser.removeFromCustomAttributeArray(customAttributeArrayKey, customAttributeArrayValue),
              "removeFromCustomAttributeArray! Will remove value=" + customAttributeArrayValue + " from array with key=" + customAttributeArrayKey + ".");
          break;
        default:
          notifyResult(false, "Error parsing attribute array radio button: " + attributeArrayResourceId);
      }
    }

    // Flushing manually is not recommended in almost all production situations as
    // Appboy automatically flushes data to its servers periodically.  This call
    // is solely for testing purposes.
    if (mRequestFlush.isChecked()) {
      Appboy.getInstance(getContext()).requestImmediateDataFlush();
    }
  }

  private void notifyResult(boolean result, String input) {
    if (result) {
      Toast.makeText(getContext(), "Successfully logged " + input + ".", Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(getContext(), "Failed to log " + input + ".", Toast.LENGTH_LONG).show();
    }
  }
}
