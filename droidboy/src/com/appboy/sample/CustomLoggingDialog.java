package com.appboy.sample;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.ui.support.StringUtils;
import java.math.BigDecimal;

public class CustomLoggingDialog extends DialogPreference {
  private EditText mCustomAttributeKeyName;
  private EditText mCustomEventName;
  private EditText mCustomPurchaseName;

  public CustomLoggingDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
    setDialogLayoutResource(R.layout.custom_logging);
    setPersistent(false);
  }

  @Override
  public View onCreateDialogView() {
    View view = super.onCreateDialogView();
    mCustomAttributeKeyName = (EditText) view.findViewById(R.id.custom_attribute_key);
    mCustomEventName = (EditText) view.findViewById(R.id.custom_event);
    mCustomPurchaseName = (EditText) view.findViewById(R.id.custom_purchase);
    return view;
  }

  @Override
  protected void onBindDialogView(View view) {
    super.onBindDialogView(view);
  }

  @Override
  public void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      String customAttributeKeyName = mCustomAttributeKeyName.getText().toString();
      String customEventName = mCustomEventName.getText().toString();
      String customPurchaseName = mCustomPurchaseName.getText().toString();

      if (!StringUtils.isNullOrBlank(customAttributeKeyName)) {
        AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
        appboyUser.setCustomUserAttribute(customAttributeKeyName, "awesome");
        notifyResult( appboyUser.setCustomUserAttribute(customAttributeKeyName, "awesome"), customAttributeKeyName);
      }
      if (!StringUtils.isNullOrBlank(customEventName)) {
        notifyResult(Appboy.getInstance(getContext()).logCustomEvent(customEventName), customEventName);
      }
      if (!StringUtils.isNullOrBlank(customPurchaseName)) {
        notifyResult(Appboy.getInstance(getContext()).logPurchase(customPurchaseName, "USD", BigDecimal.ONE), customPurchaseName);
      }
    }
  }

  private void notifyResult(boolean result, String input) {
    if (result) {
      Toast.makeText(getContext(), "Successfully logged " + input, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(getContext(), "Failed to log " + input, Toast.LENGTH_SHORT).show();
    }
  }
}