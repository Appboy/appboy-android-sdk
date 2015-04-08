package com.appboy.sample;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.ui.support.StringUtils;
import java.math.BigDecimal;

public class CustomLoggingDialog extends DialogPreference {
  private EditText mCustomAttributeKey;
  private EditText mCustomAttributeValue;
  private EditText mCustomEventName;
  private EditText mCustomPurchaseName;
  private EditText mCustomPurchaseQuantity;
  private EditText mCustomAttributeArrayKey;
  private EditText mCustomAttributeArrayValue;
  private RadioGroup mCustomAttributeArrayChoices;

  public CustomLoggingDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
    setDialogLayoutResource(R.layout.custom_logging);
    setPersistent(false);
  }

  @Override
  public View onCreateDialogView() {
    View view = super.onCreateDialogView();
    mCustomAttributeKey = (EditText) view.findViewById(R.id.custom_attribute_key);
    mCustomAttributeValue = (EditText) view.findViewById(R.id.custom_attribute_value);
    mCustomEventName = (EditText) view.findViewById(R.id.custom_event);
    mCustomPurchaseName = (EditText) view.findViewById(R.id.custom_purchase);
    mCustomPurchaseQuantity = (EditText) view.findViewById(R.id.purchase_qty);
    mCustomAttributeArrayKey = (EditText) view.findViewById(R.id.custom_attribute_array_key);
    mCustomAttributeArrayValue = (EditText) view.findViewById(R.id.custom_attribute_array_value);
    mCustomAttributeArrayChoices = (RadioGroup) view.findViewById(R.id.custom_attribute_array_radio);
    return view;
  }

  @Override
  protected void onBindDialogView(View view) {
    mCustomAttributeArrayChoices.check(R.id.custom_attribute_array_set);
    super.onBindDialogView(view);
  }

  @Override
  public void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      String customAttributeKeyName = mCustomAttributeKey.getText().toString();
      String customAttributeValueName = mCustomAttributeValue.getText().toString();
      String customEventName = mCustomEventName.getText().toString();
      String customPurchaseName = mCustomPurchaseName.getText().toString();
      String customPurchaseQuantity = mCustomPurchaseQuantity.getText().toString();
      String customAttributeArrayKey = mCustomAttributeArrayKey.getText().toString();
      String customAttributeArrayValue = mCustomAttributeArrayValue.getText().toString();
      int attributeArrayResourceId = mCustomAttributeArrayChoices.getCheckedRadioButtonId();
      View attributeArrayRadioButton = mCustomAttributeArrayChoices.findViewById(attributeArrayResourceId);
      int attributeArrayRadioButtonIndex = mCustomAttributeArrayChoices.indexOfChild(attributeArrayRadioButton);

      if (!StringUtils.isNullOrBlank(customAttributeKeyName)) {
        if (StringUtils.isNullOrBlank(customAttributeValueName)) {
          customAttributeValueName = "default";
        }
        AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
        appboyUser.setCustomUserAttribute(customAttributeKeyName, customAttributeValueName);
        notifyResult(appboyUser.setCustomUserAttribute(customAttributeKeyName, customAttributeValueName), "user attribute! key=" + customAttributeKeyName + ", value=" + customAttributeValueName);
      }
      if (!StringUtils.isNullOrBlank(customEventName)) {
        notifyResult(Appboy.getInstance(getContext()).logCustomEvent(customEventName), "custom event: " + customEventName);
      }
      if (!StringUtils.isNullOrBlank(customPurchaseName)) {
        if (!StringUtils.isNullOrBlank(customPurchaseQuantity)) {
          notifyResult(Appboy.getInstance(getContext()).logPurchase(customPurchaseName, "USD", BigDecimal.ONE, Integer.parseInt(customPurchaseQuantity)),
              customPurchaseQuantity + " purchases of: " + customPurchaseName);
        } else {
          notifyResult(Appboy.getInstance(getContext()).logPurchase(customPurchaseName, "USD", BigDecimal.ONE), "single purchase of: " + customPurchaseName);
        }
      }
      if (!StringUtils.isNullOrBlank(customAttributeArrayKey)) {
        if (StringUtils.isNullOrBlank(customAttributeArrayValue)) {
          customAttributeArrayValue = "default";
        }
        AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
        switch (attributeArrayRadioButtonIndex) {
          case 0:
            String[] attributeArray = new String[]{customAttributeArrayValue};
            notifyResult(appboyUser.setCustomAttributeArray(customAttributeArrayKey, attributeArray), "setCustomAttributeArray! Setting new array key="
                + customAttributeArrayKey + ", values={" + customAttributeArrayValue + "}");
            break;
          case 1:
            notifyResult(appboyUser.addToCustomAttributeArray(customAttributeArrayKey, customAttributeArrayValue), "addToCustomAttributeArray! Adding value="
                + customAttributeArrayValue + " to array with key=" + customAttributeArrayKey + ".");
            break;
          case 2:
            notifyResult(appboyUser.removeFromCustomAttributeArray(customAttributeArrayKey, customAttributeArrayValue), "removeFromCustomAttributeArray! Will remove value="
                + customAttributeArrayValue + " from array with key=" + customAttributeArrayKey + ".");
            break;
          default:
            notifyResult(false, "Error parsing attribute array radio button.");
        }
      }
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