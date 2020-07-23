package com.appboy.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appboy.sample.util.LifecycleUtils;

import java.util.Map;

public class SetEnvironmentPreference extends DialogPreference implements DialogInterface.OnDismissListener {
  private static final String OVERRIDE_API_KEY_ALIAS_PREF_KEY = "override_api_key_alias";

  private TextView mApiKeyAliasTextView;
  private TextView mApiKeyTextView;
  private TextView mEndpointTextView;
  private final Context mApplicationContext;
  private SharedPreferences mSharedPreferences;
  private SharedPreferences mApiKeySharedPreferences;

  public SetEnvironmentPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    mApplicationContext = context.getApplicationContext();
    setDialogLayoutResource(R.layout.set_environment_preference);
    setPersistent(false);
  }

  @Override
  protected View onCreateDialogView() {
    View view = super.onCreateDialogView();

    mSharedPreferences = mApplicationContext.getSharedPreferences(mApplicationContext.getString(R.string.shared_prefs_location), Context.MODE_PRIVATE);
    String overrideApiKeyAlias = mSharedPreferences.getString(OVERRIDE_API_KEY_ALIAS_PREF_KEY, null);
    String overrideApiKey = mSharedPreferences.getString(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY, null);
    String overrideEndpointUrl = mSharedPreferences.getString(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY, null);

    mApiKeyAliasTextView = view.findViewById(R.id.set_environment_override_api_key_alias);
    mApiKeyTextView = view.findViewById(R.id.set_environment_override_api_key);
    mEndpointTextView = view.findViewById(R.id.set_environment_override_endpoint_url);
    if (overrideApiKeyAlias != null) {
      mApiKeyAliasTextView.setText(overrideApiKeyAlias);
    }
    if (overrideApiKey != null) {
      mApiKeyTextView.setText(overrideApiKey);
    }
    if (overrideEndpointUrl != null) {
      mEndpointTextView.setText(overrideEndpointUrl);
    }

    LinearLayout storedApiKeyLinearLayout = view.findViewById(R.id.stored_api_key_layout);

    mApiKeySharedPreferences = mApplicationContext.getSharedPreferences(mApplicationContext.getString(R.string.api_key_shared_prefs_location), Context.MODE_PRIVATE);
    Map<String, ?> apiKeys = mApiKeySharedPreferences.getAll();

    // populate default API key
    if (!apiKeys.keySet().contains("Default")) {
      String appboyXmlApiKey = DroidboyApplication.getApiKeyInUse(getContext());
      storedApiKeyLinearLayout.addView(getApiKeyButton("Default", appboyXmlApiKey));
    }
    // populate previously stored API keys
    for (final String alias : apiKeys.keySet()) {
      final String apiKey = mApiKeySharedPreferences.getString(alias, null);
      storedApiKeyLinearLayout.addView(getApiKeyButton(alias, apiKey));
    }

    return view;
  }

  private Button getApiKeyButton(final String alias, final String apiKey) {
    Button button = new Button(getContext());
    button.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            mApiKeyAliasTextView.setText(alias);
            mApiKeyTextView.setText(apiKey);
          }
        }
    );
    button.setText(alias + ": " + apiKey);
    return button;
  }

  @Override
  @SuppressLint("CommitPrefEdits")
  protected void onDialogClosed(boolean clickedPositiveButton) {
    super.onDialogClosed(clickedPositiveButton);
    if (clickedPositiveButton) {
      SharedPreferences.Editor sharedPreferencesEditor = mSharedPreferences.edit();
      String apiKeyAlias = mApiKeyAliasTextView.getText().toString();
      String apiKey = mApiKeyTextView.getText().toString();
      String endpoint = mEndpointTextView.getText().toString();
      if (apiKeyAlias.length() > 0) {
        sharedPreferencesEditor.putString(OVERRIDE_API_KEY_ALIAS_PREF_KEY, apiKeyAlias);
      } else {
        sharedPreferencesEditor.remove(OVERRIDE_API_KEY_ALIAS_PREF_KEY);
      }
      if (apiKey.length() > 0) {
        sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY, apiKey);
      } else {
        sharedPreferencesEditor.remove(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY);
      }
      if (apiKeyAlias.length() > 0 && apiKey.length() > 0) {
        mApiKeySharedPreferences.edit().putString(apiKeyAlias, apiKey).commit();
      }
      if (endpoint.length() > 0) {
        sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY, endpoint);
      } else {
        sharedPreferencesEditor.remove(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY);
      }

      sharedPreferencesEditor.commit();
      LifecycleUtils.restartApp(mApplicationContext);
    }
  }
}
