package com.appboy.sample;

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
import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.sample.util.ButtonUtils;
import com.appboy.ui.support.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

public class CustomLoggingDialog extends DialogPreference {
  private EditText mCustomAttributeKey;
  private EditText mCustomAttributeValue;
  private EditText mCustomAttributeIncrementKey;
  private EditText mCustomAttributeIncrementValue;
  private EditText mCustomAttributeUnsetKey;
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
  private CheckBox mRequestFlush;

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
    mCustomAttributeIncrementKey = (EditText) view.findViewById(R.id.custom_attribute_increment_key);
    mCustomAttributeIncrementValue = (EditText) view.findViewById(R.id.custom_attribute_increment_value);
    mCustomAttributeUnsetKey = (EditText) view.findViewById(R.id.custom_attribute_unset_key);
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
    mRequestFlush = (CheckBox) view.findViewById(R.id.custom_logging_flush_checkbox);
    return view;
  }

  @Override
  protected void onBindDialogView(View view) {
    mCustomAttributeArrayChoices.check(R.id.custom_attribute_array_set);

    ButtonUtils.setUpPopulateButton(view, R.id.custom_attribute_button, mCustomAttributeKey, "color", mCustomAttributeValue, "green");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_attribute_increment_button, mCustomAttributeIncrementKey, "height", mCustomAttributeIncrementValue, "10");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_attribute_unset_button, mCustomAttributeUnsetKey, "color");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_event_button, mCustomEventName, "touchdown");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_event_property_button, mCustomEventPropertyKey, "type", mCustomEventPropertyValue, "pass");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_purchase_button, mCustomPurchaseName, "football");
    ButtonUtils.setUpPopulateButton(view, R.id.purchase_qty_button, mCustomPurchaseQuantity, "5");
    ButtonUtils.setUpPopulateButton(view, R.id.purchase_property_button, mCustomPurchasePropertyKey, "size", mCustomPurchasePropertyValue, "large");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_attribute_array_button, mCustomAttributeArrayKey, "toys", mCustomAttributeArrayValue, "doll");
    mRequestFlush.setChecked(false);
    super.onBindDialogView(view);
  }

  @Override
  public void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      String customAttributeKeyName = mCustomAttributeKey.getText().toString();
      String customAttributeValueName = mCustomAttributeValue.getText().toString();
      String customAttributeIncrementKeyName = mCustomAttributeIncrementKey.getText().toString();
      String customAttributeIncrementValueName = mCustomAttributeIncrementValue.getText().toString();
      String customAttributeUnsetKeyName = mCustomAttributeUnsetKey.getText().toString();
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

      if (!StringUtils.isNullOrBlank(customAttributeKeyName)) {
        if (StringUtils.isNullOrBlank(customAttributeValueName)) {
          customAttributeValueName = "default";
        }
        AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
        notifyResult(appboyUser.setCustomUserAttribute(customAttributeKeyName, customAttributeValueName), "set user attribute! key=" + customAttributeKeyName + ", value=" + customAttributeValueName);
      }
      if (!StringUtils.isNullOrBlank(customAttributeIncrementKeyName)) {
        AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
        if (!StringUtils.isNullOrBlank(customAttributeIncrementValueName)) {
          int incrementValue = Integer.parseInt(customAttributeIncrementValueName);
          notifyResult(appboyUser.incrementCustomUserAttribute(customAttributeIncrementKeyName, incrementValue), "Increment user attribute! key=" + customAttributeIncrementKeyName + ", value=" + customAttributeIncrementValueName);
        } else {
          notifyResult(appboyUser.incrementCustomUserAttribute(customAttributeIncrementKeyName), "Increment user attribute! key=" + customAttributeIncrementKeyName + ", value=1");
        }
      }
      if (!StringUtils.isNullOrBlank(customAttributeUnsetKeyName)) {
        AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
        notifyResult(appboyUser.unsetCustomUserAttribute(customAttributeUnsetKeyName), "Unset user attribute! key=" + customAttributeUnsetKeyName);
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
        switch (attributeArrayResourceId) {
          case R.id.custom_attribute_array_set:
            String[] attributeArray = new String[]{customAttributeArrayValue};
            notifyResult(appboyUser.setCustomAttributeArray(customAttributeArrayKey, attributeArray), "setCustomAttributeArray! Setting new array key="
                + customAttributeArrayKey + ", values={" + customAttributeArrayValue + "}");
            break;
          case R.id.custom_attribute_array_add:
            notifyResult(appboyUser.addToCustomAttributeArray(customAttributeArrayKey, customAttributeArrayValue), "addToCustomAttributeArray! Adding value="
                + customAttributeArrayValue + " to array with key=" + customAttributeArrayKey + ".");
            break;
          case R.id.custom_attribute_array_remove:
            notifyResult(appboyUser.removeFromCustomAttributeArray(customAttributeArrayKey, customAttributeArrayValue), "removeFromCustomAttributeArray! Will remove value="
                + customAttributeArrayValue + " from array with key=" + customAttributeArrayKey + ".");
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
  }

  private void notifyResult(boolean result, String input) {
    if (result) {
      Toast.makeText(getContext(), "Successfully logged " + input + ".", Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(getContext(), "Failed to log " + input + ".", Toast.LENGTH_LONG).show();
    }
  }
}