package com.appboy.sample.logging;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.sample.R;
import com.appboy.sample.util.ButtonUtils;
import com.appboy.support.StringUtils;

public class CustomUserAttributeDialog extends DialogPreference {
  private static final String EMPTY_STRING = "";
  private static final String DEFAULT_NUMBER_VALUE = "5";
  private EditText mCustomAttributeKey;
  private EditText mCustomAttributeValue;
  private EditText mCustomAttributeNumberKey;
  private EditText mCustomAttributeNumberValue;
  private EditText mCustomAttributeIncrementKey;
  private EditText mCustomAttributeIncrementValue;
  private EditText mCustomAttributeUnsetKey;
  private EditText mCustomAttributeArrayKey;
  private EditText mCustomAttributeArrayValue;
  private RadioGroup mCustomAttributeArrayChoices;

  public CustomUserAttributeDialog(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    setDialogLayoutResource(R.layout.custom_attribute);
    setPersistent(false);
  }

  @Override
  protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
    super.onPrepareDialogBuilder(builder);

    builder.setNeutralButton(R.string.user_dialog_cancel, this);
  }

  @Override
  protected View onCreateDialogView() {
    View view = super.onCreateDialogView();
    mCustomAttributeKey = view.findViewById(R.id.custom_attribute_key);
    mCustomAttributeValue = view.findViewById(R.id.custom_attribute_value);
    mCustomAttributeNumberKey = view.findViewById(R.id.custom_number_attribute_key);
    mCustomAttributeNumberValue = view.findViewById(R.id.custom_number_attribute_value);
    mCustomAttributeIncrementKey = view.findViewById(R.id.custom_attribute_increment_key);
    mCustomAttributeIncrementValue = view.findViewById(R.id.custom_attribute_increment_value);
    mCustomAttributeUnsetKey = view.findViewById(R.id.custom_attribute_unset_key);
    mCustomAttributeArrayKey = view.findViewById(R.id.custom_attribute_array_key);
    mCustomAttributeArrayValue = view.findViewById(R.id.custom_attribute_array_value);
    mCustomAttributeArrayChoices = view.findViewById(R.id.custom_attribute_array_radio);

    mCustomAttributeArrayChoices.check(R.id.custom_attribute_array_set);

    ButtonUtils.setUpPopulateButton(view, R.id.custom_attribute_button, mCustomAttributeKey, "color", mCustomAttributeValue, "green");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_number_attribute_button, mCustomAttributeNumberKey, "leagues", mCustomAttributeNumberValue, DEFAULT_NUMBER_VALUE);
    ButtonUtils.setUpPopulateButton(view, R.id.custom_attribute_increment_button, mCustomAttributeIncrementKey, "height", mCustomAttributeIncrementValue, "10");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_attribute_unset_button, mCustomAttributeUnsetKey, "color");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_attribute_array_button, mCustomAttributeArrayKey, "toys", mCustomAttributeArrayValue, "doll");

    return view;
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    super.onClick(dialog, which);

    switch (which) {
      case DialogInterface.BUTTON_NEGATIVE:
        onDialogCloseButtonClicked(true);
        break;
      case DialogInterface.BUTTON_POSITIVE:
        onDialogCloseButtonClicked(false);
        break;
      case DialogInterface.BUTTON_NEUTRAL:
      default:
        dialog.dismiss();
        break;
    }
  }

  private void onDialogCloseButtonClicked(boolean isImmediateFlushRequired) {
    String customAttributeKeyName = mCustomAttributeKey.getText().toString();
    String customAttributeValueName = mCustomAttributeValue.getText().toString();
    String customNumberAttributeKeyName = mCustomAttributeNumberKey.getText().toString();
    // Note, this is a number in string form, we'll have to parse it later
    String customNumberAttributeValueName = mCustomAttributeNumberValue.getText().toString();
    String customAttributeIncrementKeyName = mCustomAttributeIncrementKey.getText().toString();
    String customAttributeIncrementValueName = mCustomAttributeIncrementValue.getText().toString();
    String customAttributeUnsetKeyName = mCustomAttributeUnsetKey.getText().toString();
    String customAttributeArrayKey = mCustomAttributeArrayKey.getText().toString();
    String customAttributeArrayValue = mCustomAttributeArrayValue.getText().toString();
    int attributeArrayResourceId = mCustomAttributeArrayChoices.getCheckedRadioButtonId();

    if (!StringUtils.isNullOrBlank(customAttributeKeyName)) {
      if (StringUtils.isNullOrBlank(customAttributeValueName)) {
        customAttributeValueName = EMPTY_STRING;
      }
      AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
      notifyResult(appboyUser.setCustomUserAttribute(customAttributeKeyName, customAttributeValueName),
          "set user attribute! key=" + customAttributeKeyName + ", value=" + customAttributeValueName);
    }
    if (!StringUtils.isNullOrBlank(customNumberAttributeKeyName)) {
      if (StringUtils.isNullOrBlank(customNumberAttributeValueName)) {
        customNumberAttributeValueName = DEFAULT_NUMBER_VALUE;
      }
      // Convert the value into an integer
      int numberValue = stringToInteger(customNumberAttributeValueName);

      AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
      notifyResult(appboyUser.setCustomUserAttribute(customNumberAttributeKeyName, numberValue),
          "set user number attribute! key=" + customAttributeKeyName + ", value=" + numberValue);
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
        customAttributeArrayValue = EMPTY_STRING;
      }
      AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
      switch (attributeArrayResourceId) {
        case R.id.custom_attribute_array_set:
          String[] attributeArray = {customAttributeArrayValue};
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
    // Braze automatically flushes data to its servers periodically. This call
    // is solely for testing purposes.
    if (isImmediateFlushRequired) {
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

  /**
   * Parses a string into an integer. Defaults to 1 if the string cannot be parsed into an integer.
   */
  private int stringToInteger(String num) {
    try {
      return Integer.parseInt(num);
    } catch (NumberFormatException e) {
      return 5;
    }
  }
}
