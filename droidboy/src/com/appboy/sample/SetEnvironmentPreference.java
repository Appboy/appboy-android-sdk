package com.appboy.sample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.appboy.Constants;
import com.appboy.support.AppboyLogger;

public class SetEnvironmentPreference extends DialogPreference implements DialogInterface.OnDismissListener {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, SetEnvironmentPreference.class.getName());
  private View mView;
  private final Context mApplicationContext;

  public SetEnvironmentPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    mApplicationContext = context.getApplicationContext();
    setDialogLayoutResource(R.layout.set_environment_preference);
    setPersistent(false);
  }

  @Override
  protected View onCreateDialogView() {
    mView = super.onCreateDialogView();

    SharedPreferences sharedPreferences = mApplicationContext.getSharedPreferences(mApplicationContext.getString(R.string.shared_prefs_location), Context.MODE_PRIVATE);
    String overrideApiKey = sharedPreferences.getString(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY, null);
    String overrideEndpointUrl = sharedPreferences.getString(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY, null);

    TextView overrideApiKeyView = (TextView) mView.findViewById(R.id.set_environment_override_api_key);
    TextView overrideEndpointView = (TextView) mView.findViewById(R.id.set_environment_override_endpoint_url);
    if (overrideApiKey != null) {
      overrideApiKeyView.setText(overrideApiKey);
    }
    if (overrideEndpointUrl != null) {
      overrideEndpointView.setText(overrideEndpointUrl);
    }

    return mView;
  }

  @Override
  protected void onDialogClosed(boolean clickedPositiveButton) {
    super.onDialogClosed(clickedPositiveButton);
    if (clickedPositiveButton) {
      TextView overrideApiKeyView = (TextView) mView.findViewById(R.id.set_environment_override_api_key);
      TextView overrideEndpointView = (TextView) mView.findViewById(R.id.set_environment_override_endpoint_url);
      SharedPreferences sharedPreferences = mApplicationContext.getSharedPreferences(mApplicationContext.getString(R.string.shared_prefs_location), Context.MODE_PRIVATE);
      SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
      if (overrideApiKeyView.getText().length() > 0) {
        sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY, overrideApiKeyView.getText().toString());
      } else {
        sharedPreferencesEditor.remove(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY);
      }
      if (overrideEndpointView.getText().length() > 0) {
        sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY, overrideEndpointView.getText().toString());
      } else {
        sharedPreferencesEditor.remove(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY);
      }

      sharedPreferencesEditor.commit();
      restartApp();
    }
  }

  private void restartApp() {
    Intent startActivity = new Intent(mApplicationContext, DroidBoyActivity.class);
    int pendingIntentId = 109829837;
    PendingIntent pendingIntent = PendingIntent.getActivity(mApplicationContext, pendingIntentId, startActivity, PendingIntent.FLAG_CANCEL_CURRENT);
    AlarmManager alarmManager = (AlarmManager) mApplicationContext.getSystemService(Context.ALARM_SERVICE);
    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
    AppboyLogger.i(TAG, "Restarting application to apply new environment values");
    System.exit(0);
  }
}
