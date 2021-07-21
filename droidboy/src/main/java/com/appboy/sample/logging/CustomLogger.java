package com.appboy.sample.logging;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.sample.R;
import com.appboy.sample.dialog.CustomDialogBase;
import com.appboy.sample.util.ButtonUtils;
import com.braze.Braze;
import com.braze.support.StringUtils;

public abstract class CustomLogger extends CustomDialogBase {
  private Context mContext;
  private EditText mName;
  private PropertyManager mPropertyManager;

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mContext = view.getContext();
    mName = view.findViewById(R.id.custom_name);
    EditText propertyKey = view.findViewById(R.id.property_key);
    EditText propertyValue = view.findViewById(R.id.property_value);
    Spinner typeSpinner = view.findViewById(R.id.property_type_spinner);
    Button addProperty = view.findViewById(R.id.add_property_button);
    LinearLayout propertyLayout = view.findViewById(R.id.property_linear_layout);

    ButtonUtils.setUpPopulateButton(view, R.id.custom_name_button, mName, "football");

    mPropertyManager = new PropertyManager(mContext, propertyLayout, propertyKey, propertyValue, typeSpinner, addProperty);
  }

  @Override
  public void onExitButtonPressed(boolean isImmediateFlushRequired) {
    String customName = mName.getText().toString();
    if (!StringUtils.isNullOrBlank(customName)) {
      customLog(customName, mPropertyManager.getAppboyProperties());
      notifyResult(customName);
    } else {
      Toast.makeText(mContext, "Must input a name", Toast.LENGTH_LONG).show();
    }

    // Flushing manually is not recommended in almost all production situations as
    // Braze automatically flushes data to its servers periodically. This call
    // is solely for testing purposes.
    if (isImmediateFlushRequired) {
      Braze.getInstance(mContext).requestImmediateDataFlush();
    }
    this.dismiss();
  }

  private void notifyResult(String input) {
    Toast.makeText(mContext, "Successfully submitted " + input + ".", Toast.LENGTH_LONG).show();
  }

  protected abstract void customLog(String name, AppboyProperties properties);
}
