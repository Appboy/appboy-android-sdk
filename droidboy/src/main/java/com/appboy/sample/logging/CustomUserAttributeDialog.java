package com.appboy.sample.logging;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appboy.sample.R;
import com.appboy.sample.dialog.CustomDialogBase;
import com.appboy.sample.util.ButtonUtils;
import com.appboy.support.StringUtils;
import com.braze.Braze;
import com.braze.BrazeUser;

public class CustomUserAttributeDialog extends CustomDialogBase {
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

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.custom_attribute, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

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
  }

  @Override
  public void onExitButtonPressed(boolean isImmediateFlushRequired) {
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
      BrazeUser brazeUser = Braze.getInstance(getContext()).getCurrentUser();
      notifyResult(brazeUser.setCustomUserAttribute(customAttributeKeyName, customAttributeValueName),
          "set user attribute! key=" + customAttributeKeyName + ", value=" + customAttributeValueName);
    }
    if (!StringUtils.isNullOrBlank(customNumberAttributeKeyName)) {
      if (StringUtils.isNullOrBlank(customNumberAttributeValueName)) {
        customNumberAttributeValueName = DEFAULT_NUMBER_VALUE;
      }
      // Convert the value into an integer
      int numberValue = stringToInteger(customNumberAttributeValueName);

      BrazeUser brazeUser = Braze.getInstance(getContext()).getCurrentUser();
      notifyResult(brazeUser.setCustomUserAttribute(customNumberAttributeKeyName, numberValue),
          "set user number attribute! key=" + customAttributeKeyName + ", value=" + numberValue);
    }
    if (!StringUtils.isNullOrBlank(customAttributeIncrementKeyName)) {
      BrazeUser brazeUser = Braze.getInstance(getContext()).getCurrentUser();
      if (!StringUtils.isNullOrBlank(customAttributeIncrementValueName)) {
        int incrementValue = Integer.parseInt(customAttributeIncrementValueName);
        notifyResult(brazeUser.incrementCustomUserAttribute(customAttributeIncrementKeyName, incrementValue),
            "Increment user attribute! key=" + customAttributeIncrementKeyName + ", value=" + customAttributeIncrementValueName);
      } else {
        notifyResult(brazeUser.incrementCustomUserAttribute(customAttributeIncrementKeyName),
            "Increment user attribute! key=" + customAttributeIncrementKeyName + ", value=1");
      }
    }
    if (!StringUtils.isNullOrBlank(customAttributeUnsetKeyName)) {
      BrazeUser brazeUser = Braze.getInstance(getContext()).getCurrentUser();
      notifyResult(brazeUser.unsetCustomUserAttribute(customAttributeUnsetKeyName), "Unset user attribute! key=" + customAttributeUnsetKeyName);
    }
    if (!StringUtils.isNullOrBlank(customAttributeArrayKey)) {
      if (StringUtils.isNullOrBlank(customAttributeArrayValue)) {
        customAttributeArrayValue = EMPTY_STRING;
      }
      BrazeUser brazeUser = Braze.getInstance(getContext()).getCurrentUser();
      switch (attributeArrayResourceId) {
        case R.id.custom_attribute_array_set:
          String[] attributeArray = {customAttributeArrayValue};
          notifyResult(brazeUser.setCustomAttributeArray(customAttributeArrayKey, attributeArray),
              "setCustomAttributeArray! Setting new array key=" + customAttributeArrayKey + ", values={" + customAttributeArrayValue + "}");
          break;
        case R.id.custom_attribute_array_add:
          notifyResult(brazeUser.addToCustomAttributeArray(customAttributeArrayKey, customAttributeArrayValue),
              "addToCustomAttributeArray! Adding value=" + customAttributeArrayValue + " to array with key=" + customAttributeArrayKey + ".");
          break;
        case R.id.custom_attribute_array_remove:
          notifyResult(brazeUser.removeFromCustomAttributeArray(customAttributeArrayKey, customAttributeArrayValue),
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
      Braze.getInstance(getContext()).requestImmediateDataFlush();
    }
    this.dismiss();
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
