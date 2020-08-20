package com.appboy.sample.logging;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
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
  private final Context mContext;
  private EditText mName;
  private PropertyManager mPropertyManager;

  public CustomLogger(Context context, AttributeSet attributeSet, int resourceId) {
    super(context, attributeSet);
    mContext = context;
    setDialogLayoutResource(resourceId);
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
    mName = view.findViewById(R.id.custom_name);
    EditText propertyKey = view.findViewById(R.id.property_key);
    EditText propertyValue = view.findViewById(R.id.property_value);
    Spinner typeSpinner = view.findViewById(R.id.property_type_spinner);
    Button addProperty = view.findViewById(R.id.add_property_button);
    LinearLayout propertyLayout = view.findViewById(R.id.property_linear_layout);

    ButtonUtils.setUpPopulateButton(view, R.id.custom_name_button, mName, "football");

    mPropertyManager = new PropertyManager(mContext, propertyLayout, propertyKey, propertyValue, typeSpinner, addProperty);
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

  private void notifyResult(String input) {
    Toast.makeText(mContext, "Successfully submitted " + input + ".", Toast.LENGTH_LONG).show();
  }

  private void onDialogCloseButtonClicked(boolean isImmediateFlushRequired) {
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
      Appboy.getInstance(mContext).requestImmediateDataFlush();
    }
  }

  protected abstract void customLog(String name, AppboyProperties properties);
}
