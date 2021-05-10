package com.appboy.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appboy.sample.dialog.CustomDialogBase;
import com.appboy.sample.util.LifecycleUtils;

import java.util.Map;

public class SetEnvironmentPreference extends CustomDialogBase {
  private static final String OVERRIDE_API_KEY_ALIAS_PREF_KEY = "override_api_key_alias";

  private TextView mApiKeyAliasTextView;
  private TextView mApiKeyTextView;
  private TextView mEndpointTextView;
  private Context mApplicationContext;
  private SharedPreferences mSharedPreferences;
  private SharedPreferences mApiKeySharedPreferences;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mApplicationContext = getContext().getApplicationContext();
    return inflater.inflate(R.layout.set_environment_preference, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
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
    if (!apiKeys.containsKey("Default")) {
      String appboyXmlApiKey = DroidboyApplication.getApiKeyInUse(getContext());
      storedApiKeyLinearLayout.addView(getApiKeyButton("Default", appboyXmlApiKey));
    }
    // populate previously stored API keys
    for (final String alias : apiKeys.keySet()) {
      final String apiKey = mApiKeySharedPreferences.getString(alias, null);
      storedApiKeyLinearLayout.addView(getApiKeyButton(alias, apiKey));
    }
  }

  private Button getApiKeyButton(final String alias, final String apiKey) {
    Button button = new Button(getContext());
    button.setOnClickListener(
        view -> {
          mApiKeyAliasTextView.setText(alias);
          mApiKeyTextView.setText(apiKey);
        }
    );
    button.setText(alias + ": " + apiKey);
    return button;
  }

  @Override
  @SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
  public void onExitButtonPressed(boolean clickedPositiveButton) {
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
