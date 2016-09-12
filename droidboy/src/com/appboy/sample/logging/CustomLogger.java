package com.appboy.sample.logging;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import com.appboy.Appboy;
import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.sample.R;
import com.appboy.sample.util.ButtonUtils;
import com.appboy.support.StringUtils;

public abstract class CustomLogger extends DialogPreference {
  private CheckBox mRequestFlush;
  private Context mContext;
  private EditText mName;
  private EditText mPropertyKey;
  private EditText mPropertyValue;
  private LinearLayout mPropertyLayout;
  private Spinner mTypeSpinner;
  private Button mAddProperty;
  private View mView;
  private PropertyManager mPropertyManager;

  public CustomLogger(Context context, AttributeSet attributeSet, int resourceId) {
    super(context, attributeSet);
    mContext = context;
    setDialogLayoutResource(resourceId);
    setPersistent(false);
  }

  @Override
  protected View onCreateDialogView() {
    mView = super.onCreateDialogView();
    mName = (EditText) mView.findViewById(R.id.custom_name);
    mPropertyKey = (EditText) mView.findViewById(R.id.property_key);
    mPropertyValue = (EditText) mView.findViewById(R.id.property_value);
    mRequestFlush = (CheckBox) mView.findViewById(R.id.custom_logging_flush_checkbox);
    mTypeSpinner = (Spinner) mView.findViewById(R.id.property_type_spinner);
    mAddProperty = (Button) mView.findViewById(R.id.add_property_button);
    mPropertyLayout = (LinearLayout) mView.findViewById(R.id.property_linear_layout);

    ButtonUtils.setUpPopulateButton(mView, R.id.custom_name_button, mName, "football");

    mRequestFlush.setChecked(false);

    mPropertyManager = new PropertyManager(mContext, mPropertyLayout, mPropertyKey, mPropertyValue, mTypeSpinner, mAddProperty);
    return mView;
  }

  private void notifyResult(boolean result, String input) {
    if (result) {
      Toast.makeText(mContext, "Successfully logged " + input + ".", Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(mContext, "Failed to log " + input + ".", Toast.LENGTH_LONG).show();
    }
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);
    if (positiveResult) {
      String customName = mName.getText().toString();
      if (!StringUtils.isNullOrBlank(customName)) {
        notifyResult(customLog(customName, mPropertyManager.getAppboyProperties()), customName);
      } else {
        Toast.makeText(mContext, "Must input a name", Toast.LENGTH_LONG).show();
      }

      // Flushing manually is not recommended in almost all production situations as
      // Appboy automatically flushes data to its servers periodically.  This call
      // is solely for testing purposes.
      if (mRequestFlush.isChecked()) {
        Appboy.getInstance(mContext).requestImmediateDataFlush();
      }
    }
  }

  protected abstract boolean customLog(String name, AppboyProperties properties);
}
