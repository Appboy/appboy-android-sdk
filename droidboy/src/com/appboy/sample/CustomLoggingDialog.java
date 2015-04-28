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
import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.ui.support.StringUtils;
import java.math.BigDecimal;
import java.util.Date;

public class CustomLoggingDialog extends DialogPreference {
  private EditText mCustomAttributeKey;
  private EditText mCustomAttributeValue;
  private EditText mCustomEventName;
  private EditText mCustomEventPropertyKey;
  private EditText mCustomEventPropertyValue;
  private EditText mCustomPurchasePropertyKey;
  private EditText mCustomPurchasePropertyValue;
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
    mCustomEventPropertyKey =  (EditText) view.findViewById(R.id.custom_event_property_key);
    mCustomEventPropertyValue =  (EditText) view.findViewById(R.id.custom_event_property_value);
    mCustomPurchaseName = (EditText) view.findViewById(R.id.custom_purchase);
    mCustomPurchaseQuantity = (EditText) view.findViewById(R.id.purchase_qty);
    mCustomPurchasePropertyKey =  (EditText) view.findViewById(R.id.purchase_property_key);
    mCustomPurchasePropertyValue =  (EditText) view.findViewById(R.id.purchase_property_value);
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
      String customEventPropertyKey = mCustomEventPropertyKey.getText().toString();
      String customEventPropertyValue = mCustomEventPropertyValue.getText().toString();
      String customPurchaseName = mCustomPurchaseName.getText().toString();
      String customPurchaseQuantity = mCustomPurchaseQuantity.getText().toString();
      String customPurchasePropertyKey = mCustomPurchasePropertyKey.getText().toString();
      String customPurchasePropertyValue = mCustomPurchasePropertyValue.getText().toString();
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
        if (!StringUtils.isNullOrBlank(customEventPropertyKey)) {
          AppboyProperties eventProperties = new AppboyProperties();
          if (StringUtils.isNullOrBlank(customEventPropertyValue)) {
            customEventPropertyValue = "default";
          }
          eventProperties.addProperty(customEventPropertyKey, customEventPropertyValue);
          eventProperties.addProperty("time", new Date(System.currentTimeMillis()));
          eventProperties.addProperty("boolean", false);
          eventProperties.addProperty("double", 2.5);
          eventProperties.addProperty("integer", 3);
          eventProperties.addProperty("string", "string");
          notifyResult(Appboy.getInstance(getContext()).logCustomEvent(customEventName, eventProperties),
              String.format("custom event: %s. Custom properties: %s, %s",customEventName, customEventPropertyKey, customEventPropertyValue));
        } else {
          notifyResult(Appboy.getInstance(getContext()).logCustomEvent(customEventName), "custom event: " + customEventName);
        }
      }
      if (!StringUtils.isNullOrBlank(customPurchaseName)) {
        if (StringUtils.isNullOrBlank(customPurchaseQuantity) && StringUtils.isNullOrBlank(customPurchasePropertyKey)) {
          notifyResult(Appboy.getInstance(getContext()).logPurchase(customPurchaseName, "USD", BigDecimal.ONE), "single purchase of: " + customPurchaseName);
        } else if (StringUtils.isNullOrBlank(customPurchasePropertyKey)) {
          notifyResult(Appboy.getInstance(getContext()).logPurchase(customPurchaseName, "USD", BigDecimal.ONE, Integer.parseInt(customPurchaseQuantity)),
              customPurchaseQuantity + " purchases of: " + customPurchaseName);
        } else {
          AppboyProperties purchaseProperties = new AppboyProperties();
          if (StringUtils.isNullOrBlank(customPurchasePropertyValue)) {
            customPurchasePropertyValue = "default";
          }
          purchaseProperties.addProperty(customPurchasePropertyKey, customPurchasePropertyValue);
          purchaseProperties.addProperty("time", new Date(System.currentTimeMillis()));
          purchaseProperties.addProperty("boolean", true);
          purchaseProperties.addProperty("double", 1.5);
          purchaseProperties.addProperty("integer", 2);
          purchaseProperties.addProperty("string", "string");
          if (StringUtils.isNullOrBlank(customPurchaseQuantity)) {
            notifyResult(Appboy.getInstance(getContext()).logPurchase(customPurchaseName, "USD", BigDecimal.ONE, purchaseProperties),
                String.format("single purchase: %s. Custom properties: %s, %s", customPurchaseName, customPurchasePropertyKey, customPurchasePropertyValue));
          } else {
            notifyResult(Appboy.getInstance(getContext()).logPurchase(customPurchaseName, "USD", BigDecimal.ONE, Integer.parseInt(customPurchaseQuantity), purchaseProperties),
                String.format("%s purchases of %s. Custom properties: %s, %s", customPurchaseQuantity, customPurchaseName, customPurchasePropertyKey, customPurchasePropertyValue));
          }
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